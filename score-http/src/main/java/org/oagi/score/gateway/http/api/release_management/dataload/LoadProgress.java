package org.oagi.score.gateway.http.api.release_management.dataload;

/** Where the loader tells what it is doing. */
@FunctionalInterface
public interface LoadProgress {

    LoadProgress NONE = message -> {
    };

    void report(String message);
}
