package com.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-only-for-product-context-tests",
        "spring.sql.init.mode=never",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false"
})
class ProductApplicationTests {

    @Test
    void contextLoads() {
    }
}
