package com.thesis.java.javalearning.service.admin;

import com.thesis.java.javalearning.dto.UserSummaryDto;
import com.thesis.java.javalearning.entity.Submission;
import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.SubmissionRepository;
import com.thesis.java.javalearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired private SubmissionRepository subRepo;
    @Autowired private UserRepository userRepo;

    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    public void updateUserRole(Long id, String roleName) {
        User u = userRepo.findById(id)
                         .orElseThrow(() -> new RuntimeException("User not found"));
        u.setRole(User.Role.valueOf(roleName));
        userRepo.save(u);
    }

    public Map<String,Object> getGroupSummary() {
        List<Submission> subs = subRepo.findAll();
        int count           = subs.size();
        double overallAvg = subs.stream().mapToInt(Submission::getScore).average().orElse(0);
        int hints           = subs.stream().mapToInt(Submission::getHintsUsed).sum();
        int fails           = subs.stream().mapToInt(Submission::getFailedRuns).sum();
        long onSec          = subs.stream().mapToLong(Submission::getOnTaskTime).sum();
        long offSec         = subs.stream().mapToLong(Submission::getOffTaskTime).sum();

        return Map.of(
            "overallAvgScore",  overallAvg,
            "totalSubmissions", count,
            "totalHints",       hints,
            "totalFailedRuns",  fails,
            "avgOnTaskTime",    onSec  / (double) count,
            "avgOffTaskTime",   offSec / (double) count
        );
    }

    public List<UserSummaryDto> getPerUserSummary(String search) {
        List<Submission> subs = subRepo.findAll();
        Map<Long, List<Submission>> byUser = subs.stream()
            .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        return byUser.entrySet().stream()
            .map(e -> {
                Long uid = e.getKey();
                List<Submission> list = e.getValue();
                User userEntity = list.get(0).getUser(); // Get the User entity
                String username = userEntity.getUsername();
                String fullName = userEntity.getFullName(); // <--- Get fullName

                int total   = list.size();
                double avg  = list.stream()
                                     .mapToInt(Submission::getScore)
                                     .average().orElse(0);
                int hints   = list.stream().mapToInt(Submission::getHintsUsed).sum();
                int fails   = list.stream().mapToInt(Submission::getFailedRuns).sum();

                return new UserSummaryDto(uid, username, fullName, total, avg, hints, fails); // <--- Pass fullName
            })
            .filter(dto -> search == null ||
                           dto.getUsername().toLowerCase().contains(search.toLowerCase()) || // Allow search by username
                           (dto.getFullName() != null && dto.getFullName().toLowerCase().contains(search.toLowerCase()))) // <--- Allow search by fullName
            .sorted(Comparator.comparing(UserSummaryDto::getUsername))
            .toList();
    }

    public UserSummaryDto getSingleUserSummary(Long id) {
        return getPerUserSummary(null).stream()
            .filter(u -> u.getUserId().equals(id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No summary for user " + id));
    }

    /**
     * New DTO‐based summary for a single user, including all chart maps.
     */
    public UserSummaryDto getSummaryForUser(Long userId) {
        List<Submission> list = subRepo.findByUserId(userId);
        if (list.isEmpty()) {
            throw new RuntimeException("No submissions for user " + userId);
        }

        User userEntity = list.get(0).getUser(); // Get the User entity
        String username = userEntity.getUsername();
        String fullName = userEntity.getFullName(); // <--- Get fullName

        int total       = list.size();
        double avgScore = list.stream()
                                     .mapToInt(Submission::getScore)
                                     .average().orElse(0);

        int sumHints    = list.stream().mapToInt(Submission::getHintsUsed).sum();
        int sumFails    = list.stream().mapToInt(Submission::getFailedRuns).sum();

        // 1) hint‐cap distribution
        Map<String, Integer> capMap = list.stream()
          .collect(Collectors.groupingBy(
              s -> Optional.ofNullable(s.getHintLevelCap()).orElse("none"),
              Collectors.summingInt(s -> 1)
          ));

        // 2) average score per task
        Map<String, Double> avgScorePerTask = list.stream()
          .collect(Collectors.groupingBy(
              s -> s.getProblem().getTitle(),
              Collectors.averagingDouble(Submission::getScore)
          ));

        // 3) total on‐task time per task
        Map<String, Long> onTaskTimePerTask = list.stream()
          .collect(Collectors.groupingBy(
              s -> s.getProblem().getTitle(),
              Collectors.summingLong(Submission::getOnTaskTime)
          ));

        // 4) total off‐task time per task
        Map<String, Long> offTaskTimePerTask = list.stream()
          .collect(Collectors.groupingBy(
              s -> s.getProblem().getTitle(),
              Collectors.summingLong(Submission::getOffTaskTime)
          ));

        return new UserSummaryDto(
            userId,
            username,
            fullName, // <--- Pass fullName
            total,
            avgScore,
            sumHints,
            sumFails,
            capMap,
            avgScorePerTask,
            onTaskTimePerTask,
            offTaskTimePerTask
        );
    }
}