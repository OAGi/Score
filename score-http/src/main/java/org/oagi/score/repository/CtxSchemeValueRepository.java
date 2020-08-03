package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.ContextSchemeValue;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtxSchemeValueRepository implements SrtRepository<ContextSchemeValue> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<ContextSchemeValue> findAll() {
        return dslContext.select(Tables.CTX_SCHEME_VALUE.fields()).from(Tables.CTX_SCHEME_VALUE)
                .fetchInto(ContextSchemeValue.class);
    }

    @Override
    public ContextSchemeValue findById(long id) {
        return dslContext.select(Tables.CTX_SCHEME_VALUE.fields()).from(Tables.CTX_SCHEME_VALUE)
                .where(Tables.CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ContextSchemeValue.class);
    }
}
