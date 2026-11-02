package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Data
public class TenantObject {

    private BigInteger tenantId;

    private String name;

    public static TenantObject createRandomTenant() {
        TenantObject tenant = new TenantObject();
        tenant.setName("tenant_" + randomAlphanumeric(5, 10));
        return tenant;
    }

}
