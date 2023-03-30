package org.oagi.score.repo.component.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.graph.data.Node;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListManifestRecord;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Data
public class CodeListGraphContext implements GraphContext {

    private DSLContext dslContext;
    private ULong releaseId;

    private Map<ULong, CodeListManifest> codeListManifestMap;
    private Map<ULong, List<CodeListValueManifest>> codeListValueManifestMap;

    @Data
    @AllArgsConstructor
    public class CodeListManifest {
        private ULong codeListManifestId;
        private ULong basedCodeListManifestId;
        private String name;
        private String state;
        private ULong releaseId;
        private ULong prevCodeListManifestId;
    }

    @Data
    @AllArgsConstructor
    public class CodeListValueManifest {
        private ULong codeListValueManifestId;
        private ULong codeListManifestId;
        private String meaning;
        private String value;
        private String state;
        private ULong releaseId;
    }

    public CodeListGraphContext(DSLContext dslContext, BigInteger releaseId) {
        this.dslContext = dslContext;
        this.releaseId = ULong.valueOf(releaseId);

        codeListManifestMap = dslContext.select(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                CODE_LIST.NAME,
                CODE_LIST.STATE,
                CODE_LIST_MANIFEST.RELEASE_ID,
                CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST)
                .on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .fetch(record -> new CodeListManifest(
                        record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID),
                        record.get(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID),
                        record.get(CODE_LIST.NAME),
                        record.get(CODE_LIST.STATE),
                        record.get(CODE_LIST_MANIFEST.RELEASE_ID),
                        record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID)
                )).stream()
                .collect(Collectors.toMap(CodeListManifest::getCodeListManifestId, Function.identity()));

        codeListValueManifestMap = dslContext.select(
                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID,
                CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST_VALUE.MEANING,
                CODE_LIST_VALUE.VALUE,
                CODE_LIST.STATE,
                CODE_LIST_VALUE_MANIFEST.RELEASE_ID)
                .from(CODE_LIST_VALUE_MANIFEST)
                .join(CODE_LIST_VALUE)
                .on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                .join(CODE_LIST_MANIFEST)
                .on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(CODE_LIST)
                .on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .fetch(record -> new CodeListValueManifest(
                        record.get(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID),
                        record.get(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID),
                        record.get(CODE_LIST_VALUE.MEANING),
                        record.get(CODE_LIST_VALUE.VALUE),
                        record.get(CODE_LIST.STATE),
                        record.get(CODE_LIST_VALUE_MANIFEST.RELEASE_ID)
                )).stream()
                .collect(groupingBy(CodeListValueManifest::getCodeListManifestId));
    }

    @Override
    public List<Node> findChildren(Node node, boolean excludeUEG) {
        switch (node.getType()) {
            case CODE_LIST:
                List<Node> children = new ArrayList();
                children.addAll(
                        codeListValueManifestMap.getOrDefault(node.getManifestId(),
                                codeListValueManifestMap.getOrDefault(node.getPrevManifestId(), Collections.emptyList()))
                                .stream().filter(e -> e.getReleaseId().equals(releaseId))
                                .sorted(Comparator.comparing(CodeListValueManifest::getCodeListValueManifestId))
                                .map(e -> toNode(e)).collect(Collectors.toList())
                );

                return children;

            case CODE_LIST_VALUE:
            default:
                return Collections.emptyList();
        }
    }

    public Node toNode(CodeListManifestRecord codeListManifestRecord) {
        Record2<String, String> res = dslContext.select(CODE_LIST.NAME, CODE_LIST.STATE)
                .from(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId()))
                .fetchOne();

        return toNode(new CodeListManifest(
                codeListManifestRecord.getCodeListManifestId(),
                codeListManifestRecord.getBasedCodeListManifestId(),
                res.get(CODE_LIST.NAME),
                res.get(CODE_LIST.STATE),
                codeListManifestRecord.getReleaseId(),
                codeListManifestRecord.getPrevCodeListManifestId()
        ));
    }

    public Node toNode(CodeListManifest codeListManifest) {
        Node node = Node.toNode(Node.NodeType.CODE_LIST, codeListManifest.getCodeListManifestId(),
                CcState.valueOf(codeListManifest.getState()));
        node.setBasedManifestId(codeListManifest.getBasedCodeListManifestId());
        node.setPrevManifestId(codeListManifest.getPrevCodeListManifestId());
        node.put("state", codeListManifest.getState());
        node.put("name", codeListManifest.getName());
        return node;
    }

    public Node toNode(CodeListValueManifest codeListValueManifest) {
        Node node = Node.toNode(Node.NodeType.CODE_LIST_VALUE, codeListValueManifest.getCodeListValueManifestId(),
                CcState.valueOf(codeListValueManifest.getState()));
        node.setLinkedManifestId(codeListValueManifest.getCodeListManifestId());
        node.put("state", codeListValueManifest.getState());
        node.put("meaning", codeListValueManifest.getMeaning());
        node.put("value", codeListValueManifest.getValue());
        return node;
    }
}
