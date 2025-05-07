package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

@Data
public class BieEditAbie {

    private AbieId abieId;
    private AccManifestId basedAccManifestId;

}
