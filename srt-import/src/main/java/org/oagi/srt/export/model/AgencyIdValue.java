package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.AgencyIdListValue;

public class AgencyIdValue {

    private AgencyIdListValue agencyIdListValue;

    public AgencyIdValue(AgencyIdListValue agencyIdListValue) {
        this.agencyIdListValue = agencyIdListValue;
    }

    public String getValue() {
        return agencyIdListValue.getValue();
    }

    public String getName() {
        return agencyIdListValue.getName();
    }

    public String getDefinition() {
        return agencyIdListValue.getDefinition();
    }
}
