package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.model.CodeList;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CODE_LIST;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CODE_LIST_MANIFEST;

public class JooqCodeListReadRepository
        extends JooqScoreRepository
        implements CodeListReadRepository {

    public JooqCodeListReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public Map<BigInteger, CodeList> getCodeListMap(BigInteger ReleaseId) throws ScoreDataAccessException {
        List<CodeList> codeListRecords = dslContext()
                .select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        CODE_LIST.CODE_LIST_ID,
                        CODE_LIST.GUID,
                        CODE_LIST.NAME,
                        CODE_LIST.VERSION_ID,
                        CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        CODE_LIST.PREV_CODE_LIST_ID,
                        CODE_LIST.NEXT_CODE_LIST_ID)
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(ReleaseId)))
                .fetchInto(CodeList.class);

        return codeListRecords.stream()
                .collect(Collectors.toMap(CodeList::getCodeListId, Function.identity()));
    }
}
