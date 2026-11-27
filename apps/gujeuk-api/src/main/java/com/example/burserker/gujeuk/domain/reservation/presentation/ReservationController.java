package com.example.burserker.gujeuk.domain.reservation.presentation;


import com.example.burserker.gujeuk.domain.reservation.presentation.dto.request.ReservationCreateRequest;
import com.example.burserker.gujeuk.domain.reservation.presentation.dto.response.ReservationStatusResponse;
import com.example.burserker.gujeuk.domain.reservation.service.CreateOrganReservationService;
import com.example.burserker.gujeuk.domain.reservation.service.DeleteReservationService;
import com.example.burserker.gujeuk.domain.reservation.service.QueryReservationStatusService;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/reservation")
public class ReservationController {
    private final QueryReservationStatusService queryReservationStatusService;
    private final DeleteReservationService deleteReservationService;
    private final CreateOrganReservationService createOrganReservationService;

    // 토큰 없이 조회하면 구즉 예약 현황판으로 보고 전화번호를 숨긴다. 토큰이 있으면 해당 기관 현황을 모두 보여준다.
    @GetMapping
    public ReservationStatusResponse queryReservationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return queryReservationStatusService.executeForPublicBoard();
        }
        return queryReservationStatusService.execute(userDetails.organ().getId());
    }

    @DeleteMapping("/{reservation-id}")
    public void deleteReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("reservation-id") Long reservationId
    ) {
        deleteReservationService.execute(userDetails.organ().getId(), reservationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReservationCreateRequest request
    ){
        createOrganReservationService.execute(userDetails.organ().getId(), request);
    }
}