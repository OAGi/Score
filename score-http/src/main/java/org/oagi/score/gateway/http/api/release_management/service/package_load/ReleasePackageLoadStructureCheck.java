package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorts the differences between the schema that is served and the one that is loaded into what stops the swap and what does not.
 *
 * <p>The swap puts the loaded tables in place of the served ones, and the records of the users are copied into them by the columns the
 * two have in common. So what is lost is only what exists in the served schema alone and holds something: a column that has values, a table
 * that has records. A trigger or a routine that is not the same is kept in the schema, not moved with the tables, so it stops the swap.
 * Whatever else differs (an index, a foreign key, a check, a column or table that is empty, anything that only the loaded schema has) does not
 * lose anything, and is put right by the swap; it is told in the log.</p>
 */
final class ReleasePackageLoadStructureCheck {

    /** What is found: the differences that stop the swap, and those that do not. */
    record Result(List<String> blocking, List<String> tolerated) {
    }

    private ReleasePackageLoadStructureCheck() {
    }

    static Result of(List<String> differences, String served, ReleasePackageLoadQueryRepository query) {
        String servedOnly = "only in " + served + ": ";
        List<String> blocking = new ArrayList<>();
        List<String> tolerated = new ArrayList<>();
        for (String difference : differences) {
            if (stops(difference, servedOnly, served, query)) {
                blocking.add(difference);
            } else {
                tolerated.add(difference);
            }
        }
        return new Result(blocking, tolerated);
    }

    private static boolean stops(String difference, String servedOnly, String served, ReleasePackageLoadQueryRepository query) {
        String rest = difference.startsWith(servedOnly) ? difference.substring(servedOnly.length())
                : difference.substring(difference.indexOf(": ") + 2);
        boolean inServed = difference.startsWith(servedOnly);
        if (rest.startsWith("trigger ") || rest.startsWith("routine ") || rest.startsWith("view ")) {
            return true;
        }
        if (!inServed) {
            return false;
        }
        if (rest.startsWith("table ")) {
            return query.hasRows(served, rest.substring("table ".length()));
        }
        if (rest.startsWith("column ")) {
            String[] parts = rest.substring("column ".length()).split("\\|", 3);
            return parts.length >= 2 && query.hasValues(served, parts[0], parts[1]);
        }
        return false;
    }
}
