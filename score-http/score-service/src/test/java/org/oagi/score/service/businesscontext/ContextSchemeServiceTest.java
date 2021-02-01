package org.oagi.score.service.businesscontext;

import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxSchemeValueRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.AbstractServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME_VALUE;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;

public class ContextSchemeServiceTest extends AbstractServiceTest {

    private ScoreUser requester;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    @BeforeAll
    void setUp() {
        requester = new ScoreUser(BigInteger.ONE, "oagis", DEVELOPER);
    }

    @Test
    public void testCreateContextScheme() {
        BigInteger contextCategoryId;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            contextCategoryId = response.getContextCategoryId();
        }

        CreateContextSchemeRequest request = new CreateContextSchemeRequest(requester)
                .withContextCategoryId(contextCategoryId)
                .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                .withDescription(RandomStringUtils.randomAlphanumeric(200));

        CreateContextSchemeResponse response =
                contextSchemeService.createContextScheme(request);

        assertNotNull(response);
        assertNotNull(response.getContextSchemeId());

        CtxSchemeRecord ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .fetchOne();

        assertEquals(request.getSchemeId(), ctxCategoryRecord.getSchemeId());
        assertEquals(request.getSchemeName(), ctxCategoryRecord.getSchemeName());
        assertEquals(request.getSchemeAgencyId(), ctxCategoryRecord.getSchemeAgencyId());
        assertEquals(request.getSchemeVersionId(), ctxCategoryRecord.getSchemeVersionId());
        assertEquals(request.getDescription(), ctxCategoryRecord.getDescription());
    }

    @Test
    public void testCreateContextSchemeWithValues() {
        BigInteger contextCategoryId;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            contextCategoryId = response.getContextCategoryId();
        }

        CreateContextSchemeRequest request = new CreateContextSchemeRequest(requester)
                .withContextCategoryId(contextCategoryId)
                .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                .withDescription(RandomStringUtils.randomAlphanumeric(200))
                .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                        RandomStringUtils.randomAlphanumeric(200))
                .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                        RandomStringUtils.randomAlphanumeric(200))
                .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                        RandomStringUtils.randomAlphanumeric(200));

        CreateContextSchemeResponse response =
                contextSchemeService.createContextScheme(request);

        assertNotNull(response);
        assertNotNull(response.getContextSchemeId());

        CtxSchemeRecord ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .fetchOne();

        assertEquals(request.getSchemeId(), ctxCategoryRecord.getSchemeId());
        assertEquals(request.getSchemeName(), ctxCategoryRecord.getSchemeName());
        assertEquals(request.getSchemeAgencyId(), ctxCategoryRecord.getSchemeAgencyId());
        assertEquals(request.getSchemeVersionId(), ctxCategoryRecord.getSchemeVersionId());
        assertEquals(request.getDescription(), ctxCategoryRecord.getDescription());

        List<CtxSchemeValueRecord> ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .orderBy(CTX_SCHEME_VALUE.GUID.asc())
                .fetch();

        List<ContextSchemeValue> contextSchemeValueList = new ArrayList(request.getContextSchemeValueList());
        assertEquals(contextSchemeValueList.size(), ctxSchemeValueRecordList.size());

        Collections.sort(contextSchemeValueList, Comparator.comparing(ContextSchemeValue::getGuid));
        for (int i = 0; i < contextSchemeValueList.size(); ++i) {
            ContextSchemeValue contextSchemeValue = contextSchemeValueList.get(i);
            CtxSchemeValueRecord ctxSchemeValueRecord = ctxSchemeValueRecordList.get(i);

            assertEquals(contextSchemeValue.getGuid(), ctxSchemeValueRecord.getGuid());
            assertEquals(contextSchemeValue.getValue(), ctxSchemeValueRecord.getValue());
            assertEquals(contextSchemeValue.getMeaning(), ctxSchemeValueRecord.getMeaning());
        }
    }

    @Test
    public void testUpdateContextSchemeWithValues() {
        CtxSchemeRecord ctxCategoryRecord;
        List<CtxSchemeValueRecord> ctxSchemeValueRecordList;
        {
            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(new CreateContextCategoryRequest(requester)
                            .withName(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200)));

            BigInteger contextCategoryId = response.getContextCategoryId();

            BigInteger contextSchemeId =
                    contextSchemeService.createContextScheme(new CreateContextSchemeRequest(requester)
                            .withContextCategoryId(contextCategoryId)
                            .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                            .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200)))
                            .getContextSchemeId();

            ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .fetchOne();

            ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                    .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .orderBy(CTX_SCHEME_VALUE.GUID.asc())
                    .fetch();
        }

        assertEquals(3, ctxSchemeValueRecordList.size());

        List<ContextSchemeValue> updateContextSchemeValueList = new ArrayList();
        ContextSchemeValue contextSchemeValue1 = new ContextSchemeValue(); // Update
        contextSchemeValue1.setContextSchemeValueId(ctxSchemeValueRecordList.get(0).getCtxSchemeValueId().toBigInteger());
        contextSchemeValue1.setGuid(ctxSchemeValueRecordList.get(0).getGuid());
        contextSchemeValue1.setValue(RandomStringUtils.randomAlphanumeric(100));
        contextSchemeValue1.setMeaning(RandomStringUtils.randomAlphanumeric(200));
        updateContextSchemeValueList.add(contextSchemeValue1);

        ContextSchemeValue contextSchemeValue2 = new ContextSchemeValue(); // Insert
        contextSchemeValue2.setValue(RandomStringUtils.randomAlphanumeric(100));
        contextSchemeValue2.setMeaning(RandomStringUtils.randomAlphanumeric(200));
        updateContextSchemeValueList.add(contextSchemeValue2);

        UpdateContextSchemeRequest request = new UpdateContextSchemeRequest(requester)
                .withContextSchemeId(ctxCategoryRecord.getCtxSchemeId().toBigInteger())
                .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                .withDescription(RandomStringUtils.randomAlphanumeric(200))
                .withContextSchemeValueList(updateContextSchemeValueList);

        UpdateContextSchemeResponse response =
                contextSchemeService.updateContextScheme(request);

        assertNotNull(response);
        assertNotNull(response.getContextSchemeId());

        ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .fetchOne();

        assertEquals(request.getSchemeId(), ctxCategoryRecord.getSchemeId());
        assertEquals(request.getSchemeName(), ctxCategoryRecord.getSchemeName());
        assertEquals(request.getSchemeAgencyId(), ctxCategoryRecord.getSchemeAgencyId());
        assertEquals(request.getSchemeVersionId(), ctxCategoryRecord.getSchemeVersionId());
        assertEquals(request.getDescription(), ctxCategoryRecord.getDescription());

        ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(response.getContextSchemeId())))
                .orderBy(CTX_SCHEME_VALUE.GUID.asc())
                .fetch();

        List<ContextSchemeValue> contextSchemeValueList = new ArrayList(request.getContextSchemeValueList());
        assertEquals(contextSchemeValueList.size(), ctxSchemeValueRecordList.size());

        Collections.sort(contextSchemeValueList, Comparator.comparing(ContextSchemeValue::getGuid));
        for (int i = 0; i < contextSchemeValueList.size(); ++i) {
            ContextSchemeValue contextSchemeValue = contextSchemeValueList.get(i);
            CtxSchemeValueRecord ctxSchemeValueRecord = ctxSchemeValueRecordList.get(i);

            assertEquals(contextSchemeValue.getGuid(), ctxSchemeValueRecord.getGuid());
            assertEquals(contextSchemeValue.getValue(), ctxSchemeValueRecord.getValue());
            assertEquals(contextSchemeValue.getMeaning(), ctxSchemeValueRecord.getMeaning());
        }
    }

    @Test
    public void testDeleteContextSchemeWithValues() {
        CtxSchemeRecord ctxCategoryRecord;
        List<CtxSchemeValueRecord> ctxSchemeValueRecordList;
        {
            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(new CreateContextCategoryRequest(requester)
                            .withName(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200)));

            BigInteger contextCategoryId = response.getContextCategoryId();

            BigInteger contextSchemeId =
                    contextSchemeService.createContextScheme(new CreateContextSchemeRequest(requester)
                            .withContextCategoryId(contextCategoryId)
                            .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                            .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200)))
                            .getContextSchemeId();

            ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .fetchOne();

            ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                    .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .orderBy(CTX_SCHEME_VALUE.GUID.asc())
                    .fetch();
        }

        BigInteger contextSchemeId = ctxCategoryRecord.getCtxSchemeId().toBigInteger();
        DeleteContextSchemeRequest request = new DeleteContextSchemeRequest(requester)
                .withContextSchemeId(contextSchemeId);

        DeleteContextSchemeResponse response =
                contextSchemeService.deleteContextScheme(request);

        assertNotNull(response);
        assertNotNull(response.contains(contextSchemeId));

        ctxCategoryRecord = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                .fetchOptional().orElse(null);

        assertNull(ctxCategoryRecord);

        ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq((ULong.valueOf(contextSchemeId))))
                .fetch();

        assertTrue(ctxSchemeValueRecordList.isEmpty());
    }

    @Test
    public void testGetContextSchemeWithValues() {
        CtxSchemeRecord ctxSchemeRecord;
        List<CtxSchemeValueRecord> ctxSchemeValueRecordList;
        {
            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(new CreateContextCategoryRequest(requester)
                            .withName(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200)));

            BigInteger contextCategoryId = response.getContextCategoryId();

            BigInteger contextSchemeId =
                    contextSchemeService.createContextScheme(new CreateContextSchemeRequest(requester)
                            .withContextCategoryId(contextCategoryId)
                            .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                            .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200)))
                            .getContextSchemeId();

            ctxSchemeRecord = dslContext.selectFrom(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .fetchOne();

            ctxSchemeValueRecordList = dslContext.selectFrom(CTX_SCHEME_VALUE)
                    .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                    .orderBy(CTX_SCHEME_VALUE.GUID.asc())
                    .fetch();
        }

        BigInteger contextSchemeId = ctxSchemeRecord.getCtxSchemeId().toBigInteger();
        GetContextSchemeRequest request = new GetContextSchemeRequest(requester)
                .withContextSchemeId(contextSchemeId);

        GetContextSchemeResponse response =
                contextSchemeService.getContextScheme(request);

        assertNotNull(response);
        assertNotNull(response.getContextScheme());

        ContextScheme contextScheme = response.getContextScheme();
        assertEquals(ctxSchemeRecord.getSchemeId(), contextScheme.getSchemeId());
        assertEquals(ctxSchemeRecord.getSchemeName(), contextScheme.getSchemeName());
        assertEquals(ctxSchemeRecord.getSchemeAgencyId(), contextScheme.getSchemeAgencyId());
        assertEquals(ctxSchemeRecord.getSchemeVersionId(), contextScheme.getSchemeVersionId());
        assertEquals(ctxSchemeRecord.getDescription(), contextScheme.getDescription());

        List<ContextSchemeValue> contextSchemeValueList = new ArrayList(contextScheme.getContextSchemeValueList());
        assertEquals(ctxSchemeValueRecordList.size(), contextSchemeValueList.size());

        Collections.sort(contextSchemeValueList, Comparator.comparing(ContextSchemeValue::getGuid));
        for (int i = 0; i < contextSchemeValueList.size(); ++i) {
            ContextSchemeValue contextSchemeValue = contextSchemeValueList.get(i);
            CtxSchemeValueRecord ctxSchemeValueRecord = ctxSchemeValueRecordList.get(i);

            assertEquals(ctxSchemeValueRecord.getGuid(), contextSchemeValue.getGuid());
            assertEquals(ctxSchemeValueRecord.getValue(), contextSchemeValue.getValue());
            assertEquals(ctxSchemeValueRecord.getMeaning(), contextSchemeValue.getMeaning());
        }
    }

    @Test
    public void testGetContextSchemeWithSchemeNameFilter() {
        ContextScheme contextScheme;
        {
            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(new CreateContextCategoryRequest(requester)
                            .withName(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200)));

            BigInteger contextCategoryId = response.getContextCategoryId();

            BigInteger contextSchemeId =
                    contextSchemeService.createContextScheme(new CreateContextSchemeRequest(requester)
                            .withContextCategoryId(contextCategoryId)
                            .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                            .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200)))
                            .getContextSchemeId();

            contextScheme =
                    contextSchemeService.getContextScheme(new GetContextSchemeRequest(requester)
                            .withContextSchemeId(contextSchemeId))
                            .getContextScheme();
        }

        GetContextSchemeListRequest request = new GetContextSchemeListRequest(requester)
                .withSchemeName(contextScheme.getSchemeName());

        GetContextSchemeListResponse response =
                contextSchemeService.getContextSchemeList(request);
        assertNotNull(response);

        List<ContextScheme> results = response.getResults();
        assertNotNull(results);
        assertTrue(results.size() == 1);

        ContextScheme resultContextScheme = results.get(0);
        assertEquals(contextScheme.getSchemeId(), resultContextScheme.getSchemeId());
        assertEquals(contextScheme.getSchemeName(), resultContextScheme.getSchemeName());
        assertEquals(contextScheme.getSchemeAgencyId(), resultContextScheme.getSchemeAgencyId());
        assertEquals(contextScheme.getSchemeVersionId(), resultContextScheme.getSchemeVersionId());
        assertEquals(contextScheme.getDescription(), resultContextScheme.getDescription());

        assertNull(resultContextScheme.getContextSchemeValueList());
    }

    @Test
    public void testGetContextSchemeWithDescriptionFilter() {
        ContextScheme contextScheme;
        {
            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(new CreateContextCategoryRequest(requester)
                            .withName(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200)));

            BigInteger contextCategoryId = response.getContextCategoryId();

            BigInteger contextSchemeId =
                    contextSchemeService.createContextScheme(new CreateContextSchemeRequest(requester)
                            .withContextCategoryId(contextCategoryId)
                            .withSchemeId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeName(RandomStringUtils.randomAlphanumeric(255))
                            .withSchemeAgencyId(RandomStringUtils.randomAlphanumeric(45))
                            .withSchemeVersionId(RandomStringUtils.randomAlphanumeric(45))
                            .withDescription(RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200))
                            .withContextSchemeValue(RandomStringUtils.randomAlphanumeric(100),
                                    RandomStringUtils.randomAlphanumeric(200)))
                            .getContextSchemeId();

            contextScheme =
                    contextSchemeService.getContextScheme(new GetContextSchemeRequest(requester)
                            .withContextSchemeId(contextSchemeId))
                            .getContextScheme();
        }

        GetContextSchemeListRequest request = new GetContextSchemeListRequest(requester)
                .withDescription(contextScheme.getDescription());

        GetContextSchemeListResponse response =
                contextSchemeService.getContextSchemeList(request);
        assertNotNull(response);

        List<ContextScheme> results = response.getResults();
        assertNotNull(results);
        assertTrue(results.size() == 1);

        ContextScheme resultContextScheme = results.get(0);
        assertEquals(contextScheme.getSchemeId(), resultContextScheme.getSchemeId());
        assertEquals(contextScheme.getSchemeName(), resultContextScheme.getSchemeName());
        assertEquals(contextScheme.getSchemeAgencyId(), resultContextScheme.getSchemeAgencyId());
        assertEquals(contextScheme.getSchemeVersionId(), resultContextScheme.getSchemeVersionId());
        assertEquals(contextScheme.getDescription(), resultContextScheme.getDescription());

        assertNull(resultContextScheme.getContextSchemeValueList());
    }
}
