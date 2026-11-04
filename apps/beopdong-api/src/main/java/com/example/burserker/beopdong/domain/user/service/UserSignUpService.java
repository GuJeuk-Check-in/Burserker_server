package com.example.burserker.beopdong.domain.user.service;

import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.LogPurpose;
import com.example.burserker.beopdong.domain.log.domain.enums.PurposeState;
import com.example.burserker.beopdong.domain.log.domain.repository.LogPurposeRepository;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.exception.RouteDetailRequiredException;
import com.example.burserker.beopdong.domain.user.presentation.dto.request.UserSignUpRequest;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserSignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final PurposeFacade purposeFacade;
    private final LogPurposeRepository logPurposeRepository;
    private final SchoolGradeService schoolGradeService;

    @Transactional
    public UserSignUpResponse execute(UserSignUpRequest request) {
        if(request.route() == Route.OTHER &&
                (request.routeDetail() == null || request.routeDetail().isBlank())) {
            throw RouteDetailRequiredException.EXCEPTION;
        }

        List<Purpose> purposes = purposeFacade.getPurposesByIds(request.purpose());

        int grade = schoolGradeService.validateGrade(
                request.schoolType(), request.grade()
        );

        User user = userRepository.save(
                User.builder()
                        .name(request.name())
                        .gender(request.gender())
                        .phone(request.phone())
                        .birthYMD(request.birthYMD())
                        .grade(grade)
                        .school(request.school())
                        .route(request.route())
                        .routeDetail(request.route() == Route.OTHER ? request.routeDetail() : null)
                        .address(request.address())
                        .privacyAgreed(request.privacyAgreed())
                        .build()
        );

        LocalDateTime visitAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        Log log = Log.create(user, visitAt);
        logRepository.save(log);

        for(Purpose purpose : purposes) {
            logPurposeRepository.save(
                    LogPurpose.builder()
                            .log(log)
                            .purpose(purpose)
                            .purposeState(PurposeState.NONE)
                            .build()
            );
        }

        user.increaseVisitCount();

        return UserSignUpResponse.from(purposes);
    }
}
