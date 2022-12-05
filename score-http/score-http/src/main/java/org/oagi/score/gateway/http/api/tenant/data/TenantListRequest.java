package org.oagi.score.gateway.http.api.tenant.data;

import org.oagi.score.service.common.data.PageRequest;

import lombok.Data;

@Data
public class TenantListRequest {
	
	private String name;
	private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
