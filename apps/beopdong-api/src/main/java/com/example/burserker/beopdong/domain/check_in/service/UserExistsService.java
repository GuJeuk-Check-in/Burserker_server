package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UserExistsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserExistsService {
    private final UserRepository userRepository;

    @Transactional
    public UserExistsResponse execute(String phone) {
        Long userId = userRepository.findByPhone(phone)
                .map(User::getId)
                .orElse(null);

        return new UserExistsResponse(userId);
    }
}
