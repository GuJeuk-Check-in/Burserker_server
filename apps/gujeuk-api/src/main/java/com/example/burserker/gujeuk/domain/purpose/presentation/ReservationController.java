package com.example.burserker.gujeuk.domain.purpose.presentation;

import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeReservationCreateRequest;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.AvailableReservationTimeListResponse;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.UsagePurposeListResponse;
import com.example.burserker.gujeuk.domain.purpose.service.CreateReservationService;
import com.example.burserker.gujeuk.domain.purpose.service.QueryAvailableReservationTimeListService;
import com.example.burserker.gujeuk.domain.purpose.service.QueryUsagePurposeListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/reservation")
public class ReservationController {
    private final CreateReservationService createReservationService;
    private final QueryUsagePurposeListService queryUsagePurposeListService;
    private final QueryAvailableReservationTimeListService queryAvailableReservationTimeListService;

    @GetMapping
    public AvailableReservationTimeListResponse queryAvailableReservationTimeList(@RequestParam("purpose") String purpose) {
        return queryAvailableReservationTimeListService.execute(purpose);
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
