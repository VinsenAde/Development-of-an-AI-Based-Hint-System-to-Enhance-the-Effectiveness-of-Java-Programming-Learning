package com.thesis.java.javalearning.controller;
import com.thesis.java.javalearning.dto.ExecutionRequest;
import com.thesis.java.javalearning.dto.ExecutionResponse;
import com.thesis.java.javalearning.service.CodeExecutionService;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/execution")
public class ExecutionController {

    private final CodeExecutionService service;

    public ExecutionController(CodeExecutionService service) {
        this.service = service;
    }

    @PostMapping
    public ExecutionResponse execute(
            @RequestBody ExecutionRequest request) {

        return service.execute(request);
    }
}