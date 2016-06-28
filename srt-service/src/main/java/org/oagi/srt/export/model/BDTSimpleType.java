package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.DataType;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleType implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    private CodeListRepository codeListRepository;

    private AgencyIdListRepository agencyIdListRepository;

    public BDTSimpleType(DataType dataType, DataType baseDataType,
                         BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository,
                         CodeListRepository codeListRepository,
                         AgencyIdListRepository agencyIdListRepository) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;

        this.bdtPriRestriRepository = bdtPriRestriRepository;
        this.codeListRepository = codeListRepository;
        this.agencyIdListRepository = agencyIdListRepository;
    }

    public String getName() {
        return denToName(dataType.getDen());
    }

    public String getGuid() {
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return denToName(baseDataType.getDen());
    }

    public String getCodeListName() {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByBdtId(this.dataType.getDtId()).stream()
                        .filter(e -> e.getCodeListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        CodeList codeList = codeListRepository.findOne(bdtPriRestriList.get(0).getCodeListId());
        return codeList.getName();
    }

    public String getAgencyIdName() {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByBdtId(this.dataType.getDtId()).stream()
                        .filter(e -> e.getAgencyIdListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }

        AgencyIdList agencyIdList = agencyIdListRepository.findOne(bdtPriRestriList.get(0).getAgencyIdListId());
        if ("oagis-id-f1df540ef0db48318f3a423b3057955f".equals(agencyIdList.getGuid())) {
            return "clm63055D08B_AgencyIdentification";
        } else {
            throw new IllegalStateException();
        }
    }

    private String denToName(String den) {
        String name = Utility.denToTypeName(den);
        if (name.contains("Type_ ")) {
            name = name.replace("Type_ ", "") + "Type";
        }
        return name.replaceAll(" ", "").replaceAll("_", "");
    }
}
