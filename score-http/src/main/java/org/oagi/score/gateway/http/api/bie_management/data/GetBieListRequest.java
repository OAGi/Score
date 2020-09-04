package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.AuthenticatedPrincipal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBieListRequest {

    private AuthenticatedPrincipal user;
    private Long bizCtxId;
    private Boolean excludeJsonRelated;
}
