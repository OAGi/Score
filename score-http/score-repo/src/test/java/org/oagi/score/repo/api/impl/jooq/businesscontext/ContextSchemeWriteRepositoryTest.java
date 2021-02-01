package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.types.ULong;
import org.junit.jupiter.api.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.api.businesscontext.ContextSchemeWriteRepository;
import org.oagi.score.repo.api.businesscontext.model.CreateContextCategoryRequest;
import org.oagi.score.repo.api.businesscontext.model.CreateContextSchemeRequest;
import org.oagi.score.repo.api.businesscontext.model.CreateContextSchemeResponse;
import org.oagi.score.repo.api.impl.jooq.AbstractJooqScoreRepositoryTest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeValueRecord;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContextSchemeWriteRepositoryTest
        extends AbstractJooqScoreRepositoryTest {

    private ContextSchemeWriteRepository repository;
    private ScoreUser requester;
    private BigInteger contextCategoryId;

    @BeforeAll
    void setUp() {
        repository = scoreRepositoryFactory().createContextSchemeWriteRepository();
        requester = new ScoreUser(BigInteger.ONE, "oagis", DEVELOPER);
        contextCategoryId = createContextCategory();
    }

    private BigInteger createContextCategory() {
        CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester);
        request.setName(RandomStringUtils.random(45, true, true));
        request.setDescription(RandomStringUtils.random(1000, true, true));
        return scoreRepositoryFactory()
                .createContextCategoryWriteRepository().createContextCategory(request)
                .getContextCategoryId();
    }

    @Test
    @Order(1)
    public void createContextSchemeTest() {
        CreateContextSchemeRequest request = new CreateContextSchemeRequest(requester);

        request.setSchemeId(RandomStringUtils.random(45, true, true));
        request.setSchemeName(RandomStringUtils.random(255, true, true));
        request.setDescription(RandomStringUtils.random(1000, true, true));
        request.setSchemeAgencyId(RandomStringUtils.random(45, true, true));
        request.setSchemeVersionId(RandomStringUtils.random(45, true, true));
        request.setContextCategoryId(contextCategoryId);

        int cnt = 5;
        for (int i = 0; i < cnt; ++i) {
            request.addContextSchemeValue(
                    RandomStringUtils.random(45, true, true),
                    RandomStringUtils.random(1000, true, true)
            );
        }

        LocalDateTime requestTime = LocalDateTime.now();

        CreateContextSchemeResponse response = repository.createContextScheme(request);
        assertNotNull(response);
        assertNotNull(response.getContextSchemeId());

        CtxSchemeRecord record = dslContext().selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .fetchOptional().orElse(null);

        assertNotNull(record);
        assertEquals(request.getSchemeId(), record.getSchemeId());
        assertEquals(request.getSchemeName(), record.getSchemeName());
        assertEquals(request.getDescription(), record.getDescription());
        assertEquals(request.getSchemeAgencyId(), record.getSchemeAgencyId());
        assertEquals(request.getSchemeVersionId(), record.getSchemeVersionId());
        assertEquals(requester.getUserId(), record.getCreatedBy().toBigInteger());
        assertEquals(requester.getUserId(), record.getLastUpdatedBy().toBigInteger());
        assertEquals(record.getCreationTimestamp(), record.getLastUpdateTimestamp());
        assertTrue(record.getCreationTimestamp().compareTo(requestTime) > 0);

        List<CtxSchemeValueRecord> valueRecords = dslContext().selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .orderBy(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                .fetch();

        assertEquals(cnt, valueRecords.size());
    }

    @AfterAll
    void tearDown() {
        dslContext().deleteFrom(CTX_SCHEME_VALUE).execute();
        dslContext().deleteFrom(CTX_SCHEME).execute();
        dslContext().deleteFrom(CTX_CATEGORY).execute();
    }

}
