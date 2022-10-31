package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

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
