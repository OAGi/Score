package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.UnaryOperator;

/**
 * Executes a SQL script statement by statement while reading it, so that memory use is bounded by the
 * largest single statement rather than by the size of the script.
 * <p>
 * Statements are separated by ';'. Quoted strings and identifiers ('...', "...", `...`), '--' and '#' line
 * comments, and block comments are recognized. MySQL executable comments ('/*! ... *&#47;') are kept.
 * <p>
 * The mysql client command {@code DELIMITER <token>} on a line of its own changes the separator, so that a
 * compound statement such as a trigger body ({@code BEGIN ... END}) with inner ';' is sent as one statement.
 */
public final class SqlScriptExecutor {

    private SqlScriptExecutor() {
    }

    /**
     * @return the number of statements executed
     */
    public static int execute(Connection con, Reader reader) throws SQLException, IOException {
        return execute(con, reader, UnaryOperator.identity());
    }

    /**
     * @param transform what each statement is changed by before it is executed
     * @return the number of statements executed
     */
    public static int execute(Connection con, Reader reader, UnaryOperator<String> transform) throws SQLException, IOException {
        CharSource src = new CharSource(reader);
        StringBuilder sb = new StringBuilder();
        String delimiter = ";";
        int count = 0;

        try (Statement statement = con.createStatement()) {
            int c;
            while ((c = src.read()) >= 0) {
                switch (c) {
                    case '\'':
                    case '"':
                    case '`':
                        sb.append((char) c);
                        copyQuoted(src, sb, (char) c);
                        break;
                    case '-':
                        if (src.peek() == '-') {
                            skipLine(src, sb);
                        } else {
                            sb.append('-');
                        }
                        break;
                    case '#':
                        skipLine(src, sb);
                        break;
                    case '/':
                        if (src.peek() == '*') {
                            src.read();
                            if (src.peek() == '!') {
                                sb.append("/*");
                            } else {
                                skipBlockComment(src, sb);
                            }
                        } else {
                            sb.append('/');
                        }
                        break;
                    case '\n': {
                        String next = delimiterCommand(sb);
                        if (next != null) {
                            delimiter = next;
                            sb.setLength(0);
                        } else {
                            sb.append('\n');
                        }
                        break;
                    }
                    default:
                        sb.append((char) c);
                        if (endsWith(sb, delimiter) && !isDelimiterCommand(sb)) {
                            sb.setLength(sb.length() - delimiter.length());
                            count += run(statement, sb, transform);
                        }
                }
            }
            if (delimiterCommand(sb) != null) {
                sb.setLength(0);
            }
            count += run(statement, sb, transform);
        }
        return count;
    }

    /**
     * @return the new delimiter if the buffer holds only a {@code DELIMITER <token>} command, otherwise null
     */
    private static String delimiterCommand(StringBuilder sb) {
        String text = sb.toString().trim();
        if (text.length() > 10 && text.regionMatches(true, 0, "DELIMITER", 0, 9)
                && Character.isWhitespace(text.charAt(9))) {
            String token = text.substring(10).trim();
            if (!token.isEmpty() && token.indexOf('\n') < 0) {
                return token;
            }
        }
        return null;
    }

    private static boolean isDelimiterCommand(StringBuilder sb) {
        int i = 0;
        while (i < sb.length() && Character.isWhitespace(sb.charAt(i))) {
            i++;
        }
        return sb.length() - i > 9 && sb.substring(i, i + 9).equalsIgnoreCase("DELIMITER")
                && Character.isWhitespace(sb.charAt(i + 9));
    }

    private static boolean endsWith(StringBuilder sb, String suffix) {
        int offset = sb.length() - suffix.length();
        return offset >= 0 && sb.indexOf(suffix, offset) == offset;
    }

    private static int run(Statement statement, StringBuilder sb, UnaryOperator<String> transform) throws SQLException {
        String sql = sb.toString().trim();
        sb.setLength(0);
        if (sql.isEmpty()) {
            return 0;
        }
        statement.execute(transform.apply(sql));
        return 1;
    }

    private static void copyQuoted(CharSource src, StringBuilder sb, char quote) throws IOException {
        int c;
        while ((c = src.read()) >= 0) {
            sb.append((char) c);
            if (c == '\\' && quote != '`') {
                int escaped = src.read();
                if (escaped >= 0) {
                    sb.append((char) escaped);
                }
            } else if (c == quote) {
                return; // a doubled quote ('') reopens immediately in the caller's loop
            }
        }
    }

    private static void skipLine(CharSource src, StringBuilder sb) throws IOException {
        int c;
        while ((c = src.read()) >= 0 && c != '\n') {
            // skip
        }
        sb.append('\n');
    }

    private static void skipBlockComment(CharSource src, StringBuilder sb) throws IOException {
        int c;
        while ((c = src.read()) >= 0) {
            if (c == '*' && src.peek() == '/') {
                src.read();
                break;
            }
        }
        sb.append(' ');
    }

    private static final class CharSource {

        private final Reader reader;
        private final char[] buf = new char[64 * 1024];
        private int pos;
        private int len;

        CharSource(Reader reader) {
            this.reader = reader;
        }

        int peek() throws IOException {
            if (pos == len && !fill()) {
                return -1;
            }
            return buf[pos];
        }

        int read() throws IOException {
            int c = peek();
            if (c >= 0) {
                pos++;
            }
            return c;
        }

        private boolean fill() throws IOException {
            pos = 0;
            len = reader.read(buf, 0, buf.length);
            if (len < 0) {
                len = 0;
                return false;
            }
            return len > 0 || fill();
        }
    }

}
