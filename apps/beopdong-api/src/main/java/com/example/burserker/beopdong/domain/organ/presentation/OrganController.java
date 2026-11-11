package com.example.burserker.beopdong.domain.organ.presentation;

import com.example.burserker.beopdong.domain.organ.presentation.dto.request.ChangePasswordRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.LoginOrganRequest;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.TokenResponse;
import com.example.burserker.beopdong.domain.organ.service.auth.ChangePasswordService;
import com.example.burserker.beopdong.domain.organ.service.auth.LoginOrganService;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.UsageStatusResponse;
import com.example.burserker.beopdong.domain.organ.service.QueryUsageStatusService;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.CompletedUsagePageResponse;
import com.example.burserker.beopdong.domain.organ.service.QueryCompletedUsageService;
import com.example.burserker.beopdong.domain.organ.presentation.dto.request.UpdateCompletedUsageRequest;
import com.example.burserker.beopdong.domain.organ.service.UpdateCompletedUsageService;
import com.example.burserker.beopdong.global.error.ErrorResponse;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organ")
public class OrganController {
    private final LoginOrganService loginOrganService;
    private final ChangePasswordService changePasswordService;
    private final QueryUsageStatusService queryUsageStatusService;
    private final QueryCompletedUsageService queryCompletedUsageService;
    private final UpdateCompletedUsageService updateCompletedUsageService;

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

    @GetMapping("/purpose")
    public UsageStatusResponse queryUsageStatus() {
        return queryUsageStatusService.execute();
    }

    @GetMapping("/purpose/completed")
    public CompletedUsagePageResponse queryCompletedUsages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) Long purpose
    ) {
        return queryCompletedUsageService.execute(page, size, purpose);
    }

    @PutMapping("/purpose/{id}")
    public void updateCompletedUsage(@PathVariable Long id, @RequestBody @Valid UpdateCompletedUsageRequest request) {
        updateCompletedUsageService.execute(id, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.BAD_REQUEST, "필수 입력 항목을 기재해주세요."));
    }
}
