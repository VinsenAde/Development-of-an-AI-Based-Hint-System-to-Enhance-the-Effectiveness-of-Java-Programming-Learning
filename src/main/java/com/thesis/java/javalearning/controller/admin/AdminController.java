package com.thesis.java.javalearning.controller.admin;

import com.thesis.java.javalearning.dto.UserSummaryDto;
import com.thesis.java.javalearning.entity.Submission;
import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.SubmissionRepository;
import com.thesis.java.javalearning.response.ApiResponse;
import com.thesis.java.javalearning.service.admin.AdminService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Apply once for the whole controller
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private SubmissionRepository submissionRepo;

    // ——————————————————————————————————————————————————————
    // 1) List all users
    // ——————————————————————————————————————————————————————
    @GetMapping("/users")
    public List<User> listAllUsers() {
        return adminService.findAllUsers();
    }

    // ——————————————————————————————————————————————————————
    // 2) Group Summary (all users combined)
    // ——————————————————————————————————————————————————————
    @GetMapping("/summary/group")
    public ApiResponse<Map<String, Object>> getGroupSummary() {
        return ApiResponse.ok(adminService.getGroupSummary());
    }

    // ——————————————————————————————————————————————————————
    // 3) All Users' Summary (with optional CSV export)
    // ——————————————————————————————————————————————————————
    @GetMapping("/summary/users")
    public Object getUserSummaries(
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "false") boolean export,
        HttpServletResponse response
    ) throws Exception {
        List<UserSummaryDto> dtos = adminService.getPerUserSummary(search);
        if (export) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"user_summary.csv\"");
            try (PrintWriter pw = response.getWriter()) {
                pw.println("UserID,Username,Submissions,AvgScore,TotalHints,FailedRuns");
                for (var u : dtos) {
                    pw.printf("%d,%s,%d,%.2f,%d,%d%n",
                        u.getUserId(), u.getUsername(),
                        u.getTotalSubmissions(), u.getAvgScore(),
                        u.getTotalHints(), u.getTotalFailedRuns()
                    );
                }
            }
            return null;
        }
        return ApiResponse.ok(dtos);
    }

    // ——————————————————————————————————————————————————————
    // 4) Single User – Summary DTO
    // ——————————————————————————————————————————————————————
// NEW—full 10-field DTO
@GetMapping("/users/{id}/summary")
public ApiResponse<UserSummaryDto> getUserSummary(@PathVariable Long id) {
    return ApiResponse.ok(adminService.getSummaryForUser(id));
}

    // ——————————————————————————————————————————————————————
    // 5) Single User – Full Stats (for chart view)
    // ——————————————————————————————————————————————————————
    @GetMapping("/users/{id}/details")
    public ApiResponse<UserSummaryDto> getUserStatSummary(@PathVariable Long id) {
        return ApiResponse.ok(adminService.getSummaryForUser(id));
    }

    // ——————————————————————————————————————————————————————
    // 6) Promote or Change Role
    // ——————————————————————————————————————————————————————
    @PutMapping("/users/{id}/role")
    public ApiResponse<Void> updateUserRole(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        adminService.updateUserRole(id, body.get("role"));
        return ApiResponse.ok(null, "Role updated");
    }

    // ——————————————————————————————————————————————————————
    // 7) Delete a User
    // ——————————————————————————————————————————————————————
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok(null, "User deleted");
    }

    // ——————————————————————————————————————————————————————
    // 8) Get All Submissions for a User + Session
    // ——————————————————————————————————————————————————————
    @GetMapping("/submissions")
    public List<Submission> getSubmissionsForUser(
        @RequestParam Long userId,
        @RequestParam int sessionNumber
    ) {
        return submissionRepo.findByUserIdAndSessionNumber(userId, sessionNumber);
    }

    // ——————————————————————————————————————————————————————
    // 9) Delete All Submissions for a User + Session
    // ——————————————————————————————————————————————————————
    @DeleteMapping("/submissions")
    @Transactional
    public ResponseEntity<?> deleteSubmissionsForUser(
        @RequestParam Long userId,
        @RequestParam int sessionNumber
    ) {
        submissionRepo.deleteByUser_IdAndSessionNumber(userId, sessionNumber);
        return ResponseEntity.ok().build();
    }
}
