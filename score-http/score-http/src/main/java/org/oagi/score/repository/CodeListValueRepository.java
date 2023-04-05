package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.CodeListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CODE_LIST_VALUE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CODE_LIST_VALUE_MANIFEST;

@Repository
public class CodeListValueRepository implements ScoreRepository<CodeListValue> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CodeListValue> findAll() {
        return dslContext.select(
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_VALUE.CODE_LIST_ID,
                        CODE_LIST_VALUE.DEFINITION, CODE_LIST_VALUE.DEFINITION_SOURCE,
                        CODE_LIST_VALUE.MEANING, CODE_LIST_VALUE.VALUE)
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST).on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .fetchInto(CodeListValue.class);
    }

    @Override
    public CodeListValue findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_VALUE.CODE_LIST_ID,
                        CODE_LIST_VALUE.DEFINITION, CODE_LIST_VALUE.DEFINITION_SOURCE,
                        CODE_LIST_VALUE.MEANING, CODE_LIST_VALUE.VALUE)
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST).on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CodeListValue.class);
    }

}
