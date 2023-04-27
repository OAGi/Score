package org.oagi.score.service.codelist;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.codelist.model.CodeListUpliftingRequest;
import org.oagi.score.service.codelist.model.CodeListUpliftingResponse;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class CodeListUpliftingService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private LogRepository logRepository;

    @Transactional
    public CodeListUpliftingResponse upliftCodeList(CodeListUpliftingRequest request) {

        CodeListUpliftingResponse response = new CodeListUpliftingResponse();

        response.setDuplicatedValues(new ArrayList<>());

        ScoreUser requester = request.getRequester();

        CodeListManifestRecord codeListManifest = dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getCodeListManifestId())))
                .fetchOptional().orElse(null);

        List<CodeListValueManifestRecord> codeListValueManifestList = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getCodeListManifestId())))
                .fetch();

        CodeListRecord codeList = dslContext.selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifest.getCodeListId()))
                .fetchOptional().orElse(null);

        List<CodeListValueRecord> codeListValueList = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        codeListValueManifestList.stream().map(e -> e.getCodeListValueId()).collect(Collectors.toList())
                ))
                .fetch();

        AgencyIdListValueManifestRecord agencyIdListValueManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(codeListManifest.getAgencyIdListValueManifestId()))
                .fetchOne();

        AgencyIdListValueRecord agencyIdListValueRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValueManifestRecord.getAgencyIdListValueId()))
                .fetchOne();

        AgencyIdListManifestRecord agencyIdListManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListValueManifestRecord.getAgencyIdListManifestId()))
                .fetchOne();

        AgencyIdListRecord agencyIdListRecord = dslContext.selectFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListManifestRecord.getAgencyIdListId()))
                .fetchOne();


        ULong targetReleaseId = ULong.valueOf(request.getTargetReleaseId());
        LocalDateTime timestamp = LocalDateTime.now();
        CodeListManifestRecord newCodeListManifest = new CodeListManifestRecord();
        newCodeListManifest.setReleaseId(targetReleaseId);

        /*
         * Issue #1283
         * Uplift an agency ID list manifest ID
         */
        newCodeListManifest.setAgencyIdListValueManifestId(
                dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .where(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                                AGENCY_ID_LIST_VALUE.GUID.eq(agencyIdListValueRecord.getGuid()),
                                AGENCY_ID_LIST.GUID.eq(agencyIdListRecord.getGuid())
                        ))
                        .fetchOneInto(ULong.class)
        );

        CodeListRecord newCodeList;

        // Issue #1073
        // If the source CL has the base CL, all CL values should be copying from the base again.
        if (codeListManifest.getBasedCodeListManifestId() != null) {
            ULong sourceBasedCodeListManifestId = codeListManifest.getBasedCodeListManifestId();
            CodeListManifestRecord targetBasedCodeListManifest = dslContext.selectFrom(CODE_LIST_MANIFEST)
                    .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(sourceBasedCodeListManifestId))
                    .fetchOptional().orElse(null);

            List<String> sourceCodeListValues = dslContext.select(CODE_LIST_VALUE.VALUE)
                    .from(CODE_LIST_VALUE_MANIFEST)
                    .join(CODE_LIST_VALUE).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                    .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(sourceBasedCodeListManifestId))
                    .fetchInto(String.class);

            // Find a target code list manifest recursively
            while (targetBasedCodeListManifest != null && !targetBasedCodeListManifest.getReleaseId().equals(targetReleaseId)) {
                targetBasedCodeListManifest = dslContext.selectFrom(CODE_LIST_MANIFEST)
                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(targetBasedCodeListManifest.getNextCodeListManifestId()))
                        .fetchOptional().orElse(null);
            }

            if (targetBasedCodeListManifest == null) {
                throw new IllegalStateException();
            }

            newCodeList = copyCodeList(codeList, requester, timestamp);
            newCodeList.setBasedCodeListId(targetBasedCodeListManifest.getCodeListId());
            newCodeList.setCodeListId(
                    dslContext.insertInto(CODE_LIST)
                            .set(newCodeList)
                            .returning(CODE_LIST.CODE_LIST_ID)
                            .fetchOne().getCodeListId()
            );

            newCodeListManifest.setCodeListId(newCodeList.getCodeListId());
            newCodeListManifest.setBasedCodeListManifestId(targetBasedCodeListManifest.getCodeListManifestId());
            newCodeListManifest.setCodeListManifestId(
                    dslContext.insertInto(CODE_LIST_MANIFEST)
                            .set(newCodeListManifest)
                            .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                            .fetchOne().getCodeListManifestId()
            );

            List<CodeListValueManifestRecord> targetBasedCodeListValueManifestList = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                    .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(targetBasedCodeListManifest.getCodeListManifestId()))
                    .fetch();

            List<CodeListValueRecord> targetBasedCodeListValueList = dslContext.selectFrom(CODE_LIST_VALUE)
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                            targetBasedCodeListValueManifestList.stream().map(e -> e.getCodeListValueId()).collect(Collectors.toList())
                    ))
                    .fetch();

            // Use case-insensitive a value set to prevent duplicated CL values
            Set<String> basedCodeListValueSet = new HashSet();
            targetBasedCodeListValueList.forEach(e -> {
                CodeListValueRecord newCodeListValue = copyCodeListValue(e, newCodeList, requester, timestamp);
                newCodeListValue.setCodeListValueId(
                        dslContext.insertInto(CODE_LIST_VALUE)
                                .set(newCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord newCodeListValueManifest = new CodeListValueManifestRecord();
                newCodeListValueManifest.setReleaseId(targetReleaseId);
                newCodeListValueManifest.setCodeListValueId(newCodeListValue.getCodeListValueId());
                newCodeListValueManifest.setCodeListManifestId(newCodeListManifest.getCodeListManifestId());
                dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(newCodeListValueManifest)
                        .execute();

                basedCodeListValueSet.add(newCodeListValue.getValue().toLowerCase());
            });

            codeListValueList.stream().forEach(e -> {
                if (basedCodeListValueSet.contains(e.getValue().toLowerCase())) {
                    response.getDuplicatedValues().add(e.getValue().toLowerCase());
                    return;
                }

                CodeListValueRecord newCodeListValue = copyCodeListValue(e, newCodeList, requester, timestamp);
                newCodeListValue.setCodeListValueId(
                        dslContext.insertInto(CODE_LIST_VALUE)
                                .set(newCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord newCodeListValueManifest = new CodeListValueManifestRecord();
                newCodeListValueManifest.setReleaseId(targetReleaseId);
                newCodeListValueManifest.setCodeListValueId(newCodeListValue.getCodeListValueId());
                newCodeListValueManifest.setCodeListManifestId(newCodeListManifest.getCodeListManifestId());
                dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(newCodeListValueManifest)
                        .execute();
            });

            sourceCodeListValues.forEach(e -> {
                if (response.getDuplicatedValues().indexOf(e.toLowerCase()) > -1 ) {
                    response.getDuplicatedValues().remove(response.getDuplicatedValues().indexOf(e.toLowerCase()));
                }
            });

        } else {
            newCodeList = copyCodeList(codeList, requester, timestamp);
            newCodeList.setCodeListId(
                    dslContext.insertInto(CODE_LIST)
                            .set(newCodeList)
                            .returning(CODE_LIST.CODE_LIST_ID)
                            .fetchOne().getCodeListId()
            );

            newCodeListManifest.setCodeListId(newCodeList.getCodeListId());
            newCodeListManifest.setCodeListManifestId(
                    dslContext.insertInto(CODE_LIST_MANIFEST)
                            .set(newCodeListManifest)
                            .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                            .fetchOne().getCodeListManifestId()
            );

            codeListValueList.stream().forEach(e -> {
                CodeListValueRecord newCodeListValue = copyCodeListValue(e, newCodeList, requester, timestamp);
                newCodeListValue.setCodeListValueId(
                        dslContext.insertInto(CODE_LIST_VALUE)
                                .set(newCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord newCodeListValueManifest = new CodeListValueManifestRecord();
                newCodeListValueManifest.setReleaseId(targetReleaseId);
                newCodeListValueManifest.setCodeListValueId(newCodeListValue.getCodeListValueId());
                newCodeListValueManifest.setCodeListManifestId(newCodeListManifest.getCodeListManifestId());
                dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(newCodeListValueManifest)
                        .execute();
            });
        }

        LogRecord logRecord = logRepository.insertCodeListLog(newCodeListManifest,
                newCodeList,
                LogAction.Added,
                ULong.valueOf(requester.getUserId()),
                timestamp);
        newCodeListManifest.setLogId(logRecord.getLogId());
        dslContext.update(CODE_LIST_MANIFEST)
                .set(CODE_LIST_MANIFEST.LOG_ID, logRecord.getLogId())
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(newCodeListManifest.getCodeListManifestId()))
                .execute();

        response.setCodeListManifestId(newCodeListManifest.getCodeListManifestId().toBigInteger());
        return response;
    }

    private CodeListValueRecord copyCodeListValue(CodeListValueRecord codeListValue,
                                                  CodeListRecord newCodeList,
                                                  ScoreUser requester, LocalDateTime timestamp) {
        CodeListValueRecord newCodeListValue = new CodeListValueRecord();
        newCodeListValue.setGuid(ScoreGuidUtils.randomGuid());
        newCodeListValue.setCodeListId(newCodeList.getCodeListId());
        newCodeListValue.setValue(codeListValue.getValue());
        newCodeListValue.setMeaning(codeListValue.getMeaning());
        newCodeListValue.setDefinition(codeListValue.getDefinition());
        newCodeListValue.setDefinitionSource(codeListValue.getDefinitionSource());
        newCodeListValue.setIsDeprecated(codeListValue.getIsDeprecated());
        newCodeListValue.setOwnerUserId(ULong.valueOf(requester.getUserId()));
        newCodeListValue.setCreatedBy(ULong.valueOf(requester.getUserId()));
        newCodeListValue.setCreationTimestamp(timestamp);
        newCodeListValue.setLastUpdatedBy(ULong.valueOf(requester.getUserId()));
        newCodeListValue.setLastUpdateTimestamp(timestamp);
        return newCodeListValue;
    }

    private CodeListRecord copyCodeList(CodeListRecord codeList,
                                        ScoreUser requester, LocalDateTime timestamp) {
        CodeListRecord newCodeList = new CodeListRecord();
        newCodeList.setGuid(ScoreGuidUtils.randomGuid());
        if (hasLength(codeList.getEnumTypeGuid())) {
            newCodeList.setEnumTypeGuid(ScoreGuidUtils.randomGuid());
        }
        newCodeList.setBasedCodeListId(codeList.getBasedCodeListId());
        newCodeList.setName(codeList.getName());
        newCodeList.setListId(codeList.getListId());
        newCodeList.setVersionId(codeList.getVersionId());
        newCodeList.setRemark(codeList.getRemark());
        newCodeList.setDefinition(codeList.getDefinition());
        newCodeList.setDefinitionSource(codeList.getDefinitionSource());
        newCodeList.setNamespaceId(codeList.getNamespaceId());
        newCodeList.setExtensibleIndicator(codeList.getExtensibleIndicator());
        // Test Assertion #33.2.7
        // After Uplifting, the "Deprecated" should be false.
        newCodeList.setIsDeprecated((byte) 0);
        newCodeList.setOwnerUserId(ULong.valueOf(requester.getUserId()));
        newCodeList.setCreatedBy(ULong.valueOf(requester.getUserId()));
        newCodeList.setCreationTimestamp(timestamp);
        newCodeList.setLastUpdatedBy(ULong.valueOf(requester.getUserId()));
        newCodeList.setLastUpdateTimestamp(timestamp);
        newCodeList.setState(CcState.WIP.name());
        return newCodeList;
    }
}
