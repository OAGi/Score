package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class User implements Serializable {

    private int appUserId;
    private String loginId;
    private String password;
    private String name;
    private String organization;
    private boolean oagisDeveloperIndicator;

    public int getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(int appUserId) {
        this.appUserId = appUserId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isOagisDeveloperIndicator() {
        return oagisDeveloperIndicator;
    }

    public void setOagisDeveloperIndicator(boolean oagisDeveloperIndicator) {
        this.oagisDeveloperIndicator = oagisDeveloperIndicator;
    }

    @Override
    public String toString() {
        return "User{" +
                "appUserId=" + appUserId +
                ", loginId='" + loginId + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", organization='" + organization + '\'' +
                ", oagisDeveloperIndicator=" + oagisDeveloperIndicator +
                '}';
    }
}
