package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.Cardinality;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.service.common.data.TrackableImpl;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBcc extends TrackableImpl implements SeqKeySupportable, Cardinality {

    private BigInteger bccId;
    private BigInteger bccManifestId;
    private String guid;
    private BigInteger fromAccManifestId;
    private BigInteger toBccpManifestId;
    private int seqKey;
    private int entityType;

    private int cardinalityMin;
    private int cardinalityMax;

    public boolean isAttribute() {
        return (entityType == 0);
    }

    @Override
    public BigInteger getId() {
        return bccId;
    }

}
