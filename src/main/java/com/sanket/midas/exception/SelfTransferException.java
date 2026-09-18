package com.sanket.midas.exception;

public class SelfTransferException extends RuntimeException {

    public SelfTransferException(String message) {
        super(message);
    }
}