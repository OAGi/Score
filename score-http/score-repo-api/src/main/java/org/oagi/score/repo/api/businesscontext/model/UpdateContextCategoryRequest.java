package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpdateContextCategoryRequest extends Request {

    private BigInteger contextCategoryId;

    private String name;

    private String description;

    public UpdateContextCategoryRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public UpdateContextCategoryRequest withContextCategoryId(BigInteger contextCategoryId) {
        this.setContextCategoryId(contextCategoryId);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UpdateContextCategoryRequest withName(String name) {
        this.setName(name);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UpdateContextCategoryRequest withDescription(String description) {
        this.setDescription(description);
        return this;
    }
}
