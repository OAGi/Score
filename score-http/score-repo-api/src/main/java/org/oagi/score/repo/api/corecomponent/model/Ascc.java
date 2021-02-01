package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class Ascc implements CoreComponent, CcAssociation, Serializable {

    private BigInteger asccId;

    private String guid;

    private int cardinalityMin;

    private int cardinalityMax;

    private String den;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private BigInteger prevAsccId;

    private BigInteger nextAsccId;

    public BigInteger getAsccId() {
        return asccId;
    }

    public void setAsccId(BigInteger asccId) {
        this.asccId = asccId;
    }

    @Override
    public BigInteger getId() {
        return getAsccId();
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

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
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

    public BigInteger getPrevAsccId() {
        return prevAsccId;
    }

    public void setPrevAsccId(BigInteger prevAsccId) {
        this.prevAsccId = prevAsccId;
    }

    public BigInteger getNextAsccId() {
        return nextAsccId;
    }

    public void setNextAsccId(BigInteger nextAsccId) {
        this.nextAsccId = nextAsccId;
    }

    @Override
    public boolean isManifest() {
        return false;
    }

    @Override
    public boolean isAscc() {
        return true;
    }

    @Override
    public boolean isBcc() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ascc that = (Ascc) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(asccId, that.asccId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asccId, isManifest(), isAscc(), isBcc());
    }
}
