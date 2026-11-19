package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * What the transfer of a release package to the server (an upload) is told by the properties {@code score.release-package-load.*}.
 *
 * @param maxPackageSize  the most bytes a package may have
 * @param uploadDirectory where the packages that are on their way to a load are kept (empty: a directory of the temporary files)
 * @param uploadTtl       how long a package that was not loaded is kept
 */
@Component
public record ReleasePackageLoadTransferSettings(
        @Value("${score.release-package-load.max-package-size:1073741824}") long maxPackageSize,
        @Value("${score.release-package-load.upload-directory:}") String uploadDirectory,
        @Value("${score.release-package-load.upload-ttl:PT6H}") Duration uploadTtl) {
}
