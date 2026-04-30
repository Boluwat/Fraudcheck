package com.isw.fraudcheck.service;

import com.isw.fraudcheck.DTOs.AdminRequestDTO;
import com.isw.fraudcheck.DTOs.AdminResponseDTO;
import com.isw.fraudcheck.DTOs.ApiResponse;
import com.isw.fraudcheck.DTOs.loginResponse;
import com.isw.fraudcheck.ErrorHandling.BadRequestException;
import com.isw.fraudcheck.ErrorHandling.DuplicateRequestException;
import com.isw.fraudcheck.ErrorHandling.UnauthorizedException;
import com.isw.fraudcheck.entity.AdminEntity;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.Role;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.repository.AdminRepository;
import com.isw.fraudcheck.repository.BlackListedMerchantRepository;
import com.isw.fraudcheck.repository.TransactionJdbcQueryRepository;
import com.isw.fraudcheck.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final BlackListedMerchantRepository blackListedMerchantRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TransactionJdbcQueryRepository queryRepository;


    public AdminService(AdminRepository adminRepository, BlackListedMerchantRepository blackListedMerchantRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TransactionJdbcQueryRepository queryRepository) {
        this.adminRepository = adminRepository;
        this.blackListedMerchantRepository = blackListedMerchantRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.queryRepository = queryRepository;
    }

    public ApiResponse<loginResponse> adminLogin(AdminRequestDTO request) {
        AdminEntity admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        Authentication authenticateUser;
        try {
            authenticateUser = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("Invalid credentials");
        }

        UserDetails userDetails = (UserDetails) authenticateUser.getPrincipal();
        String token = jwtUtil.generateToken(userDetails != null ? userDetails.getUsername() : null);

        return  ApiResponse.<loginResponse>builder()
                .success(true)
                .message("User login Successfully")
                .data(
                        loginResponse.builder()
                                .token(token)
                                .admin(toResponseData(admin))
                                .build()
                )
                .build();
    }

    public String createAdmin(AdminRequestDTO adminRequestDTO) {
        if (adminRepository.existsByUsername(adminRequestDTO.getUsername())) {
            throw new DuplicateRequestException("Already exist in the system");
        }
        AdminEntity newAdmin = new AdminEntity();
        newAdmin.setEmail(adminRequestDTO.getEmail());
        newAdmin.setUsername(adminRequestDTO.getUsername());
        newAdmin.setPassword(passwordEncoder.encode(adminRequestDTO.getPassword()));
        newAdmin.setRole(Role.ADMIN);
        adminRepository.save(newAdmin);
        return "Admin created Successfully";
    }

    public List<BlackListedMerchant> getBlackListedMerchants() {
        return blackListedMerchantRepository.findAll();
    }

    public List<TransactionsEntity> getTransactions() {
        return queryRepository.findByStatus();
    }


    private AdminResponseDTO toResponseData(AdminEntity admin) {
        return AdminResponseDTO.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .username(admin.getUsername())
                .role(Role.ADMIN)
                .createdAt(admin.getCreatedAt())
                .build();
    }






}
