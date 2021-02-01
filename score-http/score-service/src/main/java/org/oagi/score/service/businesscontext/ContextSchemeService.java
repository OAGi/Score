package org.oagi.score.service.businesscontext;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.trueCondition;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CTX_SCHEME;

@Service
@Transactional(readOnly = true)
public class ContextSchemeService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private DSLContext dslContext;

    public GetContextSchemeListResponse getContextSchemeList(GetContextSchemeListRequest request) {
        GetContextSchemeListResponse response =
                scoreRepositoryFactory.createContextSchemeReadRepository()
                        .getContextSchemeList(request);

        return response;
    }

    public GetContextSchemeValueListResponse getContextSchemeValueList(GetContextSchemeValueListRequest request) {
        GetContextSchemeValueListResponse response =
                scoreRepositoryFactory.createContextSchemeReadRepository()
                        .getContextSchemeValueList(request);

        return response;
    }

    public GetContextSchemeResponse getContextScheme(GetContextSchemeRequest request) {
        GetContextSchemeResponse response =
                scoreRepositoryFactory.createContextSchemeReadRepository()
                        .getContextScheme(request);

        return response;
    }

    @Transactional
    public CreateContextSchemeResponse createContextScheme(CreateContextSchemeRequest request) {
        CreateContextSchemeResponse response =
                scoreRepositoryFactory.createContextSchemeWriteRepository()
                        .createContextScheme(request);

        return response;
    }

    @Transactional
    public UpdateContextSchemeResponse updateContextScheme(UpdateContextSchemeRequest request) {
        UpdateContextSchemeResponse response =
                scoreRepositoryFactory.createContextSchemeWriteRepository()
                        .updateContextScheme(request);

        return response;
    }

    @Transactional
    public DeleteContextSchemeResponse deleteContextScheme(DeleteContextSchemeRequest request) {
        DeleteContextSchemeResponse response =
                scoreRepositoryFactory.createContextSchemeWriteRepository()
                        .deleteContextScheme(request);

        return response;
    }

    public boolean hasSameCtxScheme(ContextScheme contextScheme) {
        Condition idMatch = trueCondition();
        if (contextScheme.getContextSchemeId() != null) {
            idMatch = CTX_SCHEME.CTX_SCHEME_ID.notEqual(ULong.valueOf(contextScheme.getContextSchemeId()));
        }

        return dslContext.selectCount().from(CTX_SCHEME).where(
                and(CTX_SCHEME.SCHEME_ID.eq(contextScheme.getSchemeId()),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(contextScheme.getSchemeAgencyId()),
                        CTX_SCHEME.SCHEME_VERSION_ID.eq(contextScheme.getSchemeVersionId()),
                        idMatch))
                .fetchOneInto(Integer.class) > 0;
    }

    public boolean hasSameCtxSchemeName(ContextScheme contextScheme) {
        Condition idMatch = trueCondition();
        if (contextScheme.getContextSchemeId() != null) {
            idMatch = CTX_SCHEME.CTX_SCHEME_ID.notEqual(ULong.valueOf(contextScheme.getContextSchemeId()));
        }
        return dslContext.selectCount().from(CTX_SCHEME).where(
                and(CTX_SCHEME.SCHEME_ID.eq(contextScheme.getSchemeId()),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(contextScheme.getSchemeAgencyId()),
                        CTX_SCHEME.SCHEME_VERSION_ID.notEqual(contextScheme.getSchemeVersionId()),
                        CTX_SCHEME.SCHEME_NAME.notEqual(contextScheme.getSchemeName()),
                        idMatch))
                .fetchOneInto(Integer.class) > 0;
    }
}
