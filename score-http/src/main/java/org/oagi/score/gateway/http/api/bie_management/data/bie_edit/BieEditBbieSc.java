package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditBbieSc {

    private long bbieScId;
    private long bbieId;
    private long dtScId;
    private boolean used;

}
