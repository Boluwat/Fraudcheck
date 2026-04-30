package com.isw.fraudcheck.DTOs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class loginResponse {
    private String token;
    private AdminResponseDTO admin;
}
