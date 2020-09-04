package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;

@Data
public class OAuth2AppInfo {

    private String loginUrl;
    private String providerName;
    private String displayProviderName;
    private String backgroundColor;
    private String fontColor;

}
