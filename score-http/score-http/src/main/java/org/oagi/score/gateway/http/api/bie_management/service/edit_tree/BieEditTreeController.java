package org.oagi.score.gateway.http.api.bie_management.service.edit_tree;

import org.oagi.score.service.common.data.BieState;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditNodeDetail;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.util.List;

public interface BieEditTreeController {

    BieEditAbieNode getRootNode(BigInteger topLevelAsbiepId);

    List<BieEditNode> getDescendants(AuthenticatedPrincipal user, BieEditNode node, boolean hideUnused);

    BieEditNodeDetail getDetail(BieEditNode node);

    void updateState(BieState state);

    boolean updateDetail(BieEditNodeDetail detail);
}
