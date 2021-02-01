package org.oagi.score.service.corecomponent.seqkey;

import java.math.BigInteger;

public interface SeqKeySupportable {

    String getState();
    BigInteger getSeqKeyId();
    BigInteger getPrevSeqKeyId();
    BigInteger getNextSeqKeyId();

}
