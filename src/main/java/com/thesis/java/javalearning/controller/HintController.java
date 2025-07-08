////package com.thesis.java.javalearning.controller;
////
////import com.thesis.java.javalearning.service.HintGenerationService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////@RestController
////@RequestMapping("/api/hints")
////public class HintController {
////
////    @Autowired
////    private HintGenerationService hintService;
////
////    @PostMapping
////    public ResponseEntity<String> getHint(@RequestBody HintRequest payload) {
////        if (payload == null || payload.getCode() == null || payload.getCode().isBlank()) {
////            return ResponseEntity.badRequest().body("No code provided.");
////        }
////
////        String prompt = buildScaffoldPrompt(payload.getType(), payload.getCode());
////        String hint = hintService.generateHint(prompt);
////
////        return ResponseEntity.ok(hint);
////    }
////
//// private String buildScaffoldPrompt(String type, String code) {
////    return switch (type == null ? "" : type.toLowerCase()) {
////        case "syntax" -> """
////            ONLY give a short, single-sentence hint about **SYNTAX** issues in the following Java code.
////            Do NOT solve the problem. Do NOT include greetings, code snippets, or explain anything outside syntax.
////            Just directly point out the syntax error or missing element (e.g., missing semicolon, incorrect brackets, wrong method name).
////            Keep it to maximum **1 sentence** (no extra tips or comments).
////            Assume the user is a beginner, but knows basic Java.
////            CODE:
////            """ + code;
////
////        case "logic" -> """
////            ONLY give a short, specific hint about **LOGIC** or reasoning mistake(s) in this Java code.
////            Do NOT solve the problem, do NOT restate the code or output, do NOT give any greetings or extra context.
////            Be direct: What is the possible logical flaw, off-by-one, wrong calculation, or misplacement of operation?
////            Limit answer to **1-2 sentences**. No code, no full solution, just point to what to check.
////            CODE:
////            """ + code;
////
////        case "step" -> """
////            Give a **step-by-step plan (2-4 steps)** to solve the Java problem described.
////            Do NOT write any actual code, just break down the logical steps needed to solve the problem, in list form if possible.
////            Do NOT give the solution directly.
////            Do NOT write any greeting or preamble.
////            Each step should be brief, clear, and focused on the thinking process, not code.
////            PROBLEM:
////            """ + code;
////
////        case "concept" -> """
////            ONLY give a **conceptual explanation** (not code) about the underlying, programming concept relevant to solving the task.
////            Example: variable declaration, conditional statement, loop, input/output, data type, etc.
////            Focus on the key concept or principle required to solve the task.
////            Use simple, beginner-friendly language.
////            NO code, NO solution, NO greeting—just the concept explained in 2-3 sentences.
////            PROBLEM:
////            """ + code;
////
////        case "reveal" -> """
////            Write ONLY the correct, final Java code for this problem.
////            NO explanation, NO greeting, NO intro, NO comments.
////            Output the code block ONLY.
////            PROBLEM:
////            """ + code;
////
////        default -> """
////            Give a single, concise hint related to the problem.
////            Do NOT include greetings, explanations, or solutions.
////            Focus on what the user should pay attention to, in **1 sentence only**.
////            PROBLEM:
////            """ + code;
////    };
////}
////
////
////
////public static class HintRequest {
////    private String code;
////    private String type;
////    private Long userId;
////    private Long problemId;
////    private int sessionNumber;
////
////    public String getCode() { return code; }
////    public void setCode(String code) { this.code = code; }
////
////    public String getType() { return type; }
////    public void setType(String type) { this.type = type; }
////
////    public Long getUserId() { return userId; }
////    public void setUserId(Long userId) { this.userId = userId; }
////
////    public Long getProblemId() { return problemId; }
////    public void setProblemId(Long problemId) { this.problemId = problemId; }
////
////    public int getSessionNumber() { return sessionNumber; }
////    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
////}
////
////}
//package com.thesis.java.javalearning.controller;
//
//import com.thesis.java.javalearning.entity.Problem;
//import com.thesis.java.javalearning.repository.ProblemRepository;
//import com.thesis.java.javalearning.service.HintGenerationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/hints")
//public class HintController {
//
//    @Autowired
//    private HintGenerationService hintService;
//
//    @Autowired
//    private ProblemRepository problemRepository;
//
//    @PostMapping
//    public ResponseEntity<String> getHint(@RequestBody HintRequest payload) {
//        if (payload == null) {
//            return ResponseEntity.badRequest().body("Invalid request payload.");
//        }
//
//        String type = payload.getType() == null ? "" : payload.getType().toLowerCase();
//        String promptInput;
//
//        // Ambil deskripsi masalah dari DB jika type 'concept', 
//        // untuk type lain, gunakan code. Jika code kosong dan bukan concept, return error.
//        if ("concept".equals(type)) {
//            if (payload.getProblemId() == null) {
//                return ResponseEntity.badRequest().body("Problem ID required for conceptual hint.");
//            }
//            Problem problem = problemRepository.findById(payload.getProblemId())
//                    .orElse(null);
//            if (problem == null) {
//                return ResponseEntity.badRequest().body("Problem not found.");
//            }
//            promptInput = problem.getDescription() != null ? problem.getDescription() : "";
//        } else {
//            // Untuk jenis hint lain, kode harus ada dan tidak kosong
//            if (payload.getCode() == null || payload.getCode().isBlank()) {
//                return ResponseEntity.badRequest().body("Code is required for this hint type.");
//            }
//            promptInput = payload.getCode();
//        }
//
//        String prompt = buildScaffoldPrompt(type, promptInput);
//        String hint = hintService.generateHint(prompt);
//
//        return ResponseEntity.ok(hint);
//    }
//
//    private String buildScaffoldPrompt(String type, String input) {
//        return switch (type) {
//            case "syntax" -> """
//                ONLY give a short, single-sentence hint about **SYNTAX** issues in the following Java code.
//                Do NOT solve the problem. Do NOT include greetings, code snippets, or explain anything outside syntax.
//                Just directly point out the syntax error or missing element (e.g., missing semicolon, incorrect brackets, wrong method name).
//                Keep it to maximum **1 sentence** (no extra tips or comments).
//                Assume the user is a beginner, but knows basic Java.
//                CODE:
//                """ + input;
//
//            case "logic" -> """
//                ONLY give a short, specific hint about **LOGIC** or reasoning mistake(s) in this Java code.
//                Do NOT solve the problem, do NOT restate the code or output, do NOT give any greetings or extra context.
//                Be direct: What is the possible logical flaw, off-by-one, wrong calculation, or misplacement of operation?
//                Limit answer to **1-2 sentences**. No code, no full solution, just point to what to check.
//                CODE:
//                """ + input;
//
//            case "step" -> """
//                Give a **step-by-step plan (2-4 steps)** to solve the Java problem described.
//                Do NOT write any actual code, just break down the logical steps needed to solve the problem, in list form if possible.
//                Do NOT give the solution directly.
//                Do NOT write any greeting or preamble.
//                Each step should be brief, clear, and focused on the thinking process, not code.
//                PROBLEM:
//                """ + input;
//
//            case "concept" -> """
//                ONLY give a **conceptual explanation** (not code) about the underlying, programming concept relevant to solving the task.
//                Example: variable declaration, conditional statement, loop, input/output, data type, etc.
//                Focus on the key concept or principle required to solve the task.
//                Use simple, beginner-friendly language.
//                NO code, NO solution, NO greeting—just the concept explained in 2-3 sentences.
//                PROBLEM:
//                """ + input;
//
//            case "reveal" -> """
//                Write ONLY the correct, final Java code for this problem.
//                NO explanation, NO greeting, NO intro, NO comments.
//                Output the code block ONLY.
//                PROBLEM:
//                """ + input;
//
//            default -> """
//                Give a single, concise hint related to the problem.
//                Do NOT include greetings, explanations, or solutions.
//                Focus on what the user should pay attention to, in **1 sentence only**.
//                PROBLEM:
//                """ + input;
//        };
//    }
//
//    public static class HintRequest {
//        private String code;
//        private String type;
//        private Long userId;
//        private Long problemId;
//        private int sessionNumber;
//
//        // getters and setters...
//
//        public String getCode() { return code; }
//        public void setCode(String code) { this.code = code; }
//        public String getType() { return type; }
//        public void setType(String type) { this.type = type; }
//        public Long getUserId() { return userId; }
//        public void setUserId(Long userId) { this.userId = userId; }
//        public Long getProblemId() { return problemId; }
//        public void setProblemId(Long problemId) { this.problemId = problemId; }
//        public int getSessionNumber() { return sessionNumber; }
//        public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
//    }
//}
//package com.thesis.java.javalearning.controller;
//
//import com.thesis.java.javalearning.entity.Problem;
//import com.thesis.java.javalearning.repository.ProblemRepository;
//import com.thesis.java.javalearning.service.HintGenerationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/hints")
//public class HintController {
//
//    @Autowired
//    private HintGenerationService hintService;
//
//    @Autowired
//    private ProblemRepository problemRepository;
//
//    @PostMapping
//    public ResponseEntity<String> getHint(@RequestBody HintRequest payload) {
//        if (payload == null) {
//            return ResponseEntity.badRequest().body("Invalid request payload.");
//        }
//
//        String type = payload.getType() == null ? "" : payload.getType().toLowerCase();
//        String promptInput;
//        String currentTask = payload.getTask();  // Fetching the current task to be used in the prompt
//
//        // Ambil deskripsi masalah dari DB jika type 'concept', 
//        // untuk type lain, gunakan code. Jika code kosong dan bukan concept, return error.
//        if ("concept".equals(type)) {
//            if (payload.getProblemId() == null) {
//                return ResponseEntity.badRequest().body("Problem ID required for conceptual hint.");
//            }
//            Problem problem = problemRepository.findById(payload.getProblemId())
//                    .orElse(null);
//            if (problem == null) {
//                return ResponseEntity.badRequest().body("Problem not found.");
//            }
//            promptInput = problem.getDescription() != null ? problem.getDescription() : "";
//        } else {
//            // Untuk jenis hint lain, kode harus ada dan tidak kosong
//            if (payload.getCode() == null || payload.getCode().isBlank()) {
//                return ResponseEntity.badRequest().body("Code is required for this hint type.");
//            }
//            promptInput = payload.getCode();
//        }
//
//        // Include the current task context in the prompt to give better hints
//        String prompt = buildScaffoldPrompt(type, promptInput, currentTask);
//        String hint = hintService.generateHint(prompt);
//
//        return ResponseEntity.ok(hint);
//    }
//
//    private String buildScaffoldPrompt(String type, String input, String currentTask) {
//        // Modify the prompt to include the current task context for better hint generation
//        return switch (type) {
//   case "syntax" -> """
//        ONLY give a short, clear explanation about **SYNTAX** issues found in the following Java code.
//        Do NOT solve the problem. Do NOT include greetings, code snippets, or explanations unrelated to syntax.
//        If there is a syntax issue (e.g., missing semicolon, wrong brackets, incorrect method structure), directly point it out in no more than **2 sentences**.
//        If the syntax appears correct, simply state: "The current syntax seems correct."
//        Then, add one suggestion for the next step the user can try to get closer to the solution (e.g., "Try printing the result to check correctness").
//        Assume the user is a beginner who knows basic Java syntax.
//        Always begin your answer with: Syntax Explanation
//
//        CURRENT TASK: """ + currentTask + "\nCODE:\n" + input;
//
//    case "logic" -> """
//        ONLY provide a focused hint about any **LOGICAL MISTAKE** in the Java code (e.g., wrong order, off-by-one, incorrect formula).
//        Do NOT solve the problem or explain the task again. Avoid greetings, code output, or unrelated information.
//        Limit your answer to **1–2 sentences** only. 
//        If the logic seems correct, say: "The current logic seems correct."
//        Then, give 1 helpful suggestion for what the user should try next to verify correctness.
//        Always begin your answer with: Logic Explanation
//
//        CURRENT TASK: """ + currentTask + "\nCODE:\n" + input;
//
//    case "step" -> """
//        Provide a **step-by-step breakdown (2–4 steps)** of what needs to be done to solve the Java problem.
//        Do NOT write any actual code. Do NOT solve the task or give direct answers.
//        Keep each step short, focused on reasoning, and clearly separated.
//        This should guide the thinking process without giving the answer away.
//        Do NOT include greetings or intros.
//        Always begin your answer with: Step By Step Explanation
//
//        CURRENT TASK: """ + currentTask + "\nPROBLEM:\n" + input;
//
//    case "concept" -> """
//        Explain the **main Java programming concept(s)** needed to solve this task, such as data types, operations, control flow, or I/O.
//        Use **clear, beginner-friendly language**, without including code, greetings, or full solutions.
//        Limit the explanation to **under 3 short paragraphs** and focus on helping the user understand the idea behind the task.
//        Always begin your answer with: Concept Explanation
//
//        CURRENT TASK: """ + currentTask + "\nPROBLEM:\n" + input;
//
//    case "reveal" -> """
//        Write ONLY the correct, final Java code for this task.
//        Do NOT include any explanation, greeting, comment, or additional text.
//        Output the code block ONLY.
//        Always begin your answer with: Code Reveal Explanation
//
//        CURRENT TASK: """ + currentTask + "\nPROBLEM:\n" + input;
//
//    default -> """
//        Give a **single, focused hint** related to the problem below.
//        Do NOT include greetings, long explanations, or the solution itself.
//        Focus on what the user should recheck or consider, in **1 sentence only**.
//        Always begin your answer with: Default Explanation
//        CURRENT TASK: """ + currentTask + "\nPROBLEM:\n" + input;
//};
//    }
//
//    public static class HintRequest {
//        private String code;
//        private String type;
//        private Long userId;
//        private Long problemId;
//        private int sessionNumber;
//        private String task;  // Added task to the request
//
//        // getters and setters...
//
//        public String getCode() { return code; }
//        public void setCode(String code) { this.code = code; }
//        public String getType() { return type; }
//        public void setType(String type) { this.type = type; }
//        public Long getUserId() { return userId; }
//        public void setUserId(Long userId) { this.userId = userId; }
//        public Long getProblemId() { return problemId; }
//        public void setProblemId(Long problemId) { this.problemId = problemId; }
//        public int getSessionNumber() { return sessionNumber; }
//        public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
//        public String getTask() { return task; } // Getter for task
//        public void setTask(String task) { this.task = task; } // Setter for task
//    }
//}
package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.Problem;
import com.thesis.java.javalearning.repository.ProblemRepository;
import com.thesis.java.javalearning.service.HintGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hints")
public class HintController {

    @Autowired
    private HintGenerationService hintService;

    @Autowired
    private ProblemRepository problemRepository;

    @PostMapping
    public ResponseEntity<String> getHint(@RequestBody HintRequest payload) {
        if (payload == null) {
            return ResponseEntity.badRequest().body("Invalid request payload.");
        }

        String type = payload.getType() == null ? "" : payload.getType().toLowerCase();
        String promptInput;
        String currentTask = payload.getTask(); // this should contain the task description, not just the problem name

        if (currentTask == null || currentTask.isBlank()) {
            if (payload.getProblemId() != null) {
                Problem p = problemRepository.findById(payload.getProblemId()).orElse(null);
                if (p != null && p.getDescription() != null) {
                    currentTask = p.getDescription();
                } else {
                    return ResponseEntity.badRequest().body("Task description is missing.");
                }
            } else {
                return ResponseEntity.badRequest().body("Task description or problemId is required.");
            }
        }

        // Load code or problem description
        if ("concept".equals(type)) {
            if (payload.getProblemId() == null) {
                return ResponseEntity.badRequest().body("Problem ID required for conceptual hint.");
            }
            Problem problem = problemRepository.findById(payload.getProblemId()).orElse(null);
            if (problem == null) {
                return ResponseEntity.badRequest().body("Problem not found.");
            }
            promptInput = problem.getDescription() != null ? problem.getDescription() : "";
        } else {
            if (payload.getCode() == null || payload.getCode().isBlank()) {
                return ResponseEntity.badRequest().body("Code is required for this hint type.");
            }
            promptInput = payload.getCode();
        }

        // Generate prompt
        String prompt = buildScaffoldPrompt(type, promptInput, currentTask);
        String hint = hintService.generateHint(prompt);
        return ResponseEntity.ok(hint);
    }

    private String buildScaffoldPrompt(String type, String input, String taskDescription) {
   return switch (type) {
    case "syntax" -> """
        You are an AI assistant helping beginner Java students.

        Give a short, clear explanation of any **SYNTAX ISSUE** in the user's Java code (e.g., missing semicolon, incorrect brackets, bad method structure).
        - ❌ Do NOT solve the problem or write any code.
        - ❌ Do NOT include greetings or generic Java syntax lessons.
        - ✅ If syntax is correct, just say: "The current syntax seems correct."
        - ✅ if needed add **one simple next step** that helps them move closer to solving the task.

        Always begin your answer with: Syntax Explanation
        USE BULLET POINT TO CREATE MORE NEAT EXPLANATION, SHORT AND CONCISE
        

        TASK:
        """ + taskDescription + """

        USER CODE:
        """ + input;
        

    case "logic" -> """
        You are an AI assistant helping students debug their Java code.

        Give a focused hint about any **LOGICAL MISTAKE** in the code (e.g., wrong formula, misused variable, incorrect output).
        - ❌ Do NOT write any code or repeat the task description.
        - ✅ If the logic seems okay, say: "The current logic seems correct."
        - ✅ if needed add **one simple suggestion** to verify or improve correctness.

        Always begin your answer with: Logic Explanation
         USE BULLET POINT TO CREATE MORE NEAT EXPLANATION, SHORT AND CONCISE           
        TASK:
        """ + taskDescription + """

        USER CODE:
        """ + input;

    case "step" -> """
        You are a Java tutor providing step-by-step thinking help.

        List 2 to 4 **clear, short logical steps** needed to solve the task.
        - ❌ Do NOT write code.
        - ❌ Do NOT solve the task.
        - ✅ Each step should be one sentence.
        - ✅ Keep the steps focused on reasoning (what to do, not how to write it).

        Always begin your answer with: Step By Step Explanation
          USE BULLET POINT TO CREATE MORE NEAT EXPLANATION, SHORT AND CONCISE          
        TASK:
        """ + taskDescription + """

        USER CODE:
        """ + input;

    case "conceptual" -> """
       You are an educational assistant helping a **beginner-level Java student** understand the **concepts** behind a programming task.
        
        🟢 Your goal is to explain **ONLY the key programming concepts** needed to complete the task. Do **NOT** solve it or mention the final code.
        
        🧠 Write your explanation as if teaching someone **new to programming**, using friendly, beginner-friendly tone and examples they can relate to.
        
        FORMAT & RULES:
        - ✅ Use **bullet points** for each concept (1 line or short paragraph per bullet).
        - ✅ Explain concepts like: what is a variable, what does `int` mean, what is a loop, etc.
        - ✅ Keep your response to **3–5 short bullet points maximum**.
        - ✅ Keep each bullet **short, clean, clear**.
        - ❌ Do NOT include any code.
        - ❌ Do NOT give the solution.
        - ❌ Do NOT assume the student has prior programming knowledge.
        - ✅ Use analogies or simple terms when useful (e.g., "a variable is like a container").
        
        🔁 Your job is to give the student **mental tools**, not copy-paste code.
        
        Always begin your answer with: Concept Explanation
                                     
        TASK:
        """ + taskDescription + """

        PROBLEM:
        """ + input;

    case "reveal" -> """
        Provide the full **correct Java code** that solves the task.

        - ✅ Include short inline comments for explanation.
        - ❌ Do NOT add extra explanations or greetings outside the code block.
        - ✅ The answer should ONLY be a valid Java code block.

        Always begin your answer with: Code Reveal Explanation
                     
        TASK:
        """ + taskDescription + """

        PROBLEM:
        """ + input;

    default -> """
        You are teaching a beginner Java student about **key concepts** behind this task.

        - ✅ Explain only the core idea (e.g., what is `int`, how printing works).
        - ❌ Do NOT give solutions.
        - ❌ Do NOT show any code.
        - ✅ Limit to **max 3 short paragraphs**, beginner-friendly.
        - ✅ Focus on helping them *understand*, not memorize.

        Always begin your answer with: Concept Explanation
         USE BULLET POINT TO CREATE MORE NEAT EXPLANATION, SHORT AND CONCISE       
        TASK:
        """ + taskDescription + """

        PROBLEM:
        """ + input;
};
   
    }
//return switch (type) {
//    case "syntax" -> """
//        Kamu adalah asisten AI efisien yang membantu mahasiswa pemula dalam pemrograman Java.
//
//        Berikan penjelasan singkat dan jelas tentang **KESALAHAN SINTAKSIS** dalam kode Java berikut (misalnya: titik koma hilang, kurung tidak cocok, struktur method salah).
//        - ❌ Jangan selesaikan soal dan jangan tulis kode.
//        - ❌ Jangan beri salam atau penjelasan umum tentang Java.
//        - ✅ Jika sintaks benar, cukup tulis: "Sintaks saat ini tampak benar."
//        - ✅ Jika perlu Berikan **1 saran langkah berikutnya** agar pengguna makin dekat ke solusi.
//
//        Jawabanmu harus diawali dengan: Penjelasan Sintaks
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        KODE PENGGUNA:
//        """ + input;
//
//    case "logic" -> """
//        Kamu adalah asisten AI efisien yang membantu mahasiswa menemukan **KESALAHAN LOGIKA** dalam kode Java mereka.
//
//        Berikan 1–2 kalimat yang fokus pada logika yang salah (misalnya: urutan salah, rumus salah, hasil tidak sesuai).
//        - ❌ Jangan tulis kode atau ulangi instruksi tugas.
//        - ✅ Jika logika sudah benar, tulis: "Logika saat ini tampak benar."
//        - ✅ Jika perlu tambahkan 1 saran untuk memverifikasi atau memperbaiki logika tersebut.
//
//        Jawabanmu harus diawali dengan: Penjelasan Logika
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        KODE PENGGUNA:
//        """ + input;
//
//    case "step" -> """
//        Kamu adalah tutor Java yang memberikan bantuan langkah-demi-langkah.
//
//        Tulis 2 sampai 4 **langkah logis yang jelas dan singkat** untuk menyelesaikan tugas.
//        - ❌ Jangan tulis kode.
//        - ❌ Jangan langsung selesaikan tugas.
//        - ✅ Setiap langkah cukup 1 kalimat.
//        - ✅ Fokus pada "apa yang harus dilakukan".
//
//        Jawabanmu harus diawali dengan: Penjelasan Langkah Demi Langkah
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        KODE PENGGUNA:
//        """ + input;
//
//    case "conceptual" -> """
//        Kamu sedang mengajarkan konsep dasar Java kepada mahasiswa pemula.
//                         
//        - ✅ Jelaskan konsep kunci saja (misalnya: apa itu `int`, bagaimana `System.out.println` bekerja).
//        - ❌ Jangan berikan solusi atau tulis kode jawaban apapun itu.
//        - ✅ Maksimal 2 paragraf pendek, dengan bahasa sederhana, singkat dan jelas .
//        - ✅ Bantu pengguna memahami, bukan menghafal.
//
//        Jawabanmu harus diawali dengan: Penjelasan Konsep
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        MASALAH:
//        """ + input;
//
//    case "reveal" -> """
//        Tampilkan **kode Java lengkap yang benar** untuk menyelesaikan tugas ini.
//
//        - ✅ Tambahkan komentar singkat di dalam kode sebagai penjelasan.
//        - ❌ Jangan beri penjelasan tambahan di luar blok kode.
//        - ✅ Jawaban harus berupa **blok kode Java yang valid saja**.
//
//        Jawabanmu harus diawali dengan: Penjelasan Kode Lengkap
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        MASALAH:
//        """ + input;
//
//    default -> """
//        Kamu sedang mengajarkan konsep dasar Java kepada mahasiswa pemula.
//
//        - ✅ Jelaskan konsep kunci saja (misalnya: apa itu `int`, bagaimana `System.out.println` bekerja).
//        - ❌ Jangan berikan solusi atau tulis kode jawaban apapun.
//        - ✅ Maksimal 3 paragraf pendek, dengan bahasa sederhana.
//        - ✅ Bantu pengguna memahami, bukan menghafal.
//
//        Jawabanmu harus diawali dengan: Penjelasan Konsep
//
//        INSTRUKSI TUGAS:
//        """ + taskDescription + """
//
//        MASALAH:
//        """ + input;
//};
//
//    }

    public static class HintRequest {
        private String code;
        private String type;
        private Long userId;
        private Long problemId;
        private int sessionNumber;
        private String task;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public int getSessionNumber() { return sessionNumber; }
        public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
        public String getTask() { return task; }
        public void setTask(String task) { this.task = task; }
    }
}
