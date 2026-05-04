package com.isw.fraudcheck.controller;


import com.isw.fraudcheck.DTOs.AdminRequestDTO;
import com.isw.fraudcheck.DTOs.AdminResponseDTO;
import com.isw.fraudcheck.DTOs.ApiResponse;
import com.isw.fraudcheck.DTOs.loginResponse;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.service.AdminService;
import com.isw.fraudcheck.service.FraudEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {


    private final AdminService adminService;

    public AuthenticationController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("")
    public ApiResponse<AdminResponseDTO> createAdmin(@RequestBody AdminRequestDTO adminRequestDTO) {
        return adminService.createAdmin(adminRequestDTO);
    }

    @PostMapping("/login")
    public ApiResponse<loginResponse> login(@RequestBody AdminRequestDTO adminRequestDTO) {
        return adminService.adminLogin(adminRequestDTO);
    }

}
