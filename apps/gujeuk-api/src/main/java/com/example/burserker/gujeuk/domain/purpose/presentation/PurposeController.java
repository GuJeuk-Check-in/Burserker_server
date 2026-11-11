package com.example.burserker.gujeuk.domain.purpose.presentation;

import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.PurposeResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import com.example.burserker.gujeuk.domain.purpose.service.*;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purpose")
public class PurposeController {
    private final CreatePurposeService createPurposeService;
    private final UpdatePurposeService updatePurposeService;
    private final DeletePurposeService deletePurposeService;
    private final QueryPurposeListService queryPurposeListService;
    private final MovePurposeService movePurposeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeRequest request
    ) {
        createPurposeService.execute(userDetails.organ().getId(), request);
    }

    @PatchMapping("/{id}")
    public void updatePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid PurposeRequest purposeRequest
    ) {
        updatePurposeService.execute(userDetails.organ().getId(), id, purposeRequest);
    }

    @DeleteMapping("/{id}")
    public void deletePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        deletePurposeService.execute(userDetails.organ().getId(), id);
    }

    @GetMapping("/all")
    public List<PurposeResponse> queryAllPurpose() {
        return queryPurposeListService.execute();
    }

    @PatchMapping("/move")
    public void movePurpose(@RequestBody @Valid PurposeMoveRequest PurposeMoveRequest) {
        movePurposeService.execute(PurposeMoveRequest);
    }
}
