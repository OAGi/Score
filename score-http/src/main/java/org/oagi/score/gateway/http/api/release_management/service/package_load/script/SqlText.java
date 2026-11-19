package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

/**
 * What makes two texts of the same SQL the same: without its comments, and with every run of white space as one space. The body of a
 * trigger or of a routine is kept as it was written by what made it, and the client that made it may have taken the comments out (the
 * command line client does) while another kept them; they do the same.
 *
 * <p>Quoted text and names ({@code '...'}, {@code "..."}, {@code `...`}) are kept as they are, and so are the comments that the server
 * runs ({@code /*! ... *}{@code /}).</p>
 */
public final class SqlText {

    private SqlText() {
    }

    public static String normalize(String sql) {
        if (sql == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(sql.length());
        int n = sql.length();
        for (int i = 0; i < n; i++) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                i = copyQuoted(sql, i, out);
            } else if (c == '-' && i + 1 < n && sql.charAt(i + 1) == '-' && (i + 2 >= n || Character.isWhitespace(sql.charAt(i + 2)))) {
                i = skipLine(sql, i);
                space(out);
            } else if (c == '#') {
                i = skipLine(sql, i);
                space(out);
            } else if (c == '/' && i + 1 < n && sql.charAt(i + 1) == '*' && !(i + 2 < n && sql.charAt(i + 2) == '!')) {
                int end = sql.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 1;
                space(out);
            } else if (Character.isWhitespace(c)) {
                space(out);
            } else {
                out.append(c);
            }
        }
        return out.toString().trim();
    }

    private static void space(StringBuilder out) {
        if (out.length() > 0 && out.charAt(out.length() - 1) != ' ') {
            out.append(' ');
        }
    }

    /** Copies the quoted text that starts at {@code start}; the index of its closing quote. A quote is escaped by a backslash or by itself. */
    private static int copyQuoted(String sql, int start, StringBuilder out) {
        char quote = sql.charAt(start);
        out.append(quote);
        int i = start + 1;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            out.append(c);
            if (c == '\\' && quote != '`' && i + 1 < sql.length()) {
                out.append(sql.charAt(++i));
            } else if (c == quote) {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == quote) {
                    out.append(sql.charAt(++i));
                } else {
                    return i;
                }
            }
            i++;
        }
        return i;
    }

    private static int skipLine(String sql, int start) {
        int end = sql.indexOf('\n', start);
        return end < 0 ? sql.length() : end;
    }
}
