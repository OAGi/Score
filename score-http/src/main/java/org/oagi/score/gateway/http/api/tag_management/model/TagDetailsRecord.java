package org.oagi.score.gateway.http.api.tag_management.model;

import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * A record representing detailed information about a tag.
 * Includes metadata such as creation and last update details.
 *
 * @param tagId           The unique identifier of the tag.
 * @param name            The name of the tag.
 * @param description     A brief description of the tag.
 * @param textColor       The text color associated with the tag.
 * @param backgroundColor The background color associated with the tag.
 * @param created         Metadata regarding the creation of the tag.
 * @param lastUpdated     Metadata regarding the last update of the tag.
 */
public record TagDetailsRecord(TagId tagId, String name, String description,
                               String textColor, String backgroundColor,
                               WhoAndWhen created, WhoAndWhen lastUpdated) {
}

