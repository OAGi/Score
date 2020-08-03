package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.CodeList;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CodeListRepository implements SrtRepository<CodeList> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CodeList> findAll() {
        return dslContext.select(Tables.CODE_LIST.fields())
                .from(Tables.CODE_LIST).fetchInto(CodeList.class);
    }

    @Override
    public CodeList findById(long id) {
        return dslContext.select(Tables.CODE_LIST.fields())
                .from(Tables.CODE_LIST).where(Tables.CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CodeList.class);
    }

}
