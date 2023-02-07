package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.CodeListAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.LogRecord;
import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;
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
        dummyLogRecord.setSnapshot(JSON.valueOf("{}"));
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
        dummyLogRecord.setSnapshot(JSON.valueOf("{}"));
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
        dummyLogRecord.setSnapshot(JSON.valueOf("{}"));
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
