package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.Cardinality;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.service.common.data.TrackableImpl;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAscc extends TrackableImpl implements SeqKeySupportable, Cardinality {

    private BigInteger asccId;
    private BigInteger asccManifestId;
    private String guid;
    private BigInteger fromAccManifestId;
    private BigInteger toAsccpManifestId;
    private int seqKey;

    private int cardinalityMin;
    private int cardinalityMax;

    @Override
    public BigInteger getId() {
        return asccId;
    }

}
