package com.example.burserker.beopdong.domain.purpose.presentation;

import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.beopdong.domain.purpose.service.CreatePurposeService;
import com.example.burserker.beopdong.domain.purpose.service.DeletePurposeService;
import com.example.burserker.beopdong.domain.purpose.service.MovePurposeService;
import com.example.burserker.beopdong.domain.purpose.service.UpdatePurposeService;
import com.example.burserker.beopdong.global.security.auth.CustomUserDetails;
import com.example.burserker.beopdong.domain.purpose.presentation.dto.response.PurposeListResponse;
import com.example.burserker.beopdong.domain.purpose.service.QueryPurposeListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purpose")
public class PurposeController {
    private final CreatePurposeService createPurposeService;
    private final UpdatePurposeService updatePurposeService;
    private final DeletePurposeService deletePurposeService;
    private final MovePurposeService movePurposeService;

    @PatchMapping
    public void createPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeRequest request
    ) {
        createPurposeService.execute(userDetails.organ().getId(), request);
    }

    @PatchMapping("/{purposeId}")
    public void updatePurpose(
            @PathVariable Long purposeId,
            @RequestBody @Valid PurposeRequest request
    ) {
        updatePurposeService.execute(purposeId, request);
    }

    @DeleteMapping("/{purposeId}")
    public void deletePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long purposeId
    ) {
        deletePurposeService.execute(userDetails.organ().getId(), purposeId);
    }

    private final QueryPurposeListService queryPurposeListService;

    @GetMapping
    public PurposeListResponse queryAllPurpose() {
        return new PurposeListResponse(queryPurposeListService.execute());
    }

    @PatchMapping("/move")
    public void movePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeMoveRequest request
    ) {
        movePurposeService.execute(userDetails.organ().getId(), request);
    }
}