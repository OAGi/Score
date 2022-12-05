package org.oagi.score.gateway.http.api.tenant.data;

import lombok.Data;

@Data
public class BusinessTenantContext {
	
	private Long businessCtxId;
	private String name;
	private boolean checked;
}
