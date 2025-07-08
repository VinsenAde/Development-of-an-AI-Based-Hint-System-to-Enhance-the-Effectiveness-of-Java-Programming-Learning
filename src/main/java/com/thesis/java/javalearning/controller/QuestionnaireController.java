package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.Questionnaire;
import com.thesis.java.javalearning.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @PostMapping
    public Questionnaire submitQuestionnaire(@RequestBody Questionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    @GetMapping
    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }
}
