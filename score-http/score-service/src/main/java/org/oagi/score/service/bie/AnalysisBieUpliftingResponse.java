package org.oagi.score.service.bie;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.Asbie;
import org.oagi.score.repo.api.bie.model.Bbie;
import org.oagi.score.repo.api.bie.model.BbieSc;
import org.oagi.score.repo.api.corecomponent.model.AsccManifest;
import org.oagi.score.repo.api.corecomponent.model.BccManifest;
import org.oagi.score.repo.api.corecomponent.model.DtScManifest;

import java.math.BigInteger;
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

    private Map<BigInteger, BieContextPath> sourceAsbiePathMap = new HashMap();
    private Map<BigInteger, BieContextPath> targetAsbiePathMap = new HashMap();

    private Map<BigInteger, BieContextPath> sourceBbiePathMap = new HashMap();
    private Map<BigInteger, BieContextPath> targetBbiePathMap = new HashMap();

    private Map<BigInteger, BieContextPath> sourceBbieScPathMap = new HashMap();
    private Map<BigInteger, BieContextPath> targetBbieScPathMap = new HashMap();

    @Override
    public void notFoundMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath, String sourceContextDefinition) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(sourceAsccPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath, String sourceContextDefinition,
                                      AsccManifest targetAsccManifest, String targetAsccPath) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(sourceAsccPath, sourceContextDefinition));
        targetAsbiePathMap.put(asbie.getAsbieId(), new BieContextPath(targetAsccPath, sourceContextDefinition));
    }

    @Override
    public void notFoundMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath, String sourceContextDefinition) {
        sourceBbiePathMap.put(bbie.getBbieId(), new BieContextPath(sourceBccPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath, String sourceContextDefinition,
                                     BccManifest targetBccManifest, String targetBccPath) {
        sourceBbiePathMap.put(bbie.getBbieId(), new BieContextPath(sourceBccPath, sourceContextDefinition));
        targetBbiePathMap.put(bbie.getBbieId(), new BieContextPath(targetBccPath, sourceContextDefinition));
    }

    @Override
    public void notFoundMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceDtScManifest, String sourceDtScPath, String sourceContextDefinition) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(sourceDtScPath, sourceContextDefinition));
    }

    @Override
    public void foundBestMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceAsccManifest, String sourceDtScPath, String sourceContextDefinition,
                                       DtScManifest targetDtScManifest, String targetDtScPath) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(sourceDtScPath, sourceContextDefinition));
        targetBbieScPathMap.put(bbieSc.getBbieScId(), new BieContextPath(targetDtScPath, sourceContextDefinition));
    }
}
