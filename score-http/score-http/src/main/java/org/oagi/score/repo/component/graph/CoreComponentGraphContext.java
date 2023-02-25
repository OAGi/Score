package org.oagi.score.repo.component.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.graph.data.FindUsagesResponse;
import org.oagi.score.gateway.http.api.graph.data.Graph;
import org.oagi.score.gateway.http.api.graph.data.Node;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.service.common.data.BCCEntityType;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.corecomponent.seqkey.SeqKeyHandler;
import org.oagi.score.service.corecomponent.seqkey.SeqKeySupportable;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Data
public class CoreComponentGraphContext implements GraphContext {

    private DSLContext dslContext;
    private ULong releaseId;

    private Map<ULong, AccManifest> accManifestMap;
    private Map<ULong, List<AccManifest>> accManifestMapByBasedAccManifestId;
    private Map<ULong, Set<AccManifest>> accManifestMapByToAsccpManifestId;
    private Map<ULong, Set<AccManifest>> accManifestMapByToBccpManifestId;
    private Map<ULong, List<ShortTag>> accManifestTagMap;
    private Map<ULong, AsccpManifest> asccpManifestMap;
    private Map<ULong, List<AsccpManifest>> asccpManifestMapByRoleOfAccManifestId;
    private Map<ULong, List<ShortTag>> asccpManifestTagMap;
    private Map<ULong, BccpManifest> bccpManifestMap;
    private Map<ULong, List<ShortTag>> bccpManifestTagMap;
    private Map<ULong, List<AsccManifest>> asccManifestMap;
    private Map<ULong, List<BccManifest>> bccManifestMap;
    private Map<ULong, DtManifest> dtManifestMap;
    private Map<ULong, List<ShortTag>> dtManifestTagMap;
    private Map<ULong, List<DtScManifest>> dtScManifestMap;

    @Data
    @AllArgsConstructor
    public class AccManifest {
        private ULong accManifestId;
        private ULong basedAccManifestId;
        private String objectClassTerm;
        private String den;
        private OagisComponentType componentType;
        private String state;
        private String guid;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevAccManifestId;
    }

    @Data
    @AllArgsConstructor
    public class AsccpManifest {
        private ULong asccpManifestId;
        private ULong roleOfAccManifestId;
        private String propertyTerm;
        private String state;
        private String guid;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevAsccpManifestId;
    }

    @Data
    @AllArgsConstructor
    public class BccpManifest {
        private ULong bccpManifestId;
        private ULong bdtManifestId;
        private String propertyTerm;
        private String representationTerm;
        private String state;
        private String guid;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevBccpManifestId;
    }

    @Data
    @AllArgsConstructor
    public class AsccManifest implements SeqKeySupportable {
        private ULong asccManifestId;
        private ULong fromAccManifestId;
        private ULong toAsccpManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String state;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevAsccManifestId;

        private BigInteger seqKeyId;
        private BigInteger prevSeqKeyId;
        private BigInteger nextSeqKeyId;
    }

    @Data
    @AllArgsConstructor
    public class BccManifest implements SeqKeySupportable {
        private ULong bccManifestId;
        private ULong fromAccManifestId;
        private ULong toBccpManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private BCCEntityType entityType;
        private String state;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevBccManifestId;

        private BigInteger seqKeyId;
        private BigInteger prevSeqKeyId;
        private BigInteger nextSeqKeyId;
    }

    @Data
    @AllArgsConstructor
    public class DtManifest {
        private ULong dtManifestId;
        private String dataTypeTerm;
        private String qualifier;
        private String den;
        private String state;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevDtManifestId;
    }

    @Data
    @AllArgsConstructor
    public class DtScManifest {
        private ULong dtScManifestId;
        private ULong basedDtScId;
        private ULong ownerDtManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String objectClassTerm;
        private String propertyTerm;
        private String representationTerm;
        private String State;
        private Byte isDeprecated;
        private ULong releaseId;
        private ULong prevDtScManifestId;
    }

    public CoreComponentGraphContext(DSLContext dslContext, BigInteger releaseId) {
        this.dslContext = dslContext;
        this.releaseId = ULong.valueOf(releaseId);

        List<AccManifest> accManifestList = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                ACC.OBJECT_CLASS_TERM, ACC.DEN, ACC.OAGIS_COMPONENT_TYPE, ACC.STATE, ACC.GUID,
                ACC.IS_DEPRECATED, ACC_MANIFEST.RELEASE_ID, ACC_MANIFEST.PREV_ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(this.releaseId))
                .fetch(record -> new AccManifest(
                        record.get(ACC_MANIFEST.ACC_MANIFEST_ID),
                        record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID),
                        record.get(ACC.OBJECT_CLASS_TERM),
                        record.get(ACC.DEN),
                        OagisComponentType.valueOf(record.get(ACC.OAGIS_COMPONENT_TYPE)),
                        record.get(ACC.STATE),
                        record.get(ACC.GUID),
                        record.get(ACC.IS_DEPRECATED),
                        record.get(ACC_MANIFEST.RELEASE_ID),
                        record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID)
                ));

        accManifestMap = accManifestList.stream()
                .collect(Collectors.toMap(AccManifest::getAccManifestId, Function.identity()));
        accManifestMapByBasedAccManifestId = accManifestList.stream()
                .filter(e -> e.getBasedAccManifestId() != null)
                .collect(groupingBy(AccManifest::getBasedAccManifestId));

        accManifestTagMap = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                .join(ACC_MANIFEST).on(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(this.releaseId))
                .fetchGroups(ACC_MANIFEST.ACC_MANIFEST_ID, ShortTag.class);

        List<AsccpManifest> asccpManifestList =
                dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                        ASCCP.PROPERTY_TERM, ASCCP.STATE, ASCCP.GUID, ASCCP.IS_DEPRECATED,
                        ASCCP_MANIFEST.RELEASE_ID, ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .where(ASCCP_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> new AsccpManifest(
                                record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID),
                                record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID),
                                record.get(ASCCP.PROPERTY_TERM),
                                record.get(ASCCP.STATE),
                                record.get(ASCCP.GUID),
                                record.get(ASCCP.IS_DEPRECATED),
                                record.get(ASCCP_MANIFEST.RELEASE_ID),
                                record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID)
                        ));
        asccpManifestMap = asccpManifestList.stream()
                .collect(Collectors.toMap(AsccpManifest::getAsccpManifestId, Function.identity()));
        asccpManifestMapByRoleOfAccManifestId = asccpManifestList.stream()
                .collect(groupingBy(AsccpManifest::getRoleOfAccManifestId));

        asccpManifestTagMap = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(this.releaseId))
                .fetchGroups(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ShortTag.class);

        bccpManifestMap =
                dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.BDT_MANIFEST_ID,
                        BCCP.PROPERTY_TERM, BCCP.REPRESENTATION_TERM, BCCP.STATE, BCCP.GUID, BCCP.IS_DEPRECATED,
                        BCCP_MANIFEST.RELEASE_ID, BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID)
                        .from(BCCP_MANIFEST)
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .where(BCCP_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> new BccpManifest(
                                record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID),
                                record.get(BCCP_MANIFEST.BDT_MANIFEST_ID),
                                record.get(BCCP.PROPERTY_TERM),
                                record.get(BCCP.REPRESENTATION_TERM),
                                record.get(BCCP.STATE),
                                record.get(BCCP.GUID),
                                record.get(BCCP.IS_DEPRECATED),
                                record.get(BCCP_MANIFEST.RELEASE_ID),
                                record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID)
                        )).stream()
                        .collect(Collectors.toMap(BccpManifest::getBccpManifestId, Function.identity()));

        bccpManifestTagMap = dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                .join(BCCP_MANIFEST).on(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(this.releaseId))
                .fetchGroups(BCCP_MANIFEST.BCCP_MANIFEST_ID, ShortTag.class);

        List<AsccManifest> asccManifestList =
                dslContext.select(
                        ASCC_MANIFEST.ASCC_MANIFEST_ID,
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                        ASCC.CARDINALITY_MIN, ASCC.CARDINALITY_MAX,
                        SEQ_KEY.SEQ_KEY_ID, SEQ_KEY.PREV_SEQ_KEY_ID, SEQ_KEY.NEXT_SEQ_KEY_ID,
                        ASCC.STATE, ASCC.IS_DEPRECATED, ASCC_MANIFEST.RELEASE_ID, ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .join(SEQ_KEY).on(ASCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID))
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> {
                            ULong seqKeyId = record.get(SEQ_KEY.SEQ_KEY_ID);
                            ULong prevSeqKeyId = record.get(SEQ_KEY.PREV_SEQ_KEY_ID);
                            ULong nextSeqKeyId = record.get(SEQ_KEY.NEXT_SEQ_KEY_ID);

                            return new AsccManifest(
                                    record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID),
                                    record.get(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID),
                                    record.get(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID),
                                    record.get(ASCC.CARDINALITY_MIN),
                                    record.get(ASCC.CARDINALITY_MAX),
                                    record.get(ASCC.STATE),
                                    record.get(ASCC.IS_DEPRECATED),
                                    record.get(ASCC_MANIFEST.RELEASE_ID),
                                    record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID),
                                    seqKeyId.toBigInteger(),
                                    (prevSeqKeyId != null) ? prevSeqKeyId.toBigInteger() : null,
                                    (nextSeqKeyId != null) ? nextSeqKeyId.toBigInteger() : null);
                        });
        asccManifestMap = asccManifestList.stream()
                .collect(groupingBy(AsccManifest::getFromAccManifestId));
        accManifestMapByToAsccpManifestId = new HashMap();
        for (AsccManifest asccManifest : asccManifestList) {
            ULong toAsccpManifestId = asccManifest.getToAsccpManifestId();
            Set<AccManifest> accManifests = accManifestMapByToAsccpManifestId.get(toAsccpManifestId);
            if (accManifests == null) {
                accManifests = new HashSet();
                accManifestMapByToAsccpManifestId.put(toAsccpManifestId, accManifests);
            }
            accManifests.add(accManifestMap.get(asccManifest.getFromAccManifestId()));
        }
        List<BccManifest> bccManifestList =
                dslContext.select(
                        BCC_MANIFEST.BCC_MANIFEST_ID,
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                        BCC.CARDINALITY_MIN, BCC.CARDINALITY_MAX, BCC.ENTITY_TYPE,
                        SEQ_KEY.SEQ_KEY_ID, SEQ_KEY.PREV_SEQ_KEY_ID, SEQ_KEY.NEXT_SEQ_KEY_ID,
                        BCC.STATE, BCC.IS_DEPRECATED, BCC_MANIFEST.RELEASE_ID, BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                        .from(BCC_MANIFEST)
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .join(SEQ_KEY).on(BCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID))
                        .where(BCC_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> {
                            ULong seqKeyId = record.get(SEQ_KEY.SEQ_KEY_ID);
                            ULong prevSeqKeyId = record.get(SEQ_KEY.PREV_SEQ_KEY_ID);
                            ULong nextSeqKeyId = record.get(SEQ_KEY.NEXT_SEQ_KEY_ID);

                            return new BccManifest(
                                    record.get(BCC_MANIFEST.BCC_MANIFEST_ID),
                                    record.get(BCC_MANIFEST.FROM_ACC_MANIFEST_ID),
                                    record.get(BCC_MANIFEST.TO_BCCP_MANIFEST_ID),
                                    record.get(BCC.CARDINALITY_MIN),
                                    record.get(BCC.CARDINALITY_MAX),
                                    BCCEntityType.valueOf(record.get(BCC.ENTITY_TYPE)),
                                    record.get(BCC.STATE),
                                    record.get(BCC.IS_DEPRECATED),
                                    record.get(BCC_MANIFEST.RELEASE_ID),
                                    record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID),
                                    seqKeyId.toBigInteger(),
                                    (prevSeqKeyId != null) ? prevSeqKeyId.toBigInteger() : null,
                                    (nextSeqKeyId != null) ? nextSeqKeyId.toBigInteger() : null);
                        });
        bccManifestMap = bccManifestList.stream()
                .collect(groupingBy(BccManifest::getFromAccManifestId));
        accManifestMapByToBccpManifestId = new HashMap();
        for (BccManifest bccManifest : bccManifestList) {
            ULong toBccpManifestId = bccManifest.getToBccpManifestId();
            Set<AccManifest> accManifests = accManifestMapByToBccpManifestId.get(toBccpManifestId);
            if (accManifests == null) {
                accManifests = new HashSet();
                accManifestMapByToBccpManifestId.put(toBccpManifestId, accManifests);
            }
            accManifests.add(accManifestMap.get(bccManifest.getFromAccManifestId()));
        }
        dtManifestMap =
                dslContext.select(DT_MANIFEST.DT_MANIFEST_ID, DT.DATA_TYPE_TERM, DT.QUALIFIER, DT.DEN, DT.STATE,
                        DT.IS_DEPRECATED, DT_MANIFEST.RELEASE_ID, DT_MANIFEST.PREV_DT_MANIFEST_ID)
                        .from(DT_MANIFEST)
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .where(DT_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> new DtManifest(
                                record.get(DT_MANIFEST.DT_MANIFEST_ID),
                                record.get(DT.DATA_TYPE_TERM),
                                record.get(DT.QUALIFIER),
                                record.get(DT.DEN),
                                record.get(DT.STATE),
                                record.get(DT.IS_DEPRECATED),
                                record.get(DT_MANIFEST.RELEASE_ID),
                                record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID)
                        )).stream()
                        .collect(Collectors.toMap(DtManifest::getDtManifestId, Function.identity()));

        dtManifestTagMap = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(this.releaseId))
                .fetchGroups(DT_MANIFEST.DT_MANIFEST_ID, ShortTag.class);

        dtScManifestMap =
                dslContext.select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                        DT_SC.OBJECT_CLASS_TERM, DT_SC.PROPERTY_TERM, DT_SC.REPRESENTATION_TERM, DT.STATE,
                        DT_SC.IS_DEPRECATED, DT_SC_MANIFEST.RELEASE_ID, DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                        DT_SC.CARDINALITY_MIN, DT_SC.CARDINALITY_MAX, DT_SC.BASED_DT_SC_ID)
                        .from(DT_SC_MANIFEST)
                        .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                        .join(DT).on(DT_SC.OWNER_DT_ID.eq(DT.DT_ID))
                        .where(DT_SC_MANIFEST.RELEASE_ID.eq(this.releaseId))
                        .fetch(record -> new DtScManifest(
                                record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID),
                                record.get(DT_SC.BASED_DT_SC_ID),
                                record.get(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID),
                                record.get(DT_SC.CARDINALITY_MIN),
                                record.get(DT_SC.CARDINALITY_MAX),
                                record.get(DT_SC.OBJECT_CLASS_TERM),
                                record.get(DT_SC.PROPERTY_TERM),
                                record.get(DT_SC.REPRESENTATION_TERM),
                                record.get(DT.STATE),
                                record.get(DT_SC.IS_DEPRECATED),
                                record.get(DT_SC_MANIFEST.RELEASE_ID),
                                record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                        )).stream()
                        .collect(groupingBy(DtScManifest::getOwnerDtManifestId));
    }

    public FindUsagesResponse findUsages(Node node) {
        FindUsagesResponse response = new FindUsagesResponse();

        Graph graph = new Graph();
        graph.addNode(node);

        if (node.getType() == Node.NodeType.ACC) {
            Queue<Node> queue = new LinkedBlockingQueue();
            queue.offer(node);

            while (!queue.isEmpty()) {
                Node currentNode = queue.poll();
                switch (currentNode.getType()) {
                    case ACC:
                        List<Node> accUsages = accManifestMapByBasedAccManifestId.getOrDefault(
                                currentNode.getManifestId(), Collections.emptyList())
                                .stream().map(e -> toNode(e)).collect(Collectors.toList());

                        accUsages.forEach(_node -> {
                            graph.addNode(_node);
                        });

                        graph.addEdges(currentNode, accUsages);
                        queue.addAll(accUsages);

                        break;
                }
            }

            List<Node> asccpUsages = asccpManifestMapByRoleOfAccManifestId.getOrDefault(
                    node.getManifestId(), Collections.emptyList())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            asccpUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, asccpUsages);

            Object componentType = node.getProperties().get("componentType");
            if (OagisComponentType.UserExtensionGroup.name().equals(componentType)) {
                node = asccpUsages.get(0);
            }
        }

        if (node.getType() == Node.NodeType.ASCCP) {
            List<Node> asccpUsages = accManifestMapByToAsccpManifestId.getOrDefault(
                    node.getManifestId(), Collections.emptySet())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            asccpUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, asccpUsages);
        }

        else if (node.getType() == Node.NodeType.BCCP) {
            List<Node> bccpUsages = accManifestMapByToBccpManifestId.getOrDefault(
                    node.getManifestId(), Collections.emptySet())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            bccpUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, bccpUsages);
        }

        response.setGraph(graph);
        response.setRootNodeKey(node.getKey());
        return response;
    }

    @Override
    public List<Node> findChildren(Node node, boolean excludeUEG) {
        switch (node.getType()) {
            case ACC:
                List<Node> children = new ArrayList();
                if (node.getBasedManifestId() != null) {
                    AccManifest basedAccManifest = accManifestMap.get(node.getBasedManifestId());
                    children.add(toNode(basedAccManifest));
                }

                List<SeqKeySupportable> assocs = new ArrayList();

                if (excludeUEG) {
                    if (accManifestMap.get(node.getManifestId())
                            .getComponentType().equals(OagisComponentType.UserExtensionGroup)) {
                        return children;
                    }
                }
                assocs.addAll(asccManifestMap.getOrDefault(node.getManifestId(),
                        asccManifestMap.getOrDefault(node.getPrevManifestId(), Collections.emptyList()))
                        .stream().collect(Collectors.toList()));
                assocs.addAll(bccManifestMap.getOrDefault(node.getManifestId(),
                        bccManifestMap.getOrDefault(node.getPrevManifestId(), Collections.emptyList()))
                        .stream().collect(Collectors.toList()));
                assocs = SeqKeyHandler.sort(assocs);
                children.addAll(
                        assocs.stream().map(e -> {
                            if (e instanceof AsccManifest) {
                                return toNode((AsccManifest) e);
                            } else {
                                return toNode((BccManifest) e);
                            }
                        }).collect(Collectors.toList())
                );

                return children;

            case ASCC:
                AsccpManifest asccpManifest = asccpManifestMap.get(node.getLinkedManifestId());
                if (asccpManifest == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(asccpManifest));

            case BCC:
                BccpManifest bccpManifest = bccpManifestMap.get(node.getLinkedManifestId());
                if (bccpManifest == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(bccpManifest));

            case ASCCP:
                AccManifest accManifest = accManifestMap.get(node.getLinkedManifestId());
                if (accManifest == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(accManifest));

            case BCCP:
                DtManifest dtManifest = dtManifestMap.get(node.getLinkedManifestId());
                return (dtManifest != null) ? Arrays.asList(toNode(dtManifest)) : Collections.emptyList();

            case DT:
                return dtScManifestMap.getOrDefault(node.getManifestId(),
                        dtScManifestMap.getOrDefault(node.getPrevManifestId(), Collections.emptyList()))
                        .stream()
                        .map(e -> toNode(e)).sorted((o1, o2) -> {
                            String t1 = (String) o1.getProperties().get("objectClassTerm");
                            String t2 = (String) o2.getProperties().get("objectClassTerm");
                            int compare = t1.compareTo(t2);
                            if (compare != 0) {
                                return compare;
                            }

                            t1 = (String) o1.getProperties().get("propertyTerm");
                            t2 = (String) o2.getProperties().get("propertyTerm");
                            compare = t1.compareTo(t2);
                            if (compare != 0) {
                                return compare;
                            }

                            t1 = (String) o1.getProperties().get("representationTerm");
                            t2 = (String) o2.getProperties().get("representationTerm");
                            return t1.compareTo(t2);
                        }).collect(Collectors.toList());

            case DT_SC:
            default:
                return Collections.emptyList();
        }
    }

    public Node toNode(AccManifestRecord record) {
        Record6<String, String, String, Integer, String, Byte> res = dslContext.select(ACC.OBJECT_CLASS_TERM, ACC.DEN,
                ACC.GUID, ACC.OAGIS_COMPONENT_TYPE, ACC.STATE, ACC.IS_DEPRECATED)
                .from(ACC)
                .where(ACC.ACC_ID.eq(record.getAccId()))
                .fetchOne();
        return toNode(new AccManifest(record.getAccManifestId(), record.getBasedAccManifestId(),
                res.get(ACC.OBJECT_CLASS_TERM), res.get(ACC.DEN), OagisComponentType.valueOf(res.get(ACC.OAGIS_COMPONENT_TYPE)),
                res.get(ACC.STATE), res.get(ACC.GUID), res.get(ACC.IS_DEPRECATED), record.getReleaseId(), record.getPrevAccManifestId()));
    }

    public Node toNode(AsccpManifestRecord record) {
        Record4<String, String, String, Byte> res = dslContext.select(ASCCP.PROPERTY_TERM, ASCCP.STATE, ASCCP.GUID, ASCCP.IS_DEPRECATED)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(record.getAsccpId()))
                .fetchOne();
        return toNode(new AsccpManifest(record.getAsccpManifestId(), record.getRoleOfAccManifestId(),
                res.get(ASCCP.PROPERTY_TERM), res.get(ASCCP.STATE), res.get(ASCCP.GUID), res.get(ASCCP.IS_DEPRECATED),
                record.getReleaseId(), record.getPrevAsccpManifestId()));
    }

    public Node toNode(BccpManifestRecord record) {
        Record5<String, String, String, String, Byte> res =
                dslContext.select(BCCP.PROPERTY_TERM, BCCP.REPRESENTATION_TERM, BCCP.STATE, BCCP.GUID, BCCP.IS_DEPRECATED)
                        .from(BCCP)
                        .where(BCCP.BCCP_ID.eq(record.getBccpId()))
                        .fetchOne();
        return toNode(new BccpManifest(record.getBccpManifestId(), record.getBdtManifestId(),
                res.get(BCCP.PROPERTY_TERM), res.get(BCCP.REPRESENTATION_TERM), res.get(BCCP.STATE),
                res.get(BCCP.GUID), res.get(BCCP.IS_DEPRECATED), record.getReleaseId(), record.getPrevBccpManifestId()));
    }

    public Node toNode(DtManifestRecord record) {
        Record6<String, String, String, String, String, Byte> res =
                dslContext.select(DT.DATA_TYPE_TERM, DT.QUALIFIER, DT.STATE, DT.GUID, DT.DEN, DT.IS_DEPRECATED)
                        .from(DT)
                        .where(DT.DT_ID.eq(record.getDtId()))
                        .fetchOne();
        return toNode(new DtManifest(record.getDtManifestId(),
                res.get(DT.DATA_TYPE_TERM), res.get(DT.QUALIFIER), res.get(DT.DEN), res.get(DT.STATE), res.get(DT.IS_DEPRECATED),
                record.getReleaseId(), record.getPrevDtManifestId()));
    }

    public Collection<Node> getNodes() {
        Set<Node> nodes = new HashSet();

        this.getAccManifestMap().values().stream().forEach(e -> {
            nodes.add(toNode(e));
        });
        this.getAsccManifestMap().values().stream().forEach(e -> {
            nodes.addAll(e.stream().map(e1 -> toNode(e1)).collect(Collectors.toSet()));
        });
        this.getBccManifestMap().values().stream().forEach(e -> {
            nodes.addAll(e.stream().map(e1 -> toNode(e1)).collect(Collectors.toSet()));
        });
        this.getAsccpManifestMap().values().stream().forEach(e -> {
            nodes.add(toNode(e));
        });
        this.getBccpManifestMap().values().stream().forEach(e -> {
            nodes.add(toNode(e));
        });
        this.getDtManifestMap().values().stream().forEach(e -> {
            nodes.add(toNode(e));
        });
        this.getDtScManifestMap().values().stream().forEach(e -> {
            nodes.addAll(e.stream().map(e1 -> toNode(e1)).collect(Collectors.toSet()));
        });

        return nodes;
    }

    private Node toNode(AccManifest accManifest) {
        Node node = Node.toNode(Node.NodeType.ACC, accManifest.getAccManifestId(),
                CcState.valueOf(accManifest.getState()));
        node.setBasedManifestId(accManifest.getBasedAccManifestId());
        node.setPrevManifestId(accManifest.getPrevAccManifestId());
        node.setTagList(accManifestTagMap.getOrDefault(accManifest.getAccManifestId(), Collections.emptyList()));
        node.put("state", accManifest.getState());
        node.put("deprecated", accManifest.getIsDeprecated() == 1);
        node.put("guid", accManifest.getGuid());
        node.put("objectClassTerm", accManifest.getObjectClassTerm());
        node.put("den", accManifest.getDen());
        node.put("componentType", accManifest.getComponentType().name());
        return node;
    }

    private Node toNode(AsccpManifest asccpManifest) {
        Node node = Node.toNode(Node.NodeType.ASCCP, asccpManifest.getAsccpManifestId(),
                CcState.valueOf(asccpManifest.getState()));
        node.setLinkedManifestId(asccpManifest.getRoleOfAccManifestId());
        node.setPrevManifestId(asccpManifest.getPrevAsccpManifestId());
        node.setTagList(asccpManifestTagMap.getOrDefault(asccpManifest.getAsccpManifestId(), Collections.emptyList()));
        node.put("state", asccpManifest.getState());
        node.put("deprecated", asccpManifest.getIsDeprecated() == 1);
        node.put("guid", asccpManifest.getGuid());
        node.put("propertyTerm", asccpManifest.getPropertyTerm());
        return node;
    }

    private Node toNode(BccpManifest bccpManifest) {
        Node node = Node.toNode(Node.NodeType.BCCP, bccpManifest.getBccpManifestId(),
                CcState.valueOf(bccpManifest.getState()));
        node.setLinkedManifestId(bccpManifest.getBdtManifestId());
        node.setPrevManifestId(bccpManifest.getPrevBccpManifestId());
        node.setTagList(bccpManifestTagMap.getOrDefault(bccpManifest.getBccpManifestId(), Collections.emptyList()));
        node.put("state", bccpManifest.getState());
        node.put("deprecated", bccpManifest.getIsDeprecated() == 1);
        node.put("guid", bccpManifest.getGuid());
        node.put("propertyTerm", bccpManifest.getPropertyTerm());
        return node;
    }

    private Node toNode(AsccManifest asccManifest) {
        Node node = Node.toNode(Node.NodeType.ASCC, asccManifest.getAsccManifestId(),
                CcState.valueOf(asccManifest.getState()));
        node.setLinkedManifestId(asccManifest.getToAsccpManifestId());
        node.setPrevManifestId(asccManifest.getPrevAsccManifestId());
        node.put("state", asccManifest.getState());
        node.put("deprecated", asccManifest.getIsDeprecated() == 1);
        node.put("cardinalityMin", asccManifest.getCardinalityMin());
        node.put("cardinalityMax", asccManifest.getCardinalityMax());
        return node;
    }

    private Node toNode(BccManifest bccManifest) {
        Node node = Node.toNode(Node.NodeType.BCC, bccManifest.getBccManifestId(),
                CcState.valueOf(bccManifest.getState()));
        node.setLinkedManifestId(bccManifest.getToBccpManifestId());
        node.setPrevManifestId(bccManifest.getPrevBccManifestId());
        node.put("state", bccManifest.getState());
        node.put("deprecated", bccManifest.getIsDeprecated() == 1);
        node.put("cardinalityMin", bccManifest.getCardinalityMin());
        node.put("cardinalityMax", bccManifest.getCardinalityMax());
        node.put("entityType", bccManifest.getEntityType().name());
        return node;
    }

    private Node toNode(DtManifest dtManifest) {
        Node node = Node.toNode(Node.NodeType.DT, dtManifest.getDtManifestId(),
                CcState.valueOf(dtManifest.getState()));
        node.setPrevManifestId(dtManifest.getPrevDtManifestId());
        node.setTagList(dtManifestTagMap.getOrDefault(dtManifest.getDtManifestId(), Collections.emptyList()));
        node.put("state", dtManifest.getState());
        node.put("deprecated", dtManifest.getIsDeprecated() == 1);
        node.put("dataTypeTerm", dtManifest.getDataTypeTerm());
        node.put("den", dtManifest.getDen());
        node.put("qualifier", dtManifest.getQualifier());
        return node;
    }

    private Node toNode(DtScManifest dtScManifest) {
        Node node = Node.toNode(Node.NodeType.DT_SC, dtScManifest.getDtScManifestId(),
                CcState.valueOf(dtScManifest.getState()));
        node.setPrevManifestId(dtScManifest.getPrevDtScManifestId());
        node.put("basedDtScId", dtScManifest.getBasedDtScId());
        node.put("propertyTerm", dtScManifest.getPropertyTerm());
        node.put("objectClassTerm", dtScManifest.getObjectClassTerm());
        node.put("state", dtScManifest.getState());
        node.put("deprecated", dtScManifest.getIsDeprecated() == 1);
        node.put("representationTerm", dtScManifest.getRepresentationTerm());
        node.put("cardinalityMin", dtScManifest.getCardinalityMin());
        node.put("cardinalityMax", dtScManifest.getCardinalityMax());
        return node;
    }
}
