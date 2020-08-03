package org.oagi.score.data;

import lombok.Data;

@Data
public class AppUser {

    private long appUserId;
    private String loginId;
    private String password;
    private String name;
    private String organization;
    private boolean developer;

}
