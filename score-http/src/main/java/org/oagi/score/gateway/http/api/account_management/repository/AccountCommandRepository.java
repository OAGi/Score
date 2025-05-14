package org.oagi.score.gateway.http.api.account_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserId;

public interface AccountCommandRepository {

    UserId create(String loginId, String password, String name, String organization, boolean developer, boolean admin);

    boolean update(UserId userId, String username, String organization, boolean admin, String newPassword);

    boolean updatePassword(String password);

    boolean linkOAuth2User(OAuth2UserId oAuth2UserId, UserId userId);

    boolean delinkOAuth2User(UserId userId);

    boolean updateEmail(String email);

    void setEmailVerified(UserId userId, boolean emailVerified);

    boolean delete(UserId userId);

    boolean setEnabled(UserId userId, boolean enabled);

    void updateOwnerUser(UserId userId);


}
