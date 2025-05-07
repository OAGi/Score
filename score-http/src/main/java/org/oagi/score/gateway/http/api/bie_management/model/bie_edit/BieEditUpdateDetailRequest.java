package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.TopLevelAsbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepNode;

import java.util.Collections;
import java.util.List;

@Data
public class BieEditUpdateDetailRequest {

    private TopLevelAsbiepId topLevelAsbiepId;
    private TopLevelAsbiepRequest topLevelAsbiepDetail;
    private List<AbieNode.Abie> abieDetails = Collections.emptyList();
    private List<AsbieNode.Asbie> asbieDetails = Collections.emptyList();
    private List<BbieNode.Bbie> bbieDetails = Collections.emptyList();
    private List<AsbiepNode.Asbiep> asbiepDetails = Collections.emptyList();
    private List<BbiepNode.Bbiep> bbiepDetails = Collections.emptyList();
    private List<BbieScNode.BbieSc> bbieScDetails = Collections.emptyList();

}
