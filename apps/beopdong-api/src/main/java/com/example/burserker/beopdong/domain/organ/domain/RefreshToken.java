package com.example.burserker.beopdong.domain.organ.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;


@RedisHash("RefreshToken")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String token;

    @Indexed
    private String organName;

    @TimeToLive
    private Long timeToLive;
}
