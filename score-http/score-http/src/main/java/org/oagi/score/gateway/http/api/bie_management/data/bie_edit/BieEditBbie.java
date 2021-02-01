package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.data.Cardinality;

import java.math.BigInteger;

@Data
public class BieEditBbie implements Cardinality {

    private BigInteger bbieId;
    private BigInteger fromAbieId;
    private BigInteger toBbiepId;
    private BigInteger basedBccId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
