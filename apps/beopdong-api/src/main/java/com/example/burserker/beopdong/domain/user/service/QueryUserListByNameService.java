package com.example.burserker.beopdong.domain.user.service;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.exception.UserNameNotFoundException;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserListResponse;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryUserListByNameService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSearchResponse execute(String name) {
        List<User> users = userRepository.findAllByNameContainingOrderByIdAsc(name);

        if(users.isEmpty()) {
            throw UserNameNotFoundException.EXCEPTION;
        }

        List<UserListResponse> content = users.stream()
                .map(UserListResponse::from)
                .toList();

        return new UserSearchResponse(content.size(), content);
    }
}