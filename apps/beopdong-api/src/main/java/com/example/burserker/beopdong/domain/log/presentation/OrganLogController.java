package com.example.burserker.beopdong.domain.log.presentation;

import com.example.burserker.beopdong.domain.log.presentation.dto.request.OrganLogRequest;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.CreateOrganLogResponse;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.OrganLogDetailResponse;
import com.example.burserker.beopdong.domain.log.service.CreateOrganLogService;
import com.example.burserker.beopdong.domain.log.service.CheckOutLogService;
import com.example.burserker.beopdong.domain.log.service.UpdateOrganLogService;
import com.example.burserker.beopdong.domain.log.service.DeleteOrganLogService;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.OrganLogListResponse;
import com.example.burserker.beopdong.domain.log.service.QueryOrganLogDetailService;
import com.example.burserker.beopdong.domain.log.service.QueryOrganLogListService;
import com.example.burserker.beopdong.global.error.ErrorResponse;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class OrganLogController {
    private final QueryOrganLogListService queryOrganLogListService;
    private final QueryOrganLogDetailService queryOrganLogDetailService;
    private final CreateOrganLogService createOrganLogService;
    private final UpdateOrganLogService updateOrganLogService;
    private final DeleteOrganLogService deleteOrganLogService;
    private final CheckOutLogService checkOutLogService;

    @GetMapping("/organ")
    public OrganLogListResponse queryLogs(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "30") int size) {
        return queryOrganLogListService.execute(page, size);
    }

    @GetMapping("/organ/{logId}")
    public OrganLogDetailResponse queryLogDetail(@PathVariable Long logId) {
        return queryOrganLogDetailService.execute(logId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrganLogResponse createLog(@RequestBody @Valid OrganLogRequest request) {
        return createOrganLogService.execute(request);
    }

    @PatchMapping("/{logId}")
    public void updateLog(@PathVariable Long logId, @RequestBody @Valid OrganLogRequest request) {
        updateOrganLogService.execute(logId, request);
    }

    @PatchMapping("/{logId}/check-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkOut(@PathVariable Long logId) {
        checkOutLogService.execute(logId);
    }

    @DeleteMapping("/{logId}")
    public void deleteLog(@PathVariable Long logId) {
        deleteOrganLogService.execute(logId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getErrorMessage()));
    }
}
