package org.oagi.score.gateway.http.api.tenant_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.repository.TenantCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.TenantBusinessCtxRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.TenantRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.UserTenantRecord;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqTenantCommandRepository extends JooqBaseRepository implements TenantCommandRepository {

    public JooqTenantCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void deleteTenant(TenantId tenantId) {
        dslContext().deleteFrom(USER_TENANT).where(USER_TENANT.TENANT_ID.eq(valueOf(tenantId))).execute();

        dslContext().deleteFrom(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId)))
                .execute();

        dslContext().deleteFrom(TENANT).where(TENANT.TENANT_ID.eq(valueOf(tenantId))).execute();
    }

    @Override
    public TenantId createTenant(String name) {
        if (checkIfTenantNameExists(name)) {
            return null;
        }

        TenantRecord record = new TenantRecord();
        record.setName(name);
        TenantId tenantId = new TenantId(dslContext().insertInto(TENANT).
                set(record)
                .returning()
                .fetchOne().getTenantId().toBigInteger());

        return tenantId;
    }

    @Override
    public boolean updateTenant(TenantId tenantId, String name) {
        var query = repositoryFactory().tenantQueryRepository(requester());
        TenantInfo tenant = query.getTenantById(tenantId);

        if (tenant != null && !checkIfTenantNameExists(name) && !tenant.name().equals(name)) {
            dslContext().update(TENANT).set(TENANT.NAME, name).where(TENANT.TENANT_ID.eq(valueOf(tenantId)))
                    .execute();

            return true;
        }
        return false;
    }

    @Override
    public void addUserToTenant(TenantId tenantId, UserId appUserId) {
        if (!checkIfUserTenantExists(tenantId, appUserId)) {
            UserTenantRecord record = new UserTenantRecord();
            record.setTenantId(valueOf(tenantId));
            record.setAppUserId(valueOf(appUserId));
            dslContext().insertInto(USER_TENANT).set(record).returning().fetchOne().getUserTenantId().longValue();
        }
    }

    @Override
    public void deleteTenantUser(TenantId tenantId, UserId appUserId) {
        if (checkIfUserTenantExists(tenantId, appUserId)) {
            dslContext().deleteFrom(USER_TENANT).where(USER_TENANT.TENANT_ID.eq(valueOf(tenantId)))
                    .and(USER_TENANT.APP_USER_ID.eq(valueOf(appUserId))).execute();
        }
    }

    @Override
    public void addBusinessCtxToTenant(TenantId tenantId, BusinessContextId businessCtxId) {
        if (!checkIfTenantCtxExists(tenantId, businessCtxId)) {
            TenantBusinessCtxRecord record = new TenantBusinessCtxRecord();
            record.setTenantId(valueOf(tenantId));
            record.setBizCtxId(valueOf(businessCtxId));
            dslContext().insertInto(TENANT_BUSINESS_CTX).set(record).returning().fetchOne().getTenantBusinessCtxId()
                    .longValue();
        }
    }

    @Override
    public void deleteTenantBusinessCtx(TenantId tenantId, BusinessContextId businessCtxId) {
        if (checkIfTenantCtxExists(tenantId, businessCtxId)) {
            dslContext().deleteFrom(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId)))
                    .and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(valueOf(businessCtxId))).execute();
        }
    }

    private boolean checkIfTenantCtxExists(TenantId tenantId, BusinessContextId businessCtxId) {
        ULong tenantBusinessContextId = dslContext().select(TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID)
                .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId)))
                .and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(valueOf(businessCtxId)))
                .fetchOne(TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID);
        return tenantBusinessContextId != null ? true : false;
    }

    private boolean checkIfUserTenantExists(TenantId tenantId, UserId addUserId) {
        ULong userTenant = dslContext().select(USER_TENANT.USER_TENANT_ID).from(USER_TENANT)
                .where(USER_TENANT.TENANT_ID.eq(valueOf(tenantId)))
                .and(USER_TENANT.APP_USER_ID.eq(valueOf(addUserId))).fetchOne(USER_TENANT.USER_TENANT_ID);
        return userTenant != null ? true : false;
    }

    private boolean checkIfTenantNameExists(String name) {
        ULong tenantId = dslContext().select(TENANT.TENANT_ID).from(TENANT).where(TENANT.NAME.eq(name))
                .fetchOne(TENANT.TENANT_ID);
        return tenantId != null ? true : false;
    }

}
