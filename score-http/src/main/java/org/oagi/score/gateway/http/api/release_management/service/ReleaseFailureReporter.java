package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.exception_management.model.ExceptionId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tells what went wrong when a release could not be drafted or published, or was taken back because no one was doing it.
 * The detail is kept in the {@code exception} table, to be looked into by an administrator; the user who asked for the
 * release is told in a message that says what is the matter in a few words and refers to the record by its number; and the
 * screens that show the releases are told to load them again.
 *
 * <p>It is called in the transaction that gives the release back, once the release has been given back: the record is
 * kept together with it, or neither is. The message and the announcement are sent once the transaction has committed,
 * and a failure to send them never fails the transaction.</p>
 */
@Component
public class ReleaseFailureReporter {

    private static final int MAX_CAUSES = 20;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RepositoryFactory repositoryFactory;
    private final SessionService sessionService;
    private final MessageCommandService messageCommandService;
    private final ReleaseChangeNotifier changeNotifier;

    public ReleaseFailureReporter(RepositoryFactory repositoryFactory, SessionService sessionService,
                                  MessageCommandService messageCommandService, ReleaseChangeNotifier changeNotifier) {
        this.repositoryFactory = repositoryFactory;
        this.sessionService = sessionService;
        this.messageCommandService = messageCommandService;
        this.changeNotifier = changeNotifier;
    }

    /** The work for the release failed; {@code from} is the state it was given back from. */
    public void failed(ReleaseId releaseId, ReleaseState from, UserId requester, Throwable cause) {
        Step step = Step.of(from);
        String number = numberOf(releaseId);
        String technical = "The release " + number + " (" + releaseId.value() + ") could not be " + step.done
                + ": " + rootMessage(cause);
        report(releaseId, step.tag, step, requester, technical, withStackTrace(technical, cause), reasonOf(cause), number,
                "The release **" + number + "** could not be " + step.done + ". It is **" + step.back
                        + "** again, and nothing was changed.");
    }

    /** No one was doing the work for the release since before {@code since}, so it was taken back. */
    public void abandoned(ReleaseId releaseId, ReleaseState from, UserId requester, LocalDateTime since) {
        Step step = Step.of(from);
        String number = numberOf(releaseId);
        String technical = "The release " + number + " (" + releaseId.value() + ") has been '" + from
                + "' since before " + since + ", and no one was doing it: it was given back as '" + step.back + "'.";
        report(releaseId, RECOVERY_TAG, step, requester, technical, technical,
                "The server may have stopped, or the request may have been lost.", number,
                "The release **" + number + "** was being " + step.doing + " for too long, and no one was working on it."
                        + " It is **" + step.back + "** again, and nothing was changed.");
    }

    private void report(ReleaseId releaseId, String tag, Step step, UserId requester, String technical, String details,
                        String reason, String number, String summary) {
        ExceptionId exceptionId = repositoryFactory.exceptionCommandRepository(sessionService.getScoreSystemUser())
                .create(tag + "#" + releaseId.value(), technical, details, requester);
        logger.warn(technical + " (exception " + exceptionId + ")");

        String subject = "Release " + number + " could not be " + step.done;
        String body = summary + "\n\n" + reason + "\n\n[Go to the release list](" + RELEASE_LIST_PAGE + ")";
        changeNotifier.failed(releaseId, sessionService.getScoreUserByUserId(requester).username());
        afterCommit(() -> {
            try {
                ScoreUser system = sessionService.getScoreSystemUser();
                messageCommandService.asyncSendMessage(system, List.of(requester), subject, body, "text/markdown");
            } catch (RuntimeException e) {
                logger.warn("The failure of the release " + releaseId + " could not be told to the user " + requester
                        + ": " + e.getMessage());
            }
        });
    }

    static final String RECOVERY_TAG = "release.recovery";
    static final String RELEASE_LIST_PAGE = "/release";

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private String numberOf(ReleaseId releaseId) {
        return repositoryFactory.releaseProgressRepository().releaseNumOf(releaseId)
                .orElse(String.valueOf(releaseId.value()));
    }

    /** What the user is told of the cause: a few kinds, since the rest is for an administrator. */
    static String reasonOf(Throwable cause) {
        int depth = 0;
        for (Throwable t = cause; t != null && depth < MAX_CAUSES; t = t.getCause(), depth++) {
            if (t instanceof ComponentNotMovedException) {
                return "A component assigned to the release could not be moved to the next state: it may have been"
                        + " changed in the meantime, or it is no longer a candidate.";
            }
            if (t instanceof ConcurrencyFailureException || isLockConflict(t)) {
                return "Another change was being made at the same time. Please try again.";
            }
        }
        return "An unexpected error occurred. Please try again, and tell an administrator if it happens again.";
    }

    private static boolean isLockConflict(Throwable t) {
        return t instanceof SQLException e
                && ("40001".equals(e.getSQLState()) || e.getErrorCode() == 1205 || e.getErrorCode() == 1213);
    }

    private static String rootMessage(Throwable cause) {
        Throwable root = cause;
        int depth = 0;
        while (root != null && root.getCause() != null && root.getCause() != root && depth++ < MAX_CAUSES) {
            root = root.getCause();
        }
        return root == null ? "unknown" : root.getClass().getName() + ": " + root.getMessage();
    }

    /** All there is to know, for the record to keep: what went wrong, whole, and the stack trace that goes with it. */
    private static String withStackTrace(String technical, Throwable cause) {
        if (cause == null) {
            return technical;
        }
        StringWriter writer = new StringWriter();
        writer.write(technical);
        writer.write("\n\n");
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private enum Step {
        DRAFTING("release.drafting", "made a draft", "drafted", "Initialized"),
        PUBLISHING("release.publishing", "published", "published", "Draft"),
        INITIALIZING("release.initializing", "taken back to Initialized", "taken back to Initialized", "Draft");

        final String tag;
        final String done;
        final String doing;
        final String back;

        Step(String tag, String done, String doing, String back) {
            this.tag = tag;
            this.done = done;
            this.doing = doing;
            this.back = back;
        }

        static Step of(ReleaseState from) {
            return switch (from) {
                case Publishing -> PUBLISHING;
                case Initializing -> INITIALIZING;
                default -> DRAFTING;
            };
        }
    }
}
