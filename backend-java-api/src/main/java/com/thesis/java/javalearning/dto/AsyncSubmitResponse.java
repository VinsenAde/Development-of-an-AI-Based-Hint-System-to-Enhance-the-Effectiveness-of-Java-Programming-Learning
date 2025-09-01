package com.thesis.java.javalearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsyncSubmitResponse {
    private UUID jobId;
    private String status;
    private String message;
}
