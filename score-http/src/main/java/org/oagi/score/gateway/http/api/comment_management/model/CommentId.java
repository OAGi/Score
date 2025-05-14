package org.oagi.score.gateway.http.api.comment_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a comment.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record CommentId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static CommentId from(String value) {
        return new CommentId(new BigInteger(value));
    }

    @JsonCreator
    public static CommentId from(BigInteger value) {
        return new CommentId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
