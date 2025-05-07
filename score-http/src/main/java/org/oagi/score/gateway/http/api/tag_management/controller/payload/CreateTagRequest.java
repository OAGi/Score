package org.oagi.score.gateway.http.api.tag_management.controller.payload;

import jakarta.annotation.Nullable;

import static org.springframework.util.StringUtils.hasLength;

/**
 * Payload for creating a new tag.
 * <p>
 * This record is used to capture the necessary attributes when creating a new tag,
 * including its name, text color, background color, and an optional description.
 * The {@code textColor} and {@code backgroundColor} must be in RGB hex format (e.g., "#FFFFFF").
 *
 * @param name            The name of the tag. Must not be {@code null} or empty.
 * @param textColor       The text color of the tag, in RGB hex format (e.g., "#FFFFFF"). Must not be {@code null} or empty.
 * @param backgroundColor The background color of the tag, in RGB hex format (e.g., "#D1446B"). Must not be {@code null} or empty.
 * @param description     The description of the tag. Can be {@code null}.
 */
public record CreateTagRequest(String name,
                               String textColor,
                               String backgroundColor,
                               @Nullable String description) {

    /**
     * Asserts that the request parameters meet the required constraints.
     *
     * @throws IllegalArgumentException if any required field is missing or empty.
     */
    public void assertValid() throws IllegalArgumentException {
        if (!hasLength(name())) {
            throw new IllegalArgumentException("`name` must not be null or empty.");
        }

        if (!hasLength(textColor())) {
            throw new IllegalArgumentException("`textColor` must not be null or empty.");
        }

        if (!hasLength(backgroundColor())) {
            throw new IllegalArgumentException("`backgroundColor` must not be null or empty.");
        }
    }

}
