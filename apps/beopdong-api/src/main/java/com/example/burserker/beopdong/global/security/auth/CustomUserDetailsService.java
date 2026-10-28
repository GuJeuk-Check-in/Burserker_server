package com.example.burserker.beopdong.global.security.auth;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.domain.repository.OrganRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final OrganRepository organRepository;

    @Override
    public UserDetails loadUserByUsername(String organName) {
        Organ organ = organRepository.findByOrganName(organName)
                .orElseThrow(() -> new UsernameNotFoundException("Organ Not Found"));

        return new CustomUserDetails(organ);
    }
}
