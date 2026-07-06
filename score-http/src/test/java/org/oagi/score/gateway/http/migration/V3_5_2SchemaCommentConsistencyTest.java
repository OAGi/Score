package org.oagi.score.gateway.http.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1759: the {@code V3_5_2__upgrade_from_3_5_1.sql} migration restates the full definition
 * and {@code COMMENT} of ~221 columns through {@code ALTER TABLE ... MODIFY COLUMN ...}. Each
 * restatement is only safe if it stays byte-consistent with the per-table reference DDL under
 * {@code src/main/resources/schemas/<table>.ddl}: a single drifted type/length/default would
 * SILENTLY ALTER the column when the migration runs, and a drifted comment would defeat the entire
 * purpose of the change. This test replaces the one-time manual "byte-for-byte" verification with an
 * automated guard — every MODIFY COLUMN must agree with its reference DDL on both the
 * (whitespace-normalized) column definition and the comment text.
 *
 * <p>Pure classpath/string comparison — no database required.</p>
 */
class V3_5_2SchemaCommentConsistencyTest {

    private static final String MIGRATION = "/db/migration/V3_5_2__upgrade_from_3_5_1.sql";

    // ALTER TABLE `t` MODIFY COLUMN `c` <def> COMMENT '<comment>';
    private static final Pattern MODIFY_COLUMN = Pattern.compile(
            "ALTER TABLE `([A-Za-z0-9_]+)` MODIFY COLUMN `([A-Za-z0-9_]+)` (.*?) COMMENT '(.*)';\\s*$",
            Pattern.MULTILINE);

    // `c` <def> COMMENT '<comment>',  — a column line inside a CREATE TABLE reference DDL
    private static final Pattern DDL_COLUMN = Pattern.compile(
            "^\\s*`([A-Za-z0-9_]+)`\\s+(.*?)\\s+COMMENT '(.*)',?\\s*$");

    @Test
    void everyModifyColumnMatchesItsReferenceDdl() {
        String migration = readClasspath(MIGRATION);
        assertNotNull(migration, "V3_5_2 migration not found on the classpath: " + MIGRATION);

        // The parser must account for EVERY MODIFY COLUMN, so drift cannot hide inside a statement
        // the regex silently fails to read.
        int modifyCount = countOccurrences(migration, "MODIFY COLUMN");

        Matcher m = MODIFY_COLUMN.matcher(migration);
        Map<String, Map<String, Column>> ddlByTable = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        int parsed = 0;
        while (m.find()) {
            parsed++;
            String table = m.group(1);
            String column = m.group(2);
            String def = normalize(m.group(3));
            String comment = m.group(4);

            Map<String, Column> ddl = ddlByTable.computeIfAbsent(table, this::readDdl);
            if (ddl == null) {
                problems.add(table + "." + column + ": no reference DDL /schemas/" + table + ".ddl");
                continue;
            }
            Column ref = ddl.get(column);
            if (ref == null) {
                problems.add(table + "." + column + ": column absent from /schemas/" + table + ".ddl");
                continue;
            }
            if (!def.equals(ref.def())) {
                problems.add(table + "." + column + ": definition drift"
                        + "\n    migration: " + def
                        + "\n    ddl:       " + ref.def());
            }
            if (!comment.equals(ref.comment())) {
                problems.add(table + "." + column + ": comment drift"
                        + "\n    migration: " + comment
                        + "\n    ddl:       " + ref.comment());
            }
        }

        assertEquals(modifyCount, parsed,
                "The test parsed " + parsed + " of " + modifyCount + " MODIFY COLUMN statements; "
                        + "tighten the pattern so drift cannot hide in an unread statement.");
        assertTrue(problems.isEmpty(),
                "V3_5_2 MODIFY COLUMN statements are inconsistent with their reference DDLs:\n"
                        + String.join("\n", problems));
    }

    private record Column(String def, String comment) {
    }

    private Map<String, Column> readDdl(String table) {
        String ddl = readClasspath("/schemas/" + table + ".ddl");
        if (ddl == null) {
            return null;
        }
        Map<String, Column> columns = new LinkedHashMap<>();
        for (String line : ddl.split("\n")) {
            Matcher m = DDL_COLUMN.matcher(line);
            if (m.matches()) {
                columns.put(m.group(1), new Column(normalize(m.group(2)), m.group(3)));
            }
        }
        return columns;
    }

    private static String normalize(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }

    private static String readClasspath(String path) {
        try (InputStream in = V3_5_2SchemaCommentConsistencyTest.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
