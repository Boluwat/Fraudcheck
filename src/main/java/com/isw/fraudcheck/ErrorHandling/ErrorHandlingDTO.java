package com.isw.fraudcheck.ErrorHandling;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorHandlingDTO {
    private String message;
    private int status;
    private String timestamp;


    public ErrorHandlingDTO(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
    }
}
