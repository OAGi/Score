package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import java.util.Locale;
import java.util.Set;

/**
 * The name of a copied BIE package. The name, version ID and version name of a package are unique together, so
 * the copy takes the first of "Copy of X", "Copy of X (2)", "Copy of X (3)", ... that is free, and the source's
 * name is shortened as much as needed for the whole name to fit the {@code name} column.
 */
final class BiePackageCopyName {

    static final int MAX_LENGTH = 200;
    private static final String PREFIX = "Copy of ";

    private BiePackageCopyName() {
    }

    /**
     * @param sourceName the name of the package that is copied.
     * @param takenNames the names that are already used with the same version ID and version name. Compared
     *                   without regard to case, as the column is.
     */
    static String pick(String sourceName, Set<String> takenNames) {
        Set<String> taken = new java.util.HashSet<>();
        for (String name : takenNames) {
            taken.add(name.toLowerCase(Locale.ROOT));
        }
        for (int n = 1; ; n++) {
            String suffix = (n == 1) ? "" : " (" + n + ")";
            String candidate = PREFIX + shorten(sourceName, MAX_LENGTH - PREFIX.length() - suffix.length()) + suffix;
            if (!taken.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
    }

    private static String shorten(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        int end = maxLength;
        if (Character.isHighSurrogate(name.charAt(end - 1))) {
            end--; // do not cut a character in two
        }
        return name.substring(0, end).stripTrailing();
    }
}
