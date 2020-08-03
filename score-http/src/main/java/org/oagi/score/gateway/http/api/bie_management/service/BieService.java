package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.data.BieState;
import org.oagi.score.data.BizCtx;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.entity.jooq.tables.records.AbieRecord;
import org.oagi.score.entity.jooq.tables.records.AsbiepRecord;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.score.gateway.http.api.common.data.AccessPrivilege;
import org.oagi.score.gateway.http.api.common.data.PageRequest;
import org.oagi.score.gateway.http.api.common.data.PageResponse;
import org.oagi.score.gateway.http.api.context_management.data.BizCtxAssignment;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContext;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repository.ABIERepository;
import org.oagi.score.repository.BizCtxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.data.BieState.*;
import static org.oagi.score.entity.jooq.Tables.*;
import static org.oagi.score.gateway.http.api.common.data.AccessPrivilege.*;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;

@Service
@Transactional(readOnly = true)
public class BieService {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private ABIERepository abieRepository;

    @Autowired
    private BizCtxRepository bizCtxRepository;

    @Autowired
    private DSLContext dslContext;

    public List<AsccpForBie> getAsccpListForBie(long releaseId) {
        List<AsccpForBie> asccpForBieList = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.MODULE_ID,
                Tables.MODULE.MODULE_.as("module"),
                Tables.ASCCP.STATE,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.RELEASE_ID,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP)
                .from(Tables.ASCCP)
                .leftJoin(Tables.MODULE).on(Tables.ASCCP.MODULE_ID.eq(Tables.MODULE.MODULE_ID))
                .where(and(Tables.ASCCP.REVISION_NUM.greaterThan(0),
                        Tables.ASCCP.STATE.eq(CcState.Published.getValue())))
                .fetchInto(AsccpForBie.class);

        Map<String, List<AsccpForBie>> groupingByGuidAsccpForBieList =
                asccpForBieList.stream()
                        .collect(groupingBy(AsccpForBie::getGuid));

        asccpForBieList = groupingByGuidAsccpForBieList.values().stream()
                .map(e -> CcUtility.getLatestEntity(releaseId, e))
                .filter(e -> e != null)
                .collect(Collectors.toList());

        return asccpForBieList;
    }

    @Transactional
    public BieCreateResponse createBie(User user, BieCreateRequest request) {

        long userId = sessionService.userId(user);
        long releaseId = request.getReleaseId();
        long topLevelAsbiepId = repository.createTopLevelAsbiep(userId, releaseId, Editing);

        long asccpId = request.getAsccpId();
        AccForBie accForBie = findRoleOfAccByAsccpId(asccpId, releaseId);

        long basedAccId = accForBie.getAccId();
        List<Long> bizCtxIds = request.getBizCtxIds();

        AbieRecord abieRecord = repository.createAbie(user, basedAccId, topLevelAsbiepId);
        long abieId = abieRecord.getAbieId().longValue();
        repository.createBizCtxAssignments(topLevelAsbiepId, bizCtxIds);
        AsbiepRecord asbiepRecord = repository.createAsbiep(user, asccpId, abieId, topLevelAsbiepId);
        repository.updateAsbiepIdOnTopLevelAsbiep(asbiepRecord.getAsbiepId().longValue(), topLevelAsbiepId);

        BieCreateResponse response = new BieCreateResponse();
        response.setTopLevelAsbiepId(topLevelAsbiepId);
        return response;
    }

    private AccForBie findRoleOfAccByAsccpId(long asccpId, long releaseId) {
        List<AccForBie> accForBieList = dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.CURRENT_ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.REVISION_ACTION,
                Tables.ACC.RELEASE_ID)
                .from(Tables.ACC)
                .join(Tables.ASCCP).on(Tables.ACC.CURRENT_ACC_ID.eq(Tables.ASCCP.ROLE_OF_ACC_ID))
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchInto(AccForBie.class);

        Map<String, List<AccForBie>> groupingByGuidAccForBieList =
                accForBieList.stream()
                        .collect(groupingBy(AccForBie::getGuid));

        accForBieList = groupingByGuidAccForBieList.values().stream()
                .map(e -> CcUtility.getLatestEntity(releaseId, e))
                .filter(e -> e != null)
                .collect(Collectors.toList());

        return (accForBieList.isEmpty()) ? null : accForBieList.get(0);
    }

    private SelectOnConditionStep<Record13<
            ULong, String, String, String, ULong,
            String, String, String, String, String,
            Timestamp, String, Integer>> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                Tables.ABIE.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.RELEASE.RELEASE_NUM,
                Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                Tables.APP_USER.LOGIN_ID.as("owner"),
                Tables.TOP_LEVEL_ASBIEP.VERSION,
                Tables.TOP_LEVEL_ASBIEP.STATUS,
                Tables.ASBIEP.BIZ_TERM,
                Tables.ASBIEP.REMARK,
                Tables.TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                Tables.TOP_LEVEL_ASBIEP.STATE.as("raw_state"))
                .from(Tables.TOP_LEVEL_ASBIEP)
                .join(Tables.ASBIEP).on(Tables.TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(Tables.ASBIEP.ASBIEP_ID))
                .join(Tables.ABIE).on(and(
                        Tables.ASBIEP.ROLE_OF_ABIE_ID.eq(Tables.ABIE.ABIE_ID),
                        Tables.ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(Tables.ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .join(Tables.ASCCP).on(Tables.ASCCP.ASCCP_ID.eq(Tables.ASBIEP.BASED_ASCCP_ID))
                .join(Tables.APP_USER).on(Tables.APP_USER.APP_USER_ID.eq(Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(Tables.APP_USER.as("updater")).on(Tables.APP_USER.as("updater").APP_USER_ID.eq(Tables.TOP_LEVEL_ASBIEP.LAST_UPDATED_BY))
                .join(Tables.RELEASE).on(Tables.RELEASE.RELEASE_ID.eq(Tables.TOP_LEVEL_ASBIEP.RELEASE_ID));
    }

    public PageResponse<BieList> getBieList(User user, BieListRequest request) {
        SelectOnConditionStep<Record13<
                ULong, String, String, String, ULong,
                String, String, String, String, String,
                Timestamp, String, Integer>> step = getSelectOnConditionStep();

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getPropertyTerm())) {
            conditions.addAll(contains(request.getPropertyTerm(), ASCCP.PROPERTY_TERM));
        }
        if (request.getReleaseId() != null) {
            conditions.add(RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        }
        if (request.getAsccpId() != null) {
            conditions.add(ASCCP.ASCCP_ID.eq(ULong.valueOf(request.getAsccpId())));
        }
        if (!request.getExcludePropertyTerms().isEmpty()) {
            conditions.add(Tables.ASCCP.PROPERTY_TERM.notIn(request.getExcludePropertyTerms()));
        }
        if (!request.getExcludeTopLevelAsbiepIds().isEmpty()) {
            conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(request.getExcludeTopLevelAsbiepIds()));
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(TOP_LEVEL_ASBIEP.STATE.in(request.getStates().stream().map(e -> e.getValue()).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime())));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(Tables.TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime())));
        }

        if (request.getOwnedByDeveloper() != null && request.getOwnedByDeveloper()) {
            conditions.add(APP_USER.IS_DEVELOPER.eq((byte) 1));
        }

        if (request.getAccess() != null) {
            switch (request.getAccess()) {
                case CanEdit:
                    conditions.add(
                            and(
                                    Tables.TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.getValue()),
                                    Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(sessionService.userId(user)))
                            )
                    );
                    break;

                case CanView:
                    conditions.add(
                            or(
                                    Tables.ABIE.STATE.in(Candidate.getValue(), Published.getValue()),
                                    and(
                                            Tables.TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.getValue()),
                                            Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(sessionService.userId(user)))
                                    )
                            )
                    );
                    break;
            }
        }

        SelectConnectByStep<Record13<
                ULong, String, String, String, ULong,
                String, String, String, String, String,
                Timestamp, String, Integer>> conditionStep = step.where(conditions);
        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "state":
                if ("asc".equals(sortDirection)) {
                    sortField = TOP_LEVEL_ASBIEP.STATE.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = TOP_LEVEL_ASBIEP.STATE.desc();
                }

                break;

            case "propertyTerm":
                if ("asc".equals(sortDirection)) {
                    sortField = Tables.ASCCP.PROPERTY_TERM.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = Tables.ASCCP.PROPERTY_TERM.desc();
                }

                break;

            case "releaseNum":
                if ("asc".equals(sortDirection)) {
                    sortField = Tables.RELEASE.RELEASE_NUM.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = Tables.RELEASE.RELEASE_NUM.desc();
                }

                break;

            case "lastUpdateTimestamp":
                if ("asc".equals(sortDirection)) {
                    sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.desc();
                }

                break;
        }
        int pageCount = dslContext.fetchCount(conditionStep);
        SelectWithTiesAfterOffsetStep<Record13<
                ULong, String, String, String, ULong,
                String, String, String, String, String,
                Timestamp, String, Integer>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<BieList> result = (offsetStep != null) ?
                offsetStep.fetchInto(BieList.class) : conditionStep.fetchInto(BieList.class);
        setBusinessContexts(result);

        if (!StringUtils.isEmpty(request.getBusinessContext())) {
            String nameFiltered = request.getBusinessContext();
            result = result.stream().
                    filter(bieList ->
                            bieList.getBusinessContexts().stream().anyMatch(businessContext ->
                                    businessContext.getName().toLowerCase().contains(nameFiltered.toLowerCase())))
                    .collect(Collectors.toList());
        }
        result = appendAccessPrivilege(result, user);

        PageResponse<BieList> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(pageCount);
        return response;
    }

    private void setBusinessContexts(List<BieList> bieLists) {
        Map<Long, BieList> bieListMap = bieLists.stream()
                .collect(Collectors.toMap(BieList::getTopLevelAsbiepId, Function.identity()));

        dslContext.select(
                BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID,
                BIZ_CTX.BIZ_CTX_ID,
                BIZ_CTX.NAME)
                .from(BIZ_CTX)
                .join(BIZ_CTX_ASSIGNMENT).on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(
                        bieLists.stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toList())
                ))
                .fetchStream().forEach(e -> {
            BusinessContext bc = new BusinessContext();
            bc.setBizCtxId(e.get(BIZ_CTX.BIZ_CTX_ID).longValue());
            bc.setName(e.get(BIZ_CTX.NAME));

            BieList bieList = bieListMap.get(e.get(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID).longValue());
            List<BusinessContext> bizCtxs = bieList.getBusinessContexts();
            if (bizCtxs == null) {
                bizCtxs = new ArrayList();
                bieList.setBusinessContexts(bizCtxs);
            }

            bizCtxs.add(bc);
        });
    }

    private List<BieList> appendAccessPrivilege(List<BieList> bieLists, User user) {
        long userId = sessionService.userId(user);

        bieLists.stream().forEach(e -> {
            BieState state = BieState.valueOf(e.getRawState());
            e.setState(state);

            AccessPrivilege accessPrivilege = Prohibited;
            switch (state) {
                case Initiating:
                    accessPrivilege = Unprepared;
                    break;

                case Editing:
                    if (userId == e.getOwnerUserId()) {
                        accessPrivilege = CanEdit;
                    } else {
                        accessPrivilege = Prohibited;
                    }
                    break;

                case Candidate:
                    if (userId == e.getOwnerUserId()) {
                        accessPrivilege = CanEdit;
                    } else {
                        accessPrivilege = CanView;
                    }

                    break;

                case Published:
                    accessPrivilege = CanView;
                    break;

            }

            e.setAccess(accessPrivilege.name());
        });

        return bieLists;
    }

    private List<BieList> getBieList(User user, Condition condition) {
        SelectOnConditionStep<Record10<
                ULong, String, String, String,
                ULong, String, String, String,
                Timestamp, Integer>> selectOnConditionStep = dslContext.select(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM,
                RELEASE.RELEASE_NUM,
                TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                APP_USER.LOGIN_ID.as("owner"),
                TOP_LEVEL_ASBIEP.VERSION,
                TOP_LEVEL_ASBIEP.STATUS,
                TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                TOP_LEVEL_ASBIEP.STATE.as("raw_state"))
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ABIE).on(and(
                        ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .join(ASCCP).on(ASCCP.ASCCP_ID.eq(ASBIEP.BASED_ASCCP_ID))
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID));

        List<BieList> bieLists;
        if (condition != null) {
            bieLists = selectOnConditionStep.where(condition).fetchInto(BieList.class);
        } else {
            bieLists = selectOnConditionStep.fetchInto(BieList.class);
        }

        setBusinessContexts(bieLists);
        return appendAccessPrivilege(bieLists, user);
    }

    public List<BieList> getBieList(GetBieListRequest request) {
        Long bizCtxId = request.getBizCtxId();
        Boolean excludeJsonRelated = request.getExcludeJsonRelated();
        Condition condition = null;
        if (bizCtxId != null && bizCtxId > 0L) {
            List<ULong> topLevelAsbiepIds =
                    dslContext.select(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID)
                            .from(BIZ_CTX_ASSIGNMENT)
                            .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                            .fetchInto(ULong.class);
            if (topLevelAsbiepIds.isEmpty()) {
                return Collections.emptyList();
            }

            condition = TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds);
        } else if (excludeJsonRelated != null && excludeJsonRelated == true) {
            condition = ASCCP.PROPERTY_TERM.notIn("Meta Header", "Pagination Response");
        }

        return getBieList(request.getUser(), condition);
    }

    public BizCtx findBizCtxByAbieId(long abieId) {
        long topLevelAsbiepId = abieRepository.findById(abieId).getOwnerTopLevelAsbiepId();
        // return the first biz ctx of the specific topLevelAsbiepId
        TopLevelAsbiep top = new TopLevelAsbiep();
        top.setTopLevelAsbiepId(topLevelAsbiepId);
        return bizCtxRepository.findByTopLevelAsbiep(top).get(0);
    }

    public List<BieList> getMetaHeaderBieList(User user) {
        return getBieList(user, ASCCP.PROPERTY_TERM.eq("Meta Header"));
    }

    public List<BieList> getPaginationResponseBieList(User user) {
        return getBieList(user, ASCCP.PROPERTY_TERM.eq("Pagination Response"));
    }

    @Transactional
    public void deleteBieList(User requester, List<Long> topLevelAsbiepIds) {
        if (topLevelAsbiepIds == null || topLevelAsbiepIds.isEmpty()) {
            return;
        }

        /*
         * Issue #772
         */
        ensureProperDeleteBieRequest(requester, topLevelAsbiepIds);

        dslContext.query("SET FOREIGN_KEY_CHECKS = 0").execute();

        dslContext.deleteFrom(Tables.ABIE).where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.ASBIE).where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.ASBIEP).where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        dslContext.deleteFrom(Tables.BBIE).where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.BBIEP).where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        dslContext.deleteFrom(Tables.BBIE_SC).where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.TOP_LEVEL_ASBIEP).where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
        dslContext.deleteFrom(Tables.BIZ_CTX_ASSIGNMENT).where(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();

        dslContext.query("SET FOREIGN_KEY_CHECKS = 1").execute();
    }

    private void ensureProperDeleteBieRequest(User requester, List<Long> topLevelAsbiepIds) {
        Result<Record2<Integer, ULong>> result =
                dslContext.select(TOP_LEVEL_ASBIEP.STATE, TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                        .from(TOP_LEVEL_ASBIEP)
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                                topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                        ))
                        .fetch();

        long requesterUserId = sessionService.userId(requester);
        for (Record2<Integer, ULong> record : result) {
            BieState bieState = BieState.valueOf(record.value1());
            if (bieState == Published) {
                throw new DataAccessForbiddenException("Not allowed to delete the BIE in '" + bieState + "' state.");
            }

            if (requesterUserId != record.value2().longValue()) {
                throw new DataAccessForbiddenException("Only allowed to delete the BIE by the owner.");
            }
        }
    }

    @Transactional
    public void transferOwnership(User user, long topLevelAsbiepId, String targetLoginId) {
        long ownerAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(user.getUsername()))
                .fetchOptionalInto(Long.class).orElse(0L);
        if (ownerAppUserId == 0L) {
            throw new IllegalArgumentException("Not found an owner user.");
        }

        long targetAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(targetLoginId))
                .fetchOptionalInto(Long.class).orElse(0L);
        if (targetAppUserId == 0L) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        dslContext.update(Tables.TOP_LEVEL_ASBIEP)
                .set(Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID, ULong.valueOf(targetAppUserId))
                .where(and(
                        Tables.TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .execute();
    }

    @Transactional
    public List<BizCtxAssignment> getAssignBizCtx(long topLevelAsbiepId) {
        return dslContext.select(
                BIZ_CTX_ASSIGNMENT.BIZ_CTX_ASSIGNMENT_ID,
                BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID,
                BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchInto(BizCtxAssignment.class);
    }

    @Transactional
    public void assignBizCtx(User user, long topLevelAsbiepId, Collection<Long> biz_ctx_list) {
        ArrayList<Long> newList = new ArrayList<>(biz_ctx_list);
        //remove all records of previous assignment if not in the current assignment
        dslContext.delete(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .execute();

        for (int i = 0; i < newList.size(); i++) {
            dslContext.insertInto(Tables.BIZ_CTX_ASSIGNMENT)
                    .set(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId))
                    .set(Tables.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(newList.get(i)))
                    .onDuplicateKeyIgnore()
                    .execute();
            //if a couple (biz ctx id , toplevelabieId) already exist dont insert it - just update it.
        }

    }


}
