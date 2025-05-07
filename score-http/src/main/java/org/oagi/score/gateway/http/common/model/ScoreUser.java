package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public record ScoreUser(UserId userId, String username,
                        String name, String emailAddress, boolean emailVerified,
                        Collection<ScoreRole> roles) {

    public static final String SYSTEM_USER_LOGIN_ID = "sysadm";
    public static final BigInteger SYSTEM_USER_ID = BigInteger.ZERO;

    public Collection<ScoreRole> roles() {
        return (roles == null) ? Collections.emptyList() : roles;
    }

    public boolean hasRole(ScoreRole role) {
        if (role == null) {
            throw new IllegalArgumentException();
        }

        return hasAnyRole(Arrays.asList(role));
    }

    public boolean hasAnyRole(ScoreRole... roles) {
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException();
        }

        return hasAnyRole(Arrays.asList(roles));
    }

    public boolean hasAnyRole(Collection<ScoreRole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }

        for (ScoreRole role : roles) {
            return this.roles.contains(role);
        }

        return false;
    }

    public boolean isDeveloper() {
        return hasRole(ScoreRole.DEVELOPER);
    }

    public boolean isAdministrator() {
        return hasRole(ScoreRole.ADMINISTRATOR);
    }
}
