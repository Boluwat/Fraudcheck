package com.isw.fraudcheck.repository;

import com.isw.fraudcheck.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminEntity, String> {
    Optional<AdminEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}

