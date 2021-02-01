package org.oagi.score.service.businesscontext;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_CATEGORY;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME;

@Service
@Transactional(readOnly = true)
public class ContextCategoryService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public GetContextCategoryListResponse getContextCategoryList(GetContextCategoryListRequest request) {
        GetContextCategoryListResponse response =
                scoreRepositoryFactory.createContextCategoryReadRepository()
                        .getContextCategoryList(request);

        return response;
    }

    public GetContextCategoryResponse getContextCategory(GetContextCategoryRequest request) {
        GetContextCategoryResponse response =
                scoreRepositoryFactory.createContextCategoryReadRepository()
                        .getContextCategory(request);

        return response;
    }

    @Transactional
    public CreateContextCategoryResponse createContextCategory(CreateContextCategoryRequest request) {
        CreateContextCategoryResponse response =
                scoreRepositoryFactory.createContextCategoryWriteRepository()
                        .createContextCategory(request);

        return response;
    }

    @Transactional
    public UpdateContextCategoryResponse updateContextCategory(UpdateContextCategoryRequest request) {
        UpdateContextCategoryResponse response =
                scoreRepositoryFactory.createContextCategoryWriteRepository()
                        .updateContextCategory(request);

        return response;
    }

    @Transactional
    public DeleteContextCategoryResponse deleteContextCategory(DeleteContextCategoryRequest request) {
        DeleteContextCategoryResponse response =
                scoreRepositoryFactory.createContextCategoryWriteRepository()
                        .deleteContextCategory(request);

        return response;
    }

    @Autowired
    private DSLContext dslContext;

    public List<ContextScheme> getContextSchemeByCategoryId(BigInteger ctxCategoryId) {
        return dslContext.select(
                CTX_SCHEME.CTX_SCHEME_ID,
                CTX_SCHEME.GUID,
                CTX_SCHEME.SCHEME_NAME,
                CTX_SCHEME.CTX_CATEGORY_ID,
                CTX_CATEGORY.NAME.as("ctx_category_name"),
                CTX_SCHEME.SCHEME_ID,
                CTX_SCHEME.SCHEME_AGENCY_ID,
                CTX_SCHEME.SCHEME_VERSION_ID,
                CTX_SCHEME.DESCRIPTION,
                CTX_SCHEME.LAST_UPDATE_TIMESTAMP)
                .from(CTX_SCHEME)
                .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.equal(CTX_CATEGORY.CTX_CATEGORY_ID))
                .where(CTX_SCHEME.CTX_CATEGORY_ID.eq(ULong.valueOf(ctxCategoryId)))
                .fetchStream().map(r -> {
                    ContextScheme contextScheme = new ContextScheme();
                    contextScheme.setContextSchemeId(r.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger());
                    contextScheme.setGuid(r.get(CTX_SCHEME.GUID));
                    contextScheme.setSchemeName(r.get(CTX_SCHEME.SCHEME_NAME));
                    contextScheme.setContextCategoryId(r.get(CTX_SCHEME.CTX_CATEGORY_ID).toBigInteger());
                    contextScheme.setContextCategoryName(r.get(CTX_CATEGORY.NAME.as("ctx_category_name")));
                    contextScheme.setSchemeId(r.get(CTX_SCHEME.SCHEME_ID));
                    contextScheme.setSchemeAgencyId(r.get(CTX_SCHEME.SCHEME_AGENCY_ID));
                    contextScheme.setSchemeVersionId(r.get(CTX_SCHEME.SCHEME_VERSION_ID));
                    contextScheme.setDescription(r.get(CTX_SCHEME.DESCRIPTION));
                    contextScheme.setLastUpdateTimestamp(
                            Date.from(r.get(CTX_SCHEME.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
                    return contextScheme;
                }).collect(Collectors.toList());
    }

}
