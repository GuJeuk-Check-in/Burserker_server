package com.example.burserker.beopdong.domain.checkin.presentation;

import com.example.burserker.beopdong.domain.purpose.presentation.dto.response.PurposeListResponse;
import com.example.burserker.beopdong.domain.purpose.service.QueryPurposeListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/check-in")
public class CheckInController {

    private final QueryPurposeListService queryPurposeListService;

    @GetMapping("/purpose")
    public PurposeListResponse queryAllPurpose() {
        return new PurposeListResponse(queryPurposeListService.execute());
    }
}