package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditBbie {

    private long bbieId;
    private long fromAbieId;
    private long toBbiepId;
    private long basedBccId;
    private boolean used;

}
