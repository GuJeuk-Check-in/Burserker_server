package com.example.burserker.gujeuk.domain.purpose.presentation;

import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeReservationCreateRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.TodayReservationListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.UsagePurposeListResponse;
import com.example.burserker.gujeuk.domain.purpose.service.CreateReservationService;
import com.example.burserker.gujeuk.domain.purpose.service.QueryTodayReservationListService;
import com.example.burserker.gujeuk.domain.purpose.service.QueryUsagePurposeListService;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/reservation")
public class ReservationController {
    private final CreateReservationService createReservationService;
    private final QueryUsagePurposeListService queryUsagePurposeListService;
    private final QueryTodayReservationListService queryTodayReservationListService;

    @GetMapping
    public TodayReservationListResponse queryTodayReservationList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return queryTodayReservationListService.execute(userDetails.organ().getId());
    }

    @GetMapping("/purpose")
    public UsagePurposeListResponse queryUsagePurposeList() {
        return queryUsagePurposeListService.execute();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid PurposeReservationCreateRequest request) {
        createReservationService.execute(request);
    }
}
