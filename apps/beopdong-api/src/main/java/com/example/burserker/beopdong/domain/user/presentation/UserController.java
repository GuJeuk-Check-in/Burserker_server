package com.example.burserker.beopdong.domain.user.presentation;

import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserDetailResponse;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserListResponse;
import com.example.burserker.beopdong.domain.user.service.QueryUserDetailService;
import com.example.burserker.beopdong.domain.user.service.QueryUserListService;
import com.example.burserker.beopdong.domain.user.presentation.dto.request.UpdateUserRequest;
import com.example.burserker.beopdong.domain.check_in.service.SearchSchoolService;
import com.example.burserker.beopdong.domain.user.service.UpdateUserService;
import com.example.burserker.beopdong.domain.user.service.QueryUserListByNameService;
import com.example.burserker.beopdong.domain.user.presentation.dto.response.UserSearchResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final QueryUserListService queryUserListService;
    private final QueryUserDetailService queryUserDetailService;
    private final UpdateUserService updateUserService;
    private final SearchSchoolService searchSchoolService;
    private final QueryUserListByNameService queryUserListByNameService;

    @GetMapping
    public Slice<UserListResponse> queryAllUser(Pageable pageable) {
        return queryUserListService.execute(pageable);
    }

    @GetMapping("/{id}")
    public UserDetailResponse queryUserDetail(@PathVariable Long id) {
        return queryUserDetailService.execute(id);
    }

    @PatchMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        updateUserService.execute(id, request);
    }

    @GetMapping("/search")
    public UserSearchResponse searchUser(@RequestParam String name) {
        return queryUserListByNameService.execute(name);
    }

}
