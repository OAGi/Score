package org.oagi.srt.export.model;

import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSC implements Component {

    private DataTypeSupplementaryComponent dtSc;

    private ImportedDataProvider importedDataProvider;

    public BDTSC(DataTypeSupplementaryComponent dtSc,
                 ImportedDataProvider importedDataProvider) {
        this.importedDataProvider = importedDataProvider;
        this.dtSc = dtSc;
    }

    public String getName() {
        String propertyTerm = dtSc.getPropertyTerm();
        if ("MIME".equals(propertyTerm)) {
            propertyTerm = propertyTerm.toLowerCase();
        }
        String representationTerm = dtSc.getRepresentationTerm();
        if (propertyTerm.equals(representationTerm) ||
            "Text".equals(representationTerm)) { // exceptional case. 'expressionLanguageText' must be 'expressionLanguage'.
            representationTerm = "";
        }

        if (propertyTerm.contains(representationTerm)) {
            String attrName = Character.toLowerCase(propertyTerm.charAt(0)) + propertyTerm.substring(1);
            return attrName.replaceAll(" ", "");
        } else {
            String attrName = Character.toLowerCase(propertyTerm.charAt(0)) + propertyTerm.substring(1) +
                    representationTerm.replace("Identifier", "ID");
            return attrName.replaceAll(" ", "");
        }
    }

    public String getGuid() {
        return dtSc.getGuid();
    }

    @Override
    public String getTypeName() {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList =
                importedDataProvider.findBdtScPriRestriListByDtScId(dtSc.getDtScId());

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> codeListBdtScPriRestri =
                bdtScPriRestriList.stream()
                        .filter(e -> e.getCodeListId() > 0)
                        .collect(Collectors.toList());
        if (codeListBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (codeListBdtScPriRestri.isEmpty()) {
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> agencyIdBdtScPriRestri =
                    bdtScPriRestriList.stream()
                            .filter(e -> e.getAgencyIdListId() > 0)
                            .collect(Collectors.toList());
            if (agencyIdBdtScPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (agencyIdBdtScPriRestri.isEmpty()) {
                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> defaultBdtScPriRestri =
                        bdtScPriRestriList.stream()
                                .filter(e -> e.isDefault())
                                .collect(Collectors.toList());
                if (defaultBdtScPriRestri.isEmpty() || defaultBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        importedDataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = importedDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                return xbt.getBuiltInType();
            } else {
                AgencyIdList agencyIdList = importedDataProvider.findAgencyIdList(agencyIdBdtScPriRestri.get(0).getAgencyIdListId());
                return agencyIdList.getName() + "ContentType";
            }
        } else {
            CodeList codeList = importedDataProvider.findCodeList(codeListBdtScPriRestri.get(0).getCodeListId());
            return codeList.getName() + "ContentType";
        }
    }

    public int getMinCardinality() {
        return dtSc.getMinCardinality();
    }

    public int getMaxCardinality() {
        return dtSc.getMaxCardinality();
    }

    public boolean hasBasedBDTSC() {
        return (dtSc.getBasedDtScId() > 0);
    }
}
