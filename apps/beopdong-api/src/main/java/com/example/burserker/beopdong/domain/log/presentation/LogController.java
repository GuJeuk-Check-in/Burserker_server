package com.example.burserker.beopdong.domain.log.presentation;

import com.example.burserker.beopdong.domain.log.presentation.dto.response.LogDetailResponse;
import com.example.burserker.beopdong.domain.log.presentation.dto.response.LogListResponse;
import com.example.burserker.beopdong.domain.log.service.QueryLogDetailService;
import com.example.burserker.beopdong.domain.log.service.QueryLogListService;
import com.example.burserker.beopdong.global.error.ErrorResponse;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {
    private final QueryLogListService queryLogListService;
    private final QueryLogDetailService queryLogDetailService;

    @GetMapping
    public LogListResponse queryLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return queryLogListService.execute(page, size);
    }

    @GetMapping("/{logId}")
    public LogDetailResponse queryLogDetail(@PathVariable Long logId) {
        return queryLogDetailService.execute(logId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException e) {
        String description = "logId".equals(e.getName()) ? "방명록 번호가 올바르지 않습니다." : "page 또는 size 값이 올바르지 않음";
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.BAD_REQUEST, description));
    }
}
