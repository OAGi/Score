package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;

@Data
public class AppUser {

    private long appUserId;
    private String loginId;
    private String password;
    private String name;
    private String organization;
    private boolean developer;
    private boolean admin;
    private boolean enabled;
    private long appOauth2UserId;
    private String sub;

}