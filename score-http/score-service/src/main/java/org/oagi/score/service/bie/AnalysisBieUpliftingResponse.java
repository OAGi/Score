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

    private Map<BigInteger, String> sourceAsbiePathMap = new HashMap();
    private Map<BigInteger, String> targetAsbiePathMap = new HashMap();

    private Map<BigInteger, String> sourceBbiePathMap = new HashMap();
    private Map<BigInteger, String> targetBbiePathMap = new HashMap();

    private Map<BigInteger, String> sourceBbieScPathMap = new HashMap();
    private Map<BigInteger, String> targetBbieScPathMap = new HashMap();

    public Map<BigInteger, String> getSourceAsbiePathMap() {
        return sourceAsbiePathMap;
    }

    public Map<BigInteger, String> getTargetAsbiePathMap() {
        return targetAsbiePathMap;
    }

    public Map<BigInteger, String> getSourceBbiePathMap() {
        return sourceBbiePathMap;
    }

    public Map<BigInteger, String> getTargetBbiePathMap() {
        return targetBbiePathMap;
    }

    public Map<BigInteger, String> getSourceBbieScPathMap() {
        return sourceBbieScPathMap;
    }

    public Map<BigInteger, String> getTargetBbieScPathMap() {
        return targetBbieScPathMap;
    }

    @Override
    public void notFoundMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), sourceAsccPath);
    }

    @Override
    public void foundBestMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath,
                                      AsccManifest targetAsccManifest, String targetAsccPath) {
        sourceAsbiePathMap.put(asbie.getAsbieId(), sourceAsccPath);
        targetAsbiePathMap.put(asbie.getAsbieId(), targetAsccPath);
    }

    @Override
    public void notFoundMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath) {
        sourceBbiePathMap.put(bbie.getBbieId(), sourceBccPath);
    }

    @Override
    public void foundBestMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath,
                                     BccManifest targetBccManifest, String targetBccPath) {
        sourceBbiePathMap.put(bbie.getBbieId(), sourceBccPath);
        targetBbiePathMap.put(bbie.getBbieId(), targetBccPath);
    }

    @Override
    public void notFoundMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceDtScManifest, String sourceDtScPath) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), sourceDtScPath);
    }

    @Override
    public void foundBestMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceAsccManifest, String sourceDtScPath,
                                       DtScManifest targetDtScManifest, String targetDtScPath) {
        sourceBbieScPathMap.put(bbieSc.getBbieScId(), sourceDtScPath);
        targetBbieScPathMap.put(bbieSc.getBbieScId(), targetDtScPath);
    }
}
