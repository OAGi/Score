package org.oagi.srt.gateway.http.api.bie_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBieListRequest {

    private User user;
    private Long bizCtxId;
    private Boolean excludeJsonRelated;
}
