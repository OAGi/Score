package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AppUserObject {

    private BigInteger appUserId;

    private String loginId;

    private String password;

    private String name;

    private String organization;

    private boolean developer;

    private boolean admin;

    private boolean enabled;

}
