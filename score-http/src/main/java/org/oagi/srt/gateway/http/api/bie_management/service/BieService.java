package org.oagi.srt.gateway.http.api.bie_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.srt.data.BieState;
import org.oagi.srt.data.BizCtx;
import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.entity.jooq.tables.records.AbieRecord;
import org.oagi.srt.gateway.http.api.bie_management.data.*;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.srt.gateway.http.api.common.data.AccessPrivilege;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.oagi.srt.gateway.http.api.context_management.data.BizCtxAssignment;
import org.oagi.srt.gateway.http.api.context_management.data.BusinessContext;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.oagi.srt.repository.ABIERepository;
import org.oagi.srt.repository.BizCtxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.srt.data.BieState.*;
import static org.oagi.srt.entity.jooq.Tables.*;
import static org.oagi.srt.gateway.http.api.common.data.AccessPrivilege.*;

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

    private String GET_BIE_LIST_STATEMENT =
            "SELECT top_level_abie_id, asccp.guid, asccp.property_term, `release`.release_num, " +
                    "biz_ctx.biz_ctx_id, biz_ctx.name as biz_ctx_name, " +
                    "top_level_abie.owner_user_id, app_user.login_id as owner, abie.version, abie.`status`, " +
                    "abie.last_update_timestamp, top_level_abie.state as raw_state " +
                    "FROM top_level_abie " +
                    "JOIN abie ON top_level_abie.top_level_abie_id = abie.owner_top_level_abie_id " +
                    "AND abie.abie_id = top_level_abie.abie_id " +
                    "JOIN asbiep ON asbiep.role_of_abie_id = abie.abie_id " +
                    "JOIN asccp ON asbiep.based_asccp_id = asccp.asccp_id " +
                    "JOIN biz_ctx ON biz_ctx.biz_ctx_id = abie.biz_ctx_id " +
                    "JOIN app_user ON app_user.app_user_id = top_level_abie.owner_user_id " +
                    "JOIN `release` ON top_level_abie.release_id = `release`.release_id";

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
        long topLevelAbieId = repository.createTopLevelAbie(userId, releaseId, Editing);

        long asccpId = request.getAsccpId();
        AccForBie accForBie = findRoleOfAccByAsccpId(asccpId, releaseId);

        long basedAccId = accForBie.getAccId();
        List<Long> bizCtxIds = request.getBizCtxIds();

        AbieRecord abieRecord = repository.createAbie(user, basedAccId, topLevelAbieId);
        long abieId = abieRecord.getAbieId().longValue();
        repository.createBizCtxAssignments(topLevelAbieId, bizCtxIds);
        repository.createAsbiep(user, asccpId, abieId, topLevelAbieId);
        repository.updateAbieIdOnTopLevelAbie(abieId, topLevelAbieId);

        BieCreateResponse response = new BieCreateResponse();
        response.setTopLevelAbieId(topLevelAbieId);
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

    private SelectOnConditionStep<Record11<
            ULong, String, String, String,
            ULong, String, String, String,
            Timestamp, String, Integer>> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID,
                Tables.ABIE.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.RELEASE.RELEASE_NUM,
                Tables.TOP_LEVEL_ABIE.OWNER_USER_ID,
                Tables.APP_USER.LOGIN_ID.as("owner"),
                Tables.ABIE.VERSION,
                Tables.ABIE.STATUS,
                Tables.TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                Tables.ABIE.STATE.as("raw_state"))
                .from(Tables.TOP_LEVEL_ABIE)
                .join(Tables.ABIE).on(Tables.TOP_LEVEL_ABIE.ABIE_ID.eq(Tables.ABIE.ABIE_ID))
                .and(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(Tables.ABIE.OWNER_TOP_LEVEL_ABIE_ID))
                .join(Tables.ASBIEP).on(Tables.ASBIEP.ROLE_OF_ABIE_ID.eq(Tables.ABIE.ABIE_ID))
                .join(Tables.ASCCP).on(Tables.ASCCP.ASCCP_ID.eq(Tables.ASBIEP.BASED_ASCCP_ID))
                .join(Tables.APP_USER).on(Tables.APP_USER.APP_USER_ID.eq(Tables.TOP_LEVEL_ABIE.OWNER_USER_ID))
                .join(Tables.APP_USER.as("updater")).on(Tables.APP_USER.as("updater").APP_USER_ID.eq(Tables.TOP_LEVEL_ABIE.LAST_UPDATED_BY))
                .join(Tables.RELEASE).on(Tables.RELEASE.RELEASE_ID.eq(Tables.TOP_LEVEL_ABIE.RELEASE_ID));
    }

    public PageResponse<BieList> getBieList(User user, BieListRequest request) {
        SelectOnConditionStep<Record11<
                ULong, String, String, String,
                ULong, String, String, String,
                Timestamp, String, Integer>> step = getSelectOnConditionStep();

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getPropertyTerm())) {
            conditions.add(Tables.ASCCP.PROPERTY_TERM.containsIgnoreCase(request.getPropertyTerm().trim()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(Tables.ASCCP.PROPERTY_TERM.notIn(request.getExcludes()));
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(Tables.ABIE.STATE.in(request.getStates().stream().map(e -> e.getValue()).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime())));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(Tables.TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime())));
        }
        if (request.getAccess() != null) {
            switch (request.getAccess()) {
                case CanEdit:
                    conditions.add(
                            and(
                                    Tables.ABIE.STATE.notEqual(Initiating.getValue()),
                                    Tables.TOP_LEVEL_ABIE.OWNER_USER_ID.eq(ULong.valueOf(sessionService.userId(user)))
                            )
                    );
                    break;

                case CanView:
                    conditions.add(
                            or(
                                    Tables.ABIE.STATE.in(Candidate.getValue(), Published.getValue()),
                                    and(
                                            Tables.ABIE.STATE.notEqual(Initiating.getValue()),
                                            Tables.TOP_LEVEL_ABIE.OWNER_USER_ID.eq(ULong.valueOf(sessionService.userId(user)))
                                    )
                            )
                    );
                    break;
            }
        }

        SelectConnectByStep<Record11<
                ULong, String, String, String,
                ULong, String, String, String,
                Timestamp, String, Integer>> conditionStep = step.where(conditions);
        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
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
                    sortField = TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = Tables.TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP.desc();
                }

                break;
        }
        int pageCount = dslContext.fetchCount(conditionStep);
        SelectWithTiesAfterOffsetStep<Record11<
                ULong, String, String, String,
                ULong, String, String, String,
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
        result.forEach(bieList -> setBusinessContexts(bieList));

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

    private void setBusinessContexts(BieList bieList) {
        List<ULong> bizCtxIds = dslContext.selectDistinct(
                BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(bieList.getTopLevelAbieId())))
                .fetchInto(ULong.class);

        bieList.setBusinessContexts(
                dslContext.select(
                        BIZ_CTX.BIZ_CTX_ID,
                        BIZ_CTX.NAME,
                        BIZ_CTX.GUID,
                        BIZ_CTX.CREATION_TIMESTAMP,
                        BIZ_CTX.LAST_UPDATED_BY,
                        BIZ_CTX.LAST_UPDATE_TIMESTAMP)
                        .from(BIZ_CTX)
                        .where(BIZ_CTX.BIZ_CTX_ID.in(bizCtxIds))
                        .fetchInto(BusinessContext.class)
        );
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
                TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM,
                RELEASE.RELEASE_NUM,
                TOP_LEVEL_ABIE.OWNER_USER_ID,
                APP_USER.LOGIN_ID.as("owner"),
                ABIE.VERSION,
                ABIE.STATUS,
                TOP_LEVEL_ABIE.LAST_UPDATE_TIMESTAMP,
                TOP_LEVEL_ABIE.STATE.as("raw_state"))
                .from(TOP_LEVEL_ABIE)
                .join(ABIE).on(and(
                        TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ABIE.OWNER_TOP_LEVEL_ABIE_ID),
                        TOP_LEVEL_ABIE.ABIE_ID.eq(ABIE.ABIE_ID)))
                .join(ASBIEP).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                .join(ASCCP).on(ASCCP.ASCCP_ID.eq(ASBIEP.BASED_ASCCP_ID))
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(TOP_LEVEL_ABIE.OWNER_USER_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ABIE.RELEASE_ID));

        List<BieList> bieLists;
        if (condition != null) {
            bieLists = selectOnConditionStep.where(condition).fetchInto(BieList.class);
        } else {
            bieLists = selectOnConditionStep.fetchInto(BieList.class);
        }

        bieLists.forEach(bieList -> setBusinessContexts(bieList));
        return appendAccessPrivilege(bieLists, user);
    }

    public List<BieList> getBieList(GetBieListRequest request) {
        Long bizCtxId = request.getBizCtxId();
        Boolean excludeJsonRelated = request.getExcludeJsonRelated();
        Condition condition = null;
        if (bizCtxId != null && bizCtxId > 0L) {
            List<ULong> topLevelAbieIds =
                    dslContext.select(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID)
                    .from(BIZ_CTX_ASSIGNMENT)
                    .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                    .fetchInto(ULong.class);
            if (topLevelAbieIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            condition = TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.in(topLevelAbieIds);
        } else if (excludeJsonRelated != null && excludeJsonRelated == true) {
            condition = ASCCP.PROPERTY_TERM.notIn("Meta Header", "Pagination Response");
        }

        return getBieList(request.getUser(), condition);
    }

    public BizCtx findBizCtxByAbieId(long abieId) {
        long topLevelAbieId = abieRepository.findById(abieId).getOwnerTopLevelAbieId();
        // return the first biz ctx of the specific topLevelAbieId
        TopLevelAbie top = new TopLevelAbie();
        top.setTopLevelAbieId(topLevelAbieId);
        return bizCtxRepository.findByTopLevelAbie(top).get(0);
    }

    public List<BieList> getMetaHeaderBieList(User user) {
        return getBieList(user, ASCCP.PROPERTY_TERM.eq("Meta Header"));
    }

    public List<BieList> getPaginationResponseBieList(User user) {
        return getBieList(user, ASCCP.PROPERTY_TERM.eq("Pagination Response"));
    }

    @Transactional
    public void deleteBieList(List<Long> topLevelAbieIds) {
        if (topLevelAbieIds == null || topLevelAbieIds.isEmpty()) {
            return;
        }

        dslContext.query("SET FOREIGN_KEY_CHECKS = 0").execute();

        dslContext.deleteFrom(Tables.ABIE).where(ABIE.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();
        dslContext.deleteFrom(Tables.ASBIE).where(ASBIE.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();
        dslContext.deleteFrom(Tables.ASBIEP).where(ASBIEP.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();

        dslContext.deleteFrom(Tables.BBIE).where(BBIE.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();
        dslContext.deleteFrom(Tables.BBIEP).where(BBIEP.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();

        dslContext.deleteFrom(Tables.BBIE_SC).where(BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();
        dslContext.deleteFrom(Tables.TOP_LEVEL_ABIE).where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();
        dslContext.deleteFrom(Tables.BIZ_CTX_ASSIGNMENT).where(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.in(topLevelAbieIds)).execute();

        dslContext.query("SET FOREIGN_KEY_CHECKS = 1").execute();
    }

    @Transactional
    public void transferOwnership(User user, long topLevelAbieId, String targetLoginId) {
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

        dslContext.update(Tables.TOP_LEVEL_ABIE)
                .set(Tables.TOP_LEVEL_ABIE.OWNER_USER_ID, ULong.valueOf(targetAppUserId))
                .where(and(
                        Tables.TOP_LEVEL_ABIE.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId))
                ))
                .execute();
    }

    @Transactional
    public List<BizCtxAssignment> getAssignBizCtx(long topLevelAbieId) {
        return dslContext.select(
                BIZ_CTX_ASSIGNMENT.BIZ_CTX_ASSIGNMENT_ID,
                BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID,
                BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchInto(BizCtxAssignment.class);
    }

     @Transactional
     public void assignBizCtx(User user, long topLevelAbieId, Collection<Long> biz_ctx_list) {
         ArrayList<Long> newList = new ArrayList<>(biz_ctx_list);
         //remove all records of previous assignment if not in the current assignment
         dslContext.delete(BIZ_CTX_ASSIGNMENT)
                 .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                 .execute();

        for (int i=0; i < newList.size() ; i++) {
            dslContext.insertInto(Tables.BIZ_CTX_ASSIGNMENT)
                    .set(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                    .set(Tables.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(newList.get(i)))
                    .onDuplicateKeyIgnore()
                    .execute();
            //if a couple (biz ctx id , toplevelabieId) already exist dont insert it - just update it.
         }

     }


}
