package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.data.UpdatePasswordRequest;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;

import static org.jooq.impl.DSL.row;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;

@Service
@Transactional(readOnly = true)
public class SettingsService {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DSLContext dslContext;

    @Transactional
    public void updatePassword(AuthenticatedPrincipal user, UpdatePasswordRequest request) {
        BigInteger userId = sessionService.userId(user);

        String oldPassword = validate(request.getOldPassword());
        if (!matches(userId, oldPassword)) {
            throw new IllegalArgumentException("Invalid old password");
        }

        String newPassword = validate(request.getNewPassword());
        update(userId, newPassword);
    }

    @Transactional
    public void updatePasswordAccount(UpdatePasswordRequest request) {
        if (!request.getNewPassword().equals("")) {
            String newPassword = validate(request.getNewPassword());
            updateFromLogin(request.getAccount(), newPassword);
        } else {
            updateInformationFromLogin(request.getAccount());
        }
    }

    private String validate(String password) {
        if (!StringUtils.hasLength(password) || password.length() < 5) {
            throw new IllegalArgumentException("Password must be at least 5 characters.");
        }
        return password;
    }

    private boolean matches(BigInteger userId, String oldPassword) {
        return passwordEncoder.matches(oldPassword,
                dslContext.select(APP_USER.PASSWORD).from(APP_USER)
                        .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                        .fetchOneInto(String.class));
    }

    private void update(BigInteger userId, String newPassword) {
        dslContext.update(APP_USER)
                .set(row(APP_USER.PASSWORD), row(passwordEncoder.encode(newPassword)))
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                .execute();
    }

    private void updateFromLogin(AppUser account, String newPassword) {
        dslContext.update(APP_USER)
                .set(APP_USER.PASSWORD, passwordEncoder.encode(newPassword))
                .set(APP_USER.LOGIN_ID, account.getLoginId())
                .set(APP_USER.ORGANIZATION, account.getOrganization())
                .set(APP_USER.NAME, account.getName())
                .set(APP_USER.IS_ADMIN, (byte) (account.isAdmin() ? 1 : 0))
                .where(APP_USER.APP_USER_ID.equal(ULong.valueOf(account.getAppUserId())))
                .execute();
    }

    private void updateInformationFromLogin(AppUser account) {
        dslContext.update(APP_USER)
                .set(APP_USER.LOGIN_ID, account.getLoginId())
                .set(APP_USER.ORGANIZATION, account.getOrganization())
                .set(APP_USER.NAME, account.getName())
                .set(APP_USER.IS_ADMIN, (byte) (account.isAdmin() ? 1 : 0))
                .where(APP_USER.APP_USER_ID.equal(ULong.valueOf(account.getAppUserId())))
                .execute();
    }
}
