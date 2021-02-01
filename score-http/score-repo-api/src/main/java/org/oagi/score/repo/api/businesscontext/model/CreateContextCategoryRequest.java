package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class CreateContextCategoryRequest extends Request {

    private String name;

    private String description;

    public CreateContextCategoryRequest(ScoreUser requester) {
        super(requester);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateContextCategoryRequest withName(String name) {
        this.setName(name);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CreateContextCategoryRequest withDescription(String description) {
        this.setDescription(description);
        return this;
    }
}
