package com.example.burserker.beopdong.domain.organ.presentation;

import com.example.burserker.beopdong.domain.organ.presentation.dto.request.ChangePasswordRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.LoginOrganRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.TokenResponse;
import com.example.burserker.beopdong.domain.organ.service.auth.ChangePasswordService;
import com.example.burserker.beopdong.domain.organ.service.auth.LoginOrganService;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.beopdong.domain.purpose.service.CreatePurposeService;
import com.example.burserker.beopdong.domain.purpose.service.UpdatePurposeService;
import com.example.burserker.beopdong.domain.purpose.service.DeletePurposeService;
import com.example.burserker.beopdong.global.security.auth.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organ")
public class OrganController {
    private final LoginOrganService loginOrganService;
    private final ChangePasswordService changePasswordService;
    private final CreatePurposeService createPurposeService;
    private final UpdatePurposeService updatePurposeService;
    private final DeletePurposeService deletePurposeService;

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

    @PatchMapping("/purpose")
    public void createPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeRequest request
    ) {
        createPurposeService.execute(userDetails.organ().getId(), request);
    }

    @PatchMapping("/purpose/{purposeId}")
    public void updatePurpose(
            @PathVariable Long purposeId,
            @RequestBody @Valid PurposeRequest request
    ) {
        updatePurposeService.execute(purposeId, request);
    }

    @DeleteMapping("/purpose/{purposeId}")
    public void deletePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long purposeId
    ) {
        deletePurposeService.execute(userDetails.organ().getId(), purposeId);
    }

}
