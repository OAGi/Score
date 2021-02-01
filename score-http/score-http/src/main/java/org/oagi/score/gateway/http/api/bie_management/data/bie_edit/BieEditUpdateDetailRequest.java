package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.data.TopLevelAsbiepRequest;
import org.oagi.score.repo.component.abie.AbieNode;
import org.oagi.score.repo.component.asbie.AsbieNode;
import org.oagi.score.repo.component.asbiep.AsbiepNode;
import org.oagi.score.repo.component.bbie.BbieNode;
import org.oagi.score.repo.component.bbie_sc.BbieScNode;
import org.oagi.score.repo.component.bbiep.BbiepNode;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class BieEditUpdateDetailRequest {

    private BigInteger topLevelAsbiepId;
    private TopLevelAsbiepRequest topLevelAsbiepDetail;
    private List<AbieNode.Abie> abieDetails = Collections.emptyList();
    private List<AsbieNode.Asbie> asbieDetails = Collections.emptyList();
    private List<BbieNode.Bbie> bbieDetails = Collections.emptyList();
    private List<AsbiepNode.Asbiep> asbiepDetails = Collections.emptyList();
    private List<BbiepNode.Bbiep> bbiepDetails = Collections.emptyList();
    private List<BbieScNode.BbieSc> bbieScDetails = Collections.emptyList();

}
