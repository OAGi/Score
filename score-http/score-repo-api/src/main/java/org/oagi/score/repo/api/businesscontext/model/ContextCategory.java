package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ContextCategory extends Auditable implements Serializable {

    private BigInteger contextCategoryId;

    private String guid;

    private String name;

    private String description;

    private boolean used;

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
