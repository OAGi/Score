package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;

import java.util.function.Function;

public interface SchemaNamingStrategy {

    String accName(AccSummaryRecord acc);

    default String accTypeName(AccSummaryRecord acc) {
        return accName(acc) + "Type";
    }

    String asccpName(AsccpSummaryRecord asccp);

    String asccpTypeName(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc);

    String bccpName(BccpSummaryRecord bccp);

    default String bdtScName(DtScSummaryRecord dtSc) {
        return bdtScName(dtSc, null, null);
    }

    default String bdtScName(DtScSummaryRecord dtSc, DtSummaryRecord ownerDt) {
        return bdtScName(dtSc, ownerDt, null);
    }

    String bdtScName(DtScSummaryRecord dtSc, DtSummaryRecord ownerDt, CcDocument ccDocument);

    String dtName(DtSummaryRecord dt);

    String agencyIdListName(AgencyIdListSummaryRecord agencyIdList);

    String codeListName(CodeListSummaryRecord codeList);

    default String agencyIdListTypeName(AgencyIdListSummaryRecord agencyIdList) {
        return agencyIdListName(agencyIdList) + "ContentType";
    }

    default String codeListTypeName(CodeListSummaryRecord codeList) {
        return codeListName(codeList) + "ContentType";
    }

    default Function<DtSummaryRecord, String> dtNameResolver() {
        return this::dtName;
    }

    default Function<AgencyIdListSummaryRecord, String> agencyIdListNameResolver() {
        return this::agencyIdListName;
    }

    default Function<CodeListSummaryRecord, String> codeListNameResolver() {
        return this::codeListName;
    }
}
