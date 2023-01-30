package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.CodeListValueAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListValueManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.CodeListValueRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.CodeListValueObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CODE_LIST_VALUE;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CODE_LIST_VALUE_MANIFEST;

public class DSLContextCodeListValueAPIImpl implements CodeListValueAPI {

    private final DSLContext dslContext;

    public DSLContextCodeListValueAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public List<CodeListValueObject> getCodeListValuesByCodeListManifestId(BigInteger codeListManifestId) {
        List<CodeListValueManifestRecord> codeListValueManifestRecords =
                dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                        .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)))
                        .fetch();

        Map<ULong, CodeListValueRecord> codeListValueRecordMap = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        codeListValueManifestRecords.stream().map(e -> e.getCodeListValueId()).collect(Collectors.toSet())
                ))
                .fetchStream().collect(Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));

        List<CodeListValueObject> codeListValues = new ArrayList<>();
        for (CodeListValueManifestRecord codeListValueManifestRecord : codeListValueManifestRecords) {
            CodeListValueRecord codeListValueRecord = codeListValueRecordMap.get(codeListValueManifestRecord.getCodeListValueId());

            CodeListValueObject codeListValue = new CodeListValueObject();
            codeListValue.setCodeListValueManifestId(codeListValueManifestRecord.getCodeListValueManifestId().toBigInteger());
            codeListValue.setCodeListValueId(codeListValueRecord.getCodeListValueId().toBigInteger());
            if (codeListValueManifestRecord.getBasedCodeListValueManifestId() != null) {
                codeListValue.setBasedCodeListValueManifestId(codeListValueManifestRecord.getBasedCodeListValueManifestId().toBigInteger());
            }
            codeListValue.setCodeListManifestId(codeListValueManifestRecord.getCodeListManifestId().toBigInteger());
            codeListValue.setReleaseId(codeListValueManifestRecord.getReleaseId().toBigInteger());
            codeListValue.setGuid(codeListValueRecord.getGuid());
            codeListValue.setValue(codeListValueRecord.getValue());
            codeListValue.setMeaning(codeListValueRecord.getMeaning());
            codeListValue.setDefinition(codeListValueRecord.getDefinition());
            codeListValue.setDefinitionSource(codeListValueRecord.getDefinitionSource());
            codeListValue.setDeprecated(codeListValueRecord.getIsDeprecated() == 1);
            codeListValue.setOwnerUserId(codeListValueRecord.getOwnerUserId().toBigInteger());
            codeListValue.setCreatedBy(codeListValueRecord.getCreatedBy().toBigInteger());
            codeListValue.setLastUpdatedBy(codeListValueRecord.getLastUpdatedBy().toBigInteger());
            codeListValue.setCreationTimestamp(codeListValueRecord.getCreationTimestamp());
            codeListValue.setLastUpdateTimestamp(codeListValueRecord.getLastUpdateTimestamp());
            codeListValues.add(codeListValue);
        }
        return codeListValues;
    }

    @Override
    public CodeListValueObject createRandomCodeListValue(CodeListObject codeList, AppUserObject creator) {
        CodeListValueObject codeListValue = CodeListValueObject.createRandomCodeListValue(codeList, creator);

        CodeListValueRecord codeListValueRecord = new CodeListValueRecord();
        codeListValueRecord.setGuid(codeListValue.getGuid());
        codeListValueRecord.setCodeListId(ULong.valueOf(codeList.getCodeListId()));
        codeListValueRecord.setValue(codeListValue.getValue());
        codeListValueRecord.setMeaning(codeListValue.getMeaning());
        codeListValueRecord.setDefinition(codeListValue.getDefinition());
        codeListValueRecord.setDefinitionSource(codeListValue.getDefinitionSource());
        codeListValueRecord.setIsDeprecated((byte) (codeListValue.isDeprecated() ? 1 : 0));
        codeListValueRecord.setOwnerUserId(ULong.valueOf(codeList.getOwnerUserId()));
        codeListValueRecord.setCreatedBy(ULong.valueOf(codeList.getCreatedBy()));
        codeListValueRecord.setLastUpdatedBy(ULong.valueOf(codeList.getLastUpdatedBy()));
        codeListValueRecord.setCreationTimestamp(codeList.getCreationTimestamp());
        codeListValueRecord.setLastUpdateTimestamp(codeList.getLastUpdateTimestamp());

        ULong codeListValueId = dslContext.insertInto(CODE_LIST_VALUE)
                .set(codeListValueRecord)
                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                .fetchOne().getCodeListValueId();

        CodeListValueManifestRecord codeListValueManifestRecord = new CodeListValueManifestRecord();
        codeListValueManifestRecord.setCodeListManifestId(ULong.valueOf(codeList.getCodeListManifestId()));
        codeListValueManifestRecord.setReleaseId(ULong.valueOf(codeList.getReleaseId()));
        codeListValueManifestRecord.setCodeListValueId(codeListValueId);

        ULong codeListValueManifestId = dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                .set(codeListValueManifestRecord)
                .returning(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .fetchOne().getCodeListValueManifestId();

        codeListValue.setCodeListValueManifestId(codeListValueManifestId.toBigInteger());
        codeListValue.setCodeListValueId(codeListValueId.toBigInteger());
        codeListValue.setReleaseId(codeList.getReleaseId());
        return codeListValue;
    }

    @Override
    public CodeListValueObject createDerivedCodeListValue(CodeListValueObject baseCodeListValue,
                                                          CodeListObject codeList, AppUserObject creator) {
        CodeListValueObject codeListValue = CodeListValueObject.createDerivedCodeListValue(baseCodeListValue, codeList, creator);

        CodeListValueRecord codeListValueRecord = new CodeListValueRecord();
        codeListValueRecord.setBasedCodeListValueId(ULong.valueOf(baseCodeListValue.getCodeListValueId()));
        codeListValueRecord.setGuid(codeListValue.getGuid());
        codeListValueRecord.setCodeListId(ULong.valueOf(codeList.getCodeListId()));
        codeListValueRecord.setValue(codeListValue.getValue());
        codeListValueRecord.setMeaning(codeListValue.getMeaning());
        codeListValueRecord.setDefinition(codeListValue.getDefinition());
        codeListValueRecord.setDefinitionSource(codeListValue.getDefinitionSource());
        codeListValueRecord.setIsDeprecated((byte) (codeListValue.isDeprecated() ? 1 : 0));
        codeListValueRecord.setOwnerUserId(ULong.valueOf(codeList.getOwnerUserId()));
        codeListValueRecord.setCreatedBy(ULong.valueOf(codeList.getCreatedBy()));
        codeListValueRecord.setLastUpdatedBy(ULong.valueOf(codeList.getLastUpdatedBy()));
        codeListValueRecord.setCreationTimestamp(codeList.getCreationTimestamp());
        codeListValueRecord.setLastUpdateTimestamp(codeList.getLastUpdateTimestamp());

        ULong codeListValueId = dslContext.insertInto(CODE_LIST_VALUE)
                .set(codeListValueRecord)
                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                .fetchOne().getCodeListValueId();

        CodeListValueManifestRecord codeListValueManifestRecord = new CodeListValueManifestRecord();
        codeListValueManifestRecord.setBasedCodeListValueManifestId(ULong.valueOf(baseCodeListValue.getCodeListValueManifestId()));
        codeListValueManifestRecord.setCodeListManifestId(ULong.valueOf(codeList.getCodeListManifestId()));
        codeListValueManifestRecord.setReleaseId(ULong.valueOf(codeList.getReleaseId()));
        codeListValueManifestRecord.setCodeListValueId(codeListValueId);

        ULong codeListValueManifestId = dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                .set(codeListValueManifestRecord)
                .returning(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .fetchOne().getCodeListValueManifestId();

        codeListValue.setBasedCodeListValueManifestId(baseCodeListValue.getCodeListValueManifestId());
        codeListValue.setCodeListValueManifestId(codeListValueManifestId.toBigInteger());
        codeListValue.setCodeListValueId(codeListValueId.toBigInteger());
        codeListValue.setReleaseId(codeList.getReleaseId());
        return codeListValue;
    }

    @Override
    public void addCodeListValueToAnotherRelease(CodeListValueObject codeListValue, CodeListObject codeList, AppUserObject creator, BigInteger newCodeListManifestId, ReleaseObject release) {
        CodeListValueManifestRecord codeListValueManifestRecord = new CodeListValueManifestRecord();
        codeListValueManifestRecord.setBasedCodeListValueManifestId(ULong.valueOf(codeListValue.getBasedCodeListValueManifestId()));
        codeListValueManifestRecord.setCodeListManifestId(ULong.valueOf(newCodeListManifestId));
        codeListValueManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        codeListValueManifestRecord.setCodeListValueId(ULong.valueOf(codeListValue.getCodeListValueId()));

        ULong codeListValueManifestId = dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                .set(codeListValueManifestRecord)
                .returning(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .fetchOne().getCodeListValueManifestId();
    }
}
