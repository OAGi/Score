package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditXbt {

    private long priRestriId;
    private boolean isDefault;
    private long xbtId;
    private String xbtName;

}
