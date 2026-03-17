package org.oagi.score.gateway.http.api.bie_management.service.edit_tree;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditNodeDetail;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.Collection;
import java.util.List;

public interface BieEditTreeController {

    BieEditAbieNode getRootNode(TopLevelAsbiepId topLevelAsbiepId);

    List<BieEditNode> getDescendants(ScoreUser requester, BieEditNode node, boolean hideUnused);

    BieEditNodeDetail getDetail(BieEditNode node);

    /**
     * Applies the requested state to the current root BIE and to any dependency rows
     * that were validated and approved by the caller.
     */
    void updateState(ScoreUser requester, BieState state, Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds);

    boolean updateDetail(BieEditNodeDetail detail);
}
