package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScNode;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepNode;

import java.util.Collections;
import java.util.Map;

@Data
public class BieEditUpdateDetailResponse {

    private Map<String, AbieNode.Abie> abieDetailMap = Collections.emptyMap();
    private Map<String, AsbieNode.Asbie> asbieDetailMap = Collections.emptyMap();
    private Map<String, BbieNode.Bbie> bbieDetailMap = Collections.emptyMap();
    private Map<String, AsbiepNode.Asbiep> asbiepDetailMap = Collections.emptyMap();
    private Map<String, BbiepNode.Bbiep> bbiepDetailMap = Collections.emptyMap();
    private Map<String, BbieScNode.BbieSc> bbieScDetailMap = Collections.emptyMap();
}
