package com.example.burserker.gujeuk.domain.purpose.presentation;

import com.example.burserker.gujeuk.domain.purpose.presentation.dto.request.PurposeReservationCreateRequest;
import com.example.burserker.gujeuk.domain.purpose.service.CreateReservationService;
import com.example.burserker.gujeuk.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PurposeReservationCreateRequest request
    ) {
        createReservationService.execute(userDetails.organ().getId(), request);
    }
}
