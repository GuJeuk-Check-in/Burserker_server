package com.example.burserker.gujeuk.domain.purpose.presentation;

import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.SubPurposeRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.SubPurposeUpdateRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.PurposeWithSubListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import com.example.burserker.gujeuk.domain.purpose.service.*;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final CreateSubPurposeService createSubPurposeService;
    private final DeleteSubPurposeService deleteSubPurposeService;
    private final UpdateSubPurposeService updateSubPurposeService;
    private final QueryPurposeWithSubListService queryPurposeWithSubListService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeRequest request
    ) {
        createPurposeService.execute(userDetails.organ().getId(), request);
    }

    @PostMapping("/sub")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid SubPurposeRequest request
    ) {
        createSubPurposeService.execute(userDetails.organ().getId(), request);
    }

    @PatchMapping("/{id}")
    public void updatePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid PurposeRequest purposeRequest
    ) {
        updatePurposeService.execute(userDetails.organ().getId(), id, purposeRequest);
    }

    @PatchMapping("/sub/{id}")
    public void updateSubPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid SubPurposeUpdateRequest request
    ) {
        updateSubPurposeService.execute(userDetails.organ().getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        deletePurposeService.execute(userDetails.organ().getId(), id);
    }

    @DeleteMapping("/sub/{id}")
    public void deleteSubPurpose(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        deleteSubPurposeService.execute(userDetails.organ().getId(), id);
    }

    @GetMapping("/all/with-sub")
    public PurposeWithSubListResponse queryAllPurposeWithSub() {
        return queryPurposeWithSubListService.execute();
    }

    @PatchMapping("/move")
    public void movePurpose(@RequestBody @Valid PurposeMoveRequest PurposeMoveRequest) {
        movePurposeService.execute(PurposeMoveRequest);
    }
}
