package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import java.util.regex.Pattern;
import java.util.function.UnaryOperator;

/**
 * What makes a script of a package leave the structure of the tables as the schema has it. The script of a package of the version 1 writes,
 * for some tables, the removal of the table and the making of it again from a copy of its structure; the schema the package is loaded into
 * was made by the migrations of the application, which is the structure the application is built for, and a copy that was kept elsewhere may
 * be behind it. So the table is emptied instead of removed, and the statement that makes it is not run: what the package carries is the records.
 */
final class KeepSchemaStructure implements UnaryOperator<String> {

    private static final Pattern DROP_TABLE = Pattern.compile("^\\s*DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(`[^`]+`)\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_TABLE = Pattern.compile("^\\s*CREATE\\s+(?:TEMPORARY\\s+)?TABLE\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public String apply(String statement) {
        var drop = DROP_TABLE.matcher(statement);
        if (drop.matches()) {
            return "DELETE FROM " + drop.group(1);
        }
        if (CREATE_TABLE.matcher(statement).find()) {
            return "DO 0";
        }
        return statement;
    }
}
