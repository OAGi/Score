package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What tells that two schemas hold the same data: for each table, the number of its records and the sum of a hash of each record,
 * which does not depend on the order of the records, nor on the order of the columns of the table (the columns are taken by name).
 * A record is its columns as text (a column that holds bytes is in hexadecimal), with NULL told from a text, so what the
 * records say is what is compared, not how the table is made.
 */
final class SchemaChecksums {

    private static final Set<String> BYTES = Set.of("binary", "varbinary", "tinyblob", "blob", "mediumblob", "longblob");

    private SchemaChecksums() {
    }

    /** The count and the checksum of each table of the schema but those given, by the name of the table. */
    static Map<String, String> of(Connection con, String schema, Set<String> except) throws SQLException {
        Map<String, String> checksums = new LinkedHashMap<>();
        for (String table : tables(con, schema)) {
            if (!except.contains(table)) {
                checksums.put(table, checksum(con, schema, table));
            }
        }
        return checksums;
    }

    /** The records of all the tables of the checksums, for what is told of a run. */
    static long records(Map<String, String> checksums) {
        return checksums.values().stream().mapToLong(c -> Long.parseLong(c.substring(0, c.indexOf(':')))).sum();
    }

    private static List<String> tables(Connection con, String schema) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name")) {
            statement.setString(1, schema);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        }
        return tables;
    }

    private static String checksum(Connection con, String schema, String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT column_name, data_type FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = ? ORDER BY column_name")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String column = "`" + rs.getString(1) + "`";
                    String text = BYTES.contains(rs.getString(2).toLowerCase()) ? "HEX(" + column + ")" : "CAST(" + column + " AS CHAR)";
                    columns.add("IFNULL(" + text + ", CONCAT(CHAR(1), 'NULL'))");
                }
            }
        }
        String record = "CONCAT_WS(CHAR(2), " + String.join(", ", columns) + ")";
        String sql = "SELECT CONCAT(COUNT(*), ':', IFNULL(SUM(CAST(CONV(SUBSTRING(MD5(" + record + "), 1, 12), 16, 10) AS DECIMAL(30, 0))), 0)) "
                + "FROM `" + schema + "`.`" + table + "`";
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    /** The tables whose checksums differ, or that only one has, as lines for a message. */
    static List<String> differences(Map<String, String> expected, Map<String, String> actual) {
        List<String> differences = new ArrayList<>();
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String other = actual.get(entry.getKey());
            if (other == null) {
                differences.add(entry.getKey() + ": only in the first");
            } else if (!entry.getValue().equals(other)) {
                differences.add(entry.getKey() + ": " + entry.getValue() + " != " + other);
            }
        }
        actual.keySet().stream().filter(table -> !expected.containsKey(table))
                .forEach(table -> differences.add(table + ": only in the second"));
        return differences;
    }
}
