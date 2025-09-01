        package com.thesis.java.javalearning.service;

    import com.thesis.java.javalearning.dto.CodeResult;
    import com.thesis.java.javalearning.dto.ExecutionRequest;
    import com.thesis.java.javalearning.dto.ExecutionResponse;
    import com.thesis.java.javalearning.entity.ExecutionJob;
    import com.thesis.java.javalearning.entity.ExecutionResult;
    import com.thesis.java.javalearning.repository.ExecutionJobRepository;
    import com.thesis.java.javalearning.repository.ExecutionResultRepository;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Service;

    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.time.Duration;
    import java.time.LocalDateTime;
    import java.util.Map;
    import java.util.UUID;

    @Service
    public class CodeExecutionService {

        private static final String CLASS_NAME = "StudentCode";
        private static final String JAVA_FILE = CLASS_NAME + ".java";

        @Autowired
        private ScoringService scoringService;

        @Autowired
        private ExecutionJobRepository executionJobRepository;

        @Autowired
        private ExecutionResultRepository executionResultRepository;

        @Autowired
        private SseService sseService;

        @Async
        public void executeJobAsync(ExecutionJob job) {
            try {
                job.setStatus("RUNNING");
                executionJobRepository.save(job);

                Path tempDir = Files.createTempDirectory("java_exec_");
                File javaFile = new File(tempDir.toFile(), JAVA_FILE);
                String snippet = job.getSourceCode() == null ? "" : job.getSourceCode().trim();
                boolean hasClass = snippet.contains("class ");
                String source = hasClass
                        ? snippet
                        : """
                        public class %s {
                        public static void main(String[] args) {
                            %s
                        }
                        }
                        """.formatted(CLASS_NAME, snippet);
                Files.writeString(javaFile.toPath(), source);

                Process compile = new ProcessBuilder("javac", JAVA_FILE)
                        .directory(tempDir.toFile())
                        .redirectErrorStream(false)
                        .start();
                String compileOut = readStream(compile.getInputStream());
                String compileErr = readStream(compile.getErrorStream());
                int compileExit = compile.waitFor();

                if (compileExit != 0) {
                    ExecutionResult failedResult = ExecutionResult.builder()
                            .jobId(job.getId())
                            .passed(false)
                            .stdout(compileOut)
                            .stderr(compileErr)
                            .exitCode(compileExit)
                            .scoreCalculated(0)
                            .executedAt(LocalDateTime.now())
                            .build();
                    executionResultRepository.save(failedResult);
                    job.setStatus("FAILED");
                    executionJobRepository.save(job);
                    sseService.sendResult(job.getId(), failedResult);
                    return;
                }

                Process run = new ProcessBuilder("java", "-cp", ".", CLASS_NAME)
                        .directory(tempDir.toFile())
                        .redirectErrorStream(false)
                        .start();
                String runOut = readStream(run.getInputStream());
                String runErr = readStream(run.getErrorStream());
                int runExit = run.waitFor();

                String actualNorm = runOut == null ? "" : runOut.replace("\r\n", "\n").trim();
                String expectedNorm = job.getExpectedOutput() == null ? "" : job.getExpectedOutput().replace("\r\n", "\n").trim();
                boolean passed = actualNorm.equals(expectedNorm);

                int score = scoringService.calculateScore(Map.of(), 0, "", 0L, 0L);
                if (score > 100) {
                    score = 100;
                }

                ExecutionResult result = ExecutionResult.builder()
                        .jobId(job.getId())
                        .passed(runExit == 0 && passed)
                        .stdout(actualNorm)
                        .stderr(runErr)
                        .exitCode(runExit)
                        .scoreCalculated(score)
                        .executedAt(LocalDateTime.now())
                        .build();

                executionResultRepository.save(result);
                job.setStatus(runExit == 0 && passed ? "SUCCESS" : "FAILED");
                executionJobRepository.save(job);
                sseService.sendResult(job.getId(), result);
            } catch (Exception e) {
                ExecutionResult failedResult = ExecutionResult.builder()
                        .jobId(job.getId())
                        .passed(false)
                        .stdout("")
                        .stderr("Execution Error:\n" + e.getMessage())
                        .exitCode(1)
                        .scoreCalculated(0)
                        .executedAt(LocalDateTime.now())
                        .build();
                executionResultRepository.save(failedResult);
                job.setStatus("FAILED");
                executionJobRepository.save(job);
                sseService.sendResult(job.getId(), failedResult);
            }
        }

        @Deprecated(since = "1.0", forRemoval = false)
        public CodeResult executeAndEvaluate(
                String code,
                String expectedOutput,
                Map<String, Integer> hintCounts,
                int failedRuns,
                long elapsedMillis,
                long timeLimitMillis,
                String hintLevelCap,
                String userInput,
                long onTaskTimeSeconds,   // Added parameter
                long offTaskTimeSeconds   // Added parameter
        ) {
            CodeResult result = new CodeResult();
            try {
                if (isLikelyHardcoded(code)) {
                    return new CodeResult(
                            false,
                            "⚠ Output terlihat seperti hardcoded. Gunakan variabel, perhitungan, atau struktur logika seperti if/for/Scanner.",
                            0,
                            failedRuns + 1,
                            "Kode tidak valid – terlalu hardcoded"
                    );
                }

                // 1. write source to temp dir
                Path tempDir = Files.createTempDirectory("java_exec_");
                File javaFile = new File(tempDir.toFile(), JAVA_FILE);
                String snippet = code.trim();
                boolean hasClass = snippet.contains("class ");
                String source = hasClass
                    ? snippet
                    : """
                        public class %s {
                        public static void main(String[] args) {
                            %s
                        }
                        }
                        """.formatted(CLASS_NAME, snippet);
                Files.writeString(javaFile.toPath(), source);

                // 2. compile
                Process compile = new ProcessBuilder("javac", JAVA_FILE)
                        .directory(tempDir.toFile())
                        .redirectErrorStream(true)
                        .start();
                String compileOut = readProcessOutput(compile);
                if (compile.waitFor() != 0) {
                    return new CodeResult(false,
                        "Compilation Error:\n" + compileOut,
                        0,
                        failedRuns + 1,
                        "❌ Compilation failed."
                    );
                }

                // 3. run program
                Process run = new ProcessBuilder("java", "-cp", ".", CLASS_NAME)
                        .directory(tempDir.toFile())
                        .redirectErrorStream(true)
                        .start();
                if (userInput != null && !userInput.isBlank()) {
                    try (OutputStream os = run.getOutputStream()) {
                        os.write(userInput.getBytes());
                        os.flush();
                    }
                }
                String runOut = readProcessOutput(run);
                int runExit = run.waitFor();

                // normalize line-endings and trim
                String actualNorm = runOut == null
                    ? ""
                    : runOut.replace("\r\n", "\n").trim();
                String expectNorm = expectedOutput == null
                    ? ""
                    : expectedOutput.replace("\r\n", "\n").trim();
                boolean passed = actualNorm.equals(expectNorm);

                // 4. score calculation
                int score = scoringService.calculateScore(
                        hintCounts,
                        failedRuns,
    //                    Duration.ofMillis(elapsedMillis),
                        hintLevelCap,
                        onTaskTimeSeconds,   // Passed new parameter
                        offTaskTimeSeconds   // Passed new parameter
                );
                if (score > 100) score = 100;

                // 5. populate result
                result.setSuccess(runExit == 0 && passed);
                result.setOutput(actualNorm);
                result.setScore(score);
                result.setFailedRuns(result.isSuccess() ? failedRuns : failedRuns + 1);
                result.setFeedbackMessage(passed
                    ? "✅ Output matches expected. You can now Submit."
                    : "❌ Submission failed: your output doesn’t match expected."
                );

            } catch (Exception e) {
                result.setSuccess(false);
                result.setOutput("Execution Error:\n" + e.getMessage());
                result.setScore(0);
                result.setFailedRuns(failedRuns + 1);
                result.setFeedbackMessage("❌ Error during execution.");
            }
            return result;
        }

        private String readProcessOutput(Process process) throws IOException {
            return readStream(process.getInputStream());
        }

        private String readStream(InputStream inputStream) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            }
        }

        private boolean isLikelyHardcoded(String code) {
            String noWhitespace = code.replaceAll("\\s+", "").toLowerCase();
            boolean hasOnlyPrint =
                    noWhitespace.matches(".*system\\.out\\.print(ln)?\\([\"'].*[\"']\\).*") &&
                    !noWhitespace.matches(".*(int|double|boolean|float|scanner|if|for|while|switch|catch|try|math|\\+|\\-|\\*|/|%).*");
            return hasOnlyPrint;
        }

    public ExecutionResponse execute(ExecutionRequest request) {

        ExecutionResponse response = new ExecutionResponse();

        try {

            /*
            * STEP 1
            * Create temporary workspace
            *
            * Example:
            * C:/Users/Ade/AppData/Local/Temp/submission-12345
            */
            Path tempDir = Paths.get(
                "/submissions",
                UUID.randomUUID().toString()
            );

            Files.createDirectories(tempDir);

            String hostPath =
                "D:/cue-submissions/" +
                tempDir.getFileName().toString();
            /*
            * STEP 2
            * Write student source code
            */
            Path sourceFile = tempDir.resolve("Main.java");

            Files.writeString(
                    sourceFile,
                    request.getSourceCode()
            );
            System.out.println("SOURCE CONTENT:");
            System.out.println(Files.readString(sourceFile));
            System.out.println("SOURCE FILE EXISTS: " + Files.exists(sourceFile));
            System.out.println("SOURCE FILE SIZE: " + Files.size(sourceFile));
            /*
            * STEP 3
            * Windows Docker volume fix
            *
            * Docker prefers forward slashes
            */
            String dockerVolume =
                    tempDir.toAbsolutePath()
                            .toString()
                            .replace("\\", "/");

            /*
            * STEP 4
            * Launch sandbox container
            */
            
            ProcessBuilder pb = new ProcessBuilder(

                    "docker",
                    "run",

                    "--rm",

                    "--network",
                    "none",

                    "--memory",
                    "256m",

                    "--cpus",
                    "0.5",

                    "--pids-limit",
                    "20",

                    "-v",
                    hostPath + ":/sandbox",

                    "cue-java-runner",

                    "sh",
                    "-c",

                    /*
                    * Compile
                    * Execute
                    *
                    * JVM limited to 128 MB
                    */
                    "mkdir -p /tmp/build && javac -d /tmp/build /sandbox/Main.java && java -cp /tmp/build Main"
            );

            /*
            * Merge stdout + stderr
            *
            * Compilation errors become visible
            */
            pb.redirectErrorStream(true);

            //COLLECT EVIDENCE

            System.out.println("=================================");
            System.out.println("HOST PATH = " + hostPath);
            System.out.println("TEMP DIR: " + tempDir.toAbsolutePath());
            System.out.println("SOURCE FILE: " + sourceFile.toAbsolutePath());
            System.out.println("FILE EXISTS: " + Files.exists(sourceFile));
            System.out.println("DOCKER VOLUME: " + dockerVolume);
            System.out.println("=================================");

            System.out.println("DOCKER COMMAND:");
            System.out.println(pb.command());
            Process process = pb.start();

            /*
            * STEP 5
            * Prevent infinite loops
            */
            boolean finished =
                    process.waitFor(
                            20,
                            java.util.concurrent.TimeUnit.SECONDS
                    );

            if (!finished) {

                System.out.println("PROCESS STILL RUNNING AFTER 20 SECONDS");

                process.destroyForcibly();

                response.setStatus("TIMEOUT");

                response.setOutput(
                        "Execution exceeded 20 second limit."
                );

                return response;
            }

            /*
            * STEP 6
            * Read container output
            */
            StringBuilder output = new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream()
                                    )
                            )
            ) {

                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            /*
            * STEP 7
            * Return result
            */
            int exitCode = process.exitValue();

            response.setOutput(
                    output.toString().trim()
            );

            response.setStatus(
                    exitCode == 0
                            ? "SUCCESS"
                            : "FAILED"
            );

        }
        catch (Exception e) {

            response.setStatus("ERROR");

            response.setOutput(
                    e.getMessage()
            );
        }

        return response;
    }

    }

//package com.thesis.java.javalearning.service;
//
//import com.thesis.java.javalearning.dto.CodeResult;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.util.Map;
//
//@Service
//public class CodeExecutionService {
//
//    @Autowired
//    private ScoringService scoringService;
//
//public CodeResult executeAndEvaluate(String code,
//                                     String expectedOutput,
//                                     Map<String, Integer> hintCounts,
//                                     int failedRuns,
//                                     long elapsedMillis,
//                                     long timeLimitMillis,
//                                     String hintLevelCap,
//                                     String userInput)
// {  // userInput parameter to simulate input
//    try {
//        // 1. Create temporary working directory
//        Path tempDir = Files.createTempDirectory("code");
//        String className = "StudentCode"; // default class name if boilerplate code is used
//        Path javaFilePath = tempDir.resolve(className + ".java");
//
//        // 2. Write student code to file
//        Files.write(javaFilePath, code.getBytes());
//
//        // 3. Compile the code
//        Process compileProcess = new ProcessBuilder("javac", javaFilePath.toString())
//                .redirectErrorStream(true)
//                .directory(tempDir.toFile())
//                .start();
//
//        String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
//        compileProcess.waitFor();
//
//        if (compileProcess.exitValue() != 0) {
//            return new CodeResult(false, "Compilation Error:\n" + compileOutput, 0, failedRuns, "Compilation failed");
//        }
//
//        // 4. Run the compiled class with simulated input for Scanner
//        ProcessBuilder runProcessBuilder = new ProcessBuilder("java", className)
//                .redirectErrorStream(true)
//                .directory(tempDir.toFile());
//
//        // Provide simulated input to the process (stdin)
//        OutputStream inputStream = runProcessBuilder.start().getOutputStream();
//        inputStream.write(userInput.getBytes()); // Feed the user input here
//        inputStream.flush();
//        inputStream.close();
//
//        // Run the process
//        Process runProcess = runProcessBuilder.start();
//        String output = new String(runProcess.getInputStream().readAllBytes());
//        runProcess.waitFor();
//
//        // 5. Compare output to expected, with null protection and trimming
//        String actualTrimmed = output == null ? "" : output.trim();
//        String expectTrimmed = expectedOutput == null ? "" : expectedOutput.trim();
//        boolean passed = actualTrimmed.equals(expectTrimmed);
//
//        // 6. Calculate score based on hints, failed runs, and other factors
//        int score = scoringService.calculateScore(
//                hintCounts,
//                failedRuns,
//                Duration.ofMillis(elapsedMillis),
//                hintLevelCap
//        );
//
//        // Apply score cap if needed
//        if (score > 100) {
//            score = 100; // Maximum score
//        }
//
//        // Return the final result with feedback
//        return new CodeResult(passed, actualTrimmed, score, failedRuns, "Execution completed");
//
//    } catch (Exception e) {
//        return new CodeResult(false, "Execution Error:\n" + e.getMessage(), 0, failedRuns, "Error during execution.");
//    }
//}
//
//
//    private String wrapCode(String studentCode, String className) {
//        return """
//            public class %s {
//                    %s
//                }
//            }
//            """.formatted(className, studentCode);
//    }
//}


//public CodeResult executeAndEvaluate(String code,
//                                     String expectedOutput,
//                                     Map<String, Integer> hintCounts,
//                                     int failedRuns,
//                                     long elapsedMillis,
//                                     long timeLimitMillis,
//                                     String hintLevelCap) {
//    try {
//        // 1. Create temporary working directory
//        Path tempDir = Files.createTempDirectory("code");
//        String className = "StudentCode"; // default class name if boilerplate code is used
//        Path javaFilePath = tempDir.resolve(className + ".java");
//
//        // 2. Wrap and check the code for a main method and class declaration
//        String wrappedCode = wrapCode(code, className);
//
//        // Check if the code contains a main method and class
//        boolean hasMainMethod = wrappedCode.contains("public static void main(String[] args)");
//        boolean hasClassDeclaration = wrappedCode.contains("public class " + className);
//
//        // If missing main method or class declaration, cap the score
//        int scoreCap = (hasMainMethod && hasClassDeclaration) ? 100 : 70;
//        String feedbackMessage = "";
//
//        if (!hasMainMethod) {
//            feedbackMessage += "Error: Missing public static void main(String[] args) method. ";
//        }
//        if (!hasClassDeclaration) {
//            feedbackMessage += "Error: Missing public class declaration. ";
//        }
//
//        // 3. Write student code to file
//        Files.write(javaFilePath, wrappedCode.getBytes());
//
//        // 4. Compile the code
//        Process compileProcess = new ProcessBuilder("javac", javaFilePath.toString())
//                .redirectErrorStream(true)
//                .directory(tempDir.toFile())
//                .start();
//
//        String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
//        compileProcess.waitFor();
//
//        if (compileProcess.exitValue() != 0) {
//            return new CodeResult(false, "Compilation Error:\n" + compileOutput, scoreCap, failedRuns, feedbackMessage);
//        }
//
//        // 5. Run the compiled class
//        Process runProcess = new ProcessBuilder("java", className)
//                .redirectErrorStream(true)
//                .directory(tempDir.toFile())
//                .start();
//
//        String output = new String(runProcess.getInputStream().readAllBytes());
//        runProcess.waitFor();
//
//        // 6. Compare output to expected, with null protection and trimming
//        String actualTrimmed = output == null ? "" : output.trim();
//        String expectTrimmed = expectedOutput == null ? "" : expectedOutput.trim();
//        boolean passed = actualTrimmed.equals(expectTrimmed);
//
//        // 7. Calculate score based on hints, failed runs, and other factors
//        int score = scoringService.calculateScore(
//                hintCounts,
//                failedRuns,
//                Duration.ofMillis(elapsedMillis),
//                hintLevelCap
//        );
//
//        // Apply score cap if needed
//        if (score > scoreCap) {
//            score = scoreCap;
//        }
//
//        // Return the final result with feedback
//        return new CodeResult(passed, actualTrimmed, score, failedRuns, feedbackMessage);
//
//    } catch (Exception e) {
//        return new CodeResult(false, "Execution Error:\n" + e.getMessage(), 0, failedRuns, "Error during execution.");
//    }
//}

