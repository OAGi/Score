package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

import java.util.regex.Pattern;

/**
 * What takes the user a trigger or a routine was made by out of the statement that makes it. A statement that names a user other
 * than the one that runs it needs the privilege to act as that user, which the database user of an application is not given, and the
 * user of the instance a package was written of is not that of the one it is loaded into: what is made then belongs to the user
 * that makes it.
 */
public final class SqlDefiner {

    /** Only the clause of the head of the statement: the same words in the body (in a string) are data. */
    private static final Pattern DEFINER = Pattern.compile(
            "^(\\s*CREATE\\s+(?:OR\\s+REPLACE\\s+)?)DEFINER\\s*=\\s*(?:`[^`]*`@`[^`]*`|CURRENT_USER(?:\\(\\))?)\\s*",
            Pattern.CASE_INSENSITIVE);

    private SqlDefiner() {
    }

    /** The statement without its DEFINER clause; the statement as it is when it has none. */
    public static String strip(String statement) {
        return statement == null ? null : DEFINER.matcher(statement).replaceFirst("$1");
    }
}
