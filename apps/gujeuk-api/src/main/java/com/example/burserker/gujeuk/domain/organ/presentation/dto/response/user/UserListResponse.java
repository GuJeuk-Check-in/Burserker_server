package com.example.burserker.gujeuk.domain.organ.presentation.dto.response.user;

import com.example.burserker.gujeuk.domain.user.domain.User;

import com.example.burserker.gujeuk.domain.user.domain.enums.Gender;
import lombok.Builder;

@Builder
public record UserListResponse(
        Long id,
        String name,
        Gender gender,
        String phone,
        String birthYMD,
        String residence,
        int count
) {
    public static UserListResponse from(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .gender(user.getGender())
                .phone(user.getPhone())
                .birthYMD(user.getBirthYMD())
                .residence(user.getResidence())
                .count(user.getCount())
                .build();
    }
}
