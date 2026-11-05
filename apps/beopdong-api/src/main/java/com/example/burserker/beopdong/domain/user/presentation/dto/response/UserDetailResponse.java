package com.example.burserker.beopdong.domain.user.presentation.dto.response;

import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Gender;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;

import java.time.LocalDate;

public record UserDetailResponse(
        String name,
        int grade,
        String school,
        Route route,
        String routeDetail,
        String address,
        Gender gender,
        String phone,
        LocalDate birthYMD,
        LocalDate applyDate
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getName(),
                user.getGrade(),
                user.getSchool(),
                user.getRoute(),
                user.getRouteDetail(),
                user.getAddress(),
                user.getGender(),
                user.getPhone(),
                user.getBirthYMD(),
                user.getApplyDate()
        );
    }
}