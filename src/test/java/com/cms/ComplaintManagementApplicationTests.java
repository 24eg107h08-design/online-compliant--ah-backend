package com.cms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("h2")
class ComplaintManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring application context loads properly with beans
    }
}
