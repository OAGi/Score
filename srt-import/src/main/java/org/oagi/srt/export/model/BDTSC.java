package org.oagi.srt.export.model;

import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;

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
        if ("MIME".equals(propertyTerm) || "URI".equals(propertyTerm)) {
            propertyTerm = propertyTerm.toLowerCase();
        }
        String representationTerm = dtSc.getRepresentationTerm();
        if (propertyTerm.equals(representationTerm) ||
            "Text".equals(representationTerm)) { // exceptional case. 'expressionLanguageText' must be 'expressionLanguage'.
            representationTerm = "";
        }
        if (OAGIS_VERSION < 10.3D) {
            // exceptional case. 'preferredIndicator' must be 'preferred'.
            if ("oagis-id-9bb9add40b5b415c8489b08bd4484907".equals(dtSc.getGuid())) {
                representationTerm = "";
            }
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

    public DataTypeSupplementaryComponent getBdtSc() {
        return dtSc;
    }

    private String typeName;
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap;
    private XSDBuiltInType xbt;
    private AgencyIdList agencyIdList;
    private CodeList codeList;

    public XSDBuiltInType getXbt() {
        ensureTypeName();
        return xbt;
    }

    public CoreDataTypeSupplementaryComponentAllowedPrimitive getCdtScAwdPri() {
        ensureTypeName();
        return importedDataProvider.findCdtScAwdPri(
                cdtScAwdPriXpsTypeMap.getCdtScAwdPriId()
        );
    }

    public CoreDataTypePrimitive getCdtPri() {
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri = getCdtScAwdPri();
        return importedDataProvider.findCdtPri(cdtScAwdPri.getCdtPriId());
    }

    public AgencyIdList getAgencyIdList() {
        ensureTypeName();
        return agencyIdList;
    }

    public CodeList getCodeList() {
        ensureTypeName();
        return codeList;
    }

    @Override
    public String getTypeName() {
        ensureTypeName();
        return typeName;
    }

    private void ensureTypeName() {
        if (typeName != null) {
            return;
        }

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

                cdtScAwdPriXpsTypeMap =
                        importedDataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                xbt = importedDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                typeName = xbt.getBuiltInType();
            } else {
                agencyIdList = importedDataProvider.findAgencyIdList(agencyIdBdtScPriRestri.get(0).getAgencyIdListId());
                typeName = agencyIdList.getName() + "ContentType";
            }
        } else {
            codeList = importedDataProvider.findCodeList(codeListBdtScPriRestri.get(0).getCodeListId());
            typeName = codeList.getName() + "ContentType";
        }
    }

    public int getMinCardinality() {
        return dtSc.getCardinalityMin();
    }

    public int getMaxCardinality() {
        return dtSc.getCardinalityMax();
    }

    public boolean hasBasedBDTSC() {
        return (dtSc.getBasedDtScId() > 0);
    }

    public String getDefinition() {
        return dtSc.getDefinition();
    }

    public String getDefinitionSource() {
        return dtSc.getDefinitionSource();
    }
}
