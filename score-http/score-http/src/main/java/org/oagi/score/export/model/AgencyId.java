package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AgencyIdListRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AgencyIdListValueRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AgencyId implements Component {

    private AgencyIdListRecord agencyIdList;

    private List<AgencyIdListValueRecord> agencyIdListValues;

    public AgencyId(AgencyIdListRecord agencyIdList, List<AgencyIdListValueRecord> agencyIdListValues) {
        this.agencyIdList = agencyIdList;
        this.agencyIdListValues = agencyIdListValues;
    }

    @Override
    public String getName() {
        return agencyIdList.getName();
    }

    @Override
    public String getGuid() {
        return GUID_PREFIX + agencyIdList.getGuid();
    }

    public String getEnumGuid() {
        return agencyIdList.getEnumTypeGuid();
    }

    @Override
    public String getTypeName() {
        return agencyIdList.getName() + "ContentType";
    }

    public int getMinLengthOfValues() {
        return agencyIdListValues.stream()
                .map(AgencyIdListValueRecord::getValue)
                .min((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public int getMaxLengthOfValues() {
        return agencyIdListValues.stream()
                .map(AgencyIdListValueRecord::getValue)
                .max((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public Collection<AgencyIdListValueRecord> getValues() {
        return new ArrayList<>(agencyIdListValues);
    }

    public String getDefinition() {
        return agencyIdList.getDefinition();
    }

    public String getDefinitionSource() {
        return null;
    }

    public ULong getNamespaceId() {
        return agencyIdList.getNamespaceId();
    }

    public ULong getTypeNamespaceId() {
        return this.getNamespaceId();
    }

}
