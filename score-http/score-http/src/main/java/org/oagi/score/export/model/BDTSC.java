package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repository.provider.DataProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.NS_XSD_PREFIX;
import static org.oagi.score.common.ScoreConstants.OAGIS_VERSION;

public class BDTSC implements Component {

    private DtScManifestRecord dtScManifest;

    private DtScRecord dtSc;

    private DtManifestRecord ownerDtManifest;

    private DtRecord ownerDt;

    private DataProvider dataProvider;

    public BDTSC(DtScManifestRecord dtScManifest, DtScRecord dtSc,
                 DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.dtScManifest = dtScManifest;
        this.dtSc = dtSc;

        this.ownerDtManifest = this.dataProvider.findDtManifestByDtManifestId(dtScManifest.getOwnerDtManifestId());
        this.ownerDt = this.dataProvider.findDT(this.ownerDtManifest.getDtId());
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

    public DtScManifestRecord getBdtScManifest() {
        return dtScManifest;
    }

    public DtScRecord getBdtSc() {
        return dtSc;
    }

    private String typeName;
    private CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMap;
    private XbtRecord xbt;
    private AgencyIdListRecord agencyIdList;
    private CodeListRecord codeList;
    private ULong namespaceId;

    public XbtRecord getXbt() {
        ensureTypeName();
        return xbt;
    }

    public CdtScAwdPriRecord getCdtScAwdPri() {
        ensureTypeName();
        return dataProvider.findCdtScAwdPri(
                cdtScAwdPriXpsTypeMap.getCdtScAwdPriId()
        );
    }

    public CdtPriRecord getCdtPri() {
        CdtScAwdPriRecord cdtScAwdPri = getCdtScAwdPri();
        return dataProvider.findCdtPri(cdtScAwdPri.getCdtPriId());
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
                dataProvider.findBdtScPriRestriListByDtScManifestId(dtScManifest.getDtScManifestId());

        List<BdtScPriRestriRecord> codeListBdtScPriRestri =
                bdtScPriRestriList.stream()
                        .filter(e -> e.getCodeListManifestId() != null)
                        .collect(Collectors.toList());
        if (codeListBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (codeListBdtScPriRestri.isEmpty()) {
            List<BdtScPriRestriRecord> agencyIdBdtScPriRestri =
                    bdtScPriRestriList.stream()
                            .filter(e -> e.getAgencyIdListManifestId() != null)
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
                        dataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                xbt = dataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                typeName = xbt.getBuiltinType();
                // TODO:
                // namespaceId = xbt.getNamespaceId();
                if (!typeName.startsWith(NS_XSD_PREFIX)) {
                    namespaceId = this.ownerDt.getNamespaceId();
                }
            } else {
                AgencyIdListManifestRecord agencyIdListManifest = dataProvider.findAgencyIdListManifest(agencyIdBdtScPriRestri.get(0).getAgencyIdListManifestId());
                agencyIdList = dataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                typeName = agencyIdList.getName() + "ContentType";
                namespaceId = agencyIdList.getNamespaceId();
            }
        } else {
            CodeListManifestRecord codeListManifest = dataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
            codeList = dataProvider.findCodeList(codeListManifest.getCodeListId());
            typeName = codeList.getName() + "ContentType";
            namespaceId = codeList.getNamespaceId();
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

    public ULong getNamespaceId() {
        ensureTypeName();
        return namespaceId;
    }
}
