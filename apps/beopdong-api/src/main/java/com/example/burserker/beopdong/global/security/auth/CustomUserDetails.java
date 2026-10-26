package com.example.burserker.beopdong.global.security.auth;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public record CustomUserDetails(Organ organ) implements UserDetails {

    @Override
    public String getUsername() {
        return organ.getOrganName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(Collections.singleton(new SimpleGrantedAuthority("ROLE_ORGAN")));
    }

    @Override
    public String getPassword() {
        return organ.getPassword();
    }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
