package org.oagi.score.repo.api.businesscontext.model;

import java.io.Serializable;
import java.math.BigInteger;

public class ContextSchemeValue implements Serializable {

    private BigInteger contextSchemeValueId;

    private String guid;

    private String value;

    private String meaning;

    private boolean used;

    private BigInteger ownerContextSchemeId;

    public BigInteger getContextSchemeValueId() {
        return contextSchemeValueId;
    }

    public void setContextSchemeValueId(BigInteger contextSchemeValueId) {
        this.contextSchemeValueId = contextSchemeValueId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public BigInteger getOwnerContextSchemeId() {
        return ownerContextSchemeId;
    }

    public void setOwnerContextSchemeId(BigInteger ownerContextSchemeId) {
        this.ownerContextSchemeId = ownerContextSchemeId;
    }
}
