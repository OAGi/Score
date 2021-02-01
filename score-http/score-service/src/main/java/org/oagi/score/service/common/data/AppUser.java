package org.oagi.score.service.common.data;

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
    private boolean enabled;

}
