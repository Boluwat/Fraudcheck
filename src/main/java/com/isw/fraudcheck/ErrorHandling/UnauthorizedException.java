package com.isw.fraudcheck.ErrorHandling;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
