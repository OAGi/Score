package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;

public interface BieUpliftingListener {

    void notFoundMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition);

    void foundBestMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition,
                               AsccSummaryRecord targetAscc, String targetAsccPath);

    void notFoundMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition);

    void foundBestMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition,
                              BccSummaryRecord targetBcc, String targetBccPath);

    void notFoundMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceDtSc, String sourceDtScPath, String sourceContextDefinition);

    void foundBestMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceAscc, String sourceDtScPath, String sourceContextDefinition,
                                DtScSummaryRecord targetDtSc, String targetDtScPath);

}
