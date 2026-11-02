package org.oagi.score.gateway.http.api.application_management.controller.payload;

import java.util.List;

public record FilenameExpressionSuggestResponse(int from, int to, List<Item> items) {

    /** Replace {@code [from, to)} with {@code insert}; the caret then goes {@code caretBack} before its end. */
    public record Item(String label, String insert, String detail, int caretBack) {
    }
}
