package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.types.ULong;
import org.junit.jupiter.api.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.api.businesscontext.ContextSchemeReadRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.AbstractJooqScoreRepositoryTest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeValueRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContextSchemeReadRepositoryTest
        extends AbstractJooqScoreRepositoryTest {

    private ContextSchemeReadRepository repository;
    private ScoreUser requester;
    private BigInteger contextCategoryId;
    private List<BigInteger> contextSchemeIds = new ArrayList();

    @BeforeAll
    void setUp() {
        repository = scoreRepositoryFactory().createContextSchemeReadRepository();
        requester = new ScoreUser(BigInteger.ONE, "oagis", "Open Applications Group Developer", DEVELOPER);
        contextCategoryId = createContextCategory();

        int cnt = 20;
        for (int i = 0; i < cnt; ++i) {
            createContextScheme();
        }
    }

    private BigInteger createContextCategory() {
        CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester);
        request.setName(RandomStringUtils.random(45, true, true));
        request.setDescription(RandomStringUtils.random(1000, true, true));
        return scoreRepositoryFactory()
                .createContextCategoryWriteRepository().createContextCategory(request)
                .getContextCategoryId();
    }

    private void createContextScheme() {
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

        BigInteger contextSchemeId = scoreRepositoryFactory().createContextSchemeWriteRepository()
                .createContextScheme(request)
                .getContextSchemeId();

        contextSchemeIds.add(contextSchemeId);
    }

    @Test
    @Order(1)
    public void getContextSchemeTest() {
        Assumptions.assumeTrue(!contextSchemeIds.isEmpty());

        GetContextSchemeRequest request = new GetContextSchemeRequest(requester);
        request.setContextSchemeId(contextSchemeIds.get(0));
        GetContextSchemeResponse response = repository.getContextScheme(request);
        assertNotNull(response.getContextScheme());

        ContextScheme contextScheme = response.getContextScheme();
        CtxSchemeRecord record = dslContext().selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextScheme.getContextSchemeId())))
                .fetchOptional().orElse(null);

        assertNotNull(record);
        assertEquals(record.getGuid(), contextScheme.getGuid());
        assertEquals(record.getSchemeId(), contextScheme.getSchemeId());
        assertEquals(record.getSchemeName(), contextScheme.getSchemeName());
        assertEquals(record.getDescription(), contextScheme.getDescription());
        assertEquals(record.getSchemeAgencyId(), contextScheme.getSchemeAgencyId());
        assertEquals(record.getSchemeVersionId(), contextScheme.getSchemeVersionId());
        assertEquals(record.getCreationTimestamp(), contextScheme.getCreationTimestamp());
        assertEquals(record.getLastUpdateTimestamp(), contextScheme.getLastUpdateTimestamp());

        Map<String, CtxSchemeValueRecord> valueRecords = dslContext().selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextScheme.getContextSchemeId())))
                .orderBy(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                .fetchStream().collect(Collectors.toMap(CtxSchemeValueRecord::getValue, Function.identity()));

        assertEquals(contextScheme.getContextSchemeValueList().size(), valueRecords.size());
        for (ContextSchemeValue contextSchemeValue : contextScheme.getContextSchemeValueList()) {
            CtxSchemeValueRecord valueRecord = valueRecords.get(contextSchemeValue.getValue());
            assertEquals(valueRecord.getCtxSchemeValueId().toBigInteger(), contextSchemeValue.getContextSchemeValueId());
            assertEquals(valueRecord.getGuid(), contextSchemeValue.getGuid());
            assertEquals(valueRecord.getValue(), contextSchemeValue.getValue());
            assertEquals(valueRecord.getMeaning(), contextSchemeValue.getMeaning());
        }
    }

    @AfterAll
    void tearDown() {
        dslContext().deleteFrom(CTX_SCHEME_VALUE).execute();
        dslContext().deleteFrom(CTX_SCHEME).execute();
        dslContext().deleteFrom(CTX_CATEGORY).execute();
    }
}
