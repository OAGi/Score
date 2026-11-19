package org.oagi.score.gateway.http.api.release_management.service.package_load;

/** Where the loader tells what it is doing. */
@FunctionalInterface
public interface ReleasePackageLoadProgress {

    ReleasePackageLoadProgress NONE = message -> {
    };

    void report(String message);
}
