package com.isw.fraudcheck.ErrorHandling;

public class DuplicateRequestException extends RuntimeException{
    public DuplicateRequestException(String message) {
        super(message);
    }
}
