package org.oagi.score.service.common.data;

import java.io.Serializable;
import java.math.BigInteger;

public interface Trackable extends Serializable {

    BigInteger getId();

    BigInteger getReleaseId();
    String getReleaseNum();

    BigInteger getLogId();
    int getRevisionNum();
    int getRevisionTrackingNum();


}
