package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ViewController {

    private final UserService userService;

    public ViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping({ "/", "/dashboard" })
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            User me = userService.findByUsername(principal.getName());
            model.addAttribute("currentUser", me);        // ← name it "user"
        }
        return "dashboard";
    }

    // —— Admin pages ——

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        User me = userService.findByUsername(principal.getName());
        model.addAttribute("currentUser", me);
        return "admin/dashboard";
    }

    @GetMapping("/admin/user-management")
    public String userManagement(Model model, Principal principal) {
        User me = userService.findByUsername(principal.getName());
        model.addAttribute("currentUser", me);
        return "admin/user-management";
    }

    @GetMapping("/admin/task-management")
    public String taskManagement(Model model, Principal principal) {
        User me = userService.findByUsername(principal.getName());
        model.addAttribute("currentUser", me);
        return "admin/task-management";
    }

    @GetMapping("/admin/task-form")
    public String taskForm(Model model, Principal principal) {
        User me = userService.findByUsername(principal.getName());
        model.addAttribute("currentUser", me);
        return "admin/task-form";
    }
}
