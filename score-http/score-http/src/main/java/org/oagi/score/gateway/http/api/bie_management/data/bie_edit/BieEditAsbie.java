package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.data.Cardinality;

import java.math.BigInteger;

@Data
public class BieEditAsbie implements Cardinality {

    private BigInteger asbieId;
    private BigInteger fromAbieId;
    private BigInteger toAsbiepId;
    private BigInteger basedAsccId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
