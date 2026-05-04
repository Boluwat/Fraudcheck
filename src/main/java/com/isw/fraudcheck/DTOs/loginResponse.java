package com.isw.fraudcheck.DTOs;


import com.isw.fraudcheck.logger.Sensitive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class loginResponse {
    @Sensitive
    private String token;


    private AdminResponseDTO admin;
}
