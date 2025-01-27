package com.user.exception;

import lombok.*;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class ErrorResponse {
    private String message;
    private int status;
    private Instant timestamp;
}