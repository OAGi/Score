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

    private final DtScSummaryRecord dtSc;

    private final DtSummaryRecord ownerDt;

    private final CcDocument ccDocument;
    private final SchemaNamingStrategy namingStrategy;

    public BDTSC(DtScSummaryRecord dtSc, CcDocument ccDocument) {
        this(dtSc, ccDocument, new XmlSchemaNamingStrategy());
    }

    public BDTSC(DtScSummaryRecord dtSc, CcDocument ccDocument, SchemaNamingStrategy namingStrategy) {
        this.ccDocument = ccDocument;
        this.dtSc = dtSc;
        this.namingStrategy = namingStrategy;
        this.ownerDt = ccDocument.getDt(dtSc.ownerDtManifestId());
    }

    public String getName() {
        return namingStrategy.bdtScName(dtSc, ownerDt, ccDocument);
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

        List<DtScAwdPriSummaryRecord> agencyIdBdtScPriRestri =
                dtScAwdPriList.stream()
                        .filter(e -> e.agencyIdListManifestId() != null)
                        .collect(Collectors.toList());
        if (agencyIdBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (!agencyIdBdtScPriRestri.isEmpty()) {
            agencyIdList = ccDocument.getAgencyIdList(agencyIdBdtScPriRestri.get(0).agencyIdListManifestId());
            typeName = namingStrategy.agencyIdListTypeName(agencyIdList);
            namespaceId = agencyIdList.namespaceId();
        } else {
            List<DtScAwdPriSummaryRecord> codeListBdtScPriRestri =
                    dtScAwdPriList.stream()
                            .filter(e -> e.codeListManifestId() != null)
                            .collect(Collectors.toList());
            if (codeListBdtScPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (!codeListBdtScPriRestri.isEmpty()) {
                codeList = ccDocument.getCodeList(codeListBdtScPriRestri.get(0).codeListManifestId());
                typeName = namingStrategy.codeListTypeName(codeList);
                namespaceId = codeList.namespaceId();
            } else {
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
            }
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
