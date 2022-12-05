package org.oagi.score.gateway.http.api.tenant.data;

import lombok.Data;

@Data
public class UserTenantInfo {
	
	private Long userId;
	private String login;
	private boolean checked;

}
