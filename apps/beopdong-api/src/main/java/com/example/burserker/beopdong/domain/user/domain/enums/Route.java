package com.example.burserker.beopdong.domain.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Route {
    FRIEND("친구가 데려와서"),
    PARENT("부모님의 권유"),
    OTHER("기타");

    private final String label;
}