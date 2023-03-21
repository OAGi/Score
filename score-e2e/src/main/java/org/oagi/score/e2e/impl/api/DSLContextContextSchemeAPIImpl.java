package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.ContextSchemeAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CtxSchemeRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CTX_SCHEME;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CTX_SCHEME_VALUE;

public class DSLContextContextSchemeAPIImpl implements ContextSchemeAPI {

    private final DSLContext dslContext;

    public DSLContextContextSchemeAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ContextSchemeObject getContextSchemeById(BigInteger contextSchemeId) {
        CtxSchemeRecord ctxScheme = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(contextSchemeId)))
                .fetchOne();
        return mapper(ctxScheme);
    }

    @Override
    public ContextSchemeObject getContextSchemeByName(String contextSchemeName) {
        CtxSchemeRecord ctxScheme = dslContext.selectFrom(CTX_SCHEME)
                .where(CTX_SCHEME.SCHEME_NAME.eq(contextSchemeName))
                .fetchOne();
        return mapper(ctxScheme);
    }

    private ContextSchemeObject mapper(CtxSchemeRecord ctxScheme) {
        ContextSchemeObject contextScheme = new ContextSchemeObject();
        contextScheme.setContextSchemeId(ctxScheme.getCtxSchemeId().toBigInteger());
        contextScheme.setGuid(ctxScheme.getGuid());
        contextScheme.setSchemeId(ctxScheme.getSchemeId());
        contextScheme.setSchemeName(ctxScheme.getSchemeName());
        contextScheme.setDescription(ctxScheme.getDescription());
        contextScheme.setSchemeAgencyId(ctxScheme.getSchemeAgencyId());
        contextScheme.setSchemeVersionId(ctxScheme.getSchemeVersionId());
        contextScheme.setContextCategoryId(ctxScheme.getCtxCategoryId().toBigInteger());
        if (ctxScheme.getCodeListId() != null) {
            contextScheme.setCodeListId(ctxScheme.getCodeListId().toBigInteger());
        }
        contextScheme.setCreatedBy(ctxScheme.getCreatedBy().toBigInteger());
        contextScheme.setLastUpdatedBy(ctxScheme.getLastUpdatedBy().toBigInteger());
        contextScheme.setCreationTimestamp(ctxScheme.getCreationTimestamp());
        contextScheme.setLastUpdateTimestamp(ctxScheme.getLastUpdateTimestamp());
        return contextScheme;
    }

    @Override
    public ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                         AppUserObject creator) {
        return createRandomContextScheme(contextCategory, creator, "cs");
    }

    @Override
    public ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                         AppUserObject creator, String namePrefix) {
        ContextSchemeObject contextScheme =
                ContextSchemeObject.createRandomContextScheme(contextCategory, creator, namePrefix);

        CtxSchemeRecord ctxScheme = new CtxSchemeRecord();
        ctxScheme.setGuid(contextScheme.getGuid());
        ctxScheme.setSchemeId(contextScheme.getSchemeId());
        ctxScheme.setSchemeName(contextScheme.getSchemeName());
        ctxScheme.setDescription(contextScheme.getDescription());
        ctxScheme.setSchemeAgencyId(contextScheme.getSchemeAgencyId());
        ctxScheme.setSchemeVersionId(contextScheme.getSchemeVersionId());
        ctxScheme.setCtxCategoryId(ULong.valueOf(contextScheme.getContextCategoryId()));
        if (contextScheme.getCodeListId() != null) {
            ctxScheme.setCodeListId(ULong.valueOf(contextScheme.getCodeListId()));
        }
        ctxScheme.setCreatedBy(ULong.valueOf(contextScheme.getCreatedBy()));
        ctxScheme.setLastUpdatedBy(ULong.valueOf(contextScheme.getLastUpdatedBy()));
        ctxScheme.setCreationTimestamp(contextScheme.getCreationTimestamp());
        ctxScheme.setLastUpdateTimestamp(contextScheme.getLastUpdateTimestamp());
        ULong ctxSchemeId = dslContext.insertInto(CTX_SCHEME)
                .set(ctxScheme)
                .returning(CTX_SCHEME.CTX_SCHEME_ID)
                .fetchOne().getCtxSchemeId();
        contextScheme.setContextSchemeId(ctxSchemeId.toBigInteger());
        return contextScheme;
    }

    @Override
    public void deleteContextSchemaById(BigInteger contextSchemeId) {
        ULong ctxSchemeIdLong = ULong.valueOf(contextSchemeId);
        dslContext.deleteFrom(CTX_SCHEME_VALUE).where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ctxSchemeIdLong)).execute();
        dslContext.deleteFrom(CTX_SCHEME).where(CTX_SCHEME.CTX_SCHEME_ID.eq(ctxSchemeIdLong)).execute();
    }
}
