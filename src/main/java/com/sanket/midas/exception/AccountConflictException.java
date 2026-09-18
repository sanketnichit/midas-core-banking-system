package com.sanket.midas.exception;

public class AccountConflictException extends RuntimeException {

    public AccountConflictException(String message) {
        super(message);
    }
}