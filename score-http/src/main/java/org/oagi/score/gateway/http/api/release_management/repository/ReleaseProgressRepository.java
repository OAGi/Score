package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * What it takes to keep a library from being changed while one of its releases is becoming a draft or being published:
 * the lock of the library, the state of the releases, and the library a component is of. It does not need a user, since
 * it is asked by the guard in front of the services, which is not for a user to know.
 */
public interface ReleaseProgressRepository {

    /** A release that is in the middle of a transition, and the user who last updated it: the one who asked for it. */
    record InProgress(ReleaseId releaseId, ReleaseState state, UserId lastUpdatedBy) {
    }

    /** The library the component is of, found from its manifest: empty for the manifest of what is not of a release. */
    Optional<LibraryId> libraryOfManifest(ManifestId manifestId);

    Optional<LibraryId> libraryOfRelease(ReleaseId releaseId);

    /**
     * Locks the library, and the releases of it, until the end of the transaction, shared with the others that ask for it
     * this way, and tells whether a release of the library is in the middle of a transition. What it reads is what has been
     * committed, whatever the transaction has read before: a locking read is not read from a snapshot. It does not wait for a
     * lock another transaction holds: the release that is taken hold of, and the one that is being done, hold theirs until
     * they end, and the library is busy for as long.
     */
    boolean libraryIsBusy(LibraryId libraryId);

    /** The same for a call that is not given anything a library can be found of: any library that is busy. */
    boolean anyLibraryIsBusy();

    /**
     * Locks the library of the release and the release, for the one that is about to take hold of it, until the end of the
     * transaction. It waits for the ones that are changing the library. It is to be the first thing the transaction reads,
     * so that what it reads afterwards is what has been committed up to when the lock is had.
     */
    void lockLibraryOfRelease(ReleaseId releaseId);

    /** Changes the state of the release only if it is {@code from}; whether it was. */
    boolean moveState(ReleaseId releaseId, ReleaseState from, ReleaseState to);

    /**
     * Changes the state of the release only if it is {@code from}, as the user who asks for it: the release is then
     * the one that user last updated, which tells who to report to when the work for it fails.
     */
    boolean takeHold(ReleaseId releaseId, ReleaseState from, ReleaseState to, UserId requester);

    /** The release number, for what is told about a release. */
    Optional<String> releaseNumOf(ReleaseId releaseId);

    /** The state of the release, locked until the end of the transaction. */
    Optional<ReleaseState> lockState(ReleaseId releaseId);

    /**
     * The releases that have been being drafted or published since before the time, and that no transaction holds: a
     * transition that is being done holds its release until it ends. They are locked until the end of the transaction.
     */
    List<InProgress> lockInProgressSince(LocalDateTime time);

    /**
     * The release, locked until the end of the transaction, if it is in the middle of a transition and no transaction holds
     * it: a transition that is being done holds its release until it ends. Empty when it is not in the middle of one, and when
     * something is holding it; it does not wait.
     */
    Optional<InProgress> lockInProgress(ReleaseId releaseId);

    /** The state of the release, without locking it. */
    Optional<ReleaseState> stateOf(ReleaseId releaseId);

    /** Whether the release holds any manifest, i.e. it has been copied. */
    boolean hasManifests(ReleaseId releaseId);
}
