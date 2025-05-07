package org.oagi.score.gateway.http.api.graph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class CodeListGraphContext implements GraphContext {

    private Map<CodeListManifestId, CodeListSummaryRecord> codeListMap;

    @Data
    @AllArgsConstructor
    public class CodeListManifest {
        private CodeListManifestId codeListManifestId;
        private CodeListManifestId basedCodeListManifestId;
        private String name;
        private String state;
        private ReleaseId releaseId;
        private CodeListManifestId prevCodeListManifestId;
    }

    @Data
    @AllArgsConstructor
    public class CodeListValueManifest {
        private CodeListValueManifestId codeListValueManifestId;
        private CodeListManifestId codeListManifestId;
        private String meaning;
        private String value;
        private String state;
        private ReleaseId releaseId;
    }

    public CodeListGraphContext(ScoreUser requester, RepositoryFactory repositoryFactory) {

        var query = repositoryFactory.codeListQueryRepository(requester);
        List<CodeListSummaryRecord> codeListList = query.getCodeListSummaryList();
        codeListMap = codeListList.stream().collect(Collectors.toMap(CodeListSummaryRecord::codeListManifestId, Function.identity()));
    }

    private CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return codeListMap.get(codeListManifestId);
    }

    @Override
    public List<Node> findChildren(Node node, boolean excludeUEG) {
        switch (node.getType()) {
            case CODE_LIST:
                List<Node> children = new ArrayList();
                CodeListSummaryRecord codeList = getCodeList(new CodeListManifestId(node.getManifestId().value()));
                if (codeList == null) {
                    codeList = getCodeList(new CodeListManifestId(node.getPrevManifestId().value()));
                }
                Collection<CodeListValueSummaryRecord> valueList = (codeList != null) ? codeList.valueList() : Collections.emptyList();
                children.addAll(
                        valueList.stream()
                                .sorted(Comparator.comparing(CodeListValueSummaryRecord::codeListValueManifestId))
                                .map(e -> toNode(e)).collect(Collectors.toList())
                );

                return children;

            case CODE_LIST_VALUE:
            default:
                return Collections.emptyList();
        }
    }

    public Node toNode(CodeListSummaryRecord codeList) {
        Node node = Node.toNode(Node.NodeType.CODE_LIST, codeList.codeListManifestId(), codeList.state());
        node.setBasedManifestId(codeList.basedCodeListManifestId());
        node.setPrevManifestId(codeList.prevCodeListManifestId());
        node.put("state", codeList.state());
        node.put("name", codeList.name());
        return node;
    }

    public Node toNode(CodeListValueSummaryRecord codeListValue) {
        Node node = Node.toNode(Node.NodeType.CODE_LIST_VALUE, codeListValue.codeListValueManifestId(), codeListValue.state());
        node.setLinkedManifestId(codeListValue.codeListManifestId());
        node.put("state", codeListValue.state());
        node.put("meaning", codeListValue.meaning());
        node.put("value", codeListValue.value());
        return node;
    }
}
