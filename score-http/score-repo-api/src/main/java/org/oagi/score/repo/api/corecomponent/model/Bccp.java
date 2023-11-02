package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class Bccp extends Auditable implements CoreComponent, Serializable {

    private BigInteger bccpId;

    private String guid;

    private String propertyTerm;

    private String representationTerm;

    private String definition;

    private String definitionSource;

    private BigInteger namespaceId;

    private ScoreUser owner;

    private CcState state;

    private boolean deprecated;

    private boolean nillable;

    private String defaultValue;

    private String fixedValue;

    private BigInteger prevBccpId;

    private BigInteger nextBccpId;

    public BigInteger getBccpId() {
        return bccpId;
    }

    public void setBccpId(BigInteger bccpId) {
        this.bccpId = bccpId;
    }

    @Override
    public BigInteger getId() {
        return getBccpId();
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

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }

    public ScoreUser getOwner() {
        return owner;
    }

    public void setOwner(ScoreUser owner) {
        this.owner = owner;
    }

    public CcState getState() {
        return state;
    }

    public void setState(CcState state) {
        this.state = state;
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

    public BigInteger getPrevBccpId() {
        return prevBccpId;
    }

    public void setPrevBccpId(BigInteger prevBccpId) {
        this.prevBccpId = prevBccpId;
    }

    public BigInteger getNextBccpId() {
        return nextBccpId;
    }

    public void setNextBccpId(BigInteger nextBccpId) {
        this.nextBccpId = nextBccpId;
    }
}
