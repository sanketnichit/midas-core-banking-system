package com.sanket.midas.exception;

import java.util.Map;

public class ValidationErrorResponse {

    private int status;
    private String message;
    private Map<String, String> errors;

    public ValidationErrorResponse(
            int status,
            String message,
            Map<String, String> errors) {

        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}