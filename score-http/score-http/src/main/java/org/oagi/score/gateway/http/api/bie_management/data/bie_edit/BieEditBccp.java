package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.service.common.data.TrackableImpl;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBccp extends TrackableImpl {

    private BigInteger bccpManifestId;
    private String guid;
    private String propertyTerm;
    private BigInteger bdtManifestId;

    @Override
    public BigInteger getId() {
        return bccpManifestId;
    }

}
