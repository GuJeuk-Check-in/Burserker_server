package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.presentation.dto.request.UserSignUpRequest;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.UserSignUpResponse;
import com.example.burserker.beopdong.domain.check_in.service.util.SchoolGradeService;
import com.example.burserker.beopdong.domain.log.domain.Log;
import com.example.burserker.beopdong.domain.log.domain.VisitPurpose;
import com.example.burserker.beopdong.domain.log.domain.repository.VisitPurposeRepository;
import com.example.burserker.beopdong.domain.purpose.domain.repository.PurposeRepository;
import com.example.burserker.beopdong.domain.purpose.exception.PurposeNotFoundException;
import com.example.burserker.beopdong.domain.log.domain.repository.LogRepository;
import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import com.example.burserker.beopdong.domain.purpose.facade.PurposeFacade;
import com.example.burserker.beopdong.domain.user.domain.User;
import com.example.burserker.beopdong.domain.user.domain.enums.Route;
import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.exception.RouteDetailRequiredException;
import com.example.burserker.beopdong.domain.user.exception.UserPhoneAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final PurposeFacade purposeFacade;
    private final VisitPurposeRepository visitPurposeRepository;
    private final PurposeRepository purposeRepository;
    private final SchoolGradeService schoolGradeService;

    @Transactional
    public UserSignUpResponse execute(UserSignUpRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw UserPhoneAlreadyExistsException.EXCEPTION;
        }

        if(request.route() == Route.OTHER &&
                (request.routeDetail() == null || request.routeDetail().isBlank())) {
            throw RouteDetailRequiredException.EXCEPTION;
        }

        List<Purpose> purposes = new ArrayList<>(purposeFacade.getPurposesByIds(request.purpose()));
        Purpose rest = purposeRepository.findFirstByPurposeNameOrderByIdAsc("휴식")
                .orElseThrow(() -> PurposeNotFoundException.EXCEPTION);
        if (purposes.stream().noneMatch(purpose -> purpose.getId().equals(rest.getId()))) {
            purposes.add(rest);
        }

        int grade = schoolGradeService.validateGrade(
                request.schoolType(), request.grade()
        );

        User user = User.create(
                request.name(),
                request.gender(),
                request.phone(),
                request.birthYMD(),
                grade,
                request.school(),
                request.route(),
                request.routeDetail(),
                request.address(),
                request.privacyAgreed()
        );
        userRepository.save(user);

        LocalDateTime visitAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        Log log = Log.create(user, visitAt);
        logRepository.save(log);

        for(Purpose purpose : purposes) {
            visitPurposeRepository.save(VisitPurpose.create(log, purpose));
        }

        user.increaseVisitCount();

        return UserSignUpResponse.from(log.getId(), purposes);
    }
}
