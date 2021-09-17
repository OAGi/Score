package org.oagi.score.gateway.http.api.graph.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.graph.data.FindUsagesRequest;
import org.oagi.score.gateway.http.api.graph.data.FindUsagesResponse;
import org.oagi.score.gateway.http.api.graph.data.Graph;
import org.oagi.score.gateway.http.api.graph.data.Node;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.BusinessInformationEntityRepository;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.code_list.CodeListReadRepository;
import org.oagi.score.repo.component.graph.CodeListGraphContext;
import org.oagi.score.repo.component.graph.CoreComponentGraphContext;
import org.oagi.score.repo.component.graph.GraphContext;
import org.oagi.score.repo.component.graph.GraphContextRepository;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class GraphService {

    @Autowired
    private CoreComponentRepository coreComponentRepository;

    @Autowired
    private GraphContextRepository graphContextRepository;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private CodeListReadRepository codeListReadRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DSLContext dslContext;

    public FindUsagesResponse findUsages(FindUsagesRequest request) {
        CoreComponentGraphContext ccGraphContext;
        Node node;
        switch (request.getType().toUpperCase()) {
            case "ACC":
                AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                        .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())))
                        .fetchOne();
                ccGraphContext = new CoreComponentGraphContext(dslContext, accManifestRecord.getReleaseId().toBigInteger());
                node = ccGraphContext.toNode(accManifestRecord);
                break;
            case "ASCCP":
                AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())))
                        .fetchOne();
                ccGraphContext = new CoreComponentGraphContext(dslContext, asccpManifestRecord.getReleaseId().toBigInteger());
                node = ccGraphContext.toNode(asccpManifestRecord);
                break;
            case "BCCP":
                BccpManifestRecord bccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())))
                        .fetchOne();
                ccGraphContext = new CoreComponentGraphContext(dslContext, bccpManifestRecord.getReleaseId().toBigInteger());
                node = ccGraphContext.toNode(bccpManifestRecord);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return ccGraphContext.findUsages(node);
    }

    public Graph getAccGraph(BigInteger accManifestId) {
        AccManifestRecord accManifest =
                coreComponentRepository.getAccManifestByManifestId(ULong.valueOf(accManifestId));
        if (accManifest == null) {
            throw new IllegalArgumentException();
        }

        CoreComponentGraphContext coreComponentGraphContext =
                graphContextRepository.buildGraphContext(accManifest);
        return buildGraph(coreComponentGraphContext, coreComponentGraphContext.toNode(accManifest), false);
    }

    public Graph getAsccpGraph(BigInteger asccpManifestId, boolean excludeUEG) {
        AsccpManifestRecord asccpManifest =
                coreComponentRepository.getAsccpManifestByManifestId(ULong.valueOf(asccpManifestId));
        if (asccpManifest == null) {
            throw new IllegalArgumentException();
        }

        CoreComponentGraphContext coreComponentGraphContext =
                graphContextRepository.buildGraphContext(asccpManifest);
        return buildGraph(coreComponentGraphContext, coreComponentGraphContext.toNode(asccpManifest), excludeUEG);
    }

    public Graph getBccpGraph(BigInteger bccpManifestId) {
        BccpManifestRecord bccpManifest =
                coreComponentRepository.getBccpManifestByManifestId(ULong.valueOf(bccpManifestId));
        if (bccpManifest == null) {
            throw new IllegalArgumentException();
        }

        CoreComponentGraphContext coreComponentGraphContext =
                graphContextRepository.buildGraphContext(bccpManifest);
        return buildGraph(coreComponentGraphContext, coreComponentGraphContext.toNode(bccpManifest), false);
    }

    public Graph getBdtGraph(BigInteger bdtManifestId) {
        DtManifestRecord bdtManifest =
                coreComponentRepository.getBdtManifestByManifestId(ULong.valueOf(bdtManifestId));
        if (bdtManifest == null) {
            throw new IllegalArgumentException();
        }

        CoreComponentGraphContext coreComponentGraphContext =
                graphContextRepository.buildGraphContext(bdtManifest);
        return buildGraph(coreComponentGraphContext, coreComponentGraphContext.toNode(bdtManifest), false);
    }

    public Graph getBieGraph(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId) {
        boolean excludeUEG = sessionService.getAppUser(user).isDeveloper();
        BigInteger asccpManifestId = bieRepository.getAsccpManifestIdByTopLevelAsbiepId(topLevelAsbiepId);
        return getAsccpGraph(asccpManifestId, excludeUEG);
    }

    public AsccpManifestRecord getUpliftBie(AuthenticatedPrincipal user, BigInteger topLevelAsbiepId, BigInteger targetReleaseId) {
        return bieRepository.getAsccpManifestIdByTopLevelAsbiepIdAndReleaseId(topLevelAsbiepId, targetReleaseId);
    }

    public Graph getCodeListGraph(BigInteger codeListManifestId) {
        CodeListManifestRecord codeListManifestRecord =
                codeListReadRepository.getCodeListManifestByManifestId(codeListManifestId);
        if (codeListManifestRecord == null) {
            throw new IllegalArgumentException();
        }

        CodeListGraphContext codeListGraphContext =
                graphContextRepository.buildGraphContext(codeListManifestRecord);
        return buildGraph(codeListGraphContext, codeListGraphContext.toNode(codeListManifestRecord), false);
    }

    private Graph buildGraph(GraphContext graphContext, Node root, boolean excludeUEG) {
        Queue<Node> manifestQueue = new LinkedList<>();
        manifestQueue.add(root);

        Graph graph = new Graph();

        while (!manifestQueue.isEmpty()) {
            Node node = manifestQueue.poll();
            if (!graph.addNode(node)) {
                continue;
            }

            List<Node> children = graphContext.findChildren(node, excludeUEG);
            if (children.isEmpty()) {
                continue;
            }

            graph.addEdges(node, children);
            if (Node.NodeType.BDT == node.getType()) {
                children.stream().forEach(e -> graph.addNode(e));
            } else {
                manifestQueue.addAll(children);
            }
        }

        return graph;
    }
}
