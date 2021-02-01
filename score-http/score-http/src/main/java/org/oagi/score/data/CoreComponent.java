package org.oagi.score.data;

import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.Trackable;

import java.math.BigInteger;

public interface CoreComponent extends Trackable {

    BigInteger getId();

    String getGuid();

    CcState getState();

    String getDen();

}
