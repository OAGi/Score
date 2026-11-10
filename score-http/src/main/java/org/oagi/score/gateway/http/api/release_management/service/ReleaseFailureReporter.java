package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.async.AsyncJobFailureReporter;
import org.oagi.score.gateway.http.common.async.FailureReasons;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

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

    private final RepositoryFactory repositoryFactory;
    private final AsyncJobFailureReporter reporter;
    private final ReleaseChangeNotifier changeNotifier;

    public ReleaseFailureReporter(RepositoryFactory repositoryFactory, AsyncJobFailureReporter reporter,
                                  ReleaseChangeNotifier changeNotifier) {
        this.repositoryFactory = repositoryFactory;
        this.reporter = reporter;
        this.changeNotifier = changeNotifier;
    }

    /** The work for the release failed; {@code from} is the state it was given back from. */
    public void failed(ReleaseId releaseId, ReleaseState from, UserId requester, Throwable cause) {
        Step step = Step.of(from);
        String number = numberOf(releaseId);
        String technical = "The release " + number + " (" + releaseId.value() + ") could not be " + step.done
                + ": " + FailureReasons.rootMessage(cause);
        report(releaseId, step.tag, step, requester, technical, FailureReasons.withStackTrace(technical, cause), reasonOf(cause), number,
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
        String subject = "Release " + number + " could not be " + step.done;
        String body = summary + "\n\n" + reason + "\n\n[Go to the release list](" + RELEASE_LIST_PAGE + ")";
        changeNotifier.changed(releaseId);
        reporter.report(new AsyncJobFailureReporter.Report(tag, String.valueOf(releaseId.value()), requester, technical,
                details, subject, body,
                new ReleaseChangeNotifier.Failure(ReleaseChangeNotifier.RELEASE_FAILED, libraryOf(releaseId), releaseId)));
    }

    /** The library the screens look at; none when it cannot be found, since that never keeps the failure from being told. */
    private LibraryId libraryOf(ReleaseId releaseId) {
        try {
            return repositoryFactory.releaseProgressRepository().libraryOfRelease(releaseId).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    static final String RECOVERY_TAG = "release.recovery";
    static final String RELEASE_LIST_PAGE = "/release";

    private String numberOf(ReleaseId releaseId) {
        return repositoryFactory.releaseProgressRepository().releaseNumOf(releaseId)
                .orElse(String.valueOf(releaseId.value()));
    }

    /** What the user is told of the cause: a few kinds, since the rest is for an administrator. */
    static String reasonOf(Throwable cause) {
        return FailureReasons.of(cause, ReleaseFailureReporter::ofRelease);
    }

    private static Optional<String> ofRelease(Throwable t) {
        return t instanceof ComponentNotMovedException
                ? Optional.of("A component assigned to the release could not be moved to the next state: it may have been"
                + " changed in the meantime, or it is no longer a candidate.")
                : Optional.empty();
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
