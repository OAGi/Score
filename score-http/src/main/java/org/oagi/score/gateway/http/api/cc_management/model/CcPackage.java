package org.oagi.score.gateway.http.api.cc_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;

import java.util.Collection;

@Data
public class CcPackage {

    private Collection<AccDetailsRecord> accList;

    private Collection<AsccDetailsRecord> asccList;

    private Collection<BccDetailsRecord> bccList;

    private Collection<SeqKeySummaryRecord> sequenceList;

    private Collection<AsccpDetailsRecord> asccpList;

    private Collection<BccpDetailsRecord> bccpList;

    private Collection<DtDetailsRecord> dtList;

    private Collection<DtScDetailsRecord> dtScList;

    private Collection<DtAwdPriDetailsRecord> dtAwdPriList;

    private Collection<DtScAwdPriDetailsRecord> dtScAwdPriList;

}
