package com.qudus.docket_backend.exceptions;

import com.qudus.docket_backend.family.FamilyException;
import com.qudus.docket_backend.user.NoUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("authentication_failed", exception.getMessage()));
    }

    @ExceptionHandler(FamilyException.class)
    public  ResponseEntity<ErrorResponse> handleFamilyException(FamilyException exception) {
        return ResponseEntity.status(Objects.requireNonNull(HttpStatus.resolve(exception.getStatusCode()))).body(ErrorResponse.of(exception.getErrorCode(), exception.getMessage()));
    }

    @ExceptionHandler(NoUserException.class)
    public ResponseEntity<ErrorResponse> handleNoUserException(NoUserException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.of("unauthorized", exception.getMessage()));
    }
}
