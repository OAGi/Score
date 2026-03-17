package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class AgencyId implements Component {

    private AgencyIdListSummaryRecord agencyIdList;

    private Function<AgencyIdListSummaryRecord, String> nameResolver;

    public AgencyId(AgencyIdListSummaryRecord agencyIdList,
                    Function<AgencyIdListSummaryRecord, String> nameResolver) {
        this.agencyIdList = agencyIdList;
        this.nameResolver = nameResolver;
    }

    @Override
    public String getName() {
        return nameResolver.apply(agencyIdList);
    }

    @Override
    public String getGuid() {
        return agencyIdList.guid().value();
    }

    public String getEnumGuid() {
        return agencyIdList.enumTypeGuid();
    }

    public AgencyIdListManifestId agencyIdListManifestId() {
        return agencyIdList.agencyIdListManifestId();
    }

    @Override
    public String getTypeName() {
        return getName() + "ContentType";
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
