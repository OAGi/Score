package org.oagi.score.repo.api.impl.jooq;

import org.junit.jupiter.api.Test;
import org.oagi.score.repo.api.ScoreRepositoryFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JooqScoreRepositoryFactoryTest extends AbstractJooqScoreRepositoryTest {

    @Test
    void instantiateTest() {
        ScoreRepositoryFactory scoreRepositoryFactory = scoreRepositoryFactory();
        assertNotNull(scoreRepositoryFactory);
    }
}
