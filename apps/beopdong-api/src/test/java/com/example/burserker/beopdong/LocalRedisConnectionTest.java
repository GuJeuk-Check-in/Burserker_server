package com.example.burserker.beopdong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// 명시적으로 요청한 로컬 검증에서만 실행합니다. 운영 데이터에는 접근하지 않습니다.
@EnabledIfEnvironmentVariable(named = "BEOPDONG_LOCAL_REDIS_TEST", matches = "true")
// 연결 확인만으로 기존 로컬 DB의 마이그레이션이 실행되지 않도록 막습니다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.flyway.enabled=false")
class LocalRedisConnectionTest {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private RedisProperties properties;

    private void verifyLocalTarget() {
        assertTrue(properties.getHost().equals("localhost") || properties.getHost().equals("127.0.0.1"));
        assertEquals(1, properties.getDatabase());
    }

    @Test
    void connectsToSharedRedisWithBeopdongDatabase() {
        verifyLocalTarget();
        try (RedisConnection connection = connectionFactory.getConnection()) {
            assertEquals("PONG", connection.ping());
        }
    }

    @Test
    void separatesBeopdongKeysFromGujeukDatabase() {
        verifyLocalTarget();
        RedisStandaloneConfiguration zero = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        zero.setDatabase(0);
        LettuceConnectionFactory gujeuk = new LettuceConnectionFactory(zero);
        gujeuk.afterPropertiesSet();
        byte[] key = ("beopdong:connection-test:" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);
        byte[] original = "gujeuk-test".getBytes(StandardCharsets.UTF_8);
        byte[] independent = "beopdong-test".getBytes(StandardCharsets.UTF_8);
        try (RedisConnection db0 = gujeuk.getConnection();
             RedisConnection db1 = connectionFactory.getConnection()) {
            try {
                db0.stringCommands().setEx(key, 60, original);
                assertNull(db1.stringCommands().get(key));
                db1.stringCommands().setEx(key, 60, independent);
                assertArrayEquals(original, db0.stringCommands().get(key));
                assertArrayEquals(independent, db1.stringCommands().get(key));
            } finally {
                db0.keyCommands().del(key);
                db1.keyCommands().del(key);
            }
        } finally {
            gujeuk.destroy();
        }
    }
}
