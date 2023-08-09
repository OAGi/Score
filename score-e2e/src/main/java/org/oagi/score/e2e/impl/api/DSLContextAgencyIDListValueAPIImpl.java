package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.AgencyIDListValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueRecord;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AgencyIDListValueObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextAgencyIDListValueAPIImpl implements AgencyIDListValueAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextAgencyIDListValueAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public AgencyIDListValueObject createRandomAgencyIDListValue(AppUserObject creator, AgencyIDListObject agencyIDList) {
        AgencyIDListValueObject agencyIDListValue = AgencyIDListValueObject.createRandomAgencyIDListValue(creator);

        AgencyIdListValueRecord agencyIdListValueRecord = new AgencyIdListValueRecord();
        agencyIdListValueRecord.setGuid(agencyIDListValue.getGuid());
        agencyIdListValueRecord.setValue(agencyIDListValue.getValue());
        agencyIdListValueRecord.setName(agencyIDListValue.getName());
        agencyIdListValueRecord.setDefinition(agencyIDListValue.getDefinition());
        agencyIdListValueRecord.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        agencyIdListValueRecord.setOwnerListId(ULong.valueOf(agencyIDList.getAgencyIDListId()));
        agencyIdListValueRecord.setIsDeprecated((byte) 0);
        agencyIdListValueRecord.setOwnerUserId(ULong.valueOf(agencyIDList.getOwnerUserId()));
        agencyIdListValueRecord.setCreatedBy(ULong.valueOf(agencyIDList.getCreatedBy()));
        agencyIdListValueRecord.setLastUpdatedBy(ULong.valueOf(agencyIDList.getLastUpdatedBy()));
        agencyIdListValueRecord.setCreationTimestamp(agencyIDList.getCreationTimestamp());
        agencyIdListValueRecord.setLastUpdateTimestamp(agencyIDList.getLastUpdateTimestamp());
        ULong agencyIdListValueId = dslContext.insertInto(AGENCY_ID_LIST_VALUE)
                .set(agencyIdListValueRecord)
                .returning(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID)
                .fetchOne().getAgencyIdListValueId();

        AgencyIdListValueManifestRecord agencyIdListValueManifestRecord = new AgencyIdListValueManifestRecord();
        agencyIdListValueManifestRecord.setReleaseId(ULong.valueOf(agencyIDList.getReleaseId()));
        agencyIdListValueManifestRecord.setAgencyIdListValueId(agencyIdListValueId);
        agencyIdListValueManifestRecord.setAgencyIdListManifestId(ULong.valueOf(agencyIDList.getAgencyIDListManifestId()));

        ULong agencyIdListValueManifestId = dslContext.insertInto(AGENCY_ID_LIST_VALUE_MANIFEST)
                .set(agencyIdListValueManifestRecord)
                .returning(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .fetchOne().getAgencyIdListValueManifestId();

        agencyIDListValue.setReleaseId(agencyIDList.getReleaseId());
        agencyIDListValue.setAgencyIDListValueId(agencyIdListValueId.toBigInteger());
        agencyIDListValue.setAgencyIDListValueManifestId(agencyIdListValueManifestId.toBigInteger());

        ReleaseObject release = apiFactory.getReleaseAPI().getReleaseById(agencyIDList.getReleaseId());

        if ("Working".equals(release.getReleaseNumber()) && "Published".equals(agencyIDList.getState())) {
            agencyIdListValueManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(agencyIdListValueManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease();
            AgencyIdListValueManifestRecord prevAgencyIdListValueManifestRecord = agencyIdListValueManifestRecord.copy();
            prevAgencyIdListValueManifestRecord.setAgencyIdListValueManifestId(null);
            prevAgencyIdListValueManifestRecord.setAgencyIdListManifestId(dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .where(and(
                            AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId())),
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(agencyIDList.getAgencyIDListId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevAgencyIdListValueManifestRecord.setAgencyIdListValueId(agencyIdListValueId);
            prevAgencyIdListValueManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevAgencyIdListValueManifestRecord.setNextAgencyIdListValueManifestId(agencyIdListValueManifestId);
            prevAgencyIdListValueManifestRecord.setAgencyIdListValueManifestId(
                    dslContext.insertInto(AGENCY_ID_LIST_VALUE_MANIFEST)
                            .set(prevAgencyIdListValueManifestRecord)
                            .returning(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                            .fetchOne().getAgencyIdListValueManifestId());

            dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .set(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID, prevAgencyIdListValueManifestRecord.getAgencyIdListValueManifestId())
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(agencyIdListValueManifestId))
                    .execute();
        }

        return agencyIDListValue;
    }

    @Override
    public AgencyIDListValueObject getAgencyIDListValueByManifestId(BigInteger agencyIDListValueManifestId) {
        AgencyIdListValueManifestRecord agencyIdListValueManifestRecord =
                dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(ULong.valueOf(agencyIDListValueManifestId)))
                        .fetchOne();
        AgencyIdListValueRecord agencyIdListValueRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValueManifestRecord.getAgencyIdListValueId()))
                .fetchOne();
        return mapper(agencyIdListValueManifestRecord, agencyIdListValueRecord);
    }

    @Override
    public List<AgencyIDListValueObject> getAgencyIDListValueByAgencyListID(AgencyIDListObject agencyIdList) {
        List<AgencyIDListValueObject> values = new ArrayList<>();

        Result<AgencyIdListValueManifestRecord> agencyIdListValueManifestResult = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdList.getAgencyIDListManifestId())))
                .fetch();
        Map<ULong, AgencyIdListValueRecord> agencyIdListValueRecordMap = (agencyIdListValueManifestResult.isNotEmpty()) ? dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.in(agencyIdListValueManifestResult.stream().map(e -> e.getAgencyIdListValueId()).collect(Collectors.toList())))
                .fetchStream().collect(Collectors.toMap(AgencyIdListValueRecord::getAgencyIdListValueId, Function.identity())) : Collections.emptyMap();

        for (int i = 0; i < agencyIdListValueManifestResult.size(); i++) {
            AgencyIdListValueManifestRecord agencyIDValueManifestRecord = agencyIdListValueManifestResult.get(i);
            AgencyIdListValueRecord agencyIDValueRecord = agencyIdListValueRecordMap.get(agencyIDValueManifestRecord.getAgencyIdListValueId());
            AgencyIDListValueObject value = mapper(agencyIDValueManifestRecord, agencyIDValueRecord);
            values.add(value);
        }
        return values;
    }

    private AgencyIDListValueObject mapper(AgencyIdListValueManifestRecord agencyIdListValueManifestRecord,
                                           AgencyIdListValueRecord agencyIdListValueRecord) {
        AgencyIDListValueObject agencyIDListValue = new AgencyIDListValueObject();
        agencyIDListValue.setAgencyIDListValueManifestId(agencyIdListValueManifestRecord.getAgencyIdListValueManifestId().toBigInteger());
        agencyIDListValue.setAgencyIDListValueId(agencyIdListValueManifestRecord.getAgencyIdListValueId().toBigInteger());
        if (agencyIdListValueManifestRecord.getBasedAgencyIdListValueManifestId() != null) {
            agencyIDListValue.setBasedAgencyIDListValueManifestId(agencyIdListValueManifestRecord.getBasedAgencyIdListValueManifestId().toBigInteger());
        }
        agencyIDListValue.setReleaseId(agencyIdListValueManifestRecord.getReleaseId().toBigInteger());
        agencyIDListValue.setGuid(agencyIdListValueRecord.getGuid());
        agencyIDListValue.setValue(agencyIdListValueRecord.getValue());
        agencyIDListValue.setName(agencyIdListValueRecord.getName());
        agencyIDListValue.setDefinition(agencyIdListValueRecord.getDefinition());
        agencyIDListValue.setDefinitionSource(agencyIdListValueRecord.getDefinitionSource());
        agencyIDListValue.setOwnerListId(agencyIdListValueRecord.getOwnerListId().toBigInteger());
        agencyIDListValue.setDeprecated(agencyIdListValueRecord.getIsDeprecated() == 1);
        agencyIDListValue.setOwnerUserId(agencyIdListValueRecord.getOwnerUserId().toBigInteger());
        agencyIDListValue.setCreatedBy(agencyIdListValueRecord.getCreatedBy().toBigInteger());
        agencyIDListValue.setLastUpdatedBy(agencyIdListValueRecord.getLastUpdatedBy().toBigInteger());
        agencyIDListValue.setCreationTimestamp(agencyIdListValueRecord.getCreationTimestamp());
        agencyIDListValue.setLastUpdateTimestamp(agencyIdListValueRecord.getLastUpdateTimestamp());
        return agencyIDListValue;
    }
}
