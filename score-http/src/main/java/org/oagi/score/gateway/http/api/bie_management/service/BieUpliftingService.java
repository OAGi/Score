package org.oagi.score.gateway.http.api.bie_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.*;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.WrappedAsbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.WrappedAsbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.WrappedBbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.WrappedBbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.BieCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.CcMatchingScore;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.*;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable.extractManifestId;
import static org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable.getLastTag;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.RELEASE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BieUpliftingService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BieCommandRepository command(ScoreUser requester) {
        return repositoryFactory.bieCommandRepository(requester);
    };

    private BieQueryRepository query(ScoreUser requester) {
        return repositoryFactory.bieQueryRepository(requester);
    }

    @Autowired
    private BieReadService bieReadService;

    @Autowired
    private CcMatchingService ccMatchingService;

    @Autowired
    private DSLContext dslContext;

    private class Association {

        private String parentPath;
        private CcAssociation ccAssociation;

        public Association(String parentPath, CcAssociation ccAssociation) {
            this.parentPath = parentPath;
            int ch = parentPath.lastIndexOf('>');
            if (ch >= 0) {
                if (!parentPath.substring(ch).contains("ACC")) {
                    throw new IllegalStateException();
                }
            }
            this.ccAssociation = ccAssociation;
        }

        public String getParentPath() {
            return parentPath;
        }

        public String getPath() {
            return parentPath + ">" + ((this.ccAssociation.isAscc()) ?
                    "ASCC-" + ((AsccSummaryRecord) this.ccAssociation).asccManifestId() :
                    "BCC-" + ((BccSummaryRecord) this.ccAssociation).bccManifestId());
        }

        public CcAssociation getCcAssociation() {
            return ccAssociation;
        }

        public boolean isMatched(CcAssociation ccAssociation) {
            return this.ccAssociation.equals(ccAssociation);
        }
    }

    private List<Association> getAssociationsRegardingBases(String path, CcDocument ccDocument, AccSummaryRecord acc) {
        Stack<AccSummaryRecord> accStack = new Stack();
        while (acc != null) {
            accStack.push(acc);
            acc = ccDocument.getAcc(acc.basedAccManifestId());
        }

        List<Association> associations = new ArrayList();
        while (!accStack.isEmpty()) {
            String parentPath = path + ">" + String.join(">", accStack.stream()
                    .map(e -> "ACC-" + e.accManifestId()).collect(Collectors.toList()));
            acc = accStack.pop();
            associations.addAll(getAssociationsRegardingGroup(parentPath, ccDocument, acc));
        }

        return associations;
    }

    private List<Association> getAssociationsRegardingGroup(String parentPath, CcDocument ccDocument, AccSummaryRecord acc) {
        Collection<CcAssociation> ccAssociations = ccDocument.getAssociations(acc);
        List<Association> associations = new ArrayList();
        for (CcAssociation ccAssociation : ccAssociations) {
            if (ccAssociation.isAscc()) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) ccAssociation;
                AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                AccSummaryRecord roleOfAcc = ccDocument.getAcc(asccp.roleOfAccManifestId());
                if (roleOfAcc.isGroup()) {
                    associations.addAll(
                            getAssociationsRegardingGroup(
                                    String.join(">",
                                            Arrays.asList(parentPath,
                                                    "ASCC-" + ascc.asccManifestId(),
                                                    "ASCCP-" + asccp.asccpManifestId(),
                                                    "ACC-" + roleOfAcc.accManifestId())
                                    ),
                                    ccDocument, roleOfAcc)
                    );
                    continue;
                }
            }

            associations.add(new Association(parentPath, ccAssociation));
        }

        return associations;
    }

    private class BieDiff implements BieVisitor {

        private List<BieUpliftingListener> listeners = new ArrayList();

        private BieDocument sourceBieDocument;
        private CcDocument targetCcDocument;
        private AsccpManifestId targetAsccpManifestId;

        private Queue<AsccpSummaryRecord> targetAsccpQueue = new LinkedBlockingQueue();
        private Queue<AccSummaryRecord> targetAccQueue = new LinkedBlockingQueue();
        private Bbie previousBbie;
        private Queue<BccpSummaryRecord> targetBccpQueue = new LinkedBlockingQueue();

        private String currentSourcePath;
        private String currentTargetPath;

        private Map<AbieId, List<Association>> abieSourceAssociationsMap = new HashMap();
        private Map<AbieId, List<Association>> abieTargetAssociationsMap = new HashMap();
        private Map<BbieId, List<DtScSummaryRecord>> bbieTargetDtScMap = new HashMap();

        BieDiff(BieDocument sourceBieDocument, CcDocument targetCcDocument,
                AsccpManifestId targetAsccpManifestId) {
            this.sourceBieDocument = sourceBieDocument;
            this.targetCcDocument = targetCcDocument;
            this.targetAsccpManifestId = targetAsccpManifestId;
        }

        public void addListener(BieUpliftingListener listener) {
            this.listeners.add(listener);
        }

        public void diff() {
            sourceBieDocument.accept(this);
        }

        @Override
        public void visitStart(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {
            AsccpSummaryRecord targetAsccp = targetCcDocument.getAsccp(targetAsccpManifestId);
            targetAsccpQueue.offer(targetAsccp);
        }

        @Override
        public void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {

        }

        @Override
        public void visitAbie(Abie abie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            // reused bie case;
            if (abie == null) {
                targetAccQueue.poll();
                return;
            }
            AccSummaryRecord sourceAcc = sourceCcDocument.getAcc(abie.getBasedAccManifestId());
            List<Association> sourceAssociations =
                    getAssociationsRegardingBases(currentSourcePath, sourceCcDocument, sourceAcc);
            abieSourceAssociationsMap.put(abie.getAbieId(), sourceAssociations);

            AccSummaryRecord targetAcc = targetAccQueue.poll();
            if (targetAcc != null) { // found matched acc
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(currentTargetPath, targetCcDocument, targetAcc);
                abieTargetAssociationsMap.put(abie.getAbieId(), targetAssociations);
            }
        }

        @Override
        public void visitAsbie(Asbie asbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            AsccSummaryRecord sourceAscc = sourceCcDocument.getAscc(asbie.getBasedAsccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceAscc)).findAny().orElse(null);

            List<Association> targetAssociations =
                    abieTargetAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isAscc())
                    .map(e -> ccMatchingService.score(
                            sourceCcDocument,
                            sourceAssociation,
                            targetCcDocument,
                            e,
                            (ccDocument, association) -> ccDocument.getAscc(((AsccSummaryRecord) association.getCcAssociation()).asccManifestId())))
                    .max(Comparator.comparing(CcMatchingScore::getScore))
                    .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedAsbie(asbie,
                            (AsccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedAsbie(asbie,
                            (AsccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition(),
                            (AsccSummaryRecord) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = targetAssociation.getPath();

                AsccpSummaryRecord toAsccp = targetCcDocument.getAsccp(
                        ((AsccSummaryRecord) targetAssociation.getCcAssociation()).toAsccpManifestId());
                targetAsccpQueue.offer(toAsccp);
            }
        }

        @Override
        public void visitBbie(Bbie bbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccSummaryRecord sourceBcc = sourceCcDocument.getBcc(bbie.getBasedBccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceBcc)).findAny().orElse(null);

            List<Association> targetAssociations =
                    abieTargetAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isBcc())
                    .map(e -> ccMatchingService.score(
                            sourceCcDocument,
                            sourceAssociation,
                            targetCcDocument,
                            e,
                            (ccDocument, association) -> ccDocument.getBcc(((BccSummaryRecord) association.getCcAssociation()).bccManifestId())))
                    .max(Comparator.comparing(CcMatchingScore::getScore))
                    .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedBbie(bbie,
                            (BccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbie(bbie,
                            (BccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition(),
                            (BccSummaryRecord) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = targetAssociation.getPath();

                BccpSummaryRecord toBccp = targetCcDocument.getBccp(
                        ((BccSummaryRecord) targetAssociation.getCcAssociation()).toBccpManifestId());
                this.previousBbie = bbie;
                targetBccpQueue.offer(toBccp);
            }
        }

        @Override
        public void visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            // reused bie case;
            if (asbiep == null) {
                targetAsccpQueue.poll();
                return;
            }
            AsccpSummaryRecord sourceAsccp = sourceCcDocument.getAsccp(asbiep.getBasedAsccpManifestId());

            currentSourcePath = (hasLength(currentSourcePath)) ?
                    currentSourcePath + ">" + "ASCCP-" + sourceAsccp.asccpManifestId() :
                    "ASCCP-" + sourceAsccp.asccpManifestId();

            AsccpSummaryRecord targetAsccp = targetAsccpQueue.poll();
            if (targetAsccp != null) { // found matched asccp
                AccSummaryRecord targetRoleOfAcc = targetCcDocument.getAcc(targetAsccp.roleOfAccManifestId());
                targetAccQueue.offer(targetRoleOfAcc);
                currentTargetPath = (hasLength(currentTargetPath)) ?
                        currentTargetPath + ">" + "ASCCP-" + targetAsccp.asccpManifestId() :
                        "ASCCP-" + targetAsccp.asccpManifestId();
            }
        }

        @Override
        public void visitBbiep(Bbiep bbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccpSummaryRecord sourceBccp = sourceCcDocument.getBccp(bbiep.getBasedBccpManifestId());
            DtSummaryRecord sourceDt = sourceCcDocument.getDt(sourceBccp.dtManifestId());
            currentSourcePath = currentSourcePath + ">" + "BCCP-" + sourceBccp.bccpManifestId() + ">" +
                    "DT-" + sourceDt.dtManifestId();

            BccpSummaryRecord targetBccp = targetBccpQueue.poll();
            if (targetBccp != null) {
                DtManifestId targetBdtManifestId = targetBccp.dtManifestId();
                DtSummaryRecord targetDt = targetCcDocument.getDt(targetBdtManifestId);
                bbieTargetDtScMap.put(previousBbie.getBbieId(),
                        targetCcDocument.getDtScList(targetDt.dtManifestId()));
                currentTargetPath = currentTargetPath + ">" + "BCCP-" + targetBccp.bccpManifestId() + ">" +
                        "DT-" + targetDt.dtManifestId();
            }
        }

        @Override
        public void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScSummaryRecord sourceDtSc = sourceCcDocument.getDtSc(bbieSc.getBasedDtScManifestId());

            String sourcePath = currentSourcePath + ">" + "DT_SC-" + sourceDtSc.dtScManifestId();
            CcMatchingScore matchingScore =
                    bbieTargetDtScMap.getOrDefault(bbieSc.getBbieId(), Collections.emptyList()).stream()
                            .map(e -> ccMatchingService.score(
                                    sourceCcDocument,
                                    sourceDtSc,
                                    targetCcDocument,
                                    e,
                                    (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest.dtScManifestId())))
                            .max(Comparator.comparing(CcMatchingScore::getScore))
                            .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedBbieSc(bbieSc, sourceDtSc, sourcePath, bbieSc.getDefinition());
                });
            } else {
                DtScSummaryRecord targetDtSc = (DtScSummaryRecord) matchingScore.getTarget();
                String targetPath = currentTargetPath + ">" + "DT_SC-" + targetDtSc.dtScManifestId();

                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbieSc(bbieSc,
                            sourceDtSc, sourcePath, bbieSc.getDefinition(),
                            targetDtSc, targetPath);
                });
            }
        }
    }

    private ReleaseRecord getReleaseRecordByTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        // @TODO: it will be replaced with ReleaseQueryRepository
        return dslContext.select(RELEASE.fields())
                .from(RELEASE)
                .join(TOP_LEVEL_ASBIEP).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchOneInto(ReleaseRecord.class);
    }

    private ReleaseRecord getReleaseRecordByReleaseId(ReleaseId releaseId) {
        // @TODO: it will be replaced with ReleaseQueryRepository
        return dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId.value())))
                .fetchOneInto(ReleaseRecord.class);
    }

    public AsccpSummaryRecord findTargetAsccpManifest(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {

        return findTargetAsccp(requester, topLevelAsbiepId, targetReleaseId).getKey();
    }

    private Pair<AsccpSummaryRecord, BieDocument> findTargetAsccp(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {
        ReleaseRecord sourceRelease = getReleaseRecordByTopLevelAsbiepId(topLevelAsbiepId);
        ReleaseRecord targetRelease = getReleaseRecordByReleaseId(targetReleaseId);

        if (sourceRelease.getReleaseId().toBigInteger().compareTo(targetRelease.getReleaseId().toBigInteger()) >= 0) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(requester, topLevelAsbiepId);
        AsccpSummaryRecord findNextAsccp = repositoryFactory.asccpQueryRepository(requester)
                .findNextAsccpManifest(
                        sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId(),
                        targetReleaseId
                );

        if (findNextAsccp == null) {
            throw new ScoreDataAccessException("Unable to find the target ASCCP.");
        }

        return Pair.of(findNextAsccp, sourceBieDocument);
    }

    public AnalysisBieUpliftingResponse analysisBieUplifting(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {

        AnalysisBieUpliftingResponse response = new AnalysisBieUpliftingResponse();

        var targetAsccp = findTargetAsccp(requester, topLevelAsbiepId, targetReleaseId);

        BieDocument sourceBieDocument = targetAsccp.getValue();
        AsccpManifestId targetAsccpManifestId = targetAsccp.getKey().asccpManifestId();

        CcDocument targetCcDocument = new CcDocumentImpl(requester, repositoryFactory, targetReleaseId);

        BieDiff bieDiff = new BieDiff(sourceBieDocument, targetCcDocument, targetAsccpManifestId);
        bieDiff.addListener(response);
        bieDiff.diff();

        return response;
    }

    private class BieUpliftingHandler implements BieVisitor {

        private ScoreUser requester;
        private List<BusinessContextId> bizCtxIds;
        private BieUpliftingCustomMappingTable customMappingTable;

        private BieDocument sourceBieDocument;
        private CcDocument targetCcDocument;
        private AsccpManifestId targetAsccpManifestId;

        private Queue<AsccpSummaryRecord> targetAsccpQueue = new LinkedBlockingQueue();
        private Queue<AccSummaryRecord> targetAccQueue = new LinkedBlockingQueue();
        private Bbie previousBbie;
        private Queue<BccpSummaryRecord> targetBccpQueue = new LinkedBlockingQueue();

        private String currentSourcePath;
        private String currentTargetPath;

        private Map<AbieId, List<Association>> abieSourceAssociationsMap = new HashMap();
        private Map<AbieId, List<Association>> abieTargetAssociationsMap = new HashMap();
        private Map<BbieId, List<DtScSummaryRecord>> bbieTargetDtScMap = new HashMap();

        private List<XbtSummaryRecord> sourceXbtList;
        private List<XbtSummaryRecord> targetXbtList;

        private List<CodeListSummaryRecord> sourceCodeListList;
        private List<CodeListSummaryRecord> targetCodeListList;

        private List<AgencyIdListSummaryRecord> sourceAgencyIdListList;
        private List<AgencyIdListSummaryRecord> targetAgencyIdListList;

        private Map<DtAwdPriId, DtAwdPriSummaryRecord> sourceDtAwdPriMap = new HashMap();
        private Map<DtId, List<DtAwdPriSummaryRecord>> targetDtAwdPriByDtIdMap = new HashMap();
        private Map<DtScAwdPriId, DtScAwdPriSummaryRecord> sourceDtScAwdPriMap = new HashMap();
        private Map<DtScId, List<DtScAwdPriSummaryRecord>> targetDtScAwdPriByDtScIdMap = new HashMap();

        private Map<AsbiepId, WrappedAsbiep> asbiepMap;
        private Map<AbieId, WrappedAsbiep> roleOfAbieToAsbiepMap;
        private Map<AbieId, Abie> abieIdToAbieMap;
        private Map<AsbiepId, List<WrappedAsbie>> toAsbiepToAsbieMap;
        private Map<BbiepId, WrappedBbie> toBbiepToBbieMap;
        private Map<BbieId, Bbie> bbieMap;
        private List<WrappedBbieSc> bbieScList;

        private TopLevelAsbiepId targetTopLevelAsbiepId;

        BieUpliftingHandler(ScoreUser requester, List<BusinessContextId> bizCtxIds,
                            BieUpliftingCustomMappingTable customMappingTable,
                            BieDocument sourceBieDocument, CcDocument targetCcDocument,
                            AsccpManifestId targetAsccpManifestId,
                            Map<DtAwdPriId, DtAwdPriSummaryRecord> sourceDtAwdPriMap,
                            Map<DtId, List<DtAwdPriSummaryRecord>> targetDtAwdPriByDtIdMap,
                            Map<DtScAwdPriId, DtScAwdPriSummaryRecord> sourceDtScAwdPriMap,
                            Map<DtScId, List<DtScAwdPriSummaryRecord>> targetDtScAwdPriByDtScIdMap,
                            List<XbtSummaryRecord> sourceXbtList,
                            List<XbtSummaryRecord> targetXbtList,
                            List<CodeListSummaryRecord> sourceCodeListList,
                            List<CodeListSummaryRecord> targetCodeListList,
                            List<AgencyIdListSummaryRecord> sourceAgencyIdListList,
                            List<AgencyIdListSummaryRecord> targetAgencyIdListList) {

            this.requester = requester;
            this.bizCtxIds = bizCtxIds;
            this.customMappingTable = customMappingTable;

            this.sourceBieDocument = sourceBieDocument;
            this.targetCcDocument = targetCcDocument;
            this.targetAsccpManifestId = targetAsccpManifestId;

            this.asbiepMap = new HashMap();
            this.roleOfAbieToAsbiepMap = new HashMap();
            this.abieIdToAbieMap = new HashMap();
            this.toAsbiepToAsbieMap = new HashMap();
            this.toBbiepToBbieMap = new HashMap();
            this.bbieMap = new HashMap();
            this.bbieScList = new ArrayList();

            this.sourceDtAwdPriMap = sourceDtAwdPriMap;
            this.targetDtAwdPriByDtIdMap = targetDtAwdPriByDtIdMap;
            this.sourceDtScAwdPriMap = sourceDtScAwdPriMap;
            this.targetDtScAwdPriByDtScIdMap = targetDtScAwdPriByDtScIdMap;

            this.sourceXbtList = sourceXbtList;
            this.targetXbtList = targetXbtList;

            this.sourceCodeListList = sourceCodeListList;
            this.targetCodeListList = targetCodeListList;

            this.sourceAgencyIdListList = sourceAgencyIdListList;
            this.targetAgencyIdListList = targetAgencyIdListList;
        }

        public TopLevelAsbiepId uplift() {
            this.sourceBieDocument.accept(this);
            return targetTopLevelAsbiepId;
        }

        @Override
        public void visitStart(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {
            AsccpSummaryRecord targetAsccp = targetCcDocument.getAsccp(targetAsccpManifestId);
            targetAsccpQueue.offer(targetAsccp);
        }

        private String extractAbiePath(String assocPath) {
            Stack<String> stack = new Stack();
            stack.addAll(Arrays.asList(assocPath.split(">")));

            stack.pop(); // drop the assoc tag.
            while (!stack.isEmpty()) {
                String tag = stack.pop();
                if (tag.startsWith("ACC") && !stack.lastElement().startsWith("ACC")) { // find the last tag of ACCs

                    // Issue #1287
                    // ABIE path does not end with a group ACC.
                    AccManifestId accManifestId = new AccManifestId(extractManifestId(tag));
                    AccSummaryRecord acc = this.targetCcDocument.getAcc(accManifestId);
                    if (acc.isGroup()) {
                        continue;
                    }

                    stack.push(tag);
                    return String.join(">", stack);
                }
            }

            throw new IllegalStateException();
        }

        private String extractBbiePath(String bbieScPath) {
            Stack<String> stack = new Stack();
            stack.addAll(Arrays.asList(bbieScPath.split(">")));

            stack.pop(); // drop 'BDT_SC' tag
            stack.pop(); // drop 'BDT' tag
            stack.pop(); // drop 'BCCP' tag

            return String.join(">", stack);
        }

        @Override
        public void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {
            List<Abie> emptySourceAbieList = new ArrayList();
            List<WrappedAsbie> emptySourceAsbieList = new ArrayList();
            List<WrappedBbie> emptySourceBbieList = new ArrayList();
            List<WrappedAsbiep> emptySourceAsbiepList = new ArrayList();
            List<Bbiep> emptySourceBbiepList = new ArrayList();
            List<WrappedBbieSc> emptySourceBbieScList = new ArrayList();

            Function<String, Abie> getAbieIfExist = (path) -> {
                Abie abie = abieIdToAbieMap.values().stream()
                        .filter(e -> e.getPath().equals(path))
                        .findAny().orElse(null);
                if (abie == null) {
                    abie = emptySourceAbieList.stream()
                            .filter(e -> e.getPath().equals(path))
                            .findAny().orElse(null);
                }
                return abie;
            };

            Function<String, Bbie> getBbieIfExist = (path) -> {
                Bbie bbie = bbieMap.values().stream()
                        .filter(e -> e.getPath().equals(path))
                        .findAny().orElse(null);
                if (bbie == null) {
                    bbie = emptySourceBbieList.stream()
                            .map(e -> e.getBbie())
                            .filter(e -> e.getPath().equals(path))
                            .findAny().orElse(null);
                }
                return bbie;
            };

            this.customMappingTable.getMappingList().stream()
                    .filter(e -> !hasLength(e.getSourcePath()))
                    .forEach(mapping -> {
                        switch (mapping.getBieType()) {
                            case "ASBIE":
                            case "BBIE":
                                String targetFromAbiePath = extractAbiePath(mapping.getTargetPath());
                                Abie targetFromAbie = getAbieIfExist.apply(targetFromAbiePath);

                                if (targetFromAbie == null) {
                                    AccManifestId targetAccManifestId = new AccManifestId(extractManifestId(getLastTag(targetFromAbiePath)));
                                    AccSummaryRecord targetAcc = targetCcDocument.getAcc(targetAccManifestId);

                                    targetFromAbie = new Abie();
                                    targetFromAbie.setGuid(ScoreGuidUtils.randomGuid());
                                    targetFromAbie.setBasedAccManifestId(targetAcc.accManifestId());
                                    targetFromAbie.setPath(targetFromAbiePath);
                                    targetFromAbie.setHashPath(sha256(targetFromAbie.getPath()));

                                    emptySourceAbieList.add(targetFromAbie);
                                }

                                break;
                        }

                        switch (mapping.getBieType()) {
                            case "ASBIE":
                                AsccSummaryRecord targetAscc =
                                        targetCcDocument.getAscc(new AsccManifestId(mapping.getTargetManifestId()));
                                AsccpSummaryRecord targetAsccp =
                                        targetCcDocument.getAsccp(targetAscc.toAsccpManifestId());
                                AccSummaryRecord targetRoleOfAcc =
                                        targetCcDocument.getAcc(targetAsccp.roleOfAccManifestId());

                                Asbie targetAsbie = new Asbie();
                                targetAsbie.setGuid(ScoreGuidUtils.randomGuid());
                                targetAsbie.setBasedAsccManifestId(targetAscc.asccManifestId());
                                targetAsbie.setPath(mapping.getTargetPath());
                                targetAsbie.setHashPath(sha256(targetAsbie.getPath()));
                                targetAsbie.setCardinalityMin(targetAscc.cardinality().min());
                                targetAsbie.setCardinalityMax(targetAscc.cardinality().max());
                                targetAsbie.setNillable(false);
                                targetAsbie.setDeprecated(false);
                                targetAsbie.setUsed(true);

                                WrappedAsbie wrappedAsbie = new WrappedAsbie();
                                wrappedAsbie.setAsbie(targetAsbie);
                                emptySourceAsbieList.add(wrappedAsbie);

                                Asbiep targetAsbiep = new Asbiep();
                                targetAsbiep.setGuid(ScoreGuidUtils.randomGuid());
                                targetAsbiep.setBasedAsccpManifestId(targetAsccp.asccpManifestId());
                                targetAsbiep.setPath(targetAsbie.getPath() + ">" + "ASCCP-" + targetAsccp.asccpManifestId());
                                targetAsbiep.setHashPath(sha256(targetAsbiep.getPath()));

                                WrappedAsbiep wrappedAsbiep = new WrappedAsbiep();
                                wrappedAsbie.setToAsbiep(wrappedAsbiep);
                                wrappedAsbiep.setAsbiep(targetAsbiep);
                                emptySourceAsbiepList.add(wrappedAsbiep);

                                String targetRoleOfAccPath = targetAsbiep.getPath() + ">" + "ACC-" + targetRoleOfAcc.accManifestId();
                                Abie targetRoleOfAbie = getAbieIfExist.apply(targetRoleOfAccPath);
                                if (targetRoleOfAbie == null) {
                                    targetRoleOfAbie = new Abie();
                                    targetRoleOfAbie.setGuid(ScoreGuidUtils.randomGuid());
                                    targetRoleOfAbie.setBasedAccManifestId(targetRoleOfAcc.accManifestId());
                                    targetRoleOfAbie.setPath(targetRoleOfAccPath);
                                    targetRoleOfAbie.setHashPath(sha256(targetRoleOfAbie.getPath()));

                                    emptySourceAbieList.add(targetRoleOfAbie);
                                }

                                break;

                            case "BBIE":
                                BccSummaryRecord targetBcc =
                                        targetCcDocument.getBcc(new BccManifestId(mapping.getTargetManifestId()));
                                BccpSummaryRecord targetBccp =
                                        targetCcDocument.getBccp(targetBcc.toBccpManifestId());
                                DtSummaryRecord targetDtManifest =
                                        targetCcDocument.getDt(targetBccp.dtManifestId());
                                DtAwdPriSummaryRecord targetDefaultDtAwdPri =
                                        targetCcDocument.getDtAwdPriList(targetDtManifest.dtManifestId()).stream()
                                                .filter(e -> e.isDefault())
                                                .findFirst().get();

                                Bbie targetBbie = new Bbie();
                                targetBbie.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbie.setBasedBccManifestId(targetBcc.bccManifestId());
                                targetBbie.setPath(mapping.getTargetPath());
                                targetBbie.setHashPath(sha256(targetBbie.getPath()));
                                targetBbie.setXbtManifestId(targetDefaultDtAwdPri.xbtManifestId());
                                if (targetBcc.valueConstraint() != null) {
                                    if (targetBcc.valueConstraint().hasFixedValue()) {
                                        targetBbie.setFixedValue(targetBcc.valueConstraint().fixedValue());
                                    } else {
                                        targetBbie.setDefaultValue(targetBcc.valueConstraint().defaultValue());
                                    }
                                }
                                targetBbie.setCardinalityMin(targetBcc.cardinality().min());
                                targetBbie.setCardinalityMax(targetBcc.cardinality().max());
                                targetBbie.setNillable(targetBcc.nillable());
                                targetBbie.setUsed(true);

                                WrappedBbie wrappedBbie = new WrappedBbie();
                                wrappedBbie.setBbie(targetBbie);
                                emptySourceBbieList.add(wrappedBbie);

                                Bbiep targetBbiep = new Bbiep();
                                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbiep.setBasedBccpManifestId(targetBccp.bccpManifestId());
                                targetBbiep.setPath(targetBbie.getPath() + ">" + "BCCP-" + targetBccp.bccpManifestId());
                                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));

                                wrappedBbie.setToBbiep(targetBbiep);
                                emptySourceBbiepList.add(targetBbiep);

                                break;

                            case "BBIE_SC":
                                DtScSummaryRecord targetDtSc =
                                        targetCcDocument.getDtSc(new DtScManifestId(mapping.getTargetManifestId()));
                                DtScAwdPriSummaryRecord targetDefaultDtScAwdPri =
                                        targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                                .filter(e -> e.isDefault())
                                                .findFirst().get();

                                BbieSc targetBbieSc = new BbieSc();
                                targetBbieSc.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbieSc.setBasedDtScManifestId(targetDtSc.dtScManifestId());
                                targetBbieSc.setPath(mapping.getTargetPath());
                                targetBbieSc.setHashPath(sha256(targetBbieSc.getPath()));
                                targetBbieSc.setXbtManifestId(targetDefaultDtScAwdPri.xbtManifestId());
                                if (targetDtSc.valueConstraint() != null) {
                                    if (targetDtSc.valueConstraint().hasFixedValue()) {
                                        targetBbieSc.setFixedValue(targetDtSc.valueConstraint().fixedValue());
                                    } else {
                                        targetBbieSc.setDefaultValue(targetDtSc.valueConstraint().defaultValue());
                                    }
                                }
                                targetBbieSc.setCardinalityMin(targetDtSc.cardinality().min());
                                targetBbieSc.setCardinalityMax(targetDtSc.cardinality().max());
                                targetBbieSc.setUsed(true);

                                WrappedBbieSc wrappedBbieSc = new WrappedBbieSc();
                                wrappedBbieSc.setBbieSc(targetBbieSc);
                                emptySourceBbieScList.add(wrappedBbieSc);

                                break;
                        }
                    });

            CreateBieRequest createBieRequest = new CreateBieRequest(this.requester);
            createBieRequest.setBizCtxIds(bizCtxIds);
            createBieRequest.setStatus(topLevelAsbiep.status());
            createBieRequest.setVersion(topLevelAsbiep.version());
            createBieRequest.setTopLevelAsbiep(this.asbiepMap.get(topLevelAsbiep.asbiepId()));
            List<WrappedAsbie> wrappedAsbieList = new ArrayList<WrappedAsbie>();
            toAsbiepToAsbieMap.values().forEach(list -> {
                wrappedAsbieList.addAll(list);
            });
            wrappedAsbieList.addAll(emptySourceAsbieList);
            createBieRequest.setAsbieList(
                    wrappedAsbieList.stream()
                            .map(asbie -> {
                                if (asbie.getFromAbie() == null) {
                                    String targetFromAbiePath = extractAbiePath(asbie.getAsbie().getPath());
                                    Abie targetFromAbie = getAbieIfExist.apply(targetFromAbiePath);
                                    if (targetFromAbie == null) {
                                        throw new IllegalStateException();
                                    }
                                    asbie.setFromAbie(targetFromAbie);
                                }

                                WrappedAsbiep asbiep = asbie.getToAsbiep();
                                if (asbiep == null) {
                                    String path = asbie.getAsbie().getPath();
                                    BieUpliftingMapping mapping = customMappingTable.getTargetAsccMappingByTargetPath(path);
                                    if (mapping != null) {
                                        asbie.setRefTopLevelAsbiepId(mapping.getRefTopLevelAsbiepId());
                                    } else {
                                        return null;
                                    }
                                } else {
                                    if (asbiep.getRoleOfAbie() == null) {
                                        AsccpSummaryRecord targetAsccp =
                                                targetCcDocument.getAsccp(asbiep.getAsbiep().getBasedAsccpManifestId());
                                        String targetRoleOfAbiePath = asbiep.getAsbiep().getPath() + ">" + "ACC-" +
                                                targetAsccp.roleOfAccManifestId();
                                        Abie targetRoleOfAbie = getAbieIfExist.apply(targetRoleOfAbiePath);
                                        if (targetRoleOfAbie == null) {
                                            throw new IllegalStateException();
                                        }
                                        asbiep.setRoleOfAbie(targetRoleOfAbie);
                                    }
                                }

                                return asbie;
                            })
                            .filter(e -> e != null)
                            .collect(Collectors.toList()));
            createBieRequest.setBbieList(
                    Stream.concat(toBbiepToBbieMap.values().stream(),
                                    emptySourceBbieList.stream())
                            .map(bbie -> {
                                if (bbie.getFromAbie() == null) {
                                    String targetFromAbiePath = extractAbiePath(bbie.getBbie().getPath());
                                    Abie targetFromAbie = getAbieIfExist.apply(targetFromAbiePath);
                                    if (targetFromAbie == null) {
                                        throw new IllegalStateException();
                                    }
                                    bbie.setFromAbie(targetFromAbie);
                                }

                                return bbie;
                            })
                            .collect(Collectors.toList()));
            createBieRequest.setBbieScList(
                    Stream.concat(bbieScList.stream(),
                                    emptySourceBbieScList.stream())
                            .map(bbieSc -> {
                                if (bbieSc.getBbie() == null) {
                                    String targetBbiePath = extractBbiePath(bbieSc.getBbieSc().getPath());
                                    Bbie targetBbie = getBbieIfExist.apply(targetBbiePath);
                                    if (targetBbie == null) {
                                        throw new IllegalStateException();
                                    }
                                    bbieSc.setBbie(targetBbie);
                                }

                                return bbieSc;
                            })
                            .collect(Collectors.toList()));

            createBieRequest.setSourceTopLevelAsbiepId(topLevelAsbiep.topLevelAsbiepId());
            createBieRequest.setSourceAction("Uplift");

            targetTopLevelAsbiepId = command(requester)
                    .createBie(createBieRequest)
                    .getTopLevelAsbiepId();
        }

        @Override
        public void visitAbie(Abie abie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            if (abie == null) {
                targetAccQueue.poll();
                return;
            }
            AccSummaryRecord sourceAcc = sourceCcDocument.getAcc(abie.getBasedAccManifestId());
            List<Association> sourceAssociations =
                    getAssociationsRegardingBases(currentSourcePath, sourceCcDocument, sourceAcc);
            abieSourceAssociationsMap.put(abie.getAbieId(), sourceAssociations);

            AccSummaryRecord targetAcc = targetAccQueue.poll();
            if (targetAcc != null) { // found matched acc
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(currentTargetPath, targetCcDocument, targetAcc);
                abieTargetAssociationsMap.put(abie.getAbieId(), targetAssociations);

                currentTargetPath = currentTargetPath + ">" + "ACC-" + targetAcc.accManifestId();
                Abie targetAbie = new Abie();
                targetAbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAbie.setBasedAccManifestId(targetAcc.accManifestId());
                targetAbie.setPath(currentTargetPath);
                targetAbie.setHashPath(sha256(currentTargetPath));
                targetAbie.setDefinition(abie.getDefinition());
                targetAbie.setRemark(abie.getRemark());
                targetAbie.setBizTerm(abie.getBizTerm());

                this.roleOfAbieToAsbiepMap.get(abie.getAbieId()).setRoleOfAbie(targetAbie);
                this.abieIdToAbieMap.put(abie.getAbieId(), targetAbie);
            }
        }

        @Override
        public void visitAsbie(Asbie asbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            AsccSummaryRecord sourceAsccManifest = sourceCcDocument.getAscc(asbie.getBasedAsccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceAsccManifest)).findAny().orElse(null);

            currentSourcePath = sourceAssociation.getPath();
            AsccSummaryRecord targetAscc = null;
            BieUpliftingMapping targetAsccMapping =
                    this.customMappingTable.getTargetAsccMappingBySourcePath(currentSourcePath);
            if (targetAsccMapping != null) {
                currentTargetPath = targetAsccMapping.getTargetPath();
                targetAscc = targetCcDocument.getAscc(new AsccManifestId(targetAsccMapping.getTargetManifestId()));
            } else {
                List<Association> targetAssociations =
                        abieTargetAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
                CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isAscc())
                        .map(e -> ccMatchingService.score(
                                sourceCcDocument,
                                sourceAssociation,
                                targetCcDocument,
                                e,
                                (ccDocument, association) -> ccDocument.getAscc(((AsccSummaryRecord) association.getCcAssociation()).asccManifestId())))
                        .max(Comparator.comparing(CcMatchingScore::getScore))
                        .orElse(new CcMatchingScore(0.0d, null, null));
                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    currentTargetPath = targetAssociation.getPath();
                    targetAscc = (AsccSummaryRecord) targetAssociation.getCcAssociation();
                }
            }

            if (targetAscc != null) {
                AsccpSummaryRecord toAsccp = targetCcDocument.getAsccp(
                        targetAscc.toAsccpManifestId());
                targetAsccpQueue.offer(toAsccp);

                Asbie targetAsbie = new Asbie();
                targetAsbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbie.setBasedAsccManifestId(targetAscc.asccManifestId());
                targetAsbie.setPath(currentTargetPath);
                targetAsbie.setHashPath(sha256(targetAsbie.getPath()));
                targetAsbie.setCardinalityMin(asbie.getCardinalityMin());
                targetAsbie.setCardinalityMax(asbie.getCardinalityMax());
                targetAsbie.setNillable(asbie.isNillable());
                targetAsbie.setDefinition(asbie.getDefinition());
                targetAsbie.setRemark(asbie.getRemark());
                targetAsbie.setDeprecated(asbie.isDeprecated());
                targetAsbie.setUsed(asbie.isUsed());

                WrappedAsbie upliftingAsbie = new WrappedAsbie();
                Abie fromAbie = this.abieIdToAbieMap.get(asbie.getFromAbieId());

                if (fromAbie != null && currentTargetPath.contains(fromAbie.getPath())) {
                    upliftingAsbie.setFromAbie(fromAbie);
                }

                upliftingAsbie.setAsbie(targetAsbie);

                if (targetAsccMapping != null) {
                    upliftingAsbie.setRefTopLevelAsbiepId(targetAsccMapping.getRefTopLevelAsbiepId());
                }
                List<WrappedAsbie> wrappedAsbieList = this.toAsbiepToAsbieMap.get(asbie.getToAsbiepId());
                if (wrappedAsbieList != null && wrappedAsbieList.size() > 0) {
                    wrappedAsbieList.add(upliftingAsbie);
                    this.toAsbiepToAsbieMap.put(asbie.getToAsbiepId(), wrappedAsbieList);
                } else {
                    List<WrappedAsbie> newWrappedAsbieList = new ArrayList<>();
                    newWrappedAsbieList.add(upliftingAsbie);
                    this.toAsbiepToAsbieMap.put(asbie.getToAsbiepId(), newWrappedAsbieList);
                }


            }
        }

        @Override
        public void visitBbie(Bbie bbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccSummaryRecord sourceBcc = sourceCcDocument.getBcc(bbie.getBasedBccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceBcc)).findAny().orElse(null);

            currentSourcePath = sourceAssociation.getPath();
            BccSummaryRecord targetBcc = null;
            BieUpliftingMapping targetBccMapping =
                    this.customMappingTable.getTargetBccMappingBySourcePath(currentSourcePath);
            if (targetBccMapping != null) {
                currentTargetPath = targetBccMapping.getTargetPath();
                targetBcc = targetCcDocument.getBcc(
                        new BccManifestId(targetBccMapping.getTargetManifestId()));
            } else {
                List<Association> targetAssociations =
                        abieTargetAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
                CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isBcc())
                        .map(e -> ccMatchingService.score(
                                sourceCcDocument,
                                sourceAssociation,
                                targetCcDocument,
                                e,
                                (ccDocument, association) -> ccDocument.getBcc(((BccSummaryRecord) association.getCcAssociation()).bccManifestId())))
                        .max(Comparator.comparing(CcMatchingScore::getScore))
                        .orElse(new CcMatchingScore(0.0d, null, null));

                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    currentTargetPath = targetAssociation.getPath();
                    targetBcc = (BccSummaryRecord) targetAssociation.getCcAssociation();
                }
            }

            if (targetBcc != null) {
                BccpSummaryRecord toBccp = targetCcDocument.getBccp(
                        targetBcc.toBccpManifestId());
                this.previousBbie = bbie;
                targetBccpQueue.offer(toBccp);

                Bbie targetBbie = new Bbie();
                targetBbie.setGuid(ScoreGuidUtils.randomGuid());
                targetBbie.setBasedBccManifestId(targetBcc.bccManifestId());
                targetBbie.setPath(currentTargetPath);
                targetBbie.setHashPath(sha256(targetBbie.getPath()));
                targetBbie.setDefaultValue(bbie.getDefaultValue());
                targetBbie.setFixedValue(bbie.getFixedValue());
                targetBbie.setCardinalityMin(bbie.getCardinalityMin());
                targetBbie.setCardinalityMax(bbie.getCardinalityMax());
                targetBbie.setFacetMinLength(bbie.getFacetMinLength());
                targetBbie.setFacetMaxLength(bbie.getFacetMaxLength());
                targetBbie.setFacetPattern(bbie.getFacetPattern());
                targetBbie.setNillable(bbie.isNillable());
                targetBbie.setDefinition(bbie.getDefinition());
                targetBbie.setRemark(bbie.getRemark());
                targetBbie.setExample(bbie.getExample());
                targetBbie.setDeprecated(bbie.isDeprecated());
                targetBbie.setUsed(bbie.isUsed());

                setValueDomain(bbie, targetBbie, toBccp.dtManifestId(),
                        sourceXbtList,
                        sourceCodeListList,
                        sourceAgencyIdListList);

                WrappedBbie upliftingBbie = new WrappedBbie();
                Abie fromAbie = this.abieIdToAbieMap.get(bbie.getFromAbieId());

                if (fromAbie != null && currentTargetPath.contains(fromAbie.getPath())) {
                    upliftingBbie.setFromAbie(fromAbie);
                }
                upliftingBbie.setBbie(targetBbie);

                this.toBbiepToBbieMap.put(bbie.getToBbiepId(), upliftingBbie);
                this.bbieMap.put(bbie.getBbieId(), targetBbie);
            }
        }

        @Override
        public void visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            if (asbiep == null) {
                targetAsccpQueue.poll();
                return;
            }
            AsccpSummaryRecord sourceAsccp = sourceCcDocument.getAsccp(
                    asbiep.getBasedAsccpManifestId());

            currentSourcePath = (hasLength(currentSourcePath)) ?
                    currentSourcePath + ">" + "ASCCP-" + sourceAsccp.asccpManifestId() :
                    "ASCCP-" + sourceAsccp.asccpManifestId();

            AsccpSummaryRecord targetAsccp = targetAsccpQueue.poll();
            if (targetAsccp != null) { // found matched asccp
                AccSummaryRecord targetRoleOfAcc = targetCcDocument.getAcc(targetAsccp.roleOfAccManifestId());
                targetAccQueue.offer(targetRoleOfAcc);
                currentTargetPath = (hasLength(currentTargetPath)) ?
                        currentTargetPath + ">" + "ASCCP-" + targetAsccp.asccpManifestId() :
                        "ASCCP-" + targetAsccp.asccpManifestId();

                Asbiep targetAsbiep = new Asbiep();
                targetAsbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbiep.setBasedAsccpManifestId(targetAsccp.asccpManifestId());
                targetAsbiep.setPath(currentTargetPath);
                targetAsbiep.setHashPath(sha256(targetAsbiep.getPath()));
                targetAsbiep.setDefinition(asbiep.getDefinition());
                targetAsbiep.setRemark(asbiep.getRemark());
                targetAsbiep.setBizTerm(asbiep.getBizTerm());
                targetAsbiep.setDisplayName(asbiep.getDisplayName());

                WrappedAsbiep upliftingAsbiep = new WrappedAsbiep();
                upliftingAsbiep.setAsbiep(targetAsbiep);

                List<WrappedAsbie> upliftingAsbieList = this.toAsbiepToAsbieMap.get(asbiep.getAsbiepId());
                if (upliftingAsbieList != null) {
                    upliftingAsbieList.forEach(upliftingAsbie -> {
                        upliftingAsbie.setToAsbiep(upliftingAsbiep);
                    });

                }
                this.asbiepMap.put(asbiep.getAsbiepId(), upliftingAsbiep);
                this.roleOfAbieToAsbiepMap.put(asbiep.getRoleOfAbieId(), upliftingAsbiep);
            }
        }

        @Override
        public void visitBbiep(Bbiep bbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccpSummaryRecord sourceBccp = sourceCcDocument.getBccp(bbiep.getBasedBccpManifestId());
            currentSourcePath = currentSourcePath + ">" + "BCCP-" + sourceBccp.bccpManifestId();

            BccpSummaryRecord targetBccp = targetBccpQueue.poll();
            if (targetBccp != null) {
                currentTargetPath = currentTargetPath + ">" + "BCCP-" + targetBccp.bccpManifestId();

                DtManifestId targetBdtManifestId = targetBccp.dtManifestId();
                DtSummaryRecord targetDt = targetCcDocument.getDt(targetBdtManifestId);
                bbieTargetDtScMap.put(previousBbie.getBbieId(),
                        targetCcDocument.getDtScList(targetDt.dtManifestId()));

                Bbiep targetBbiep = new Bbiep();
                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetBbiep.setBasedBccpManifestId(targetBccp.bccpManifestId());
                targetBbiep.setPath(currentTargetPath);
                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));
                targetBbiep.setDefinition(bbiep.getDefinition());
                targetBbiep.setRemark(bbiep.getRemark());
                targetBbiep.setBizTerm(bbiep.getBizTerm());
                targetBbiep.setDisplayName(bbiep.getDisplayName());

                this.toBbiepToBbieMap.get(bbiep.getBbiepId()).setToBbiep(targetBbiep);
            }
        }

        @Override
        public void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScSummaryRecord sourceDtSc = sourceCcDocument.getDtSc(bbieSc.getBasedDtScManifestId());
            DtSummaryRecord sourceDt = sourceCcDocument.getDt(sourceDtSc.ownerDtManifestId());

            String sourcePath = currentSourcePath + ">" + "DT-" + sourceDt.dtManifestId() +
                    ">" + "DT_SC-" + sourceDtSc.dtScManifestId();
            DtScSummaryRecord targetDtSc = null;
            String targetPath = null;
            BieUpliftingMapping targetDtScMapping =
                    this.customMappingTable.getTargetDtScMappingBySourcePath(sourcePath);
            if (targetDtScMapping != null) {
                targetDtSc = targetCcDocument.getDtSc(
                        new DtScManifestId(targetDtScMapping.getTargetManifestId()));
                targetPath = targetDtScMapping.getTargetPath();
            } else {
                CcMatchingScore matchingScore =
                        bbieTargetDtScMap.getOrDefault(bbieSc.getBbieId(), Collections.emptyList()).stream()
                                .map(e -> ccMatchingService.score(
                                        sourceCcDocument,
                                        sourceDtSc,
                                        targetCcDocument,
                                        e,
                                        (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest.dtScManifestId())))
                                .max(Comparator.comparing(CcMatchingScore::getScore))
                                .orElse(new CcMatchingScore(0.0d, null, null));
                if (matchingScore.getScore() > 0.0d) {
                    targetDtSc = (DtScSummaryRecord) matchingScore.getTarget();
                    DtSummaryRecord targetDt = targetCcDocument.getDt(targetDtSc.ownerDtManifestId());
                    targetPath = currentTargetPath + ">" + "DT-" + targetDt.dtManifestId() +
                            ">" + "DT_SC-" + targetDtSc.dtScManifestId();
                }
            }

            if (targetDtSc != null) {
                BbieSc targetBbieSc = new BbieSc();
                targetBbieSc.setGuid(ScoreGuidUtils.randomGuid());
                targetBbieSc.setBasedDtScManifestId(targetDtSc.dtScManifestId());
                targetBbieSc.setPath(targetPath);
                targetBbieSc.setHashPath(sha256(targetBbieSc.getPath()));
                targetBbieSc.setDefaultValue(bbieSc.getDefaultValue());
                targetBbieSc.setFixedValue(bbieSc.getFixedValue());
                targetBbieSc.setFacetMinLength(bbieSc.getFacetMinLength());
                targetBbieSc.setFacetMaxLength(bbieSc.getFacetMaxLength());
                targetBbieSc.setFacetPattern(bbieSc.getFacetPattern());
                targetBbieSc.setCardinalityMin(bbieSc.getCardinalityMin());
                targetBbieSc.setCardinalityMax(bbieSc.getCardinalityMax());
                targetBbieSc.setNillable(bbieSc.isNillable());
                targetBbieSc.setDefinition(bbieSc.getDefinition());
                targetBbieSc.setRemark(bbieSc.getRemark());
                targetBbieSc.setBizTerm(bbieSc.getBizTerm());
                targetBbieSc.setDisplayName(bbieSc.getDisplayName());
                targetBbieSc.setExample(bbieSc.getExample());
                targetBbieSc.setDeprecated(bbieSc.isDeprecated());
                targetBbieSc.setUsed(bbieSc.isUsed());

                setValueDomain(bbieSc, targetBbieSc, targetDtSc.dtScManifestId(),
                        sourceXbtList,
                        sourceCodeListList,
                        sourceAgencyIdListList);

                WrappedBbieSc upliftingBbieSc = new WrappedBbieSc();
                upliftingBbieSc.setBbie(this.bbieMap.get(bbieSc.getBbieId()));
                upliftingBbieSc.setBbieSc(targetBbieSc);

                this.bbieScList.add(upliftingBbieSc);
            }
        }

        private Bbie setValueDomain(Bbie sourceBbie,
                                    Bbie targetBbie,
                                    DtManifestId targetDtManifestId,
                                    List<XbtSummaryRecord> sourceXbtList,
                                    List<CodeListSummaryRecord> sourceCodeListList,
                                    List<AgencyIdListSummaryRecord> sourceAgencyIdListList) {

            DtAwdPriSummaryRecord targetDefaultDtAwdPri;
            DtSummaryRecord targetDt = targetCcDocument.getDt(targetDtManifestId);

            if (sourceBbie.getXbtManifestId() != null) {
                XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(e -> e.xbtManifestId().equals(sourceBbie.getXbtManifestId())).findAny().orElse(null);
                XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
                if (targetXbt != null) {
                    targetBbie.setXbtManifestId(targetXbt.xbtManifestId());
                }
            } else if (sourceBbie.getCodeListManifestId() != null) {
                CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(e -> e.codeListManifestId().equals(sourceBbie.getCodeListManifestId())).findAny().orElse(null);
                CodeListSummaryRecord targetCodeList = getTargetCodeListManifest(
                        sourceCodeList, targetCodeListList);
                if (targetCodeList != null) {
                    targetBbie.setCodeListManifestId(targetCodeList.codeListManifestId());
                }
            } else if (sourceBbie.getAgencyIdListManifestId() != null) {
                AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(e -> e.agencyIdListManifestId().equals(sourceBbie.getAgencyIdListManifestId())).findFirst().orElse(null);
                AgencyIdListSummaryRecord targetAgencyIdList = getTargetAgencyIdListManifest(
                        sourceAgencyIdList, targetAgencyIdListList);
                if (targetAgencyIdList != null) {
                    targetBbie.setAgencyIdListManifestId(targetAgencyIdList.agencyIdListManifestId());
                }
            }

            if (targetBbie.getXbtManifestId() == null &&
                    targetBbie.getCodeListManifestId() == null &&
                    targetBbie.getAgencyIdListManifestId() == null) {
                if ("Date Time".equals(targetDt.dataTypeTerm())) {
                    targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("date time"))
                                    .findFirst().get();
                } else if ("Date".equals(targetDt.dataTypeTerm())) {
                    targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("date"))
                                    .findFirst().get();
                } else if ("Time".equals(targetDt.dataTypeTerm())) {
                    targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("time"))
                                    .findFirst().get();
                } else {
                    targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(e -> e.isDefault())
                                    .findFirst().get();
                }
                targetBbie.setXbtManifestId(targetDefaultDtAwdPri.xbtManifestId());
            }
            return targetBbie;
        }

        private BbieSc setValueDomain(BbieSc sourceBbieSc,
                                      BbieSc targetBbieSc,
                                      DtScManifestId dtScManifestId,
                                      List<XbtSummaryRecord> sourceXbtList,
                                      List<CodeListSummaryRecord> sourceCodeListList,
                                      List<AgencyIdListSummaryRecord> sourceAgencyIdListList) {

            DtScAwdPriSummaryRecord targetDefaultDtScAwdPri;
            DtScSummaryRecord targetDtSc = targetCcDocument.getDtSc(dtScManifestId);

            if (sourceBbieSc.getXbtManifestId() != null) {
                XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(e -> e.xbtManifestId().equals(sourceBbieSc.getXbtManifestId())).findAny().orElse(null);
                XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
                if (targetXbt != null) {
                    targetBbieSc.setXbtManifestId(targetXbt.xbtManifestId());
                }
            } else if (sourceBbieSc.getCodeListManifestId() != null) {
                CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(e -> e.codeListManifestId().equals(sourceBbieSc.getCodeListManifestId())).findAny().orElse(null);
                CodeListSummaryRecord targetCodeList = getTargetCodeListManifest(
                        sourceCodeList, targetCodeListList);
                if (targetCodeList != null) {
                    targetBbieSc.setCodeListManifestId(targetCodeList.codeListManifestId());
                }
            } else if (sourceBbieSc.getAgencyIdListManifestId() != null) {
                AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(e -> e.agencyIdListManifestId().equals(sourceBbieSc.getAgencyIdListManifestId())).findFirst().orElse(null);
                AgencyIdListSummaryRecord targetAgencyIdListManifest = getTargetAgencyIdListManifest(
                        sourceAgencyIdList, targetAgencyIdListList);
                if (targetAgencyIdListManifest != null) {
                    targetBbieSc.setAgencyIdListManifestId(targetAgencyIdListManifest.agencyIdListManifestId());
                }
            }

            if (targetBbieSc.getXbtManifestId() == null &&
                    targetBbieSc.getCodeListManifestId() == null &&
                    targetBbieSc.getAgencyIdListManifestId() == null) {
                if ("Date Time".equals(targetDtSc.representationTerm())) {
                    targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("date time"))
                                    .findFirst().get();
                } else if ("Date".equals(targetDtSc.representationTerm())) {
                    targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("date"))
                                    .findFirst().get();
                } else if ("Time".equals(targetDtSc.representationTerm())) {
                    targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(e -> targetCcDocument.getXbt(e.xbtManifestId()).name().equalsIgnoreCase("time"))
                                    .findFirst().get();
                } else {
                    targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(e -> e.isDefault())
                                    .findFirst().get();
                }
                targetBbieSc.setXbtManifestId(targetDefaultDtScAwdPri.xbtManifestId());
            }
            return targetBbieSc;
        }
    }

    @Transactional
    public UpliftBieResponse upliftBie(ScoreUser requester, UpliftBieRequest request) {

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);

        AsccpManifestId targetAsccpManifestId = request.getTargetAsccpManifestId();
        if (targetAsccpManifestId == null) {
            targetAsccpManifestId = asccpQuery.findNextAsccpManifest(
                            request.getTopLevelAsbiepId(),
                            request.getTargetReleaseId())
                    .asccpManifestId();
        }

        AsccpSummaryRecord targetAsccp = asccpQuery.getAsccpSummary(targetAsccpManifestId);
        if (targetAsccp == null) {
            throw new IllegalArgumentException("Target ASCCP record not found.");
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        CcDocument targetCcDocument = new CcDocumentImpl(requester, repositoryFactory, targetAsccp.release().releaseId());

        List<BusinessContextId> bizCtxIds = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                .getAssignedBusinessContextList(request.getTopLevelAsbiepId());

        List<BieUpliftingMapping> mappingList = request.getCustomMappingTable();
        BieUpliftingCustomMappingTable customMappingTable = new BieUpliftingCustomMappingTable(
                sourceBieDocument.getCcDocument(),
                targetCcDocument,
                mappingList);

        ReleaseId sourceReleaseId = sourceBieDocument.getCcDocument().getAsccp(sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId()).release().releaseId();
        ReleaseId targetReleaseId = targetCcDocument.getAsccp(targetAsccpManifestId).release().releaseId();

        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);

        List<XbtSummaryRecord> sourceXbtList = xbtQuery.getXbtSummaryList(sourceReleaseId);
        List<XbtSummaryRecord> targetXbtList = xbtQuery.getXbtSummaryList(targetReleaseId);

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);

        List<CodeListSummaryRecord> sourceCodeListList = codeListQuery.getCodeListSummaryList(sourceReleaseId);
        List<CodeListSummaryRecord> targetCodeListList = codeListQuery.getCodeListSummaryList(targetReleaseId);

        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);

        List<AgencyIdListSummaryRecord> sourceAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(sourceReleaseId);
        List<AgencyIdListSummaryRecord> targetAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(targetReleaseId);

        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        Map<DtAwdPriId, DtAwdPriSummaryRecord> sourceDtAwdPriMap = dtQuery.getDtAwdPriSummaryList(sourceReleaseId).stream()
                .collect(Collectors.toMap(DtAwdPriSummaryRecord::dtAwdPriId, Function.identity()));
        Map<DtId, List<DtAwdPriSummaryRecord>> targetDtAwdPriByDtIdMap = dtQuery.getDtAwdPriSummaryList(targetReleaseId).stream()
                .collect(groupingBy(DtAwdPriSummaryRecord::dtId));

        Map<DtScAwdPriId, DtScAwdPriSummaryRecord> sourceDtScAwdPriMap = dtQuery.getDtScAwdPriSummaryList(sourceReleaseId).stream()
                .collect(Collectors.toMap(DtScAwdPriSummaryRecord::dtScAwdPriId, Function.identity()));
        Map<DtScId, List<DtScAwdPriSummaryRecord>> targetDtScAwdPriByDtScIdMap = dtQuery.getDtScAwdPriSummaryList(sourceReleaseId).stream()
                .collect(groupingBy(DtScAwdPriSummaryRecord::dtScId));

        BieUpliftingHandler upliftingHandler =
                new BieUpliftingHandler(request.getRequester(), bizCtxIds, customMappingTable,
                        sourceBieDocument, targetCcDocument, targetAsccpManifestId,
                        sourceDtAwdPriMap, targetDtAwdPriByDtIdMap,
                        sourceDtScAwdPriMap, targetDtScAwdPriByDtScIdMap,
                        sourceXbtList, targetXbtList,
                        sourceCodeListList, targetCodeListList,
                        sourceAgencyIdListList, targetAgencyIdListList);
        TopLevelAsbiepId targetTopLevelAsbiepId = upliftingHandler.uplift();

        UpliftBieResponse response = new UpliftBieResponse();
        response.setTopLevelAsbiepId(targetTopLevelAsbiepId);
        return response;
    }

    public UpliftValidationResponse validateBieUplifting(ScoreUser requester, UpliftValidationRequest request) {
        UpliftValidationResponse response = new UpliftValidationResponse();
        List<BieUpliftingValidation> validations = new ArrayList<>();

        ReleaseRecord sourceRelease = getReleaseRecordByTopLevelAsbiepId(request.getTopLevelAsbiepId());
        ReleaseRecord targetRelease = getReleaseRecordByReleaseId(request.getTargetReleaseId());

        if (sourceRelease.getReleaseId().toBigInteger().compareTo(targetRelease.getReleaseId().toBigInteger()) >= 0) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        CcDocument targetCcDocument = new CcDocumentImpl(requester, repositoryFactory, request.getTargetReleaseId());

        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);

        List<XbtSummaryRecord> sourceXbtList = xbtQuery.getXbtSummaryList(new ReleaseId(sourceRelease.getReleaseId().toBigInteger()));
        List<XbtSummaryRecord> targetXbtList = xbtQuery.getXbtSummaryList(new ReleaseId(targetRelease.getReleaseId().toBigInteger()));

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);

        List<CodeListSummaryRecord> sourceCodeListList = codeListQuery.getCodeListSummaryList(new ReleaseId(sourceRelease.getReleaseId().toBigInteger()));
        List<CodeListSummaryRecord> targetCodeListList = codeListQuery.getCodeListSummaryList(new ReleaseId(targetRelease.getReleaseId().toBigInteger()));

        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);

        List<AgencyIdListSummaryRecord> sourceAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(new ReleaseId(sourceRelease.getReleaseId().toBigInteger()));
        List<AgencyIdListSummaryRecord> targetAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(request.getTargetReleaseId());

        ReleaseId sourceReleaseId = new ReleaseId(sourceRelease.getReleaseId().toBigInteger());
        ReleaseId targetReleaseId = request.getTargetReleaseId();

        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        Map<DtAwdPriId, DtAwdPriSummaryRecord> sourceDtAwdPriMap = dtQuery.getDtAwdPriSummaryList(sourceReleaseId).stream()
                .collect(Collectors.toMap(DtAwdPriSummaryRecord::dtAwdPriId, Function.identity()));
        Map<DtId, List<DtAwdPriSummaryRecord>> targetDtAwdPriByDtIdMap = dtQuery.getDtAwdPriSummaryList(targetReleaseId).stream()
                .collect(groupingBy(DtAwdPriSummaryRecord::dtId));

        Map<DtScAwdPriId, DtScAwdPriSummaryRecord> sourceDtScAwdPriMap = dtQuery.getDtScAwdPriSummaryList(sourceReleaseId).stream()
                .collect(Collectors.toMap(DtScAwdPriSummaryRecord::dtScAwdPriId, Function.identity()));
        Map<DtScId, List<DtScAwdPriSummaryRecord>> targetDtScAwdPriByDtScIdMap = dtQuery.getDtScAwdPriSummaryList(sourceReleaseId).stream()
                .collect(groupingBy(DtScAwdPriSummaryRecord::dtScId));

        request.getMappingList().forEach(mapping -> {
            BieUpliftingValidation validation = new BieUpliftingValidation();
            validation.setBieId(mapping.getBieId());
            validation.setBieType(mapping.getBieType());
            switch (mapping.getBieType().toUpperCase()) {
                case "ABIE":
                    validation.setValid(true);
                    break;
                case "ASBIE":
                    validation.setValid(true);
                    break;
                case "BBIE":
                    Bbie bbie = sourceBieDocument.getBbie(new BbieId(mapping.getBieId()));
                    BccManifestId bccManifestId = (mapping.getTargetManifestId() != null) ? new BccManifestId(mapping.getTargetManifestId()) : null;
                    if (bccManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    BccSummaryRecord bcc = targetCcDocument.getBcc(bccManifestId);
                    BccpSummaryRecord bccp = targetCcDocument.getBccp(bcc.toBccpManifestId());
                    DtSummaryRecord dt = targetCcDocument.getDt(bccp.dtManifestId());

                    if (bbie.getXbtManifestId() != null) {
                        XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(xbt -> xbt.xbtManifestId().equals(bbie.getXbtManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtPriRestriIdMappable(
                                sourceXbt, dt.dtManifestId(), targetDtAwdPriByDtIdMap, targetXbtList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbie.getCodeListManifestId() != null) {
                        CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(codeList -> codeList.codeListManifestId().equals(bbie.getCodeListManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtCodeListManifestIdMappable(
                                sourceCodeList, dt.dtManifestId(), targetDtAwdPriByDtIdMap, targetCodeListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else {
                        AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(agencyIdList -> agencyIdList.agencyIdListManifestId().equals(bbie.getAgencyIdListManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtAgencyIdListManifestIdMappable(
                                sourceAgencyIdList, dt.dtManifestId(), targetDtAwdPriByDtIdMap, targetAgencyIdListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    }
                    break;
                case "BBIE_SC":
                    BbieSc bbieSc = sourceBieDocument.getBbieSc(new BbieScId(mapping.getBieId()));
                    DtScManifestId dtScManifestId = (mapping.getTargetManifestId() != null) ? new DtScManifestId(mapping.getTargetManifestId()) : null;
                    if (dtScManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    DtScSummaryRecord dtSc = targetCcDocument.getDtSc(dtScManifestId);

                    if (bbieSc.getXbtManifestId() != null) {
                        XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(xbt -> xbt.xbtManifestId().equals(bbieSc.getXbtManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtScPriRestriIdMappable(
                                sourceXbt, dtSc.dtScManifestId(), targetDtScAwdPriByDtScIdMap, targetXbtList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbieSc.getCodeListManifestId() != null) {
                        CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(codeList -> codeList.codeListManifestId().equals(bbieSc.getCodeListManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtScCodeListIdMappable(
                                sourceCodeList, dtSc.dtScManifestId(), targetDtScAwdPriByDtScIdMap, targetCodeListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else {
                        AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(agencyIdList -> agencyIdList.agencyIdListManifestId().equals(bbieSc.getAgencyIdListManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtScAgencyIdListIdMappable(
                                sourceAgencyIdList, dtSc.dtScManifestId(), targetDtScAwdPriByDtScIdMap, targetAgencyIdListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    }
                    break;
            }
            validations.add(validation);
        });
        response.setValidations(validations);
        return response;
    }

    private String checkBdtPriRestriIdMappable(XbtSummaryRecord sourceXbt,
                                               DtManifestId targetDtManifestId,
                                               Map<DtId, List<DtAwdPriSummaryRecord>> targetMap,
                                               List<XbtSummaryRecord> targetXbtList) {
        XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
        if (targetXbt != null) {
            return "";
        }
        return "Primitive value '" + sourceXbt.name() + "' is not allowed in the target node. Uplifted node will use its default primitive in the domain value restriction.";
    }

    private String checkBdtScPriRestriIdMappable(XbtSummaryRecord sourceXbt,
                                                 DtScManifestId targetDtScManifestId,
                                                 Map<DtScId, List<DtScAwdPriSummaryRecord>> targetMap,
                                                 List<XbtSummaryRecord> targetXbtList) {
        XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
        if (targetXbt != null) {
            return "";
        }
        return "Primitive value '" + sourceXbt.name() + "' is not allowed in the target node. Uplifted node will use its default primitive in the domain value restriction.";
    }

    private String checkBdtCodeListManifestIdMappable(CodeListSummaryRecord sourceCodeList,
                                                      DtManifestId targetBdtManifestId,
                                                      Map<DtId, List<DtAwdPriSummaryRecord>> targetMap,
                                                      List<CodeListSummaryRecord> targetCodeListList) {
        CodeListSummaryRecord targetCodeListManifest = getTargetCodeListManifest(sourceCodeList, targetCodeListList);
        if (targetCodeListManifest != null) {
            return "";
        }
        return "Code List '" + sourceCodeList.name() + "' is not allowed in the target node or the system cannot find the exact match code list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtScCodeListIdMappable(CodeListSummaryRecord sourceCodeList,
                                                DtScManifestId targetBdtScManifestId,
                                                Map<DtScId, List<DtScAwdPriSummaryRecord>> targetMap,
                                                List<CodeListSummaryRecord> targetCodeListList) {
        CodeListSummaryRecord targetCodeListManifest = getTargetCodeListManifest(sourceCodeList, targetCodeListList);
        if (targetCodeListManifest != null) {
            return "";
        }
        return "Code List '" + sourceCodeList.name() + "' is not allowed in the target node or the system cannot find the exact match code list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtAgencyIdListManifestIdMappable(AgencyIdListSummaryRecord sourceAgencyIdList,
                                                          DtManifestId targetBdtManifestId,
                                                          Map<DtId, List<DtAwdPriSummaryRecord>> targetMap,
                                                          List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        AgencyIdListSummaryRecord targetAgencyIdListManifest = getTargetAgencyIdListManifest(
                sourceAgencyIdList, targetAgencyIdListList);
        if (targetAgencyIdListManifest != null) {
            return "";
        }
        return "Agency ID List '" + sourceAgencyIdList.name() + "' is not allowed in the target node or the system cannot find the exact match agency ID list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtScAgencyIdListIdMappable(AgencyIdListSummaryRecord sourceAgencyIdList,
                                                    DtScManifestId targetBdtScManifestId,
                                                    Map<DtScId, List<DtScAwdPriSummaryRecord>> targetMap,
                                                    List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        AgencyIdListSummaryRecord targetAgencyIdListManifest =
                getTargetAgencyIdListManifest(sourceAgencyIdList, targetAgencyIdListList);
        if (targetAgencyIdListManifest != null) {
            return "";
        }
        return "Agency ID List '" + sourceAgencyIdList.name() + "' is not allowed in the target node or the system cannot find the exact match agency ID list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    public XbtSummaryRecord getTargetXbtManifest(
            XbtSummaryRecord sourceXbt,
            List<XbtSummaryRecord> targetXbtList) {
        if (sourceXbt == null) {
            return null;
        }

        XbtSummaryRecord targetXbt = targetXbtList.stream()
                .filter(e -> e.guid().equals(sourceXbt.guid()))
                .findFirst().orElse(null);
        if (targetXbt == null) {
            return null;
        }

        return targetXbtList.stream()
                .filter(e -> e.xbtId().equals(targetXbt.xbtId()))
                .findFirst().orElse(null);
    }

    public CodeListSummaryRecord getTargetCodeListManifest(
            CodeListSummaryRecord sourceCodeList,
            List<CodeListSummaryRecord> targetCodeListList) {
        if (sourceCodeList == null) {
            return null;
        }

        CodeListSummaryRecord targetCodeList = targetCodeListList.stream()
                .filter(e -> e.guid().equals(sourceCodeList.guid()))
                .findFirst().orElse(null);
        if (targetCodeList == null) {
            // Issue #1356
            // End-user code list assigned to a source BIE node can be carried into the uplifted BIE only
            // if the end-user code list with the same name, list ID, and agency ID exists (or has been uplifted)
            // in the target release and it is allowed by the target BIE node.
            targetCodeList = targetCodeListList.stream()
                    .filter(e -> StringUtils.equals(sourceCodeList.name(), e.name()) &&
                            StringUtils.equals(sourceCodeList.listId(), e.listId()) &&
//                            StringUtils.equals(sourceCodeList.agencyIdListValueManifestId(), e.agencyIdListValueManifestId()) &&
                            StringUtils.equals(sourceCodeList.versionId(), e.versionId()))
                    .findFirst().orElse(null);
        }

        if (targetCodeList == null) {
            return null;
        }

        CodeListSummaryRecord finalTargetCodeList = targetCodeList;
        return targetCodeListList.stream()
                .filter(e -> e.codeListId().equals(finalTargetCodeList.codeListId()))
                .findFirst().orElse(null);
    }

    public AgencyIdListSummaryRecord getTargetAgencyIdListManifest(
            AgencyIdListSummaryRecord sourceAgencyIdList,
            List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        if (sourceAgencyIdList == null) {
            return null;
        }

        AgencyIdListSummaryRecord targetAgencyIdList = targetAgencyIdListList.stream()
                .filter(e -> e.guid().equals(sourceAgencyIdList.guid()))
                .findFirst().orElse(null);
        if (targetAgencyIdList == null) {
            // Issue #1356
            // End-user agency ID list assigned to a source BIE node can be carried into the uplifted BIE only
            // if the end-user agency ID list with the list ID, agency ID, and version exists (or has been uplifted)
            // in the target release and it is allowed by the target BIE node.
            targetAgencyIdList = targetAgencyIdListList.stream()
                    .filter(e -> StringUtils.equals(sourceAgencyIdList.name(), e.name()) &&
                            StringUtils.equals(sourceAgencyIdList.listId(), e.listId()) &&
                            StringUtils.equals(sourceAgencyIdList.agencyIdListValueName(), e.agencyIdListValueName()) &&
                            StringUtils.equals(sourceAgencyIdList.versionId(), e.versionId()))
                    .findFirst().orElse(null);
        }

        if (targetAgencyIdList == null) {
            return null;
        }

        AgencyIdListSummaryRecord finalTargetAgencyIdList = targetAgencyIdList;
        return targetAgencyIdListList.stream()
                .filter(e -> e.agencyIdListId().equals(finalTargetAgencyIdList.agencyIdListId()))
                .findFirst().orElse(null);
    }

}
