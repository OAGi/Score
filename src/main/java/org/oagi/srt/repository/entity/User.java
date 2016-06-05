package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "app_user")
public class User implements Serializable {

    public static final String SEQUENCE_NAME = "APP_USER_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private int appUserId;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column
    private String name;

    @Column
    private String organization;

    @Column(nullable = false)
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
