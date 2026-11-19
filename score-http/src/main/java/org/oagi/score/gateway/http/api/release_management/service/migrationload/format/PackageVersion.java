package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The version of the format of a release package, which the information file of the package says ({@code 1.1}, {@code 1.2.3}): the first two numbers count. */
public record PackageVersion(int major, int minor) implements Comparable<PackageVersion> {

    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.\\d+)?");

    public static PackageVersion of(int major, int minor) {
        return new PackageVersion(major, minor);
    }

    /** @throws IllegalArgumentException when the text is not a version of the format (numbers separated by dots, two or three of them) */
    public static PackageVersion parse(String version) {
        Matcher matcher = version == null ? null : VERSION.matcher(version);
        if (matcher == null || !matcher.matches()) {
            throw new IllegalArgumentException("Not a version of the format of a release package: " + version);
        }
        return new PackageVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    @Override
    public int compareTo(PackageVersion other) {
        return major != other.major ? Integer.compare(major, other.major) : Integer.compare(minor, other.minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
