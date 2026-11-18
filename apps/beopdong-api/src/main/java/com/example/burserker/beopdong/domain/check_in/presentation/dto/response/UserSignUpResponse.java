package com.example.burserker.beopdong.domain.check_in.presentation.dto.response;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;

import java.util.List;

public record UserSignUpResponse(
        Long logId,
        boolean pcForm,
        boolean singForm,
        boolean pingForm
) {
    /**
     * 생성한 방명록 ID와 선택한 시설별 신청서 작성 필요 여부를 반환한다.
     */
    public static UserSignUpResponse from(Long logId, List<Purpose> purposes) {
        boolean pcForm = false;
        boolean singForm = false;
        boolean pingForm = false;

        for(Purpose purpose : purposes) {
            switch(purpose.getPurposeName()) {
                case "인터넷" -> pcForm = true;
                case "노래방" -> singForm = true;
                case "탁구" -> pingForm = true;
            }
        }

        return new UserSignUpResponse(logId, pcForm, singForm, pingForm);
    }
}
