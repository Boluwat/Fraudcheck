package com.isw.fraudcheck.ErrorHandling;

public class ForbiddenRequestException extends RuntimeException{
    public ForbiddenRequestException(String message) {
        super(message);
    }
}
