package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Which releases a package has, for each library. The information file names the releases but not their libraries, which the script has,
 * in the statements that put the {@code library} and the {@code release} records in place. Only those two statements are read: the rest
 * of the script, which is most of it, is passed over without being kept.
 */
public final class ReleasePackageReleases {

    /** The name of the release that is worked on, which is not the latest of a library whatever its number is. */
    public static final String WORKING = "Working";

    private static final byte[] LIBRARY = "INSERT INTO `library` (".getBytes(StandardCharsets.UTF_8);
    private static final byte[] RELEASE = "INSERT INTO `release` (".getBytes(StandardCharsets.UTF_8);
    private static final int LONGEST_STATEMENT = 64 << 20;

    private ReleasePackageReleases() {
    }

    /** The latest release of each library that the scripts have, which is the working release for a library that has no other. */
    public static List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest(ScriptSource scripts, List<String> names) throws IOException {
        Map<LibraryId, LibrarySummaryRecord> libraries = new HashMap<>();
        List<ReleaseSummaryRecord> releases = new ArrayList<>();
        for (String name : names) {
            if (!scripts.exists(name)) {
                continue;
            }
            try (InputStream in = scripts.open(name)) {
                read(in, libraries, releases);
            }
            if (!releases.isEmpty()) {
                break;
            }
        }
        return latestOf(libraries, releases);
    }

    /**
     * The release of the releases of one library that is the latest: the one made last (a release is numbered as it is made) that is not
     * the working one, or the working one when there is no other.
     */
    public static Optional<ReleaseSummaryRecord> latestOf(Collection<ReleaseSummaryRecord> releases) {
        return releases.stream().filter(release -> !isWorking(release)).max(BY_ID)
                .or(() -> releases.stream().max(BY_ID));
    }

    public static boolean isWorking(ReleaseSummaryRecord release) {
        return WORKING.equals(release.releaseNum());
    }

    private static final Comparator<ReleaseSummaryRecord> BY_ID = Comparator.comparing(release -> release.releaseId().value());

    static List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latestOf(Map<LibraryId, LibrarySummaryRecord> libraries, List<ReleaseSummaryRecord> releases) {
        Map<LibraryId, List<ReleaseSummaryRecord>> byLibrary = new TreeMap<>(Comparator.comparing(LibraryId::value));
        for (ReleaseSummaryRecord release : releases) {
            byLibrary.computeIfAbsent(release.libraryId(), id -> new ArrayList<>()).add(release);
        }
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = new ArrayList<>();
        byLibrary.forEach((libraryId, of) -> latestOf(of).ifPresent(release -> latest.add(Pair.of(
                libraries.getOrDefault(libraryId, summaryOf(libraryId, null)), release))));
        return latest;
    }

    /** What the script says of a library is its identifier and its name; the rest is not read. */
    private static LibrarySummaryRecord summaryOf(LibraryId libraryId, String name) {
        return new LibrarySummaryRecord(libraryId, name, null, false, false);
    }

    /** Reads the statements of the libraries and of the releases of one script, and stops after the releases, which come after the libraries. */
    static void read(InputStream in, Map<LibraryId, LibrarySummaryRecord> libraries, List<ReleaseSummaryRecord> releases) throws IOException {
        Lines lines = new Lines(in);
        while (true) {
            byte[] statement = lines.nextStatementOf(LIBRARY, RELEASE);
            if (statement == null) {
                return;
            }
            String sql = new String(statement, StandardCharsets.UTF_8);
            if (startsWith(statement, LIBRARY)) {
                for (Map<String, String> row : Insert.rows(sql)) {
                    BigInteger id = number(row.get("library_id"));
                    if (id != null) {
                        LibraryId libraryId = new LibraryId(id);
                        libraries.put(libraryId, summaryOf(libraryId, row.get("name")));
                    }
                }
            } else {
                for (Map<String, String> row : Insert.rows(sql)) {
                    BigInteger releaseId = number(row.get("release_id"));
                    BigInteger libraryId = number(row.get("library_id"));
                    if (releaseId != null && libraryId != null) {
                        // What the script says of a release is its number and its library; the state is not read.
                        releases.add(new ReleaseSummaryRecord(new ReleaseId(releaseId), new LibraryId(libraryId), row.get("release_num"), null));
                    }
                }
                return;
            }
        }
    }

    /** The number a value is, or null when it is not one (a value that is not a number is not an identifier). */
    private static BigInteger number(String value) {
        try {
            return value == null ? null : new BigInteger(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /** Lines of a stream, of which only those that start with one of the prefixes are kept; the others are passed over, however long. */
    private static final class Lines {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int position;
        private int limit;

        Lines(InputStream in) {
            this.in = in;
        }

        private boolean fill() throws IOException {
            do {
                limit = in.read(buffer, 0, buffer.length);
            } while (limit == 0);
            position = 0;
            return limit > 0;
        }

        /** The next line that starts with one of the prefixes, or null when there is none. */
        byte[] nextStatementOf(byte[]... prefixes) throws IOException {
            int longest = 0;
            for (byte[] prefix : prefixes) {
                longest = Math.max(longest, prefix.length);
            }
            byte[] head = new byte[longest];
            while (true) {
                int count = 0;
                boolean endOfLine = false;
                while (count < longest) {
                    if (position >= limit && !fill()) {
                        return count == 0 ? null : matching(head, count, prefixes) ? rest(head, count) : null;
                    }
                    byte b = buffer[position++];
                    if (b == '\n') {
                        endOfLine = true;
                        break;
                    }
                    head[count++] = b;
                }
                if (endOfLine) {
                    continue;
                }
                if (matching(head, count, prefixes)) {
                    byte[] statement = rest(head, count);
                    if (statement != null) {
                        return statement;
                    }
                } else if (!skipLine()) {
                    return null;
                }
            }
        }

        private static boolean matching(byte[] head, int count, byte[]... prefixes) {
            for (byte[] prefix : prefixes) {
                if (count >= prefix.length && startsWith(head, prefix)) {
                    return true;
                }
            }
            return false;
        }

        /** The rest of the line, with what was read of it; null when it is longer than a statement of this kind can be (it is passed over). */
        private byte[] rest(byte[] head, int count) throws IOException {
            java.io.ByteArrayOutputStream line = new java.io.ByteArrayOutputStream();
            line.write(head, 0, count);
            while (true) {
                if (position >= limit && !fill()) {
                    return line.toByteArray();
                }
                int end = position;
                while (end < limit && buffer[end] != '\n') {
                    end++;
                }
                line.write(buffer, position, end - position);
                boolean found = end < limit;
                position = found ? end + 1 : limit;
                if (line.size() > LONGEST_STATEMENT) {
                    if (!found) {
                        skipLine();
                    }
                    return null;
                }
                if (found) {
                    return line.toByteArray();
                }
            }
        }

        /** Passes over the rest of the line; false when the stream ends. */
        private boolean skipLine() throws IOException {
            while (true) {
                if (position >= limit && !fill()) {
                    return false;
                }
                while (position < limit) {
                    if (buffer[position++] == '\n') {
                        return true;
                    }
                }
            }
        }
    }

    /** The rows of one {@code INSERT INTO ... (columns) VALUES (...), (...);} statement, by the name of the column. */
    static final class Insert {
        private final String sql;
        private int at;

        private Insert(String sql) {
            this.sql = sql;
        }

        static List<Map<String, String>> rows(String sql) {
            return new Insert(sql).parse();
        }

        private List<Map<String, String>> parse() {
            at = sql.indexOf('(');
            List<String> columns = new ArrayList<>();
            at++;
            while (at < sql.length() && sql.charAt(at) != ')') {
                if (sql.charAt(at) == '`') {
                    int end = sql.indexOf('`', at + 1);
                    columns.add(sql.substring(at + 1, end));
                    at = end + 1;
                } else {
                    at++;
                }
            }
            int values = sql.toUpperCase(java.util.Locale.ROOT).indexOf("VALUES", at);
            List<Map<String, String>> rows = new ArrayList<>();
            if (values < 0) {
                return rows;
            }
            at = values + "VALUES".length();
            while (true) {
                while (at < sql.length() && sql.charAt(at) != '(' && sql.charAt(at) != ';') {
                    at++;
                }
                if (at >= sql.length() || sql.charAt(at) == ';') {
                    return rows;
                }
                at++;
                Map<String, String> row = new LinkedHashMap<>();
                int column = 0;
                while (at < sql.length()) {
                    String value = value();
                    if (column < columns.size()) {
                        row.put(columns.get(column), value);
                    }
                    column++;
                    while (at < sql.length() && Character.isWhitespace(sql.charAt(at))) {
                        at++;
                    }
                    char c = at < sql.length() ? sql.charAt(at++) : ')';
                    if (c == ')') {
                        break;
                    }
                }
                rows.add(row);
            }
        }

        /** One value, with {@code at} left at the character that ends it: null for NULL, the text without its quotes for a string. */
        private String value() {
            while (at < sql.length() && Character.isWhitespace(sql.charAt(at))) {
                at++;
            }
            if (at < sql.length() && sql.charAt(at) == '_') {   // a character set introducer, as in _binary '...'
                while (at < sql.length() && sql.charAt(at) != '\'' && sql.charAt(at) != ',' && sql.charAt(at) != ')') {
                    at++;
                }
            }
            if (at < sql.length() && sql.charAt(at) == '\'') {
                StringBuilder text = new StringBuilder();
                at++;
                while (at < sql.length()) {
                    char c = sql.charAt(at++);
                    if (c == '\\' && at < sql.length()) {
                        char escaped = sql.charAt(at++);
                        text.append(switch (escaped) {
                            case 'n' -> '\n';
                            case 'r' -> '\r';
                            case 't' -> '\t';
                            case '0' -> '\0';
                            case 'b' -> '\b';
                            case 'Z' -> '\u001a';
                            default -> escaped;
                        });
                    } else if (c == '\'') {
                        if (at < sql.length() && sql.charAt(at) == '\'') {
                            text.append('\'');
                            at++;
                        } else {
                            return text.toString();
                        }
                    } else {
                        text.append(c);
                    }
                }
                return text.toString();
            }
            int start = at;
            int depth = 0;   // a call, as NOW(), has parentheses of its own
            while (at < sql.length()) {
                char c = sql.charAt(at);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    if (depth == 0) {
                        break;
                    }
                    depth--;
                } else if (c == ',' && depth == 0) {
                    break;
                }
                at++;
            }
            String token = sql.substring(start, at).trim();
            return "NULL".equalsIgnoreCase(token) ? null : token;
        }
    }
}
