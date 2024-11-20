package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.agency_id_management.data.AgencyIdListState;
import org.oagi.score.gateway.http.api.bie_management.data.DeprecateBIERequest;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditNodeDetail;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditRef;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.BieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.DefaultBieEditTreeController;
import org.oagi.score.gateway.http.api.cc_management.service.ExtensionService;
import org.oagi.score.gateway.http.api.code_list_management.data.CodeListState;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.bie.model.GetBaseBieRequest;
import org.oagi.score.repo.api.bie.model.GetInheritedBieListRequest;
import org.oagi.score.repo.api.bie.model.GetReuseBieListRequest;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.message.model.SendMessageRequest;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;
import org.oagi.score.repo.api.openapidoc.model.UpdateBieForOasDocRequest;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.abie.AbieNode;
import org.oagi.score.repo.component.abie.AbieReadRepository;
import org.oagi.score.repo.component.abie.AbieWriteRepository;
import org.oagi.score.repo.component.abie.UpsertAbieRequest;
import org.oagi.score.repo.component.agency_id_list.AgencyIdListReadRepository;
import org.oagi.score.repo.component.agency_id_list.AvailableAgencyIdList;
import org.oagi.score.repo.component.asbie.AsbieNode;
import org.oagi.score.repo.component.asbie.AsbieReadRepository;
import org.oagi.score.repo.component.asbie.AsbieWriteRepository;
import org.oagi.score.repo.component.asbie.UpsertAsbieRequest;
import org.oagi.score.repo.component.asbiep.AsbiepNode;
import org.oagi.score.repo.component.asbiep.AsbiepReadRepository;
import org.oagi.score.repo.component.asbiep.AsbiepWriteRepository;
import org.oagi.score.repo.component.asbiep.UpsertAsbiepRequest;
import org.oagi.score.repo.component.bbie.BbieNode;
import org.oagi.score.repo.component.bbie.BbieReadRepository;
import org.oagi.score.repo.component.bbie.BbieWriteRepository;
import org.oagi.score.repo.component.bbie.UpsertBbieRequest;
import org.oagi.score.repo.component.bbie_sc.BbieScNode;
import org.oagi.score.repo.component.bbie_sc.BbieScReadRepository;
import org.oagi.score.repo.component.bbie_sc.BbieScWriteRepository;
import org.oagi.score.repo.component.bbie_sc.UpsertBbieScRequest;
import org.oagi.score.repo.component.bbiep.BbiepNode;
import org.oagi.score.repo.component.bbiep.BbiepReadRepository;
import org.oagi.score.repo.component.bbiep.BbiepWriteRepository;
import org.oagi.score.repo.component.bbiep.UpsertBbiepRequest;
import org.oagi.score.repo.component.bdt_pri_restri.AvailableBdtPriRestri;
import org.oagi.score.repo.component.bdt_pri_restri.BdtPriRestriReadRepository;
import org.oagi.score.repo.component.bdt_sc_pri_restri.AvailableBdtScPriRestri;
import org.oagi.score.repo.component.bdt_sc_pri_restri.BdtScPriRestriReadRepository;
import org.oagi.score.repo.component.code_list.AvailableCodeList;
import org.oagi.score.repo.component.code_list.CodeListReadRepository;
import org.oagi.score.repo.component.dt.BdtNode;
import org.oagi.score.repo.component.dt.BdtReadRepository;
import org.oagi.score.repo.component.top_level_asbiep.TopLevelAsbiepWriteRepository;
import org.oagi.score.repo.component.top_level_asbiep.UpdateTopLevelAsbiepRequest;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.message.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.in;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreDigestUtils.sha256;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BieEditService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BieRepository bieRepository;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Autowired
    private TopLevelAsbiepWriteRepository topLevelAsbiepWriteRepository;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private BieService bieService;

    @Autowired
    private OpenAPIDocService openAPIDocService;

    @Autowired
    private MessageService messageService;

    private final String PURGE_BIE_EVENT_NAME = "purgeBieEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this,
                "onPurgeBieEventReceived",
                new ChannelTopic(PURGE_BIE_EVENT_NAME));
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, BieEditNode node) {
        return getTreeController(user, node.getTopLevelAsbiepId(), node.isDerived(), node.isLocked());
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        return getTreeController(user, topLevelAsbiepId, false, false);
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                                    boolean isDerived, boolean isLocked) {
        DefaultBieEditTreeController bieEditTreeController =
                applicationContext.getBean(DefaultBieEditTreeController.class);

        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId);
        bieEditTreeController.initialize(user, topLevelAsbiep);
        if (isDerived || isLocked) {
            // bieEditTreeController.setForceBieUpdate(false);
        }

        return bieEditTreeController;
    }

    @Transactional
    public BieEditAbieNode getRootNode(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        BieEditTreeController treeController = getTreeController(user, topLevelAsbiepId);
        return treeController.getRootNode(topLevelAsbiepId);
    }

    @Transactional
    public BccForBie getBcc(AuthenticatedPrincipal user, BigInteger bccId) {
        return bieRepository.getBcc(bccId);
    }

    @Transactional
    public List<BieEditNode> getDescendants(AuthenticatedPrincipal user, BieEditNode node, boolean hideUnused) {
        BieEditTreeController treeController = getTreeController(user, node);
        return treeController.getDescendants(user, node, hideUnused).stream()
                .map(e -> {
                    if (node.isDerived() || node.isLocked()) {
                        e.setLocked(true);
                    }
                    return e;
                }).collect(Collectors.toList());
    }

    @Transactional
    public BieEditNodeDetail getDetail(AuthenticatedPrincipal user, BieEditNode node) {
        BieEditTreeController treeController = getTreeController(user, node);
        return treeController.getDetail(node);
    }

    public boolean hasReuseBie(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        BieReadRepository bieReadRepository = scoreRepositoryFactory.createBieReadRepository();
        return !bieReadRepository.getReuseBieList(new GetReuseBieListRequest(sessionService.asScoreUser(user))
                        .withTopLevelAsbiepId(topLevelAsbiepId, true))
                .getTopLevelAsbiepList().isEmpty();
    }

    private void ensureReusingRelationships(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BieState state) {
        // Issue #1010
        ScoreUser requester = sessionService.asScoreUser(user);
        BieReadRepository bieReadRepository = scoreRepositoryFactory.createBieReadRepository();
        StringBuilder failureMessageBody = new StringBuilder();

        if (state == BieState.WIP) { // 'Move to WIP' Case.
            List<org.oagi.score.repo.api.bie.model.TopLevelAsbiep> reusingTopLevelAsbiepList =
                    bieReadRepository.getReuseBieList(new GetReuseBieListRequest(requester)
                                    .withTopLevelAsbiepId(topLevelAsbiepId, true))
                            .getTopLevelAsbiepList();

            reusingTopLevelAsbiepList = reusingTopLevelAsbiepList.stream()
                    .filter(e -> !requester.getUserId().equals(e.getOwner().getUserId()))
                    .filter(e -> e.getState().getLevel() > state.getLevel()).collect(Collectors.toList());
            if (!reusingTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
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
                for (org.oagi.score.repo.api.bie.model.TopLevelAsbiep target : reusingTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.getPropertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.getTopLevelAsbiepId())
                            .append(") (")
                            .append(target.getGuid())
                            .append(") - ")
                            .append(target.getState()).append(" state")
                            .append("\n");
                }
            }
        } else {
            List<org.oagi.score.repo.api.bie.model.TopLevelAsbiep> reusedTopLevelAsbiepList =
                    bieReadRepository.getReuseBieList(new GetReuseBieListRequest(sessionService.asScoreUser(user))
                                    .withTopLevelAsbiepId(topLevelAsbiepId, false))
                            .getTopLevelAsbiepList();

            reusedTopLevelAsbiepList = reusedTopLevelAsbiepList.stream()
                    .filter(e -> !requester.getUserId().equals(e.getOwner().getUserId()))
                    .filter(e -> e.getState().getLevel() < state.getLevel()).collect(Collectors.toList());

            if (!reusedTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
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
                for (org.oagi.score.repo.api.bie.model.TopLevelAsbiep target : reusedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.getPropertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.getTopLevelAsbiepId())
                            .append(") (")
                            .append(target.getGuid())
                            .append(") - ")
                            .append(target.getState()).append(" state")
                            .append("\n");
                }
            }
        }

        // Issue #1635
        if (state == BieState.WIP) { // 'Move to WIP' Case.
            List<org.oagi.score.repo.api.bie.model.TopLevelAsbiep> inheritedTopLevelAsbiepList =
                    bieReadRepository.getInheritedBieList(new GetInheritedBieListRequest(requester)
                                    .withTopLevelAsbiepId(topLevelAsbiepId))
                            .getTopLevelAsbiepList();

            inheritedTopLevelAsbiepList = inheritedTopLevelAsbiepList.stream()
                    .filter(e -> e.getState().getLevel() > state.getLevel()).collect(Collectors.toList());
            if (!inheritedTopLevelAsbiepList.isEmpty()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
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
                for (org.oagi.score.repo.api.bie.model.TopLevelAsbiep target : inheritedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.getPropertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.getTopLevelAsbiepId())
                            .append(") (")
                            .append(target.getGuid())
                            .append(") - ")
                            .append(target.getState()).append(" state")
                            .append("\n");
                }
            }
        } else {
            org.oagi.score.repo.api.bie.model.TopLevelAsbiep basedTopLevelAsbiep =
                    bieReadRepository.getBaseBie(new GetBaseBieRequest(requester)
                                    .withTopLevelAsbiepId(topLevelAsbiepId))
                            .getBaseTopLevelAsbiep();

            if (basedTopLevelAsbiep != null && basedTopLevelAsbiep.getState().getLevel() < state.getLevel()) {
                Record source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
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
                        .append(basedTopLevelAsbiep.getPropertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(basedTopLevelAsbiep.getTopLevelAsbiepId())
                        .append(") (")
                        .append(basedTopLevelAsbiep.getGuid())
                        .append(") - ")
                        .append(basedTopLevelAsbiep.getState()).append(" state")
                        .append("\n");
            }
        }

        if (failureMessageBody.length() > 0) {
            SendMessageRequest sendMessageRequest = new SendMessageRequest(
                    sessionService.getScoreSystemUser())
                    .withRecipient(sessionService.asScoreUser(user))
                    .withSubject("Failed to update BIE state")
                    .withBody(failureMessageBody.toString())
                    .withBodyContentType(SendMessageRequest.MARKDOWN_CONTENT_TYPE);

            BigInteger errorMessageId = messageService.asyncSendMessage(sendMessageRequest).join()
                    .getMessageIds().values().iterator().next();
            throw new DataAccessForbiddenException(sendMessageRequest.getSubject(), errorMessageId);
        }
    }

    @Transactional
    public void updateState(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BieState state) {
        ensureReusingRelationships(user, topLevelAsbiepId, state);

        BieEditTreeController treeController = getTreeController(user, topLevelAsbiepId);
        treeController.updateState(state);
    }

    @Autowired
    private AbieWriteRepository abieWriteRepository;

    @Autowired
    private AsbieWriteRepository asbieWriteRepository;

    @Autowired
    private BbieWriteRepository bbieWriteRepository;

    @Autowired
    private AsbiepWriteRepository asbiepWriteRepository;

    @Autowired
    private BbiepWriteRepository bbiepWriteRepository;

    @Autowired
    private BbieScWriteRepository bbieScWriteRepository;

    @Transactional
    public BieEditUpdateDetailResponse updateDetails(AuthenticatedPrincipal user, BieEditUpdateDetailRequest request) {
        BieEditUpdateDetailResponse response = new BieEditUpdateDetailResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        response.setAbieDetailMap(
                request.getAbieDetails().stream()
                        .map(abie ->
                                abieWriteRepository.upsertAbie(new UpsertAbieRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), abie))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        response.setAsbiepDetailMap(
                request.getAsbiepDetails().stream()
                        .map(asbiep ->
                                asbiepWriteRepository.upsertAsbiep(new UpsertAsbiepRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), asbiep))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        response.setBbiepDetailMap(
                request.getBbiepDetails().stream()
                        .map(bbiep ->
                                bbiepWriteRepository.upsertBbiep(new UpsertBbiepRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), bbiep
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        response.setAsbieDetailMap(
                request.getAsbieDetails().stream()
                        .map(asbie ->
                                asbieWriteRepository.upsertAsbie(new UpsertAsbieRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), asbie
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        response.setBbieDetailMap(
                request.getBbieDetails().stream()
                        .map(bbie ->
                                bbieWriteRepository.upsertBbie(new UpsertBbieRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), bbie
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        response.setBbieScDetailMap(
                request.getBbieScDetails().stream()
                        .map(bbieSc ->
                                bbieScWriteRepository.upsertBbieSc(new UpsertBbieScRequest(
                                        user, timestamp, request.getTopLevelAsbiepId(), bbieSc
                                ))
                        )
                        .collect(Collectors.toMap(e -> e.getHashPath(), Function.identity()))
        );

        String status = request.getTopLevelAsbiepDetail().getStatus();
        String version = request.getTopLevelAsbiepDetail().getVersion();
        Boolean inverseMode = request.getTopLevelAsbiepDetail().getInverseMode();
        UpdateTopLevelAsbiepRequest topLevelAsbiepRequest = new UpdateTopLevelAsbiepRequest(user, timestamp,
                request.getTopLevelAsbiepId(), status, version, inverseMode);
        topLevelAsbiepWriteRepository.updateTopLevelAsbiep(topLevelAsbiepRequest);

        // Verify the referred OAS_DOC
        BieForOasDoc bieForOasDoc = request.getTopLevelAsbiepDetail().getBieForOasDoc();
        if (bieForOasDoc != null) {
            openAPIDocService.updateDetails(user, new UpdateBieForOasDocRequest(sessionService.asScoreUser(user))
                    .withOasDocId(bieForOasDoc.getOasDocId())
                    .withBieForOasDocList(Arrays.asList(bieForOasDoc)));
        }

        // Issue #1635
        // If there are inherited BIEs based on this TOP_LEVEL_ASBIEP,
        // changes will be propagated to them in a cascading manner.
        propagateChangesToInheritedBIEs(user, request.getTopLevelAsbiepId());

        return response;
    }

    private void propagateChangesToInheritedBIEs(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId);

        Queue<TopLevelAsbiep> inheritedTopLevelAsbiepList = new LinkedBlockingDeque<>();
        inheritedTopLevelAsbiepList.addAll(
                topLevelAsbiepRepository.findByBasedTopLevelAsbiepId(topLevelAsbiepId));
        while (!inheritedTopLevelAsbiepList.isEmpty()) {
            TopLevelAsbiep inheritedTopLevelAsbiep = inheritedTopLevelAsbiepList.poll();
            if (inheritedTopLevelAsbiep.getState() != BieState.WIP) {
                throw new IllegalArgumentException("The BIE cannot be edited due to the inherited BIE being in " + inheritedTopLevelAsbiep.getState() + " state.");
            }

            overrideInheritedBIE(requester, inheritedTopLevelAsbiep, topLevelAsbiep);

            inheritedTopLevelAsbiepList.addAll(
                    topLevelAsbiepRepository.findByBasedTopLevelAsbiepId(inheritedTopLevelAsbiep.getTopLevelAsbiepId()));
        }
    }

    @Transactional
    public CreateExtensionResponse createLocalAbieExtension(AuthenticatedPrincipal user, BieEditAsbiepNode extension) {
        BigInteger asccpManifestId = extension.getAsccpManifestId();
        BigInteger releaseId = topLevelAsbiepRepository.findById(extension.getTopLevelAsbiepId()).getReleaseId();
        BigInteger roleOfAccManifestId = bieRepository.getRoleOfAccManifestIdByAsccpManifestId(asccpManifestId);

        CreateExtensionResponse response = new CreateExtensionResponse();

        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccManifestId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            ACC latestUeAcc = ueAcc;
            if (ueAcc.getState() == CcState.Production) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.getState() == CcState.QA) {
                response.setCanView(true);
            }
            boolean isSameBetweenRequesterAndOwner = sessionService.userId(user).equals(latestUeAcc.getOwnerUserId());
            if (isSameBetweenRequesterAndOwner) {
                response.setCanEdit(true);
                response.setCanView(true);
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }

        response.setExtensionId(createAbieExtension(user, roleOfAccManifestId, releaseId));
        return response;
    }

    @Transactional
    public CreateExtensionResponse createGlobalAbieExtension(AuthenticatedPrincipal user, BieEditAsbiepNode extension) {
        BigInteger releaseId = topLevelAsbiepRepository.findById(extension.getTopLevelAsbiepId()).getReleaseId();
        BigInteger roleOfAccManifestId = dslContext.select(Tables.ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(Tables.ACC_MANIFEST)
                .join(Tables.ACC).on(Tables.ACC_MANIFEST.ACC_ID.eq(Tables.ACC.ACC_ID))
                .where(and(
                        Tables.ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        Tables.ACC.OBJECT_CLASS_TERM.eq("All Extension")
                ))
                .fetchOneInto(BigInteger.class);

        CreateExtensionResponse response = new CreateExtensionResponse();
        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccManifestId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            ACC latestUeAcc = ueAcc;
            if (ueAcc.getState() == CcState.Production) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.getState() == CcState.QA) {
                response.setCanView(true);
            }
            boolean isSameBetweenRequesterAndOwner = sessionService.userId(user).equals(latestUeAcc.getOwnerUserId());
            if (isSameBetweenRequesterAndOwner) {
                response.setCanEdit(true);
                response.setCanView(true);
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }
        response.setExtensionId(createAbieExtension(user, roleOfAccManifestId, releaseId));
        return response;
    }

    private BigInteger createAbieExtension(AuthenticatedPrincipal user, BigInteger roleOfAccManifestId, BigInteger releaseId) {
        BieEditAcc eAcc = bieRepository.getAccByAccManifestId(roleOfAccManifestId);
        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccManifestId);

        BigInteger manifestId = extensionService.appendUserExtension(eAcc, ueAcc, releaseId, user);
        return manifestId;
    }

    @Autowired
    private AbieReadRepository abieReadRepository;

    public AbieNode getAbieDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                  BigInteger accManifestId, String hashPath) {
        return abieReadRepository.getAbieNode(topLevelAsbiepId, accManifestId, hashPath);
    }

    public AbieNode getAbieDetail(AuthenticatedPrincipal user, BigInteger abieId) {
        return abieReadRepository.getAbieNode(abieId);
    }

    @Autowired
    private AsbieReadRepository asbieReadRepository;

    public AsbieNode getAsbieDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                    BigInteger asccManifestId, String hashPath) {
        return asbieReadRepository.getAsbieNode(topLevelAsbiepId, asccManifestId, hashPath);
    }

    public AsbieNode getAsbieDetail(AuthenticatedPrincipal user, BigInteger asbieId) {
        return asbieReadRepository.getAsbieNode(asbieId);
    }

    @Autowired
    private BbieReadRepository bbieReadRepository;

    public BbieNode getBbieDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                  BigInteger bccManifestId, String hashPath) {
        return bbieReadRepository.getBbieNode(topLevelAsbiepId, bccManifestId, hashPath);
    }

    public BbieNode getBbieDetail(AuthenticatedPrincipal user, BigInteger bbieId) {
        return bbieReadRepository.getBbieNode(bbieId);
    }

    @Autowired
    private AsbiepReadRepository asbiepReadRepository;

    public AsbiepNode getAsbiepDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                      BigInteger asccpManifestId, String hashPath) {
        return asbiepReadRepository.getAsbiepNode(topLevelAsbiepId, asccpManifestId, hashPath);
    }

    public AsbiepNode getAsbiepDetail(AuthenticatedPrincipal user, BigInteger asbiepId) {
        return asbiepReadRepository.getAsbiepNode(asbiepId);
    }

    @Autowired
    private BbiepReadRepository bbiepReadRepository;

    public BbiepNode getBbiepDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                    BigInteger bccpManifestId, String hashPath) {
        return bbiepReadRepository.getBbiepNode(topLevelAsbiepId, bccpManifestId, hashPath);
    }

    public BbiepNode getBbiepDetail(AuthenticatedPrincipal user, BigInteger bbiepId) {
        return bbiepReadRepository.getBbiepNode(bbiepId);
    }

    @Autowired
    private BbieScReadRepository bbieScReadRepository;

    public BbieScNode getBbieScDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                      BigInteger dtScManifestId, String hashPath) {
        return bbieScReadRepository.getBbieScNode(topLevelAsbiepId, dtScManifestId, hashPath);
    }

    public BbieScNode getBbieScDetail(AuthenticatedPrincipal user, BigInteger bbieScId) {
        return bbieScReadRepository.getBbieScNode(bbieScId);
    }

    @Autowired
    private BdtReadRepository bdtReadRepository;

    public BdtNode getBdtDetail(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId,
                                BigInteger dtManifestId) {
        return bdtReadRepository.getBdtNode(topLevelAsbiepId, dtManifestId);
    }

    public List<BieEditUsed> getBieUsedList(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        List<BieEditUsed> usedList = new ArrayList();

        asbieReadRepository.getBieRefList(topLevelAsbiepId).stream()
                .filter(e -> e.getRefTopLevelAsbiepId() != null)
                .map(BieEditRef::getRefTopLevelAsbiepId)
                .distinct()
                .forEach(refTopLevelAsbiepId -> {
                    usedList.addAll(getBieUsedList(user, refTopLevelAsbiepId));
                });

        usedList.addAll(asbieReadRepository.getUsedAsbieList(topLevelAsbiepId));
        usedList.addAll(bbieReadRepository.getUsedBbieList(topLevelAsbiepId));
        usedList.addAll(bbieScReadRepository.getUsedBbieScList(topLevelAsbiepId));

        return usedList;
    }

    public List<BieEditRef> getBieRefList(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        return asbieReadRepository.getBieRefList(topLevelAsbiepId);
    }

    // begins supporting dynamic primitive type lists

    @Autowired
    private BdtPriRestriReadRepository bdtPriRestriReadRepository;

    public List<AvailableBdtPriRestri> availableBdtPriRestriListByBccpManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bccpManifestId) {
        return bdtPriRestriReadRepository.availableBdtPriRestriListByBccpManifestId(bccpManifestId);
    }

    @Autowired
    private BdtScPriRestriReadRepository bdtScPriRestriReadRepository;

    public List<AvailableBdtScPriRestri> availableBdtScPriRestriListByBdtScManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bdtScManifestId) {
        return bdtScPriRestriReadRepository.availableBdtScPriRestriListByBdtScManifestId(bdtScManifestId);
    }

    @Autowired
    private CodeListReadRepository codeListReadRepository;

    public List<AvailableCodeList> availableCodeListListByBccpManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bccpManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        List<CodeListState> states = Collections.emptyList();
        if (requester.isDeveloper()) {
            states = Arrays.asList(CodeListState.Published);
        }
        return codeListReadRepository.availableCodeListByBccpManifestId(bccpManifestId, states);
    }

    public List<AvailableCodeList> availableCodeListListByBdtScManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bdtScManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        List<CodeListState> states = Collections.emptyList();
        if (requester.isDeveloper()) {
            states = Arrays.asList(CodeListState.Published);
        }
        return codeListReadRepository.availableCodeListByBdtScManifestId(bdtScManifestId, states);
    }

    @Autowired
    private AgencyIdListReadRepository agencyIdListReadRepository;

    public List<AvailableAgencyIdList> availableAgencyIdListListByBccpManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bccpManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        List<AgencyIdListState> states = Collections.emptyList();
        if (requester.isDeveloper()) {
            states = Arrays.asList(AgencyIdListState.Published);
        }
        return agencyIdListReadRepository.availableAgencyIdListByBccpManifestId(bccpManifestId, states);
    }

    public List<AvailableAgencyIdList> availableAgencyIdListListByBdtScManifestId(
            AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger bdtScManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        List<AgencyIdListState> states = Collections.emptyList();
        if (requester.isDeveloper()) {
            states = Arrays.asList(AgencyIdListState.Published);
        }
        return agencyIdListReadRepository.availableAgencyIdListByBdtScManifestId(bdtScManifestId, states);
    }

    // ends supporting dynamic primitive type lists

    @Transactional
    public void useBaseBIE(AuthenticatedPrincipal user, UseBaseBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        TopLevelAsbiep topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, request.getTopLevelAsbiepId());
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to reuse end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        TopLevelAsbiep baseTopLevelAsbiep = topLevelAsbiepRepository.findById(request.getBaseTopLevelAsbiepId());

        overrideInheritedBIE(requester, topLevelAsbiep, baseTopLevelAsbiep);

        ULong topLevelAsbiepId = ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId());
        ULong baseTopLevelAsbiepId = ULong.valueOf(baseTopLevelAsbiep.getTopLevelAsbiepId());

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID, baseTopLevelAsbiepId)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .execute();
    }

    private void overrideInheritedBIE(AppUser requester,
                                      TopLevelAsbiep topLevelAsbiep,
                                      TopLevelAsbiep baseTopLevelAsbiep) {
        ULong topLevelAsbiepId = ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId());
        ULong baseTopLevelAsbiepId = ULong.valueOf(baseTopLevelAsbiep.getTopLevelAsbiepId());
        ULong requesterUserId = ULong.valueOf(requester.getAppUserId());

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
                        // Root node's display name is not overwritten.
                        if (!hasLength(inheritedAsbiep.getDisplayName()) &&
                            !topLevelAsbiep.getAsbiepId().equals(inheritedAsbiep.getAsbiepId().toBigInteger())) {
                            inheritedAsbiep.setDisplayName(baseAsbiep.getDisplayName());
                        }
                        inheritedAsbiep.update();

                        asbiepIdChangeMap.put(baseAsbiep.getAsbiepId(), inheritedAsbiep.getAsbiepId());
                    } else {
                        ULong oldAsbiepId = baseAsbiep.getAsbiepId();
                        baseAsbiep.setAsbiepId(null);
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
                        if (!hasLength(inheritedBbiep.getDisplayName())) {
                            inheritedBbiep.setDisplayName(baseBbiep.getDisplayName());
                        }
                        inheritedBbiep.update();

                        bbiepIdChangeMap.put(baseBbiep.getBbiepId(), inheritedBbiep.getBbiepId());
                    } else {
                        ULong oldBbiepId = baseBbiep.getBbiepId();
                        baseBbiep.setBbiepId(null);
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
                        if ((inheritedBbie.getBdtPriRestriId() != null && !inheritedBbie.getBdtPriRestriId().equals(baseBbie.getBdtPriRestriId())) ||
                            (inheritedBbie.getCodeListManifestId() != null && !inheritedBbie.getCodeListManifestId().equals(baseBbie.getCodeListManifestId())) ||
                            (inheritedBbie.getAgencyIdListManifestId() != null && !inheritedBbie.getAgencyIdListManifestId().equals(baseBbie.getAgencyIdListManifestId()))) {
                            inheritedBbie.setBdtPriRestriId(baseBbie.getBdtPriRestriId());
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
                        if (!hasLength(inheritedBbie.getDefaultValue())) {
                            inheritedBbie.setDefaultValue(baseBbie.getDefaultValue());
                            inheritedBbie.setFixedValue(null);
                        }
                        if (!hasLength(inheritedBbie.getFixedValue())) {
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
                        if (!hasLength(inheritedBbieSc.getDisplayName())) {
                            inheritedBbieSc.setDisplayName(baseBbieSc.getDisplayName());
                        }
                        if ((inheritedBbieSc.getDtScPriRestriId() != null && !inheritedBbieSc.getDtScPriRestriId().equals(baseBbieSc.getDtScPriRestriId())) ||
                            (inheritedBbieSc.getCodeListManifestId() != null && !inheritedBbieSc.getCodeListManifestId().equals(baseBbieSc.getCodeListManifestId())) ||
                            (inheritedBbieSc.getAgencyIdListManifestId() != null && !inheritedBbieSc.getAgencyIdListManifestId().equals(baseBbieSc.getAgencyIdListManifestId()))) {
                            inheritedBbieSc.setDtScPriRestriId(baseBbieSc.getDtScPriRestriId());
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
                        if (!hasLength(inheritedBbieSc.getDefaultValue())) {
                            inheritedBbieSc.setDefaultValue(baseBbieSc.getDefaultValue());
                            inheritedBbieSc.setFixedValue(null);
                        }
                        if (!hasLength(inheritedBbieSc.getFixedValue())) {
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
    public void removeBaseBIE(AuthenticatedPrincipal user, RemoveBaseBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(request.getTopLevelAsbiepId());
        if (!topLevelAsbiep.getOwnerUserId().equals(requester.getAppUserId())) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        }
        if (topLevelAsbiep.getState() != BieState.WIP) {
            throw new IllegalArgumentException("The BIE in " + topLevelAsbiep.getState() + " state cannot be edited.");
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .setNull(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                .execute();
    }

    private TopLevelAsbiep validateBIEAccessForReuse(AppUser requester, BigInteger topLevelAsbiepId) {
        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId);
        AppUser bieOwner = sessionService.getAppUserByUserId(topLevelAsbiep.getOwnerUserId());
        if (requester.isDeveloper() && !bieOwner.isDeveloper()) {
            throw new RoleMismatchedException();
        }
        if (!topLevelAsbiep.getOwnerUserId().equals(requester.getAppUserId())) {
            throw new NotOwnerException();
        }
        if (topLevelAsbiep.getState() != BieState.WIP) {
            throw new InvalidBieStateException(topLevelAsbiep.getState());
        }
        return topLevelAsbiep;
    }

    @Transactional
    public void reuseBIE(AuthenticatedPrincipal user, ReuseBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        TopLevelAsbiep topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, request.getTopLevelAsbiepId());
        } catch (RoleMismatchedException e) {
            throw new IllegalArgumentException("Developers are not permitted to reuse end users' BIEs.");
        } catch (NotOwnerException e) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        } catch (InvalidBieStateException e) {
            throw new IllegalArgumentException("The BIE in " + e.getInvalidState() + " state cannot be edited.");
        }

        doReuseBIE(requester, topLevelAsbiep.getTopLevelAsbiepId(),
                request.getAsbieHashPath(), request.getReuseTopLevelAsbiepId(), false);
    }

    private void doReuseBIE(AppUser requester, BigInteger topLevelAsbiepId,
                            String asbieHashPath, BigInteger reuseTopLevelAsbiepId, boolean inherit) {
        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(
                        ASBIE.HASH_PATH.eq(asbieHashPath),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .fetchOne();
        ULong prevToAsbiepId = asbieRecord.getToAsbiepId();

        ULong ownerTopLevelAsbiepOfToAsbiep =
                dslContext.select(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                        .from(ASBIEP)
                        .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId()))
                        .fetchOneInto(ULong.class);

        TopLevelAsbiep reuseTopLevelAsbiep = topLevelAsbiepRepository.findById(reuseTopLevelAsbiepId);
        ULong reuseAsbiepId = ULong.valueOf(reuseTopLevelAsbiep.getAsbiepId());
        if (!inherit || !isInInheritance(reuseAsbiepId, prevToAsbiepId)) {
            asbieRecord.setToAsbiepId(reuseAsbiepId);
        }

        asbieRecord.setIsDeprecated((byte) (reuseTopLevelAsbiep.isDeprecated() ? 1 : 0));
        asbieRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
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
                    asbieRecord.getOwnerTopLevelAsbiepId().toBigInteger());
            /*
             * Message Publishing
             */
            redisTemplate.convertAndSend(PURGE_BIE_EVENT_NAME, event);
        }

        // Issue #1635
        List<TopLevelAsbiep> inheritedTopLevelAsbiepList =
                topLevelAsbiepRepository.findByBasedTopLevelAsbiepId(topLevelAsbiepId);
        for (TopLevelAsbiep inheritedTopLevelAsbiep : inheritedTopLevelAsbiepList) {
            doReuseBIE(requester, inheritedTopLevelAsbiep.getTopLevelAsbiepId(),
                    asbieHashPath, reuseTopLevelAsbiepId, true);
        }
    }

    /**
     * This method is invoked by 'purgeBieEvent' channel subscriber.
     *
     * @param purgeBieEvent
     */
    @Transactional
    public void onPurgeBieEventReceived(PurgeBieEvent purgeBieEvent) {
        ULong topLevelAsbiepId = ULong.valueOf(purgeBieEvent.getTopLevelAsbiepId());

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
    public void removeReusedBIE(AuthenticatedPrincipal user, RemoveReusedBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(request.getTopLevelAsbiepId()))))
                .fetchOne();

        if (asbieRecord == null) {
            throw new IllegalArgumentException("Cannot fount target BIE.");
        }

        TopLevelAsbiep topLevelAsbiep;
        try {
            topLevelAsbiep = validateBIEAccessForReuse(requester, asbieRecord.getOwnerTopLevelAsbiepId().toBigInteger());
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
    public void retainReuseBIE(AuthenticatedPrincipal user, RemoveReusedBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        TopLevelAsbiep topLevelAsbiep;
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
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))
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
                    abie.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                    abie.setLastUpdateTimestamp(LocalDateTime.now());
                    abie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
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
                    asbiep.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                    asbiep.setLastUpdateTimestamp(LocalDateTime.now());
                    asbiep.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
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
                    bbiep.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                    bbiep.setLastUpdateTimestamp(LocalDateTime.now());
                    bbiep.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
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
                            asbie.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                            asbie.setLastUpdateTimestamp(LocalDateTime.now());
                            asbie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
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
                    bbie.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                    bbie.setLastUpdateTimestamp(LocalDateTime.now());
                    bbie.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
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
                            bbieSc.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                            bbieSc.setLastUpdateTimestamp(LocalDateTime.now());
                            bbieSc.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
                            return dslContext.insertInto(BBIE_SC).set(bbieSc);
                        }).collect(Collectors.toList())
        ).execute();

        dslContext.update(ASBIE)
                .set(ASBIE.TO_ASBIEP_ID, newToAsbiepIdRef.get())
                .where(ASBIE.ASBIE_ID.eq(asbieRecord.getAsbieId()))
                .execute();
    }

    @Transactional
    public void resetDetailBIE(AuthenticatedPrincipal user, ResetDetailBIERequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(request.getTopLevelAsbiepId());
        if (!topLevelAsbiep.getOwnerUserId().equals(requester.getAppUserId())) {
            throw new IllegalArgumentException("The requester is not the owner of the target BIE.");
        }
        if (topLevelAsbiep.getState() != BieState.WIP) {
            throw new IllegalArgumentException("The BIE in " + topLevelAsbiep.getState() + " state cannot be edited.");
        }

        switch (request.getBieType().toUpperCase()) {
            case "ABIE":
                AbieRecord abieRecord = dslContext.selectFrom(ABIE).where(
                        and(ABIE.PATH.eq(request.getPath()),
                                ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))).fetchOne();

                if (abieRecord == null) {
                    return;
                }

                abieRecord.setBizTerm(null);
                abieRecord.setDefinition(null);
                abieRecord.setRemark(null);
                abieRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                abieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                abieRecord.update(ABIE.BIZ_TERM,
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.LAST_UPDATED_BY,
                        ABIE.LAST_UPDATE_TIMESTAMP);

                AsbiepRecord asbiepRecord = dslContext.selectFrom(ASBIEP).where(
                        and(ASBIEP.ROLE_OF_ABIE_ID.eq(abieRecord.getAbieId()),
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))).fetchOne();

                asbiepRecord.setBizTerm(null);
                asbiepRecord.setDefinition(null);
                asbiepRecord.setRemark(null);
                asbiepRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                asbiepRecord.setLastUpdateTimestamp(LocalDateTime.now());

                asbiepRecord.update(ASBIEP.BIZ_TERM,
                        ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);

                dslContext.update(TOP_LEVEL_ASBIEP)
                        .setNull(TOP_LEVEL_ASBIEP.VERSION)
                        .setNull(TOP_LEVEL_ASBIEP.STATUS)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                        .execute();
                break;

            case "ASBIEP":
                AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE).where(
                        and(ASBIE.PATH.eq(request.getPath()),
                                ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))).fetchOne();

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
                asbieRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                asbieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                asbieRecord.update(ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.DEFINITION,
                        ASBIE.IS_NILLABLE,
                        ASBIE.LAST_UPDATED_BY,
                        ASBIE.LAST_UPDATE_TIMESTAMP);

                asbiep.setRemark(null);
                asbiep.setBizTerm(null);
                asbiep.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                asbiep.setLastUpdateTimestamp(LocalDateTime.now());

                asbiep.update(ASBIEP.BIZ_TERM,
                        ASBIEP.REMARK,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);
                break;

            case "BBIEP":
                BbieRecord bbieRecord = dslContext.selectFrom(BBIE).where(
                        and(BBIE.PATH.eq(request.getPath()),
                                BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))).fetchOne();

                if (bbieRecord == null) {
                    return;
                }

                BccManifestRecord bccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                        .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bbieRecord.getBasedBccManifestId()))
                        .fetchOne();

                BccRecord bccRecord = dslContext.selectFrom(BCC)
                        .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                        .fetchOne();

                BbiepRecord bbiepRecord = dslContext.selectFrom(BBIEP).where(BBIEP.BBIEP_ID.eq(bbieRecord.getToBbiepId())).fetchOne();

                bbieRecord.setCardinalityMin(bccRecord.getCardinalityMin());
                bbieRecord.setCardinalityMax(bccRecord.getCardinalityMax());
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(null);
                bbieRecord.setIsNull((byte) 0);
                bbieRecord.setIsNillable(bccRecord.getIsNillable());
                bbieRecord.setDefinition(null);
                bbieRecord.setExample(null);
                bbieRecord.setFacetMinLength(null);
                bbieRecord.setFacetMaxLength(null);
                bbieRecord.setFacetPattern(null);

                List<AvailableBdtPriRestri> bdtPriRestriList =
                        bdtPriRestriReadRepository.availableBdtPriRestriListByBccManifestId(bbieRecord.getBasedBccManifestId().toBigInteger());
                bdtPriRestriList = bdtPriRestriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (bdtPriRestriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                AvailableBdtPriRestri defaultBdtPriRestri = bdtPriRestriList.get(0);
                if (defaultBdtPriRestri.getCodeListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(ULong.valueOf(defaultBdtPriRestri.getCodeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (defaultBdtPriRestri.getAgencyIdListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(ULong.valueOf(defaultBdtPriRestri.getAgencyIdListManifestId()));
                } else {
                    bbieRecord.setBdtPriRestriId(ULong.valueOf(defaultBdtPriRestri.getBdtPriRestriId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                }
                bbieRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                bbieRecord.setLastUpdateTimestamp(LocalDateTime.now());

                bbieRecord.update(BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.DEFINITION,
                        BBIE.BDT_PRI_RESTRI_ID,
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
                                BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))).fetchOne();

                if (bbieScRecord == null) {
                    return;
                }

                DtScManifestRecord dtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(bbieScRecord.getBasedDtScManifestId()))
                        .fetchOne();

                DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                        .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId()))
                        .fetchOne();

                bbieScRecord.setCardinalityMin(dtScRecord.getCardinalityMin());
                bbieScRecord.setCardinalityMax(dtScRecord.getCardinalityMax());

                List<AvailableBdtScPriRestri> bdtScPriRestriList =
                        bdtScPriRestriReadRepository.availableBdtScPriRestriListByBdtScManifestId(bbieScRecord.getBasedDtScManifestId().toBigInteger());
                bdtScPriRestriList = bdtScPriRestriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (bdtScPriRestriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                AvailableBdtScPriRestri defaultBdtScPriRestri = bdtScPriRestriList.get(0);
                if (defaultBdtScPriRestri.getCodeListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(ULong.valueOf(defaultBdtScPriRestri.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (defaultBdtScPriRestri.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(ULong.valueOf(defaultBdtScPriRestri.getAgencyIdListManifestId()));
                } else {
                    bbieScRecord.setDtScPriRestriId(ULong.valueOf(defaultBdtScPriRestri.getBdtScPriRestriId()));
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
                bbieScRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
                bbieScRecord.setLastUpdateTimestamp(LocalDateTime.now());

                bbieScRecord.update(BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.DT_SC_PRI_RESTRI_ID,
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
    public void deprecateBIE(AuthenticatedPrincipal user, DeprecateBIERequest request) {
        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(request.getTopLevelAsbiepId());
        if (topLevelAsbiep == null) {
            throw new ScoreDataAccessException();
        }

        ScoreUser requester = sessionService.asScoreUser(user);
        boolean isAdmin = requester.hasRole(ScoreRole.ADMINISTRATOR);
        boolean isOwner = topLevelAsbiep.getOwnerUserId().equals(requester.getUserId());
        if (!isAdmin && !isOwner) {
            throw new ScoreDataAccessException();
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.IS_DEPRECATED, (byte) 1)
                .set(TOP_LEVEL_ASBIEP.DEPRECATED_REASON, request.getReason())
                .set(TOP_LEVEL_ASBIEP.DEPRECATED_REMARK, request.getRemark())
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY, ULong.valueOf(requester.getUserId()))
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                .execute();
    }
}
