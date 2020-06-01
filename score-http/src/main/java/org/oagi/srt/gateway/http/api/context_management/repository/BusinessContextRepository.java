package org.oagi.srt.gateway.http.api.context_management.repository;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.oagi.srt.gateway.http.api.context_management.data.BusinessContext;
import org.oagi.srt.gateway.http.api.context_management.data.BusinessContextListRequest;
import org.oagi.srt.gateway.http.api.context_management.data.BusinessContextValue;
import org.oagi.srt.gateway.http.api.context_management.data.FindBizCtxIdsByTopLevelAbieIdsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.srt.entity.jooq.Tables.*;
import static org.oagi.srt.gateway.http.helper.filter.ContainsFilterBuilder.contains;

@Repository
public class BusinessContextRepository {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep
            <Record5<ULong, String, String, Timestamp, String>> getSelectOnConditionStepForBusinessContext() {
        return dslContext.select(
                BIZ_CTX.BIZ_CTX_ID,
                BIZ_CTX.GUID,
                BIZ_CTX.NAME,
                BIZ_CTX.LAST_UPDATE_TIMESTAMP,
                APP_USER.LOGIN_ID.as("last_update_user"))
                .from(BIZ_CTX)
                .join(APP_USER).on(BIZ_CTX.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID));
    }

    public PageResponse<BusinessContext> findBusinessContexts(BusinessContextListRequest request) {
        if (request.getTopLevelAbieId() != null) {
            request.setBizCtxIds(findBizCtxIdsByTopLevelAbieId(request.getTopLevelAbieId()));
        }

        SelectOnConditionStep
                <Record5<ULong, String, String, Timestamp, String>> step = getSelectOnConditionStepForBusinessContext();

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getName())) {
            conditions.addAll(contains(request.getName(), BIZ_CTX.NAME));
        }
        if (!request.getBizCtxIds().isEmpty()) {
            conditions.add(BIZ_CTX.BIZ_CTX_ID.in(request.getBizCtxIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime())));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime())));
        }

        SelectConnectByStep
                <Record5<ULong, String, String, Timestamp, String>> conditionStep = step.where(conditions);

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        if (!StringUtils.isEmpty(pageRequest.getSortActive())) {
            switch (pageRequest.getSortActive()) {
                case "name":
                    if ("asc".equals(sortDirection)) {
                        sortField = BIZ_CTX.NAME.asc();
                    } else if ("desc".equals(sortDirection)) {
                        sortField = BIZ_CTX.NAME.desc();
                    }

                    break;

                case "lastUpdateTimestamp":
                    if ("asc".equals(sortDirection)) {
                        sortField = BIZ_CTX.LAST_UPDATE_TIMESTAMP.asc();
                    } else if ("desc".equals(sortDirection)) {
                        sortField = BIZ_CTX.LAST_UPDATE_TIMESTAMP.desc();
                    }

                    break;
            }
        }

        SelectWithTiesAfterOffsetStep
                <Record5<ULong, String, String, Timestamp, String>> offsetStep = null;
        if (sortField != null && pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        PageResponse<BusinessContext> response = new PageResponse();
        List<BusinessContext> result = (offsetStep != null) ?
                offsetStep.fetchInto(BusinessContext.class) : conditionStep.fetchInto(BusinessContext.class);
        if (!result.isEmpty()) {
            result.stream().forEach(bizCtx -> {
                bizCtx.setUsed(used(bizCtx.getBizCtxId()));
            });
        }

        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(dslContext.selectCount()
                .from(BIZ_CTX)
                .join(APP_USER).on(BIZ_CTX.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID))
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0));

        return response;
    }

    public BusinessContext findBusinessContextByBizCtxId(long bizCtxId) {
        if (bizCtxId <= 0L) {
            return null;
        }

        BusinessContext bizCtx = getSelectOnConditionStepForBusinessContext()
                .where(BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .fetchOptionalInto(BusinessContext.class).orElse(null);
        if (bizCtx == null) {
            return null;
        }

        bizCtx.setBizCtxValues(findBusinessContextValuesByBizCtxId(bizCtxId));
        bizCtx.setUsed(used(bizCtxId));

        return bizCtx;
    }

    private boolean used(long bizCtxId) {
        if (bizCtxId <= 0L) {
            return false;
        }
        return dslContext.selectCount()
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    public List<BusinessContext> findBusinessContextsByBizCtxIdIn(List<Long> bizCtxIds) {
        if (bizCtxIds == null || bizCtxIds.isEmpty()) {
            return Collections.emptyList();
        }

        return bizCtxIds.stream()
                .map(e -> findBusinessContextByBizCtxId(e))
                .collect(Collectors.toList());
    }

    private SelectOnConditionStep
            <Record9<ULong, ULong, ULong, String, ULong,
                    String, ULong, String, String>>
    getSelectOnConditionStepForBusinessContextValue() {
        return dslContext.select(
                BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID,
                BIZ_CTX_VALUE.BIZ_CTX_ID,
                CTX_CATEGORY.CTX_CATEGORY_ID,
                CTX_CATEGORY.NAME.as("ctx_category_name"),
                CTX_SCHEME.CTX_SCHEME_ID,
                CTX_SCHEME.SCHEME_NAME.as("ctx_scheme_name"),
                CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                CTX_SCHEME_VALUE.VALUE.as("ctx_scheme_value"),
                CTX_SCHEME_VALUE.MEANING.as("ctx_scheme_value_meaning")
        ).from(BIZ_CTX_VALUE)
                .join(CTX_SCHEME_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.equal(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                .join(CTX_SCHEME).on(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.equal(CTX_SCHEME.CTX_SCHEME_ID))
                .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.equal(CTX_CATEGORY.CTX_CATEGORY_ID));
    }

    public List<Long> findBizCtxIdsByTopLevelAbieId(Long topLevelAbieId) {
        if (topLevelAbieId == null || topLevelAbieId <= 0L) {
            return Collections.emptyList();
        }

        return dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchInto(Long.class);
    }

    public Map<Long, List<FindBizCtxIdsByTopLevelAbieIdsResult>> findBizCtxIdsByTopLevelAbieIds(List<Long> topLevelAbieIds) {
        if (topLevelAbieIds == null || topLevelAbieIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return dslContext.select(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID, BIZ_CTX.BIZ_CTX_ID, BIZ_CTX.NAME)
                .from(BIZ_CTX)
                .join(BIZ_CTX_ASSIGNMENT).on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.in(topLevelAbieIds))
                .fetchStream()
                .map(e -> {
                    FindBizCtxIdsByTopLevelAbieIdsResult r = new FindBizCtxIdsByTopLevelAbieIdsResult();
                    r.setTopLevelAbieId(e.get(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID).longValue());
                    r.setBizCtxId(e.get(BIZ_CTX.BIZ_CTX_ID).longValue());
                    r.setName(e.get(BIZ_CTX.NAME));
                    return r;
                })
                .collect(groupingBy(FindBizCtxIdsByTopLevelAbieIdsResult::getTopLevelAbieId));
    }

    public List<BusinessContextValue> findBusinessContextValues() {
        return getSelectOnConditionStepForBusinessContextValue()
                .fetchInto(BusinessContextValue.class);
    }

    public List<BusinessContextValue> findBusinessContextValuesByBizCtxId(long bizCtxId) {
        if (bizCtxId <= 0L) {
            return Collections.emptyList();
        }
        return getSelectOnConditionStepForBusinessContextValue()
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .fetchInto(BusinessContextValue.class);
    }
}
