package com.example.burserker.beopdong.domain.user.service;

import com.example.burserker.beopdong.domain.user.domain.repository.UserRepository;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserListResponse;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserSliceWithTotalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryUserListService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Slice<UserListResponse> execute(Pageable p) {
        Pageable pageable = PageRequest.of(
                p.getPageNumber(),
                30,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return userRepository.findAllBy(pageable)
                .map(UserListResponse::from);
    }
}
