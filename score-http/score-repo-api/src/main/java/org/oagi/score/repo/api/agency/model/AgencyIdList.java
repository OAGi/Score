package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.corecomponent.model.CoreComponent;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class AgencyIdList extends Auditable implements CoreComponent, Serializable {

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    private String releaseNum;

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    private BigInteger releaseId;

    private String revisionNum;

    public String getRevisionNum() {
        return revisionNum;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String modulePath;

    public void setRevisionNum(String revisionNum) {
        this.revisionNum = revisionNum;
    }

    private BigInteger agencyIdListManifestId;

    private BigInteger agencyIdListId;

    private String guid;

    private String enumTypeGuid;

    private String name;

    private String listId;

    private BigInteger agencyIdListValueId;

    public BigInteger getAgencyIdListValueManifestId() {
        return agencyIdListValueManifestId;
    }

    public void setAgencyIdListValueManifestId(BigInteger agencyIdListValueManifestId) {
        this.agencyIdListValueManifestId = agencyIdListValueManifestId;
    }

    private BigInteger agencyIdListValueManifestId;

    private String versionId;

    private BigInteger basedAgencyIdListId;

    private String definition;

    private String definitionSource;

    private String remark;

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private BigInteger namespaceId;

    private ScoreUser owner;

    private CcState state;

    private boolean deprecated;

    private BigInteger prevAgencyIdListId;

    private BigInteger nextAgencyIdListId;

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    private String access;

    public AgencyIdList getPrev() {
        return prev;
    }

    public void setPrev(AgencyIdList prev) {
        this.prev = prev;
    }

    private AgencyIdList prev;

    public List<AgencyIdListValue> getValues() {
        return values;
    }

    public void setValues(List<AgencyIdListValue> values) {
        this.values = values;
    }

    private List<AgencyIdListValue> values;

    public BigInteger getAgencyIdListId() {
        return agencyIdListId;
    }

    public void setAgencyIdListId(BigInteger agencyIdListId) {
        this.agencyIdListId = agencyIdListId;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public BigInteger getId() {
        return agencyIdListId;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEnumTypeGuid() {
        return enumTypeGuid;
    }

    public void setEnumTypeGuid(String enumTypeGuid) {
        this.enumTypeGuid = enumTypeGuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public BigInteger getAgencyIdListValueId() {
        return agencyIdListValueId;
    }

    public void setAgencyIdListValueId(BigInteger agencyIdListValueId) {
        this.agencyIdListValueId = agencyIdListValueId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public BigInteger getBasedAgencyIdListId() {
        return basedAgencyIdListId;
    }

    public void setBasedAgencyIdListId(BigInteger basedAgencyIdListId) {
        this.basedAgencyIdListId = basedAgencyIdListId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public BigInteger getPrevAgencyIdListId() {
        return prevAgencyIdListId;
    }

    public void setPrevAgencyIdListId(BigInteger prevAgencyIdListId) {
        this.prevAgencyIdListId = prevAgencyIdListId;
    }

    public BigInteger getNextAgencyIdListId() {
        return nextAgencyIdListId;
    }

    public void setNextAgencyIdListId(BigInteger nextAgencyIdListId) {
        this.nextAgencyIdListId = nextAgencyIdListId;
    }

    private String agencyIdListValueName;

    public String getAgencyIdListValueName() {
        return agencyIdListValueName;
    }

    public void setAgencyIdListValueName(String agencyIdListValueName) {
        this.agencyIdListValueName = agencyIdListValueName;
    }
}
