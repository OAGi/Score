package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.ScoreConstants.OAGIS_VERSION;

public class BDTSC implements Component {

    private DtScSummaryRecord dtSc;

    private DtSummaryRecord ownerDt;

    private CcDocument ccDocument;

    public BDTSC(DtScSummaryRecord dtSc, CcDocument ccDocument) {
        this.ccDocument = ccDocument;
        this.dtSc = dtSc;

        this.ownerDt = ccDocument.getDt(dtSc.ownerDtManifestId());
    }

    public String getName() {
        String propertyTerm = dtSc.propertyTerm();
        if ("MIME".equals(propertyTerm) || "URI".equals(propertyTerm)) {
            propertyTerm = propertyTerm.toLowerCase();
        }
        String representationTerm = dtSc.representationTerm();
        if (propertyTerm.equals(representationTerm) ||
                "Text".equals(representationTerm)) { // exceptional case. 'expressionLanguageText' must be 'expressionLanguage'.
            representationTerm = "";
        }
        if (OAGIS_VERSION < 10.3D) {
            // exceptional case. 'preferredIndicator' must be 'preferred'.
            if ("9bb9add40b5b415c8489b08bd4484907".equals(dtSc.getId().value())) {
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
        return dtSc.guid().value();
    }

    public DtScSummaryRecord getDtSc() {
        return dtSc;
    }

    private String typeName;
    private DtScAwdPriSummaryRecord dtScAwdPri;
    private XbtSummaryRecord xbt;
    private AgencyIdListSummaryRecord agencyIdList;
    private CodeListSummaryRecord codeList;
    private NamespaceId namespaceId;

    public XbtSummaryRecord getXbt() {
        ensureTypeName();
        return xbt;
    }

    public DtScAwdPriSummaryRecord getDtScAwdPri() {
        ensureTypeName();
        return dtScAwdPri;
    }

    public String getCdtPriName() {
        ensureTypeName();
        return (dtScAwdPri != null) ? dtScAwdPri.cdtPriName() : null;
    }

    public AgencyIdListSummaryRecord getAgencyIdList() {
        ensureTypeName();
        return agencyIdList;
    }

    public CodeListSummaryRecord getCodeList() {
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

        List<DtScAwdPriSummaryRecord> dtScAwdPriList =
                ccDocument.getDtScAwdPriList(dtSc.dtScManifestId());

        List<DtScAwdPriSummaryRecord> codeListBdtScPriRestri =
                dtScAwdPriList.stream()
                        .filter(e -> e.codeListManifestId() != null && e.isDefault())
                        .collect(Collectors.toList());
        if (codeListBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (codeListBdtScPriRestri.isEmpty()) {
            List<DtScAwdPriSummaryRecord> agencyIdBdtScPriRestri =
                    dtScAwdPriList.stream()
                            .filter(e -> e.agencyIdListManifestId() != null && e.isDefault())
                            .collect(Collectors.toList());
            if (agencyIdBdtScPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (agencyIdBdtScPriRestri.isEmpty()) {
                List<DtScAwdPriSummaryRecord> defaultBdtScPriRestri =
                        dtScAwdPriList.stream()
                                .filter(e -> e.isDefault())
                                .collect(Collectors.toList());
                if (defaultBdtScPriRestri.isEmpty() || defaultBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                dtScAwdPri = defaultBdtScPriRestri.get(0);
                xbt = ccDocument.getXbt(dtScAwdPri.xbtManifestId());
                typeName = xbt.builtInType();
                namespaceId = this.ownerDt.namespaceId();
            } else {
                agencyIdList = ccDocument.getAgencyIdList(agencyIdBdtScPriRestri.get(0).agencyIdListManifestId());
                typeName = agencyIdList.name().replaceAll(" ", "").replace("Identifier", "ID") + "ContentType";
                namespaceId = agencyIdList.namespaceId();
            }
        } else {
            codeList = ccDocument.getCodeList(codeListBdtScPriRestri.get(0).codeListManifestId());
            typeName = codeList.name().replaceAll(" ", "").replace("Identifier", "ID") + "ContentType";
            namespaceId = codeList.namespaceId();
        }
    }

    public int getMinCardinality() {
        return dtSc.cardinality().min();
    }

    public int getMaxCardinality() {
        return dtSc.cardinality().max();
    }

    public boolean hasBasedBDTSC() {
        return (dtSc.basedDtScManifestId() != null);
    }

    public String getDefinition() {
        return dtSc.definition().content();
    }

    public String getDefinitionSource() {
        return dtSc.definition().source();
    }

    public NamespaceId getNamespaceId() {
        ensureTypeName();
        return namespaceId;
    }

    public NamespaceId getTypeNamespaceId() {
        return this.getNamespaceId();
    }

}
