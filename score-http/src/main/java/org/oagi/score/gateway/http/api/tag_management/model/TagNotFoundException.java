package org.oagi.score.gateway.http.api.tag_management.model;

/**
 * Exception thrown when a tag is not found.
 */
public class TagNotFoundException extends Exception {

    private final TagId tagId;

    public TagNotFoundException(TagId tagId) {
        this.tagId = tagId;
    }

    public TagId getTagId() {
        return tagId;
    }

}
