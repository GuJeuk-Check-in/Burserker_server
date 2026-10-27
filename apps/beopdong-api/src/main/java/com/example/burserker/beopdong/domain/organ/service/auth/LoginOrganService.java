package com.example.burserker.beopdong.domain.organ.service.auth;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.domain.repository.OrganRepository;
import com.example.burserker.beopdong.domain.organ.exception.PasswordMismatchException;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.LoginOrganRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.TokenResponse;
import com.example.burserker.beopdong.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import com.example.burserker.beopdong.domain.organ.exception.OrganNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginOrganService {
    private final OrganRepository organRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse execute(LoginOrganRequest request) {
        Organ organ = organRepository.findByOrganName(request.organName())
                .orElseThrow(() -> OrganNotFoundException.EXCEPTION);

        if (!passwordEncoder.matches(request.password(), organ.getPassword())) {
            throw PasswordMismatchException.EXCEPTION;
        }

        return jwtTokenProvider.receiveToken(organ.getOrganName());
    }
}
