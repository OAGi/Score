package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class Acc extends Auditable implements CoreComponent, Serializable {

    private BigInteger accId;

    private String guid;

    private String type;

    private String objectClassTerm;

    private String den;

    private String definition;

    private String definitionSource;

    private String objectClassQualifier;

    private OagisComponentType oagisComponentType;

    private BigInteger namespaceId;

    private ScoreUser owner;

    private CcState state;

    private boolean deprecated;

    private boolean anAbstract;

    private BigInteger prevAccId;

    private BigInteger nextAccId;

    public BigInteger getAccId() {
        return accId;
    }

    public void setAccId(BigInteger accId) {
        this.accId = accId;
    }

    @Override
    public BigInteger getId() {
        return getAccId();
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

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
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

    public String getObjectClassQualifier() {
        return objectClassQualifier;
    }

    public void setObjectClassQualifier(String objectClassQualifier) {
        this.objectClassQualifier = objectClassQualifier;
    }

    public OagisComponentType getOagisComponentType() {
        return oagisComponentType;
    }

    public void setOagisComponentType(OagisComponentType oagisComponentType) {
        this.oagisComponentType = oagisComponentType;
    }

    public boolean isGroup() {
        return oagisComponentType.isGroup();
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

    public boolean isAbstract() {
        return anAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        this.anAbstract = anAbstract;
    }

    public BigInteger getPrevAccId() {
        return prevAccId;
    }

    public void setPrevAccId(BigInteger prevAccId) {
        this.prevAccId = prevAccId;
    }

    public BigInteger getNextAccId() {
        return nextAccId;
    }

    public void setNextAccId(BigInteger nextAccId) {
        this.nextAccId = nextAccId;
    }
}
