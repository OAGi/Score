package org.oagi.score.repo;

import org.oagi.score.gateway.http.ScoreHttpApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = ScoreHttpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class BusinessContextRepositoryTest {

}
