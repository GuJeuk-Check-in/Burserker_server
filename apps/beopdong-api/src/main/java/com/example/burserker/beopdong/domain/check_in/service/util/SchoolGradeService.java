package com.example.burserker.beopdong.domain.check_in.service.util;

import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class SchoolGradeService {

    public int validateGrade(SchoolType schoolType, int grade) {
        int maxGrade = switch(schoolType) {
            case ELEMENTARY -> 6;
            case MIDDLE, HIGH -> 3;
            default -> 0;
        };

        if(maxGrade == 0 || grade < 1 || grade > maxGrade) {
            throw new BeopdongException(ErrorCode.SIGN_UP_GRADE_INVALID);
        }

        return grade;
    }
}