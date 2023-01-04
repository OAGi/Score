package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AppUser {

    private BigInteger appUserId;
    private String loginId;
    private String password;
    private String name;
    private String organization;
    private boolean developer;
    private boolean admin;
    private boolean enabled;
    private BigInteger appOauth2UserId;
    private String sub;

}