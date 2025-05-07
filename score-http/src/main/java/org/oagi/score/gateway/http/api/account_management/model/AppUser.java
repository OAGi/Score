package org.oagi.score.gateway.http.api.account_management.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AppUser {

    private BigInteger appUserId;
    private String loginId;
    private String password;
    private String name;
    private String organization;
    private String email;
    private boolean emailVerified;
    private boolean developer;
    private boolean admin;
    private boolean enabled;
    private boolean hasData;
    private BigInteger appOauth2UserId;
    private String providerName;
    private String sub;
    private String oidcName;
    private String oidcEmail;
    private String nickname;
    private String preferredUsername;
    private String phoneNumber;

}