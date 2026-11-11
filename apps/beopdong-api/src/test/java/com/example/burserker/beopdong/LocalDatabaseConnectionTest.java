package com.example.burserker.beopdong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 로컬 환경변수를 명시적으로 제공한 경우에만 실제 MySQL을 사용합니다.
// CI의 일반 빌드에는 개발자의 DB나 비밀값이 필요하지 않습니다.
@EnabledIfEnvironmentVariable(named = "BEOPDONG_LOCAL_DB_TEST", matches = "true")
// 연결 확인만으로 기존 로컬 DB의 마이그레이션이 실행되지 않도록 막습니다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.flyway.enabled=false")
class LocalDatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToLocalBeopdongSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            assertTrue(url.startsWith("jdbc:mysql://localhost:")
                    || url.startsWith("jdbc:mysql://127.0.0.1:"));
            // macOS MySQL은 DB 이름을 소문자로 반환할 수 있습니다.
            assertEquals("beopdong", connection.getCatalog().toLowerCase(java.util.Locale.ROOT));
        }
    }

    @Test
    void executesReadOnlyQueryThroughApplicationDataSource() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT DATABASE(), 1")) {
            assertTrue(result.next());
            assertEquals("beopdong", result.getString(1).toLowerCase(java.util.Locale.ROOT));
            assertEquals(1, result.getInt(2));
        }
    }
}
