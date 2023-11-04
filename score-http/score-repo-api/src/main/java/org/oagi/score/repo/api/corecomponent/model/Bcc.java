package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class Bcc implements CoreComponent, CcAssociation, Serializable {

    private BigInteger bccId;

    private String guid;

    private int cardinalityMin;

    private int cardinalityMax;

    private EntityType entityType;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private boolean nillable;

    private String defaultValue;

    private String fixedValue;

    private BigInteger prevBccId;

    private BigInteger nextBccId;

    public BigInteger getBccId() {
        return bccId;
    }

    public void setBccId(BigInteger bccId) {
        this.bccId = bccId;
    }

    @Override
    public BigInteger getId() {
        return getBccId();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(int cardinalityMin) {
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        this.cardinalityMax = cardinalityMax;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public BigInteger getPrevBccId() {
        return prevBccId;
    }

    public void setPrevBccId(BigInteger prevBccId) {
        this.prevBccId = prevBccId;
    }

    public BigInteger getNextBccId() {
        return nextBccId;
    }

    public void setNextBccId(BigInteger nextBccId) {
        this.nextBccId = nextBccId;
    }

    @Override
    public boolean isManifest() {
        return false;
    }

    @Override
    public boolean isAscc() {
        return false;
    }

    @Override
    public boolean isBcc() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bcc that = (Bcc) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(bccId, that.bccId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bccId, isManifest(), isAscc(), isBcc());
    }
}
