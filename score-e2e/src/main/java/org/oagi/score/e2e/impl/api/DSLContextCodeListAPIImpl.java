package org.oagi.score.e2e.impl.api;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.CodeListAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.LogRecord;
import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextCodeListAPIImpl implements CodeListAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextCodeListAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public CodeListObject getCodeListByManifestId(BigInteger codeListManifestId) {
        CodeListManifestRecord codeListManifestRecord = dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)))
                .fetchOne();
        CodeListRecord codeListRecord = dslContext.selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId()))
                .fetchOne();
        return mapper(codeListManifestRecord, codeListRecord);
    }

    @Override
    public CodeListObject getCodeListByCodeListNameAndReleaseNum(String codeListName, String releaseNum) {
        ULong codeListManifestId = dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        CODE_LIST.NAME.eq(codeListName),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchOneInto(ULong.class);
        return getCodeListByManifestId(codeListManifestId.toBigInteger());
    }

    private CodeListObject mapper(CodeListManifestRecord codeListManifestRecord, CodeListRecord codeListRecord) {
        CodeListObject codeList = new CodeListObject();
        codeList.setCodeListManifestId(codeListManifestRecord.getCodeListManifestId().toBigInteger());
        codeList.setCodeListId(codeListRecord.getCodeListId().toBigInteger());
        if (codeListManifestRecord.getBasedCodeListManifestId() != null) {
            codeList.setBasedCodeListManifestId(codeListManifestRecord.getBasedCodeListManifestId().toBigInteger());
        }
        codeList.setGuid(codeListRecord.getGuid());
        codeList.setName(codeListRecord.getName());
        codeList.setListId(codeListRecord.getListId());
        codeList.setVersionId(codeListRecord.getVersionId());
        codeList.setDefinition(codeListRecord.getDefinition());
        codeList.setDefinitionSource(codeListRecord.getDefinitionSource());
        codeList.setRemark(codeListRecord.getRemark());
        codeList.setNamespaceId(codeListRecord.getNamespaceId().toBigInteger());
        codeList.setExtensibleIndicator(codeListRecord.getExtensibleIndicator() == 1);
        codeList.setDeprecated(codeListRecord.getIsDeprecated() == 1);
        codeList.setState(codeListRecord.getState());
        codeList.setOwnerUserId(codeListRecord.getOwnerUserId().toBigInteger());
        codeList.setCreatedBy(codeListRecord.getCreatedBy().toBigInteger());
        codeList.setLastUpdatedBy(codeListRecord.getLastUpdatedBy().toBigInteger());
        codeList.setCreationTimestamp(codeListRecord.getCreationTimestamp());
        codeList.setLastUpdateTimestamp(codeListRecord.getLastUpdateTimestamp());
        return codeList;
    }

    @Override
    public CodeListObject createRandomCodeList(AppUserObject creator, NamespaceObject namespace,
                                               ReleaseObject release, String state) {
        CodeListObject codeList = CodeListObject.createRandomCodeList(creator, namespace, state);

        CodeListRecord codeListRecord = new CodeListRecord();
        codeListRecord.setGuid(codeList.getGuid());
        codeListRecord.setName(codeList.getName());
        codeListRecord.setListId(codeList.getListId());
        codeListRecord.setVersionId(codeList.getVersionId());
        codeListRecord.setDefinition(codeList.getDefinition());
        codeListRecord.setDefinitionSource(codeList.getDefinitionSource());
        codeListRecord.setRemark(codeList.getRemark());
        codeListRecord.setNamespaceId(ULong.valueOf(namespace.getNamespaceId()));
        codeListRecord.setExtensibleIndicator((byte) (codeList.isExtensibleIndicator() ? 1 : 0));
        codeListRecord.setIsDeprecated((byte) (codeList.isDeprecated() ? 1 : 0));
        codeListRecord.setState(codeList.getState());
        codeListRecord.setOwnerUserId(ULong.valueOf(codeList.getOwnerUserId()));
        codeListRecord.setCreatedBy(ULong.valueOf(codeList.getCreatedBy()));
        codeListRecord.setLastUpdatedBy(ULong.valueOf(codeList.getLastUpdatedBy()));
        codeListRecord.setCreationTimestamp(codeList.getCreationTimestamp());
        codeListRecord.setLastUpdateTimestamp(codeList.getLastUpdateTimestamp());

        ULong codeListId = dslContext.insertInto(CODE_LIST)
                .set(codeListRecord)
                .returning(CODE_LIST.CODE_LIST_ID)
                .fetchOne().getCodeListId();

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(codeListRecord.getGuid());
        dummyLogRecord.setSnapshot(JSON.valueOf("{\"component\": \"code_list\"}"));
        dummyLogRecord.setCreatedBy(codeListRecord.getCreatedBy());
        dummyLogRecord.setCreationTimestamp(codeListRecord.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        String agencyIdListValueName;
        if (creator.isDeveloper()) {
            agencyIdListValueName = "OAGi (Open Applications Group, Incorporated)";
        } else {
            agencyIdListValueName = "Mutually defined";
        }

        ULong agencyIdListValueManifestId =
                dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_VALUE)
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                AGENCY_ID_LIST_VALUE.NAME.eq(agencyIdListValueName)
                        ))
                        .fetchOneInto(ULong.class);

        CodeListManifestRecord codeListManifestRecord = new CodeListManifestRecord();
        codeListManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        codeListManifestRecord.setCodeListId(codeListId);
        codeListManifestRecord.setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        codeListManifestRecord.setLogId(logId);

        ULong codeListManifestId = dslContext.insertInto(CODE_LIST_MANIFEST)
                .set(codeListManifestRecord)
                .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .fetchOne().getCodeListManifestId();
        codeList.setCodeListManifestId(codeListManifestId.toBigInteger());
        codeList.setCodeListId(codeListId.toBigInteger());
        codeList.setAgencyIdListValueManifestId(agencyIdListValueManifestId.toBigInteger());
        codeList.setReleaseId(release.getReleaseId());
        return codeList;
    }

    @Override
    public CodeListObject createDerivedCodeList(CodeListObject baseCodeList,
                                                AppUserObject creator, NamespaceObject namespace,
                                                ReleaseObject release, String state) {
        CodeListObject codeList = CodeListObject.createDerivedCodeList(baseCodeList, creator, namespace, state);

        CodeListRecord codeListRecord = new CodeListRecord();
        codeListRecord.setBasedCodeListId(ULong.valueOf(baseCodeList.getCodeListId()));
        codeListRecord.setGuid(codeList.getGuid());
        codeListRecord.setName(codeList.getName());
        codeListRecord.setListId(codeList.getListId());
        codeListRecord.setVersionId(codeList.getVersionId());
        codeListRecord.setDefinition(codeList.getDefinition());
        codeListRecord.setDefinitionSource(codeList.getDefinitionSource());
        codeListRecord.setRemark(codeList.getRemark());
        codeListRecord.setNamespaceId(ULong.valueOf(namespace.getNamespaceId()));
        codeListRecord.setExtensibleIndicator((byte) (codeList.isExtensibleIndicator() ? 1 : 0));
        codeListRecord.setIsDeprecated((byte) (codeList.isDeprecated() ? 1 : 0));
        codeListRecord.setState(codeList.getState());
        codeListRecord.setOwnerUserId(ULong.valueOf(codeList.getOwnerUserId()));
        codeListRecord.setCreatedBy(ULong.valueOf(codeList.getCreatedBy()));
        codeListRecord.setLastUpdatedBy(ULong.valueOf(codeList.getLastUpdatedBy()));
        codeListRecord.setCreationTimestamp(codeList.getCreationTimestamp());
        codeListRecord.setLastUpdateTimestamp(codeList.getLastUpdateTimestamp());

        ULong codeListId = dslContext.insertInto(CODE_LIST)
                .set(codeListRecord)
                .returning(CODE_LIST.CODE_LIST_ID)
                .fetchOne().getCodeListId();

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(codeListRecord.getGuid());
        dummyLogRecord.setSnapshot(JSON.valueOf("{\"component\": \"code_list\"}"));
        dummyLogRecord.setCreatedBy(codeListRecord.getCreatedBy());
        dummyLogRecord.setCreationTimestamp(codeListRecord.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        String agencyIdListValueName;
        if (creator.isDeveloper()) {
            agencyIdListValueName = "OAGi (Open Applications Group, Incorporated)";
        } else {
            agencyIdListValueName = "Mutually defined";
        }

        ULong agencyIdListValueManifestId =
                dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_VALUE)
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                AGENCY_ID_LIST_VALUE.NAME.eq(agencyIdListValueName)
                        ))
                        .fetchOneInto(ULong.class);

        CodeListManifestRecord codeListManifestRecord = new CodeListManifestRecord();
        codeListManifestRecord.setBasedCodeListManifestId(ULong.valueOf(baseCodeList.getCodeListManifestId()));
        codeListManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        codeListManifestRecord.setCodeListId(codeListId);
        codeListManifestRecord.setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        codeListManifestRecord.setLogId(logId);

        ULong codeListManifestId = dslContext.insertInto(CODE_LIST_MANIFEST)
                .set(codeListManifestRecord)
                .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .fetchOne().getCodeListManifestId();

        codeList.setCodeListManifestId(codeListManifestId.toBigInteger());
        codeList.setCodeListId(codeListId.toBigInteger());
        codeList.setAgencyIdListValueManifestId(agencyIdListValueManifestId.toBigInteger());
        codeList.setReleaseId(release.getReleaseId());

        createDerivedCodeListValues(baseCodeList, codeList, creator);
        return codeList;
    }

    private void createDerivedCodeListValues(CodeListObject baseCodeList,
                                             CodeListObject codeList, AppUserObject creator) {
        List<CodeListValueObject> codeListValueList =
                this.apiFactory.getCodeListValueAPI().getCodeListValuesByCodeListManifestId(baseCodeList.getCodeListManifestId());

        for (CodeListValueObject codeListValue : codeListValueList) {
            this.apiFactory.getCodeListValueAPI().createDerivedCodeListValue(codeListValue, codeList, creator);
        }
    }

    private ULong getReleaseIdByReleaseNum(String releaseNum) {
        return dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq(releaseNum))
                .fetchOneInto(ULong.class);
    }

    @Override
    public Boolean doesCodeListExistInTheRelease(CodeListObject codeList, String release) {
        return dslContext.fetchExists(
                dslContext.selectFrom(CODE_LIST_MANIFEST).where(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(ULong.valueOf(codeList.getCodeListId())).
                        and(CODE_LIST_MANIFEST.RELEASE_ID.eq(getReleaseIdByReleaseNum(release)))));
    }

    @Override
    public void updateCodeList(CodeListObject codeListWIP) {
        dslContext.update(CODE_LIST)
                .set(CODE_LIST.IS_DEPRECATED, (byte) (codeListWIP.isDeprecated() ? 1 : 0))
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeListWIP.getCodeListId())))
                .execute();
    }

    @Override
    public String getModuleNameForCodeList(CodeListObject codeList, String releaseNumber) {
        return dslContext.select(MODULE.NAME)
                .from(MODULE)
                .join(MODULE_CODE_LIST_MANIFEST).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(CODE_LIST_MANIFEST).on(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeList.getCodeListId())).and
                        (RELEASE.RELEASE_NUM.eq(releaseNumber)))
                .fetchOneInto(String.class);
    }

    @Override
    public ArrayList<CodeListObject> getDefaultCodeListsForDT(String guid, BigInteger releaseId) {
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(CODE_LIST.fields()));
        List<Result<Record>> records = dslContext.select(fields)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(CODE_LIST_MANIFEST).on(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.isNotNull().and(DT.GUID.eq(guid).and(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))))
                .fetchMany();
        return codeListMapperList(records);
    }

    @Override
    public CodeListObject getCodeListByNameAndReleaseNum(String name, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(CODE_LIST.fields()));
        return dslContext.select(fields)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId),
                        CODE_LIST.NAME.eq(name)))
                .fetchOne(record -> codeListMapper(record));

    }

    @Override
    public boolean isListIdUnique(String listId) {
        List<Result<Record>> records = dslContext.select(CODE_LIST.CODE_LIST_ID)
                .from(CODE_LIST)
                .where(CODE_LIST.LIST_ID.eq(listId))
                .fetchMany();
        if (records.size() > 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> getOAGISOwnedLists(BigInteger releaseId) {
        return dslContext.select(AGENCY_ID_LIST.NAME)
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .join(APP_USER).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        APP_USER.LOGIN_ID.eq("oagis")
                ))
                .fetchInto(String.class);
    }

    @Override
    public boolean checkCodeListUniqueness(CodeListObject codeList, String agencyIDList) {
        List<Result<Record>> records = dslContext.select(CODE_LIST.CODE_LIST_ID)
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(and(CODE_LIST.LIST_ID.eq(codeList.getListId()),
                        (CODE_LIST.VERSION_ID.eq(codeList.getVersionId())),
                        (AGENCY_ID_LIST.NAME).eq(agencyIDList)))
                .fetchMany();
        if (records.size() > 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public CodeListObject createRevisionOfACodeListAndPublishInAnotherRelease(CodeListObject codeListToBeRevised, ReleaseObject release, AppUserObject creator, int revisionNumber) {
        /**
         * Create new revision
         */
        CodeListRecord codeListRecord = new CodeListRecord();
        codeListRecord.setGuid(codeListToBeRevised.getGuid());
        codeListRecord.setName(codeListToBeRevised.getName());
        codeListRecord.setListId(codeListToBeRevised.getListId());
        codeListRecord.setVersionId(codeListToBeRevised.getVersionId() + "_New");
        codeListRecord.setDefinition(codeListToBeRevised.getDefinition());
        codeListRecord.setDefinitionSource(codeListToBeRevised.getDefinitionSource());
        codeListRecord.setRemark(codeListToBeRevised.getRemark());
        codeListRecord.setNamespaceId(ULong.valueOf(codeListToBeRevised.getNamespaceId()));
        codeListRecord.setExtensibleIndicator((byte) (codeListToBeRevised.isExtensibleIndicator() ? 1 : 0));
        codeListRecord.setIsDeprecated((byte) (codeListToBeRevised.isDeprecated() ? 1 : 0));
        codeListRecord.setState(codeListToBeRevised.getState());
        codeListRecord.setOwnerUserId(ULong.valueOf(codeListToBeRevised.getOwnerUserId()));
        codeListRecord.setCreatedBy(ULong.valueOf(codeListToBeRevised.getCreatedBy()));
        codeListRecord.setLastUpdatedBy(ULong.valueOf(codeListToBeRevised.getLastUpdatedBy()));
        codeListRecord.setCreationTimestamp(LocalDateTime.now());
        codeListRecord.setLastUpdateTimestamp(LocalDateTime.now());
        codeListRecord.setPrevCodeListId(ULong.valueOf(codeListToBeRevised.getCodeListId()));

        ULong codeListId = dslContext.insertInto(CODE_LIST)
                .set(codeListRecord)
                .returning(CODE_LIST.CODE_LIST_ID)
                .fetchOne().getCodeListId();

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(revisionNumber));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Revised");
        dummyLogRecord.setReference(codeListRecord.getGuid());
        dummyLogRecord.setSnapshot(JSON.valueOf("{\"component\": \"code_list\"}"));
        dummyLogRecord.setCreatedBy(codeListRecord.getCreatedBy());
        dummyLogRecord.setCreationTimestamp(codeListRecord.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        String agencyIdListValueName;
        if (creator.isDeveloper()) {
            agencyIdListValueName = "OAGi (Open Applications Group, Incorporated)";
        } else {
            agencyIdListValueName = "Mutually defined";
        }

        ULong agencyIdListValueManifestId =
                dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_VALUE)
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                AGENCY_ID_LIST_VALUE.NAME.eq(agencyIdListValueName)
                        ))
                        .fetchOneInto(ULong.class);

        CodeListManifestRecord codeListManifestRecord = new CodeListManifestRecord();
        codeListManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        codeListManifestRecord.setCodeListId(codeListId);
        codeListManifestRecord.setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        codeListManifestRecord.setLogId(logId);

        ULong codeListManifestId = dslContext.insertInto(CODE_LIST_MANIFEST)
                .set(codeListManifestRecord)
                .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .fetchOne().getCodeListManifestId();

        List<Field<?>> fields = new ArrayList();
        fields.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(CODE_LIST.fields()));
        CodeListObject revisedCodeList = dslContext.select(fields)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListId))
                .fetchOne(record -> codeListMapper(record));

        /**
         * Update old version
         */
        dslContext.update(CODE_LIST)
                .set(CODE_LIST.NEXT_CODE_LIST_ID, ULong.valueOf(codeListToBeRevised.getCodeListId()))
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(revisedCodeList.getCodeListId())))
                .execute();

        return revisedCodeList;
    }

    @Override
    public CodeListObject getNewlyCreatedCodeList(AppUserObject user, String releaseNumber) {
        ULong latestCodeListIDByUserInRelease = dslContext.select(DSL.max(CODE_LIST.CODE_LIST_ID))
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.eq(releaseNumber),
                        CODE_LIST.OWNER_USER_ID.eq(ULong.valueOf(user.getAppUserId()))))
                .fetchOneInto(ULong.class);
        List<Field<?>> fields = new ArrayList();
        fields.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID);
        fields.add(CODE_LIST_MANIFEST.RELEASE_ID);
        fields.addAll(Arrays.asList(CODE_LIST.fields()));
        return dslContext.select(fields)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(CODE_LIST.CODE_LIST_ID.eq(latestCodeListIDByUserInRelease))
                .fetchOne(record -> codeListMapper(record));
    }

    private CodeListObject codeListMapper(Record record) {
        CodeListObject codeList = new CodeListObject();
        codeList.setCodeListId(record.get(CODE_LIST.CODE_LIST_ID).toBigInteger());
        codeList.setCodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
        codeList.setName(record.get(CODE_LIST.NAME));
        codeList.setGuid(record.get(CODE_LIST.GUID));
        codeList.setEnumTypeGuid(record.get(CODE_LIST.ENUM_TYPE_GUID));
        codeList.setListId(record.get(CODE_LIST.LIST_ID));
        codeList.setVersionId(record.get(CODE_LIST.VERSION_ID));
        codeList.setDefinition(record.get(CODE_LIST.DEFINITION));
        codeList.setRemark(record.get(CODE_LIST.REMARK));
        codeList.setDefinitionSource(record.get(CODE_LIST.DEFINITION_SOURCE));
        codeList.setNamespaceId(record.get(CODE_LIST.NAMESPACE_ID) == null ? null : record.get(CODE_LIST.NAMESPACE_ID).toBigInteger());
        codeList.setCreatedBy(record.get(CODE_LIST.CREATED_BY).toBigInteger());
        codeList.setOwnerUserId(record.get(CODE_LIST.OWNER_USER_ID).toBigInteger());
        codeList.setLastUpdatedBy(record.get(CODE_LIST.LAST_UPDATED_BY).toBigInteger());
        codeList.setState(record.get(CODE_LIST.STATE));
        return codeList;
    }

    private ArrayList<CodeListObject> codeListMapperList(List<Result<Record>> records) {
        ArrayList<CodeListObject> codeLists = new ArrayList<>();
        for (Result result : records) {
            CodeListObject codeList = new CodeListObject();
            if (result.isNotEmpty()) {
                Record record = (Record) result.get(0);
                codeList.setCodeListId(record.get(CODE_LIST.CODE_LIST_ID).toBigInteger());
                codeList.setName(record.get(CODE_LIST.NAME));
                codeList.setGuid(record.get(CODE_LIST.GUID));
                codeList.setEnumTypeGuid(record.get(CODE_LIST.ENUM_TYPE_GUID));
                codeList.setListId(record.get(CODE_LIST.LIST_ID));
                codeList.setVersionId(record.get(CODE_LIST.VERSION_ID));
                codeList.setDefinition(record.get(CODE_LIST.DEFINITION));
                codeList.setRemark(record.get(CODE_LIST.REMARK));
                codeList.setDefinitionSource(record.get(CODE_LIST.DEFINITION_SOURCE));
                codeList.setNamespaceId(record.get(CODE_LIST.NAMESPACE_ID).toBigInteger());
                codeList.setCreatedBy(record.get(CODE_LIST.CREATED_BY).toBigInteger());
                codeList.setOwnerUserId(record.get(CODE_LIST.OWNER_USER_ID).toBigInteger());
                codeList.setLastUpdatedBy(record.get(CODE_LIST.LAST_UPDATED_BY).toBigInteger());
                codeList.setState(record.get(CODE_LIST.STATE));
                codeLists.add(codeList);
            }
        }
        return codeLists;
    }


    @Override
    public void addCodeListToAnotherRelease(CodeListObject codeList, ReleaseObject release, AppUserObject creator) {
        CodeListRecord codeListRecord = new CodeListRecord();
        codeListRecord.setGuid(codeList.getGuid());
        codeListRecord.setName(codeList.getName());
        codeListRecord.setListId(codeList.getListId());
        codeListRecord.setVersionId(codeList.getVersionId());
        codeListRecord.setDefinition(codeList.getDefinition());
        codeListRecord.setDefinitionSource(codeList.getDefinitionSource());
        codeListRecord.setRemark(codeList.getRemark());
        codeListRecord.setExtensibleIndicator((byte) (codeList.isExtensibleIndicator() ? 1 : 0));
        codeListRecord.setIsDeprecated((byte) (codeList.isDeprecated() ? 1 : 0));
        codeListRecord.setState(codeList.getState());
        codeListRecord.setOwnerUserId(ULong.valueOf(codeList.getOwnerUserId()));
        codeListRecord.setCreatedBy(ULong.valueOf(codeList.getCreatedBy()));
        codeListRecord.setLastUpdatedBy(ULong.valueOf(codeList.getLastUpdatedBy()));
        codeListRecord.setCreationTimestamp(codeList.getCreationTimestamp());
        codeListRecord.setLastUpdateTimestamp(codeList.getLastUpdateTimestamp());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(codeListRecord.getGuid());
        dummyLogRecord.setSnapshot(JSON.valueOf("{\"component\": \"code_list\"}"));
        dummyLogRecord.setCreatedBy(codeListRecord.getCreatedBy());
        dummyLogRecord.setCreationTimestamp(codeListRecord.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        String agencyIdListValueName;
        if (creator.isDeveloper()) {
            agencyIdListValueName = "OAGi (Open Applications Group, Incorporated)";
        } else {
            agencyIdListValueName = "Mutually defined";
        }

        ULong agencyIdListValueManifestId =
                dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_VALUE)
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                AGENCY_ID_LIST_VALUE.NAME.eq(agencyIdListValueName)
                        ))
                        .fetchOneInto(ULong.class);

        CodeListManifestRecord codeListManifestRecord = new CodeListManifestRecord();
        codeListManifestRecord.setBasedCodeListManifestId(codeList.getBasedCodeListManifestId() != null ? ULong.valueOf(codeList.getBasedCodeListManifestId()) : null);
        codeListManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        codeListManifestRecord.setCodeListId(ULong.valueOf(codeList.getCodeListId()));
        codeListManifestRecord.setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        codeListManifestRecord.setLogId(logId);

        ULong newCodeListManifestId = dslContext.insertInto(CODE_LIST_MANIFEST)
                .set(codeListManifestRecord)
                .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .fetchOne().getCodeListManifestId();

        addCodeListValuesToAnotherRelease(codeList, creator, release, newCodeListManifestId.toBigInteger());
    }

    private void addCodeListValuesToAnotherRelease(CodeListObject codeList, AppUserObject creator, ReleaseObject release, BigInteger newCodeListManifestId) {
        List<CodeListValueObject> codeListValueList =
                this.apiFactory.getCodeListValueAPI().getCodeListValuesByCodeListManifestId(codeList.getCodeListManifestId());

        for (CodeListValueObject codeListValue : codeListValueList) {
            this.apiFactory.getCodeListValueAPI().addCodeListValueToAnotherRelease(codeListValue, codeList, creator, newCodeListManifestId, release);
        }
    }


}
