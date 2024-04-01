package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.data.BizCtx;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.data.*;
import org.oagi.score.gateway.http.api.business_term_management.data.AsbieListRecord;
import org.oagi.score.gateway.http.api.context_management.data.BizCtxAssignment;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.BusinessInformationEntityRepository;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.PaginationResponse;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.bie.model.GetReuseBieListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.OasMessageBody;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.message.model.SendMessageRequest;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repository.ABIERepository;
import org.oagi.score.repository.BizCtxRepository;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.oagi.score.service.common.data.*;
import org.oagi.score.service.message.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieEditService bieEditService;

    @Autowired
    private OpenAPIDocService oasDocService;

    @Autowired
    private ABIERepository abieRepository;

    @Autowired
    private BizCtxRepository bizCtxRepository;

    @Autowired
    private CoreComponentRepository ccRepository;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Transactional
    public BieCreateResponse createBie(AuthenticatedPrincipal user, BieCreateRequest request) {
        BigInteger userId = sessionService.userId(user);
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.asccpManifestId() == null) {
            throw new IllegalArgumentException("`ASCCP` parameter must not be null.");
        }

        List<BigInteger> bizCtxIds = request.getBizCtxIds();
        if (bizCtxIds == null || bizCtxIds.isEmpty()) {
            throw new IllegalArgumentException("`bizCtxIds` parameter must not be null.");
        }

        AsccpManifestRecord asccpManifest =
                ccRepository.getAsccpManifestByManifestId(request.asccpManifestId());
        if (asccpManifest == null) {
            throw new IllegalArgumentException();
        }
        AccManifestRecord accManifestRecord = ccRepository.getAccManifestByManifestId(asccpManifest.getRoleOfAccManifestId());
        AccRecord accRecord = ccRepository.getAccById(accManifestRecord.getAccId());
        if (OagisComponentType.valueOf(accRecord.getOagisComponentType()).isGroup()) {
            throw new IllegalArgumentException("Cannot create BIE of `ASCCP` with group `ACC`.");
        }

        String asccpPath = "ASCCP-" + asccpManifest.getAsccpManifestId();
        String accPath = "ACC-" + asccpManifest.getRoleOfAccManifestId();
        accPath = String.join(">",
                Arrays.asList(asccpPath, accPath));

        long millis = System.currentTimeMillis();

        ULong topLevelAsbiepId = bieRepository.insertTopLevelAsbiep()
                .setUserId(userId)
                .setReleaseId(asccpManifest.getReleaseId())
                .setTimestamp(millis)
                .execute();

        ULong abieId = bieRepository.insertAbie()
                .setUserId(userId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setAccManifestId(asccpManifest.getRoleOfAccManifestId())
                .setPath(accPath)
                .setTimestamp(millis)
                .execute();

        bieRepository.insertBizCtxAssignments()
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setBizCtxIds(bizCtxIds)
                .execute();

        ULong asbiepId = bieRepository.insertAsbiep()
                .setAsccpManifestId(asccpManifest.getAsccpManifestId())
                .setRoleOfAbieId(abieId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setPath(asccpPath)
                .setUserId(userId)
                .setTimestamp(millis)
                .execute();

        bieRepository.updateTopLevelAsbiep()
                .setAsbiepId(asbiepId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .execute();

        BieCreateResponse response = new BieCreateResponse();
        response.setTopLevelAsbiepId(topLevelAsbiepId.toBigInteger());
        return response;
    }

    public PageResponse<BieList> getBieList(AuthenticatedPrincipal user, BieListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        AppUser requester = sessionService.getAppUserByUsername(user);
        List<ULong> userTenantIds = tenantService
                .getUserTenantsRoleByUserId(requester.getAppUserId());

        PaginationResponse<BieList> result = bieRepository.selectBieLists()
                .setDen(request.getDen())
                .setPropertyTerm(request.getPropertyTerm())
                .setVersion(request.getVersion())
                .setRemark(request.getRemark())
                .setBusinessContext(request.getBusinessContext())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setTopLevelAsbiepIds(request.getTopLevelAsbiepIds())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(request.getStates())
                .setDeprecated(request.getDeprecated())
                .setReleaseIds(request.getReleaseIds())
                .setOwnerLoginIds(request.getOwnerLoginIds())
                .setUpdaterLoginIds(request.getUpdaterLoginIds())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setAccess(ULong.valueOf(requester.getAppUserId()), request.getAccess())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setTenantBusinessCtx(requester.isAdmin(), userTenantIds)
                .setSort(pageRequest.getSortActives(), pageRequest.getSortDirections())
                .setOffset(pageRequest.getOffset(), pageRequest.getPageSize())
                .fetchInto(BieList.class);

        List<BieList> bieLists = result.getResult();
        bieLists.forEach(bieList -> {
            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(authenticationService.asScoreUser(user))
                            .withTopLevelAsbiepIdList(Arrays.asList(bieList.getTopLevelAsbiepId()))
                            .withName(request.getBusinessContext());

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, applicationConfigurationService.isTenantEnabled());

            bieList.setBusinessContexts(getBusinessContextListResponse.getResults());
            bieList.setAccess(
                    AccessPrivilege.toAccessPrivilege(requester, bieList.getOwnerUserId(), bieList.getState())
            );
        });

        PageResponse<BieList> response = new PageResponse();
        response.setList(bieLists);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }

    public PageResponse<BieList> getUsageOfBieList(AuthenticatedPrincipal user, BieListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        AppUser requester = sessionService.getAppUserByUsername(user);
        PageResponse<BieList> response = new PageResponse();
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());

        List<BigInteger> reusingTopLevelAsbiepIds = bieRepository.getReusingTopLevelAsbiepIds(request.getUsageTopLevelAsbiepId());

        if (reusingTopLevelAsbiepIds.isEmpty()) {
            response.setList(Collections.emptyList());
            response.setLength(0);
            return response;
        }

        PaginationResponse<BieList> result = bieRepository.selectBieLists()
                .setIncludeTopLevelAsbiepIds(reusingTopLevelAsbiepIds)
                .setSort(pageRequest.getSortActives(), pageRequest.getSortDirections())
                .setOffset(pageRequest.getOffset(), pageRequest.getPageSize())
                .fetchInto(BieList.class);

        List<BieList> bieLists = result.getResult();
        bieLists.forEach(bieList -> {

            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(authenticationService.asScoreUser(user))
                            .withTopLevelAsbiepIdList(Arrays.asList(bieList.getTopLevelAsbiepId()))
                            .withName(request.getBusinessContext());

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, false);

            bieList.setBusinessContexts(getBusinessContextListResponse.getResults());
            bieList.setAccess(
                    AccessPrivilege.toAccessPrivilege(requester, bieList.getOwnerUserId(), bieList.getState())
            );
        });

        response.setList(bieLists);
        response.setLength(result.getPageCount());
        return response;
    }

    public PageResponse<AsbieListRecord> getAsbieAndBbieList(AuthenticatedPrincipal user, BieListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        AppUser requester = sessionService.getAppUserByUsername(user);

        PaginationResponse<AsbieListRecord> result = bieRepository.selectBieLists()
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setStates(request.getStates())
                .setBieIdAndType(request.getBieId(), request.getTypes())
                .setReleaseIds(request.getReleaseIds())
                .setOwnerLoginIds(request.getOwnerLoginIds())
                .setUpdaterLoginIds(request.getUpdaterLoginIds())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setAsccBccDen(request.getAsccBccDen())
                .setAccess(ULong.valueOf(requester.getAppUserId()), request.getAccess())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort(pageRequest.getSortActives(), pageRequest.getSortDirections())
                .setOffset(pageRequest.getOffset(), pageRequest.getPageSize())
                .fetchAsbieBbieInto(request.getTypes(), AsbieListRecord.class);

        List<AsbieListRecord> bieLists = result.getResult();
        bieLists.forEach(bieList -> {
            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(authenticationService.asScoreUser(user))
                            .withTopLevelAsbiepIdList(
                                    (bieList.getTopLevelAsbiepId() == null) ?
                                            Collections.emptyList() : Arrays.asList(bieList.getTopLevelAsbiepId()))
                            .withName(request.getBusinessContext());

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, false);

            bieList.setBusinessContexts(getBusinessContextListResponse.getResults());
            bieList.setAccess(
                    AccessPrivilege.toAccessPrivilege(requester, bieList.getOwnerUserId(), bieList.getState())
            );
        });

        PageResponse<AsbieListRecord> response = new PageResponse();
        response.setList(bieLists);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }

    public BizCtx findBizCtxByAbieId(BigInteger abieId) {
        BigInteger topLevelAsbiepId = abieRepository.findById(abieId).getOwnerTopLevelAsbiepId();
        // return the first biz ctx of the specific topLevelAsbiepId
        TopLevelAsbiep top = new TopLevelAsbiep();
        top.setTopLevelAsbiepId(topLevelAsbiepId);
        return bizCtxRepository.findByTopLevelAsbiep(top).get(0);
    }

    @Transactional
    public void deleteBieList(AuthenticatedPrincipal requester, List<BigInteger> topLevelAsbiepIds) {
        if (topLevelAsbiepIds == null || topLevelAsbiepIds.isEmpty()) {
            return;
        }

        /*
         * Issue #772, #1010
         */
        ensureProperDeleteBieRequest(requester, topLevelAsbiepIds);

        dslContext.query("SET FOREIGN_KEY_CHECKS = 0").execute();

        dslContext.deleteFrom(ABIE).where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(ASBIE).where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(ASBIEP).where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        dslContext.deleteFrom(Tables.BBIE).where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.BBIEP).where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        dslContext.deleteFrom(Tables.BBIE_SC).where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.TOP_LEVEL_ASBIEP).where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.BIZ_CTX_ASSIGNMENT).where(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        // Issue #1492
        List<ULong> oasMessageBodyIdList = dslContext.selectDistinct(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .from(OAS_MESSAGE_BODY)
                .where(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetchInto(ULong.class);
        if (!oasMessageBodyIdList.isEmpty()) {
            List<ULong> oasRequestIdList = dslContext.selectDistinct(OAS_REQUEST.OAS_REQUEST_ID)
                    .from(OAS_REQUEST)
                    .where(OAS_REQUEST.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
                    .fetchInto(ULong.class);
            if (!oasRequestIdList.isEmpty()) {
                dslContext.deleteFrom(OAS_REQUEST_PARAMETER)
                        .where(OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(oasRequestIdList))
                        .execute();
                dslContext.deleteFrom(OAS_REQUEST)
                        .where(OAS_REQUEST.OAS_REQUEST_ID.in(oasRequestIdList))
                        .execute();
            }
            List<ULong> oasResponseIdList = dslContext.selectDistinct(OAS_RESPONSE.OAS_RESPONSE_ID)
                    .from(OAS_RESPONSE)
                    .where(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
                    .fetchInto(ULong.class);
            if (!oasResponseIdList.isEmpty()) {
                dslContext.deleteFrom(OAS_RESPONSE_HEADERS)
                        .where(OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(oasResponseIdList))
                        .execute();
                dslContext.deleteFrom(OAS_RESPONSE)
                        .where(OAS_RESPONSE.OAS_RESPONSE_ID.in(oasResponseIdList))
                        .execute();
            }
            dslContext.deleteFrom(OAS_MESSAGE_BODY)
                    .where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
                    .execute();
        }

        // Issue #1615
        List<ULong> biePackageIdList = dslContext.select(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID)
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetchInto(ULong.class);

        dslContext.deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .execute();

        dslContext.query("SET FOREIGN_KEY_CHECKS = 1").execute();

        List<ULong> topLevelAsbiepListThatHasThisAsSource = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetchInto(ULong.class);
        if (!topLevelAsbiepListThatHasThisAsSource.isEmpty()) {
            dslContext.update(TOP_LEVEL_ASBIEP)
                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID)
                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_ACTION)
                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP)
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepListThatHasThisAsSource))
                    .execute();
        }
    }

    private void ensureProperDeleteBieRequest(AuthenticatedPrincipal prinpical, List<BigInteger> topLevelAsbiepIds) {
        ScoreUser requester = sessionService.asScoreUser(prinpical);
        // Issue #1569
        // check to see if the BIE is referenced in an OpenAPI document
        Result<Record1<ULong>> resultForOasDocId = dslContext.select(OAS_DOC.OAS_DOC_ID)
                .from(OAS_DOC)
                .fetch();
        List<BigInteger> oasDocIds = Collections.emptyList();
        if (resultForOasDocId != null) {
            oasDocIds = resultForOasDocId.stream().map(r -> r.value1().toBigInteger()).collect(Collectors.toList());
        }
        if (!oasDocIds.isEmpty()) {
            for (BigInteger oasDocId : oasDocIds) {
                List<BigInteger> topLevelAsbiepIdsInOasDoc = new ArrayList<>();
                GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(requester);
                getBieForOasDocRequest.setOasDocId(oasDocId);
                GetBieForOasDocResponse bieForOasDocTable = oasDocService.getBieForOasDoc(getBieForOasDocRequest);
                List<BieForOasDoc> bieListForOasDoc = bieForOasDocTable.getResults();
                if (bieListForOasDoc != null) {
                    topLevelAsbiepIdsInOasDoc = bieListForOasDoc.stream().map(s -> s.getTopLevelAsbiepId()).collect(Collectors.toList());
                    if (topLevelAsbiepIdsInOasDoc != null) {
                        for (BigInteger topLevelAsbiepId : topLevelAsbiepIds) {
                            if (topLevelAsbiepIdsInOasDoc.contains(topLevelAsbiepId)) {
                                throw new DataAccessForbiddenException("Cannot delete the BIE '" + topLevelAsbiepId + "'. please remove the BIE from the OpenAPI document first.");
                            }
                        }
                    }
                }
            }
        }

        Result<Record2<String, ULong>> result =
                dslContext.select(TOP_LEVEL_ASBIEP.STATE, TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                                topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                        ))
                        .fetch();

        // Issue #1576
        // Administrator can discard BIEs in any state.
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            BigInteger requesterUserId = requester.getUserId();
            for (Record2<String, ULong> record : result) {
                BieState bieState = BieState.valueOf(record.value1());
                if (bieState == BieState.Production) {
                    throw new DataAccessForbiddenException("Not allowed to delete the BIE in '" + bieState + "' state.");
                }

                if (!requesterUserId.equals(record.value2().toBigInteger())) {
                    throw new DataAccessForbiddenException("Only allowed to delete the BIE by the owner.");
                }
            }
        }

        // Issue #1010
        int failureCount = 0;
        StringBuilder failureMessageBody = new StringBuilder();
        BieReadRepository bieReadRepository = scoreRepositoryFactory.createBieReadRepository();
        for (BigInteger topLevelAsbiepId : topLevelAsbiepIds) {
            List<org.oagi.score.repo.api.bie.model.TopLevelAsbiep> reusedTopLevelAsbiepList =
                    bieReadRepository.getReuseBieList(new GetReuseBieListRequest(requester)
                                    .withTopLevelAsbiepId(topLevelAsbiepId, true))
                            .getTopLevelAsbiepList();

            if (!reusedTopLevelAsbiepList.isEmpty()) {
                failureCount += 1;
                Record source = selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot be discarded due to the referential integrity violation by following BIEs:")
                        .append("\n\n");
                for (org.oagi.score.repo.api.bie.model.TopLevelAsbiep target : reusedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.getPropertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.getTopLevelAsbiepId())
                            .append(") (")
                            .append(target.getGuid())
                            .append(")\n");
                }
            }
        }

        if (failureCount > 0) { // i.e. failed?
            SendMessageRequest sendMessageRequest = new SendMessageRequest(
                    sessionService.getScoreSystemUser())
                    .withRecipient(requester)
                    .withSubject("Failed to discard BIE" + ((failureCount > 1) ? "s" : ""))
                    .withBody(failureMessageBody.toString())
                    .withBodyContentType(SendMessageRequest.MARKDOWN_CONTENT_TYPE);

            BigInteger errorMessageId = messageService.asyncSendMessage(sendMessageRequest).join()
                    .getMessageIds().values().iterator().next();
            throw new DataAccessForbiddenException(sendMessageRequest.getSubject(), errorMessageId);
        }
    }

    public Record2<String, String> selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(
            ULong topLevelAsbiepId) {
        return dslContext.select(ASCCP.PROPERTY_TERM, ASBIEP.GUID)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .fetchOne();
    }

    private SelectConditionStep<Record3<ULong, String, String>> selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepIdList(
            List<ULong> topLevelAsbiepGuidList) {
        Condition cond = (topLevelAsbiepGuidList.size() == 1) ?
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepGuidList.get(0)) :
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepGuidList);
        return dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID, ASCCP.PROPERTY_TERM, ASBIEP.GUID)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(cond);
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, String targetLoginId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        long ownerAppUserId;
        // Issue #1576
        // Even if the administrator does not own BIE, they can transfer ownership.
        if (requester.isAdmin()) {
            ownerAppUserId = dslContext.select(TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                    .from(TOP_LEVEL_ASBIEP)
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                    .fetchOptionalInto(Long.class).orElse(0L);
        } else {
            ownerAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                    .from(APP_USER)
                    .where(APP_USER.LOGIN_ID.equalIgnoreCase(requester.getLoginId()))
                    .fetchOptionalInto(Long.class).orElse(0L);
        }
        if (ownerAppUserId == 0L) {
            throw new IllegalArgumentException("Not found an owner user.");
        }

        Long targetAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(targetLoginId))
                .fetchOptionalInto(Long.class).orElse(null);
        if (targetAppUserId == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        if (dslContext.selectCount()
                .from(TOP_LEVEL_ASBIEP)
                .where(and(
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) == 0) {
            throw new IllegalArgumentException("This BIE is not owned by the current user.");
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.OWNER_USER_ID, ULong.valueOf(targetAppUserId))
                .where(and(
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .execute();
    }

    @Transactional
    public List<BizCtxAssignment> getAssignBizCtx(BigInteger topLevelAsbiepId) {
        return dslContext.select(
                        BIZ_CTX_ASSIGNMENT.BIZ_CTX_ASSIGNMENT_ID,
                        BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID,
                        BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchInto(BizCtxAssignment.class);
    }

    @Transactional
    public void assignBizCtx(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, Collection<Long> biz_ctx_list) {
        ArrayList<Long> newList = new ArrayList<>(biz_ctx_list);
        //remove all records of previous assignment if not in the current assignment
        dslContext.delete(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .execute();

        for (int i = 0; i < newList.size(); i++) {
            dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                    .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId))
                    .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(newList.get(i)))
                    .onDuplicateKeyIgnore()
                    .execute();
            //if a couple (biz ctx id , toplevelasbiepId) already exist dont insert it - just update it.
        }

    }

    public void fireBieEvent(BieEvent event) {
        try {
            simpMessagingTemplate.convertAndSend("/topic/bie/" + event.getTopLevelAsbiepId(), event);
        } catch (Exception ignore) {
            logger.error("Couldn't send BIE event: " + event, ignore);
        }
    }

    @Transactional
    public void updateStateBieList(AuthenticatedPrincipal user, BieUpdateStateListRequest request) {
        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            bieEditService.updateState(user, topLevelAsbiepId, request.getToState());
        });
    }

    @Transactional
    public void transferOwnershipList(AuthenticatedPrincipal user, BieTransferOwnershipListRequest request) {
        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            transferOwnership(user, topLevelAsbiepId, request.getTargetLoginId());
        });
    }

}
