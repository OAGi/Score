package org.oagi.score.gateway.http.api.passkey_management.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tells a user, in the application's own messages, when the passkeys of their account change, and how many they have
 * now. A passkey added by somebody who got hold of a session would otherwise go unnoticed until the user happened to
 * open the Passkeys page. Email is not used because not every deployment can send it.
 * <p>
 * A message that cannot be sent never fails the change it reports.
 */
@Component
public class PasskeyNotifier {

    static final String PASSKEYS_PAGE = "/settings/account/passkeys";

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MessageCommandService messageCommandService;

    public void passkeyAdded(UserId userId) {
        send(userId, "A passkey was added to your account", (count) ->
                "A passkey was added to your account. " + have(count) + "\n\n"
                        + "If you did not add it, [remove it](" + PASSKEYS_PAGE + "), change your password and tell an administrator.");
    }

    public void passkeyRemoved(UserId userId) {
        send(userId, "A passkey was removed from your account", (count) ->
                "A passkey was removed from your account. " + have(count) + "\n\n"
                        + "[Review your passkeys](" + PASSKEYS_PAGE + ").");
    }

    public void passkeysRemovedByAdministrator(UserId userId, int removed) {
        if (removed <= 0) {
            return;
        }
        send(userId, "An administrator removed passkeys from your account", (count) ->
                "An administrator removed " + removed + (removed == 1 ? " passkey" : " passkeys")
                        + " from your account. " + have(count) + "\n\n"
                        + "[Review your passkeys](" + PASSKEYS_PAGE + ").");
    }

    private void send(UserId userId, String subject, java.util.function.IntFunction<String> body) {
        try {
            ScoreUser system = sessionService.getScoreSystemUser();
            int count = repositoryFactory.passkeyQueryRepository(system).countCredentialsByUserId(userId);
            messageCommandService.asyncSendMessage(system, List.of(userId), subject, body.apply(count), "text/markdown");
        } catch (RuntimeException e) {
            logger.warn("Unable to notify user " + userId + " about the change of their passkeys", e);
        }
    }

    static String have(int count) {
        return (count == 0) ? "You have no passkeys now."
                : "You have " + count + (count == 1 ? " passkey" : " passkeys") + " now.";
    }

}
