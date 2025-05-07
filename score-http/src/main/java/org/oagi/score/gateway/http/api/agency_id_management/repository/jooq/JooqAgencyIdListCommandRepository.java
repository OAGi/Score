package org.oagi.score.gateway.http.api.agency_id_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AgencyIdListManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AgencyIdListRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AgencyIdListValueManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AgencyIdListValueRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Deleted;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.CodeListManifest.CODE_LIST_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAgencyIdListCommandRepository
        extends JooqBaseRepository
        implements AgencyIdListCommandRepository {

    private final AgencyIdListQueryRepository agencyIdListQueryRepository;

    private final LogCommandRepository logCommandRepository;

    public JooqAgencyIdListCommandRepository(DSLContext dslContext, ScoreUser requester,
                                             RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.agencyIdListQueryRepository = repositoryFactory.agencyIdListQueryRepository(requester);
        this.logCommandRepository = repositoryFactory.logCommandRepository(requester);
    }

    @Override
    public AgencyIdListManifestId create(
            ReleaseId releaseId, AgencyIdListManifestId basedAgencyIdListManifestId) {

        ULong requesterId = valueOf(requester().userId());
        LocalDateTime timestamp = LocalDateTime.now();
        AgencyIdListRecord agencyIdListRecord = new AgencyIdListRecord();
        AgencyIdListManifestRecord agencyIdListManifestRecord = new AgencyIdListManifestRecord();
        agencyIdListRecord.setGuid(ScoreGuidUtils.randomGuid());
        agencyIdListRecord.setEnumTypeGuid(ScoreGuidUtils.randomGuid());
        agencyIdListRecord.setListId(ScoreGuidUtils.randomGuid());
        agencyIdListRecord.setState(CcState.WIP.name());
        agencyIdListRecord.setOwnerUserId(requesterId);
        agencyIdListRecord.setCreatedBy(requesterId);
        agencyIdListRecord.setLastUpdatedBy(requesterId);
        agencyIdListRecord.setCreationTimestamp(timestamp);
        agencyIdListRecord.setLastUpdateTimestamp(timestamp);

        AgencyIdListDetailsRecord basedAgencyIdListDetails = null;
        if (basedAgencyIdListManifestId != null) {
            basedAgencyIdListDetails = agencyIdListQueryRepository.getAgencyIdListDetails(basedAgencyIdListManifestId);

            agencyIdListRecord.setName(basedAgencyIdListDetails.name());
            agencyIdListRecord.setVersionId(basedAgencyIdListDetails.versionId());
            agencyIdListRecord.setBasedAgencyIdListId(valueOf(basedAgencyIdListDetails.agencyIdListId()));

            agencyIdListManifestRecord.setReleaseId(valueOf(basedAgencyIdListDetails.release().releaseId()));
            agencyIdListManifestRecord.setBasedAgencyIdListManifestId(valueOf(basedAgencyIdListDetails.agencyIdListManifestId()));

        } else {
            agencyIdListRecord.setName("AgencyIdentification");
            agencyIdListManifestRecord.setReleaseId(valueOf(releaseId));
        }

        AgencyIdListId agencyIdListId = new AgencyIdListId(
                dslContext().insertInto(AGENCY_ID_LIST)
                        .set(agencyIdListRecord)
                        .returning(AGENCY_ID_LIST.AGENCY_ID_LIST_ID)
                        .fetchOne().getAgencyIdListId().toBigInteger());

        agencyIdListManifestRecord.setAgencyIdListId(valueOf(agencyIdListId));

        AgencyIdListManifestId agencyIdListManifestId = new AgencyIdListManifestId(
                dslContext().insertInto(AGENCY_ID_LIST_MANIFEST)
                        .set(agencyIdListManifestRecord)
                        .returning(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                        .fetchOne().getAgencyIdListManifestId().toBigInteger());

        if (basedAgencyIdListDetails != null) {
            for (AgencyIdListValueDetailsRecord basedAgencyIdListValueDetails : basedAgencyIdListDetails.valueList()) {
                AgencyIdListValueRecord agencyIdListValueRecord = new AgencyIdListValueRecord();
                agencyIdListValueRecord.setOwnerListId(valueOf(agencyIdListId));
                agencyIdListValueRecord.setGuid(ScoreGuidUtils.randomGuid());
                agencyIdListValueRecord.setBasedAgencyIdListValueId(valueOf(basedAgencyIdListValueDetails.agencyIdListValueId()));
                agencyIdListValueRecord.setValue(basedAgencyIdListValueDetails.value());
                agencyIdListValueRecord.setName(basedAgencyIdListValueDetails.name());
                if (basedAgencyIdListValueDetails.definition() != null) {
                    agencyIdListValueRecord.setDefinition(basedAgencyIdListValueDetails.definition().content());
                    agencyIdListValueRecord.setDefinitionSource(basedAgencyIdListValueDetails.definition().source());
                }
                agencyIdListValueRecord.setIsDeprecated((byte) (basedAgencyIdListValueDetails.deprecated() ? 1 : 0));
                agencyIdListValueRecord.setIsDeveloperDefault((byte) (basedAgencyIdListValueDetails.isDeveloperDefault() ? 1 : 0));
                agencyIdListValueRecord.setIsUserDefault((byte) (basedAgencyIdListValueDetails.isUserDefault() ? 1 : 0));
                agencyIdListValueRecord.setCreatedBy(requesterId);
                agencyIdListValueRecord.setLastUpdatedBy(requesterId);
                agencyIdListValueRecord.setOwnerUserId(requesterId);
                agencyIdListValueRecord.setCreationTimestamp(timestamp);
                agencyIdListValueRecord.setLastUpdateTimestamp(timestamp);
                agencyIdListValueRecord.setPrevAgencyIdListValueId(null);
                agencyIdListValueRecord.setNextAgencyIdListValueId(null);
                agencyIdListValueRecord.setAgencyIdListValueId(
                        dslContext().insertInto(AGENCY_ID_LIST_VALUE)
                                .set(agencyIdListValueRecord)
                                .returning(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID)
                                .fetchOne().getAgencyIdListValueId()
                );

                AgencyIdListValueManifestRecord agencyIdListValueManifestRecord = new AgencyIdListValueManifestRecord();
                agencyIdListValueManifestRecord.setReleaseId(valueOf(basedAgencyIdListDetails.release().releaseId()));
                agencyIdListValueManifestRecord.setAgencyIdListValueId(agencyIdListValueRecord.getAgencyIdListValueId());
                agencyIdListValueManifestRecord.setAgencyIdListManifestId(valueOf(agencyIdListManifestId));
                agencyIdListValueManifestRecord.setBasedAgencyIdListValueManifestId(valueOf(basedAgencyIdListValueDetails.agencyIdListValueManifestId()));
                agencyIdListValueManifestRecord.setPrevAgencyIdListValueManifestId(null);
                agencyIdListValueManifestRecord.setNextAgencyIdListValueManifestId(null);

                agencyIdListValueManifestRecord.setAgencyIdListValueManifestId(
                        dslContext().insertInto(AGENCY_ID_LIST_VALUE_MANIFEST)
                                .set(agencyIdListValueManifestRecord)
                                .returning(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).fetchOne().getAgencyIdListValueManifestId()
                );

                if (basedAgencyIdListDetails.agencyIdListValue() != null &&
                        basedAgencyIdListDetails.agencyIdListValue().agencyIdListValueManifestId().equals(
                                basedAgencyIdListValueDetails.agencyIdListValueManifestId())) {
                    dslContext().update(AGENCY_ID_LIST)
                            .set(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID, agencyIdListValueRecord.getAgencyIdListValueId())
                            .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(valueOf(agencyIdListId)))
                            .execute();

                    dslContext().update(AGENCY_ID_LIST_MANIFEST)
                            .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, agencyIdListValueManifestRecord.getAgencyIdListValueManifestId())
                            .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                            .execute();
                }
            }
        }

        return agencyIdListManifestId;
    }

    @Override
    public boolean update(AgencyIdListManifestId agencyIdListManifestId,
                          String name, String versionId, String listId,
                          AgencyIdListValueManifestId agencyIdListValueManifestId,
                          Definition definition, String remark,
                          NamespaceId namespaceId,
                          Boolean deprecated) {

        AgencyIdListDetailsRecord agencyIdListDetails =
                agencyIdListQueryRepository.getAgencyIdListDetails(agencyIdListManifestId);
        if (agencyIdListDetails == null) {
            return false;
        }

        AgencyIdListValueDetailsRecord agencyIdListValueDetails = null;
        if (agencyIdListValueManifestId != null) {
            agencyIdListValueDetails = agencyIdListQueryRepository.getAgencyIdListValueDetails(agencyIdListValueManifestId);

            dslContext().update(AGENCY_ID_LIST_MANIFEST)
                    .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, valueOf(agencyIdListValueManifestId))
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                    .execute();
        }

        var step = dslContext().update(AGENCY_ID_LIST);
        UpdateSetMoreStep more = null;
        if (hasLength(name)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.NAME, name);
        }
        if (hasLength(versionId)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.VERSION_ID, versionId);
        }
        if (hasLength(listId)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.LIST_ID, listId);
        }
        if (agencyIdListValueDetails != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID, valueOf(agencyIdListValueDetails.agencyIdListValueId()));
        }
        if (definition != null) {
            if (hasLength(definition.content())) {
                more = ((more != null) ? more : step).set(AGENCY_ID_LIST.DEFINITION, definition.content());
            }
            if (hasLength(definition.source())) {
                more = ((more != null) ? more : step).set(AGENCY_ID_LIST.DEFINITION_SOURCE, definition.source());
            }
        }
        if (hasLength(remark)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.REMARK, remark);
        }
        if (namespaceId != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.NAMESPACE_ID, valueOf(namespaceId));
        }
        if (deprecated != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }

        if (more != null) {
            int numOfUpdatedRecords = more.set(AGENCY_ID_LIST.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(valueOf(agencyIdListDetails.agencyIdListId())))
                    .execute();
            return numOfUpdatedRecords == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateState(AgencyIdListManifestId agencyIdListManifestId, CcState nextState) {

        AgencyIdListDetailsRecord agencyIdListDetails =
                agencyIdListQueryRepository.getAgencyIdListDetails(agencyIdListManifestId);
        if (agencyIdListDetails == null) {
            return false;
        }

        LocalDateTime timestamp = LocalDateTime.now();
        ULong userId = valueOf(requester().userId());

        CcState prevState = agencyIdListDetails.state();
        UpdateSetMoreStep<AgencyIdListRecord> step = dslContext().update(AGENCY_ID_LIST)
                .set(AGENCY_ID_LIST.STATE, nextState.name());
        if (!prevState.isImplicitMove(nextState)) {
            step = step.set(AGENCY_ID_LIST.LAST_UPDATED_BY, userId)
                    .set(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, timestamp);
        }
        if (prevState == Deleted) {
            step = step.set(AGENCY_ID_LIST.OWNER_USER_ID, userId);
        }
        int numOfUpdatedRecords =
                step.where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(valueOf(agencyIdListDetails.agencyIdListId())))
                        .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, AgencyIdListManifestId agencyIdListManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("`targetUser` must not be null.");
        }

        if (agencyIdListManifestId == null) {
            throw new IllegalArgumentException("`agencyIdListManifestId` must not be null.");
        }

        LocalDateTime timestamp = LocalDateTime.now();

        AgencyIdListSummaryRecord agencyIdList =
                agencyIdListQueryRepository.getAgencyIdListSummary(agencyIdListManifestId);

        int numOfUpdatedRecords = dslContext().update(AGENCY_ID_LIST)
                .set(AGENCY_ID_LIST.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(AGENCY_ID_LIST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(valueOf(agencyIdList.agencyIdListId())))
                .execute();
        if (numOfUpdatedRecords < 1) {
            return false;
        }

        dslContext().update(AGENCY_ID_LIST_VALUE)
                .set(AGENCY_ID_LIST_VALUE.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(AGENCY_ID_LIST_VALUE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(AGENCY_ID_LIST_VALUE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.in(
                        valueOf(agencyIdList.valueList().stream()
                                .map(e -> e.agencyIdListValueId()).collect(Collectors.toSet()))
                ))
                .execute();

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(AgencyIdListManifestId agencyIdListManifestId) {

        AgencyIdListDetailsRecord agencyIdListDetails =
                agencyIdListQueryRepository.getAgencyIdListDetails(agencyIdListManifestId);
        if (agencyIdListDetails == null) {
            return false;
        }

        if (!agencyIdListDetails.valueList().isEmpty()) {
            // Remove foreign key references.
            dslContext().update(AGENCY_ID_LIST_MANIFEST)
                    .setNull(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(
                            agencyIdListDetails.valueList().stream()
                                    .map(e -> valueOf(e.agencyIdListValueManifestId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();

            dslContext().update(CODE_LIST_MANIFEST)
                    .setNull(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                    .where(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(
                            agencyIdListDetails.valueList().stream()
                                    .map(e -> valueOf(e.agencyIdListValueManifestId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();

            dslContext().update(AGENCY_ID_LIST)
                    .setNull(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID)
                    .where(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID.in(
                            agencyIdListDetails.valueList().stream()
                                    .map(e -> valueOf(e.agencyIdListValueId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();

            // Delete records.
            dslContext().deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(
                            agencyIdListDetails.valueList().stream()
                                    .map(e -> valueOf(e.agencyIdListValueManifestId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();
            dslContext().deleteFrom(AGENCY_ID_LIST_VALUE)
                    .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.in(
                            agencyIdListDetails.valueList().stream()
                                    .map(e -> valueOf(e.agencyIdListValueId()))
                                    .collect(Collectors.toSet())
                    ))
                    .execute();
        }

        dslContext().deleteFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListDetails.agencyIdListManifestId())))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(valueOf(agencyIdListDetails.agencyIdListId())))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public void revise(AgencyIdListManifestId agencyIdListManifestId) {
        LocalDateTime timestamp = LocalDateTime.now();

        AgencyIdListManifestRecord currentAgencyIdListManifestRecord = dslContext().selectFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOne();

        AgencyIdListRecord currentAgencyIdListRecord = dslContext().select(AGENCY_ID_LIST.fields())
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOneInto(AgencyIdListRecord.class);

        AgencyIdListRecord nextAgencyIdListRecord = currentAgencyIdListRecord.copy();
        nextAgencyIdListRecord.setAgencyIdListId(null);
        nextAgencyIdListRecord.setState(CcState.WIP.name());
        nextAgencyIdListRecord.setVersionId(nextAgencyIdListRecord.getVersionId());
        nextAgencyIdListRecord.setCreatedBy(valueOf(requester().userId()));
        nextAgencyIdListRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextAgencyIdListRecord.setOwnerUserId(valueOf(requester().userId()));
        nextAgencyIdListRecord.setCreationTimestamp(timestamp);
        nextAgencyIdListRecord.setLastUpdateTimestamp(timestamp);
        nextAgencyIdListRecord.setPrevAgencyIdListId(currentAgencyIdListRecord.getAgencyIdListId());
        nextAgencyIdListRecord.setAgencyIdListId(
                dslContext().insertInto(AGENCY_ID_LIST)
                        .set(nextAgencyIdListRecord)
                        .returning(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).fetchOne().getAgencyIdListId()
        );

        currentAgencyIdListRecord.setNextAgencyIdListId(nextAgencyIdListRecord.getAgencyIdListId());
        currentAgencyIdListRecord.update(AGENCY_ID_LIST.NEXT_AGENCY_ID_LIST_ID);

        createNewAgencyIdListValueForRevisedRecord(agencyIdListManifestId, nextAgencyIdListRecord, timestamp);

        currentAgencyIdListManifestRecord.setAgencyIdListId(nextAgencyIdListRecord.getAgencyIdListId());
        currentAgencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID);

        if (currentAgencyIdListManifestRecord.getAgencyIdListValueManifestId() != null) {
            nextAgencyIdListRecord.setAgencyIdListValueId(dslContext().select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)
                    .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(currentAgencyIdListManifestRecord.getAgencyIdListValueManifestId()))
                    .fetchOneInto(ULong.class));
            nextAgencyIdListRecord.update(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID);
        }
    }

    private void createNewAgencyIdListValueForRevisedRecord(
            AgencyIdListManifestId agencyIdListManifestId,
            AgencyIdListRecord nextAgencyIdListRecord,
            LocalDateTime timestamp) {

        for (AgencyIdListValueManifestRecord agencyIdListValueManifestRecord : dslContext().selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetch()) {

            AgencyIdListValueRecord prevAgencyIdListValueRecord = dslContext().selectFrom(AGENCY_ID_LIST_VALUE)
                    .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValueManifestRecord.getAgencyIdListValueId()))
                    .fetchOne();

            AgencyIdListValueRecord nextAgencyIdListValueRecord = prevAgencyIdListValueRecord.copy();
            nextAgencyIdListValueRecord.setAgencyIdListValueId(null);
            nextAgencyIdListValueRecord.setOwnerListId(nextAgencyIdListRecord.getAgencyIdListId());
            nextAgencyIdListValueRecord.setCreatedBy(valueOf(requester().userId()));
            nextAgencyIdListValueRecord.setLastUpdatedBy(valueOf(requester().userId()));
            nextAgencyIdListValueRecord.setOwnerUserId(valueOf(requester().userId()));
            nextAgencyIdListValueRecord.setCreationTimestamp(timestamp);
            nextAgencyIdListValueRecord.setLastUpdateTimestamp(timestamp);
            nextAgencyIdListValueRecord.setPrevAgencyIdListValueId(prevAgencyIdListValueRecord.getAgencyIdListValueId());
            nextAgencyIdListValueRecord.setAgencyIdListValueId(
                    dslContext().insertInto(AGENCY_ID_LIST_VALUE)
                            .set(nextAgencyIdListValueRecord)
                            .returning(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID).fetchOne().getAgencyIdListValueId()
            );

            prevAgencyIdListValueRecord.setNextAgencyIdListValueId(nextAgencyIdListValueRecord.getAgencyIdListValueId());
            prevAgencyIdListValueRecord.update(AGENCY_ID_LIST_VALUE.NEXT_AGENCY_ID_LIST_VALUE_ID);

            agencyIdListValueManifestRecord.setAgencyIdListValueId(nextAgencyIdListValueRecord.getAgencyIdListValueId());
            agencyIdListValueManifestRecord.update(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID);
        }
    }

    @Override
    public void cancel(AgencyIdListManifestId agencyIdListManifestId) {
        AgencyIdListManifestRecord agencyIdListManifestRecord = dslContext().selectFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOne();

        AgencyIdListRecord agencyIdListRecord = dslContext().selectFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListManifestRecord.getAgencyIdListId())).fetchOne();

        AgencyIdListRecord prevAgencyIdListRecord = dslContext().selectFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListRecord.getPrevAgencyIdListId())).fetchOne();

        // update AGENCY ID LIST MANIFEST's agencyIdList_id and revision_id
        agencyIdListManifestRecord.setAgencyIdListId(agencyIdListRecord.getPrevAgencyIdListId());
        agencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID);

        agencyIdListRecord.setAgencyIdListValueId(null);
        agencyIdListRecord.update(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID);

        discardLogAgencyIdListValues(agencyIdListManifestRecord);

        // unlink prev AGENCY_ID_LIST
        prevAgencyIdListRecord.setNextAgencyIdListId(null);
        prevAgencyIdListRecord.update(AGENCY_ID_LIST.NEXT_AGENCY_ID_LIST_ID);

        agencyIdListManifestRecord.setLogId(null);
        agencyIdListManifestRecord.update(AGENCY_ID_LIST_MANIFEST.LOG_ID);

        if (prevAgencyIdListRecord.getAgencyIdListValueId() != null) {
            agencyIdListManifestRecord.setAgencyIdListValueManifestId(
                    dslContext().select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                            .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                            .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(agencyIdListManifestRecord.getReleaseId()),
                                    AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(prevAgencyIdListRecord.getAgencyIdListValueId())
                            )).fetchOneInto(ULong.class));
        }

        // delete current AGENCY_ID_LIST
        agencyIdListRecord.delete();
    }

    private void discardLogAgencyIdListValues(AgencyIdListManifestRecord agencyIdListManifestRecord) {
        List<AgencyIdListValueManifestRecord> agencyIdListValueManifests = dslContext().selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListManifestRecord.getAgencyIdListManifestId()))
                .fetch();

        for (AgencyIdListValueManifestRecord agencyIdListValueManifest : agencyIdListValueManifests) {
            AgencyIdListValueRecord agencyIdListValue = dslContext().selectFrom(AGENCY_ID_LIST_VALUE)
                    .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValueManifest.getAgencyIdListValueId()))
                    .fetchOne();

            if (agencyIdListValue.getPrevAgencyIdListValueId() == null) {
                // delete code list value and code list manifest which added this revision
                agencyIdListValueManifest.delete();
                agencyIdListValue.delete();
            } else {
                AgencyIdListValueRecord prevAgencyIdListValue = dslContext().selectFrom(AGENCY_ID_LIST_VALUE)
                        .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValue.getPrevAgencyIdListValueId()))
                        .fetchOne();

                prevAgencyIdListValue.setNextAgencyIdListValueId(null);
                prevAgencyIdListValue.update(AGENCY_ID_LIST_VALUE.NEXT_AGENCY_ID_LIST_VALUE_ID);
                agencyIdListValueManifest.setAgencyIdListValueId(prevAgencyIdListValue.getAgencyIdListValueId());
                agencyIdListValueManifest.update(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID);
                agencyIdListValue.delete();
            }
        }
    }

    @Override
    public AgencyIdListValueManifestId createValue(
            AgencyIdListManifestId agencyIdListManifestId,
            AgencyIdListId agencyIdListId,
            ReleaseId releaseId,
            String value, String name,
            Definition definition) {

        LocalDateTime timestamp = LocalDateTime.now();

        AgencyIdListValueRecord agencyIdListValueRecord = new AgencyIdListValueRecord();
        agencyIdListValueRecord.setOwnerListId(valueOf(agencyIdListId));
        agencyIdListValueRecord.setGuid(ScoreGuidUtils.randomGuid());
        agencyIdListValueRecord.setValue(value);
        agencyIdListValueRecord.setName(name);
        if (definition != null) {
            agencyIdListValueRecord.setDefinition(definition.content());
            agencyIdListValueRecord.setDefinitionSource(definition.source());
        }
        agencyIdListValueRecord.setCreatedBy(valueOf(requester().userId()));
        agencyIdListValueRecord.setOwnerUserId(valueOf(requester().userId()));
        agencyIdListValueRecord.setLastUpdatedBy(valueOf(requester().userId()));
        agencyIdListValueRecord.setCreationTimestamp(timestamp);
        agencyIdListValueRecord.setLastUpdateTimestamp(timestamp);
        agencyIdListValueRecord.setIsDeprecated((byte) 0);
        agencyIdListValueRecord.setIsDeveloperDefault((byte) 0);
        agencyIdListValueRecord.setIsUserDefault((byte) 0);

        AgencyIdListValueId agencyIdListValueId = new AgencyIdListValueId(
                dslContext().insertInto(AGENCY_ID_LIST_VALUE)
                        .set(agencyIdListValueRecord)
                        .returning(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID)
                        .fetchOne().getAgencyIdListValueId().toBigInteger());

        AgencyIdListValueManifestRecord agencyIdListValueManifestRecord = new AgencyIdListValueManifestRecord();

        agencyIdListValueManifestRecord.setReleaseId(valueOf(releaseId));
        agencyIdListValueManifestRecord.setAgencyIdListValueId(valueOf(agencyIdListValueId));
        agencyIdListValueManifestRecord.setAgencyIdListManifestId(valueOf(agencyIdListManifestId));

        return new AgencyIdListValueManifestId(
                dslContext().insertInto(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .set(agencyIdListValueManifestRecord)
                        .returning(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .fetchOne().getAgencyIdListValueManifestId().toBigInteger()
        );
    }

    @Override
    public boolean updateValue(
            AgencyIdListValueManifestId agencyIdListValueManifestId,
            String value, String name,
            Definition definition,
            Boolean deprecated,
            Boolean isDeveloperDefault, Boolean isUserDefault) {

        AgencyIdListValueDetailsRecord agencyIdListValueDetails =
                agencyIdListQueryRepository.getAgencyIdListValueDetails(agencyIdListValueManifestId);

        var step = dslContext().update(AGENCY_ID_LIST_VALUE);
        UpdateSetMoreStep more = null;
        if (hasLength(value)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.VALUE, value);
        }
        if (hasLength(name)) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.NAME, name);
        }
        if (definition != null) {
            if (hasLength(definition.content())) {
                more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.DEFINITION, definition.content());
            }
            if (hasLength(definition.source())) {
                more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE, definition.source());
            }
        }
        if (deprecated != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (isDeveloperDefault != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.IS_DEVELOPER_DEFAULT, (byte) (isDeveloperDefault ? 1 : 0));
        }
        if (isUserDefault != null) {
            more = ((more != null) ? more : step).set(AGENCY_ID_LIST_VALUE.IS_USER_DEFAULT, (byte) (isUserDefault ? 1 : 0));
        }

        if (more != null) {
            int numOfUpdatedRecords = more.set(AGENCY_ID_LIST_VALUE.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(AGENCY_ID_LIST_VALUE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(valueOf(agencyIdListValueDetails.agencyIdListValueId())))
                    .execute();
            return numOfUpdatedRecords == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteValue(AgencyIdListValueManifestId agencyIdListValueManifestId) {
        AgencyIdListValueDetailsRecord agencyIdListValueDetails =
                agencyIdListQueryRepository.getAgencyIdListValueDetails(agencyIdListValueManifestId);

        int numOfDeletedRecords = dslContext().deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)))
                .execute();

        dslContext().deleteFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(valueOf(agencyIdListValueDetails.agencyIdListValueId())))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean updateLogId(AgencyIdListManifestId agencyIdListManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(AGENCY_ID_LIST_MANIFEST)
                .set(AGENCY_ID_LIST_MANIFEST.LOG_ID, valueOf(logId))
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

}
