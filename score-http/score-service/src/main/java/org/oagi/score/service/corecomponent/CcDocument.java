package org.oagi.score.service.corecomponent;

import org.oagi.score.repo.api.corecomponent.model.*;

import java.math.BigInteger;
import java.util.List;

public interface CcDocument {

    AccManifest getAccManifest(BigInteger accManifestId);

    Acc getAcc(AccManifest accManifest);

    AccManifest getRoleOfAccManifest(AsccpManifest asccpManifest);

    AccManifest getBasedAccManifest(AccManifest accManifest);

    List<CcAssociation> getAssociations(AccManifest accManifest);

    AsccManifest getAsccManifest(BigInteger asccManifestId);

    Ascc getAscc(AsccManifest asccManifest);

    BccManifest getBccManifest(BigInteger bccManifestId);

    Bcc getBcc(BccManifest bccManifest);

    AsccpManifest getAsccpManifest(BigInteger asccpManifestId);

    Asccp getAsccp(AsccpManifest asccpManifest);

    BccpManifest getBccpManifest(BigInteger bccpManifestId);

    Bccp getBccp(BccpManifest bccpManifest);

    DtManifest getDtManifest(BigInteger dtManifestId);

    Dt getDt(DtManifest dtManifest);

    DtScManifest getDtScManifest(BigInteger dtScManifestId);

    DtSc getDtSc(DtScManifest dtScManifest);

    List<DtScManifest> getDtScManifests(DtManifest dtManifest);

    List<BdtPriRestri> getBdtPriRestriList(DtManifest bdtManifest);

    List<BdtScPriRestri> getBdtScPriRestriList(DtScManifest bdtScManifest);

}
