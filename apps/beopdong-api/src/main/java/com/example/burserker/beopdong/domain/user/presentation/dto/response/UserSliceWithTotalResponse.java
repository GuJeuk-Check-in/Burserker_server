package com.example.burserker.beopdong.domain.user.presentation.dto.response;

import org.springframework.data.domain.Slice;

public record UserSliceWithTotalResponse(
        long totalCount,
        Slice<UserListResponse> slice
) {
}