package com.isw.fraudcheck.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class AdminRequestDTO {
    private String username;
    private String password;
    private String email;
//    private String role;

}
