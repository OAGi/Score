package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.ContextCategoryAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CtxCategoryRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CTX_CATEGORY;

public class DSLContextContextCategoryAPIImpl implements ContextCategoryAPI {

    private final DSLContext dslContext;

    public DSLContextContextCategoryAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ContextCategoryObject getContextCategoryById(BigInteger contextCategoryId) {
        CtxCategoryRecord ctxCategory = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategoryId)))
                .fetchOne();
        return mapper(ctxCategory);
    }

    @Override
    public ContextCategoryObject getContextCategoryByName(String contextCategoryName) {
        CtxCategoryRecord ctxCategory = dslContext.selectFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.NAME.eq(contextCategoryName))
                .fetchOne();
        return mapper(ctxCategory);
    }

    private ContextCategoryObject mapper(CtxCategoryRecord ctxCategory) {
        ContextCategoryObject contextCategory = new ContextCategoryObject();
        contextCategory.setContextCategoryId(ctxCategory.getCtxCategoryId().toBigInteger());
        contextCategory.setGuid(ctxCategory.getGuid());
        contextCategory.setName(ctxCategory.getName());
        contextCategory.setDescription(ctxCategory.getDescription());
        contextCategory.setCreatedBy(ctxCategory.getCreatedBy().toBigInteger());
        contextCategory.setLastUpdatedBy(ctxCategory.getLastUpdatedBy().toBigInteger());
        contextCategory.setCreationTimestamp(ctxCategory.getCreationTimestamp());
        contextCategory.setLastUpdateTimestamp(ctxCategory.getLastUpdateTimestamp());
        return contextCategory;
    }

    @Override
    public ContextCategoryObject createContextCategory(ContextCategoryObject contextCategory,
                                                       AppUserObject creator) {
        CtxCategoryRecord ctxCategory = new CtxCategoryRecord();
        ctxCategory.setGuid(contextCategory.getGuid());
        ctxCategory.setName(contextCategory.getName());
        ctxCategory.setDescription(contextCategory.getDescription());
        ctxCategory.setCreatedBy(ULong.valueOf(creator.getAppUserId()));
        ctxCategory.setLastUpdatedBy(ULong.valueOf(creator.getAppUserId()));
        ctxCategory.setCreationTimestamp(contextCategory.getCreationTimestamp());
        ctxCategory.setLastUpdateTimestamp(contextCategory.getLastUpdateTimestamp());
        ULong ctxCategoryId = dslContext.insertInto(CTX_CATEGORY)
                .set(ctxCategory)
                .returning(CTX_CATEGORY.CTX_CATEGORY_ID)
                .fetchOne().getCtxCategoryId();
        contextCategory.setContextCategoryId(ctxCategoryId.toBigInteger());
        return contextCategory;
    }

    @Override
    public ContextCategoryObject createRandomContextCategory(AppUserObject creator) {
        return createRandomContextCategory(creator, "cat");
    }

    @Override
    public ContextCategoryObject createRandomContextCategory(AppUserObject creator, String namePrefix) {
        ContextCategoryObject contextCategory = ContextCategoryObject.newRandomContextCategory(creator, namePrefix);
        return createContextCategory(contextCategory, creator);
    }

    @Override
    public void deleteContextCategoryByName(String categoryName) {
        dslContext.deleteFrom(CTX_CATEGORY).where(CTX_CATEGORY.NAME.eq(categoryName)).execute();
    }

    @Override
    public void deleteContextCategoryById(ULong categoryID) {
        dslContext.deleteFrom(CTX_CATEGORY).where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(categoryID)).execute();
    }
}
