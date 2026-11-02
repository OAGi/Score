package org.oagi.score.gateway.http.api.account_management.model;

import lombok.Data;

import java.util.Date;

@Data
public class AppOauth2User {

    private OAuth2UserId appOauth2UserId;
    private UserId appUserId;
    private String providerName;
    private String sub;
    private String name;
    private String email;
    private String nickname;
    private String preferredUsername;
    private String phoneNumber;
    private Date creationTimestamp;

}