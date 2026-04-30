package com.isw.fraudcheck.DTOs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse <T> {
    private boolean success;
    private String message;
    private T data;
}
