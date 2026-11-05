package com.example.burserker.beopdong.domain.user.service;

import com.example.burserker.beopdong.domain.check_in.service.util.SchoolGradeService;
import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.exception.RouteDetailRequiredException;
import com.example.burserker.beopdong.domain.user.facade.UserFacade;
import com.example.burserker.beopdong.domain.user.presentation.dto.request.UpdateUserRequest;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.exception.UserPhoneAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserService {
    private final UserFacade userFacade;
    private final SchoolGradeService schoolGradeService;
    private final UserRepository userRepository;

    @Transactional
    public void execute(Long userId, UpdateUserRequest request) {
        if(request.route() == Route.OTHER &&
                (request.routeDetail() == null || request.routeDetail().isBlank())) {
            throw RouteDetailRequiredException.EXCEPTION;
        }

        User user = userFacade.getUserById(userId);

        if (userRepository.existsByPhoneAndIdNot(request.phone(), userId)) {
            throw UserPhoneAlreadyExistsException.EXCEPTION;
        }

        int grade = schoolGradeService.validateGrade(
                request.schoolType(), request.grade()
        );

        user.updateUser(
                request.name(),
                grade,
                request.school(),
                request.route(),
                request.routeDetail(),
                request.address(),
                request.gender(),
                request.phone(),
                request.birthYMD(),
                request.applyDate()
        );
    }
}