package com.example.burserker.beopdong.domain.user.service;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.facade.UserFacade;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryUserDetailService {
    private final UserFacade userFacade;

    @Transactional(readOnly = true)
    public UserDetailResponse execute(Long userId) {
        User user = userFacade.getUserById(userId);

        return UserDetailResponse.from(user);
    }
}