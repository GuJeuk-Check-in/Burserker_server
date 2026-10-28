package com.example.burserker.beopdong.domain.organ.service.auth;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.exception.InvalidPasswordConfirmException;
import com.example.burserker.beopdong.domain.organ.exception.SameOldPasswordException;
import com.example.burserker.beopdong.domain.organ.facade.OrganFacade;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.ChangePasswordRequest;
import com.example.burserker.beopdong.domain.organ.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final OrganFacade organFacade;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(ChangePasswordRequest request) {
        Long organId = 1L;
        Organ organ = organFacade.getOrganById(organId);

        if (!passwordEncoder.matches(request.oldPassword(), organ.getPassword())) {
            throw PasswordMismatchException.EXCEPTION;
        }

        if (passwordEncoder.matches(request.newPassword(), organ.getPassword())) {
            throw SameOldPasswordException.EXCEPTION;
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw InvalidPasswordConfirmException.EXCEPTION;
        }

        organ.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
