package org.oagi.score.gateway.http.api.release_management.service.package_load;

import java.util.List;

/**
 * What a look at a package, before it is loaded, finds.
 *
 * @param releaseNum the release the package adds
 * @param version    the version of the format of the package
 * @param format     the format that loads it, or null when none does
 * @param problems   everything that is in the way, which is empty when the package can be loaded
 * @param libraries  the latest release of each library, in the package and in this database
 */
public record ReleasePackageLoadPreflightReport(String releaseNum, String version, String format, List<String> problems,
                                                List<Library> libraries) {

    /**
     * @param name           the library
     * @param packageRelease its latest release in the package, or null when the package has none
     * @param currentRelease its latest release in this database, or null when it has none
     * @param behind         whether the package is behind this database in the library, which refuses it
     */
    public record Library(String name, String packageRelease, String currentRelease, boolean behind) {
    }

    public ReleasePackageLoadPreflightReport(String releaseNum, String version, String format, List<String> problems) {
        this(releaseNum, version, format, problems, List.of());
    }

    public boolean ok() {
        return problems.isEmpty();
    }
}
