package com.example.burserker.beopdong.domain.check_in.presentation;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.PcFormRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.UserExistsRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.UserSignUpRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.SchoolSearchResponse;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UserExistsResponse;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UserSignUpResponse;
import com.example.burserker.beopdong.domain.check_in.service.*;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.SpringFormRequest;
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
    private final SearchSchoolService searchSchoolService;
    private final CreateSpringFormService createSpringFormService;
    private final CreatePcFormService createPcFormService;

    @GetMapping(params = "phone")
    public UserExistsResponse userExists(@ModelAttribute @Valid UserExistsRequest request) {
        return userExistsService.execute(request.phone());
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSignUpResponse signUp(@RequestBody @Valid UserSignUpRequest request) {
        return userSignUpService.execute(request);
    }

    @GetMapping("/school")
    public SchoolSearchResponse searchSchool(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") int page
    ) {
        return searchSchoolService.execute(name, page);
    }

    @PostMapping("/sping")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createSpringForm(
            @RequestBody @Valid SpringFormRequest request
    ) {
        createSpringFormService.execute(request);
    }

    @PostMapping("/pc")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createPcForm(
            @RequestBody @Valid PcFormRequest request
    ) {
        createPcFormService.execute(request);
    }
}