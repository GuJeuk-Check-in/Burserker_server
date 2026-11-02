package com.example.burserker.gujeuk.domain.purpose.presentation;


import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.ReservationStatusResponse;
import com.example.burserker.gujeuk.domain.purpose.service.QueryReservationStatusService;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/reservation")
public class ReservationStatusController {
    private final QueryReservationStatusService queryReservationStatusService;

    @GetMapping
    public ReservationStatusResponse queryReservationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return queryReservationStatusService.execute(userDetails.organ().getId());
    }
}