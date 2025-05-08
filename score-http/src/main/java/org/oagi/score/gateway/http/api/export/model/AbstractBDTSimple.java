package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBDTSimple implements BDTSimple {

    private CcDocument ccDocument;

    public AbstractBDTSimple(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
    }

    public DtAwdPriSummaryRecord getDefaultDtAwdPri() {
        DtSummaryRecord dt = ccDocument.getDt(getBdtManifestId());
        List<DtAwdPriSummaryRecord> dtAwdPriList = ccDocument.getDtAwdPriList(dt.dtManifestId());

        List<DtAwdPriSummaryRecord> defaultBdtPriRestri = dtAwdPriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toList());
        if (defaultBdtPriRestri.size() != 1) {
            throw new IllegalStateException();
        }

        return defaultBdtPriRestri.get(0);
    }

    public XbtSummaryRecord getDefaultXbtManifest() {
        DtAwdPriSummaryRecord defaultBdtPriRestri = getDefaultDtAwdPri();
        return ccDocument.getXbt(defaultBdtPriRestri.xbtManifestId());
    }

    public String getCdtPriName() {
        DtAwdPriSummaryRecord dtAwdPri = getDefaultDtAwdPri();
        if (dtAwdPri == null) {
            return null;
        }
        return dtAwdPri.cdtPriName();
    }

    public String getXbtName() {
        XbtSummaryRecord xbt = getDefaultXbtManifest();
        if (xbt == null) {
            return null;
        }
        return xbt.builtInType();
    }

}
