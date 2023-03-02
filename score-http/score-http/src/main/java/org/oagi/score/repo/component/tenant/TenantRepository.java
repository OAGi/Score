package org.oagi.score.repo.component.tenant;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.tenant_management.data.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantListRequest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TenantBusinessCtxRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TenantRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.UserTenantRecord;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class TenantRepository {

    @Autowired
    private DSLContext dslContext;

    public List<ULong> getUserTenantsRoleByUserId(BigInteger userId) {
        return dslContext.select(TENANT.TENANT_ID).from(TENANT).join(USER_TENANT)
                .on(TENANT.TENANT_ID.eq(USER_TENANT.TENANT_ID)).where(USER_TENANT.APP_USER_ID.eq(ULong.valueOf(userId)))
                .fetch(TENANT.TENANT_ID);
    }

    public TenantInfo getTenantById(BigInteger tenantId) {
        TenantInfo tenant = dslContext.select(TENANT.TENANT_ID, TENANT.NAME).from(TENANT)
                .where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantId))).fetchOneInto(TenantInfo.class);

        tenant.setUsersCount(dslContext.selectCount().from(USER_TENANT)
                .where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId))).fetchOne().value1());

        tenant.setBusinessCtxCount(dslContext.selectCount().from(TENANT_BUSINESS_CTX)
                .where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId))).fetchOne().value1());

        return tenant;
    }

    public void deleteTenant(BigInteger tenantId) {
        dslContext.deleteFrom(USER_TENANT).where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId))).execute();

        dslContext.deleteFrom(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
                .execute();

        dslContext.deleteFrom(TENANT).where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantId))).execute();
    }

    public List<String> getTenantNameByBusinessCtxId(BigInteger businessCtxId) {
        return dslContext.select(TENANT.NAME).from(TENANT).join(TENANT_BUSINESS_CTX)
                .on(TENANT.TENANT_ID.eq(TENANT_BUSINESS_CTX.TENANT_ID))
                .where(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(ULong.valueOf(businessCtxId))).fetch(TENANT.NAME);
    }

    public PageResponse<Tenant> getAllTenantsRole(TenantListRequest tenantRequest) {

        PageResponse<Tenant> response = new PageResponse<>();
        SelectJoinStep<Record2<ULong, String>> step = null;
        SelectConnectByStep<Record2<ULong, String>> conditionStep = null;
        SelectWithTiesAfterOffsetStep<Record2<ULong, String>> offsetStep = null;
        int pageCount = 0;

        step = dslContext.select(TENANT.TENANT_ID, TENANT.NAME).from(TENANT);

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(tenantRequest.getName())) {
            conditions.addAll(contains(tenantRequest.getName(), TENANT.NAME));
        }
        conditionStep = step.where(conditions);
        pageCount = dslContext.fetchCount(conditionStep);
        SortField sortField = TENANT.NAME.asc();

        if ("asc".equals(tenantRequest.getPageRequest().getSortDirection())) {
            sortField = TENANT.NAME.asc();
        } else if ("desc".equals(tenantRequest.getPageRequest().getSortDirection())) {
            sortField = TENANT.NAME.desc();
        }

        offsetStep = conditionStep.orderBy(sortField).limit(tenantRequest.getPageRequest().getOffset(),
                tenantRequest.getPageRequest().getPageSize());

        response.setList(
                (offsetStep != null) ? offsetStep.fetchInto(Tenant.class) : conditionStep.fetchInto(Tenant.class));
        response.setPage(tenantRequest.getPageRequest().getPageIndex());
        response.setSize(tenantRequest.getPageRequest().getPageSize());
        response.setLength(pageCount);
        return response;

    }

    public boolean createTenant(String name) {
        if (checkIfTenantNameExists(name)) {
            return false;
        }

        TenantRecord record = new TenantRecord();
        record.setName(name);
        dslContext.insertInto(TENANT).set(record).returning().fetchOne().getTenantId().longValue();

        return true;
    }

    public boolean updateTenant(BigInteger tenantId, String name) {
        Tenant tenant = getTenantById(tenantId);

        if (tenant != null && !checkIfTenantNameExists(name) && !tenant.getName().equals(name)) {
            dslContext.update(TENANT).set(TENANT.NAME, name).where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
                    .execute();

            return true;
        }
        return false;
    }

    public void addUserToTenant(BigInteger tenantId, BigInteger appUserId) {
        if (!checkIfUserTenantExists(tenantId, appUserId)) {
            UserTenantRecord record = new UserTenantRecord();
            record.setTenantId(ULong.valueOf(tenantId));
            record.setAppUserId(ULong.valueOf(appUserId));
            dslContext.insertInto(USER_TENANT).set(record).returning().fetchOne().getUserTenantId().longValue();
        }
    }

    public void deleteTenantUser(BigInteger tenantId, BigInteger appUserId) {
        if (checkIfUserTenantExists(tenantId, appUserId)) {
            dslContext.deleteFrom(USER_TENANT).where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
                    .and(USER_TENANT.APP_USER_ID.eq(ULong.valueOf(appUserId))).execute();
        }
    }

    public void addBusinessCtxToTenant(BigInteger tenantId, BigInteger businessCtxId) {
        if (!checkIfTenantCtxExists(tenantId, businessCtxId)) {
            TenantBusinessCtxRecord record = new TenantBusinessCtxRecord();
            record.setTenantId(ULong.valueOf(tenantId));
            record.setBizCtxId(ULong.valueOf(businessCtxId));
            dslContext.insertInto(TENANT_BUSINESS_CTX).set(record).returning().fetchOne().getTenantBusinessCtxId()
                    .longValue();
        }
    }

    public void deleteTenantBusinessCtx(BigInteger tenantId, BigInteger businessCtxId) {
        if (checkIfTenantCtxExists(tenantId, businessCtxId)) {
            dslContext.deleteFrom(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
                    .and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(ULong.valueOf(businessCtxId))).execute();
        }
    }

    private boolean checkIfTenantCtxExists(BigInteger tenantId, BigInteger businessCtxId) {
        ULong tenantBusinessContextId = dslContext.select(TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID)
                .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
                .and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(ULong.valueOf(businessCtxId)))
                .fetchOne(TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID);
        return tenantBusinessContextId != null ? true : false;
    }

    private boolean checkIfUserTenantExists(BigInteger tenantId, BigInteger addUserId) {
        ULong userTenant = dslContext.select(USER_TENANT.USER_TENANT_ID).from(USER_TENANT)
                .where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
                .and(USER_TENANT.APP_USER_ID.eq(ULong.valueOf(addUserId))).fetchOne(USER_TENANT.USER_TENANT_ID);
        return userTenant != null ? true : false;
    }

    private boolean checkIfTenantNameExists(String name) {
        ULong tenantId = dslContext.select(TENANT.TENANT_ID).from(TENANT).where(TENANT.NAME.eq(name))
                .fetchOne(TENANT.TENANT_ID);
        return tenantId != null ? true : false;
    }

}
