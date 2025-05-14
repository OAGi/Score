package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.code_list_management.model.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CodeListRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CodeListValueManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CodeListValueRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Deleted;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CodeListManifest.CODE_LIST_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqCodeListCommandRepository
        extends JooqBaseRepository
        implements CodeListCommandRepository {

    private final CodeListQueryRepository codeListQueryRepository;

    private final AgencyIdListQueryRepository agencyIdListQueryRepository;

    private final LogCommandRepository logCommandRepository;

    public JooqCodeListCommandRepository(DSLContext dslContext, ScoreUser requester,
                                         RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.codeListQueryRepository = repositoryFactory.codeListQueryRepository(requester);
        this.agencyIdListQueryRepository = repositoryFactory.agencyIdListQueryRepository(requester);
        this.logCommandRepository = repositoryFactory.logCommandRepository(requester);
    }

    @Override
    public CodeListManifestId create(ReleaseId releaseId,
                                     CodeListManifestId basedCodeListManifestId) {

        ULong userId = valueOf(requester().userId());
        LocalDateTime timestamp = LocalDateTime.now();

        CodeListRecord codeList = new CodeListRecord();
        codeList.setGuid(ScoreGuidUtils.randomGuid());
        codeList.setListId(ScoreGuidUtils.randomGuid());
        codeList.setState(CcState.WIP.name());
        codeList.setCreatedBy(userId);
        codeList.setLastUpdatedBy(userId);
        codeList.setOwnerUserId(userId);
        codeList.setCreationTimestamp(timestamp);
        codeList.setLastUpdateTimestamp(timestamp);

        Collection<CodeListValueDetailsRecord> basedCodeListValueList = null;
        CodeListManifestRecord basedCodeListManifestRecord = null;
        AgencyIdListValueSummaryRecord agencyIdListValue;

        CodeListDetailsRecord basedCodeListDetails = null;
        if (basedCodeListManifestId != null) {
            basedCodeListDetails = codeListQueryRepository.getCodeListDetails(basedCodeListManifestId);
            if (basedCodeListDetails == null) {
                throw new IllegalArgumentException("Cannot find a based Code List [codeListManifestId=" + basedCodeListManifestId + "]");
            }

            codeList.setName(basedCodeListDetails.name());
            agencyIdListValue = basedCodeListDetails.agencyIdListValue();

            codeList.setVersionId(basedCodeListDetails.versionId());
            codeList.setBasedCodeListId(valueOf(basedCodeListDetails.codeListId()));
            if (requester().isDeveloper()) {
                codeList.setExtensibleIndicator((byte) (basedCodeListDetails.extensible() ? 1 : 0));
            } else {
                codeList.setExtensibleIndicator((byte) 0);
            }
            codeList.setIsDeprecated((byte) 0);

            basedCodeListValueList = basedCodeListDetails.valueList();
        } else {
            codeList.setName("Code List");
            agencyIdListValue = agencyIdListQueryRepository.getAgencyIdListValueSummaryList(releaseId)
                    .stream().filter(e -> (requester().isDeveloper()) ? e.isDeveloperDefault() : e.isUserDefault())
                    .findAny().orElse(null);

            codeList.setVersionId("1");
            codeList.setExtensibleIndicator((byte) (requester().isDeveloper() ? 1 : 0));
            codeList.setIsDeprecated((byte) 0);
        }

        if (basedCodeListManifestRecord != null) {
            codeList.setBasedCodeListId(basedCodeListManifestRecord.getCodeListId());
        }

        CodeListId codeListId = new CodeListId(dslContext().insertInto(CODE_LIST)
                .set(codeList)
                .returning(CODE_LIST.CODE_LIST_ID)
                .fetchOne().getCodeListId().toBigInteger());
        codeList.setCodeListId(valueOf(codeListId));

        CodeListManifestRecord codeListManifest = new CodeListManifestRecord();
        codeListManifest.setCodeListId(codeList.getCodeListId());
        codeListManifest.setReleaseId(valueOf(releaseId));
        if (basedCodeListManifestRecord != null) {
            codeListManifest.setBasedCodeListManifestId(basedCodeListManifestRecord.getCodeListManifestId());
        }
        if (agencyIdListValue != null) {
            codeListManifest.setAgencyIdListValueManifestId(valueOf(agencyIdListValue.agencyIdListValueManifestId()));
        }

        CodeListManifestId codeListManifestId = new CodeListManifestId(
                dslContext().insertInto(CODE_LIST_MANIFEST)
                        .set(codeListManifest)
                        .returning(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                        .fetchOne().getCodeListManifestId().toBigInteger());

        if (basedCodeListValueList != null) {
            for (CodeListValueDetailsRecord basedCodeListValue : basedCodeListValueList) {

                CodeListValueRecord basedCodeListValueRecord =
                        dslContext().selectFrom(CODE_LIST_VALUE)
                                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(valueOf(basedCodeListValue.codeListValueId())))
                                .fetchOne();

                CodeListValueRecord codeListValueRecord = basedCodeListValueRecord.copy();
                codeListValueRecord.setCodeListId(codeList.getCodeListId());
                codeListValueRecord.setGuid(ScoreGuidUtils.randomGuid());
                codeListValueRecord.setBasedCodeListValueId(valueOf(basedCodeListValue.codeListValueId()));
                codeListValueRecord.setCreatedBy(userId);
                codeListValueRecord.setLastUpdatedBy(userId);
                codeListValueRecord.setOwnerUserId(userId);
                codeListValueRecord.setCreationTimestamp(timestamp);
                codeListValueRecord.setLastUpdateTimestamp(timestamp);
                codeListValueRecord.setPrevCodeListValueId(null);
                codeListValueRecord.setNextCodeListValueId(null);

                codeListValueRecord.setCodeListValueId(
                        dslContext().insertInto(CODE_LIST_VALUE)
                                .set(codeListValueRecord)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID).fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord basedCodeListValueManifestRecord =
                        dslContext().selectFrom(CODE_LIST_VALUE_MANIFEST)
                                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(basedCodeListValue.codeListValueManifestId())))
                                .fetchOne();
                CodeListValueManifestRecord codeListValueManifestRecord = basedCodeListValueManifestRecord.copy();
                codeListValueManifestRecord.setReleaseId(valueOf(releaseId));
                codeListValueManifestRecord.setCodeListValueId(codeListValueRecord.getCodeListValueId());
                codeListValueManifestRecord.setCodeListManifestId(valueOf(codeListManifestId));
                codeListValueManifestRecord.setBasedCodeListValueManifestId(valueOf(basedCodeListValue.codeListValueManifestId()));
                codeListValueManifestRecord.setPrevCodeListValueManifestId(null);
                codeListValueManifestRecord.setNextCodeListValueManifestId(null);

                dslContext().insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(codeListValueManifestRecord)
                        .execute();
            }
        }

        return codeListManifestId;
    }

    @Override
    public boolean update(CodeListManifestId codeListManifestId,
                          String name, String versionId, String listId,
                          AgencyIdListValueManifestId agencyIdListValueManifestId,
                          Definition definition, String remark,
                          NamespaceId namespaceId,
                          Boolean deprecated, Boolean extensible) {

        CodeListDetailsRecord codeListDetails =
                codeListQueryRepository.getCodeListDetails(codeListManifestId);
        if (codeListDetails == null) {
            return false;
        }

        AgencyIdListValueDetailsRecord agencyIdListValueDetails = null;
        if (agencyIdListValueManifestId != null) {
            agencyIdListValueDetails = agencyIdListQueryRepository.getAgencyIdListValueDetails(agencyIdListValueManifestId);
            if (agencyIdListValueDetails == null) {
                throw new IllegalArgumentException("AgencyIdListValueManifestId does not exist");
            }

            dslContext().update(CODE_LIST_MANIFEST)
                    .set(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, valueOf(agencyIdListValueDetails.agencyIdListValueManifestId()))
                    .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                    .execute();
        }

        var step = dslContext().update(CODE_LIST);
        UpdateSetMoreStep more = null;
        if (hasLength(name)) {
            more = ((more != null) ? more : step).set(CODE_LIST.NAME, name);
        }
        if (hasLength(versionId)) {
            more = ((more != null) ? more : step).set(CODE_LIST.VERSION_ID, versionId);
        }
        if (hasLength(listId)) {
            more = ((more != null) ? more : step).set(CODE_LIST.LIST_ID, listId);
        }
        if (definition != null) {
            if (hasLength(definition.content())) {
                more = ((more != null) ? more : step).set(CODE_LIST.DEFINITION, definition.content());
            }
            if (hasLength(definition.source())) {
                more = ((more != null) ? more : step).set(CODE_LIST.DEFINITION_SOURCE, definition.source());
            }
        }
        if (hasLength(remark)) {
            more = ((more != null) ? more : step).set(CODE_LIST.REMARK, remark);
        }
        if (namespaceId != null) {
            more = ((more != null) ? more : step).set(CODE_LIST.NAMESPACE_ID, valueOf(namespaceId));
        }
        if (deprecated != null) {
            more = ((more != null) ? more : step).set(CODE_LIST.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (extensible != null) {
            more = ((more != null) ? more : step).set(CODE_LIST.EXTENSIBLE_INDICATOR, (byte) (extensible ? 1 : 0));
        }

        if (more != null) {
            int numOfUpdatedRecords = more.set(CODE_LIST.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(CODE_LIST.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(CODE_LIST.CODE_LIST_ID.eq(valueOf(codeListDetails.codeListId())))
                    .execute();
            return numOfUpdatedRecords == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateState(CodeListManifestId codeListManifestId, CcState nextState) {

        CodeListDetailsRecord codeListDetails =
                codeListQueryRepository.getCodeListDetails(codeListManifestId);
        if (codeListDetails == null) {
            return false;
        }

        LocalDateTime timestamp = LocalDateTime.now();
        ULong userId = valueOf(requester().userId());

        CcState prevState = codeListDetails.state();
        UpdateSetMoreStep<CodeListRecord> step = dslContext().update(CODE_LIST)
                .set(CODE_LIST.STATE, nextState.name());
        if (!prevState.isImplicitMove(nextState)) {
            step = step.set(CODE_LIST.LAST_UPDATED_BY, userId)
                    .set(CODE_LIST.LAST_UPDATE_TIMESTAMP, timestamp);
        }
        if (prevState == Deleted) {
            step = step.set(CODE_LIST.OWNER_USER_ID, userId);
        }
        int numOfUpdatedRecords =
                step.where(CODE_LIST.CODE_LIST_ID.eq(valueOf(codeListDetails.codeListId())))
                        .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, CodeListManifestId codeListManifestId) {

        LocalDateTime timestamp = LocalDateTime.now();

        CodeListDetailsRecord codeListDetails =
                codeListQueryRepository.getCodeListDetails(codeListManifestId);

        int numOfUpdatedRecords = dslContext().update(CODE_LIST)
                .set(CODE_LIST.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(CODE_LIST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(CODE_LIST.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(CODE_LIST.CODE_LIST_ID.eq(valueOf(codeListDetails.codeListId())))
                .execute();
        if (numOfUpdatedRecords < 1) {
            return false;
        }

        dslContext().update(CODE_LIST_VALUE)
                .set(CODE_LIST_VALUE.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(CODE_LIST_VALUE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(CODE_LIST_VALUE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        valueOf(codeListDetails.valueList().stream()
                                .map(e -> e.codeListValueId()).collect(Collectors.toSet()))
                ))
                .execute();

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(CodeListManifestId codeListManifestId) {

        CodeListDetailsRecord codeListDetails =
                codeListQueryRepository.getCodeListDetails(codeListManifestId);
        if (codeListDetails == null) {
            return false;
        }

        if (!codeListDetails.valueList().isEmpty()) {
            // Delete records.
            dslContext().deleteFrom(CODE_LIST_VALUE_MANIFEST)
                    .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.in(
                            codeListDetails.valueList().stream()
                                    .map(e -> valueOf(e.codeListValueManifestId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();
            dslContext().deleteFrom(CODE_LIST_VALUE)
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                            codeListDetails.valueList().stream()
                                    .map(e -> valueOf(e.codeListValueId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();
        }

        dslContext().deleteFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListDetails.codeListManifestId())))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(valueOf(codeListDetails.codeListId())))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public void revise(CodeListManifestId codeListManifestId) {
        LocalDateTime timestamp = LocalDateTime.now();

        CodeListManifestRecord currentCodeListManifestRecord = dslContext().selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOne();

        CodeListRecord currentCodeListRecord = dslContext().select(CODE_LIST.fields())
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOneInto(CodeListRecord.class);

        CodeListRecord nextCodeListRecord = currentCodeListRecord.copy();
        nextCodeListRecord.setCodeListId(null);
        nextCodeListRecord.setState(CcState.WIP.name());
        nextCodeListRecord.setVersionId(nextCodeListRecord.getVersionId() + "_New");
        nextCodeListRecord.setCreatedBy(valueOf(requester().userId()));
        nextCodeListRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextCodeListRecord.setOwnerUserId(valueOf(requester().userId()));
        nextCodeListRecord.setCreationTimestamp(timestamp);
        nextCodeListRecord.setLastUpdateTimestamp(timestamp);
        nextCodeListRecord.setPrevCodeListId(currentCodeListRecord.getCodeListId());
        nextCodeListRecord.setCodeListId(
                dslContext().insertInto(CODE_LIST)
                        .set(nextCodeListRecord)
                        .returning(CODE_LIST.CODE_LIST_ID).fetchOne().getCodeListId()
        );

        currentCodeListRecord.setNextCodeListId(nextCodeListRecord.getCodeListId());
        currentCodeListRecord.update(CODE_LIST.NEXT_CODE_LIST_ID);

        createNewCodeListValueForRevisedRecord(codeListManifestId, nextCodeListRecord, timestamp);

        currentCodeListManifestRecord.setCodeListId(nextCodeListRecord.getCodeListId());
        currentCodeListManifestRecord.update(CODE_LIST_MANIFEST.CODE_LIST_ID);
    }

    private void createNewCodeListValueForRevisedRecord(
            CodeListManifestId codeListManifestId,
            CodeListRecord nextCodeListRecord,
            LocalDateTime timestamp) {

        for (CodeListValueManifestRecord codeListValueManifestRecord : dslContext().selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetch()) {

            CodeListValueRecord prevCodeListValueRecord = dslContext().selectFrom(CODE_LIST_VALUE)
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(codeListValueManifestRecord.getCodeListValueId()))
                    .fetchOne();

            CodeListValueRecord nextCodeListValueRecord = prevCodeListValueRecord.copy();
            nextCodeListValueRecord.setCodeListValueId(null);
            nextCodeListValueRecord.setCodeListId(nextCodeListRecord.getCodeListId());
            nextCodeListValueRecord.setCreatedBy(valueOf(requester().userId()));
            nextCodeListValueRecord.setLastUpdatedBy(valueOf(requester().userId()));
            nextCodeListValueRecord.setOwnerUserId(valueOf(requester().userId()));
            nextCodeListValueRecord.setCreationTimestamp(timestamp);
            nextCodeListValueRecord.setLastUpdateTimestamp(timestamp);
            nextCodeListValueRecord.setPrevCodeListValueId(prevCodeListValueRecord.getCodeListValueId());
            nextCodeListValueRecord.setCodeListValueId(
                    dslContext().insertInto(CODE_LIST_VALUE)
                            .set(nextCodeListValueRecord)
                            .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID).fetchOne().getCodeListValueId()
            );

            prevCodeListValueRecord.setNextCodeListValueId(nextCodeListValueRecord.getCodeListValueId());
            prevCodeListValueRecord.update(CODE_LIST_VALUE.NEXT_CODE_LIST_VALUE_ID);

            codeListValueManifestRecord.setCodeListValueId(nextCodeListValueRecord.getCodeListValueId());
            codeListValueManifestRecord.update(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID);
        }
    }

    @Override
    public void cancel(CodeListManifestId codeListManifestId) {
        CodeListManifestRecord codeListManifestRecord = dslContext().selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOne();

        CodeListRecord codeListRecord = dslContext().selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId())).fetchOne();

        CodeListRecord prevCodeListRecord = dslContext().selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListRecord.getPrevCodeListId())).fetchOne();

        // update AGENCY ID LIST MANIFEST's codeList_id and revision_id
        codeListManifestRecord.setCodeListId(codeListRecord.getPrevCodeListId());
        codeListManifestRecord.update(CODE_LIST_MANIFEST.CODE_LIST_ID);

        discardLogCodeListValues(codeListManifestRecord);

        // unlink prev CODE_LIST
        prevCodeListRecord.setNextCodeListId(null);
        prevCodeListRecord.update(CODE_LIST.NEXT_CODE_LIST_ID);

        codeListManifestRecord.setLogId(null);
        codeListManifestRecord.update(CODE_LIST_MANIFEST.LOG_ID);

        // delete current CODE_LIST
        codeListRecord.delete();
    }

    private void discardLogCodeListValues(CodeListManifestRecord codeListManifestRecord) {
        List<CodeListValueManifestRecord> codeListValueManifests = dslContext().selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestRecord.getCodeListManifestId()))
                .fetch();

        for (CodeListValueManifestRecord codeListValueManifest : codeListValueManifests) {
            CodeListValueRecord codeListValue = dslContext().selectFrom(CODE_LIST_VALUE)
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(codeListValueManifest.getCodeListValueId()))
                    .fetchOne();

            if (codeListValue.getPrevCodeListValueId() == null) {
                // delete code list value and code list manifest which added this revision
                codeListValueManifest.delete();
                codeListValue.delete();
            } else {
                CodeListValueRecord prevCodeListValue = dslContext().selectFrom(CODE_LIST_VALUE)
                        .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(codeListValue.getPrevCodeListValueId()))
                        .fetchOne();

                prevCodeListValue.setNextCodeListValueId(null);
                prevCodeListValue.update(CODE_LIST_VALUE.NEXT_CODE_LIST_VALUE_ID);
                codeListValueManifest.setCodeListValueId(prevCodeListValue.getCodeListValueId());
                codeListValueManifest.update(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID);
                codeListValue.delete();
            }
        }
    }

    @Override
    public CodeListValueManifestId createValue(
            CodeListManifestId codeListManifestId,
            CodeListId codeListId,
            ReleaseId releaseId,
            String value, String meaning,
            Definition definition) {

        LocalDateTime timestamp = LocalDateTime.now();

        CodeListValueRecord codeListValueRecord = new CodeListValueRecord();
        codeListValueRecord.setCodeListId(valueOf(codeListId));
        codeListValueRecord.setGuid(ScoreGuidUtils.randomGuid());
        codeListValueRecord.setValue(value);
        codeListValueRecord.setMeaning(meaning);
        if (definition != null) {
            codeListValueRecord.setDefinition(definition.content());
            codeListValueRecord.setDefinitionSource(definition.source());
        }
        codeListValueRecord.setCreatedBy(valueOf(requester().userId()));
        codeListValueRecord.setOwnerUserId(valueOf(requester().userId()));
        codeListValueRecord.setLastUpdatedBy(valueOf(requester().userId()));
        codeListValueRecord.setCreationTimestamp(timestamp);
        codeListValueRecord.setLastUpdateTimestamp(timestamp);
        codeListValueRecord.setIsDeprecated((byte) 0);

        CodeListValueId codeListValueId = new CodeListValueId(
                dslContext().insertInto(CODE_LIST_VALUE)
                        .set(codeListValueRecord)
                        .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                        .fetchOne().getCodeListValueId().toBigInteger());

        CodeListValueManifestRecord codeListValueManifestRecord = new CodeListValueManifestRecord();

        codeListValueManifestRecord.setReleaseId(valueOf(releaseId));
        codeListValueManifestRecord.setCodeListValueId(valueOf(codeListValueId));
        codeListValueManifestRecord.setCodeListManifestId(valueOf(codeListManifestId));

        return new CodeListValueManifestId(
                dslContext().insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(codeListValueManifestRecord)
                        .returning(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                        .fetchOne().getCodeListValueManifestId().toBigInteger()
        );
    }

    @Override
    public boolean updateValue(
            CodeListValueManifestId codeListValueManifestId,
            String value, String meaning,
            Definition definition,
            Boolean deprecated) {

        CodeListValueDetailsRecord codeListValueDetails =
                codeListQueryRepository.getCodeListValueDetails(codeListValueManifestId);

        var step = dslContext().update(CODE_LIST_VALUE);
        UpdateSetMoreStep more = null;
        if (hasLength(value)) {
            more = ((more != null) ? more : step).set(CODE_LIST_VALUE.VALUE, value);
        }
        if (hasLength(meaning)) {
            more = ((more != null) ? more : step).set(CODE_LIST_VALUE.MEANING, meaning);
        }
        if (definition != null) {
            if (hasLength(definition.content())) {
                more = ((more != null) ? more : step).set(CODE_LIST_VALUE.DEFINITION, definition.content());
            }
            if (hasLength(definition.source())) {
                more = ((more != null) ? more : step).set(CODE_LIST_VALUE.DEFINITION_SOURCE, definition.source());
            }
        }
        if (deprecated != null) {
            more = ((more != null) ? more : step).set(CODE_LIST_VALUE.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }

        if (more != null) {
            int numOfUpdatedRecords = more.set(CODE_LIST_VALUE.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(CODE_LIST_VALUE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(valueOf(codeListValueDetails.codeListValueId())))
                    .execute();
            return numOfUpdatedRecords == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteValue(CodeListValueManifestId codeListValueManifestId) {
        CodeListValueDetailsRecord codeListValueDetails =
                codeListQueryRepository.getCodeListValueDetails(codeListValueManifestId);

        int numOfDeletedRecords = dslContext().deleteFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(codeListValueManifestId)))
                .execute();

        dslContext().deleteFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(valueOf(codeListValueDetails.codeListValueId())))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean updateLogId(CodeListManifestId codeListManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(CODE_LIST_MANIFEST)
                .set(CODE_LIST_MANIFEST.LOG_ID, valueOf(logId))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public Pair<CodeListManifestId, List<String>> uplift(
            ScoreUser requester, CodeListManifestId codeListManifestId, ReleaseId targetReleaseId) {

        List<String> duplicateValues = new ArrayList<>();
        CodeListDetailsRecord sourceCodeList = codeListQueryRepository.getCodeListDetails(codeListManifestId);

        CodeListManifestRecord targetCodeListManifest = new CodeListManifestRecord();
        targetCodeListManifest.setReleaseId(valueOf(targetReleaseId));

        /*
         * Issue #1283
         * Uplift an agency ID list manifest ID
         */
        if (sourceCodeList.agencyIdListValue() != null) {
            AgencyIdListValueSummaryRecord targetAgencyIdListValue =
                    agencyIdListQueryRepository.getAgencyIdListValueSummaryList(targetReleaseId)
                            .stream().filter(e -> e.guid().equals(sourceCodeList.agencyIdListValue().guid()))
                            .findAny().orElse(null);
            if (targetAgencyIdListValue != null) {
                targetCodeListManifest.setAgencyIdListValueManifestId(valueOf(targetAgencyIdListValue.agencyIdListValueManifestId()));
            }
        }

        CodeListRecord targetCodeList;

        // Issue #1073
        // If the source CL has the base CL, all CL values should be copying from the base again.
        if (sourceCodeList.based() != null) {
            CodeListDetailsRecord sourceBasedCodeList = codeListQueryRepository.getCodeListDetails(sourceCodeList.based().codeListManifestId());
            CodeListDetailsRecord targetBasedCodeList = sourceBasedCodeList;

            // Find a target code list manifest recursively
            while (targetBasedCodeList != null && !targetBasedCodeList.release().releaseId().equals(targetReleaseId)) {
                targetBasedCodeList = codeListQueryRepository.getCodeListDetails(targetBasedCodeList.nextCodeListManifestId());
            }

            if (targetBasedCodeList == null) {
                throw new IllegalStateException();
            }

            targetCodeList = copyCodeList(sourceCodeList);
            targetCodeList.setBasedCodeListId(valueOf(targetBasedCodeList.codeListId()));
            targetCodeList.setCodeListId(
                    dslContext().insertInto(CODE_LIST)
                            .set(targetCodeList)
                            .returning(CODE_LIST.CODE_LIST_ID)
                            .fetchOne().getCodeListId()
            );

            targetCodeListManifest.setCodeListId(targetCodeList.getCodeListId());
            targetCodeListManifest.setBasedCodeListManifestId(valueOf(targetBasedCodeList.codeListManifestId()));
            targetCodeListManifest.setCodeListManifestId(
                    dslContext().insertInto(Tables.CODE_LIST_MANIFEST)
                            .set(targetCodeListManifest)
                            .returning(Tables.CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                            .fetchOne().getCodeListManifestId()
            );

            // Use case-insensitive a value set to prevent duplicated CL values
            Set<String> basedCodeListValueSet = new HashSet();
            targetBasedCodeList.valueList().forEach(e -> {
                CodeListValueRecord targetCodeListValue = copyCodeListValue(e, targetCodeList);
                targetCodeListValue.setCodeListValueId(
                        dslContext().insertInto(CODE_LIST_VALUE)
                                .set(targetCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord targetCodeListValueManifest = new CodeListValueManifestRecord();
                targetCodeListValueManifest.setReleaseId(valueOf(targetReleaseId));
                targetCodeListValueManifest.setCodeListValueId(targetCodeListValue.getCodeListValueId());
                targetCodeListValueManifest.setCodeListManifestId(targetCodeListManifest.getCodeListManifestId());
                dslContext().insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(targetCodeListValueManifest)
                        .execute();

                basedCodeListValueSet.add(targetCodeListValue.getValue().toLowerCase());
            });

            sourceCodeList.valueList().stream().forEach(e -> {
                if (basedCodeListValueSet.contains(e.value().toLowerCase())) {
                    duplicateValues.add(e.value().toLowerCase());
                    return;
                }

                CodeListValueRecord targetCodeListValue = copyCodeListValue(e, targetCodeList);
                targetCodeListValue.setCodeListValueId(
                        dslContext().insertInto(CODE_LIST_VALUE)
                                .set(targetCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord targetCodeListValueManifest = new CodeListValueManifestRecord();
                targetCodeListValueManifest.setReleaseId(valueOf(targetReleaseId));
                targetCodeListValueManifest.setCodeListValueId(targetCodeListValue.getCodeListValueId());
                targetCodeListValueManifest.setCodeListManifestId(targetCodeListManifest.getCodeListManifestId());
                dslContext().insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(targetCodeListValueManifest)
                        .execute();
            });

            sourceCodeList.valueList().forEach(e -> {
                if (duplicateValues.indexOf(e.value().toLowerCase()) > -1) {
                    duplicateValues.remove(duplicateValues.indexOf(e.value().toLowerCase()));
                }
            });

        } else {
            targetCodeList = copyCodeList(sourceCodeList);
            targetCodeList.setCodeListId(
                    dslContext().insertInto(CODE_LIST)
                            .set(targetCodeList)
                            .returning(CODE_LIST.CODE_LIST_ID)
                            .fetchOne().getCodeListId()
            );

            targetCodeListManifest.setCodeListId(targetCodeList.getCodeListId());
            targetCodeListManifest.setCodeListManifestId(
                    dslContext().insertInto(Tables.CODE_LIST_MANIFEST)
                            .set(targetCodeListManifest)
                            .returning(Tables.CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                            .fetchOne().getCodeListManifestId()
            );

            sourceCodeList.valueList().stream().forEach(e -> {
                CodeListValueRecord targetCodeListValue = copyCodeListValue(e, targetCodeList);
                targetCodeListValue.setCodeListValueId(
                        dslContext().insertInto(CODE_LIST_VALUE)
                                .set(targetCodeListValue)
                                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                                .fetchOne().getCodeListValueId()
                );

                CodeListValueManifestRecord targetCodeListValueManifest = new CodeListValueManifestRecord();
                targetCodeListValueManifest.setReleaseId(valueOf(targetReleaseId));
                targetCodeListValueManifest.setCodeListValueId(targetCodeListValue.getCodeListValueId());
                targetCodeListValueManifest.setCodeListManifestId(targetCodeListManifest.getCodeListManifestId());
                dslContext().insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(targetCodeListValueManifest)
                        .execute();
            });
        }

        return Pair.of(new CodeListManifestId(targetCodeListManifest.getCodeListManifestId().toBigInteger()), duplicateValues);
    }

    private CodeListRecord copyCodeList(CodeListDetailsRecord codeList) {
        LocalDateTime timestamp = LocalDateTime.now();

        CodeListRecord newCodeList = new CodeListRecord();
        newCodeList.setGuid(ScoreGuidUtils.randomGuid());
        if (hasLength(codeList.enumTypeGuid())) {
            newCodeList.setEnumTypeGuid(ScoreGuidUtils.randomGuid());
        }
        if (codeList.based() != null) {
            newCodeList.setBasedCodeListId(valueOf(codeList.based().codeListId()));
        }
        newCodeList.setName(codeList.name());
        newCodeList.setListId(codeList.listId());
        newCodeList.setVersionId(codeList.versionId());
        newCodeList.setRemark(codeList.remark());
        if (codeList.definition() != null) {
            newCodeList.setDefinition(codeList.definition().content());
            newCodeList.setDefinitionSource(codeList.definition().source());
        }
        if (codeList.namespace() != null) {
            newCodeList.setNamespaceId(valueOf(codeList.namespace().namespaceId()));
        }
        newCodeList.setExtensibleIndicator((byte) (codeList.extensible() ? 1 : 0));
        // Test Assertion #33.2.7
        // After Uplifting, the "Deprecated" should be false.
        newCodeList.setIsDeprecated((byte) 0);
        newCodeList.setOwnerUserId(valueOf(requester().userId()));
        newCodeList.setCreatedBy(valueOf(requester().userId()));
        newCodeList.setLastUpdatedBy(valueOf(requester().userId()));
        newCodeList.setCreationTimestamp(timestamp);
        newCodeList.setLastUpdateTimestamp(timestamp);
        newCodeList.setState(CcState.WIP.name());
        return newCodeList;
    }

    private CodeListValueRecord copyCodeListValue(CodeListValueDetailsRecord codeListValue,
                                                  CodeListRecord newCodeList) {

        LocalDateTime timestamp = LocalDateTime.now();

        CodeListValueRecord newCodeListValue = new CodeListValueRecord();
        newCodeListValue.setGuid(ScoreGuidUtils.randomGuid());
        newCodeListValue.setCodeListId(newCodeList.getCodeListId());
        newCodeListValue.setValue(codeListValue.value());
        newCodeListValue.setMeaning(codeListValue.meaning());
        if (codeListValue.definition() != null) {
            newCodeListValue.setDefinition(codeListValue.definition().content());
            newCodeListValue.setDefinitionSource(codeListValue.definition().source());
        }
        newCodeListValue.setIsDeprecated((byte) (codeListValue.deprecated() ? 1 : 0));
        newCodeListValue.setOwnerUserId(valueOf(requester().userId()));
        newCodeListValue.setCreatedBy(valueOf(requester().userId()));
        newCodeListValue.setLastUpdatedBy(valueOf(requester().userId()));
        newCodeListValue.setCreationTimestamp(timestamp);
        newCodeListValue.setLastUpdateTimestamp(timestamp);
        return newCodeListValue;
    }

}
