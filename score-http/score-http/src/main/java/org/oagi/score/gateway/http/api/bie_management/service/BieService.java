package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateResponse;
import org.oagi.score.gateway.http.api.bie_management.data.BieList;
import org.oagi.score.gateway.http.api.bie_management.data.BieListRequest;
import org.oagi.score.service.common.data.*;
import org.oagi.score.gateway.http.api.context_management.data.BizCtxAssignment;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.BusinessInformationEntityRepository;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.PaginationResponse;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repository.ABIERepository;
import org.oagi.score.repository.BizCtxRepository;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieService {

    @Autowired
    private SessionService sessionService;

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
    private DSLContext dslContext;

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
        String accPath =  "ACC-" + asccpManifest.getRoleOfAccManifestId();
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
        AppUser requester = sessionService.getAppUser(user);

        PaginationResponse<BieList> result = bieRepository.selectBieLists()
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(request.getStates())
                .setReleaseId(request.getReleaseId())
                .setOwnerLoginIds(request.getOwnerLoginIds())
                .setUpdaterLoginIds(request.getUpdaterLoginIds())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setAccess(ULong.valueOf(requester.getAppUserId()), request.getAccess())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort(pageRequest.getSortActive(), pageRequest.getSortDirection())
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
                    .getBusinessContextList(getBusinessContextListRequest);

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
         * Issue #772
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

        dslContext.query("SET FOREIGN_KEY_CHECKS = 1").execute();
    }

    private void ensureProperDeleteBieRequest(AuthenticatedPrincipal requester, List<BigInteger> topLevelAsbiepIds) {
        Result<Record2<String, ULong>> result =
                dslContext.select(TOP_LEVEL_ASBIEP.STATE, TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                                topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                        ))
                        .fetch();

        BigInteger requesterUserId = sessionService.userId(requester);
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

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, String targetLoginId) {
        long ownerAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(
                        sessionService.getAppUser(user).getLoginId()
                ))
                .fetchOptionalInto(Long.class).orElse(0L);
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


}
