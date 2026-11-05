package com.example.burserker.beopdong.domain.check_in.service;

import com.example.burserker.beopdong.domain.check_in.client.NeisSchoolClient;
import com.example.burserker.beopdong.domain.user.domain.enums.SchoolType;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.SchoolResponse;
import com.example.burserker.beopdong.domain.check_in.presentation.dto.response.SchoolSearchResponse;
import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;
import com.example.burserker.beopdong.domain.check_in.exception.SchoolNameRequiredException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchSchoolService {
    private final NeisSchoolClient neisSchoolClient;

    public SchoolSearchResponse execute(String name) {
        if(name == null || name.isBlank()) {
            throw SchoolNameRequiredException.EXCEPTION;
        }

        List<SchoolResponse> schools = new ArrayList<>();
        int page = 1;

        while(true) {
            JsonNode response = neisSchoolClient.search(name.trim(), page);

            if(response == null) {
                throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            JsonNode result = response.path("RESULT");

            if(!result.isMissingNode()) {
                if("INFO-200".equals(result.path("CODE").asText())) {
                    break;
                }

                throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            JsonNode schoolInfo = response.path("schoolInfo");

            if(!schoolInfo.isArray() || schoolInfo.size() < 2) {
                throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            JsonNode rows = schoolInfo.get(1).path("row");

            if(!rows.isArray()) {
                throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            for(JsonNode row : rows) {
                schools.add(new SchoolResponse(
                        row.path("ATPT_OFCDC_SC_CODE").asText(),
                        row.path("SD_SCHUL_CODE").asText(),
                        row.path("SCHUL_NM").asText(),
                        getSchoolType(row.path("SCHUL_KND_SC_NM").asText()),
                        row.path("ORG_RDNMA").asText()
                ));
            }

            int total = schoolInfo.get(0)
                    .path("head")
                    .path(0)
                    .path("list_total_count")
                    .asInt(-1);

            if(total < 0 || rows.isEmpty() && schools.size() < total) {
                throw new BeopdongException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            if(schools.size() >= total) {
                break;
            }

            page++;
        }

        return new SchoolSearchResponse(schools);
    }

    private SchoolType getSchoolType(String schoolType) {
        return switch(schoolType) {
            case "초등학교" -> SchoolType.ELEMENTARY;
            case "중학교" -> SchoolType.MIDDLE;
            case "고등학교" -> SchoolType.HIGH;
            default -> SchoolType.OTHER;
        };
    }
}