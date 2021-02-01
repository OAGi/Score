package org.oagi.score.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = org.oagi.score.service.configuration.TestConfiguration.class)
public class AbstractServiceTest {

    @BeforeAll
    public void setUpFirst() {
        System.setProperty("org.jooq.no-logo", "true");
    }

}
