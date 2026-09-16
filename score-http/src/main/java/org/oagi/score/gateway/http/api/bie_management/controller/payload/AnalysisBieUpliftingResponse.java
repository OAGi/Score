package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingListener;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class AnalysisBieUpliftingResponse implements BieUpliftingListener {

    @Data
    private static class BieContextPath {
        private String path;
        private String context;

        public BieContextPath(String path, String context) {
            this.path = path;
            this.context = context;
        }
    }

    @Data
    private static class BiePathMapping {
        private BigInteger bieId;
        private BieContextPath source;
        private BieContextPath target;

        public BiePathMapping(BigInteger bieId, BieContextPath source, BieContextPath target) {
            this.bieId = bieId;
            this.source = source;
            this.target = target;
        }
    }

    // A reused BIE can occur multiple times while retaining the same BIE IDs.
    // Keep every occurrence in traversal order instead of collapsing mappings by ID.
    private List<BiePathMapping> asbiePathList = new ArrayList<>();
    private List<BiePathMapping> bbiePathList = new ArrayList<>();
    private List<BiePathMapping> bbieScPathList = new ArrayList<>();

    @Override
    public void notFoundMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition) {
        BieContextPath source = new BieContextPath(sourceAsccPath, sourceContextDefinition);
        asbiePathList.add(new BiePathMapping(asbie.getAsbieId().value(), source, null));
    }

    @Override
    public void foundBestMatchedAsbie(Asbie asbie, AsccSummaryRecord sourceAscc, String sourceAsccPath, String sourceContextDefinition,
                                      AsccSummaryRecord targetAscc, String targetAsccPath) {
        BieContextPath source = new BieContextPath(sourceAsccPath, sourceContextDefinition);
        BieContextPath target = new BieContextPath(targetAsccPath, sourceContextDefinition);
        asbiePathList.add(new BiePathMapping(asbie.getAsbieId().value(), source, target));
    }

    @Override
    public void notFoundMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition) {
        BieContextPath source = new BieContextPath(sourceBccPath, sourceContextDefinition);
        bbiePathList.add(new BiePathMapping(bbie.getBbieId().value(), source, null));
    }

    @Override
    public void foundBestMatchedBbie(Bbie bbie, BccSummaryRecord sourceBcc, String sourceBccPath, String sourceContextDefinition,
                                     BccSummaryRecord targetBcc, String targetBccPath) {
        BieContextPath source = new BieContextPath(sourceBccPath, sourceContextDefinition);
        BieContextPath target = new BieContextPath(targetBccPath, sourceContextDefinition);
        bbiePathList.add(new BiePathMapping(bbie.getBbieId().value(), source, target));
    }

    @Override
    public void notFoundMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceDtSc, String sourceDtScPath, String sourceContextDefinition) {
        BieContextPath source = new BieContextPath(sourceDtScPath, sourceContextDefinition);
        bbieScPathList.add(new BiePathMapping(bbieSc.getBbieScId().value(), source, null));
    }

    @Override
    public void foundBestMatchedBbieSc(BbieSc bbieSc, DtScSummaryRecord sourceAscc, String sourceDtScPath, String sourceContextDefinition,
                                       DtScSummaryRecord targetDtSc, String targetDtScPath) {
        BieContextPath source = new BieContextPath(sourceDtScPath, sourceContextDefinition);
        BieContextPath target = new BieContextPath(targetDtScPath, sourceContextDefinition);
        bbieScPathList.add(new BiePathMapping(bbieSc.getBbieScId().value(), source, target));
    }
}
