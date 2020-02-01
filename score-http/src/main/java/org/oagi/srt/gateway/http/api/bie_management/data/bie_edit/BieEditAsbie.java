package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.srt.data.Cardinality;

@Data
public class BieEditAsbie implements Cardinality {

    private long asbieId;
    private long fromAbieId;
    private long toAsbiepId;
    private long basedAsccId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
