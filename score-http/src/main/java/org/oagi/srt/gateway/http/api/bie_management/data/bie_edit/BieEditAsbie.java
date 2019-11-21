package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditAsbie {

    private long asbieId;
    private long fromAbieId;
    private long toAsbiepId;
    private long basedAsccId;
    private boolean used;

}
