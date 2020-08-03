package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.ContextCategory;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtxCategoryRepository implements SrtRepository<ContextCategory> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<ContextCategory> findAll() {
        return dslContext.select(Tables.CTX_CATEGORY.fields())
                .from(Tables.CTX_CATEGORY).fetchInto(ContextCategory.class);
    }

    @Override
    public ContextCategory findById(long id) {
        return dslContext.select(Tables.CTX_CATEGORY.fields())
                .from(Tables.CTX_CATEGORY).where(Tables.CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ContextCategory.class);
    }

}
