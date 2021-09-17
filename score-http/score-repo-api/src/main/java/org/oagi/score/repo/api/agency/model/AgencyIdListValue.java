package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.corecomponent.model.CoreComponent;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class AgencyIdListValue extends Auditable implements CoreComponent, Serializable {

    private BigInteger agencyIdListValueManifestId;

    private String guid;

    private String value;

    private String name;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    private String definition;

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    private String definitionSource;

    private BigInteger ownerAgencyIdListId;

    private ScoreUser owner;

    private boolean deprecated;

    private BigInteger prevAgencyIdListValueId;

    private BigInteger nextAgencyIdListValueId;

    @Override
    public BigInteger getId() {
        return agencyIdListValueManifestId;
    }

    public BigInteger getAgencyIdListValueManifestId() {
        return agencyIdListValueManifestId;
    }

    public void setAgencyIdListValueManifestId(BigInteger agencyIdListValueManifestId) {
        this.agencyIdListValueManifestId = agencyIdListValueManifestId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getOwnerAgencyIdListId() {
        return ownerAgencyIdListId;
    }

    public void setOwnerAgencyIdListId(BigInteger ownerAgencyIdListId) {
        this.ownerAgencyIdListId = ownerAgencyIdListId;
    }

    public ScoreUser getOwner() {
        return owner;
    }

    public void setOwner(ScoreUser owner) {
        this.owner = owner;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public BigInteger getPrevAgencyIdListValueId() {
        return prevAgencyIdListValueId;
    }

    public void setPrevAgencyIdListValueId(BigInteger prevAgencyIdListValueId) {
        this.prevAgencyIdListValueId = prevAgencyIdListValueId;
    }

    public BigInteger getNextAgencyIdListValueId() {
        return nextAgencyIdListValueId;
    }

    public void setNextAgencyIdListValueId(BigInteger nextAgencyIdListValueId) {
        this.nextAgencyIdListValueId = nextAgencyIdListValueId;
    }
}
