package com.example.burserker.gujeuk.domain.user.presentation.dto.response;

import lombok.Builder;

@Builder
public record UserExistsResponse(
        boolean userExists,
        Long userId,
        boolean duplicateName
) {
    public static UserExistsResponse of(boolean userExists, Long userId) {
        return UserExistsResponse.builder()
                .userExists(userExists)
                .userId(userId)
                .duplicateName(false)
                .build();
    }

    public static UserExistsResponse of(boolean userExists, Long userId, boolean duplicateName) {
        return UserExistsResponse.builder()
                .userExists(userExists)
                .userId(userId)
                .duplicateName(duplicateName)
                .build();
    }
}
