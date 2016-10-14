package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "app_user")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class User implements Serializable {

    public static final String SEQUENCE_NAME = "APP_USER_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long appUserId;

    @Column(nullable = false, length = 45)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String organization;

    @Column(nullable = false)
    private boolean oagisDeveloperIndicator;

    public long getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(long appUserId) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        if (appUserId != 0L && appUserId == that.appUserId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (appUserId ^ (appUserId >>> 32));
        result = 31 * result + (loginId != null ? loginId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (oagisDeveloperIndicator ? 1 : 0);
        return result;
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
