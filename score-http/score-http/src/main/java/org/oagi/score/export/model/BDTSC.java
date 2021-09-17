package org.oagi.score.export.model;

import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.OAGIS_VERSION;

public class BDTSC implements Component {

    private DtScRecord dtSc;

    private ImportedDataProvider importedDataProvider;

    public BDTSC(DtScRecord dtSc,
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
        return GUID_PREFIX + dtSc.getGuid();
    }

    public DtScRecord getBdtSc() {
        return dtSc;
    }

    private String typeName;
    private CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMap;
    private XbtRecord xbt;
    private AgencyIdListRecord agencyIdList;
    private CodeListRecord codeList;

    public XbtRecord getXbt() {
        ensureTypeName();
        return xbt;
    }

    public CdtScAwdPriRecord getCdtScAwdPri() {
        ensureTypeName();
        return importedDataProvider.findCdtScAwdPri(
                cdtScAwdPriXpsTypeMap.getCdtScAwdPriId()
        );
    }

    public CdtPriRecord getCdtPri() {
        CdtScAwdPriRecord cdtScAwdPri = getCdtScAwdPri();
        return importedDataProvider.findCdtPri(cdtScAwdPri.getCdtPriId());
    }

    public AgencyIdListRecord getAgencyIdList() {
        ensureTypeName();
        return agencyIdList;
    }

    public CodeListRecord getCodeList() {
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

        List<BdtScPriRestriRecord> bdtScPriRestriList =
                importedDataProvider.findBdtScPriRestriListByDtScId(dtSc.getDtScId());

        List<BdtScPriRestriRecord> codeListBdtScPriRestri =
                bdtScPriRestriList.stream()
                        .filter(e -> e.getCodeListId() != null)
                        .collect(Collectors.toList());
        if (codeListBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (codeListBdtScPriRestri.isEmpty()) {
            List<BdtScPriRestriRecord> agencyIdBdtScPriRestri =
                    bdtScPriRestriList.stream()
                            .filter(e -> e.getAgencyIdListId() != null)
                            .collect(Collectors.toList());
            if (agencyIdBdtScPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (agencyIdBdtScPriRestri.isEmpty()) {
                List<BdtScPriRestriRecord> defaultBdtScPriRestri =
                        bdtScPriRestriList.stream()
                                .filter(e -> e.getIsDefault() == 1)
                                .collect(Collectors.toList());
                if (defaultBdtScPriRestri.isEmpty() || defaultBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                cdtScAwdPriXpsTypeMap =
                        importedDataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                xbt = importedDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                typeName = xbt.getBuiltinType();
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
        return (dtSc.getBasedDtScId() != null);
    }

    public String getDefinition() {
        return dtSc.getDefinition();
    }

    public String getDefinitionSource() {
        return dtSc.getDefinitionSource();
    }
}
