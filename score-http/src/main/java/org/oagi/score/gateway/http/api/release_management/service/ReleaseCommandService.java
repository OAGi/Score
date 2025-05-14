package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListCommandService;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcCommandService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.controller.payload.*;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCreateRequestEvent;
import org.oagi.score.gateway.http.api.tag_management.model.AccManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.AsccpManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.BccpManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.DtManifestTagSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Candidate;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.ReleaseDraft;
import static org.oagi.score.gateway.http.api.release_management.model.ReleaseState.*;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;

/**
 * Service class for managing releases, including creation, updating, and deletion.
 */
@Service
@Transactional
public class ReleaseCommandService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private CcCommandService ccCommandService;

    @Autowired
    private CodeListCommandService codeListCommandService;

    @Autowired
    private AgencyIdListCommandService agencyIdListCommandService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private final String RELEASE_CREATE_REQUEST_EVENT = "releaseCreateRequestEvent";
    private final String RELEASE_CLEANUP_EVENT = "releaseCleanupEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onReleaseCreateRequestEventReceived",
                new ChannelTopic(RELEASE_CREATE_REQUEST_EVENT));
        eventListenerContainer.addMessageListener(this, "onReleaseCleanupEventReceived",
                new ChannelTopic(RELEASE_CLEANUP_EVENT));
    }

    /**
     * Creates a new release if the release number is not already in use.
     *
     * @param requester The user requesting the release creation.
     * @param request   The details of the release to be created.
     * @return The ID of the newly created release.
     * @throws IllegalArgumentException if a release with the same release number already exists.
     */
    public ReleaseId create(ScoreUser requester, CreateReleaseRequest request) {
        var query = repositoryFactory.releaseQueryRepository(requester);
        if (query.hasDuplicateReleaseNumber(request.libraryId(), request.releaseNum())) {
            throw new IllegalArgumentException("'" + request.releaseNum() + "' already exists.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        return command.create(
                request.libraryId(),
                request.namespaceId(),
                request.releaseNum(),
                request.releaseNote(),
                request.releaseLicense());
    }

    /**
     * Updates an existing release.
     *
     * @param requester The user requesting the update.
     * @param request   The details of the release update.
     * @throws IllegalArgumentException if the release does not exist or if the release number is duplicated.
     */
    @Transactional
    public void update(ScoreUser requester, UpdateReleaseRequest request) {

        if (request.releaseId() == null) {
            throw new IllegalArgumentException("'releaseId' is required.");
        }

        var query = repositoryFactory.releaseQueryRepository(requester);

        if (!query.exists(request.releaseId())) {
            throw new IllegalArgumentException("'" + request.releaseId() + "' does not exist.");
        }
        if (query.hasDuplicateReleaseNumberExcludingCurrent(request.releaseId(), request.releaseNum())) {
            throw new IllegalArgumentException("'" + request.releaseNum() + "' already exists.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.update(
                request.releaseId(),
                request.namespaceId(),
                request.releaseNum(),
                request.releaseNote(),
                request.releaseLicense());
    }

    /**
     * Discards (deletes) a release if it meets the required conditions.
     *
     * @param requester The user requesting the deletion.
     * @param releaseId The ID of the release to be discarded.
     * @throws IllegalArgumentException if the release does not exist, is a working release, is not in the initialized state,
     *                                  or if the user lacks the required permissions.
     */
    public void discard(ScoreUser requester, ReleaseId releaseId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("'releaseId' is required.");
        }

        var query = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord releaseSummary = query.getReleaseSummary(releaseId);
        if (releaseSummary == null) {
            throw new IllegalArgumentException("Release with ID '" + releaseId + "' does not exist.");
        }
        if (releaseSummary.isWorkingRelease()) {
            throw new IllegalArgumentException("'" + releaseSummary.releaseNum() + "' release cannot be discarded.");
        }
        if (Initialized != releaseSummary.state()) {
            throw new IllegalArgumentException("Only the release in '" + Initialized + "' can be discarded.");
        }
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("Only users with the '" + DEVELOPER + "' role can discard a release.");
        }
        var moduleSetReleaseQuery = repositoryFactory.moduleSetReleaseQueryRepository(requester);
        if (moduleSetReleaseQuery.exists(releaseId)) {
            throw new IllegalArgumentException("The release '" + releaseSummary.releaseNum() + "' cannot be discarded due to dependent module set releases.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.delete(releaseId);
    }

    /**
     * Discards multiple releases in batch.
     *
     * @param requester  The user requesting the deletion.
     * @param releaseIds The collection of release IDs to be discarded.
     */
    public void discard(ScoreUser requester, Collection<ReleaseId> releaseIds) {

        for (ReleaseId releaseId : releaseIds) {
            discard(requester, releaseId);
        }
    }

    public void transitState(ScoreUser requester,
                             TransitStateRequest request) {

        ReleaseState requestState = ReleaseState.valueOf(request.getState());
        if (requestState == Published && !requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can publish the release.");
        }

        var query = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord release = query.getReleaseSummary(request.getReleaseId());

        CcState fromCcState = null;
        CcState toCcState = null;

        switch (release.state()) {
            case Initialized:
                if (requestState != Draft) {
                    throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot transit to '" + requestState + "' state.");
                }

                requestState = Processing;
                fromCcState = Candidate;
                toCcState = ReleaseDraft;
                break;

            case Draft:
                if (requestState != Initialized && requestState != Published) {
                    throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot transit to '" + requestState + "' state.");
                }

                if (requestState == Initialized) {
                    fromCcState = ReleaseDraft;
                    toCcState = Candidate;
                } else if (requestState == Published) {
                    requestState = Processing;
                    fromCcState = ReleaseDraft;
                    toCcState = CcState.Published;
                }

                break;

            case Processing:
            case Published:
                throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot be transited.");
        }

        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException("It only allows to modify the release by the developer.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.updateState(request.getReleaseId(), requestState);

        LocalDateTime timestamp = LocalDateTime.now();

        // update CCs' states by transited release state.
        if (fromCcState != null && toCcState != null) {
            if (toCcState == ReleaseDraft) {
                ReleaseValidationRequest validationRequest = request.getValidationRequest();
                for (AccManifestId accManifestId : validationRequest.getAssignedAccComponentManifestIds()) {
                    ccCommandService.updateState(requester, accManifestId, toCcState);
                }
                for (AsccpManifestId asccpManifestId : validationRequest.getAssignedAsccpComponentManifestIds()) {
                    ccCommandService.updateState(requester, asccpManifestId, toCcState);
                }
                for (BccpManifestId bccpManifestId : validationRequest.getAssignedBccpComponentManifestIds()) {
                    ccCommandService.updateState(requester, bccpManifestId, toCcState);
                }
                for (CodeListManifestId codeListManifestId : validationRequest.getAssignedCodeListComponentManifestIds()) {
                    codeListCommandService.updateState(requester, codeListManifestId, toCcState);
                }
                for (AgencyIdListManifestId agencyIdListManifestId : validationRequest.getAssignedAgencyIdListComponentManifestIds()) {
                    agencyIdListCommandService.updateState(requester, agencyIdListManifestId, toCcState);
                }
                for (DtManifestId dtManifestId : validationRequest.getAssignedDtComponentManifestIds()) {
                    ccCommandService.updateState(requester, dtManifestId, toCcState);
                }
            } else if (toCcState == Candidate) {
                updateCCStates(requester, release, fromCcState, toCcState, timestamp);

                // Remove release dependencies
                command.deleteDeps(request.getReleaseId());

                // Remove module set releases
                var moduleSetReleaseQuery = repositoryFactory.moduleSetReleaseQueryRepository(requester);
                List<ModuleSetReleaseSummaryRecord> moduleSetReleaseList =
                        moduleSetReleaseQuery.getModuleSetReleaseSummaryList(release.releaseId());

                if (!moduleSetReleaseList.isEmpty()) {
                    var moduleManifestCommand = repositoryFactory.moduleManifestCommandRepository(requester);
                    moduleManifestCommand.deleteModuleManifests(
                            moduleSetReleaseList.stream().map(e -> e.moduleSetReleaseId()).collect(Collectors.toSet())
                    );
                }

                // Remove tags.
                {
                    var tagQuery = repositoryFactory.tagQueryRepository(requester);
                    var tagCommand = repositoryFactory.tagCommandRepository(requester);

                    for (AccManifestTagSummaryRecord accManifestTag : tagQuery.getAccManifestTagList(Arrays.asList(release.releaseId()))) {
                        tagCommand.removeTag(accManifestTag.tagId(), accManifestTag.accManifestId());
                    }
                    for (AsccpManifestTagSummaryRecord asccpManifestTag : tagQuery.getAsccpManifestTagList(Arrays.asList(release.releaseId()))) {
                        tagCommand.removeTag(asccpManifestTag.tagId(), asccpManifestTag.asccpManifestId());
                    }
                    for (BccpManifestTagSummaryRecord bccpManifestTag : tagQuery.getBccpManifestTagList(Arrays.asList(release.releaseId()))) {
                        tagCommand.removeTag(bccpManifestTag.tagId(), bccpManifestTag.bccpManifestId());
                    }
                    for (DtManifestTagSummaryRecord dtManifestTag : tagQuery.getDtManifestTagList(Arrays.asList(release.releaseId()))) {
                        tagCommand.removeTag(dtManifestTag.tagId(), dtManifestTag.dtManifestId());
                    }
                }

                var ccCommand = repositoryFactory.ccCommandRepository(requester);

                // Remove replacement
                ccCommand.clearReplacement(release.releaseId());

                // Remove CCs
                ccCommand.delete(release.releaseId());
            } else if (toCcState == CcState.Published) {
                updateCCStates(requester, release, fromCcState, toCcState, timestamp);

                // fire the create release draft event.
                ReleaseCleanupEvent releaseCleanupEvent = new ReleaseCleanupEvent(
                        requester.userId(), request.getReleaseId());

                /*
                 * Message Publishing
                 */
                redisTemplate.convertAndSend(RELEASE_CLEANUP_EVENT, releaseCleanupEvent);
            }
        }
    }

    private void updateCCStates(ScoreUser requester, ReleaseSummaryRecord release,
                                CcState fromCcState, CcState toCcState, LocalDateTime timestamp) {
        var accQuery = repositoryFactory.accQueryRepository(requester);
        for (var acc : accQuery.getAccSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, acc.accManifestId(), toCcState);
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        for (var asccp : asccpQuery.getAsccpSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, asccp.asccpManifestId(), toCcState);
        }

        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        for (var bccp : bccpQuery.getBccpSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, bccp.bccpManifestId(), toCcState);
        }

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        for (var codeList : codeListQuery.getCodeListSummaryList(release.libraryId(), "Working", fromCcState)) {
            codeListCommandService.updateState(requester, codeList.codeListManifestId(), toCcState);
        }

        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);
        for (var agencyIdList : agencyIdListQuery.getAgencyIdListSummaryList(release.libraryId(), "Working", fromCcState)) {
            agencyIdListCommandService.updateState(requester, agencyIdList.agencyIdListManifestId(), toCcState);
        }

        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        for (var dt : dtQuery.getDtSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, dt.dtManifestId(), toCcState);
        }
    }

    public ReleaseValidationResponse validate(ScoreUser requester,
                                              ReleaseValidationRequest request) {

        ReleaseValidator validator = new ReleaseValidator(requester, request.getReleaseId(), repositoryFactory);
        validator.setAssignedAccComponentManifestIds(request.getAssignedAccComponentManifestIds());
        validator.setAssignedAsccpComponentManifestIds(request.getAssignedAsccpComponentManifestIds());
        validator.setAssignedBccpComponentManifestIds(request.getAssignedBccpComponentManifestIds());
        validator.setAssignedCodeListComponentManifestIds(request.getAssignedCodeListComponentManifestIds());
        validator.setAssignedAgencyIdListComponentManifestIds(request.getAssignedAgencyIdListComponentManifestIds());
        validator.setAssignedDtComponentManifestIds(request.getAssignedDtComponentManifestIds());
        return validator.validate();
    }

    private boolean isReleaseInAnyOfStates(ScoreUser requester, ReleaseId releaseId, ReleaseState... states) {
        if (states == null || states.length == 0) {
            return false;
        }

        var query = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord releaseSummary = query.getReleaseSummary(releaseId);
        if (releaseSummary == null) {
            return false;
        }

        return Arrays.asList(states).contains(releaseSummary.state());
    }

    @Transactional
    public ReleaseValidationResponse createDraft(ScoreUser requester,
                                                 ReleaseValidationRequest request) {
        ReleaseId releaseId = request.getReleaseId();
        if (isReleaseInAnyOfStates(requester, releaseId, Draft, Processing)) {
            throw new IllegalArgumentException("It cannot make any release to 'Draft' due to a release restriction.");
        }

        ReleaseValidationResponse response = this.validate(requester, request);
        if (response.isSucceed()) {
            // update state to 'Release Draft' for assigned CCs
            TransitStateRequest transitStateRequest = new TransitStateRequest();
            transitStateRequest.setReleaseId(releaseId);
            transitStateRequest.setState(Draft.name());
            transitStateRequest.setValidationRequest(request);

            this.transitState(requester, transitStateRequest);

            // fire the create release draft event.
            ReleaseCreateRequestEvent releaseCreateRequestEvent = new ReleaseCreateRequestEvent(
                    requester.userId(),
                    releaseId,
                    request.getAssignedAccComponentManifestIds(),
                    request.getAssignedAsccpComponentManifestIds(),
                    request.getAssignedBccpComponentManifestIds(),
                    request.getAssignedDtComponentManifestIds(),
                    request.getAssignedCodeListComponentManifestIds(),
                    request.getAssignedAgencyIdListComponentManifestIds());

            /*
             * Message Publishing
             */
            redisTemplate.convertAndSend(RELEASE_CREATE_REQUEST_EVENT, releaseCreateRequestEvent);

            response.clearWarnings();
        }

        return response;
    }

    /**
     * This method is invoked by 'releaseCreateRequestEvent' channel subscriber.
     *
     * @param releaseCreateRequestEvent
     */
    @Transactional
    public void onReleaseCreateRequestEventReceived(ReleaseCreateRequestEvent releaseCreateRequestEvent) {
        RLock lock = redissonClient.getLock("ReleaseCreateRequestEvent:" + releaseCreateRequestEvent.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received ReleaseCreateRequestEvent: " + releaseCreateRequestEvent);
            ScoreUser requester = sessionService.getScoreUserByUserId(releaseCreateRequestEvent.getUserId());

            copyWorkingManifests(requester,
                    releaseCreateRequestEvent.getReleaseId(),
                    releaseCreateRequestEvent.getAccManifestIds(),
                    releaseCreateRequestEvent.getAsccpManifestIds(),
                    releaseCreateRequestEvent.getBccpManifestIds(),
                    releaseCreateRequestEvent.getDtManifestIds(),
                    releaseCreateRequestEvent.getCodeListManifestIds(),
                    releaseCreateRequestEvent.getAgencyIdListManifestIds()
            );

            updateState(sessionService.getScoreUserByUserId(releaseCreateRequestEvent.getUserId()),
                    releaseCreateRequestEvent.getReleaseId(), Draft);
        } finally {
            lock.unlock();
        }
    }

    private void copyWorkingManifests(
            ScoreUser requester, ReleaseId releaseId,
            List<AccManifestId> accManifestIds,
            List<AsccpManifestId> asccpManifestIds,
            List<BccpManifestId> bccpManifestIds,
            List<DtManifestId> dtManifestIds,
            List<CodeListManifestId> codeListManifestIds,
            List<AgencyIdListManifestId> agencyIdListManifestIds) {

        var query = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord release = query.getReleaseSummary(releaseId);
        ReleaseSummaryRecord workingRelease = query.getReleaseSummary(release.libraryId(), "Working");

        if (workingRelease == null) {
            throw new IllegalStateException("Cannot find 'Working' release");
        }

        try {
            // copying manifests from 'Working' release
            ReleaseId workingReleaseId = workingRelease.releaseId();

            repositoryFactory.ccCommandRepository(requester)
                    .copyWorkingManifests(releaseId, workingReleaseId,
                            accManifestIds,
                            asccpManifestIds,
                            bccpManifestIds,
                            dtManifestIds,
                            codeListManifestIds,
                            agencyIdListManifestIds);

            repositoryFactory.releaseCommandRepository(requester)
                    .copyDepsFromWorking(releaseId, workingReleaseId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method is invoked by 'releaseCleanupEvent' channel subscriber.
     *
     * @param releaseCleanupEvent
     */
    @Transactional
    public void onReleaseCleanupEventReceived(ReleaseCleanupEvent releaseCleanupEvent) {
        RLock lock = redissonClient.getLock("ReleaseCleanupEvent:" + releaseCleanupEvent.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received ReleaseCleanupEvent: " + releaseCleanupEvent);
            ScoreUser requester = sessionService.getScoreUserByUserId(releaseCleanupEvent.getUserId());

            var ccCommand = repositoryFactory.ccCommandRepository(requester);
            ccCommand.cleanUp(releaseCleanupEvent.getReleaseId());
            updateState(requester, releaseCleanupEvent.getReleaseId(), Published);
        } finally {
            lock.unlock();
        }
    }

    private void updateState(ScoreUser requester, ReleaseId releaseId, ReleaseState releaseState) {
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("It only allows to update the state of the release for developers.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.updateState(releaseId, releaseState);
    }

}
