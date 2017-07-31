package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.entity.AgencyIdListValue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AgencyId implements Component {

    private AgencyIdList agencyIdList;

    private List<AgencyIdListValue> agencyIdListValues;

    public AgencyId(AgencyIdList agencyIdList, List<AgencyIdListValue> agencyIdListValues) {
        this.agencyIdList = agencyIdList;
        this.agencyIdListValues = agencyIdListValues;
    }

    @Override
    public String getName() {
        return agencyIdList.getName();
    }

    @Override
    public String getGuid() {
        return agencyIdList.getGuid();
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
                .map(AgencyIdListValue::getValue)
                .min((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public int getMaxLengthOfValues() {
        return agencyIdListValues.stream()
                .map(AgencyIdListValue::getValue)
                .max((o1, o2) -> o1.length() - o2.length())
                .get().length();
    }

    public Collection<AgencyIdValue> getValues() {
        return agencyIdListValues.stream()
                .map(e -> new AgencyIdValue(e)).collect(Collectors.toList());
    }

    public String getDefinition() {
        return agencyIdList.getDefinition();
    }

    public String getDefinitionSource() {
        return null;
    }
}
