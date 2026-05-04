package com.isw.fraudcheck.security;

import com.isw.fraudcheck.entity.AdminEntity;
import com.isw.fraudcheck.entity.Role;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final AdminEntity admin;

    public CustomUserDetails(AdminEntity admin) {
        this.admin = admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + admin.getRole().name(); // ADMIN -> ROLE_ADMIN
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    // Optional helper methods to still access your UserEntity fields
    public Role getRole() {
        return admin.getRole();
    }

    public String getId() {
        return admin.getId();
    }

    public AdminEntity getUser() {
        return admin;
    }

}
