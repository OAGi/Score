package org.oagi.score.service.bie;

import org.oagi.score.repo.api.bie.model.Asbie;
import org.oagi.score.repo.api.bie.model.Bbie;
import org.oagi.score.repo.api.bie.model.BbieSc;
import org.oagi.score.repo.api.corecomponent.model.AsccManifest;
import org.oagi.score.repo.api.corecomponent.model.BccManifest;
import org.oagi.score.repo.api.corecomponent.model.DtScManifest;

public interface BieUpliftingListener {

    void notFoundMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath, String sourceContextDefinition);

    void foundBestMatchedAsbie(Asbie asbie, AsccManifest sourceAsccManifest, String sourceAsccPath, String sourceContextDefinition,
                               AsccManifest targetAsccManifest, String targetAsccPath);

    void notFoundMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath, String sourceContextDefinition);

    void foundBestMatchedBbie(Bbie bbie, BccManifest sourceBccManifest, String sourceBccPath, String sourceContextDefinition,
                              BccManifest targetBccManifest, String targetBccPath);

    void notFoundMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceDtScManifest, String sourceDtScPath, String sourceContextDefinition);

    void foundBestMatchedBbieSc(BbieSc bbieSc, DtScManifest sourceAsccManifest, String sourceDtScPath, String sourceContextDefinition,
                                DtScManifest targetDtScManifest, String targetDtScPath);

}
