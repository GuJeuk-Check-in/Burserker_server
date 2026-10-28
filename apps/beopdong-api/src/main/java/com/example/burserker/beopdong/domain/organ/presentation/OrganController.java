package com.example.burserker.beopdong.domain.organ.presentation;

import com.example.burserker.beopdong.domain.organ.presentation.dto.request.ChangePasswordRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.LoginOrganRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.TokenResponse;
import com.example.burserker.beopdong.domain.organ.service.auth.ChangePasswordService;
import com.example.burserker.beopdong.domain.organ.service.auth.LoginOrganService;
import com.example.burserker.beopdong.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organ")
public class OrganController {
    private final LoginOrganService loginOrganService;
    private final ChangePasswordService changePasswordService;

    @PostMapping
    public TokenResponse login(@RequestBody @Valid LoginOrganRequest request) {
        return loginOrganService.execute(request);
    }

    @PatchMapping
    public void changeOrgan(
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        changePasswordService.execute(request);
    }

}
