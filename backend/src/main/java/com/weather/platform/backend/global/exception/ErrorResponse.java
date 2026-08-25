package com.weather.platform.backend.global.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        OffsetDateTime timestamp,
        List<FieldError> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, OffsetDateTime.now(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.name(), message, OffsetDateTime.now(), fieldErrors);
    }

    public record FieldError(String field, String reason) {
    }
}
