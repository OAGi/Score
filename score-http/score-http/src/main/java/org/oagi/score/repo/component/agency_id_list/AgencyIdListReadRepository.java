package org.oagi.score.repo.component.agency_id_list;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.code_list_management.data.CodeListState;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListService;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
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
import static org.jooq.impl.DSL.trueCondition;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class AgencyIdListReadRepository {

    @Autowired
    private DSLContext dslContext;

    public List<AvailableAgencyIdList> availableAgencyIdListByBccpManifestId(BigInteger bccpManifestId) {
        BccpManifestRecord bccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOneInto(BccpManifestRecord.class);

        Result<Record2<ULong, ULong>> result = dslContext.selectDistinct(
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                BCCP_MANIFEST.RELEASE_ID)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(BDT_PRI_RESTRI).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BDT_PRI_RESTRI.BDT_MANIFEST_ID))
                .join(AGENCY_ID_LIST_MANIFEST).on(and(
                        BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID),
                        BCCP_MANIFEST.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID)
                ))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                    availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                            e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger(),
                            e.get(AGENCY_ID_LIST_MANIFEST.RELEASE_ID).toBigInteger()))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AvailableAgencyIdList::getAgencyIdListName))
                    .collect(Collectors.toList());

        } else {
            return availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                    null, bccpManifestRecord.getReleaseId().toBigInteger());
        }
    }

    private List<AvailableAgencyIdList> availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
            BigInteger agencyIdListManifestId, BigInteger releaseId) {
        if (agencyIdListManifestId == null) {
            return dslContext.select(
                    AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                    AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                    AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                    AGENCY_ID_LIST.VERSION_ID,
                    AGENCY_ID_LIST.IS_DEPRECATED,
                    AGENCY_ID_LIST.STATE,
                    AGENCY_ID_LIST.NAME.as("agency_id_list_name"))
                    .from(AGENCY_ID_LIST)
                    .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                    .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            AGENCY_ID_LIST.STATE.in(
                            Arrays.asList(CcState.Published.name(), CcState.Production.name()))
            )).fetchInto(AvailableAgencyIdList.class);
        }

        List<AvailableAgencyIdList> availableAgencyIdLists = dslContext.select(
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.VERSION_ID,
                AGENCY_ID_LIST.IS_DEPRECATED,
                AGENCY_ID_LIST.STATE,
                AGENCY_ID_LIST.NAME.as("code_list_name"))
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdListManifestId)))
                .fetchInto(AvailableAgencyIdList.class);

        List<BigInteger> associatedAgencyIdLists = dslContext.selectDistinct(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.in(
                        availableAgencyIdLists.stream()
                                .filter(e -> e.getAgencyIdListManifestId() != null)
                                .map(e -> e.getAgencyIdListManifestId())
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetchInto(BigInteger.class);
        
        List<AvailableAgencyIdList> mergedAgencyIdLists = new ArrayList();
        mergedAgencyIdLists.addAll(availableAgencyIdLists);
        for (BigInteger associatedAgencyId : associatedAgencyIdLists) {
            mergedAgencyIdLists.addAll(
                    availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                            associatedAgencyId, releaseId)
            );
        }
        return mergedAgencyIdLists.stream().distinct().collect(Collectors.toList());
    }

    public List<AvailableAgencyIdList> availableAgencyIdListByBdtScManifestId(BigInteger bdtScManifestId) {
        DtScManifestRecord dtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bdtScManifestId)))
                .fetchOneInto(DtScManifestRecord.class);

        Result<Record2<ULong, ULong>> result = dslContext.selectDistinct(
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                DT_SC_MANIFEST.RELEASE_ID)
                .from(DT_SC_MANIFEST)
                .join(BDT_SC_PRI_RESTRI).on(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID))
                .join(AGENCY_ID_LIST_MANIFEST).on(and(
                        BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID),
                        DT_SC_MANIFEST.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID)
                ))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bdtScManifestId)))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                        e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger(),
                        e.get(AGENCY_ID_LIST_MANIFEST.RELEASE_ID).toBigInteger()))
                .flatMap(e -> e.stream())
                .distinct()
                .sorted(Comparator.comparing(AvailableAgencyIdList::getAgencyIdListName))
                .collect(Collectors.toList());
        } else {
            return availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                    null, dtScManifestRecord.getReleaseId().toBigInteger());
        }
    }
}
