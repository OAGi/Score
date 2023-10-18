package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class Asccp extends Auditable implements CoreComponent, Serializable {

    private BigInteger asccpId;

    private String guid;

    private String type;

    private String propertyTerm;

    private String definition;

    private String definitionSource;

    private BigInteger namespaceId;

    private ScoreUser owner;

    private CcState state;

    private boolean deprecated;

    private boolean reusable;

    private boolean nillable;

    private BigInteger prevAsccpId;

    private BigInteger nextAsccpId;

    public BigInteger getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(BigInteger asccpId) {
        this.asccpId = asccpId;
    }

    @Override
    public BigInteger getId() {
        return getAsccpId();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
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

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public BigInteger getPrevAsccpId() {
        return prevAsccpId;
    }

    public void setPrevAsccpId(BigInteger prevAsccpId) {
        this.prevAsccpId = prevAsccpId;
    }

    public BigInteger getNextAsccpId() {
        return nextAsccpId;
    }

    public void setNextAsccpId(BigInteger nextAsccpId) {
        this.nextAsccpId = nextAsccpId;
    }
}
