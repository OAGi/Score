package org.oagi.score.gateway.http.common.model;

import java.util.List;

/**
 * DTO representing a result set with a corresponding count.
 * Useful for paginated queries where the result list and the total number of items are required.
 *
 * @param <T> the type of the result items in the list.
 */
public record ResultAndCount<T>(List<T> result, int count) {
}
