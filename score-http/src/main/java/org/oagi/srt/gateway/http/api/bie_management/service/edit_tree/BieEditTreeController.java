package org.oagi.srt.gateway.http.api.bie_management.service.edit_tree;

import org.oagi.srt.data.BieState;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.BieEditNode;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAbieNode;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree.BieEditNodeDetail;

import java.util.List;

public interface BieEditTreeController {

    BieEditAbieNode getRootNode(long topLevelAbieId);

    List<BieEditNode> getDescendants(BieEditNode node, boolean hideUnused);

    BieEditNodeDetail getDetail(BieEditNode node);

    void updateState(BieState state);

    boolean updateDetail(BieEditNodeDetail detail);
}
