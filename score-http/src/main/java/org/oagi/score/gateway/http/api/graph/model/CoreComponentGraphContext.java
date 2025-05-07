package org.oagi.score.gateway.http.api.graph.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Data
public class CoreComponentGraphContext implements GraphContext {

    private CcDocument ccDocument;

    public CoreComponentGraphContext(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
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
                        List<Node> accUsages = ccDocument.getAccListByBasedAccManifestId((AccManifestId) currentNode.getManifestId())
                                .stream().map(e -> toNode(e)).collect(Collectors.toList());

                        accUsages.forEach(_node -> {
                            graph.addNode(_node);
                        });

                        graph.addEdges(currentNode, accUsages);
                        queue.addAll(accUsages);

                        break;
                }
            }

            List<Node> asccpUsages = ccDocument.getAsccpListByRoleOfAccManifestId((AccManifestId) node.getManifestId())
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
            List<Node> asccpUsages = ccDocument.getAccListByToAsccpManifestId((AsccpManifestId) node.getManifestId())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            asccpUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, asccpUsages);
        } else if (node.getType() == Node.NodeType.BCCP) {
            List<Node> bccpUsages = ccDocument.getAccListByToBccpManifestId((BccpManifestId) node.getManifestId())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            bccpUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, bccpUsages);
        } else if (node.getType() == Node.NodeType.DT) {
            List<Node> dtUsages = ccDocument.getBccpListByDtManifestId((DtManifestId) node.getManifestId())
                    .stream().map(e -> toNode(e)).collect(Collectors.toList());

            dtUsages.forEach(_node -> {
                graph.addNode(_node);
            });
            graph.addEdges(node, dtUsages);
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
                    AccSummaryRecord basedAcc = ccDocument.getAcc((AccManifestId) node.getBasedManifestId());
                    children.add(toNode(basedAcc));
                }

                List<SeqKeySupportable> assocs = new ArrayList();

                if (excludeUEG) {
                    if (ccDocument.getAcc((AccManifestId) node.getManifestId())
                            .componentType().equals(OagisComponentType.UserExtensionGroup)) {
                        return children;
                    }
                }

                List<AsccSummaryRecord> asccList = ccDocument.getAsccListByFromAccManifestId((AccManifestId) node.getManifestId());
                if (asccList == null || asccList.isEmpty()) {
                    asccList = ccDocument.getAsccListByFromAccManifestId((AccManifestId) node.getPrevManifestId());
                }
                assocs.addAll(asccList);

                List<BccSummaryRecord> bccList = ccDocument.getBccListByFromAccManifestId((AccManifestId) node.getManifestId());
                if (bccList == null || bccList.isEmpty()) {
                    bccList = ccDocument.getBccListByFromAccManifestId((AccManifestId) node.getPrevManifestId());
                }
                assocs.addAll(bccList);

                assocs = SeqKeyHandler.sort(assocs);
                children.addAll(
                        assocs.stream().map(e -> {
                            if (e instanceof AsccSummaryRecord) {
                                return toNode((AsccSummaryRecord) e);
                            } else {
                                return toNode((BccSummaryRecord) e);
                            }
                        }).collect(Collectors.toList())
                );

                return children;

            case ASCC:
                AsccpSummaryRecord asccp = ccDocument.getAsccp((AsccpManifestId) node.getLinkedManifestId());
                if (asccp == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(asccp));

            case BCC:
                BccpSummaryRecord bccp = ccDocument.getBccp((BccpManifestId) node.getLinkedManifestId());
                if (bccp == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(bccp));

            case ASCCP:
                AccSummaryRecord acc = ccDocument.getAcc((AccManifestId) node.getLinkedManifestId());
                if (acc == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(toNode(acc));

            case BCCP:
                DtSummaryRecord dt = ccDocument.getDt((DtManifestId) node.getLinkedManifestId());
                return (dt != null) ? Arrays.asList(toNode(dt)) : Collections.emptyList();

            case DT:
                List<DtScSummaryRecord> dtScList = ccDocument.getDtScListByDtManifestId((DtManifestId) node.getManifestId());
                if (dtScList == null || dtScList.isEmpty()) {
                    dtScList = ccDocument.getDtScListByDtManifestId((DtManifestId) node.getPrevManifestId());
                }

                return dtScList.stream()
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

    public Node toNode(AccSummaryRecord acc) {
        Node node = Node.toNode(Node.NodeType.ACC, acc.accManifestId(), acc.state());
        if (acc.basedAccManifestId() != null) {
            node.setBasedManifestId(acc.basedAccManifestId());
        }
        node.setPrevManifestId(acc.prevAccManifestId());
        node.setTagList(ccDocument.getTagListByAccManifestId(acc.accManifestId()));
        node.put("state", acc.state());
        node.put("deprecated", acc.deprecated());
        node.put("guid", acc.guid());
        node.put("objectClassTerm", acc.objectClassTerm());
        node.put("den", acc.den());
        node.put("componentType", acc.componentType().name());
        return node;
    }

    public Node toNode(AsccpSummaryRecord asccp) {
        Node node = Node.toNode(Node.NodeType.ASCCP, asccp.asccpManifestId(), asccp.state());
        node.setLinkedManifestId(asccp.roleOfAccManifestId());
        node.setPrevManifestId(asccp.prevAsccpManifestId());
        node.setTagList(ccDocument.getTagListByAsccpManifestId(asccp.asccpManifestId()));
        node.put("state", asccp.state());
        node.put("deprecated", asccp.deprecated());
        node.put("guid", asccp.guid());
        node.put("propertyTerm", asccp.propertyTerm());
        node.put("den", asccp.den());
        return node;
    }

    public Node toNode(BccpSummaryRecord bccp) {
        Node node = Node.toNode(Node.NodeType.BCCP, bccp.bccpManifestId(), bccp.state());
        node.setLinkedManifestId(bccp.dtManifestId());
        node.setPrevManifestId(bccp.prevBccpManifestId());
        node.setTagList(ccDocument.getTagListByBccpManifestId(bccp.bccpManifestId()));
        node.put("state", bccp.state());
        node.put("deprecated", bccp.deprecated());
        node.put("guid", bccp.guid());
        node.put("propertyTerm", bccp.propertyTerm());
        node.put("den", bccp.den());
        return node;
    }

    public Node toNode(AsccSummaryRecord ascc) {
        Node node = Node.toNode(Node.NodeType.ASCC, ascc.asccManifestId(), ascc.state());
        node.setLinkedManifestId(ascc.toAsccpManifestId());
        node.setPrevManifestId(ascc.prevAsccManifestId());
        node.put("state", ascc.state());
        node.put("deprecated", ascc.deprecated());
        node.put("cardinalityMin", ascc.cardinality().min());
        node.put("cardinalityMax", ascc.cardinality().max());
        node.put("den", ascc.den());
        return node;
    }

    public Node toNode(BccSummaryRecord bcc) {
        Node node = Node.toNode(Node.NodeType.BCC, bcc.bccManifestId(), bcc.state());
        node.setLinkedManifestId(bcc.toBccpManifestId());
        node.setPrevManifestId(bcc.prevBccManifestId());
        node.put("state", bcc.state());
        node.put("deprecated", bcc.deprecated());
        node.put("cardinalityMin", bcc.cardinality().min());
        node.put("cardinalityMax", bcc.cardinality().max());
        node.put("entityType", bcc.entityType().name());
        node.put("den", bcc.den());
        return node;
    }

    public Node toNode(DtSummaryRecord dt) {
        Node node = Node.toNode(Node.NodeType.DT, dt.dtManifestId(), dt.state());
        node.setPrevManifestId(dt.prevDtManifestId());
        node.setTagList(ccDocument.getTagListByDtManifestId(dt.dtManifestId()));
        node.put("state", dt.state());
        node.put("deprecated", dt.deprecated());
        node.put("dataTypeTerm", dt.dataTypeTerm());
        node.put("den", dt.den());
        node.put("qualifier", dt.qualifier());
        return node;
    }

    public Node toNode(DtScSummaryRecord dtSc) {
        Node node = Node.toNode(Node.NodeType.DT_SC, dtSc.dtScManifestId(), dtSc.state());
        node.setPrevManifestId(dtSc.prevDtScManifestId());
        if (dtSc.basedDtScManifestId() != null) {
            node.put("basedDtScManifestId", dtSc.basedDtScManifestId());
        }
        node.put("propertyTerm", dtSc.propertyTerm());
        node.put("objectClassTerm", dtSc.objectClassTerm());
        node.put("state", dtSc.state());
        node.put("deprecated", dtSc.deprecated());
        node.put("representationTerm", dtSc.representationTerm());
        node.put("cardinalityMin", dtSc.cardinality().min());
        node.put("cardinalityMax", dtSc.cardinality().max());
        return node;
    }
}
