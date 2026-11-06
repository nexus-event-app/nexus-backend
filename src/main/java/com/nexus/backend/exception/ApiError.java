package com.nexus.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
    private String path;

    public static ApiError of(HttpStatus status, String error, String message, String path) {
        return new ApiError(status.value(), error, message, null, LocalDateTime.now().withNano(0), path);
    }

    public static ApiError validation(String message, Map<String, String> fieldErrors, String path) {
        return new ApiError(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", message, fieldErrors,
                LocalDateTime.now().withNano(0), path);
    }
}
