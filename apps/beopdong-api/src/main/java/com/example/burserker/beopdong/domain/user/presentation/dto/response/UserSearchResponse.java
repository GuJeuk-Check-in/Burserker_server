package com.example.burserker.beopdong.domain.user.presentation.dto.response;

import java.util.List;

public record UserSearchResponse(
        long totalCount,
        List<UserListResponse> content
) {
}