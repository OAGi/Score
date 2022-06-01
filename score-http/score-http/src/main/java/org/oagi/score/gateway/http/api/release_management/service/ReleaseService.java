package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.data.Release;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.api.release_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.event.ReleaseCreateRequestEvent;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.repo.component.release.ReleaseRepositoryDiscardRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.gateway.http.api.release_management.data.ReleaseState.Published;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.RELEASE;

@Service
@Transactional(readOnly = true)
public class ReleaseService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ReleaseRepository repository;

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

    public List<SimpleRelease> getSimpleReleases(SimpleReleasesRequest request) {
        AppUser requester = sessionService.getAppUser(request.getUser());

        List<Condition> conditions = new ArrayList();
        if (!request.getStates().isEmpty()) {
            conditions.add(RELEASE.STATE.in(request.getStates()));
        }

        List<SimpleRelease> releases = new ArrayList(dslContext.select(RELEASE.RELEASE_ID, RELEASE.RELEASE_NUM, RELEASE.STATE)
                .from(RELEASE)
                .where(conditions)
                .orderBy(RELEASE.RELEASE_ID.desc())
                .fetch().map(row -> {
                    SimpleRelease simpleRelease = new SimpleRelease();
                    simpleRelease.setReleaseId(row.getValue(RELEASE.RELEASE_ID).toBigInteger());
                    simpleRelease.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
                    simpleRelease.setState(ReleaseState.valueOf(row.getValue(RELEASE.STATE)));
                    return simpleRelease;
                })
        );

        SimpleRelease workingRelease =
                releases.stream().filter(e -> "Working".equalsIgnoreCase(e.getReleaseNum())).findAny().orElse(null);
        releases.remove(workingRelease);

        if (requester.isDeveloper()) {
            releases.add(0, workingRelease);
        } else {
            releases.add(workingRelease);
        }

        return releases;
    }

    public SimpleRelease getSimpleReleaseByReleaseId(BigInteger releaseId) {
        return dslContext.select(RELEASE.RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOneInto(SimpleRelease.class);
    }

    public List<ReleaseList> getReleaseList(AuthenticatedPrincipal user) {
        List<ReleaseList> releaseLists = dslContext.select(
                        RELEASE.RELEASE_ID,
                        RELEASE.GUID,
                        RELEASE.RELEASE_NUM,
                        RELEASE.RELEASE_NOTE,
                        RELEASE.RELEASE_LICENSE,
                        RELEASE.STATE,
                        APP_USER.as("creator").LOGIN_ID.as("created_by"),
                        RELEASE.CREATION_TIMESTAMP,
                        APP_USER.as("updater").LOGIN_ID.as("last_updated_by"),
                        RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(RELEASE)
                .join(APP_USER.as("creator"))
                .on(RELEASE.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater"))
                .on(RELEASE.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID))
                .fetchInto(ReleaseList.class);
        return releaseLists;
    }

    private SelectOnConditionStep<Record10<
            ULong, String, String, String, String,
            String, String, LocalDateTime, String, LocalDateTime>> getSelectOnConditionStep() {
        return dslContext.select(
                        RELEASE.RELEASE_ID,
                        RELEASE.GUID,
                        RELEASE.RELEASE_NUM,
                        RELEASE.RELEASE_NOTE,
                        RELEASE.RELEASE_LICENSE,
                        RELEASE.STATE,
                        APP_USER.as("creator").LOGIN_ID.as("created_by"),
                        RELEASE.CREATION_TIMESTAMP,
                        APP_USER.as("updater").LOGIN_ID.as("last_updated_by"),
                        RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(RELEASE)
                .join(APP_USER.as("creator"))
                .on(RELEASE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater"))
                .on(RELEASE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    public PageResponse<ReleaseList> getReleases(AuthenticatedPrincipal user, ReleaseListRequest request) {
        SelectOnConditionStep<Record10<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String, LocalDateTime>> step = getSelectOnConditionStep();

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(request.getReleaseNum())) {
            conditions.add(RELEASE.RELEASE_NUM.containsIgnoreCase(request.getReleaseNum().trim()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(RELEASE.RELEASE_NUM.notIn(request.getExcludes()));
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(RELEASE.STATE.in(request.getStates()));
        }
        if (!request.getCreatorLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("creator").LOGIN_ID.in(request.getCreatorLoginIds()));
        }
        if (request.getCreateStartDate() != null) {
            conditions.add(RELEASE.CREATION_TIMESTAMP.greaterOrEqual(new Timestamp(request.getCreateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getCreateEndDate() != null) {
            conditions.add(RELEASE.CREATION_TIMESTAMP.lessThan(new Timestamp(request.getCreateEndDate().getTime()).toLocalDateTime()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(RELEASE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(RELEASE.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        SelectConnectByStep<Record10<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String, LocalDateTime>> conditionStep = step.where(conditions);
        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "releaseNum":
                if ("asc".equals(sortDirection)) {
                    sortField = RELEASE.RELEASE_NUM.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = RELEASE.RELEASE_NUM.desc();
                }

                break;

            case "state":
                if ("asc".equals(sortDirection)) {
                    sortField = RELEASE.STATE.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = RELEASE.STATE.desc();
                }

                break;

            case "creationTimestamp":
                if ("asc".equals(sortDirection)) {
                    sortField = RELEASE.CREATION_TIMESTAMP.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = RELEASE.CREATION_TIMESTAMP.desc();
                }

                break;

            case "lastUpdateTimestamp":
                if ("asc".equals(sortDirection)) {
                    sortField = RELEASE.LAST_UPDATE_TIMESTAMP.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = RELEASE.LAST_UPDATE_TIMESTAMP.desc();
                }

                break;
        }
        int pageCount = dslContext.fetchCount(conditionStep);
        SelectWithTiesAfterOffsetStep<Record10<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String, LocalDateTime>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<ReleaseList> result = (offsetStep != null) ?
                offsetStep.fetchInto(ReleaseList.class) : conditionStep.fetchInto(ReleaseList.class);

        PageResponse<ReleaseList> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(pageCount);
        return response;
    }

    @Transactional
    public ReleaseResponse createRelease(AuthenticatedPrincipal user, ReleaseDetail releaseDetail) {
        BigInteger userId = sessionService.userId(user);
        ReleaseResponse response = new ReleaseResponse();

        ReleaseRecord releaseRecord = repository.create(userId,
                releaseDetail.getReleaseNum(),
                releaseDetail.getReleaseNote(),
                releaseDetail.getReleaseLicense(),
                releaseDetail.getNamespaceId());

        response.setStatus("success");
        response.setStatusMessage("");

        releaseDetail.setReleaseId(releaseRecord.getReleaseId().toBigInteger());
        if (releaseRecord.getNamespaceId() != null) {
            releaseDetail.setNamespaceId(releaseRecord.getNamespaceId().toBigInteger());
        }
        releaseDetail.setReleaseNum(releaseRecord.getReleaseNum());
        releaseDetail.setReleaseNote(releaseRecord.getReleaseNote());
        releaseDetail.setReleaseLicense(releaseRecord.getReleaseLicense());
        releaseDetail.setState(releaseRecord.getState());
        response.setReleaseDetail(releaseDetail);

        return response;
    }

    @Transactional
    public void updateRelease(AuthenticatedPrincipal user, ReleaseDetail releaseDetail) {
        BigInteger userId = sessionService.userId(user);

        repository.update(userId,
                releaseDetail.getReleaseId(),
                releaseDetail.getReleaseNum(),
                releaseDetail.getReleaseNote(),
                releaseDetail.getReleaseLicense(),
                releaseDetail.getNamespaceId());
    }

    public ReleaseDetail getReleaseDetail(AuthenticatedPrincipal user, BigInteger releaseId) {
        Release release = repository.findById(releaseId);
        ReleaseDetail detail = new ReleaseDetail();
        detail.setReleaseId(release.getReleaseId());
        detail.setNamespaceId(release.getNamespaceId());
        detail.setReleaseNum(release.getReleaseNum());
        detail.setReleaseNote(release.getReleaseNote());
        detail.setReleaseLicense(release.getReleaseLicense());
        detail.setState(release.getState());
        return detail;
    }

    @Transactional
    public void discard(AuthenticatedPrincipal user, List<BigInteger> releaseIds) {
        for (BigInteger releaseId : releaseIds) {
            ReleaseRepositoryDiscardRequest request = new ReleaseRepositoryDiscardRequest(user, releaseId);
            repository.discard(request);
        }
    }

    public AssignComponents getAssignComponents(BigInteger releaseId) {
        return repository.getAssignComponents(releaseId);
    }

    @Transactional
    public void transitState(AuthenticatedPrincipal user,
                             TransitStateRequest request) {

        repository.transitState(user, request);
        if (Published == ReleaseState.valueOf(request.getState())) {
            // fire the create release draft event.
            ReleaseCleanupEvent releaseCleanupEvent = new ReleaseCleanupEvent(
                    sessionService.userId(user), request.getReleaseId());

            /*
             * Message Publishing
             */
            redisTemplate.convertAndSend(RELEASE_CLEANUP_EVENT, releaseCleanupEvent);
        }
    }

    public ReleaseValidationResponse validate(AuthenticatedPrincipal user,
                                              ReleaseValidationRequest request) {

        ReleaseValidator validator = new ReleaseValidator(dslContext);
        validator.setAssignedAccComponentManifestIds(request.getAssignedAccComponentManifestIds());
        validator.setAssignedAsccpComponentManifestIds(request.getAssignedAsccpComponentManifestIds());
        validator.setAssignedBccpComponentManifestIds(request.getAssignedBccpComponentManifestIds());
        validator.setAssignedCodeListComponentManifestIds(request.getAssignedCodeListComponentManifestIds());
        validator.setAssignedDtComponentManifestIds(request.getAssignedDtComponentManifestIds());
        return validator.validate();
    }

    @Transactional
    public ReleaseValidationResponse createDraft(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 BigInteger releaseId,
                                                 @RequestBody ReleaseValidationRequest request) {
        if (repository.isThereAnyDraftRelease(releaseId)) {
            throw new IllegalArgumentException("It cannot make any release to 'Draft' due to a release restriction.");
        }

        ReleaseValidationResponse response = this.validate(user, request);
        if (response.isSucceed()) {
            // update state to 'Release Draft' for assigned CCs
            TransitStateRequest transitStateRequest = new TransitStateRequest();
            transitStateRequest.setReleaseId(releaseId);
            transitStateRequest.setState(ReleaseState.Draft.name());
            transitStateRequest.setValidationRequest(request);

            this.transitState(user, transitStateRequest);

            // fire the create release draft event.
            ReleaseCreateRequestEvent releaseCreateRequestEvent = new ReleaseCreateRequestEvent(
                    sessionService.userId(user),
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
            repository.copyWorkingManifestsTo(releaseCreateRequestEvent.getReleaseId(),
                    releaseCreateRequestEvent.getAccManifestIds(),
                    releaseCreateRequestEvent.getAsccpManifestIds(),
                    releaseCreateRequestEvent.getBccpManifestIds(),
                    releaseCreateRequestEvent.getDtManifestIds(),
                    releaseCreateRequestEvent.getCodeListManifestIds(),
                    releaseCreateRequestEvent.getAgencyIdListManifestIds()
            );
            repository.updateState(releaseCreateRequestEvent.getUserId(),
                    releaseCreateRequestEvent.getReleaseId(), ReleaseState.Draft);
        } finally {
            lock.unlock();
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
            repository.cleanUp(releaseCleanupEvent.getReleaseId());
            repository.updateState(releaseCleanupEvent.getUserId(),
                    releaseCleanupEvent.getReleaseId(), ReleaseState.Published);
        } finally {
            lock.unlock();
        }
    }
}
