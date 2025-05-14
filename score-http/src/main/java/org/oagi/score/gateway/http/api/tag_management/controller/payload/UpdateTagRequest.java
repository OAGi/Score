package org.oagi.score.gateway.http.api.tag_management.controller.payload;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;

import static org.springframework.util.StringUtils.hasLength;

/**
 * Payload for updating a new tag.
 * <p>
 * This record is used to capture the necessary attributes when updating an existing tag,
 * including its ID, name, text color, background color, and an optional description.
 * The {@code textColor} and {@code backgroundColor} must be in RGB hex format (e.g., "#FFFFFF").
 *
 * @param tagId           The unique identifier of the tag. Must not be {@code null}.
 * @param name            The name of the tag. Must not be {@code null} or empty.
 * @param textColor       The text color of the tag, in RGB hex format (e.g., "#FFFFFF"). Must not be {@code null} or empty.
 * @param backgroundColor The background color of the tag, in RGB hex format (e.g., "#D1446B"). Must not be {@code null} or empty.
 * @param description     The description of the tag. Can be {@code null}.
 */
public record UpdateTagRequest(TagId tagId,
                               String name,
                               @Nullable String textColor,
                               @Nullable String backgroundColor,
                               @Nullable String description) {

    /**
     * Asserts that the request parameters meet the required constraints.
     *
     * @throws IllegalArgumentException if any required field is missing or empty.
     */
    public void assertValid() throws IllegalArgumentException {
        if (tagId() == null) {
            throw new IllegalArgumentException("`tagId` must not be null or empty.");
        }

        if (!hasLength(name())) {
            throw new IllegalArgumentException("`name` must not be null or empty.");
        }
    }

    public UpdateTagRequest withTagId(TagId tagId) {
        return new UpdateTagRequest(tagId, name, textColor, backgroundColor, description);
    }
}
