package org.oagi.score.gateway.http.api.tenant.data;

import java.util.List;

import lombok.Data;

@Data
public class TenantBusinessCtxInfo extends Tenant {
	
	private List<BusinessTenantContext> businessContext;

}
