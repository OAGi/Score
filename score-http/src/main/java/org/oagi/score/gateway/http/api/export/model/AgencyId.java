package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.ArrayList;
import java.util.Collection;

public class AgencyId implements Component {

    private AgencyIdListSummaryRecord agencyIdList;

    public AgencyId(AgencyIdListSummaryRecord agencyIdList) {
        this.agencyIdList = agencyIdList;
    }

    @Override
    public String getName() {
        return agencyIdList.name();
    }

    @Override
    public String getGuid() {
        return agencyIdList.guid().value();
    }

    public String getEnumGuid() {
        return agencyIdList.enumTypeGuid();
    }

    @Override
    public String getTypeName() {
        return agencyIdList.name().replaceAll(" ", "").replace("Identifier", "ID") + "ContentType";
    }

    public int getMinLengthOfValues() {
        return agencyIdList.valueList().stream()
                .map(AgencyIdListValueSummaryRecord::value)
                .min((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public int getMaxLengthOfValues() {
        return agencyIdList.valueList().stream()
                .map(AgencyIdListValueSummaryRecord::value)
                .max((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public Collection<AgencyIdListValueSummaryRecord> getValues() {
        return new ArrayList<>(agencyIdList.valueList());
    }

    public String getDefinition() {
        return (agencyIdList.definition() != null) ? agencyIdList.definition().content() : null;
    }

    public String getDefinitionSource() {
        return (agencyIdList.definition() != null) ? agencyIdList.definition().source() : null;
    }

    public NamespaceId getNamespaceId() {
        return agencyIdList.namespaceId();
    }

    public NamespaceId getTypeNamespaceId() {
        return this.getNamespaceId();
    }

}
