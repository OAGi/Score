package org.oagi.score.repo.component.code_list;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScManifestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
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

    public List<AvailableCodeList> availableCodeListByBccpManifestId(BigInteger bccpManifestId) {
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
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestRecord.getBccpManifestId()))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                    availableCodeListByCodeListManifestIdOrReleaseId(
                            e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger(),
                            e.get(CODE_LIST_MANIFEST.RELEASE_ID).toBigInteger()))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AvailableCodeList::getCodeListName))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByCodeListManifestIdOrReleaseId(
                    null, bccpManifestRecord.getReleaseId().toBigInteger());
        }
    }

    private List<AvailableCodeList> availableCodeListByCodeListManifestIdOrReleaseId(
            BigInteger codeListManifestId, BigInteger releaseId) {
        if (codeListManifestId == null) {
            return dslContext.select(
                    CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                    CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                    CODE_LIST.CODE_LIST_ID,
                    CODE_LIST.NAME.as("code_list_name"))
                    .from(CODE_LIST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                    .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST.STATE.in(Arrays.asList(CcState.Published.name(), CcState.Production.name()))
            )).fetchInto(AvailableCodeList.class);
        }

        List<AvailableCodeList> availableCodeLists =
                dslContext.select(
                    CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                    CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                    CODE_LIST.CODE_LIST_ID,
                    CODE_LIST.NAME.as("code_list_name"))
                    .from(CODE_LIST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)))
                .fetchInto(AvailableCodeList.class);

        List<BigInteger> associatedCodeLists = dslContext.selectDistinct(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
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
                    availableCodeListByCodeListManifestIdOrReleaseId(
                            associatedCodeListId, releaseId)
            );
        }
        return mergedCodeLists.stream().distinct().collect(Collectors.toList());
    }

    public List<AvailableCodeList> availableCodeListByBdtScManifestId(BigInteger bdtScManifestId) {
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
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId()))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                    availableCodeListByCodeListManifestIdOrReleaseId(
                            e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger(),
                            e.get(CODE_LIST_MANIFEST.RELEASE_ID).toBigInteger()))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AvailableCodeList::getCodeListName))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByCodeListManifestIdOrReleaseId(
                    null, dtScManifestRecord.getReleaseId().toBigInteger());
        }
    }
}
