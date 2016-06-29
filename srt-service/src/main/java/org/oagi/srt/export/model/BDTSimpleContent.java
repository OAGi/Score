package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleContent implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private List<BDTSC> dtScList;

    private List<BDTSC> baseDtScList;

    public BDTSimpleContent(DataType dataType, DataType baseDataType,
                            List<DataTypeSupplementaryComponent> dtScList,
                            List<DataTypeSupplementaryComponent> baseDtScList) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.dtScList = map(dtScList);
        this.baseDtScList = map(baseDtScList);
    }

    private List<BDTSC> map(List<DataTypeSupplementaryComponent> dtScList) {
        return dtScList.stream()
                .map(dtSc -> new BDTSC(
                        dtSc.getDtScId(),
                        dtSc.getGuid(),
                        getName(dtSc),
                        dtSc.getMinCardinality(),
                        dtSc.getMaxCardinality(),
                        (dtSc.getBasedDtScId() > 0) ? true : false))
                .collect(Collectors.toList());
    }

    private String getName(DataTypeSupplementaryComponent dtSc) {
        String propertyTerm = dtSc.getPropertyTerm();
        if ("MIME".equals(propertyTerm)) {
            propertyTerm = propertyTerm.toLowerCase();
        }
        String representationTerm = dtSc.getRepresentationTerm();
        if (propertyTerm.equals(representationTerm)) {
            representationTerm = "";
        }

        String attrName = Character.toLowerCase(propertyTerm.charAt(0)) + propertyTerm.substring(1) + representationTerm;
        return attrName.replaceAll(" ", "");
    }

    @Override
    public int getBdtId() {
        return dataType.getDtId();
    }

    public String getName() {
        return Utility.denToName(dataType.getDen());
    }

    public String getGuid() {
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return Utility.denToName(baseDataType.getDen());
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }

    public List<BDTSC> getBaseDtScList() {
        return baseDtScList;
    }
}
