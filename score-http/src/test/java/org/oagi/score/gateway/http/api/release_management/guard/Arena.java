package org.oagi.score.gateway.http.api.release_management.guard;

import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationRequest;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCreateRequestEvent;
import org.oagi.score.gateway.http.api.release_management.guard.GuardHarness.K;

import java.util.Map;

/** A new release of the library, with a component of every kind that is assigned to it: what a test drives. */
public final class Arena {

    private final GuardHarness harness;
    private final ReleaseId release;
    private final long working;
    private final Map<K, Long> components;
    private final ReleaseValidationRequest request;

    Arena(GuardHarness harness, ReleaseId release, long working, Map<K, Long> components, ReleaseValidationRequest request) {
        this.harness = harness;
        this.release = release;
        this.working = working;
        this.components = components;
        this.request = request;
    }

    public ReleaseId release() {
        return release;
    }

    public long working() {
        return working;
    }

    public Map<K, Long> components() {
        return components;
    }

    public ReleaseValidationRequest request() {
        return request;
    }

    public GuardHarness.Observed observe() {
        return harness.observe(this);
    }

    /** What the handlers do with the events the services published, in the thread that calls this. */
    public void handleEvents() {
        for (Object event : harness.drainEvents()) {
            if (event instanceof ReleaseCreateRequestEvent e) {
                harness.releaseService.onReleaseCreateRequestEventReceived(e);
            } else if (event instanceof ReleaseCleanupEvent e) {
                harness.releaseService.onReleaseCleanupEventReceived(e);
            }
        }
    }

    /** The request to make the release a draft, without the handling of its event; whatever fails is returned. */
    public Throwable requestDraft() {
        try {
            ReleaseValidationResponse response = harness.releaseService.createDraft(GuardHarness.REQUESTER, request);
            if (!response.isSucceed()) {
                return new IllegalStateException("the validation did not succeed: " + response);
            }
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    /** The request to make the release a draft, and the handling of its event; whatever fails is returned. */
    public Throwable draft() {
        Throwable failure = requestDraft();
        if (failure != null) {
            return failure;
        }
        try {
            handleEvents();
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    /** The request to take the draft back to 'Initialized'. */
    public Throwable initialize() {
        try {
            harness.releaseService.transitState(GuardHarness.REQUESTER, harness.transit(this, "Initialized"));
            handleEvents();
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    /** The request to publish the draft, and the handling of its event. */
    public Throwable publish() {
        try {
            harness.releaseService.transitState(GuardHarness.REQUESTER, harness.transit(this, "Published"));
        } catch (Throwable t) {
            return t;
        }
        try {
            handleEvents();
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    /** Makes the release a draft, without anything going wrong. */
    public Arena toDraft() {
        Throwable failure = draft();
        if (failure != null) {
            throw new IllegalStateException("the release could not be made a draft", failure);
        }
        GuardHarness.Observed observed = observe();
        if (!"Draft".equals(observed.releaseState())) {
            throw new IllegalStateException("the release is not a draft: " + observed);
        }
        return this;
    }

    /** Removes the release and what it holds. */
    public void remove() {
        harness.remove(release.value().longValue(), components);
    }
}
