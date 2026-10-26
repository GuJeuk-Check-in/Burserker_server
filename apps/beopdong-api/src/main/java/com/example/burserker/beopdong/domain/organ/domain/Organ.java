package com.example.burserker.beopdong.domain.organ.domain;

import com.example.burserker.beopdong.global.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organ extends BaseIdEntity {

    @Column(nullable = false, unique = true)
    private String organName;

    @Column(nullable = false)
    private String password;

    public void changePassword(String password) {this.password = password;}

}
