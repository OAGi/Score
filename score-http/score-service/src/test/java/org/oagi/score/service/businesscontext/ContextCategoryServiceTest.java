package org.oagi.score.service.businesscontext;

import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxCategoryRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.AbstractServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_CATEGORY;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;

public class ContextCategoryServiceTest extends AbstractServiceTest {

    private ScoreUser requester;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ContextCategoryService contextCategoryService;

    @BeforeAll
    void setUp() {
        requester = new ScoreUser(BigInteger.ONE, "oagis", "Open Applications Group Developer", DEVELOPER);
    }

    @Test
    public void testCreateContextCategory() {
        CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                .withName(RandomStringUtils.randomAlphanumeric(45))
                .withDescription(RandomStringUtils.randomAlphanumeric(200));

        CreateContextCategoryResponse response =
                contextCategoryService.createContextCategory(request);

        assertNotNull(response);
        assertNotNull(response.getContextCategoryId());

        CtxCategoryRecord ctxCategoryRecord = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(response.getContextCategoryId())))
                .fetchOne();

        assertEquals(request.getName(), ctxCategoryRecord.getName());
        assertEquals(request.getDescription(), ctxCategoryRecord.getDescription());
    }

    @Test
    public void testUpdateContextCategory() {
        ContextCategory contextCategory;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            contextCategory = contextCategoryService.getContextCategory(
                    new GetContextCategoryRequest(requester)
                            .withContextCategoryId(response.getContextCategoryId()))
                    .getContextCategory();
        }

        UpdateContextCategoryRequest request = new UpdateContextCategoryRequest(requester)
                .withContextCategoryId(contextCategory.getContextCategoryId())
                .withName(RandomStringUtils.randomAlphanumeric(45))
                .withDescription(RandomStringUtils.randomAlphanumeric(200));

        UpdateContextCategoryResponse response =
                contextCategoryService.updateContextCategory(request);

        assertNotNull(response);
        assertNotNull(response.getContextCategoryId());
        assertEquals(request.getContextCategoryId(), response.getContextCategoryId());

        CtxCategoryRecord ctxCategoryRecord = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(response.getContextCategoryId())))
                .fetchOne();

        assertEquals(contextCategory.getGuid(), ctxCategoryRecord.getGuid());
        assertEquals(request.getName(), ctxCategoryRecord.getName());
        assertEquals(request.getDescription(), ctxCategoryRecord.getDescription());
    }

    @Test
    public void testDeleteContextCategory() {
        ContextCategory contextCategory;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            contextCategory = contextCategoryService.getContextCategory(
                    new GetContextCategoryRequest(requester)
                            .withContextCategoryId(response.getContextCategoryId()))
                    .getContextCategory();
        }

        DeleteContextCategoryRequest request = new DeleteContextCategoryRequest(requester)
                .withContextCategoryId(contextCategory.getContextCategoryId());

        DeleteContextCategoryResponse response =
                contextCategoryService.deleteContextCategory(request);

        assertNotNull(response);
        assertNotNull(response.getContextCategoryIdList());
        assertTrue(response.contains(contextCategory.getContextCategoryId()));

        CtxCategoryRecord ctxCategoryRecord = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategory.getContextCategoryId())))
                .fetchOptional().orElse(null);

        assertNull(ctxCategoryRecord);
    }

    @Test
    public void testGetContextCategory() {
        BigInteger contextCategoryId;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            contextCategoryId = response.getContextCategoryId();
        }

        GetContextCategoryRequest request = new GetContextCategoryRequest(requester)
                .withContextCategoryId(contextCategoryId);

        GetContextCategoryResponse response = contextCategoryService.getContextCategory(request);
        assertNotNull(response);

        ContextCategory contextCategory = response.getContextCategory();
        assertNotNull(contextCategory);

        CtxCategoryRecord ctxCategoryRecord = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategory.getContextCategoryId())))
                .fetchOne();

        assertEquals(ctxCategoryRecord.getCtxCategoryId().toBigInteger(), contextCategory.getContextCategoryId());
        assertEquals(ctxCategoryRecord.getGuid(), contextCategory.getGuid());
        assertEquals(ctxCategoryRecord.getName(), contextCategory.getName());
        assertEquals(ctxCategoryRecord.getDescription(), contextCategory.getDescription());
        assertEquals(ctxCategoryRecord.getCreatedBy().toBigInteger(), contextCategory.getCreatedBy().getUserId());
        assertEquals(ctxCategoryRecord.getLastUpdatedBy().toBigInteger(), contextCategory.getLastUpdatedBy().getUserId());
    }

    @Test
    public void testGetContextCategoryListWithNameFilter() {
        ContextCategory contextCategory;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            BigInteger contextCategoryId = response.getContextCategoryId();

            contextCategory =
                    contextCategoryService.getContextCategory(new GetContextCategoryRequest(requester)
                            .withContextCategoryId(contextCategoryId))
                            .getContextCategory();
        }

        GetContextCategoryListRequest request = new GetContextCategoryListRequest(requester)
                .withName(contextCategory.getName());

        GetContextCategoryListResponse response =
                contextCategoryService.getContextCategoryList(request);
        assertNotNull(response);

        List<ContextCategory> results = response.getResults();
        assertNotNull(results);
        assertTrue(results.size() == 1);

        ContextCategory resultContextCategory = results.get(0);
        assertEquals(contextCategory.getContextCategoryId(), resultContextCategory.getContextCategoryId());
        assertEquals(contextCategory.getGuid(), resultContextCategory.getGuid());
        assertEquals(contextCategory.getName(), resultContextCategory.getName());
        assertEquals(contextCategory.getDescription(), resultContextCategory.getDescription());
        assertEquals(contextCategory.getCreatedBy(), resultContextCategory.getCreatedBy());
        assertEquals(contextCategory.getCreationTimestamp(), resultContextCategory.getCreationTimestamp());
        assertEquals(contextCategory.getLastUpdatedBy(), resultContextCategory.getLastUpdatedBy());
        assertEquals(contextCategory.getLastUpdateTimestamp(), resultContextCategory.getLastUpdateTimestamp());
    }

    @Test
    public void testGetContextCategoryListWithDescriptionFilter() {
        ContextCategory contextCategory;
        {
            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester)
                    .withName(RandomStringUtils.randomAlphanumeric(45))
                    .withDescription(RandomStringUtils.randomAlphanumeric(200));

            CreateContextCategoryResponse response =
                    contextCategoryService.createContextCategory(request);

            BigInteger contextCategoryId = response.getContextCategoryId();

            contextCategory =
                    contextCategoryService.getContextCategory(new GetContextCategoryRequest(requester)
                            .withContextCategoryId(contextCategoryId))
                            .getContextCategory();
        }

        GetContextCategoryListRequest request = new GetContextCategoryListRequest(requester)
                .withDescription(contextCategory.getDescription());

        GetContextCategoryListResponse response =
                contextCategoryService.getContextCategoryList(request);
        assertNotNull(response);

        List<ContextCategory> results = response.getResults();
        assertNotNull(results);
        assertTrue(results.size() == 1);

        ContextCategory resultContextCategory = results.get(0);
        assertEquals(contextCategory.getContextCategoryId(), resultContextCategory.getContextCategoryId());
        assertEquals(contextCategory.getGuid(), resultContextCategory.getGuid());
        assertEquals(contextCategory.getName(), resultContextCategory.getName());
        assertEquals(contextCategory.getDescription(), resultContextCategory.getDescription());
        assertEquals(contextCategory.getCreatedBy(), resultContextCategory.getCreatedBy());
        assertEquals(contextCategory.getCreationTimestamp(), resultContextCategory.getCreationTimestamp());
        assertEquals(contextCategory.getLastUpdatedBy(), resultContextCategory.getLastUpdatedBy());
        assertEquals(contextCategory.getLastUpdateTimestamp(), resultContextCategory.getLastUpdateTimestamp());
    }
}
