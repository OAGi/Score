package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.AgencyIDListAPI;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CODE_LIST;

public class DSLContextAgencyIDListAPIImpl implements AgencyIDListAPI {

    private final DSLContext dslContext;

    public DSLContextAgencyIDListAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public AgencyIDListObject getNewlyCreatedAgencyIDList(AppUserObject user, String release) {
        ULong latestAgencyIDListIDByUserInRelease = dslContext.select(DSL.max(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(AGENCY_ID_LIST.OWNER_USER_ID.eq(ULong.valueOf(user.getAppUserId())),
                        RELEASE.RELEASE_NUM.eq(release)))
                .fetchOneInto(ULong.class);
        List<Field<?>> fields = new ArrayList();
        fields.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(AGENCY_ID_LIST.fields()));
        return dslContext.select(fields)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(latestAgencyIDListIDByUserInRelease))
                .fetchOne(record -> agencyIDListListMapper(record));
    }

    private AgencyIDListObject agencyIDListListMapper(Record record) {
        AgencyIDListObject agencyIDList = new AgencyIDListObject();
        agencyIDList.setAgencyIDListId(record.get(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).toBigInteger());
        agencyIDList.setAgencyIDListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
        agencyIDList.setBasedAgencyIDListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID) != null ?
                record.get(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger() : null);
        agencyIDList.setReleaseId(record.get(AGENCY_ID_LIST_MANIFEST.RELEASE_ID).toBigInteger());
        agencyIDList.setGuid(record.get(AGENCY_ID_LIST.GUID));
        agencyIDList.setEnumTypeGuid(record.get(AGENCY_ID_LIST.ENUM_TYPE_GUID));
        agencyIDList.setName(record.get(AGENCY_ID_LIST.NAME));
        agencyIDList.setListId(record.get(AGENCY_ID_LIST.LIST_ID));
        agencyIDList.setVersionId(record.get(AGENCY_ID_LIST.VERSION_ID));
        agencyIDList.setDefinition(record.get(AGENCY_ID_LIST.DEFINITION));
        agencyIDList.setDefinitionSource(record.get(AGENCY_ID_LIST.DEFINITION_SOURCE));
        agencyIDList.setRemark(record.get(AGENCY_ID_LIST.REMARK));
        agencyIDList.setNamespaceId(record.get(AGENCY_ID_LIST.NAMESPACE_ID) != null ?
                record.get(AGENCY_ID_LIST.NAMESPACE_ID).toBigInteger() : null);
        agencyIDList.setDeprecated(record.get(AGENCY_ID_LIST.IS_DEPRECATED) == 1);
        agencyIDList.setState(record.get(AGENCY_ID_LIST.STATE));
        agencyIDList.setOwnerUserId(record.get(AGENCY_ID_LIST.OWNER_USER_ID).toBigInteger());
        agencyIDList.setCreatedBy(record.get(AGENCY_ID_LIST.CREATED_BY).toBigInteger());
        agencyIDList.setLastUpdatedBy(record.get(AGENCY_ID_LIST.LAST_UPDATED_BY).toBigInteger());
        agencyIDList.setCreationTimestamp(record.get(AGENCY_ID_LIST.CREATION_TIMESTAMP));
        agencyIDList.setLastUpdateTimestamp(record.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP));
        return agencyIDList;
    }
}
