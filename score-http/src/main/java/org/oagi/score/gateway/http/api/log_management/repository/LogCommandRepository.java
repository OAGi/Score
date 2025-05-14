package org.oagi.score.gateway.http.api.log_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.common.model.Guid;

public interface LogCommandRepository {

    LogId create(AccDetailsRecord accDetails, LogAction logAction, String logHash);

    LogId create(AsccpDetailsRecord asccpDetails, LogAction logAction);

    LogId create(BccpDetailsRecord bccpDetails, LogAction logAction);

    LogId create(DtDetailsRecord dtDetails, LogAction logAction);

    LogId create(CodeListDetailsRecord codeListDetails, LogAction logAction);

    LogId create(AgencyIdListDetailsRecord agencyIdListDetails, LogAction logAction);

    void deleteByReference(Guid guid);

    LogId revertToStableStateByReference(Guid reference, CcType ccType);

}
