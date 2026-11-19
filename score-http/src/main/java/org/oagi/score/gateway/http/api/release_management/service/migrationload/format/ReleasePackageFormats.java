package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

import org.oagi.score.gateway.http.api.release_management.service.migrationload.MigrationLoadRefusedException;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The formats of release packages that the application loads, and the one that loads a package: of the formats that handle the version
 * the information file of the package says, the one that starts at the latest version (the most particular), so that a format for newer
 * versions is added without a format for older ones being changed.
 */
@Component
public class ReleasePackageFormats {

    private final List<ReleasePackageFormat> formats;

    /** @throws IllegalStateException when two formats start at the same version, which would leave it to chance which one loads a package */
    public ReleasePackageFormats(List<ReleasePackageFormat> formats) {
        Set<PackageVersion> starts = new HashSet<>();
        for (ReleasePackageFormat format : formats) {
            if (format.from().compareTo(format.before()) >= 0) {
                throw new IllegalStateException("The format '" + format.name() + "' handles no version: from " + format.from() + " before " + format.before() + ".");
            }
            if (!starts.add(format.from())) {
                throw new IllegalStateException("Two formats of release packages start at the version " + format.from() + ".");
            }
        }
        this.formats = formats.stream().sorted(Comparator.comparing(ReleasePackageFormat::from)).toList();
    }

    /** @throws MigrationLoadRefusedException when no format handles the version of the package */
    public ReleasePackageFormat resolve(Info info) {
        PackageVersion version;
        try {
            version = PackageVersion.parse(info.getVersion());
        } catch (IllegalArgumentException e) {
            throw new MigrationLoadRefusedException("The package says a version of its format that is not one: " + info.getVersion(), e);
        }
        return formats.stream().filter(format -> format.handles(version)).max(Comparator.comparing(ReleasePackageFormat::from))
                .orElseThrow(() -> new MigrationLoadRefusedException("The package is of the version " + version + " of the format, which this "
                        + "application does not load; it loads " + supported() + "."));
    }

    private String supported() {
        return formats.stream().map(format -> format.from() + " up to " + format.before() + " (" + format.name() + ")")
                .collect(Collectors.joining(", "));
    }
}
