package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.CodeListValue;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CodeListValueRepository implements SrtRepository<CodeListValue> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CodeListValue> findAll() {
        return dslContext.select(Tables.CODE_LIST_VALUE.CODE_LIST_ID, Tables.CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                Tables.CODE_LIST_VALUE.LOCKED_INDICATOR, Tables.CODE_LIST_VALUE.DEFINITION,
                Tables.CODE_LIST_VALUE.DEFINITION_SOURCE, Tables.CODE_LIST_VALUE.EXTENSION_INDICATOR,
                Tables.CODE_LIST_VALUE.NAME, Tables.CODE_LIST_VALUE.USED_INDICATOR, Tables.CODE_LIST_VALUE.VALUE)
                .from(Tables.CODE_LIST_VALUE).fetchInto(CodeListValue.class);
    }

    @Override
    public CodeListValue findById(long id) {
        return dslContext.select(Tables.CODE_LIST_VALUE.CODE_LIST_ID, Tables.CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                Tables.CODE_LIST_VALUE.LOCKED_INDICATOR, Tables.CODE_LIST_VALUE.DEFINITION,
                Tables.CODE_LIST_VALUE.DEFINITION_SOURCE, Tables.CODE_LIST_VALUE.EXTENSION_INDICATOR,
                Tables.CODE_LIST_VALUE.NAME, Tables.CODE_LIST_VALUE.USED_INDICATOR, Tables.CODE_LIST_VALUE.VALUE)
                .from(Tables.CODE_LIST_VALUE).where(Tables.CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CodeListValue.class);
    }

}
