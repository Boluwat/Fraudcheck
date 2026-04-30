package com.isw.fraudcheck.service;


import com.isw.fraudcheck.entity.AdminEntity;
import com.isw.fraudcheck.repository.AdminRepository;
import io.jsonwebtoken.lang.Collections;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;


    public CustomUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials provided"));;

        return new org.springframework.security.core.userdetails.User(
                admin.getUsername(),
                admin.getPassword(),
                List.of(new SimpleGrantedAuthority("ADMIN"))
        );
    }
}
