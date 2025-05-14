package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BDTSimpleContent extends AbstractBDTSimple {

    private DtSummaryRecord dataType;

    private DtSummaryRecord baseDataType;

    private boolean isDefaultBDT;

    private List<BDTSC> dtScList;

    private CcDocument ccDocument;

    public BDTSimpleContent(DtSummaryRecord dataType,
                            DtSummaryRecord baseDataType,
                            boolean isDefaultBDT,
                            Map<DtScManifestId, DtScSummaryRecord> dtScMap,
                            CcDocument ccDocument) {
        super(ccDocument);

        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.ccDocument = ccDocument;
        this.dtScList = map(dtScMap);
    }

    private List<BDTSC> map(Map<DtScManifestId, DtScSummaryRecord> dtScMap) {
        return dtScMap.entrySet().stream()
                .map(entry -> new BDTSC(entry.getValue(), ccDocument))
                .collect(Collectors.toList());
    }

    public DtManifestId getBdtManifestId() {
        return dataType.dtManifestId();
    }

    @Override
    public DtId getBdtId() {
        return dataType.dtId();
    }

    @Override
    public boolean isDefaultBDT() {
        return isDefaultBDT;
    }

    @Override
    public DtSummaryRecord getDataType() {
        return dataType;
    }

    @Override
    public DtSummaryRecord getBaseDataType() {
        return baseDataType;
    }

    public String getName() {
        return ModelUtils.getTypeName(dataType);
    }

    public String getGuid() {
        return dataType.guid().value();
    }

    public String getBaseDTName() {
        return ModelUtils.getTypeName(baseDataType);
    }

    public NamespaceId getNamespaceId() {
        return dataType.namespaceId();
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
