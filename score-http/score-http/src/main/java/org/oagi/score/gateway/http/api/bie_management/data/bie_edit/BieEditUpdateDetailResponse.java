package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.repo.component.abie.AbieNode;
import org.oagi.score.repo.component.asbie.AsbieNode;
import org.oagi.score.repo.component.asbiep.AsbiepNode;
import org.oagi.score.repo.component.bbie.BbieNode;
import org.oagi.score.repo.component.bbie_sc.BbieScNode;
import org.oagi.score.repo.component.bbiep.BbiepNode;

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
