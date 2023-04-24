package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.AgencyIDListValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueRecord;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AgencyIDListValueObject;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.AGENCY_ID_LIST_VALUE;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.AGENCY_ID_LIST_VALUE_MANIFEST;

public class DSLContextAgencyIDListValueAPIImpl implements AgencyIDListValueAPI {

    private final DSLContext dslContext;

    public DSLContextAgencyIDListValueAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
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
    public ArrayList<AgencyIDListValueObject> getAgencyIDListValueByAgencyListID(AgencyIDListObject agencyIdList) {
        ArrayList<AgencyIDListValueObject> values = new ArrayList<>();

        Result agencyIdListValueResult = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.OWNER_LIST_ID.eq(ULong.valueOf(agencyIdList.getAgencyIDListId())))
                .fetch();
        for (int i=0; i<agencyIdListValueResult.size(); i++){
            AgencyIDListValueObject value = new AgencyIDListValueObject();
            Record agencyIDValueRecord = (Record) agencyIdListValueResult.get(i);
            ULong agencyIDListValueId = agencyIDValueRecord.get(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID);
            AgencyIdListValueManifestRecord agencyIdListValueManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(agencyIDListValueId))
                    .fetchOne();
            value = mapper(agencyIdListValueManifestRecord, (AgencyIdListValueRecord) agencyIDValueRecord);
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
