package org.oagi.score.gateway.http.api.release_management.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Where a load of a release package is. A load goes through the states in order (the schema is made and loaded, brought to the
 * shape of this version, the records of the users are copied into it, it is compared with the served one, and it is swapped
 * for it) and ends as {@link #SUCCEEDED} or {@link #FAILED}; a swap that has been undone is {@link #ROLLED_BACK}.
 */
public enum MigrationLoadState {

    PENDING, PREFLIGHT, LOADING, MIGRATING, COPYING, VERIFYING, SWAPPING, ROLLING_BACK,
    SUCCEEDED, FAILED, ROLLED_BACK;

    private static final Set<MigrationLoadState> ENDED = EnumSet.of(SUCCEEDED, FAILED, ROLLED_BACK);

    /** The states of a load that goes on. */
    public static final Set<MigrationLoadState> ACTIVE = EnumSet.complementOf(EnumSet.copyOf(ENDED));

    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    public static MigrationLoadState of(String name) {
        return Arrays.stream(values()).filter(state -> state.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown state of a load: " + name));
    }
}
