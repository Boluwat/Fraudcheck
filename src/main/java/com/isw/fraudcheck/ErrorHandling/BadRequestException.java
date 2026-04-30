package com.isw.fraudcheck.ErrorHandling;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
