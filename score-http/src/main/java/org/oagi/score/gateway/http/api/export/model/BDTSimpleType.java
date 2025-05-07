package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleType extends AbstractBDTSimple {

    private DtSummaryRecord dataType;

    private DtSummaryRecord baseDataType;

    private boolean isDefaultBDT;

    private List<DtAwdPriSummaryRecord> dtAwdPriRecordList;

    private List<XbtSummaryRecord> xbtList;

    private CcDocument ccDocument;

    public BDTSimpleType(DtSummaryRecord dataType,
                         DtSummaryRecord baseDataType,
                         boolean isDefaultBDT, CcDocument ccDocument) {
        this(dataType, baseDataType, isDefaultBDT, null, null, ccDocument);
    }

    public BDTSimpleType(DtSummaryRecord dataType,
                         DtSummaryRecord baseDataType,
                         boolean isDefaultBDT,
                         List<DtAwdPriSummaryRecord> dtAwdPriRecordList,
                         List<XbtSummaryRecord> xbtList,
                         CcDocument ccDocument) {
        super(ccDocument);

        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.dtAwdPriRecordList = (dtAwdPriRecordList != null) ? dtAwdPriRecordList : Collections.emptyList();
        this.xbtList = (xbtList != null) ? xbtList : Collections.emptyList();
        this.ccDocument = ccDocument;
    }

    public DtManifestId getBdtManifestId() {
        return this.dataType.dtManifestId();
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
        return this.dataType.namespaceId();
    }

    public boolean isTimepointCDT() {
        String dataTypeTerm = dataType.dataTypeTerm();
        return (dataTypeTerm.contains("Date") || dataTypeTerm.contains("Time") || dataTypeTerm.contains("Duration"));
    }

    public boolean isBaseDT_CDT() {
        return baseDataType.basedDtManifestId() == null;
    }

    public long count_BDT_PRI_RESTRI() {
        return this.dtAwdPriRecordList.stream().filter(e -> e.xbtManifestId() != null).count();
    }

    public List<String> getXbtBuiltInTypes() {
        return xbtList.stream()
                .map(e -> e.builtInType())
                .collect(Collectors.toList());
    }
}
