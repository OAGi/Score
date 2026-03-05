package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

/**
 * Utility helpers for composing filename-safe tokens.
 */
final class FilenameTokenUtils {

    private FilenameTokenUtils() {
    }

    /**
     * Trims the text, replaces internal whitespace with '-', and removes invalid filename chars.
     */
    static String normalizeTokenWithHyphen(String text) {
        return sanitizeFileName(compactWhitespace(text, "-"));
    }

    /**
     * Trims the text, removes internal whitespace, and removes invalid filename chars.
     */
    static String normalizeTokenWithoutSpaces(String text) {
        return sanitizeFileName(compactWhitespace(text, ""));
    }

    /**
     * Removes characters that are invalid in common filesystem filename rules.
     */
    static String sanitizeFileName(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    private static String compactWhitespace(String text, String replacement) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", replacement);
    }

}
