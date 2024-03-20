package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.types.ULong;
import org.junit.jupiter.api.*;
import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.api.businesscontext.ContextCategoryReadRepository;
import org.oagi.score.repo.api.businesscontext.ContextCategoryWriteRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.AbstractJooqScoreRepositoryTest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CtxCategoryRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_CATEGORY;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContextCategoryReadRepositoryTest
        extends AbstractJooqScoreRepositoryTest {

    private ContextCategoryReadRepository repository;
    private ScoreUser requester;
    private List<BigInteger> contextCategoryIds = new ArrayList();

    @BeforeAll
    void setUp() {
        repository = scoreRepositoryFactory().createContextCategoryReadRepository();
        requester = new ScoreUser(BigInteger.ONE, "oagis", "Open Applications Group Developer", DEVELOPER);

        int cnt = 20;
        for (int i = 0; i < cnt; ++i) {
            ContextCategoryWriteRepository writeRepository =
                    scoreRepositoryFactory().createContextCategoryWriteRepository();

            CreateContextCategoryRequest request = new CreateContextCategoryRequest(requester);
            request.setName(RandomStringUtils.random(45, true, true));
            request.setDescription(RandomStringUtils.random(1000, true, true));
            contextCategoryIds.add(writeRepository.createContextCategory(request).getContextCategoryId());
        }
    }

    @Test
    @Order(1)
    public void getContextCategoryTestWithoutParameter() {
        GetContextCategoryRequest request = new GetContextCategoryRequest(requester);
        GetContextCategoryResponse response = repository.getContextCategory(request);
        assertNull(response.getContextCategory());
    }

    @Test
    @Order(2)
    public void getContextCategoryTest() {
        Assumptions.assumeTrue(!contextCategoryIds.isEmpty());

        GetContextCategoryRequest request = new GetContextCategoryRequest(requester);
        request.setContextCategoryId(contextCategoryIds.get(0));
        GetContextCategoryResponse response = repository.getContextCategory(request);
        assertNotNull(response.getContextCategory());

        ContextCategory contextCategory = response.getContextCategory();
        CtxCategoryRecord record = dslContext().selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategory.getContextCategoryId())))
                .fetchOptional().orElse(null);

        assertNotNull(record);
        assertEquals(record.getGuid(), contextCategory.getGuid());
        assertEquals(record.getName(), contextCategory.getName());
        assertEquals(record.getDescription(), contextCategory.getDescription());
        assertEquals(record.getCreationTimestamp(), contextCategory.getCreationTimestamp());
        assertEquals(record.getLastUpdateTimestamp(), contextCategory.getLastUpdateTimestamp());
    }

    @Test
    @Order(3)
    public void listContextCategoriesTestWithoutParameter() {
        Assumptions.assumeTrue(!contextCategoryIds.isEmpty());

        ContextCategoryReadRepository repository =
                scoreRepositoryFactory().createContextCategoryReadRepository();
        GetContextCategoryListRequest request = new GetContextCategoryListRequest(requester);
        GetContextCategoryListResponse response = repository.getContextCategoryList(request);
        assertNotNull(response);
        assertTrue(!response.getResults().isEmpty());
        assertEquals(PaginationRequest.DEFAULT_PAGE_INDEX, response.getPage());
        assertEquals(PaginationRequest.DEFAULT_PAGE_SIZE, response.getSize());
        assertEquals(PaginationRequest.DEFAULT_PAGE_SIZE, response.getResults().size());

        for (ContextCategory contextCategory : response.getResults()) {
            CtxCategoryRecord record = dslContext().selectFrom(CTX_CATEGORY)
                    .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategory.getContextCategoryId())))
                    .fetchOptional().orElse(null);

            assertNotNull(record);
            assertEquals(record.getGuid(), contextCategory.getGuid());
            assertEquals(record.getName(), contextCategory.getName());
            assertEquals(record.getDescription(), contextCategory.getDescription());
            assertEquals(record.getCreationTimestamp(), contextCategory.getCreationTimestamp());
            assertEquals(record.getLastUpdateTimestamp(), contextCategory.getLastUpdateTimestamp());
        }
    }

    @AfterAll
    void tearDown() {
        dslContext().deleteFrom(CTX_CATEGORY).execute();
    }

}
