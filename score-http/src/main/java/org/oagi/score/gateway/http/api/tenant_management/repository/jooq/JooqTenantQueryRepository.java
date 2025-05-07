package org.oagi.score.gateway.http.api.tenant_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.controller.payload.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant_management.model.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.repository.TenantQueryRepository;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqTenantQueryRepository extends JooqBaseRepository implements TenantQueryRepository {

    public JooqTenantQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<TenantId> getUserTenantsRoleByUserId(UserId userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return dslContext().select(TENANT.TENANT_ID).from(TENANT).join(USER_TENANT)
                .on(TENANT.TENANT_ID.eq(USER_TENANT.TENANT_ID)).where(USER_TENANT.APP_USER_ID.eq(valueOf(userId)))
                .fetch(record -> new TenantId(record.get(TENANT.TENANT_ID).toBigInteger()));
    }

    @Override
    public TenantInfo getTenantById(TenantId tenantId) {
        if (tenantId == null) {
            return null;
        }

        Tenant tenant = dslContext().select(TENANT.TENANT_ID, TENANT.NAME).from(TENANT)
                .where(TENANT.TENANT_ID.eq(valueOf(tenantId))).fetchOne(mapper());

        int usersCount = dslContext().selectCount().from(USER_TENANT)
                .where(USER_TENANT.TENANT_ID.eq(valueOf(tenantId))).fetchOne().value1();

        int businessCtxCount = dslContext().selectCount().from(TENANT_BUSINESS_CTX)
                .where(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId))).fetchOne().value1();

        return new TenantInfo(tenant.tenantId(), tenant.name(), usersCount, businessCtxCount);
    }

    @Override
    public List<String> getTenantNameByBusinessCtxId(BusinessContextId businessCtxId) {
        if (businessCtxId == null) {
            return Collections.emptyList();
        }

        return dslContext().select(TENANT.NAME)
                .from(TENANT)
                .join(TENANT_BUSINESS_CTX).on(TENANT.TENANT_ID.eq(TENANT_BUSINESS_CTX.TENANT_ID))
                .where(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(valueOf(businessCtxId)))
                .fetch(TENANT.NAME);
    }

    @Override
    public PageResponse<Tenant> getAllTenantsRole(TenantListRequest tenantRequest) {

        PageResponse<Tenant> response = new PageResponse<>();
        SelectJoinStep<Record2<ULong, String>> step;
        SelectConnectByStep<Record2<ULong, String>> conditionStep;
        SelectWithTiesAfterOffsetStep<Record2<ULong, String>> offsetStep;
        int pageCount;

        step = dslContext().select(TENANT.TENANT_ID, TENANT.NAME).from(TENANT);

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(tenantRequest.getName())) {
            conditions.addAll(contains(tenantRequest.getName(), TENANT.NAME));
        }
        conditionStep = step.where(conditions);
        pageCount = dslContext().fetchCount(conditionStep);
        SortField sortField = TENANT.NAME.asc();

        if (!tenantRequest.getPageRequest().sorts().isEmpty()) {
            Sort sort = tenantRequest.getPageRequest().sorts().iterator().next();
            if (DESC == sort.direction()) {
                sortField = TENANT.NAME.desc();
            } else {
                sortField = TENANT.NAME.asc();
            }
        }

        offsetStep = conditionStep.orderBy(sortField).limit(tenantRequest.getPageRequest().pageOffset(),
                tenantRequest.getPageRequest().pageSize());

        response.setList(
                (offsetStep != null) ? offsetStep.fetch(mapper()) : conditionStep.fetch(mapper()));
        response.setPage(tenantRequest.getPageRequest().pageIndex());
        response.setSize(tenantRequest.getPageRequest().pageSize());
        response.setLength(pageCount);
        return response;
    }

    private RecordMapper<Record, Tenant> mapper() {
        return record -> new Tenant(
                new TenantId(record.get(TENANT.TENANT_ID).toBigInteger()),
                record.get(TENANT.NAME));
    }

}
