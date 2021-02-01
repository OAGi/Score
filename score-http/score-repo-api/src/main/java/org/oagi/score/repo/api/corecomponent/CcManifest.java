package org.oagi.score.repo.api.corecomponent;

import java.math.BigInteger;

public interface CcManifest {

    BigInteger getManifestId();

    BigInteger getReleaseId();

    BigInteger getBasedCcId();

}
