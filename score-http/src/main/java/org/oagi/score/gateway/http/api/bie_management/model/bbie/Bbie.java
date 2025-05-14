package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.BieAssociation;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.math.BigInteger;

public class Bbie implements BieAssociation {

    private BbieId bbieId;

    private String guid;

    private BccManifestId basedBccManifestId;

    private String path;

    private String hashPath;

    private AbieId fromAbieId;

    private BbiepId toBbiepId;

    private XbtManifestId xbtManifestId;

    private CodeListManifestId codeListManifestId;

    private AgencyIdListManifestId agencyIdListManifestId;

    private String defaultValue;

    private String fixedValue;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;

    private int cardinalityMin;

    private int cardinalityMax;

    private boolean nillable;

    private String definition;

    private String remark;

    private String example;

    private boolean deprecated;

    private boolean used;

    private TopLevelAsbiepId ownerTopLevelAsbiepId;

    public BbieId getBbieId() {
        return bbieId;
    }

    public void setBbieId(BbieId bbieId) {
        this.bbieId = bbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BccManifestId getBasedBccManifestId() {
        return basedBccManifestId;
    }

    public void setBasedBccManifestId(BccManifestId basedBccManifestId) {
        this.basedBccManifestId = basedBccManifestId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHashPath() {
        return hashPath;
    }

    public void setHashPath(String hashPath) {
        this.hashPath = hashPath;
    }

    public AbieId getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(AbieId fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public BbiepId getToBbiepId() {
        return toBbiepId;
    }

    public void setToBbiepId(BbiepId toBbiepId) {
        this.toBbiepId = toBbiepId;
    }

    public XbtManifestId getXbtManifestId() {
        return xbtManifestId;
    }

    public void setXbtManifestId(XbtManifestId xbtManifestId) {
        this.xbtManifestId = xbtManifestId;
    }

    public CodeListManifestId getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(CodeListManifestId codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public AgencyIdListManifestId getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(AgencyIdListManifestId agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
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

    public BigInteger getFacetMinLength() {
        return facetMinLength;
    }

    public void setFacetMinLength(BigInteger facetMinLength) {
        this.facetMinLength = facetMinLength;
    }

    public BigInteger getFacetMaxLength() {
        return facetMaxLength;
    }

    public void setFacetMaxLength(BigInteger facetMaxLength) {
        this.facetMaxLength = facetMaxLength;
    }

    public String getFacetPattern() {
        return facetPattern;
    }

    public void setFacetPattern(String facetPattern) {
        this.facetPattern = facetPattern;
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

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public TopLevelAsbiepId getOwnerTopLevelAsbiepId() {
        return ownerTopLevelAsbiepId;
    }

    public void setOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
    }

    @Override
    public boolean isAsbie() {
        return false;
    }

    @Override
    public boolean isBbie() {
        return true;
    }
}
