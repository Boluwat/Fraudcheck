package com.isw.fraudcheck.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="Admin")
public class AdminEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique=true)
    private String email;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name="created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;




}
