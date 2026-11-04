package com.example.burserker.beopdong.domain.checkin.presentation;

import com.example.burserker.beopdong.domain.checkin.presentation.dto.request.UserExistsRequest;
import com.example.burserker.beopdong.domain.user.presentation.dto.request.UserSignUpRequest;
import com.example.burserker.beopdong.domain.checkin.presentation.dto.response.UserExistsResponse;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserSignUpResponse;
import com.example.burserker.beopdong.domain.checkin.service.UserExistsService;
import com.example.burserker.beopdong.domain.user.service.UserSignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/check-in")
public class CheckInController {
    private final UserExistsService userExistsService;
    private final UserSignUpService userSignUpService;

    @GetMapping(params = "phone")
    public UserExistsResponse userExists(@ModelAttribute @Valid UserExistsRequest request) {
        return userExistsService.execute(request.phone());
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSignUpResponse signUp(@RequestBody @Valid UserSignUpRequest request) {
        return userSignUpService.execute(request);
    }
}