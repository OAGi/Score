package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

public interface BDTSimple {

    DtManifestId getBdtManifestId();

    DtId getBdtId();

    boolean isDefaultBDT();

    String getName();

    String getGuid();

    String getBaseDTName();


    DtSummaryRecord getDataType();

    DtSummaryRecord getBaseDataType();

    DtAwdPriSummaryRecord getDefaultDtAwdPri();

    XbtSummaryRecord getDefaultXbtManifest();

    String getCdtPriName();

}
