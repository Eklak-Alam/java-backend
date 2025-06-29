package com.lic.exception;

public class DuplicatePanException extends RuntimeException {
    public DuplicatePanException(String message) {
        super(message);
    }
}