package org.oagi.score.gateway.http.api.release_management.service.package_load;

import java.util.List;

/**
 * What a look at a package, before it is loaded, finds.
 *
 * @param releaseNum the release the package adds
 * @param version    the version of the format of the package
 * @param format     the format that loads it, or null when none does
 * @param problems   everything that is in the way, which is empty when the package can be loaded
 */
public record ReleasePackageLoadPreflightReport(String releaseNum, String version, String format, List<String> problems) {

    public boolean ok() {
        return problems.isEmpty();
    }
}
