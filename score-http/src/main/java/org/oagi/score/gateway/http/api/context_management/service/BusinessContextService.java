package org.oagi.score.gateway.http.api.context_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.common.data.PageResponse;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContext;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContextListRequest;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContextValue;
import org.oagi.score.gateway.http.api.context_management.data.SimpleContextSchemeValue;
import org.oagi.score.gateway.http.api.context_management.repository.BusinessContextRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.SrtGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.entity.jooq.Tables.*;

@Service
@Transactional(readOnly = true)
public class BusinessContextService {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BusinessContextRepository repository;

    @Autowired
    private BieRepository bieRepository;

    public PageResponse<BusinessContext> getBusinessContextList(BusinessContextListRequest request) {
        return repository.findBusinessContexts(request);
    }

    public BusinessContext getBusinessContext(long bizCtxId) {
        return repository.findBusinessContextByBizCtxId(bizCtxId);
    }

    public List<BusinessContext> getBusinessContexts(List<Long> bizCtxIds) {
        return repository.findBusinessContextsByBizCtxIdIn(bizCtxIds);
    }

    public List<BusinessContext> getBusinessContextsByTopLevelAsbiepId(long topLevelAsbiepId) {
        List<Long> bizCtxIds = bieRepository.getBizCtxIdByTopLevelAsbiepId(topLevelAsbiepId);
        if (bizCtxIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getBusinessContexts(bizCtxIds);
    }

    public List<BusinessContextValue> getBusinessContextValues() {
        return repository.findBusinessContextValues();
    }

    public List<SimpleContextSchemeValue> getSimpleContextSchemeValueList(long ctxSchemeId) {
        return dslContext.select(
                CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                CTX_SCHEME_VALUE.VALUE,
                CTX_SCHEME_VALUE.MEANING
        ).from(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(ULong.valueOf(ctxSchemeId)))
                .fetchInto(SimpleContextSchemeValue.class);
    }

    public List<BusinessContextValue> getBusinessContextValuesByBusinessCtxId(long businessCtxID) {
        return dslContext.select(
                BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID,
                BIZ_CTX_VALUE.BIZ_CTX_ID,
                BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID
        ).from(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(businessCtxID)))
                .fetchInto(BusinessContextValue.class);
    }

    @Transactional
    public void insert(AuthenticatedPrincipal user, BusinessContext bizCtx) {
        if (StringUtils.isEmpty(bizCtx.getGuid())) {
            bizCtx.setGuid(SrtGuid.randomGuid());
        }

        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long bizCtxId = dslContext.insertInto(BIZ_CTX,
                BIZ_CTX.GUID,
                BIZ_CTX.NAME,
                BIZ_CTX.CREATED_BY,
                BIZ_CTX.LAST_UPDATED_BY,
                BIZ_CTX.CREATION_TIMESTAMP,
                BIZ_CTX.LAST_UPDATE_TIMESTAMP)
                .values(
                        bizCtx.getGuid(),
                        bizCtx.getName(),
                        userId, userId, timestamp, timestamp)
                .returning(BIZ_CTX.BIZ_CTX_ID).fetchOne().getBizCtxId().longValue();

        for (BusinessContextValue bizCtxValue : bizCtx.getBizCtxValues()) {
            insert(bizCtxId, bizCtxValue);
        }
    }

    @Transactional
    public void insert(long bizCtxId, BusinessContextValue bizCtxValue) {
        dslContext.insertInto(BIZ_CTX_VALUE,
                BIZ_CTX_VALUE.BIZ_CTX_ID,
                BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID)
                .values(
                        ULong.valueOf(bizCtxId),
                        ULong.valueOf(bizCtxValue.getCtxSchemeValueId()))
                .execute();
    }

    @Transactional
    public void update(AuthenticatedPrincipal user, BusinessContext bizCtx) {
        dslContext.update(BIZ_CTX)
                .set(BIZ_CTX.NAME, bizCtx.getName())
                .set(BIZ_CTX.LAST_UPDATED_BY, ULong.valueOf(sessionService.userId(user)))
                .set(BIZ_CTX.LAST_UPDATE_TIMESTAMP, new Timestamp(System.currentTimeMillis()))
                .where(BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(bizCtx.getBizCtxId())))
                .execute();

        update(bizCtx.getBizCtxId(), bizCtx.getBizCtxValues());
    }

    @Transactional
    public void update(final long bizCtxId, List<BusinessContextValue> bizCtxValues) {
        List<Long> oldBizCtxValueIds = dslContext.select(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID)
                .from(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .fetchInto(Long.class);

        Map<Long, BusinessContextValue> newBizCtxValues = bizCtxValues.stream()
                .filter(e -> e.getBizCtxValueId() > 0L)
                .collect(Collectors.toMap(BusinessContextValue::getBizCtxValueId, Function.identity()));

        oldBizCtxValueIds.removeAll(newBizCtxValues.keySet());
        for (long deleteBizCtxValueId : oldBizCtxValueIds) {
            delete(bizCtxId, deleteBizCtxValueId);
        }

        for (BusinessContextValue bizCtxValue : newBizCtxValues.values()) {
            update(bizCtxId, bizCtxValue);
        }

        for (BusinessContextValue bizCtxValue : bizCtxValues.stream()
                .filter(e -> e.getBizCtxValueId() == 0L)
                .collect(Collectors.toList())) {
            insert(bizCtxId, bizCtxValue);
        }
    }

    @Transactional
    public void update(long bizCtxId, BusinessContextValue bizCtxValue) {
        dslContext.update(BIZ_CTX_VALUE)
                .set(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID, ULong.valueOf(bizCtxValue.getCtxSchemeValueId()))
                .where(and(
                        BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(ULong.valueOf(bizCtxValue.getBizCtxValueId())),
                        BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId))
                ))
                .execute();
    }

    @Transactional
    public void delete(long bizCtxId, long bizCtxValueId) {
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(and(
                        BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(ULong.valueOf(bizCtxValueId)),
                        BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId))))
                .execute();
    }

    @Transactional
    public void assign(long bizCtxId, long topLevelAsbiepId) {
        dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId))
                .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(bizCtxId))
                .execute();
    }

    @Transactional
    public void dismiss(long bizCtxId, long topLevelAsbiepId) {
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(and(
                        BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId))
                ))
                .execute();
    }

    @Transactional
    public void delete(long bizCtxId) {
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .execute();
        dslContext.deleteFrom(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId)))
                .execute();
    }

    @Transactional
    public void delete(List<Long> bizCtxIds) {
        if (bizCtxIds == null || bizCtxIds.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.in(
                        bizCtxIds.stream().map(
                                e -> ULong.valueOf(e)).collect(Collectors.toList())
                ))
                .execute();
        dslContext.deleteFrom(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.in(
                        bizCtxIds.stream().map(
                                e -> ULong.valueOf(e)).collect(Collectors.toList())
                ))
                .execute();
    }
}
