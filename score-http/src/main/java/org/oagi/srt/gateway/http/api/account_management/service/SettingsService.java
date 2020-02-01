package org.oagi.srt.gateway.http.api.account_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.gateway.http.api.account_management.data.AppUser;
import org.oagi.srt.gateway.http.api.account_management.data.UpdatePasswordRequest;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static org.jooq.impl.DSL.row;
import static org.oagi.srt.entity.jooq.Tables.APP_USER;

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
    public void updatePassword(User user, UpdatePasswordRequest request) {
        long userId = sessionService.userId(user);

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
        if (StringUtils.isEmpty(password) || password.length() < 5) {
            throw new IllegalArgumentException("Password must be at least 5 characters.");
        }
        return password;
    }

    private boolean matches(long userId, String oldPassword) {
        return passwordEncoder.matches(oldPassword,
                dslContext.select(APP_USER.PASSWORD).from(APP_USER)
                        .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                        .fetchOneInto(String.class));
    }

    private void update(long userId, String newPassword) {
        dslContext.update(APP_USER)
                .set(row(APP_USER.PASSWORD), row(passwordEncoder.encode(newPassword)))
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                .execute();
    }

    private void updateFromLogin(AppUser account, String newPassword) {
        dslContext.update(APP_USER)
                .set(row(APP_USER.PASSWORD, APP_USER.ORGANIZATION, APP_USER.NAME),
                        row(passwordEncoder.encode(newPassword), account.getOrganization(), account.getName()))
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(account.getLoginId()))
                .execute();
    }

    private void updateInformationFromLogin(AppUser account) {
        dslContext.update(APP_USER)
                .set(row(APP_USER.ORGANIZATION, APP_USER.NAME),
                        row(account.getOrganization(), account.getName()))
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(account.getLoginId()))
                .execute();
    }
}
