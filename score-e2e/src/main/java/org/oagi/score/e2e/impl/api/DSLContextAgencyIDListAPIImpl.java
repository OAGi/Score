package org.oagi.score.e2e.impl.api;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.AgencyIDListAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AgencyIdListRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.LogRecord;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.*;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextAgencyIDListAPIImpl implements AgencyIDListAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextAgencyIDListAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public AgencyIDListObject createRandomAgencyIDList(AppUserObject creator, NamespaceObject namespace,
                                                       ReleaseObject release, String state) {
        AgencyIDListObject agencyIDList = AgencyIDListObject.createRandomAgencyIDList(creator, namespace, state);

        AgencyIdListRecord agencyIdListRecord = new AgencyIdListRecord();
        agencyIdListRecord.setGuid(agencyIDList.getGuid());
        agencyIdListRecord.setEnumTypeGuid(agencyIDList.getEnumTypeGuid());
        agencyIdListRecord.setName(agencyIDList.getName());
        agencyIdListRecord.setListId(agencyIDList.getListId());
        agencyIdListRecord.setVersionId(agencyIDList.getVersionId());
        agencyIdListRecord.setDefinition(agencyIDList.getDefinition());
        agencyIdListRecord.setDefinitionSource(agencyIDList.getDefinitionSource());
        agencyIdListRecord.setRemark(agencyIDList.getRemark());
        agencyIdListRecord.setNamespaceId(ULong.valueOf(agencyIDList.getNamespaceId()));
        agencyIdListRecord.setOwnerUserId(ULong.valueOf(agencyIDList.getOwnerUserId()));
        agencyIdListRecord.setCreatedBy(ULong.valueOf(agencyIDList.getCreatedBy()));
        agencyIdListRecord.setLastUpdatedBy(ULong.valueOf(agencyIDList.getLastUpdatedBy()));
        agencyIdListRecord.setCreationTimestamp(agencyIDList.getCreationTimestamp());
        agencyIdListRecord.setLastUpdateTimestamp(agencyIDList.getLastUpdateTimestamp());
        agencyIdListRecord.setState(agencyIDList.getState());
        agencyIdListRecord.setIsDeprecated((byte) (agencyIDList.isDeprecated() ? 1 : 0));
        ULong agencyIdListId = dslContext.insertInto(AGENCY_ID_LIST)
                .set(agencyIdListRecord)
                .returning(AGENCY_ID_LIST.AGENCY_ID_LIST_ID)
                .fetchOne().getAgencyIdListId();

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(agencyIDList.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"agencyIdList\"}");
        dummyLogRecord.setCreatedBy(agencyIdListRecord.getCreatedBy());
        dummyLogRecord.setCreationTimestamp(agencyIdListRecord.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        AgencyIdListManifestRecord agencyIdListManifestRecord = new AgencyIdListManifestRecord();
        agencyIdListManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        agencyIdListManifestRecord.setAgencyIdListId(agencyIdListId);
        agencyIdListManifestRecord.setLogId(logId);

        ULong agencyIdListManifestId = dslContext.insertInto(AGENCY_ID_LIST_MANIFEST)
                .set(agencyIdListManifestRecord)
                .returning(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .fetchOne().getAgencyIdListManifestId();

        agencyIDList.setReleaseId(release.getReleaseId());
        agencyIDList.setAgencyIDListId(agencyIdListId.toBigInteger());
        agencyIDList.setAgencyIDListManifestId(agencyIdListManifestId.toBigInteger());

        if ("Working".equals(release.getReleaseNumber()) && "Published".equals(state)) {
            agencyIdListManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_MANIFEST)
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease();
            AgencyIdListManifestRecord prevAgencyIdListManifestRecord = agencyIdListManifestRecord.copy();
            prevAgencyIdListManifestRecord.setAgencyIdListManifestId(null);
            prevAgencyIdListManifestRecord.setAgencyIdListId(agencyIdListId);
            prevAgencyIdListManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevAgencyIdListManifestRecord.setNextAgencyIdListManifestId(agencyIdListManifestId);
            prevAgencyIdListManifestRecord.setAgencyIdListManifestId(
                    dslContext.insertInto(AGENCY_ID_LIST_MANIFEST)
                            .set(prevAgencyIdListManifestRecord)
                            .returning(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                            .fetchOne().getAgencyIdListManifestId());

            dslContext.update(AGENCY_ID_LIST_MANIFEST)
                    .set(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID, prevAgencyIdListManifestRecord.getAgencyIdListManifestId())
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListManifestId))
                    .execute();
        }

        return agencyIDList;
    }

    @Override
    public AgencyIDListObject getAgencyIDListByManifestId(BigInteger agencyIdListManifestId) {
        List<Field<?>> fields = new ArrayList();
        fields.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(AGENCY_ID_LIST.fields()));
        return dslContext.select(fields)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdListManifestId)))
                .fetchOne(record -> agencyIDListListMapper(record));
    }

    @Override
    public List<AgencyIDListObject> getAgencyIDListsByRelease(ReleaseObject release) {
        if (release == null) {
            return Collections.emptyList();
        }
        List<Field<?>> fields = new ArrayList();
        fields.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(AGENCY_ID_LIST.fields()));
        return dslContext.select(fields)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())))
                .fetch(record -> agencyIDListListMapper(record));
    }

    @Override
    public AgencyIDListObject getAgencyIDListByNameAndBranch(String name, String branch) {
        return getAgencyIDListByNameAndBranchAndState(name, branch, null);
    }

    @Override
    public AgencyIDListObject getAgencyIDListByNameAndBranchAndState(String name, String branch, String state) {
        List<Condition> conditions = new ArrayList<>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(AGENCY_ID_LIST.NAME.eq(name));
        }
        if (!StringUtils.isEmpty(branch)) {
            conditions.add(RELEASE.RELEASE_NUM.eq(branch));
        }
        if (!StringUtils.isEmpty(state)) {
            conditions.add(AGENCY_ID_LIST.STATE.eq(state));
        }

        List<Field<?>> fields = new ArrayList();
        fields.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID);
        fields.add(AGENCY_ID_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(AGENCY_ID_LIST.fields()));
        return dslContext.select(fields)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(and(conditions))
                .fetchOne(record -> agencyIDListListMapper(record));
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

    @Override
    public void updateAgencyIDList(AgencyIDListObject agencyIDList) {
        dslContext.update(AGENCY_ID_LIST)
                .set(AGENCY_ID_LIST.LIST_ID, agencyIDList.getListId())
                .set(AGENCY_ID_LIST.VERSION_ID, agencyIDList.getVersionId())
                .set(AGENCY_ID_LIST.IS_DEPRECATED, (byte) (agencyIDList.isDeprecated() ? 1 : 0))
                .set(AGENCY_ID_LIST.DEFINITION, agencyIDList.getDefinition())
                .set(AGENCY_ID_LIST.DEFINITION_SOURCE, agencyIDList.getDefinitionSource())
                .set(AGENCY_ID_LIST.CREATION_TIMESTAMP, agencyIDList.getCreationTimestamp())
                .set(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, agencyIDList.getLastUpdateTimestamp())
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(agencyIDList.getAgencyIDListId())))
                .execute();
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
