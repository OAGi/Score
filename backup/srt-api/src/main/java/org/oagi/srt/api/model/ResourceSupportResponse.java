package org.oagi.srt.api.model;

import org.springframework.hateoas.ResourceSupport;

public abstract class ResourceSupportResponse extends ResourceSupport implements Response {

    private final String category;

    public ResourceSupportResponse(String category) {
        this.category = category;
    }

    @Override
    public String getCategory() {
        return category;
    }
}
