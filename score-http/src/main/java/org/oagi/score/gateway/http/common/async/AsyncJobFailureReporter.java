package org.oagi.score.gateway.http.common.async;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.exception_management.model.ExceptionId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tells what went wrong when background work failed or was abandoned, in the three places it is needed: the detail in the
 * {@code exception} table, for an administrator to look into; a message to the user who asked for the work, which says in a
 * few words what is the matter and where to go; and, when the work has an event for it, a STOMP message to that user alone.
 *
 * <p>It is called in the transaction that undoes the work: the record is kept together with it, or neither is. The message
 * and the STOMP message are sent once that transaction has committed, and a failure to send them never fails it.</p>
 */
@Component
public class AsyncJobFailureReporter {

    /**
     * What is reported.
     *
     * @param tag       what kind of work, as the record is tagged (the record is found by {@code tag#key})
     * @param key       which one of that kind
     * @param requester the user who asked for the work: told, and named in the record
     * @param technical a line for the log and for the record
     * @param details   all there is to know, for the record
     * @param subject   the subject of the message to the user
     * @param body      the body of the message to the user, in markdown
     * @param userEvent sent over STOMP to the requester alone; {@code null} when the work has none
     */
    public record Report(String tag, String key, UserId requester, String technical, String details,
                         String subject, String body, Object userEvent) {
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RepositoryFactory repositoryFactory;
    private final SessionService sessionService;
    private final MessageCommandService messageCommandService;
    private final StompNotifier stompNotifier;

    public AsyncJobFailureReporter(RepositoryFactory repositoryFactory, SessionService sessionService,
                                   MessageCommandService messageCommandService, StompNotifier stompNotifier) {
        this.repositoryFactory = repositoryFactory;
        this.sessionService = sessionService;
        this.messageCommandService = messageCommandService;
        this.stompNotifier = stompNotifier;
    }

    public void report(Report report) {
        ExceptionId exceptionId = repositoryFactory.exceptionCommandRepository(sessionService.getScoreSystemUser())
                .create(report.tag() + "#" + report.key(), report.technical(), report.details(), report.requester());
        logger.warn(report.technical() + " (exception " + exceptionId + ")");

        if (report.userEvent() != null) {
            stompNotifier.toUser(sessionService.getScoreUserByUserId(report.requester()).username(), report.userEvent());
        }
        AfterCommit.run(() -> {
            try {
                ScoreUser system = sessionService.getScoreSystemUser();
                messageCommandService.asyncSendMessage(system, List.of(report.requester()),
                        report.subject(), report.body(), "text/markdown");
            } catch (RuntimeException e) {
                logger.warn("The failure of " + report.tag() + "#" + report.key() + " could not be told to the user "
                        + report.requester() + ": " + e.getMessage());
            }
        });
    }
}
