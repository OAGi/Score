package org.oagi.score.gateway.http.api.application_management.controller.payload;

import lombok.Data;

@Data
public class FilenameExpressionSuggestRequest {

    private String text;
    /** The caret position in {@code text}; the end when absent. */
    private Integer caret;
    /** Whether {@code text} is the duplicate handler expression rather than the filename expression. */
    private boolean duplicateHandler;

}
