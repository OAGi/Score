package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.common.model.TextSearchPredicate;

/**
 * One condition of a search expression that the server evaluates.
 *
 * @param id            what the client calls the predicate; the answer is given under it
 * @param attribute     the attribute whose text is compared
 * @param operator      how {@code value} is compared with the stored text
 * @param value         the text to look for
 * @param caseSensitive whether upper and lower case letters differ (the stored text is compared without regard to case otherwise)
 */
public record BieSearchPredicate(String id, BieSearchAttribute attribute, TextSearchOperator operator, String value, boolean caseSensitive)
        implements TextSearchPredicate {
}
