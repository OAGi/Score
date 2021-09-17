package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ModifyAgencyIdListValuesRepositoryRequest {

    private BigInteger agencyIdListManifestId;
    private String state;
    private List<AgencyIdListValue> agencyIdListValueList = new ArrayList();

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    private ScoreUser requester;

    public static class AgencyIdListValue {
        private String value;
        private String name;
        private String definition;

        public String getDefinitionSource() {
            return definitionSource;
        }

        public void setDefinitionSource(String definitionSource) {
            this.definitionSource = definitionSource;
        }

        private String definitionSource;

        private boolean deprecated;
        private boolean used;
        private boolean locked;
        private boolean extension;

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

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
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

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isExtension() {
            return extension;
        }

        public void setExtension(boolean extension) {
            this.extension = extension;
        }
    }

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void addAgencyIdListValue(AgencyIdListValue agencyIdListValue) {
        this.agencyIdListValueList.add(agencyIdListValue);
    }

    public void setAgencyIdListValueList(List<AgencyIdListValue> agencyIdListValueList) {
        this.agencyIdListValueList = agencyIdListValueList;
    }

    public List<AgencyIdListValue> getAgencyIdListValueList() {
        return agencyIdListValueList;
    }
}
