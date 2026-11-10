package org.oagi.score.gateway.http.api.release_management.service;

import java.util.Objects;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
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
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseProgressRepository;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCreateRequestEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseInitializeEvent;
import org.oagi.score.gateway.http.api.tag_management.model.AccManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.AsccpManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.BccpManifestTagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.DtManifestTagSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.AfterCommitEventPublisher;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

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
    private ObjectProvider<RecordCache> recordCache;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AfterCommitEventPublisher eventPublisher;

    @Autowired
    private ReleaseChangeNotifier changeNotifier;

    @Autowired
    private ReleaseFailureReporter failureReporter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private final String RELEASE_CREATE_REQUEST_EVENT = "releaseCreateRequestEvent";
    private final String RELEASE_CLEANUP_EVENT = "releaseCleanupEvent";
    private final String RELEASE_INITIALIZE_EVENT = "releaseInitializeEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onReleaseCreateRequestEventReceived",
                new ChannelTopic(RELEASE_CREATE_REQUEST_EVENT));
        eventListenerContainer.addMessageListener(this, "onReleaseCleanupEventReceived",
                new ChannelTopic(RELEASE_CLEANUP_EVENT));
        eventListenerContainer.addMessageListener(this, "onReleaseInitializeEventReceived",
                new ChannelTopic(RELEASE_INITIALIZE_EVENT));
    }

    /**
     * Creates a new release if the release number is not already in use.
     *
     * @param requester The user requesting the release creation.
     * @param request   The details of the release to be created.
     * @return The ID of the newly created release.
     * @throws IllegalArgumentException if the requester is not a developer or a release with the
     *                                  same release number already exists.
     */
    public ReleaseId create(ScoreUser requester, CreateReleaseRequest request) {
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("Only users with the '" + DEVELOPER + "' role can create a release.");
        }

        if (!StringUtils.hasText(request.releaseNum())) {
            throw new IllegalArgumentException("'releaseNum' must not be blank.");
        }

        var query = repositoryFactory.releaseQueryRepository(requester);
        if (query.hasDuplicateReleaseNumber(request.libraryId(), request.releaseNum())) {
            throw new IllegalArgumentException("'" + request.releaseNum() + "' already exists.");
        }

        requireNamespaceOfLibrary(requester, request.namespaceId(), request.libraryId());

        var command = repositoryFactory.releaseCommandRepository(requester);
        ReleaseId created = command.create(
                request.libraryId(),
                request.namespaceId(),
                request.releaseNum(),
                request.releaseNote(),
                request.releaseLicense());
        changeNotifier.changed(request.libraryId(), created);
        return created;
    }

    /**
     * Updates an existing release.
     *
     * @param requester The user requesting the update.
     * @param request   The details of the release update.
     * @throws IllegalArgumentException if the requester is not a developer, the release does not exist,
     *                                  the release is in the 'Published' or 'Processing' state, the update
     *                                  attempts to rename the 'Working' release, or the release number is duplicated.
     */
    @Transactional
    public void update(ScoreUser requester, UpdateReleaseRequest request) {

        if (request.releaseId() == null) {
            throw new IllegalArgumentException("'releaseId' is required.");
        }
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("Only users with the '" + DEVELOPER + "' role can update a release.");
        }
        if (!StringUtils.hasText(request.releaseNum())) {
            throw new IllegalArgumentException("'releaseNum' must not be blank.");
        }

        var query = repositoryFactory.releaseQueryRepository(requester);

        ReleaseSummaryRecord releaseSummary = query.getReleaseSummary(request.releaseId());
        if (releaseSummary == null) {
            throw new IllegalArgumentException("'" + request.releaseId() + "' does not exist.");
        }
        if (releaseSummary.state() == Published || releaseSummary.state() == Processing
                || releaseSummary.state().inProgress()) {
            throw new IllegalArgumentException("The release in '" + releaseSummary.state() + "' state cannot be updated.");
        }
        if (releaseSummary.isWorkingRelease() && !releaseSummary.releaseNum().equals(request.releaseNum())) {
            throw new IllegalArgumentException("'" + releaseSummary.releaseNum() + "' release number cannot be changed.");
        }
        if (query.hasDuplicateReleaseNumberExcludingCurrent(request.releaseId(), request.releaseNum())) {
            throw new IllegalArgumentException("'" + request.releaseNum() + "' already exists.");
        }

        requireNamespaceOfLibrary(requester, request.namespaceId(), releaseSummary.libraryId());

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.update(
                request.releaseId(),
                request.namespaceId(),
                request.releaseNum(),
                request.releaseNote(),
                request.releaseLicense());
        changeNotifier.changed(releaseSummary.libraryId(), request.releaseId());
    }

    private void requireNamespaceOfLibrary(ScoreUser requester, NamespaceId namespaceId, LibraryId libraryId) {
        if (namespaceId == null) {
            return;
        }
        NamespaceDetailsRecord namespace = repositoryFactory.namespaceQueryRepository(requester)
                .getNamespaceDetails(namespaceId, requester.userId());
        if (namespace == null || !Objects.equals(namespace.libraryId(), libraryId)) {
            throw new IllegalArgumentException("The namespace does not belong to the library of the release.");
        }
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
        changeNotifier.changed(releaseSummary.libraryId(), releaseId);
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

    /**
     * Gives back a release that has been left in the middle of its transition ('Drafting', 'Publishing' or 'Initializing'), to the state it
     * was in before: because the server stopped, or the work for it did not end. What is done for the transition is done in
     * one transaction that is held by its release, so that a release that is being worked on is not given back from under
     * the work: it is refused, and may be recovered when the work is over or has failed.
     *
     * @throws IllegalArgumentException if the requester is not a developer, the release does not exist or is not in the
     *                                  middle of a transition, or the work for it is going on.
     */
    public void recover(ScoreUser requester, ReleaseId releaseId) {
        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException("Only users with the '" + DEVELOPER + "' role can recover a release.");
        }

        var inProgress = progress().lockInProgress(releaseId).orElse(null);
        if (inProgress == null) {
            ReleaseState state = progress().stateOf(releaseId)
                    .orElseThrow(() -> new IllegalArgumentException("Release with ID '" + releaseId + "' does not exist."));
            if (state.inProgress()) {
                throw new IllegalArgumentException("The release is being worked on. Try again when it is over.");
            }
            throw new IllegalArgumentException("The release in '" + state + "' state is not in the middle of a transition.");
        }

        ReleaseState back = inProgress.state().givenBack();
        if (!progress().moveState(releaseId, inProgress.state(), back)) {
            throw new IllegalArgumentException("The release is no longer in '" + inProgress.state() + "' state.");
        }
        logger.warn("The release " + releaseId + " was '" + inProgress.state() + "', and " + requester.username()
                + " recovered it: it is '" + back + "' again.");
        changeNotifier.changed(releaseId);
    }

    public void transitState(ScoreUser requester,
                             TransitStateRequest request) {

        ReleaseState requestState = ReleaseState.valueOf(request.getState());
        if (requestState == Published && !requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can publish the release.");
        }

        // Before anything is read: the library is held until the end of the transaction, no one is changing it while the
        // release is taken hold of, and what is read next is what has been committed.
        progress().lockLibraryOfRelease(request.getReleaseId());

        var query = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord release = query.getReleaseSummary(request.getReleaseId());

        CcState fromCcState = null;
        CcState toCcState = null;

        switch (release.state()) {
            case Initialized:
                if (requestState != Draft) {
                    throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot transit to '" + requestState + "' state.");
                }

                // the release becomes a draft when its event has been handled: until then it is being drafted
                requestState = Drafting;
                fromCcState = Candidate;
                toCcState = ReleaseDraft;
                break;

            case Draft:
                if (requestState != Initialized && requestState != Published) {
                    throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot transit to '" + requestState + "' state.");
                }

                if (requestState == Initialized) {
                    // the release is 'Initialized' when its event has been handled: until then it is being taken back
                    requestState = Initializing;
                } else if (requestState == Published) {
                    // the release is published when its event has been handled: until then it is being published
                    requestState = Publishing;
                    fromCcState = ReleaseDraft;
                    toCcState = CcState.Published;
                }

                break;

            case Processing:
            case Drafting:
            case Publishing:
            case Initializing:
            case Published:
                throw new IllegalArgumentException("The release in '" + release.state() + "' state cannot be transited.");
        }

        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException("It only allows to modify the release by the developer.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);

        if (requestState.inProgress()) {
            // Only one request takes hold of the release: it is still as it was when it was read.
            if (!progress().takeHold(request.getReleaseId(), release.state(), requestState, requester.userId())) {
                throw new IllegalArgumentException("The release is no longer in '" + release.state() + "' state.");
            }
        } else {
            command.updateState(request.getReleaseId(), requestState);
        }
        changeNotifier.changed(release.libraryId(), request.getReleaseId());

        if (requestState == Initializing) {
            // The components of the release become candidates again, and what the release holds is removed, when the
            // event is handled, all together with the end of the transition: see onReleaseInitializeEventReceived.
            eventPublisher.publish(RELEASE_INITIALIZE_EVENT,
                    new ReleaseInitializeEvent(requester.userId(), request.getReleaseId()));
        }

        // update CCs' states by transited release state.
        if (fromCcState != null && toCcState != null) {
            if (toCcState == ReleaseDraft) {
                // The components of the release are moved, and what they hold copied, when the event is handled, all
                // together with the end of the transition: see onReleaseCreateRequestEventReceived.
            } else if (toCcState == CcState.Published) {
                // The components are published, and the release with them, when the event is handled: see
                // onReleaseCleanupEventReceived.
                ReleaseCleanupEvent releaseCleanupEvent = new ReleaseCleanupEvent(
                        requester.userId(), request.getReleaseId());

                /*
                 * Message Publishing
                 */
                eventPublisher.publish(RELEASE_CLEANUP_EVENT, releaseCleanupEvent);
            }
        }
    }

    /**
     * The components of the release become candidates again, and what the release holds is removed: the work of a draft
     * that goes back to 'Initialized'.
     */
    private void takeBackToInitialized(ScoreUser requester, ReleaseSummaryRecord release) {
        var command = repositoryFactory.releaseCommandRepository(requester);

        updateCCStates(requester, release, ReleaseDraft, Candidate, LocalDateTime.now());

        // Remove release dependencies
        command.deleteDeps(release.releaseId());

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

        // Remove GitHub issue links before the CCs they reference are deleted (issue #1533).
        repositoryFactory.gitHubIssueLinkCommandRepository(requester)
                .deleteLinksByRelease(release.releaseId());

        // Remove the drafted release's forwarded view-order rows before their ACC/ASCC/BCC
        // manifests are deleted, so those FKs are never violated (issue #1638).
        repositoryFactory.bieViewOrderCommandRepository(requester)
                .deleteByRelease(release.releaseId());

        var ccCommand = repositoryFactory.ccCommandRepository(requester);

        // Remove replacement
        ccCommand.clearReplacement(release.releaseId());

        // Remove CCs
        ccCommand.delete(release.releaseId());
    }

    private void updateCCStates(ScoreUser requester, ReleaseSummaryRecord release,
                                CcState fromCcState, CcState toCcState, LocalDateTime timestamp) {
        var accQuery = repositoryFactory.accQueryRepository(requester);
        for (var acc : accQuery.getAccSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, acc.accManifestId(), toCcState, null, null, true);
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        for (var asccp : asccpQuery.getAsccpSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, asccp.asccpManifestId(), toCcState, null, null, true);
        }

        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        for (var bccp : bccpQuery.getBccpSummaryList(release.libraryId(), "Working", fromCcState)) {
            ccCommandService.updateState(requester, bccp.bccpManifestId(), toCcState, null, null, true);
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
            ccCommandService.updateState(requester, dt.dtManifestId(), toCcState, null, null, true);
        }
    }

    public ReleaseValidationResponse validate(ScoreUser requester,
                                              ReleaseValidationRequest request) {
        return validator(requester, request).validate();
    }

    private ReleaseValidator validator(ScoreUser requester, ReleaseValidationRequest request) {

        ReleaseValidator validator = new ReleaseValidator(requester, request.getReleaseId(), repositoryFactory);
        validator.setAssignedAccComponentManifestIds(request.getAssignedAccComponentManifestIds());
        validator.setAssignedAsccpComponentManifestIds(request.getAssignedAsccpComponentManifestIds());
        validator.setAssignedBccpComponentManifestIds(request.getAssignedBccpComponentManifestIds());
        validator.setAssignedCodeListComponentManifestIds(request.getAssignedCodeListComponentManifestIds());
        validator.setAssignedAgencyIdListComponentManifestIds(request.getAssignedAgencyIdListComponentManifestIds());
        validator.setAssignedDtComponentManifestIds(request.getAssignedDtComponentManifestIds());
        return validator;
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

        // Before anything is read: what is validated is what has been committed, and nothing changes the library until
        // the release has taken hold of it.
        progress().lockLibraryOfRelease(releaseId);

        if (isReleaseInAnyOfStates(requester, releaseId, Draft, Processing, Drafting, Publishing, Initializing)) {
            throw new IllegalArgumentException("It cannot make any release to 'Draft' due to a release restriction.");
        }

        ReleaseValidationResponse response = validator(requester, request).validateForDraft();
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
            eventPublisher.publish(RELEASE_CREATE_REQUEST_EVENT, releaseCreateRequestEvent);

            response.clearWarnings();
        }

        return response;
    }

    /**
     * This method is invoked by 'releaseCreateRequestEvent' channel subscriber.
     *
     * <p>All of it is done in one transaction of its own: the components of the release are moved to 'ReleaseDraft', what
     * the release holds is copied, and the release becomes a draft, or nothing of it is. The release row is locked first, so
     * that the one that handles the event is the only one, wherever the event is received, and the release is as it was
     * asked to be. When something fails, the release is 'Initialized' again.</p>
     *
     * <p>It is not run in a transaction of the service, whether it is called through the proxy of the service or not: it
     * makes its own.</p>
     *
     * @param releaseCreateRequestEvent
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onReleaseCreateRequestEventReceived(ReleaseCreateRequestEvent releaseCreateRequestEvent) {
        ReleaseId releaseId = releaseCreateRequestEvent.getReleaseId();
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (progress().lockState(releaseId).orElse(null) != Drafting) {
                    return; // handled already
                }
                logger.debug("Received ReleaseCreateRequestEvent: " + releaseCreateRequestEvent);
                ScoreUser requester = sessionService.getScoreUserByUserId(releaseCreateRequestEvent.getUserId());
                doReleaseWork(() -> {
                    moveToReleaseDraft(requester, releaseCreateRequestEvent);
                    copyWorkingManifests(requester,
                            releaseId,
                            releaseCreateRequestEvent.getAccManifestIds(),
                            releaseCreateRequestEvent.getAsccpManifestIds(),
                            releaseCreateRequestEvent.getBccpManifestIds(),
                            releaseCreateRequestEvent.getDtManifestIds(),
                            releaseCreateRequestEvent.getCodeListManifestIds(),
                            releaseCreateRequestEvent.getAgencyIdListManifestIds());
                });
                if (!progress().moveState(releaseId, Drafting, Draft)) {
                    throw new IllegalStateException("The release is no longer being drafted.");
                }
                changeNotifier.changed(releaseId);
            });
        } catch (RuntimeException e) {
            logger.error("The release " + releaseId + " could not be made a draft: " + e.getMessage(), e);
            giveBack(releaseId, Drafting, Initialized, releaseCreateRequestEvent.getUserId(), e);
        }
    }

    /** The components that are assigned to the release become 'ReleaseDraft'; one that is not 'Candidate' fails it. */
    private void moveToReleaseDraft(ScoreUser requester, ReleaseCreateRequestEvent event) {
        for (AccManifestId accManifestId : event.getAccManifestIds()) {
            requireMoved(ccCommandService.updateState(requester, accManifestId, ReleaseDraft, null, null, true), accManifestId);
        }
        for (AsccpManifestId asccpManifestId : event.getAsccpManifestIds()) {
            requireMoved(ccCommandService.updateState(requester, asccpManifestId, ReleaseDraft, null, null, true), asccpManifestId);
        }
        for (BccpManifestId bccpManifestId : event.getBccpManifestIds()) {
            requireMoved(ccCommandService.updateState(requester, bccpManifestId, ReleaseDraft, null, null, true), bccpManifestId);
        }
        for (CodeListManifestId codeListManifestId : event.getCodeListManifestIds()) {
            requireMoved(codeListCommandService.updateState(requester, codeListManifestId, ReleaseDraft), codeListManifestId);
        }
        for (AgencyIdListManifestId agencyIdListManifestId : event.getAgencyIdListManifestIds()) {
            requireMoved(agencyIdListCommandService.updateState(requester, agencyIdListManifestId, ReleaseDraft), agencyIdListManifestId);
        }
        for (DtManifestId dtManifestId : event.getDtManifestIds()) {
            requireMoved(ccCommandService.updateState(requester, dtManifestId, ReleaseDraft, null, null, true), dtManifestId);
        }
    }

    private static void requireMoved(boolean moved, Object manifestId) {
        if (!moved) {
            throw new ComponentNotMovedException("The component " + manifestId + " could not be moved to '" + ReleaseDraft + "'.");
        }
    }

    /** Gives the release back, after what was done for it has been rolled back. */
    private ReleaseProgressRepository progress() {
        return repositoryFactory.releaseProgressRepository();
    }

    /**
     * Gives the release back, after what was done for it has been rolled back, and tells the user who asked for it what
     * went wrong: the record of it is kept in the transaction that gives the release back.
     */
    private void giveBack(ReleaseId releaseId, ReleaseState from, ReleaseState to, UserId requester, Throwable cause) {
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (!progress().moveState(releaseId, from, to)) {
                    logger.warn("The release " + releaseId + " is no longer '" + from + "'.");
                    return;
                }
                failureReporter.failed(releaseId, from, requester, cause);
            });
        } catch (RuntimeException e) {
            // it is taken back later: see ReleaseProgressRecovery
            logger.error("The release " + releaseId + " could not be given back to '" + to + "': " + e.getMessage(), e);
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

        // Carry GitHub issue links over from the 'Working' release onto the new draft release's
        // manifests, mirroring how component tags are carried over (issue #1533). The repository
        // resolves the library's 'Working' release internally from the target release.
        repositoryFactory.gitHubIssueLinkCommandRepository(requester)
                .copyLinksFromWorking(releaseId);

        // Forward the 'Working' release's sibling view-order weights onto the corresponding new-release
        // manifests, so ordering authored on 'Working' carries into each new release (issue #1638).
        repositoryFactory.bieViewOrderCommandRepository(requester)
                .copyFromWorking(releaseId);
    }

    /**
     * This method is invoked by 'releaseCleanupEvent' channel subscriber.
     *
     * <p>The components of the release become 'Published', the release is cleaned up and published, in one transaction
     * of its own, or nothing of it is. When something fails, the release is a 'Draft' again.</p>
     *
     * @param releaseCleanupEvent
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onReleaseCleanupEventReceived(ReleaseCleanupEvent releaseCleanupEvent) {
        ReleaseId releaseId = releaseCleanupEvent.getReleaseId();
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (progress().lockState(releaseId).orElse(null) != Publishing) {
                    return; // handled already
                }
                logger.debug("Received ReleaseCleanupEvent: " + releaseCleanupEvent);
                ScoreUser requester = sessionService.getScoreUserByUserId(releaseCleanupEvent.getUserId());
                var release = repositoryFactory.releaseQueryRepository(requester).getReleaseSummary(releaseId);

                doReleaseWork(() -> {
                    updateCCStates(requester, release, ReleaseDraft, CcState.Published, LocalDateTime.now());
                    repositoryFactory.ccCommandRepository(requester).cleanUp(releaseId);
                });
                updateState(requester, releaseId, Published);
                changeNotifier.changed(releaseId);

                // The published release already holds its own copy of the GitHub issue links (carried over
                // when it was drafted); remove the 'Working' release's links for the components included in
                // this release so those issues live only in the published release going forward. Links on
                // Working components not part of this release are kept (issue #1533). The repository resolves
                // the library's 'Working' release internally from the published release.
                repositoryFactory.gitHubIssueLinkCommandRepository(requester)
                        .deleteWorkingLinksIncludedInRelease(releaseId);
            });
        } catch (RuntimeException e) {
            logger.error("The release " + releaseId + " could not be published: " + e.getMessage(), e);
            giveBack(releaseId, Publishing, Draft, releaseCleanupEvent.getUserId(), e);
        }
    }

    /**
     * This method is invoked by 'releaseInitializeEvent' channel subscriber.
     *
     * <p>The components of the release become candidates again, what the release holds is removed, and the release is
     * 'Initialized', in one transaction of its own, or nothing of it is. When something fails, the release is a 'Draft' again.</p>
     *
     * @param releaseInitializeEvent
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onReleaseInitializeEventReceived(ReleaseInitializeEvent releaseInitializeEvent) {
        ReleaseId releaseId = releaseInitializeEvent.getReleaseId();
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (progress().lockState(releaseId).orElse(null) != Initializing) {
                    return; // handled already
                }
                logger.debug("Received ReleaseInitializeEvent: " + releaseInitializeEvent);
                ScoreUser requester = sessionService.getScoreUserByUserId(releaseInitializeEvent.getUserId());
                var release = repositoryFactory.releaseQueryRepository(requester).getReleaseSummary(releaseId);

                doReleaseWork(() -> takeBackToInitialized(requester, release));
                if (!progress().moveState(releaseId, Initializing, Initialized)) {
                    throw new IllegalStateException("The release is no longer being taken back to 'Initialized'.");
                }
                changeNotifier.changed(releaseId);
            });
        } catch (RuntimeException e) {
            logger.error("The release " + releaseId + " could not be taken back to 'Initialized': " + e.getMessage(), e);
            giveBack(releaseId, Initializing, Draft, releaseInitializeEvent.getUserId(), e);
        }
    }

    /**
     * What a release does to the components and to what it holds. It is not changed by anyone else meanwhile (see
     * {@link ReleaseInProgressGuard}), and it is a bulk change as far as the cache is concerned (see {@link RecordCache#bulkChange}):
     * it writes tens of thousands of rows in one transaction.
     *
     * <p>The three pieces of work only call this, so that how the cache is kept right can change without them changing.</p>
     */
    private void doReleaseWork(Runnable work) {
        recordCache.getIfAvailable(() -> RecordCache.DISABLED).bulkChange(() -> ReleaseInProgressGuard.asReleaseWork(work));
    }

    private void updateState(ScoreUser requester, ReleaseId releaseId, ReleaseState releaseState) {
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("It only allows to update the state of the release for developers.");
        }

        var command = repositoryFactory.releaseCommandRepository(requester);
        command.updateState(releaseId, releaseState);
    }

}
