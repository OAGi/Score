package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.CodeList;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CodeListRepository implements ScoreRepository<CodeList> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        List<Field> fields = new ArrayList();
        fields.add(Tables.CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
        fields.addAll(Arrays.asList(Tables.CODE_LIST.fields()));
        return dslContext.select(fields)
                .from(Tables.CODE_LIST)
                .join(Tables.CODE_LIST_MANIFEST).on(Tables.CODE_LIST.CODE_LIST_ID.eq(Tables.CODE_LIST_MANIFEST.CODE_LIST_ID));
    }

    @Override
    public List<CodeList> findAll() {
        return getSelectOnConditionStep()
                .fetchInto(CodeList.class);
    }

    @Override
    public List<CodeList> findAllByReleaseIds(Collection<BigInteger> releaseIds) {
        if (releaseIds == null || releaseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSelectOnConditionStep()
                .where(
                        (releaseIds.size() == 1) ?
                                Tables.CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseIds.iterator().next())) :
                                Tables.CODE_LIST_MANIFEST.RELEASE_ID.in(releaseIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(CodeList.class);
    }

    @Override
    public CodeList findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.CODE_LIST.fields())
                .from(Tables.CODE_LIST).where(Tables.CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CodeList.class);
    }

}
