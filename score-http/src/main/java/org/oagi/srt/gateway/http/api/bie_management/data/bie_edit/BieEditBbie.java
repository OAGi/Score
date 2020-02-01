package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.srt.data.Cardinality;

@Data
public class BieEditBbie implements Cardinality {

    private long bbieId;
    private long fromAbieId;
    private long toBbiepId;
    private long basedBccId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
