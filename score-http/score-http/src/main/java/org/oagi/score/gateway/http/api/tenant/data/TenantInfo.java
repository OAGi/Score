package org.oagi.score.gateway.http.api.tenant.data;

import lombok.Data;

@Data
public class TenantInfo extends Tenant{

	private Integer usersCount;
	private Integer businessCtxCount;
}
