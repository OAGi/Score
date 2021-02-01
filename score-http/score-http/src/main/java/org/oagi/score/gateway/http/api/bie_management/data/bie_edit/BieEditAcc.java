package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.service.common.data.TrackableImpl;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAcc extends TrackableImpl {

    private BigInteger accManifestId;
    private String guid;
    private int oagisComponentType;
    private BigInteger basedAccManifestId;

    @Override
    public BigInteger getId() {
        return accManifestId;
    }
}
