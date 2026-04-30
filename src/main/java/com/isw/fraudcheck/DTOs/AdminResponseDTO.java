package com.isw.fraudcheck.DTOs;


import com.isw.fraudcheck.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminResponseDTO {
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private String id;
    private Role role;
}
