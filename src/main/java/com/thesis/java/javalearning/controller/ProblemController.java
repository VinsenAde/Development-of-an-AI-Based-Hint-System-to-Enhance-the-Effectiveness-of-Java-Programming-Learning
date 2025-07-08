//package com.thesis.java.javalearning.controller;
//
//import com.thesis.java.javalearning.entity.Problem;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/problems")
//public class ProblemController {
//
//    // For demonstration, store in memory
//    private List<Problem> mockProblems = new ArrayList<>();
//
//    @GetMapping
//    public List<Problem> getAllProblems() {
//        // TODO: fetch from DB if using ProblemRepository
//        return mockProblems;
//    }
//
//    @PostMapping
//    public Problem createProblem(@RequestBody Problem problem) {
//        // Mock ID assignment
//        problem.setId((long) (mockProblems.size() + 1));
//        mockProblems.add(problem);
//        return problem;
//    }
//
//    @GetMapping("/{id}")
//    public Problem getProblem(@PathVariable Long id) {
//        return mockProblems.stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst()
//                .orElse(null);
//    }
//}
package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.Problem;
import com.thesis.java.javalearning.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
// …

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemRepository problemRepository;

    @GetMapping
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(@PathVariable Long id) {
        return problemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Problem createProblem(@RequestBody Problem problem) {
        return problemRepository.save(problem);
    }
}
