package com.example.burserker.beopdong.domain.user.presentation.dto.response;

import com.example.burserker.beopdong.domain.user.domain.User;

import java.time.LocalDate;

public record UserListResponse(
        Long id,
        String name,
        int grade,
        String school,
        String phone,
        LocalDate birthYMD,
        int visitCount
) {
    public static UserListResponse from(User user) {
        return new UserListResponse(
                user.getId(),
                user.getName(),
                user.getGrade(),
                user.getSchool(),
                user.getPhone(),
                user.getBirthYMD(),
                user.getVisitCount()
        );
    }
}