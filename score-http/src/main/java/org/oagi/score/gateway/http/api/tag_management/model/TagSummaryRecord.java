package org.oagi.score.gateway.http.api.tag_management.model;

/**
 * A record representing a summary of a tag.
 * Contains essential tag attributes for display purposes.
 *
 * @param tagId           The unique identifier of the tag.
 * @param name            The name of the tag.
 * @param textColor       The text color associated with the tag.
 * @param backgroundColor The background color associated with the tag.
 */
public record TagSummaryRecord(TagId tagId, String name,
                               String textColor, String backgroundColor) {
}
