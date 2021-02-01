package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;

public class DtSc implements CoreComponent, Serializable {

    private BigInteger dtScId;

    private String guid;

    private String propertyTerm;

    private String representationTerm;

    private int cardinalityMin;

    private int cardinalityMax;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private String defaultValue;

    private String fixedValue;

    private BigInteger prevDtScId;

    private BigInteger nextDtScId;

    public BigInteger getDtScId() {
        return dtScId;
    }

    public void setDtScId(BigInteger dtScId) {
        this.dtScId = dtScId;
    }

    @Override
    public BigInteger getId() {
        return getDtScId();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
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

    public BigInteger getPrevDtScId() {
        return prevDtScId;
    }

    public void setPrevDtScId(BigInteger prevDtScId) {
        this.prevDtScId = prevDtScId;
    }

    public BigInteger getNextDtScId() {
        return nextDtScId;
    }

    public void setNextDtScId(BigInteger nextDtScId) {
        this.nextDtScId = nextDtScId;
    }
}
