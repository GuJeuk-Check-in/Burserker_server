package com.example.burserker.beopdong.domain.purpose.presentation;

import com.example.burserker.beopdong.domain.purpose.presentation.dto.request.PurposeMoveRequest;
import com.example.burserker.beopdong.domain.purpose.service.MovePurposeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purpose")
public class PurposeController {

    private final MovePurposeService movePurposeService;

    @PatchMapping("/move")
    public void movePurpose(
            @RequestBody @Valid PurposeMoveRequest request
    ) {
        movePurposeService.execute(request);
    }
}