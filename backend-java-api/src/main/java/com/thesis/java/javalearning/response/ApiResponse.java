package com.thesis.java.javalearning.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standard API response wrapper, now generic.
 */
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data    = data;
        this.message = message;
    }

    /** Success without data */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /** Success with data */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Success with data and message */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /** Error with message */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
}
