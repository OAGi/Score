package org.oagi.score.gateway.http.api.account_management.model;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class AppOauth2User {

    private long appOauth2UserId;
    private BigInteger appUserId;
    private String providerName;
    private String sub;
    private String name;
    private String email;
    private String nickname;
    private String preferredUsername;
    private String phoneNumber;
    private Date creationTimestamp;

}