package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.AgencyIDListValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListValueRecord;
import org.oagi.score.e2e.obj.AgencyIDListValueObject;

import java.math.BigInteger;

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
