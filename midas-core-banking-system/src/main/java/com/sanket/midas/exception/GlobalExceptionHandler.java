package com.sanket.midas.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handles requests for accounts that do not exist
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleAccountNotFound(
            AccountNotFoundException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    // Handles insufficient balance
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ValidationErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handles attempts to transfer money to the same account
    @ExceptionHandler(SelfTransferException.class)
    public ResponseEntity<ValidationErrorResponse> handleSelfTransfer(
            SelfTransferException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handles account conflicts
    @ExceptionHandler(AccountConflictException.class)
    public ResponseEntity<ValidationErrorResponse> handleAccountConflict(
            AccountConflictException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    // Handles optimistic locking failures
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "The account was modified by another request. Please retry.",
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    // Handles database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "The requested operation violates a database constraint.",
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}