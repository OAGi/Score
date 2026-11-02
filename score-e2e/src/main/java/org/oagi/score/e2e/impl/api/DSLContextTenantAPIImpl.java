package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.TenantAPI;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextTenantAPIImpl implements TenantAPI {

    private final DSLContext dslContext;

    public DSLContextTenantAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public TenantObject createRandomTenant() {
        TenantObject tenant = TenantObject.createRandomTenant();
        ULong tenantId = dslContext.insertInto(TENANT)
                .set(TENANT.NAME, tenant.getName())
                .returning(TENANT.TENANT_ID)
                .fetchOne().getTenantId();
        tenant.setTenantId(tenantId.toBigInteger());
        return tenant;
    }

    @Override
    public void addUserToTenant(AppUserObject appUser, TenantObject tenant) {
        dslContext.insertInto(USER_TENANT)
                .set(USER_TENANT.APP_USER_ID, ULong.valueOf(appUser.getAppUserId()))
                .set(USER_TENANT.TENANT_ID, ULong.valueOf(tenant.getTenantId()))
                .execute();
    }

    @Override
    public void addBusinessContextToTenant(BusinessContextObject businessContext, TenantObject tenant) {
        dslContext.insertInto(TENANT_BUSINESS_CTX)
                .set(TENANT_BUSINESS_CTX.BIZ_CTX_ID, ULong.valueOf(businessContext.getBusinessContextId()))
                .set(TENANT_BUSINESS_CTX.TENANT_ID, ULong.valueOf(tenant.getTenantId()))
                .execute();
    }

    @Override
    public void deleteTenant(TenantObject tenant) {
        ULong tenantId = ULong.valueOf(tenant.getTenantId());
        dslContext.deleteFrom(USER_TENANT).where(USER_TENANT.TENANT_ID.eq(tenantId)).execute();
        dslContext.deleteFrom(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(tenantId)).execute();
        dslContext.deleteFrom(TENANT).where(TENANT.TENANT_ID.eq(tenantId)).execute();
    }

    @Override
    public void deleteTenantByName(String name) {
        for (ULong tenantId : dslContext.select(TENANT.TENANT_ID).from(TENANT)
                .where(TENANT.NAME.eq(name)).fetch(TENANT.TENANT_ID)) {
            TenantObject tenant = new TenantObject();
            tenant.setTenantId(tenantId.toBigInteger());
            deleteTenant(tenant);
        }
    }

}
