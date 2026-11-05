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

    @GetMapping
    public ReservationStatusResponse queryReservationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
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