package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.ContextScheme;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtxSchemeRepository implements SrtRepository<ContextScheme> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<ContextScheme> findAll() {
        return dslContext.select(Tables.CTX_SCHEME.fields()).from(Tables.CTX_SCHEME).fetchInto(ContextScheme.class);
    }

    @Override
    public ContextScheme findById(long id) {
        return dslContext.select(Tables.CTX_SCHEME.fields())
                .from(Tables.CTX_SCHEME).where(Tables.CTX_SCHEME.CTX_SCHEME_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ContextScheme.class);
    }

}
