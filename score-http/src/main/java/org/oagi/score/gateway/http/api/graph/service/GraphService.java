package org.oagi.score.gateway.http.api.graph.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.BusinessInformationEntityRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.graph.model.*;
import org.oagi.score.gateway.http.api.graph.repository.GraphContextRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.service.TagQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@Transactional(readOnly = true)
public class GraphService {

    @Autowired
    private GraphContextRepository graphContextRepository;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private TagQueryService tagQueryService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RepositoryFactory repositoryFactory;

    public FindUsagesResponse findUsages(ScoreUser requester, FindUsagesRequest request) {
        CcDocument ccDocument;
        CoreComponentGraphContext ccGraphContext;
        Node node;
        switch (request.type()) {
            case ACC:
                AccManifestId accManifestId = (AccManifestId) request.manifestId();
                AccSummaryRecord acc = repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId);
                ccDocument = new CcDocumentImpl(requester, repositoryFactory, acc.release().releaseId());
                ccGraphContext = new CoreComponentGraphContext(ccDocument);
                node = ccGraphContext.toNode(ccDocument.getAcc(accManifestId));
                break;
            case ASCCP:
                AsccpManifestId asccpManifestId = (AsccpManifestId) request.manifestId();
                AsccpSummaryRecord asccp = repositoryFactory.asccpQueryRepository(requester).getAsccpSummary(asccpManifestId);
                ccDocument = new CcDocumentImpl(requester, repositoryFactory, asccp.release().releaseId());
                ccGraphContext = new CoreComponentGraphContext(ccDocument);
                node = ccGraphContext.toNode(ccDocument.getAsccp(asccpManifestId));
                break;
            case BCCP:
                BccpManifestId bccpManifestId = (BccpManifestId) request.manifestId();
                BccpSummaryRecord bccp = repositoryFactory.bccpQueryRepository(requester).getBccpSummary(bccpManifestId);
                ccDocument = new CcDocumentImpl(requester, repositoryFactory, bccp.release().releaseId());
                ccGraphContext = new CoreComponentGraphContext(ccDocument);
                node = ccGraphContext.toNode(ccDocument.getBccp(bccpManifestId));
                break;
            case DT:
                DtManifestId dtManifestId = (DtManifestId) request.manifestId();
                DtSummaryRecord dt = repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId);
                ccDocument = new CcDocumentImpl(requester, repositoryFactory, dt.release().releaseId());
                ccGraphContext = new CoreComponentGraphContext(ccDocument);
                node = ccGraphContext.toNode(ccDocument.getDt(dtManifestId));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return ccGraphContext.findUsages(node);
    }

    public Graph getAccGraph(ScoreUser requester, AccManifestId accManifestId) {
        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = accQuery.getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException();
        }

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, acc.release().releaseId());
        CoreComponentGraphContext ccGraphContext = new CoreComponentGraphContext(ccDocument);
        return buildGraph(ccGraphContext, ccGraphContext.toNode(acc), false);
    }

    public Graph getAsccpGraph(ScoreUser requester, AsccpManifestId asccpManifestId, boolean excludeUEG) {
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = asccpQuery.getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            throw new IllegalArgumentException();
        }

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, asccp.release().releaseId());
        CoreComponentGraphContext ccGraphContext = new CoreComponentGraphContext(ccDocument);
        return buildGraph(ccGraphContext, ccGraphContext.toNode(asccp), excludeUEG);
    }

    public Graph getBccpGraph(ScoreUser requester, BccpManifestId bccpManifestId) {
        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = bccpQuery.getBccpSummary(bccpManifestId);
        if (bccp == null) {
            throw new IllegalArgumentException();
        }

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, bccp.release().releaseId());
        CoreComponentGraphContext ccGraphContext = new CoreComponentGraphContext(ccDocument);
        return buildGraph(ccGraphContext, ccGraphContext.toNode(bccp), false);
    }

    public Graph getDtGraph(ScoreUser requester, DtManifestId dtManifestId) {
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = dtQuery.getDtSummary(dtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException();
        }

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, dt.release().releaseId());
        CoreComponentGraphContext ccGraphContext = new CoreComponentGraphContext(ccDocument);
        return buildGraph(ccGraphContext, ccGraphContext.toNode(dt), false);
    }

    public Graph getBieGraph(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        boolean excludeUEG = requester.isDeveloper();

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiepSummary = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        AsbiepSummaryRecord asbiepSummary = asbiepQuery.getAsbiepSummary(topLevelAsbiepSummary.asbiepId());

        AsccpManifestId asccpManifestId = asbiepSummary.basedAsccpManifestId();
        return getAsccpGraph(requester, asccpManifestId, excludeUEG);
    }

    public AsccpSummaryRecord getUpliftBie(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiepSummary = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        AsbiepSummaryRecord asbiepSummary = asbiepQuery.getAsbiepSummary(topLevelAsbiepSummary.asbiepId());

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccpSummary = asccpQuery.getAsccpSummary(asbiepSummary.basedAsccpManifestId());

        return asccpQuery.getAsccpSummary(asccpSummary.asccpId(), targetReleaseId);
    }

    public Graph getCodeListGraph(ScoreUser requester, CodeListManifestId codeListManifestId) {
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        CodeListSummaryRecord codeList = codeListQuery.getCodeListSummary(codeListManifestId);
        if (codeList == null) {
            throw new IllegalArgumentException();
        }

        CodeListGraphContext codeListGraphContext = new CodeListGraphContext(requester, repositoryFactory);
        return buildGraph(codeListGraphContext, codeListGraphContext.toNode(codeList), false);
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
            if (Node.NodeType.DT == node.getType()) {
                children.stream().forEach(e -> graph.addNode(e));
            } else {
                manifestQueue.addAll(children);
            }
        }

        return graph;
    }
}
