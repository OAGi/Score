package org.oagi.score.repo.component.code_list;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.code_list_management.data.CodeListState;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScManifestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.trueCondition;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class CodeListReadRepository {

    @Autowired
    private DSLContext dslContext;

    public CodeListManifestRecord getCodeListManifestByManifestId(BigInteger codeListManifestId) {
        return dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)))
                .fetchOne();
    }

    public List<AvailableCodeList> availableCodeListByBccpManifestId(BigInteger bccpManifestId, List<CodeListState> states) {
        BccpManifestRecord bccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOneInto(BccpManifestRecord.class);

        Result<Record2<ULong, ULong>> result = dslContext.selectDistinct(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                BCCP_MANIFEST.RELEASE_ID)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(BDT_PRI_RESTRI).on(DT_MANIFEST.DT_ID.eq(BDT_PRI_RESTRI.BDT_ID))
                .join(CODE_LIST_MANIFEST).on(and(
                        BDT_PRI_RESTRI.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID),
                        BCCP_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestRecord.getBccpManifestId()))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                    availableCodeListByCodeListManifestId(
                            e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger(), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AvailableCodeList::getCodeListName))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByReleaseId(bccpManifestRecord.getReleaseId().toBigInteger(), states);
        }
    }

    private List<AvailableCodeList> availableCodeListByCodeListManifestId(BigInteger codeListManifestId, List<CodeListState> states) {
        if (codeListManifestId == null) {
            return Collections.emptyList();
        }

        List<AvailableCodeList> availableCodeLists =
                dslContext.select(
                    CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                    CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                    CODE_LIST.CODE_LIST_ID,
                    CODE_LIST.NAME.as("code_list_name"),
                    CODE_LIST.STATE)
                    .from(CODE_LIST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(and(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .fetchInto(AvailableCodeList.class);

        List<BigInteger> associatedCodeLists = dslContext.selectDistinct(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(
                        availableCodeLists.stream()
                                .filter(e -> e.getBasedCodeListManifestId() != null)
                                .map(e -> e.getBasedCodeListManifestId())
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetchInto(BigInteger.class);

        List<AvailableCodeList> mergedCodeLists = new ArrayList();
        mergedCodeLists.addAll(availableCodeLists);
        for (BigInteger associatedCodeListId : associatedCodeLists) {
            mergedCodeLists.addAll(
                    availableCodeListByCodeListManifestId(
                            associatedCodeListId, states)
            );
        }

        // #1094: Add Code list which is base availableCodeLists
        List<AvailableCodeList> baseCodeLists =
                dslContext.select(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                        CODE_LIST.CODE_LIST_ID,
                        CODE_LIST.NAME.as("code_list_name"),
                        CODE_LIST.STATE)
                        .from(CODE_LIST)
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                        .where(and(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)),
                                states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                        .fetchInto(AvailableCodeList.class);

        mergedCodeLists.addAll(baseCodeLists);
        return mergedCodeLists.stream().distinct().collect(Collectors.toList());
    }

    private List<AvailableCodeList> availableCodeListByReleaseId(BigInteger releaseId, List<CodeListState> states) {

        List<Condition> conditions = new ArrayList();

        conditions.add(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)));
        if (!states.isEmpty()) {
            conditions.add(CODE_LIST.STATE.in(states));
        }

        return dslContext.select(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                CODE_LIST.CODE_LIST_ID,
                CODE_LIST.NAME.as("code_list_name"),
                CODE_LIST.STATE)
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(conditions)
                .fetchInto(AvailableCodeList.class);
    }

    public List<AvailableCodeList> availableCodeListByBdtScManifestId(BigInteger bdtScManifestId, List<CodeListState> states) {
        DtScManifestRecord dtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bdtScManifestId)))
                .fetchOneInto(DtScManifestRecord.class);

        Result<Record2<ULong, ULong>> result = dslContext.selectDistinct(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                DT_SC_MANIFEST.RELEASE_ID)
                .from(DT_SC_MANIFEST)
                .join(BDT_SC_PRI_RESTRI).on(DT_SC_MANIFEST.DT_SC_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_ID))
                .join(CODE_LIST_MANIFEST).on(and(
                        BDT_SC_PRI_RESTRI.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID),
                        DT_SC_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId()))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                    availableCodeListByCodeListManifestId(
                            e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger(), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AvailableCodeList::getCodeListName))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByReleaseId(dtScManifestRecord.getReleaseId().toBigInteger(), states);
        }
    }
}
