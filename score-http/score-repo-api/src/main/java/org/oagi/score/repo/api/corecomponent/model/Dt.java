package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class Dt extends Auditable implements CoreComponent, Serializable {

    private BigInteger dtId;

    private String guid;

    private DtType type;

    private String versionNum;

    private String dataTypeTerm;

    private String qualifier;

    private String den;

    private String definition;

    private String definitionSource;

    private String contentComponentDen;

    private String contentComponentDefinition;

    private BigInteger namespaceId;

    private ScoreUser owner;

    private CcState state;

    private boolean deprecated;

    private boolean commonlyUsed;

    private BigInteger prevDtId;

    private BigInteger nextDtId;

    public BigInteger getDtId() {
        return dtId;
    }

    public void setDtId(BigInteger dtId) {
        this.dtId = dtId;
    }

    @Override
    public BigInteger getId() {
        return getDtId();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public DtType getType() {
        return type;
    }

    public void setType(DtType type) {
        this.type = type;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getDataTypeTerm() {
        return dataTypeTerm;
    }

    public void setDataTypeTerm(String dataTypeTerm) {
        this.dataTypeTerm = dataTypeTerm;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
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

    public String getContentComponentDen() {
        return contentComponentDen;
    }

    public void setContentComponentDen(String contentComponentDen) {
        this.contentComponentDen = contentComponentDen;
    }

    public String getContentComponentDefinition() {
        return contentComponentDefinition;
    }

    public void setContentComponentDefinition(String contentComponentDefinition) {
        this.contentComponentDefinition = contentComponentDefinition;
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

    public boolean isCommonlyUsed() {
        return commonlyUsed;
    }

    public void setCommonlyUsed(boolean commonlyUsed) {
        this.commonlyUsed = commonlyUsed;
    }

    public BigInteger getPrevDtId() {
        return prevDtId;
    }

    public void setPrevDtId(BigInteger prevDtId) {
        this.prevDtId = prevDtId;
    }

    public BigInteger getNextDtId() {
        return nextDtId;
    }

    public void setNextDtId(BigInteger nextDtId) {
        this.nextDtId = nextDtId;
    }
}
