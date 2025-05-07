package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingListener;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;

import java.util.HashMap;
import java.util.Map;

@Data
public class AnalysisBieUpliftingResponse implements BieUpliftingListener {

    @Data
    private class BieContextPath {
        private String path;
        private String context;

        public BieContextPath(String path, String context) {
            this.path = path;
            this.context = context;
        }
    }

    private Map<AsbieId, BieContextPath> sourceAsbiePathMap = new HashMap();
    private Map<AsbieId, BieContextPath> targetAsbiePathMap = new HashMap();

    private Map<BbieId, BieContextPath> sourceBbiePathMap = new HashMap();
    private Map<BbieId, BieContextPath> targetBbiePathMap = new HashMap();

    private Map<BbieScId, BieContextPath> sourceBbieScPathMap = new HashMap();
    private Map<BbieScId, BieContextPath> targetBbieScPathMap = new HashMap();

    @Override
    public void notFoundMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(sourceAsccPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition,
                                      AsccSummaryRecord targetAscc, String targetAsccPath) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(sourceAsccPath, sourceContextDefinition));
        targetAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(targetAsccPath, sourceContextDefinition));
    }

    @Override
    public void notFoundMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition) {
        sourceBbiePathMap.put(bbie.getBbieId(), new BieContextPath(sourceBccPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition,
                                     BccSummaryRecord targetBcc, String targetBccPath) {
        sourceBbiePathMap.put(bbie.getBbieId(), new BieContextPath(sourceBccPath, sourceContextDefinition));
        targetBbiePathMap.put(bbie.getBbieId(), new BieContextPath(targetBccPath, sourceContextDefinition));
    }

    @Override
    public void notFoundMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceDtSc, String sourceDtScPath, String sourceContextDefinition) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(sourceDtScPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceAscc, String sourceDtScPath, String sourceContextDefinition,
                                       DtScSummaryRecord targetDtSc, String targetDtScPath) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(sourceDtScPath, sourceContextDefinition));
        targetBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(targetDtScPath, sourceContextDefinition));
    }
}
