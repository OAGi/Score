package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.*;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditAsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditNodeDetail;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditRef;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.BieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.DefaultBieEditTreeController;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcCommandService;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BieEditService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private CcCommandService ccCommandService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Autowired
    private BieService bieService;

    @Autowired
    private OpenAPIDocService openAPIDocService;

    @Autowired
    private MessageCommandService messageCommandService;

    private final String PURGE_BIE_EVENT_NAME = "purgeBieEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this,
                "onPurgeBieEventReceived",
                new ChannelTopic(PURGE_BIE_EVENT_NAME));
    }

    private BieEditTreeController getTreeController(ScoreUser requester, BieEditNode node) {
        return getTreeController(requester, node.getTopLevelAsbiepId(), node.isDerived(), node.isLocked());
    }

    private BieEditTreeController getTreeController(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        return getTreeController(requester, topLevelAsbiepId, false, false);
    }

    private BieEditTreeController getTreeController(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                                    boolean isDerived, boolean isLocked) {
        DefaultBieEditTreeController bieEditTreeController =
                applicationContext.getBean(DefaultBieEditTreeController.class);

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        bieEditTreeController.initialize(requester, topLevelAsbiep);
        if (isDerived || isLocked) {
            // bieEditTreeController.setForceBieUpdate(false);
        }

        return bieEditTreeController;
    }

    @Transactional
    public BieEditAbieNode getRootNode(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
        return treeController.getRootNode(topLevelAsbiepId);
    }

    @Transactional
    public List<BieEditNode> getDescendants(ScoreUser requester, BieEditNode node, boolean hideUnused) {
        BieEditTreeController treeController = getTreeController(requester, node);
        return treeController.getDescendants(requester, node, hideUnused).stream()
                .map(e -> {
                    if (node.isDerived() || node.isLocked()) {
                        e.setLocked(true);
                    }
                    return e;
                }).collect(Collectors.toList());
    }

    @Transactional
    public BieEditNodeDetail getDetail(ScoreUser requester, BieEditNode node) {
        BieEditTreeController treeController = getTreeController(requester, node);
        return treeController.getDetail(node);
    }

    private void ensureBieRelationshipsForChangingState(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        // Issue #1010
        StringBuilder failureMessageBody = new StringBuilder();

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        if (state == BieState.WIP) { // 'Move to WIP' Case.
            List<TopLevelAsbiepSummaryRecord> reusedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId);

            reusedTopLevelAsbiepList = reusedTopLevelAsbiepList.stream()
                    .filter(e -> !e.owner().userId().equals(requester.userId()))
                    .filter(e -> e.state().getLevel() > state.getLevel()).collect(Collectors.toList());
            if (!reusedTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reusing BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            List<TopLevelAsbiepSummaryRecord> reusingTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId);

            reusingTopLevelAsbiepList = reusingTopLevelAsbiepList.stream()
                    .filter(e -> !e.owner().userId().equals(requester.userId()))
                    .filter(e -> e.state().getLevel() < state.getLevel()).collect(Collectors.toList());

            if (!reusingTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reused BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusingTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        }

        // Issue #1635
        if (state == BieState.WIP) { // 'Move to WIP' Case.
            List<TopLevelAsbiepSummaryRecord> derivedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId);

            derivedTopLevelAsbiepList = derivedTopLevelAsbiepList.stream()
                    .filter(e -> !e.owner().userId().equals(requester.userId()))
                    .filter(e -> e.state().getLevel() > state.getLevel()).collect(Collectors.toList());
            if (!derivedTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following inherited BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : derivedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId());

            if (basedTopLevelAsbiep != null &&
                    !requester.userId().equals(basedTopLevelAsbiep.owner().userId().value()) &&
                    basedTopLevelAsbiep.state().getLevel() < state.getLevel()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following base BIE:")
                        .append("\n\n");
                failureMessageBody = failureMessageBody.append("- [")
                        .append(basedTopLevelAsbiep.propertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(basedTopLevelAsbiep.topLevelAsbiepId())
                        .append(") (")
                        .append(basedTopLevelAsbiep.guid())
                        .append(") - ")
                        .append(basedTopLevelAsbiep.state()).append(" state")
                        .append("\n");
            }
        }

        if (failureMessageBody.length() > 0) {
            String subject = "Failed to update BIE state";
            MessageId errorMessageId = messageCommandService.asyncSendMessage(
                    sessionService.getScoreSystemUser(),
                    Arrays.asList(requester.userId()),
                    subject,
                    failureMessageBody.toString(),
                    "text/markdown").join().values().iterator().next();

            throw new DataAccessForbiddenException(subject, errorMessageId.value());
        }
    }

    @Transactional
    public void updateState(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        ensureBieRelationshipsForChangingState(requester, topLevelAsbiepId, state);

        BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
        treeController.updateState(requester, state);
    }

    @Transactional
    public BieEditUpdateDetailResponse updateDetails(ScoreUser requester, BieEditUpdateDetailRequest request) {
        BieEditUpdateDetailResponse response = new BieEditUpdateDetailResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        var abieCommand = repositoryFactory.abieCommandRepository(requester);
        response.setAbieDetailMap(
                request.getAbieDetails().stream()
                        .map(abie ->
                                abieCommand.upsertAbie(new UpsertAbieRequest(
                                        request.getTopLevelAsbiepId(), abie))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        var asbiepCommand = repositoryFactory.asbiepCommandRepository(requester);
        response.setAsbiepDetailMap(
                request.getAsbiepDetails().stream()
                        .map(asbiep ->
                                asbiepCommand.upsertAsbiep(new UpsertAsbiepRequest(
                                        request.getTopLevelAsbiepId(), asbiep))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        var bbiepCommand = repositoryFactory.bbiepCommandRepository(requester);
        response.setBbiepDetailMap(
                request.getBbiepDetails().stream()
                        .map(bbiep ->
                                bbiepCommand.upsertBbiep(new UpsertBbiepRequest(
                                        request.getTopLevelAsbiepId(), bbiep
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        var asbieCommand = repositoryFactory.asbieCommandRepository(requester);
        response.setAsbieDetailMap(
                request.getAsbieDetails().stream()
                        .map(asbie ->
                                asbieCommand.upsertAsbie(new UpsertAsbieRequest(
                                        request.getTopLevelAsbiepId(), asbie
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        var bbieCommand = repositoryFactory.bbieCommandRepository(requester);
        response.setBbieDetailMap(
                request.getBbieDetails().stream()
                        .map(bbie ->
                                bbieCommand.upsertBbie(new UpsertBbieRequest(
                                        request.getTopLevelAsbiepId(), bbie
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        var bbieScCommand = repositoryFactory.bbieScCommandRepository(requester);
        response.setBbieScDetailMap(
                request.getBbieScDetails().stream()
                        .map(bbieSc ->
                                bbieScCommand.upsertBbieSc(new UpsertBbieScRequest(
                                        request.getTopLevelAsbiepId(), bbieSc
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        String status = request.getTopLevelAsbiepDetail().getStatus();
        String version = request.getTopLevelAsbiepDetail().getVersion();
        Boolean inverseMode = request.getTopLevelAsbiepDetail().getInverseMode();
        UpdateTopLevelAsbiepRequest topLevelAsbiepRequest = new UpdateTopLevelAsbiepRequest(
                request.getTopLevelAsbiepId(), status, version, inverseMode);
        var topLevelAsbiepCommand = repositoryFactory.topLevelAsbiepCommandRepository(requester);
        topLevelAsbiepCommand.updateTopLevelAsbiep(topLevelAsbiepRequest);

        // Verify the referred OAS_DOC
        BieForOasDoc bieForOasDoc = request.getTopLevelAsbiepDetail().getBieForOasDoc();
        if (bieForOasDoc != null) {
            openAPIDocService.updateDetails(requester, new UpdateBieForOasDocRequest(requester)
                    .withOasDocId(bieForOasDoc.getOasDocId())
                    .withBieForOasDocList(Arrays.asList(bieForOasDoc)));
        }

        // Issue #1635
        // If there are inherited BIEs based on this TOP_LEVEL_ASBIEP,
        // changes will be propagated to them in a cascading manner.
        propagateChangesToInheritedBIEs(requester, request.getTopLevelAsbiepId());

        return response;
    }

    private void propagateChangesToInheritedBIEs(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        Queue<TopLevelAsbiepSummaryRecord> inheritedTopLevelAsbiepList = new LinkedBlockingDeque<>();
        inheritedTopLevelAsbiepList.addAll(
                topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId));
        while (!inheritedTopLevelAsbiepList.isEmpty()) {
            TopLevelAsbiepSummaryRecord inheritedTopLevelAsbiep = inheritedTopLevelAsbiepList.poll();
            if (inheritedTopLevelAsbiep.state() != BieState.WIP) {
                throw new IllegalArgumentException("The BIE cannot be edited due to the inherited BIE being in " + inheritedTopLevelAsbiep.state() + " state.");
            }

            overrideInheritedBIE(requester, inheritedTopLevelAsbiep, topLevelAsbiep);

            inheritedTopLevelAsbiepList.addAll(
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(inheritedTopLevelAsbiep.topLevelAsbiepId()));
        }
    }

    @Transactional
    public CreateExtensionResponse createLocalAbieExtension(ScoreUser requester, BieEditAsbiepNode extension) {
        AsccpManifestId asccpManifestId = extension.getAsccpManifestId();

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(extension.getTopLevelAsbiepId());
        ReleaseId releaseId = topLevelAsbiep.release().releaseId();
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = asccpQuery.getAsccpSummary(asccpManifestId);
        AccManifestId roleOfAccManifestId = asccp.roleOfAccManifestId();

        CreateExtensionResponse response = new CreateExtensionResponse();

        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord ueAcc = accQuery.getExistsUserExtension(roleOfAccManifestId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            AccSummaryRecord latestUeAcc = ueAcc;
            if (ueAcc.state() == CcState.Production) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.state() == CcState.QA) {
                response.setCanView(true);
            }
            boolean isSameBetweenRequesterAndOwner = requester.userId().equals(latestUeAcc.owner().userId());
            if (isSameBetweenRequesterAndOwner) {
                response.setCanEdit(true);
                response.setCanView(true);
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }

        response.setExtensionId(createAbieExtension(requester, roleOfAccManifestId, releaseId));
        return response;
    }

    @Transactional
    public CreateExtensionResponse createGlobalAbieExtension(ScoreUser requester, BieEditAsbiepNode extension) {

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(extension.getTopLevelAsbiepId());
        ReleaseId releaseId = topLevelAsbiep.release().releaseId();
        AccManifestId roleOfAccManifestId = dslContext.select(Tables.ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(Tables.ACC_MANIFEST)
                .join(Tables.ACC).on(Tables.ACC_MANIFEST.ACC_ID.eq(Tables.ACC.ACC_ID))
                .where(and(
                        Tables.ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId.value())),
                        Tables.ACC.OBJECT_CLASS_TERM.eq("All Extension")
                ))
                .fetchOneInto(AccManifestId.class);

        CreateExtensionResponse response = new CreateExtensionResponse();

        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord ueAcc = accQuery.getExistsUserExtension(roleOfAccManifestId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            AccSummaryRecord latestUeAcc = ueAcc;
            if (ueAcc.state() == CcState.Production) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.state() == CcState.QA) {
                response.setCanView(true);
            }
            boolean isSameBetweenRequesterAndOwner = requester.userId().equals(latestUeAcc.owner().userId());
            if (isSameBetweenRequesterAndOwner) {
                response.setCanEdit(true);
                response.setCanView(true);
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }
        response.setExtensionId(createAbieExtension(requester, roleOfAccManifestId, releaseId));
        return response;
    }

    private AccManifestId createAbieExtension(ScoreUser requester, AccManifestId roleOfAccManifestId, ReleaseId releaseId) {
        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord eAcc = accQuery.getAccSummary(roleOfAccManifestId);
        AccSummaryRecord ueAcc = accQuery.getExistsUserExtension(roleOfAccManifestId);

        AccManifestId manifestId = ccCommandService.appendUserExtension(requester, eAcc, ueAcc, releaseId);
        return manifestId;
    }

    public AbieDetailsRecord getAbieDetails(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, AccManifestId accManifestId, String hashPath) {

        var abieQuery = repositoryFactory.abieQueryRepository(requester);
        AbieDetailsRecord abieDetails = abieQuery.getAbieDetails(topLevelAsbiepId, hashPath);
        if (abieDetails == null) {
            var accQuery = repositoryFactory.accQueryRepository(requester);
            AccSummaryRecord basedAcc = accQuery.getAccSummary(accManifestId);

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            abieDetails = AbieDetailsRecord.builder()
                    .basedAcc(basedAcc)
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return abieDetails;
    }

    public AbieDetailsRecord getAbieDetails(ScoreUser requester, AbieId abieId) {

        var query = repositoryFactory.abieQueryRepository(requester);
        return query.getAbieDetails(abieId);
    }

    public AsbieDetailsRecord getAsbieDetails(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                              AsccManifestId asccManifestId, String hashPath) {

        var asbieQuery = repositoryFactory.asbieQueryRepository(requester);
        AsbieDetailsRecord asbieDetails = asbieQuery.getAsbieDetails(topLevelAsbiepId, hashPath);
        if (asbieDetails == null) {
            var accQuery = repositoryFactory.accQueryRepository(requester);
            AsccSummaryRecord basedAscc = accQuery.getAsccSummary(asccManifestId);

            var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
            AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(basedAscc.toAsccpManifestId());

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            asbieDetails = AsbieDetailsRecord.builder()
                    .basedAscc(basedAscc)
                    .cardinality(basedAscc.cardinality())
                    .nillable(toAsccp.nillable())
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return asbieDetails;
    }

    public AsbieDetailsRecord getAsbieDetails(ScoreUser requester, AsbieId asbieId) {

        var query = repositoryFactory.asbieQueryRepository(requester);
        return query.getAsbieDetails(asbieId);
    }

    public BbieDetailsRecord getBbieDetails(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                            BccManifestId bccManifestId, String hashPath) {

        var bbieQuery = repositoryFactory.bbieQueryRepository(requester);
        BbieDetailsRecord bbieDetails = bbieQuery.getBbieDetails(topLevelAsbiepId, hashPath);
        if (bbieDetails == null) {
            var accQuery = repositoryFactory.accQueryRepository(requester);
            BccSummaryRecord basedBcc = accQuery.getBccSummary(bccManifestId);

            var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
            BccpSummaryRecord toBccp = bccpQuery.getBccpSummary(basedBcc.toBccpManifestId());

            var dtQuery = repositoryFactory.dtQueryRepository(requester);
            PrimitiveRestriction primitiveRestriction = PrimitiveRestriction.fromDtAwdPri(
                    dtQuery.getDefaultDtAwdPriSummary(toBccp.dtManifestId()));

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            bbieDetails = BbieDetailsRecord.builder()
                    .basedBcc(basedBcc)
                    .cardinality(basedBcc.cardinality())
                    .primitiveRestriction(primitiveRestriction)
                    .valueConstraint(basedBcc.valueConstraint())
                    .nillable(toBccp.nillable())
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return bbieDetails;
    }

    public BbieDetailsRecord getBbieDetails(ScoreUser requester, BbieId bbieId) {

        var query = repositoryFactory.bbieQueryRepository(requester);
        return query.getBbieDetails(bbieId);
    }

    public AsbiepDetailsRecord getAsbiepDetails(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                                AsccpManifestId asccpManifestId, String hashPath) {

        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        AsbiepDetailsRecord asbiepDetails = asbiepQuery.getAsbiepDetails(topLevelAsbiepId, hashPath);
        if (asbiepDetails == null) {
            var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
            AsccpSummaryRecord basedAsccp = asccpQuery.getAsccpSummary(asccpManifestId);

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            asbiepDetails = AsbiepDetailsRecord.builder()
                    .basedAsccp(basedAsccp)
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return asbiepDetails;
    }

    public AsbiepDetailsRecord getAsbiepDetails(ScoreUser requester, AsbiepId asbiepId) {

        var query = repositoryFactory.asbiepQueryRepository(requester);
        return query.getAsbiepDetails(asbiepId);
    }

    public BbiepDetailsRecord getBbiepDetails(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                              BccpManifestId bccpManifestId, String hashPath) {

        var bbiepQuery = repositoryFactory.bbiepQueryRepository(requester);
        BbiepDetailsRecord bbiepDetails = bbiepQuery.getBbiepDetails(topLevelAsbiepId, hashPath);
        if (bbiepDetails == null) {
            var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
            BccpSummaryRecord basedBccp = bccpQuery.getBccpSummary(bccpManifestId);

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            bbiepDetails = BbiepDetailsRecord.builder()
                    .basedBccp(basedBccp)
                    .valueConstraint(basedBccp.valueConstraint())
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return bbiepDetails;
    }

    public BbiepDetailsRecord getBbiepDetails(ScoreUser requester, BbiepId bbiepId) {

        var query = repositoryFactory.bbiepQueryRepository(requester);
        return query.getBbiepDetails(bbiepId);
    }

    public BbieScDetailsRecord getBbieScDetails(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId,
                                                DtScManifestId dtScManifestId, String hashPath) {

        var bbieScQuery = repositoryFactory.bbieScQueryRepository(requester);
        BbieScDetailsRecord bbieScDetails = bbieScQuery.getBbieScDetails(topLevelAsbiepId, hashPath);
        if (bbieScDetails == null) {
            var dtQuery = repositoryFactory.dtQueryRepository(requester);
            DtScSummaryRecord basedDtSc = dtQuery.getDtScSummary(dtScManifestId);

            PrimitiveRestriction primitiveRestriction = PrimitiveRestriction.fromDtScAwdPri(
                    dtQuery.getDefaultDtScAwdPriSummary(dtScManifestId));

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            bbieScDetails = BbieScDetailsRecord.builder()
                    .basedDtSc(basedDtSc)
                    .cardinality(basedDtSc.cardinality())
                    .primitiveRestriction(primitiveRestriction)
                    .valueConstraint(basedDtSc.valueConstraint())
                    .hashPath(hashPath)
                    .ownerTopLevelAsbiep(ownerTopLevelAsbiep)
                    .build();
        }
        return bbieScDetails;
    }

    public BbieScDetailsRecord getBbieScDetails(ScoreUser requester, BbieScId bbieScId) {

        var query = repositoryFactory.bbieScQueryRepository(requester);
        return query.getBbieScDetails(bbieScId);
    }

    public List<BieEditUsed> getBieUsedList(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {

        List<BieEditUsed> usedList = new ArrayList();

        var asbieQuery = repositoryFactory.asbieQueryRepository(requester);
        asbieQuery.getBieRefList(topLevelAsbiepId).stream()
                .filter(e -> e.getRefTopLevelAsbiepId() != null)
                .map(BieEditRef::getRefTopLevelAsbiepId)
                .distinct()
                .forEach(refTopLevelAsbiepId -> {
                    usedList.addAll(getBieUsedList(requester, refTopLevelAsbiepId));
                });

        usedList.addAll(asbieQuery.getUsedAsbieList(topLevelAsbiepId));

        var bbieQuery = repositoryFactory.bbieQueryRepository(requester);
        usedList.addAll(bbieQuery.getUsedBbieList(topLevelAsbiepId));

        var bbieScQuery = repositoryFactory.bbieScQueryRepository(requester);
        usedList.addAll(bbieScQuery.getUsedBbieScList(topLevelAsbiepId));

        return usedList;
    }

    public List<BieEditRef> getBieRefList(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        var asbieQuery = repositoryFactory.asbieQueryRepository(requester);
        return asbieQuery.getBieRefList(topLevelAsbiepId);
    }

    @Transactional
    public void useBaseBIE(ScoreUser requester, UseBaseBIERequest request) {
        TopLevelAsbiepSummaryRecord topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, request.getTopLevelAsbiepId());
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to reuse end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord baseTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(request.getBaseTopLevelAsbiepId());

        overrideInheritedBIE(requester, topLevelAsbiep, baseTopLevelAsbiep);

        ULong topLevelAsbiepId = ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value());
        ULong baseTopLevelAsbiepId = ULong.valueOf(baseTopLevelAsbiep.topLevelAsbiepId().value());

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID, baseTopLevelAsbiepId)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .execute();
    }

    private void overrideInheritedBIE(ScoreUser requester,
                                      TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                      TopLevelAsbiepSummaryRecord baseTopLevelAsbiep) {
        ULong topLevelAsbiepId = ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value());
        ULong baseTopLevelAsbiepId = ULong.valueOf(baseTopLevelAsbiep.topLevelAsbiepId().value());
        ULong requesterUserId = ULong.valueOf(requester.userId().value());

        Map<ULong, ULong> abieIdChangeMap = new HashMap<>();
        List<AbieRecord> abieRecords = dslContext.selectFrom(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, AbieRecord> inheritedAbieMapByHashPath =
                abieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(AbieRecord::getHashPath, Function.identity()));
        abieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseAbie -> {
                    if (inheritedAbieMapByHashPath.containsKey(baseAbie.getHashPath())) {
                        AbieRecord inheritedAbie = inheritedAbieMapByHashPath.get(baseAbie.getHashPath());
                        if (!hasLength(inheritedAbie.getDefinition())) {
                            inheritedAbie.setDefinition(baseAbie.getDefinition());
                        }
                        if (!hasLength(inheritedAbie.getRemark())) {
                            inheritedAbie.setRemark(baseAbie.getRemark());
                        }
                        if (!hasLength(inheritedAbie.getBizTerm())) {
                            inheritedAbie.setBizTerm(baseAbie.getBizTerm());
                        }
                        inheritedAbie.update();

                        abieIdChangeMap.put(baseAbie.getAbieId(), inheritedAbie.getAbieId());
                    } else {
                        ULong oldAbieId = baseAbie.getAbieId();
                        baseAbie.setAbieId(null);
                        baseAbie.setLastUpdatedBy(requesterUserId);
                        baseAbie.setLastUpdateTimestamp(LocalDateTime.now());
                        baseAbie.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newAbieId = dslContext.insertInto(ABIE).set(baseAbie)
                                .returning(ABIE.ABIE_ID).fetchOne().getAbieId();
                        abieIdChangeMap.put(oldAbieId, newAbieId);
                    }
                });

        Map<ULong, ULong> asbiepIdChangeMap = new HashMap<>();
        List<AsbiepRecord> asbiepRecords = dslContext.selectFrom(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, AsbiepRecord> inheritedAsbiepMapByHashPath =
                asbiepRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(AsbiepRecord::getHashPath, Function.identity()));
        asbiepRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseAsbiep -> {
                    if (inheritedAsbiepMapByHashPath.containsKey(baseAsbiep.getHashPath())) {
                        AsbiepRecord inheritedAsbiep = inheritedAsbiepMapByHashPath.get(baseAsbiep.getHashPath());

                        if (!hasLength(inheritedAsbiep.getDefinition())) {
                            inheritedAsbiep.setDefinition(baseAsbiep.getDefinition());
                        }
                        if (!hasLength(inheritedAsbiep.getRemark())) {
                            inheritedAsbiep.setRemark(baseAsbiep.getRemark());
                        }
                        if (!hasLength(inheritedAsbiep.getBizTerm())) {
                            inheritedAsbiep.setBizTerm(baseAsbiep.getBizTerm());
                        }
                        inheritedAsbiep.update();

                        asbiepIdChangeMap.put(baseAsbiep.getAsbiepId(), inheritedAsbiep.getAsbiepId());
                    } else {
                        ULong oldAsbiepId = baseAsbiep.getAsbiepId();
                        baseAsbiep.setAsbiepId(null);
                        baseAsbiep.setDisplayName(null);
                        baseAsbiep.setRoleOfAbieId(abieIdChangeMap.get(baseAsbiep.getRoleOfAbieId()));
                        baseAsbiep.setLastUpdatedBy(requesterUserId);
                        baseAsbiep.setLastUpdateTimestamp(LocalDateTime.now());
                        baseAsbiep.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newAsbiepId = dslContext.insertInto(ASBIEP).set(baseAsbiep)
                                .returning(ASBIEP.ASBIEP_ID).fetchOne().getAsbiepId();
                        asbiepIdChangeMap.put(oldAsbiepId, newAsbiepId);
                    }
                });

        Map<ULong, ULong> bbiepIdChangeMap = new HashMap<>();
        List<BbiepRecord> bbiepRecords = dslContext.selectFrom(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, BbiepRecord> inheritedBbiepMapByHashPath =
                bbiepRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(BbiepRecord::getHashPath, Function.identity()));
        bbiepRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseBbiep -> {
                    if (inheritedBbiepMapByHashPath.containsKey(baseBbiep.getHashPath())) {
                        BbiepRecord inheritedBbiep = inheritedBbiepMapByHashPath.get(baseBbiep.getHashPath());

                        if (!hasLength(inheritedBbiep.getDefinition())) {
                            inheritedBbiep.setDefinition(baseBbiep.getDefinition());
                        }
                        if (!hasLength(inheritedBbiep.getRemark())) {
                            inheritedBbiep.setRemark(baseBbiep.getRemark());
                        }
                        if (!hasLength(inheritedBbiep.getBizTerm())) {
                            inheritedBbiep.setBizTerm(baseBbiep.getBizTerm());
                        }
                        inheritedBbiep.update();

                        bbiepIdChangeMap.put(baseBbiep.getBbiepId(), inheritedBbiep.getBbiepId());
                    } else {
                        ULong oldBbiepId = baseBbiep.getBbiepId();
                        baseBbiep.setBbiepId(null);
                        baseBbiep.setDisplayName(null);
                        baseBbiep.setLastUpdatedBy(requesterUserId);
                        baseBbiep.setLastUpdateTimestamp(LocalDateTime.now());
                        baseBbiep.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newBbiepId = dslContext.insertInto(BBIEP).set(baseBbiep)
                                .returning(BBIEP.BBIEP_ID).fetchOne().getBbiepId();
                        bbiepIdChangeMap.put(oldBbiepId, newBbiepId);
                    }
                });

        List<AsbieRecord> asbieRecords = dslContext.selectFrom(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, AsbieRecord> inheritedAsbieMapByHashPath =
                asbieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(AsbieRecord::getHashPath, Function.identity()));
        asbieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseAsbie -> {
                    if (inheritedAsbieMapByHashPath.containsKey(baseAsbie.getHashPath())) {
                        AsbieRecord inheritedAsbie = inheritedAsbieMapByHashPath.get(baseAsbie.getHashPath());

                        ULong prevToAsbiepId = inheritedAsbie.getToAsbiepId();
                        ULong asbiepId = asbiepIdChangeMap.get(baseAsbie.getToAsbiepId());
                        if (asbiepId == null) {
                            asbiepId = baseAsbie.getToAsbiepId();
                        }
                        if (!isInInheritance(asbiepId, prevToAsbiepId)) {
                            inheritedAsbie.setToAsbiepId(asbiepId);
                        }

                        if (!hasLength(inheritedAsbie.getDefinition())) {
                            inheritedAsbie.setDefinition(baseAsbie.getDefinition());
                        }
                        if (!hasLength(inheritedAsbie.getRemark())) {
                            inheritedAsbie.setRemark(baseAsbie.getRemark());
                        }
                        if (inheritedAsbie.getCardinalityMin() < baseAsbie.getCardinalityMin()) {
                            inheritedAsbie.setCardinalityMin(baseAsbie.getCardinalityMin());
                        }
                        if (baseAsbie.getCardinalityMax() > 0 &&
                            (inheritedAsbie.getCardinalityMax() <= 0 ||
                            inheritedAsbie.getCardinalityMax() > baseAsbie.getCardinalityMax())) {
                            inheritedAsbie.setCardinalityMax(baseAsbie.getCardinalityMax());
                        }
                        if (baseAsbie.getIsNillable() == (byte) 1) {
                            inheritedAsbie.setIsNillable(baseAsbie.getIsNillable());
                        }
                        if (baseAsbie.getIsDeprecated() == (byte) 1) {
                            inheritedAsbie.setIsDeprecated(baseAsbie.getIsDeprecated());
                        }
                        if (baseAsbie.getIsUsed() == (byte) 1) {
                            inheritedAsbie.setIsUsed(baseAsbie.getIsUsed());
                        }
                        inheritedAsbie.update();
                    } else {
                        ULong oldAsbieId = baseAsbie.getAsbieId();
                        baseAsbie.setAsbieId(null);
                        baseAsbie.setFromAbieId(abieIdChangeMap.get(baseAsbie.getFromAbieId()));
                        ULong asbiepId = asbiepIdChangeMap.get(baseAsbie.getToAsbiepId());
                        if (asbiepId == null) {
                            asbiepId = baseAsbie.getToAsbiepId();
                        }
                        baseAsbie.setToAsbiepId(asbiepId);
                        baseAsbie.setLastUpdatedBy(requesterUserId);
                        baseAsbie.setLastUpdateTimestamp(LocalDateTime.now());
                        baseAsbie.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newAsbieId = dslContext.insertInto(ASBIE).set(baseAsbie)
                                .returning(ASBIE.ASBIE_ID).fetchOne().getAsbieId();
                    }
                });

        Map<ULong, ULong> bbieIdChangeMap = new HashMap<>();
        List<BbieRecord> bbieRecords = dslContext.selectFrom(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, BbieRecord> inheritedBbieMapByHashPath =
                bbieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(BbieRecord::getHashPath, Function.identity()));
        bbieRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseBbie -> {
                    if (inheritedBbieMapByHashPath.containsKey(baseBbie.getHashPath())) {
                        BbieRecord inheritedBbie = inheritedBbieMapByHashPath.get(baseBbie.getHashPath());

                        if (!hasLength(inheritedBbie.getDefinition())) {
                            inheritedBbie.setDefinition(baseBbie.getDefinition());
                        }
                        if (!hasLength(inheritedBbie.getRemark())) {
                            inheritedBbie.setRemark(baseBbie.getRemark());
                        }
                        if ((inheritedBbie.getXbtManifestId() != null && !inheritedBbie.getXbtManifestId().equals(baseBbie.getXbtManifestId())) ||
                            (inheritedBbie.getCodeListManifestId() != null && !inheritedBbie.getCodeListManifestId().equals(baseBbie.getCodeListManifestId())) ||
                            (inheritedBbie.getAgencyIdListManifestId() != null && !inheritedBbie.getAgencyIdListManifestId().equals(baseBbie.getAgencyIdListManifestId()))) {
                            inheritedBbie.setXbtManifestId(baseBbie.getXbtManifestId());
                            inheritedBbie.setCodeListManifestId(baseBbie.getCodeListManifestId());
                            inheritedBbie.setAgencyIdListManifestId(baseBbie.getAgencyIdListManifestId());
                        }
                        if (inheritedBbie.getCardinalityMin() < baseBbie.getCardinalityMin()) {
                            inheritedBbie.setCardinalityMin(baseBbie.getCardinalityMin());
                        }
                        if (baseBbie.getCardinalityMax() > 0 &&
                            (inheritedBbie.getCardinalityMax() <= 0 ||
                            inheritedBbie.getCardinalityMax() > baseBbie.getCardinalityMax())) {
                            inheritedBbie.setCardinalityMax(baseBbie.getCardinalityMax());
                        }
                        if (!Objects.equals(inheritedBbie.getFacetMinLength(), baseBbie.getFacetMinLength())) {
                            inheritedBbie.setFacetMinLength(baseBbie.getFacetMinLength());
                        }
                        if (!Objects.equals(inheritedBbie.getFacetMaxLength(), baseBbie.getFacetMaxLength())) {
                            inheritedBbie.setFacetMaxLength(baseBbie.getFacetMaxLength());
                        }
                        if (!hasLength(inheritedBbie.getFacetPattern())) {
                            inheritedBbie.setFacetPattern(baseBbie.getFacetPattern());
                        }
                        if (hasLength(baseBbie.getDefaultValue())) {
                            inheritedBbie.setDefaultValue(baseBbie.getDefaultValue());
                            inheritedBbie.setFixedValue(null);
                        }
                        else if (hasLength(baseBbie.getFixedValue())) {
                            inheritedBbie.setFixedValue(baseBbie.getFixedValue());
                            inheritedBbie.setDefaultValue(null);
                        }
                        if (baseBbie.getIsNillable() == (byte) 1) {
                            inheritedBbie.setIsNillable(baseBbie.getIsNillable());
                        }
                        if (baseBbie.getIsNull() == (byte) 1) {
                            inheritedBbie.setIsNull(baseBbie.getIsNull());
                        }
                        if (!hasLength(inheritedBbie.getExample())) {
                            inheritedBbie.setExample(baseBbie.getExample());
                        }
                        if (baseBbie.getIsDeprecated() == (byte) 1) {
                            inheritedBbie.setIsDeprecated(baseBbie.getIsDeprecated());
                        }
                        if (baseBbie.getIsUsed() == (byte) 1) {
                            inheritedBbie.setIsUsed(baseBbie.getIsUsed());
                        }
                        inheritedBbie.update();

                        bbieIdChangeMap.put(baseBbie.getBbieId(), inheritedBbie.getBbieId());
                    } else {
                        ULong oldBbieId = baseBbie.getBbieId();
                        baseBbie.setBbieId(null);
                        baseBbie.setFromAbieId(abieIdChangeMap.get(baseBbie.getFromAbieId()));
                        baseBbie.setToBbiepId(bbiepIdChangeMap.get(baseBbie.getToBbiepId()));
                        baseBbie.setLastUpdatedBy(requesterUserId);
                        baseBbie.setLastUpdateTimestamp(LocalDateTime.now());
                        baseBbie.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newBbieId = dslContext.insertInto(BBIE).set(baseBbie)
                                .returning(BBIE.BBIE_ID).fetchOne().getBbieId();
                        bbieIdChangeMap.put(oldBbieId, newBbieId);
                    }
                });

        List<BbieScRecord> bbieScRecords = dslContext.selectFrom(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(baseTopLevelAsbiepId, topLevelAsbiepId))
                .fetch();
        Map<String, BbieScRecord> inheritedBbieScMapByHashPath =
                bbieScRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(topLevelAsbiepId))
                        .collect(Collectors.toMap(BbieScRecord::getHashPath, Function.identity()));
        bbieScRecords.stream().filter(e -> e.getOwnerTopLevelAsbiepId().equals(baseTopLevelAsbiepId))
                .forEach(baseBbieSc -> {
                    if (inheritedBbieScMapByHashPath.containsKey(baseBbieSc.getHashPath())) {
                        BbieScRecord inheritedBbieSc = inheritedBbieScMapByHashPath.get(baseBbieSc.getHashPath());

                        if (!hasLength(inheritedBbieSc.getDefinition())) {
                            inheritedBbieSc.setDefinition(baseBbieSc.getDefinition());
                        }
                        if (!hasLength(inheritedBbieSc.getRemark())) {
                            inheritedBbieSc.setRemark(baseBbieSc.getRemark());
                        }
                        if ((inheritedBbieSc.getXbtManifestId() != null && !inheritedBbieSc.getXbtManifestId().equals(baseBbieSc.getXbtManifestId())) ||
                            (inheritedBbieSc.getCodeListManifestId() != null && !inheritedBbieSc.getCodeListManifestId().equals(baseBbieSc.getCodeListManifestId())) ||
                            (inheritedBbieSc.getAgencyIdListManifestId() != null && !inheritedBbieSc.getAgencyIdListManifestId().equals(baseBbieSc.getAgencyIdListManifestId()))) {
                            inheritedBbieSc.setXbtManifestId(baseBbieSc.getXbtManifestId());
                            inheritedBbieSc.setCodeListManifestId(baseBbieSc.getCodeListManifestId());
                            inheritedBbieSc.setAgencyIdListManifestId(baseBbieSc.getAgencyIdListManifestId());
                        }
                        if (inheritedBbieSc.getCardinalityMin() < baseBbieSc.getCardinalityMin()) {
                            inheritedBbieSc.setCardinalityMin(baseBbieSc.getCardinalityMin());
                        }
                        if (baseBbieSc.getCardinalityMax() > 0 &&
                            (inheritedBbieSc.getCardinalityMax() <= 0 ||
                            inheritedBbieSc.getCardinalityMax() > baseBbieSc.getCardinalityMax())) {
                            inheritedBbieSc.setCardinalityMax(baseBbieSc.getCardinalityMax());
                        }
                        if (!Objects.equals(inheritedBbieSc.getFacetMinLength(), baseBbieSc.getFacetMinLength())) {
                            inheritedBbieSc.setFacetMinLength(baseBbieSc.getFacetMinLength());
                        }
                        if (!Objects.equals(inheritedBbieSc.getFacetMaxLength(), baseBbieSc.getFacetMaxLength())) {
                            inheritedBbieSc.setFacetMaxLength(baseBbieSc.getFacetMaxLength());
                        }
                        if (!hasLength(inheritedBbieSc.getFacetPattern())) {
                            inheritedBbieSc.setFacetPattern(baseBbieSc.getFacetPattern());
                        }
                        if (hasLength(baseBbieSc.getDefaultValue())) {
                            inheritedBbieSc.setDefaultValue(baseBbieSc.getDefaultValue());
                            inheritedBbieSc.setFixedValue(null);
                        }
                        else if (hasLength(baseBbieSc.getFixedValue())) {
                            inheritedBbieSc.setFixedValue(baseBbieSc.getFixedValue());
                            inheritedBbieSc.setDefaultValue(null);
                        }
                        if (!hasLength(inheritedBbieSc.getExample())) {
                            inheritedBbieSc.setExample(baseBbieSc.getExample());
                        }
                        if (baseBbieSc.getIsDeprecated() == (byte) 1) {
                            inheritedBbieSc.setIsDeprecated(baseBbieSc.getIsDeprecated());
                        }
                        if (baseBbieSc.getIsUsed() == (byte) 1) {
                            inheritedBbieSc.setIsUsed(baseBbieSc.getIsUsed());
                        }
                        inheritedBbieSc.update();
                    } else {
                        baseBbieSc.setBbieScId(null);
                        baseBbieSc.setDisplayName(null);
                        baseBbieSc.setBbieId(bbieIdChangeMap.get(baseBbieSc.getBbieId()));
                        baseBbieSc.setLastUpdatedBy(requesterUserId);
                        baseBbieSc.setLastUpdateTimestamp(LocalDateTime.now());
                        baseBbieSc.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
                        ULong newBbieScId = dslContext.insertInto(BBIE_SC).set(baseBbieSc)
                                .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getBbieScId();
                    }
                });
    }

    private boolean isInInheritance(ULong baseAsbiepId, ULong asbiepId) {
        List<Record2<ULong, ULong>> topLevelAsbiepIdToAsbiepIdList =
                dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                                TOP_LEVEL_ASBIEP.ASBIEP_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(baseAsbiepId))
                        .fetch();
        Set<ULong> inheritance = new HashSet<>();
        while (!topLevelAsbiepIdToAsbiepIdList.isEmpty()) {
            inheritance.addAll(topLevelAsbiepIdToAsbiepIdList.stream()
                    .map(e -> e.get(TOP_LEVEL_ASBIEP.ASBIEP_ID)).collect(Collectors.toList()));
            if (inheritance.contains(asbiepId)) {
                return true;
            }

            topLevelAsbiepIdToAsbiepIdList =
                    dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                                    TOP_LEVEL_ASBIEP.ASBIEP_ID)
                            .from(TOP_LEVEL_ASBIEP)
                            .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.in(
                                    topLevelAsbiepIdToAsbiepIdList.stream()
                                            .map(e -> e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)).collect(Collectors.toList())
                            ))
                            .fetch();
        }

        return inheritance.contains(asbiepId);
    }

    @Transactional
    public void removeBaseBIE(ScoreUser requester, RemoveBaseBIERequest request) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(request.getTopLevelAsbiepId());
        if (!topLevelAsbiep.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        }
        if (topLevelAsbiep.state() != BieState.WIP) {
            throw new IllegalArgumentException("The BIE in " + topLevelAsbiep.state() + " state cannot be edited.");
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .setNull(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))
                .execute();
    }

    private TopLevelAsbiepSummaryRecord validateBIEAccessForReuse(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        ScoreUser bieOwner = sessionService.getScoreUserByUserId(topLevelAsbiep.owner().userId());
        if (requester.hasRole(DEVELOPER) && !bieOwner.isDeveloper()) {
            throw new RoleMismatchedException();
        }
        if (!topLevelAsbiep.owner().userId().equals(requester.userId())) {
            throw new NotOwnerException();
        }
        if (topLevelAsbiep.state() != BieState.WIP) {
            throw new InvalidBieStateException(topLevelAsbiep.state());
        }
        return topLevelAsbiep;
    }

    @Transactional
    public void reuseBIE(ScoreUser requester, ReuseBIERequest request) {
        TopLevelAsbiepSummaryRecord topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, request.getTopLevelAsbiepId());
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to reuse end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        doReuseBIE(requester, topLevelAsbiep.topLevelAsbiepId(), request, false);
    }

    private void doReuseBIE(ScoreUser requester,
                            TopLevelAsbiepId topLevelAsbiepId, ReuseBIERequest request,
                            boolean nestedCall) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord reuseTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(request.getReuseTopLevelAsbiepId());
        ULong reuseAsbiepId = ULong.valueOf(reuseTopLevelAsbiep.asbiepId().value());

        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(
                        ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value()))
                ))
                .fetchOptional().orElse(null);
        if (asbieRecord == null) {
            var abieCommand = repositoryFactory.abieCommandRepository(requester);
            LocalDateTime timestamp = LocalDateTime.now();
            AbieNode.Abie abie = new AbieNode.Abie();
            abie.setPath(request.getFromAbiePath());
            abie.setHashPath(request.getFromAbieHashPath());
            abie.setBasedAccManifestId(request.getAccManifestId());
            abie.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
            abie = abieCommand.upsertAbie(new UpsertAbieRequest(topLevelAsbiepId, abie));

            var asbieCommand = repositoryFactory.asbieCommandRepository(requester);
            AsbieNode.Asbie asbie = new AsbieNode.Asbie();
            asbie.setUsed(true);
            asbie.setBasedAsccManifestId(request.getAsccManifestId());
            asbie.setPath(request.getAsbiePath());
            asbie.setHashPath(request.getAsbieHashPath());
            asbie.setFromAbiePath(request.getFromAbiePath());
            asbie.setFromAbieHashPath(request.getFromAbieHashPath());
            asbie.setToAsbiepId(reuseTopLevelAsbiep.asbiepId());
            asbie.setOwnerTopLevelAsbiepId(topLevelAsbiepId);
            asbieCommand.upsertAsbie(new UpsertAsbieRequest(topLevelAsbiepId, asbie));
        } else {
            ULong prevToAsbiepId = asbieRecord.getToAsbiepId();

            ULong ownerTopLevelAsbiepOfToAsbiep =
                    dslContext.select(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                            .from(ASBIEP)
                            .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId()))
                            .fetchOneInto(ULong.class);


            if (!nestedCall || !isInInheritance(reuseAsbiepId, prevToAsbiepId)) {
                asbieRecord.setToAsbiepId(reuseAsbiepId);
            }

            asbieRecord.setIsUsed((byte) 1);
            asbieRecord.setIsDeprecated((byte) (reuseTopLevelAsbiep.deprecated() ? 1 : 0));
            asbieRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
            asbieRecord.setLastUpdateTimestamp(LocalDateTime.now());
            asbieRecord.update(
                    ASBIE.TO_ASBIEP_ID,
                    ASBIE.IS_DEPRECATED,
                    ASBIE.LAST_UPDATED_BY,
                    ASBIE.LAST_UPDATE_TIMESTAMP);

            boolean isReused = !asbieRecord.getOwnerTopLevelAsbiepId().equals(ownerTopLevelAsbiepOfToAsbiep);
            if (!isReused) {
                // Delete orphan ASBIEP record.
                dslContext.deleteFrom(ASBIEP)
                        .where(ASBIEP.ASBIEP_ID.eq(prevToAsbiepId))
                        .execute();

                PurgeBieEvent event = new PurgeBieEvent(
                        new TopLevelAsbiepId(asbieRecord.getOwnerTopLevelAsbiepId().toBigInteger()));
                /*
                 * Message Publishing
                 */
                redisTemplate.convertAndSend(PURGE_BIE_EVENT_NAME, event);
            }
        }

        // Issue #1635
        List<TopLevelAsbiepSummaryRecord> derivedTopLevelAsbiepList =
                topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId);
        for (TopLevelAsbiepSummaryRecord inheritedTopLevelAsbiep : derivedTopLevelAsbiepList) {
            doReuseBIE(requester, inheritedTopLevelAsbiep.topLevelAsbiepId(), request, true);
        }
    }

    /**
     * This method is invoked by 'purgeBieEvent' channel subscriber.
     *
     * @param purgeBieEvent
     */
    @Transactional
    public void onPurgeBieEventReceived(PurgeBieEvent purgeBieEvent) {
        ULong topLevelAsbiepId = ULong.valueOf(purgeBieEvent.getTopLevelAsbiepId().value());

        while (true) {
            List<ULong> unreferencedAbieList = dslContext.select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .leftJoin(ASBIEP).on(and(
                            ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID),
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                    ))
                    .leftJoin(TOP_LEVEL_ASBIEP).on(ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.isNull(),
                            ASBIEP.ASBIEP_ID.isNull()
                    ))
                    .fetchInto(ULong.class);

            if (unreferencedAbieList.isEmpty()) {
                break;
            }

            List<Record2<ULong, ULong>> unreferencedAsbieList = dslContext.select(ASBIE.ASBIE_ID, ASBIE.TO_ASBIEP_ID)
                    .from(ASBIE)
                    .where(and(
                            ASBIE.FROM_ABIE_ID.in(unreferencedAbieList),
                            ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                    ))
                    .fetch();

            if (!unreferencedAsbieList.isEmpty()) {
                dslContext.deleteFrom(ASBIE)
                        .where(ASBIE.ASBIE_ID.in(
                                unreferencedAsbieList.stream()
                                        .map(e -> e.get(ASBIE.ASBIE_ID)).collect(Collectors.toList())
                        ))
                        .execute();

                dslContext.deleteFrom(ASBIEP)
                        .where(and(
                                ASBIEP.ASBIEP_ID.in(unreferencedAsbieList.stream()
                                        .map(e -> e.get(ASBIE.TO_ASBIEP_ID)).collect(Collectors.toList())),
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();
            }

            List<Record2<ULong, ULong>> unreferencedBbieList = dslContext.select(BBIE.BBIE_ID, BBIE.TO_BBIEP_ID)
                    .from(BBIE)
                    .where(and(
                            BBIE.FROM_ABIE_ID.in(unreferencedAbieList),
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                    ))
                    .fetch();

            if (!unreferencedBbieList.isEmpty()) {
                dslContext.deleteFrom(BBIE_SC)
                        .where(and(
                                BBIE_SC.BBIE_ID.in(unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.BBIE_ID)).collect(Collectors.toList())),
                                BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();

                dslContext.deleteFrom(BBIE)
                        .where(BBIE.BBIE_ID.in(
                                unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.BBIE_ID)).collect(Collectors.toList())
                        ))
                        .execute();

                dslContext.deleteFrom(BBIEP)
                        .where(and(
                                BBIEP.BBIEP_ID.in(unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.TO_BBIEP_ID)).collect(Collectors.toList())),
                                BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();
            }

            dslContext.deleteFrom(ABIE)
                    .where(ABIE.ABIE_ID.in(unreferencedAbieList))
                    .execute();
        }
    }

    @Transactional
    public void removeReusedBIE(ScoreUser requester, RemoveReusedBIERequest request) {

        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(request.getTopLevelAsbiepId().value()))))
                .fetchOne();

        if (asbieRecord == null) {
            throw new IllegalArgumentException("Cannot fount target BIE.");
        }

        TopLevelAsbiepSummaryRecord topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, new TopLevelAsbiepId(asbieRecord.getOwnerTopLevelAsbiepId().toBigInteger()));
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to remove end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        AsbiepRecord asbiepRecord = dslContext.selectFrom(ASBIEP)
                .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId()))
                .fetchOne();

        boolean isReused = !asbiepRecord.getOwnerTopLevelAsbiepId().equals(asbieRecord.getOwnerTopLevelAsbiepId());
        if (!isReused) {
            throw new IllegalArgumentException("The target BIE does not have a reused BIE.");
        }

        dslContext.deleteFrom(ASBIE).where(ASBIE.ASBIE_ID.eq(asbieRecord.getAsbieId())).execute();
    }

    @Transactional
    public void retainReuseBIE(ScoreUser requester, RemoveReusedBIERequest request) {

        TopLevelAsbiepSummaryRecord topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, request.getTopLevelAsbiepId());
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to paste end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(
                        ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()))
                ))
                .fetchOne();

        AsbiepRecord toAsbiepRecord =
                dslContext.selectFrom(ASBIEP)
                        .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId()))
                        .fetchOne();

        ULong ownerTopLevelAsbiepOfToAsbiep = toAsbiepRecord.getOwnerTopLevelAsbiepId();
        boolean isReused = !asbieRecord.getOwnerTopLevelAsbiepId().equals(ownerTopLevelAsbiepOfToAsbiep);
        if (!isReused) {
            throw new IllegalArgumentException("There is no reuse BIE for retention.");
        }

        ULong reuseTopLevelAsbiepId = toAsbiepRecord.getOwnerTopLevelAsbiepId();
        Map<ULong, ULong> abieIdChangeMap = new HashMap<>();
        dslContext.selectFrom(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                .fetchStream().forEach(abie -> {
                    ULong oldAbieId = abie.getAbieId();
                    abie.setAbieId(null);
                    abie.setPath(asbieRecord.getPath() + ">" + abie.getPath());
                    abie.setHashPath(sha256(abie.getPath()));
                    abie.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                    abie.setLastUpdateTimestamp(LocalDateTime.now());
                    abie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                    ULong newAbieId = dslContext.insertInto(ABIE).set(abie)
                            .returning(ABIE.ABIE_ID).fetchOne().getAbieId();
                    abieIdChangeMap.put(oldAbieId, newAbieId);
                });
        Map<ULong, ULong> asbiepIdChangeMap = new HashMap<>();
        AtomicReference<ULong> newToAsbiepIdRef = new AtomicReference<>();
        dslContext.selectFrom(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                .fetchStream().forEach(asbiep -> {
                    ULong oldAsbiepId = asbiep.getAsbiepId();
                    boolean isTopLevelAsbiepId = asbiep.getAsbiepId().equals(toAsbiepRecord.getAsbiepId());
                    asbiep.setAsbiepId(null);
                    asbiep.setRoleOfAbieId(abieIdChangeMap.get(asbiep.getRoleOfAbieId()));
                    asbiep.setPath(asbieRecord.getPath() + ">" + asbiep.getPath());
                    asbiep.setHashPath(sha256(asbiep.getPath()));
                    asbiep.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                    asbiep.setLastUpdateTimestamp(LocalDateTime.now());
                    asbiep.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                    ULong newAsbiepId = dslContext.insertInto(ASBIEP).set(asbiep)
                            .returning(ASBIEP.ASBIEP_ID).fetchOne().getAsbiepId();
                    asbiepIdChangeMap.put(oldAsbiepId, newAsbiepId);
                    if (isTopLevelAsbiepId) {
                        newToAsbiepIdRef.set(newAsbiepId);
                    }
                });
        Map<ULong, ULong> bbiepIdChangeMap = new HashMap<>();
        dslContext.selectFrom(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                .fetchStream().forEach(bbiep -> {
                    ULong oldBbiepId = bbiep.getBbiepId();
                    bbiep.setBbiepId(null);
                    bbiep.setPath(asbieRecord.getPath() + ">" + bbiep.getPath());
                    bbiep.setHashPath(sha256(bbiep.getPath()));
                    bbiep.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                    bbiep.setLastUpdateTimestamp(LocalDateTime.now());
                    bbiep.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                    ULong newBbiepId = dslContext.insertInto(BBIEP).set(bbiep)
                            .returning(BBIEP.BBIEP_ID).fetchOne().getBbiepId();
                    bbiepIdChangeMap.put(oldBbiepId, newBbiepId);
                });
        dslContext.batch(
                dslContext.selectFrom(ASBIE)
                        .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                        .fetchStream().map(asbie -> {
                            asbie.setAsbieId(null);
                            asbie.setPath(asbieRecord.getPath() + ">" + asbie.getPath());
                            asbie.setHashPath(sha256(asbie.getPath()));
                            asbie.setFromAbieId(abieIdChangeMap.get(asbie.getFromAbieId()));
                            ULong asbiepId = asbiepIdChangeMap.get(asbie.getToAsbiepId());
                            // There's no change if the ASBIE is reusing the BIE.
                            if (asbiepId != null) {
                                asbie.setToAsbiepId(asbiepId);
                            }
                            asbie.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                            asbie.setLastUpdateTimestamp(LocalDateTime.now());
                            asbie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                            return dslContext.insertInto(ASBIE).set(asbie);
                        }).collect(Collectors.toList())
        ).execute();
        Map<ULong, ULong> bbieIdChangeMap = new HashMap<>();
        dslContext.selectFrom(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                .fetchStream().forEach(bbie -> {
                    ULong oldBbieId = bbie.getBbieId();
                    bbie.setBbieId(null);
                    bbie.setPath(asbieRecord.getPath() + ">" + bbie.getPath());
                    bbie.setHashPath(sha256(bbie.getPath()));
                    bbie.setFromAbieId(abieIdChangeMap.get(bbie.getFromAbieId()));
                    bbie.setToBbiepId(bbiepIdChangeMap.get(bbie.getToBbiepId()));
                    bbie.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                    bbie.setLastUpdateTimestamp(LocalDateTime.now());
                    bbie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                    ULong newBbieId = dslContext.insertInto(BBIE).set(bbie)
                            .returning(BBIE.BBIE_ID).fetchOne().getBbieId();
                    bbieIdChangeMap.put(oldBbieId, newBbieId);
                });
        dslContext.batch(
                dslContext.selectFrom(BBIE_SC)
                        .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuseTopLevelAsbiepId))
                        .fetchStream().map(bbieSc -> {
                            bbieSc.setBbieScId(null);
                            bbieSc.setPath(asbieRecord.getPath() + ">" + bbieSc.getPath());
                            bbieSc.setHashPath(sha256(bbieSc.getPath()));
                            bbieSc.setBbieId(bbieIdChangeMap.get(bbieSc.getBbieId()));
                            bbieSc.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                            bbieSc.setLastUpdateTimestamp(LocalDateTime.now());
                            bbieSc.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()));
                            return dslContext.insertInto(BBIE_SC).set(bbieSc);
                        }).collect(Collectors.toList())
        ).execute();

        dslContext.update(ASBIE)
                .set(ASBIE.TO_ASBIEP_ID, newToAsbiepIdRef.get())
                .where(ASBIE.ASBIE_ID.eq(asbieRecord.getAsbieId()))
                .execute();
    }

    @Transactional
    public void resetDetailBIE(ScoreUser requester, ResetDetailBIERequest request) {

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(request.getTopLevelAsbiepId());
        if (!topLevelAsbiep.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        }
        if (topLevelAsbiep.state() != BieState.WIP) {
            throw new IllegalArgumentException("The BIE in " + topLevelAsbiep.state() + " state cannot be edited.");
        }

        var dtQuery = repositoryFactory.dtQueryRepository(requester);

        switch (request.getBieType().toUpperCase()) {
            case "ABIE":
                AbieRecord abieRecord = dslContext.selectFrom(ABIE).where(
                        and(ABIE.PATH.eq(request.getPath()),
                                ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))).fetchOne();

                if (abieRecord == null) {
                    return;
                }

                abieRecord.setBizTerm(null);
                abieRecord.setDefinition(null);
                abieRecord.setRemark(null);
                abieRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                abieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                abieRecord.update(ABIE.BIZ_TERM,
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.LAST_UPDATED_BY,
                        ABIE.LAST_UPDATE_TIMESTAMP);

                AsbiepRecord asbiepRecord = dslContext.selectFrom(ASBIEP).where(
                        and(ASBIEP.ROLE_OF_ABIE_ID.eq(abieRecord.getAbieId()),
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))).fetchOne();

                asbiepRecord.setBizTerm(null);
                asbiepRecord.setDefinition(null);
                asbiepRecord.setRemark(null);
                asbiepRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                asbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());

                asbiepRecord.update(ASBIEP.BIZ_TERM,
                        ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);

                dslContext.update(TOP_LEVEL_ASBIEP)
                        .setNull(TOP_LEVEL_ASBIEP.VERSION)
                        .setNull(TOP_LEVEL_ASBIEP.STATUS)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))
                        .execute();
                break;

            case "ASBIEP":
                AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE).where(
                        and(ASBIE.PATH.eq(request.getPath()),
                                ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))).fetchOne();

                if (asbieRecord == null) {
                    return;
                }

                AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                        .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asbieRecord.getBasedAsccManifestId()))
                        .fetchOne();

                AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                        .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                        .fetchOne();

                AsbiepRecord asbiep = dslContext.selectFrom(ASBIEP)
                        .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId())).fetchOne();

                asbieRecord.setCardinalityMin(asccRecord.getCardinalityMin());
                asbieRecord.setCardinalityMax(asccRecord.getCardinalityMax());

                asbieRecord.setDefinition(null);
                asbieRecord.setIsNillable((byte) 0);
                asbieRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                asbieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                asbieRecord.update(ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.DEFINITION,
                        ASBIE.IS_NILLABLE,
                        ASBIE.LAST_UPDATED_BY,
                        ASBIE.LAST_UPDATE_TIMESTAMP);

                asbiep.setRemark(null);
                asbiep.setBizTerm(null);
                asbiep.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                asbiep.setLastUpdateTimestamp(LocalDateTime.now());

                asbiep.update(ASBIEP.BIZ_TERM,
                        ASBIEP.REMARK,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);
                break;

            case "BBIEP":
                BbieRecord bbieRecord = dslContext.selectFrom(BBIE).where(
                        and(BBIE.PATH.eq(request.getPath()),
                                BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))).fetchOne();

                if (bbieRecord == null) {
                    return;
                }

                var accQuery = repositoryFactory.accQueryRepository(requester);
                BccSummaryRecord bccRecord = accQuery.getBccSummary(new BccManifestId(bbieRecord.getBasedBccManifestId().toBigInteger()));

                BbiepRecord bbiepRecord = dslContext.selectFrom(BBIEP).where(BBIEP.BBIEP_ID.eq(bbieRecord.getToBbiepId())).fetchOne();

                bbieRecord.setCardinalityMin(bccRecord.cardinality().min());
                bbieRecord.setCardinalityMax(bccRecord.cardinality().max());
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(null);
                bbieRecord.setIsNull((byte) 0);
                bbieRecord.setIsNillable((byte) (bccRecord.nillable() ? 1 : 0));
                bbieRecord.setDefinition(null);
                bbieRecord.setExample(null);
                bbieRecord.setFacetMinLength(null);
                bbieRecord.setFacetMaxLength(null);
                bbieRecord.setFacetPattern(null);

                var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
                BccpSummaryRecord bccpRecord = bccpQuery.getBccpSummary(bccRecord.toBccpManifestId());

                List<DtAwdPriSummaryRecord> dtAwdPriList = dtQuery.getDtAwdPriSummaryList(bccpRecord.dtManifestId());
                dtAwdPriList = dtAwdPriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (dtAwdPriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                DtAwdPriSummaryRecord defaultDtAwdPri = dtAwdPriList.get(0);
                if (defaultDtAwdPri.codeListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(ULong.valueOf(defaultDtAwdPri.codeListManifestId().value()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (defaultDtAwdPri.agencyIdListManifestId() != null) {
                    bbieRecord.setXbtManifestId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(ULong.valueOf(defaultDtAwdPri.agencyIdListManifestId().value()));
                } else {
                    bbieRecord.setXbtManifestId(ULong.valueOf(defaultDtAwdPri.xbtManifestId().value()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                }
                bbieRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                bbieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                bbieRecord.update(BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.DEFINITION,
                        BBIE.XBT_MANIFEST_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.EXAMPLE,
                        BBIE.FACET_MIN_LENGTH,
                        BBIE.FACET_MAX_LENGTH,
                        BBIE.FACET_PATTERN,
                        BBIE.IS_NULL,
                        BBIE.IS_NILLABLE,
                        BBIE.LAST_UPDATED_BY,
                        BBIE.LAST_UPDATE_TIMESTAMP);

                bbiepRecord.setRemark(null);
                bbiepRecord.setBizTerm(null);

                bbiepRecord.update(BBIEP.REMARK, BBIEP.BIZ_TERM);
                break;

            case "BBIE_SC":
                BbieScRecord bbieScRecord = dslContext.selectFrom(BBIE_SC).where(
                        and(BBIE_SC.PATH.eq(request.getPath()),
                                BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))).fetchOne();

                if (bbieScRecord == null) {
                    return;
                }

                DtScDetailsRecord dtScRecord = dtQuery.getDtScDetails(new DtScManifestId(bbieScRecord.getBasedDtScManifestId().toBigInteger()));

                bbieScRecord.setCardinalityMin(dtScRecord.cardinality().min());
                bbieScRecord.setCardinalityMax(dtScRecord.cardinality().max());

                List<DtScAwdPriDetailsRecord> dtScAwdPriList = dtScRecord.dtScAwdPriList();
                dtScAwdPriList = dtScAwdPriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (dtScAwdPriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                DtScAwdPriDetailsRecord defaultDtScAwdPri = dtScAwdPriList.get(0);
                if (defaultDtScAwdPri.codeList() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(ULong.valueOf(defaultDtScAwdPri.codeList().codeListManifestId().value()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (defaultDtScAwdPri.agencyIdList() != null) {
                    bbieScRecord.setXbtManifestId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(ULong.valueOf(defaultDtScAwdPri.agencyIdList().agencyIdListManifestId().value()));
                } else {
                    bbieScRecord.setXbtManifestId(ULong.valueOf(defaultDtScAwdPri.xbt().xbtManifestId().value()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                }

                bbieScRecord.setDefaultValue(null);
                bbieScRecord.setFixedValue(null);
                bbieScRecord.setExample(null);
                bbieScRecord.setDefinition(null);
                bbieScRecord.setRemark(null);
                bbieScRecord.setBizTerm(null);
                bbieScRecord.setFacetMinLength(null);
                bbieScRecord.setFacetMaxLength(null);
                bbieScRecord.setFacetPattern(null);
                bbieScRecord.setLastUpdatedBy(ULong.valueOf(requester.userId().value()));
                bbieScRecord.setLastUpdateTimestamp(LocalDateTime.now());

                bbieScRecord.update(BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.XBT_MANIFEST_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.FACET_MIN_LENGTH,
                        BBIE_SC.FACET_MAX_LENGTH,
                        BBIE_SC.FACET_PATTERN,
                        BBIE_SC.LAST_UPDATED_BY,
                        BBIE_SC.LAST_UPDATE_TIMESTAMP);
                break;

            default:
                throw new IllegalArgumentException("Cannot fount target BIE.");
        }
    }

    @Transactional
    public void deprecateBIE(ScoreUser requester, DeprecateBIERequest request) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(request.getTopLevelAsbiepId());
        if (topLevelAsbiep == null) {
            throw new ScoreDataAccessException();
        }

        boolean isAdmin = requester.hasRole(ADMINISTRATOR);
        boolean isOwner = topLevelAsbiep.owner().userId().equals(requester.userId());
        if (!isAdmin && !isOwner) {
            throw new ScoreDataAccessException();
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.IS_DEPRECATED, (byte) 1)
                .set(TOP_LEVEL_ASBIEP.DEPRECATED_REASON, request.getReason())
                .set(TOP_LEVEL_ASBIEP.DEPRECATED_REMARK, request.getRemark())
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY, ULong.valueOf(requester.userId().value()))
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))
                .execute();
    }
}
