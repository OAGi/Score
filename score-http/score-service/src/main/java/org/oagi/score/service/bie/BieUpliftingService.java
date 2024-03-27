package org.oagi.score.service.bie;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.AgencyIdListManifest;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.*;
import org.oagi.score.repo.api.corecomponent.ValueDomainReadRepository;
import org.oagi.score.repo.api.corecomponent.model.*;
import org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.release.ReleaseReadRepository;
import org.oagi.score.repo.api.release.model.GetReleaseRequest;
import org.oagi.score.repo.api.release.model.Release;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.bie.model.BieUpliftingCustomMappingTable;
import org.oagi.score.service.bie.model.BieUpliftingMapping;
import org.oagi.score.service.bie.model.BieUpliftingValidation;
import org.oagi.score.service.corecomponent.CcDocument;
import org.oagi.score.service.corecomponent.CcMatchingService;
import org.oagi.score.service.corecomponent.model.CcDocumentImpl;
import org.oagi.score.service.corecomponent.model.CcMatchingScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.repo.api.impl.jooq.utils.ScoreDigestUtils.sha256;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;
import static org.oagi.score.service.bie.model.BieUpliftingCustomMappingTable.extractManifestId;
import static org.oagi.score.service.bie.model.BieUpliftingCustomMappingTable.getLastTag;

@Service
@Transactional(readOnly = true)
public class BieUpliftingService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private BieReadService bieReadService;

    @Autowired
    private CcMatchingService ccMatchingService;

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
                    "ASCC-" + ((AsccManifest) this.ccAssociation).getAsccManifestId() :
                    "BCC-" + ((BccManifest) this.ccAssociation).getBccManifestId());
        }

        public CcAssociation getCcAssociation() {
            return ccAssociation;
        }

        public boolean isMatched(CcAssociation ccAssociation) {
            return this.ccAssociation.equals(ccAssociation);
        }
    }

    private List<Association> getAssociationsRegardingBases(String path, CcDocument ccDocument, AccManifest accManifest) {
        Stack<AccManifest> accManifestStack = new Stack();
        while (accManifest != null) {
            accManifestStack.push(accManifest);
            accManifest = ccDocument.getBasedAccManifest(accManifest);
        }

        List<Association> associations = new ArrayList();
        while (!accManifestStack.isEmpty()) {
            String parentPath = path + ">" + String.join(">", accManifestStack.stream()
                    .map(e -> "ACC-" + e.getAccManifestId()).collect(Collectors.toList()));
            accManifest = accManifestStack.pop();
            associations.addAll(getAssociationsRegardingGroup(parentPath, ccDocument, accManifest));
        }

        return associations;
    }

    private List<Association> getAssociationsRegardingGroup(String parentPath, CcDocument ccDocument, AccManifest accManifest) {
        Collection<CcAssociation> ccAssociations = ccDocument.getAssociations(accManifest);
        List<Association> associations = new ArrayList();
        for (CcAssociation ccAssociation : ccAssociations) {
            if (ccAssociation.isAscc()) {
                AsccManifest asccManifest = (AsccManifest) ccAssociation;
                AsccpManifest asccpManifest =
                        ccDocument.getAsccpManifest(asccManifest.getToAsccpManifestId());
                AccManifest roleOfAccManifest =
                        ccDocument.getRoleOfAccManifest(asccpManifest);
                Acc acc = ccDocument.getAcc(roleOfAccManifest);
                if (acc.isGroup()) {
                    associations.addAll(
                            getAssociationsRegardingGroup(
                                    String.join(">",
                                            Arrays.asList(parentPath,
                                                    "ASCC-" + asccManifest.getAsccManifestId(),
                                                    "ASCCP-" + asccpManifest.getAsccpManifestId(),
                                                    "ACC-" + roleOfAccManifest.getAccManifestId())
                                    ),
                                    ccDocument, roleOfAccManifest)
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
        private BigInteger targetAsccpManifestId;

        private Queue<AsccpManifest> targetAsccpManifestQueue = new LinkedBlockingQueue();
        private Queue<AccManifest> targetAccManifestQueue = new LinkedBlockingQueue();
        private Bbie previousBbie;
        private Queue<BccpManifest> targetBccpManifestQueue = new LinkedBlockingQueue();

        private String currentSourcePath;
        private String currentTargetPath;

        private Map<BigInteger, List<Association>> abieSourceAssociationsMap = new HashMap();
        private Map<BigInteger, List<Association>> abieTargetAssociationsMap = new HashMap();
        private Map<BigInteger, List<DtScManifest>> bbieTargetDtScManifestsMap = new HashMap();

        BieDiff(BieDocument sourceBieDocument, CcDocument targetCcDocument,
                BigInteger targetAsccpManifestId) {
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
        public void visitStart(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {
            AsccpManifest targetAsccpManifest = targetCcDocument.getAsccpManifest(targetAsccpManifestId);
            targetAsccpManifestQueue.offer(targetAsccpManifest);
        }

        @Override
        public void visitEnd(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {

        }

        @Override
        public void visitAbie(Abie abie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            // reused bie case;
            if (abie == null) {
                targetAccManifestQueue.poll();
                return;
            }
            AccManifest sourceAccManifest = sourceCcDocument.getAccManifest(
                    abie.getBasedAccManifestId()
            );
            List<Association> sourceAssociations =
                    getAssociationsRegardingBases(currentSourcePath, sourceCcDocument, sourceAccManifest);
            abieSourceAssociationsMap.put(abie.getAbieId(), sourceAssociations);

            AccManifest targetAccManifest = targetAccManifestQueue.poll();
            if (targetAccManifest != null) { // found matched acc
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(currentTargetPath, targetCcDocument, targetAccManifest);
                abieTargetAssociationsMap.put(abie.getAbieId(), targetAssociations);
            }
        }

        @Override
        public void visitAsbie(Asbie asbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            AsccManifest sourceAsccManifest = sourceCcDocument.getAsccManifest(asbie.getBasedAsccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceAsccManifest)).findAny().orElse(null);

            List<Association> targetAssociations =
                    abieTargetAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isAscc())
                    .map(e -> ccMatchingService.score(
                            sourceCcDocument,
                            sourceAssociation,
                            targetCcDocument,
                            e,
                            (ccDocument, association) -> ccDocument.getAscc((AsccManifest) association.getCcAssociation())))
                    .max(Comparator.comparing(CcMatchingScore::getScore))
                    .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedAsbie(asbie,
                            (AsccManifest) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedAsbie(asbie,
                            (AsccManifest) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition(),
                            (AsccManifest) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = targetAssociation.getPath();

                AsccpManifest toAsccpManifest = targetCcDocument.getAsccpManifest(
                        ((AsccManifest) targetAssociation.getCcAssociation()).getToAsccpManifestId());
                targetAsccpManifestQueue.offer(toAsccpManifest);
            }
        }

        @Override
        public void visitBbie(Bbie bbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccManifest sourceBccManifest = sourceCcDocument.getBccManifest(bbie.getBasedBccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceBccManifest)).findAny().orElse(null);

            List<Association> targetAssociations =
                    abieTargetAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isBcc())
                    .map(e -> ccMatchingService.score(
                            sourceCcDocument,
                            sourceAssociation,
                            targetCcDocument,
                            e,
                            (ccDocument, association) -> ccDocument.getBcc((BccManifest) association.getCcAssociation())))
                    .max(Comparator.comparing(CcMatchingScore::getScore))
                    .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedBbie(bbie,
                            (BccManifest) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbie(bbie,
                            (BccManifest) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition(),
                            (BccManifest) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                currentSourcePath = sourceAssociation.getPath();
                currentTargetPath = targetAssociation.getPath();

                BccpManifest toBccpManifest = targetCcDocument.getBccpManifest(
                        ((BccManifest) targetAssociation.getCcAssociation()).getToBccpManifestId());
                this.previousBbie = bbie;
                targetBccpManifestQueue.offer(toBccpManifest);
            }
        }

        @Override
        public void visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            // reused bie case;
            if (asbiep == null) {
                targetAsccpManifestQueue.poll();
                return;
            }
            AsccpManifest sourceAsccpManifest = sourceCcDocument.getAsccpManifest(
                    asbiep.getBasedAsccpManifestId());

            currentSourcePath = (hasLength(currentSourcePath)) ?
                    currentSourcePath + ">" + "ASCCP-" + sourceAsccpManifest.getAsccpManifestId() :
                    "ASCCP-" + sourceAsccpManifest.getAsccpManifestId();

            AsccpManifest targetAsccpManifest = targetAsccpManifestQueue.poll();
            if (targetAsccpManifest != null) { // found matched asccp
                AccManifest targetRoleOfAccManifest = targetCcDocument.getRoleOfAccManifest(targetAsccpManifest);
                targetAccManifestQueue.offer(targetRoleOfAccManifest);
                currentTargetPath = (hasLength(currentTargetPath)) ?
                        currentTargetPath + ">" + "ASCCP-" + targetAsccpManifest.getAsccpManifestId() :
                        "ASCCP-" + targetAsccpManifest.getAsccpManifestId();
            }
        }

        @Override
        public void visitBbiep(Bbiep bbiep, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccpManifest sourceBccpManifest = sourceCcDocument.getBccpManifest(
                    bbiep.getBasedBccpManifestId());
            DtManifest sourceDtManifest = sourceCcDocument.getDtManifest(
                    sourceBccpManifest.getBdtManifestId());
            currentSourcePath = currentSourcePath + ">" + "BCCP-" + sourceBccpManifest.getBccpManifestId() + ">" +
                    "DT-" + sourceDtManifest.getDtManifestId();

            BccpManifest targetBccpManifest = targetBccpManifestQueue.poll();
            if (targetBccpManifest != null) {
                BigInteger targetBdtManifestId = targetBccpManifest.getBdtManifestId();
                DtManifest targetDtManifest = targetCcDocument.getDtManifest(targetBdtManifestId);
                bbieTargetDtScManifestsMap.put(previousBbie.getBbieId(),
                        targetCcDocument.getDtScManifests(targetDtManifest));
                currentTargetPath = currentTargetPath + ">" + "BCCP-" + targetBccpManifest.getBccpManifestId() + ">" +
                        "DT-" + targetDtManifest.getDtManifestId();
            }
        }

        @Override
        public void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScManifest sourceDtScManifest = sourceCcDocument.getDtScManifest(bbieSc.getBasedDtScManifestId());

            String sourcePath = currentSourcePath + ">" + "DT_SC-" + sourceDtScManifest.getDtScManifestId();
            CcMatchingScore matchingScore =
                    bbieTargetDtScManifestsMap.getOrDefault(bbieSc.getBbieId(), Collections.emptyList()).stream()
                            .map(e -> ccMatchingService.score(
                                    sourceCcDocument,
                                    sourceDtScManifest,
                                    targetCcDocument,
                                    e,
                                    (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest)))
                            .max(Comparator.comparing(CcMatchingScore::getScore))
                            .orElse(new CcMatchingScore(0.0d, null, null));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedBbieSc(bbieSc, sourceDtScManifest, sourcePath, bbieSc.getDefinition());
                });
            } else {
                DtScManifest targetDtScManifest = (DtScManifest) matchingScore.getTarget();
                String targetPath = currentTargetPath + ">" + "DT_SC-" + targetDtScManifest.getDtScManifestId();

                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbieSc(bbieSc,
                            sourceDtScManifest, sourcePath, bbieSc.getDefinition(),
                            targetDtScManifest, targetPath);
                });
            }
        }
    }

    public FindTargetAsccpManifestResponse findTargetAsccpManifest(FindTargetAsccpManifestRequest request) {
        FindTargetAsccpManifestResponse response = new FindTargetAsccpManifestResponse();

        ReleaseReadRepository releaseReadRepository = scoreRepositoryFactory.createReleaseReadRepository();
        Release sourceRelease = releaseReadRepository.getRelease(new GetReleaseRequest(request.getRequester())
                        .withTopLevelAsbiepId(request.getTopLevelAsbiepId()))
                .getRelease();
        Release targetRelease = releaseReadRepository.getRelease(new GetReleaseRequest(request.getRequester())
                        .withReleaseId(request.getTargetReleaseId()))
                .getRelease();

        if (sourceRelease.compareTo(targetRelease) >= 0) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        FindNextAsccpManifestResponse findNextAsccpManifestResponse = scoreRepositoryFactory.createCcReadRepository().findNextAsccpManifest(
                new FindNextAsccpManifestRequest(request.getRequester())
                        .withAsccpManifestId(sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId())
                        .withNextReleaseId(request.getTargetReleaseId()));

        if (findNextAsccpManifestResponse.getNextAsccpManifestId() == null) {
            throw new ScoreDataAccessException("Unable to find the target ASCCP.");
        }

        response.setAsccpManifestId(findNextAsccpManifestResponse.getNextAsccpManifestId());
        if (request.isIncludingBieDocument()) {
            response.setBieDocument(sourceBieDocument);
        }
        response.setReleaseNum(findNextAsccpManifestResponse.getReleaseNum());
        return response;
    }

    public AnalysisBieUpliftingResponse analysisBieUplifting(AnalysisBieUpliftingRequest request) {
        AnalysisBieUpliftingResponse response = new AnalysisBieUpliftingResponse();

        FindTargetAsccpManifestResponse targetAsccpManifestResponse = findTargetAsccpManifest(
                new FindTargetAsccpManifestRequest(request.getRequester())
                        .withTopLevelAsbiepId(request.getTopLevelAsbiepId())
                        .withTargetReleaseId(request.getTargetReleaseId())
                        .withIncludingBieDocument(true));

        BieDocument sourceBieDocument = targetAsccpManifestResponse.getBieDocument();
        BigInteger targetAsccpManifestId = targetAsccpManifestResponse.getAsccpManifestId();

        CcDocument targetCcDocument = new CcDocumentImpl(scoreRepositoryFactory.createCcReadRepository()
                .getCcPackage(new GetCcPackageRequest(request.getRequester())
                        .withAsccpManifestId(targetAsccpManifestId))
                .getCcPackage());

        BieDiff bieDiff = new BieDiff(sourceBieDocument, targetCcDocument, targetAsccpManifestId);
        bieDiff.addListener(response);
        bieDiff.diff();

        return response;
    }

    private class BieUpliftingHandler implements BieVisitor {

        private ScoreUser requester;
        private List<BigInteger> bizCtxIds;
        private BieUpliftingCustomMappingTable customMappingTable;

        private BieDocument sourceBieDocument;
        private CcDocument targetCcDocument;
        private BigInteger targetAsccpManifestId;

        private Queue<AsccpManifest> targetAsccpManifestQueue = new LinkedBlockingQueue();
        private Queue<AccManifest> targetAccManifestQueue = new LinkedBlockingQueue();
        private Bbie previousBbie;
        private Queue<BccpManifest> targetBccpManifestQueue = new LinkedBlockingQueue();

        private String currentSourcePath;
        private String currentTargetPath;

        private Map<BigInteger, List<Association>> abieSourceAssociationsMap = new HashMap();
        private Map<BigInteger, List<Association>> abieTargetAssociationsMap = new HashMap();
        private Map<BigInteger, List<DtScManifest>> bbieTargetDtScManifestsMap = new HashMap();

        private List<CodeListManifest> sourceCodeListManifestList;
        private List<CodeList> sourceCodeListList;
        private List<CodeListManifest> targetCodeListManifestList;
        private List<CodeList> targetCodeListList;

        private List<AgencyIdListManifest> sourceAgencyIdListManifestList;
        private List<AgencyIdList> sourceAgencyIdListList;
        private List<AgencyIdListManifest> targetAgencyIdListManifestList;
        private List<AgencyIdList> targetAgencyIdListList;

        private Map<BigInteger, BdtPriRestri> sourceBdtPriRestriMap = new HashMap();
        private Map<BigInteger, List<BdtPriRestri>> targetBdtPriRestriBdtManifestIdMap = new HashMap();

        private Map<BigInteger, BdtScPriRestri> sourceBdtScPriRestriMap = new HashMap();
        private Map<BigInteger, List<BdtScPriRestri>> targetBdtScPriRestriBdtScManifestIdMap = new HashMap();

        private Map<BigInteger, WrappedAsbiep> asbiepMap;
        private Map<BigInteger, WrappedAsbiep> roleOfAbieToAsbiepMap;
        private Map<BigInteger, Abie> abieIdToAbieMap;
        private Map<BigInteger, List<WrappedAsbie>> toAsbiepToAsbieMap;
        private Map<BigInteger, WrappedBbie> toBbiepToBbieMap;
        private Map<BigInteger, Bbie> bbieMap;
        private List<WrappedBbieSc> bbieScList;

        private BigInteger targetTopLevelAsbiepId;

        BieUpliftingHandler(ScoreUser requester, List<BigInteger> bizCtxIds,
                            BieUpliftingCustomMappingTable customMappingTable,
                            BieDocument sourceBieDocument, CcDocument targetCcDocument,
                            BigInteger targetAsccpManifestId,
                            List<CodeListManifest> sourceCodeListManifestList,
                            List<CodeList> sourceCodeListList,
                            List<CodeListManifest> targetCodeListManifestList,
                            List<CodeList> targetCodeListList,
                            Map<BigInteger, BdtPriRestri> sourceBdtPriRestriMap,
                            Map<BigInteger, List<BdtPriRestri>> targetBdtPriRestriBdtManifestIdMap,
                            Map<BigInteger, BdtScPriRestri> sourceBdtScPriRestriMap,
                            Map<BigInteger, List<BdtScPriRestri>> targetBdtScPriRestriBdtScManifestIdMap,
                            List<AgencyIdListManifest> sourceAgencyIdListManifestList,
                            List<AgencyIdList> sourceAgencyIdListList,
                            List<AgencyIdListManifest> targetAgencyIdListManifestList,
                            List<AgencyIdList> targetAgencyIdListList) {

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

            this.sourceCodeListManifestList = sourceCodeListManifestList;
            this.sourceCodeListList = sourceCodeListList;
            this.targetCodeListManifestList = targetCodeListManifestList;
            this.targetCodeListList = targetCodeListList;

            this.sourceBdtPriRestriMap = sourceBdtPriRestriMap;
            this.targetBdtPriRestriBdtManifestIdMap = targetBdtPriRestriBdtManifestIdMap;
            this.sourceBdtScPriRestriMap = sourceBdtScPriRestriMap;
            this.targetBdtScPriRestriBdtScManifestIdMap = targetBdtScPriRestriBdtScManifestIdMap;

            this.sourceAgencyIdListManifestList = sourceAgencyIdListManifestList;
            this.sourceAgencyIdListList = sourceAgencyIdListList;
            this.targetAgencyIdListManifestList = targetAgencyIdListManifestList;
            this.targetAgencyIdListList = targetAgencyIdListList;
        }

        public BigInteger uplift() {
            this.sourceBieDocument.accept(this);
            return targetTopLevelAsbiepId;
        }

        @Override
        public void visitStart(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {
            AsccpManifest targetAsccpManifest = targetCcDocument.getAsccpManifest(targetAsccpManifestId);
            targetAsccpManifestQueue.offer(targetAsccpManifest);
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
                    BigInteger accManifestId = new BigInteger(tag.substring(tag.indexOf('-') + 1));
                    AccManifest accManifest = this.targetCcDocument.getAccManifest(accManifestId);
                    Acc acc = this.targetCcDocument.getAcc(accManifest);
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
        public void visitEnd(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {
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
                                    BigInteger targetAccManifestId = extractManifestId(getLastTag(targetFromAbiePath));
                                    AccManifest targetAccManifest =
                                            targetCcDocument.getAccManifest(targetAccManifestId);

                                    targetFromAbie = new Abie();
                                    targetFromAbie.setGuid(ScoreGuidUtils.randomGuid());
                                    targetFromAbie.setBasedAccManifestId(targetAccManifest.getAccManifestId());
                                    targetFromAbie.setPath(targetFromAbiePath);
                                    targetFromAbie.setHashPath(sha256(targetFromAbie.getPath()));

                                    emptySourceAbieList.add(targetFromAbie);
                                }

                                break;
                        }

                        switch (mapping.getBieType()) {
                            case "ASBIE":
                                AsccManifest targetAsccManifest =
                                        targetCcDocument.getAsccManifest(mapping.getTargetManifestId());
                                Ascc targetAscc = targetCcDocument.getAscc(targetAsccManifest);
                                AsccpManifest targetAsccpManifest =
                                        targetCcDocument.getAsccpManifest(targetAsccManifest.getToAsccpManifestId());
                                AccManifest targetRoleOfAccManifest =
                                        targetCcDocument.getAccManifest(targetAsccpManifest.getRoleOfAccManifestId());

                                Asbie targetAsbie = new Asbie();
                                targetAsbie.setGuid(ScoreGuidUtils.randomGuid());
                                targetAsbie.setBasedAsccManifestId(targetAsccManifest.getAsccManifestId());
                                targetAsbie.setPath(mapping.getTargetPath());
                                targetAsbie.setHashPath(sha256(targetAsbie.getPath()));
                                targetAsbie.setCardinalityMin(targetAscc.getCardinalityMin());
                                targetAsbie.setCardinalityMax(targetAscc.getCardinalityMax());
                                targetAsbie.setNillable(false);
                                targetAsbie.setDeprecated(false);
                                targetAsbie.setUsed(true);

                                WrappedAsbie wrappedAsbie = new WrappedAsbie();
                                wrappedAsbie.setAsbie(targetAsbie);
                                emptySourceAsbieList.add(wrappedAsbie);

                                Asbiep targetAsbiep = new Asbiep();
                                targetAsbiep.setGuid(ScoreGuidUtils.randomGuid());
                                targetAsbiep.setBasedAsccpManifestId(targetAsccpManifest.getAsccpManifestId());
                                targetAsbiep.setPath(targetAsbie.getPath() + ">" + "ASCCP-" + targetAsccpManifest.getAsccpManifestId());
                                targetAsbiep.setHashPath(sha256(targetAsbiep.getPath()));

                                WrappedAsbiep wrappedAsbiep = new WrappedAsbiep();
                                wrappedAsbie.setToAsbiep(wrappedAsbiep);
                                wrappedAsbiep.setAsbiep(targetAsbiep);
                                emptySourceAsbiepList.add(wrappedAsbiep);

                                String targetRoleOfAccPath = targetAsbiep.getPath() + ">" + "ACC-" + targetRoleOfAccManifest.getAccManifestId();
                                Abie targetRoleOfAbie = getAbieIfExist.apply(targetRoleOfAccPath);
                                if (targetRoleOfAbie == null) {
                                    targetRoleOfAbie = new Abie();
                                    targetRoleOfAbie.setGuid(ScoreGuidUtils.randomGuid());
                                    targetRoleOfAbie.setBasedAccManifestId(targetRoleOfAccManifest.getAccManifestId());
                                    targetRoleOfAbie.setPath(targetRoleOfAccPath);
                                    targetRoleOfAbie.setHashPath(sha256(targetRoleOfAbie.getPath()));

                                    emptySourceAbieList.add(targetRoleOfAbie);
                                }

                                break;

                            case "BBIE":
                                BccManifest targetBccManifest =
                                        targetCcDocument.getBccManifest(mapping.getTargetManifestId());
                                Bcc targetBcc = targetCcDocument.getBcc(targetBccManifest);
                                BccpManifest targetBccpManifest =
                                        targetCcDocument.getBccpManifest(targetBccManifest.getToBccpManifestId());
                                DtManifest targetDtManifest =
                                        targetCcDocument.getDtManifest(targetBccpManifest.getBdtManifestId());
                                BdtPriRestri targetDefaultBdtPriRestri =
                                        targetCcDocument.getBdtPriRestriList(targetDtManifest).stream()
                                                .filter(e -> e.isDefault())
                                                .findFirst().get();

                                Bbie targetBbie = new Bbie();
                                targetBbie.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbie.setBasedBccManifestId(targetBccManifest.getBccManifestId());
                                targetBbie.setPath(mapping.getTargetPath());
                                targetBbie.setHashPath(sha256(targetBbie.getPath()));
                                targetBbie.setBdtPriRestriId(targetDefaultBdtPriRestri.getBdtPriRestriId());
                                targetBbie.setDefaultValue(targetBcc.getDefaultValue());
                                targetBbie.setFixedValue(targetBcc.getFixedValue());
                                targetBbie.setCardinalityMin(targetBcc.getCardinalityMin());
                                targetBbie.setCardinalityMax(targetBcc.getCardinalityMax());
                                targetBbie.setNillable(targetBcc.isNillable());
                                targetBbie.setUsed(true);

                                WrappedBbie wrappedBbie = new WrappedBbie();
                                wrappedBbie.setBbie(targetBbie);
                                emptySourceBbieList.add(wrappedBbie);

                                Bbiep targetBbiep = new Bbiep();
                                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbiep.setBasedBccpManifestId(targetBccpManifest.getBccpManifestId());
                                targetBbiep.setPath(targetBbie.getPath() + ">" + "BCCP-" + targetBccpManifest.getBccpManifestId());
                                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));

                                wrappedBbie.setToBbiep(targetBbiep);
                                emptySourceBbiepList.add(targetBbiep);

                                break;

                            case "BBIE_SC":
                                DtScManifest targetDtScManifest =
                                        targetCcDocument.getDtScManifest(mapping.getTargetManifestId());
                                DtSc targetDtSc = targetCcDocument.getDtSc(targetDtScManifest);
                                BdtScPriRestri targetDefaultBdtScPriRestri =
                                        targetCcDocument.getBdtScPriRestriList(targetDtScManifest).stream()
                                                .filter(e -> e.isDefault())
                                                .findFirst().get();

                                BbieSc targetBbieSc = new BbieSc();
                                targetBbieSc.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbieSc.setBasedDtScManifestId(targetDtScManifest.getDtScManifestId());
                                targetBbieSc.setPath(mapping.getTargetPath());
                                targetBbieSc.setHashPath(sha256(targetBbieSc.getPath()));
                                targetBbieSc.setDtScPriRestriId(targetDefaultBdtScPriRestri.getBdtScPriRestriId());
                                targetBbieSc.setDefaultValue(targetDtSc.getDefaultValue());
                                targetBbieSc.setFixedValue(targetDtSc.getFixedValue());
                                targetBbieSc.setCardinalityMin(targetDtSc.getCardinalityMin());
                                targetBbieSc.setCardinalityMax(targetDtSc.getCardinalityMax());
                                targetBbieSc.setUsed(true);

                                WrappedBbieSc wrappedBbieSc = new WrappedBbieSc();
                                wrappedBbieSc.setBbieSc(targetBbieSc);
                                emptySourceBbieScList.add(wrappedBbieSc);

                                break;
                        }
                    });

            CreateBieRequest createBieRequest = new CreateBieRequest(this.requester);
            createBieRequest.setBizCtxIds(bizCtxIds);
            createBieRequest.setStatus(topLevelAsbiep.getStatus());
            createBieRequest.setVersion(topLevelAsbiep.getVersion());
            createBieRequest.setTopLevelAsbiep(this.asbiepMap.get(topLevelAsbiep.getAsbiepId()));
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
                                    asbie.setRefTopLevelAsbiepId(mapping.getRefTopLevelAsbiepId());

                                } else {
                                    if (asbiep.getRoleOfAbie() == null) {
                                        AsccpManifest targetAsccpManifest =
                                                targetCcDocument.getAsccpManifest(asbiep.getAsbiep().getBasedAsccpManifestId());
                                        String targetRoleOfAbiePath = asbiep.getAsbiep().getPath() + ">" + "ACC-" +
                                                targetAsccpManifest.getRoleOfAccManifestId();
                                        Abie targetRoleOfAbie = getAbieIfExist.apply(targetRoleOfAbiePath);
                                        if (targetRoleOfAbie == null) {
                                            throw new IllegalStateException();
                                        }
                                        asbiep.setRoleOfAbie(targetRoleOfAbie);
                                    }
                                }

                                return asbie;
                            })
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

            createBieRequest.setSourceTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
            createBieRequest.setSourceAction("Uplift");

            targetTopLevelAsbiepId = scoreRepositoryFactory.createBieWriteRepository()
                    .createBie(createBieRequest)
                    .getTopLevelAsbiepId();
        }

        @Override
        public void visitAbie(Abie abie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            if (abie == null) {
                targetAccManifestQueue.poll();
                return;
            }
            AccManifest sourceAccManifest = sourceCcDocument.getAccManifest(
                    abie.getBasedAccManifestId()
            );
            List<Association> sourceAssociations =
                    getAssociationsRegardingBases(currentSourcePath, sourceCcDocument, sourceAccManifest);
            abieSourceAssociationsMap.put(abie.getAbieId(), sourceAssociations);

            AccManifest targetAccManifest = targetAccManifestQueue.poll();
            if (targetAccManifest != null) { // found matched acc
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(currentTargetPath, targetCcDocument, targetAccManifest);
                abieTargetAssociationsMap.put(abie.getAbieId(), targetAssociations);

                currentTargetPath = currentTargetPath + ">" + "ACC-" + targetAccManifest.getAccManifestId();
                Abie targetAbie = new Abie();
                targetAbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAbie.setBasedAccManifestId(targetAccManifest.getAccManifestId());
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
            AsccManifest sourceAsccManifest = sourceCcDocument.getAsccManifest(asbie.getBasedAsccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceAsccManifest)).findAny().orElse(null);

            currentSourcePath = sourceAssociation.getPath();
            AsccManifest targetAsccManifest = null;
            BieUpliftingMapping targetAsccMapping =
                    this.customMappingTable.getTargetAsccMappingBySourcePath(currentSourcePath);
            if (targetAsccMapping != null) {
                currentTargetPath = targetAsccMapping.getTargetPath();
                targetAsccManifest = targetCcDocument.getAsccManifest(targetAsccMapping.getTargetManifestId());
            } else {
                List<Association> targetAssociations =
                        abieTargetAssociationsMap.getOrDefault(asbie.getFromAbieId(), Collections.emptyList());
                CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isAscc())
                        .map(e -> ccMatchingService.score(
                                sourceCcDocument,
                                sourceAssociation,
                                targetCcDocument,
                                e,
                                (ccDocument, association) -> ccDocument.getAscc((AsccManifest) association.getCcAssociation())))
                        .max(Comparator.comparing(CcMatchingScore::getScore))
                        .orElse(new CcMatchingScore(0.0d, null, null));
                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    currentTargetPath = targetAssociation.getPath();
                    targetAsccManifest = (AsccManifest) targetAssociation.getCcAssociation();
                }
            }

            if (targetAsccManifest != null) {
                AsccpManifest toAsccpManifest = targetCcDocument.getAsccpManifest(
                        targetAsccManifest.getToAsccpManifestId());
                targetAsccpManifestQueue.offer(toAsccpManifest);

                Asbie targetAsbie = new Asbie();
                targetAsbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbie.setBasedAsccManifestId(targetAsccManifest.getAsccManifestId());
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
            BccManifest sourceBccManifest = sourceCcDocument.getBccManifest(bbie.getBasedBccManifestId());
            List<Association> sourceAssociations =
                    abieSourceAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
            Association sourceAssociation = sourceAssociations.stream()
                    .filter(e -> e.isMatched(sourceBccManifest)).findAny().orElse(null);

            currentSourcePath = sourceAssociation.getPath();
            BccManifest targetBccManifest = null;
            BieUpliftingMapping targetBccMapping =
                    this.customMappingTable.getTargetBccMappingBySourcePath(currentSourcePath);
            if (targetBccMapping != null) {
                currentTargetPath = targetBccMapping.getTargetPath();
                targetBccManifest = targetCcDocument.getBccManifest(targetBccMapping.getTargetManifestId());
            } else {
                List<Association> targetAssociations =
                        abieTargetAssociationsMap.getOrDefault(bbie.getFromAbieId(), Collections.emptyList());
                CcMatchingScore matchingScore = targetAssociations.stream().filter(e -> e.getCcAssociation().isBcc())
                        .map(e -> ccMatchingService.score(
                                sourceCcDocument,
                                sourceAssociation,
                                targetCcDocument,
                                e,
                                (ccDocument, association) -> ccDocument.getBcc((BccManifest) association.getCcAssociation())))
                        .max(Comparator.comparing(CcMatchingScore::getScore))
                        .orElse(new CcMatchingScore(0.0d, null, null));

                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    currentTargetPath = targetAssociation.getPath();
                    targetBccManifest = (BccManifest) targetAssociation.getCcAssociation();
                }
            }

            if (targetBccManifest != null) {
                BccpManifest toBccpManifest = targetCcDocument.getBccpManifest(
                        targetBccManifest.getToBccpManifestId());
                this.previousBbie = bbie;
                targetBccpManifestQueue.offer(toBccpManifest);

                Bbie targetBbie = new Bbie();
                targetBbie.setGuid(ScoreGuidUtils.randomGuid());
                targetBbie.setBasedBccManifestId(targetBccManifest.getBccManifestId());
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

                setValueDomain(bbie, targetBbie, toBccpManifest.getBdtManifestId(),
                        sourceBdtPriRestriMap, targetBdtPriRestriBdtManifestIdMap,
                        sourceCodeListManifestList, sourceCodeListList,
                        sourceAgencyIdListManifestList, sourceAgencyIdListList);

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
                targetAsccpManifestQueue.poll();
                return;
            }
            AsccpManifest sourceAsccpManifest = sourceCcDocument.getAsccpManifest(
                    asbiep.getBasedAsccpManifestId());

            currentSourcePath = (hasLength(currentSourcePath)) ?
                    currentSourcePath + ">" + "ASCCP-" + sourceAsccpManifest.getAsccpManifestId() :
                    "ASCCP-" + sourceAsccpManifest.getAsccpManifestId();

            AsccpManifest targetAsccpManifest = targetAsccpManifestQueue.poll();
            if (targetAsccpManifest != null) { // found matched asccp
                AccManifest targetRoleOfAccManifest = targetCcDocument.getRoleOfAccManifest(targetAsccpManifest);
                targetAccManifestQueue.offer(targetRoleOfAccManifest);
                currentTargetPath = (hasLength(currentTargetPath)) ?
                        currentTargetPath + ">" + "ASCCP-" + targetAsccpManifest.getAsccpManifestId() :
                        "ASCCP-" + targetAsccpManifest.getAsccpManifestId();

                Asbiep targetAsbiep = new Asbiep();
                targetAsbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbiep.setBasedAsccpManifestId(targetAsccpManifest.getAsccpManifestId());
                targetAsbiep.setPath(currentTargetPath);
                targetAsbiep.setHashPath(sha256(targetAsbiep.getPath()));
                targetAsbiep.setDefinition(asbiep.getDefinition());
                targetAsbiep.setRemark(asbiep.getRemark());
                targetAsbiep.setBizTerm(asbiep.getBizTerm());

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
            BccpManifest sourceBccpManifest = sourceCcDocument.getBccpManifest(
                    bbiep.getBasedBccpManifestId());
            currentSourcePath = currentSourcePath + ">" + "BCCP-" + sourceBccpManifest.getBccpManifestId();

            BccpManifest targetBccpManifest = targetBccpManifestQueue.poll();
            if (targetBccpManifest != null) {
                currentTargetPath = currentTargetPath + ">" + "BCCP-" + targetBccpManifest.getBccpManifestId();

                BigInteger targetBdtManifestId = targetBccpManifest.getBdtManifestId();
                DtManifest targetDtManifest = targetCcDocument.getDtManifest(targetBdtManifestId);
                bbieTargetDtScManifestsMap.put(previousBbie.getBbieId(),
                        targetCcDocument.getDtScManifests(targetDtManifest));

                Bbiep targetBbiep = new Bbiep();
                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetBbiep.setBasedBccpManifestId(targetBccpManifest.getBccpManifestId());
                targetBbiep.setPath(currentTargetPath);
                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));
                targetBbiep.setDefinition(bbiep.getDefinition());
                targetBbiep.setRemark(bbiep.getRemark());
                targetBbiep.setBizTerm(bbiep.getBizTerm());

                this.toBbiepToBbieMap.get(bbiep.getBbiepId()).setToBbiep(targetBbiep);
            }
        }

        @Override
        public void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScManifest sourceDtScManifest = sourceCcDocument.getDtScManifest(bbieSc.getBasedDtScManifestId());
            DtManifest sourceDtManifest = sourceCcDocument.getDtManifest(sourceDtScManifest.getOwnerDtManifestId());

            String sourcePath = currentSourcePath + ">" + "DT-" + sourceDtManifest.getDtManifestId() +
                    ">" + "DT_SC-" + sourceDtScManifest.getDtScManifestId();
            DtScManifest targetDtScManifest = null;
            String targetPath = null;
            BieUpliftingMapping targetDtScMapping =
                    this.customMappingTable.getTargetDtScMappingBySourcePath(sourcePath);
            if (targetDtScMapping != null) {
                targetDtScManifest = targetCcDocument.getDtScManifest(targetDtScMapping.getTargetManifestId());
                targetPath = targetDtScMapping.getTargetPath();
            } else {
                CcMatchingScore matchingScore =
                        bbieTargetDtScManifestsMap.getOrDefault(bbieSc.getBbieId(), Collections.emptyList()).stream()
                                .map(e -> ccMatchingService.score(
                                        sourceCcDocument,
                                        sourceDtScManifest,
                                        targetCcDocument,
                                        e,
                                        (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest)))
                                .max(Comparator.comparing(CcMatchingScore::getScore))
                                .orElse(new CcMatchingScore(0.0d, null, null));
                if (matchingScore.getScore() > 0.0d) {
                    targetDtScManifest = (DtScManifest) matchingScore.getTarget();
                    DtManifest targetDtManifest = targetCcDocument.getDtManifest(targetDtScManifest.getOwnerDtManifestId());
                    targetPath = currentTargetPath + ">" + "DT-" + targetDtManifest.getDtManifestId() +
                            ">" + "DT_SC-" + targetDtScManifest.getDtScManifestId();
                }
            }

            if (targetDtScManifest != null) {
                BbieSc targetBbieSc = new BbieSc();
                targetBbieSc.setGuid(ScoreGuidUtils.randomGuid());
                targetBbieSc.setBasedDtScManifestId(targetDtScManifest.getDtScManifestId());
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
                targetBbieSc.setExample(bbieSc.getExample());
                targetBbieSc.setDeprecated(bbieSc.isDeprecated());
                targetBbieSc.setUsed(bbieSc.isUsed());

                setValueDomain(bbieSc, targetBbieSc, targetDtScManifest.getDtScManifestId(),
                        sourceBdtScPriRestriMap, targetBdtScPriRestriBdtScManifestIdMap,
                        sourceCodeListManifestList, sourceCodeListList,
                        sourceAgencyIdListManifestList, sourceAgencyIdListList);

                WrappedBbieSc upliftingBbieSc = new WrappedBbieSc();
                upliftingBbieSc.setBbie(this.bbieMap.get(bbieSc.getBbieId()));
                upliftingBbieSc.setBbieSc(targetBbieSc);

                this.bbieScList.add(upliftingBbieSc);
            }
        }

        private Bbie setValueDomain(Bbie sourceBbie,
                                    Bbie targetBbie,
                                    BigInteger dtManifestId,
                                    Map<BigInteger, BdtPriRestri> sourceMap,
                                    Map<BigInteger, List<BdtPriRestri>> targetMap,
                                    List<CodeListManifest> sourceCodeListManifestList,
                                    List<CodeList> sourceCodeListList,
                                    List<AgencyIdListManifest> sourceAgencyIdListManifestList,
                                    List<AgencyIdList> sourceAgencyIdListList) {

            BdtPriRestri targetDefaultBdtPriRestri;
            DtManifest targetDtManifest = targetCcDocument.getDtManifest(dtManifestId);
            Dt targetDt = targetCcDocument.getDt(targetDtManifest);

            BdtPriRestri source = sourceMap.get(sourceBbie.getBdtPriRestriId());
            List<BdtPriRestri> availableBdtPriRestriList = targetMap.get(targetDtManifest.getDtManifestId());

            if (sourceBbie.getBdtPriRestriId() != null) {
                BdtPriRestri matched = availableBdtPriRestriList.stream().filter(e -> e.getXbtId() != null && e.getXbtId().equals(source.getXbtId())).findFirst().orElse(null);
                if (!availableBdtPriRestriList.isEmpty() && matched != null) {
                    targetBbie.setBdtPriRestriId(matched.getBdtPriRestriId());
                }
            } else if (sourceBbie.getCodeListManifestId() != null) {
                CodeListManifest sourceCodeListManifest = sourceCodeListManifestList.stream().filter(e -> e.getCodeListManifestId().equals(sourceBbie.getCodeListManifestId())).findAny().orElse(null);
                CodeList sourceCodeList = sourceCodeListList.stream().filter(e -> e.getCodeListId().equals(sourceCodeListManifest.getCodeListId())).findFirst().orElse(null);
                CodeListManifest targetCodeListManifest = getTargetCodeListManifest(
                        sourceCodeListManifest, sourceCodeList, targetCodeListManifestList, targetCodeListList);
                if (targetCodeListManifest != null) {
                    targetBbie.setCodeListManifestId(targetCodeListManifest.getCodeListManifestId());
                }
            } else if (sourceBbie.getAgencyIdListManifestId() != null) {
                AgencyIdListManifest sourceAgencyIdListManifest = sourceAgencyIdListManifestList.stream().filter(e -> e.getAgencyIdListManifestId().equals(sourceBbie.getAgencyIdListManifestId())).findAny().orElse(null);
                AgencyIdList sourceAgencyIdList = sourceAgencyIdListList.stream().filter(e -> e.getAgencyIdListId().equals(sourceAgencyIdListManifest.getAgencyIdListId())).findFirst().orElse(null);
                AgencyIdListManifest targetAgencyIdListManifest = getTargetAgencyIdListManifest(
                        sourceAgencyIdListManifest, sourceAgencyIdList, targetAgencyIdListManifestList, targetAgencyIdListList);
                if (targetAgencyIdListManifest != null) {
                    targetBbie.setAgencyIdListManifestId(targetAgencyIdListManifest.getAgencyIdListManifestId());
                }
            }

            if (targetBbie.getBdtPriRestriId() == null &&
                    targetBbie.getCodeListManifestId() == null &&
                    targetBbie.getAgencyIdListManifestId() == null) {
                if ("Date Time".equals(targetDt.getDataTypeTerm())) {
                    targetDefaultBdtPriRestri =
                            targetCcDocument.getBdtPriRestriList(targetDtManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("date time"))
                                    .findFirst().get();
                } else if ("Date".equals(targetDt.getDataTypeTerm())) {
                    targetDefaultBdtPriRestri =
                            targetCcDocument.getBdtPriRestriList(targetDtManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("date"))
                                    .findFirst().get();
                } else if ("Time".equals(targetDt.getDataTypeTerm())) {
                    targetDefaultBdtPriRestri =
                            targetCcDocument.getBdtPriRestriList(targetDtManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("time"))
                                    .findFirst().get();
                } else {
                    targetDefaultBdtPriRestri =
                            targetCcDocument.getBdtPriRestriList(targetDtManifest).stream()
                                    .filter(e -> e.isDefault())
                                    .findFirst().get();
                }
                targetBbie.setBdtPriRestriId(targetDefaultBdtPriRestri.getBdtPriRestriId());
            }
            return targetBbie;
        }

        private BbieSc setValueDomain(BbieSc sourceBbieSc,
                                      BbieSc targetBbieSc,
                                      BigInteger dtScManifestId,
                                      Map<BigInteger, BdtScPriRestri> sourceMap,
                                      Map<BigInteger, List<BdtScPriRestri>> targetMap,
                                      List<CodeListManifest> sourceCodeListManifestList,
                                      List<CodeList> sourceCodeListList,
                                      List<AgencyIdListManifest> sourceAgencyIdListManifestList,
                                      List<AgencyIdList> sourceAgencyIdListList) {

            BdtScPriRestri targetDefaultBdtScPriRestri;
            DtScManifest targetDtScManifest = targetCcDocument.getDtScManifest(dtScManifestId);
            DtSc targetDtSc = targetCcDocument.getDtSc(targetDtScManifest);
            BdtScPriRestri source = sourceMap.get(sourceBbieSc.getDtScPriRestriId());
            List<BdtScPriRestri> availableBdtScPriRestriList = targetMap.get(targetDtScManifest.getDtScManifestId());

            if (sourceBbieSc.getDtScPriRestriId() != null) {
                BdtScPriRestri matched = availableBdtScPriRestriList.stream().filter(e -> e.getXbtId() != null && e.getXbtId().equals(source.getXbtId())).findFirst().orElse(null);
                if (!availableBdtScPriRestriList.isEmpty() && matched != null) {
                    targetBbieSc.setDtScPriRestriId(matched.getBdtScPriRestriId());
                }
            } else if (sourceBbieSc.getCodeListManifestId() != null) {
                CodeListManifest sourceCodeListManifest = sourceCodeListManifestList.stream().filter(e -> e.getCodeListManifestId().equals(sourceBbieSc.getCodeListManifestId())).findAny().orElse(null);
                CodeList sourceCodeList = sourceCodeListList.stream().filter(e -> e.getCodeListId().equals(sourceCodeListManifest.getCodeListId())).findFirst().orElse(null);
                CodeListManifest targetCodeListManifest = getTargetCodeListManifest(
                        sourceCodeListManifest, sourceCodeList, targetCodeListManifestList, targetCodeListList);
                if (targetCodeListManifest != null) {
                    targetBbieSc.setCodeListManifestId(targetCodeListManifest.getCodeListManifestId());
                }
            } else if (sourceBbieSc.getAgencyIdListManifestId() != null) {
                AgencyIdListManifest sourceAgencyIdListManifest = sourceAgencyIdListManifestList.stream().filter(e -> e.getAgencyIdListManifestId().equals(sourceBbieSc.getAgencyIdListManifestId())).findAny().orElse(null);
                AgencyIdList sourceAgencyIdList = sourceAgencyIdListList.stream().filter(e -> e.getAgencyIdListId().equals(sourceAgencyIdListManifest.getAgencyIdListId())).findFirst().orElse(null);
                AgencyIdListManifest targetAgencyIdListManifest = getTargetAgencyIdListManifest(
                        sourceAgencyIdListManifest, sourceAgencyIdList, targetAgencyIdListManifestList, targetAgencyIdListList);
                if (targetAgencyIdListManifest != null) {
                    targetBbieSc.setAgencyIdListManifestId(targetAgencyIdListManifest.getAgencyIdListManifestId());
                }
            }

            if (targetBbieSc.getDtScPriRestriId() == null &&
                    targetBbieSc.getCodeListManifestId() == null &&
                    targetBbieSc.getAgencyIdListManifestId() == null) {
                if ("Date Time".equals(targetDtSc.getRepresentationTerm())) {
                    targetDefaultBdtScPriRestri =
                            targetCcDocument.getBdtScPriRestriList(targetDtScManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("date time"))
                                    .findFirst().get();
                } else if ("Date".equals(targetDtSc.getRepresentationTerm())) {
                    targetDefaultBdtScPriRestri =
                            targetCcDocument.getBdtScPriRestriList(targetDtScManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("date"))
                                    .findFirst().get();
                } else if ("Time".equals(targetDtSc.getRepresentationTerm())) {
                    targetDefaultBdtScPriRestri =
                            targetCcDocument.getBdtScPriRestriList(targetDtScManifest).stream()
                                    .filter(e -> e.getXbtName().equalsIgnoreCase("time"))
                                    .findFirst().get();
                } else {
                    targetDefaultBdtScPriRestri =
                            targetCcDocument.getBdtScPriRestriList(targetDtScManifest).stream()
                                    .filter(e -> e.isDefault())
                                    .findFirst().get();
                }
                targetBbieSc.setDtScPriRestriId(targetDefaultBdtScPriRestri.getBdtScPriRestriId());
            }
            return targetBbieSc;
        }
    }

    @Transactional
    public UpliftBieResponse upliftBie(UpliftBieRequest request) {
        BigInteger targetAsccpManifestId = request.getTargetAsccpManifestId();
        if (targetAsccpManifestId == null) {
            targetAsccpManifestId = scoreRepositoryFactory.createCcReadRepository()
                    .findNextAsccpManifest(new FindNextAsccpManifestRequest(request.getRequester())
                            .withTopLevelAsbiepId(request.getTopLevelAsbiepId())
                            .withNextReleaseId(request.getTargetReleaseId()))
                    .getNextAsccpManifestId();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        CcDocument targetCcDocument = new CcDocumentImpl(scoreRepositoryFactory.createCcReadRepository()
                .getCcPackage(new GetCcPackageRequest(request.getRequester())
                        .withAsccpManifestId(targetAsccpManifestId))
                .getCcPackage());

        List<BigInteger> bizCtxIds = scoreRepositoryFactory.createBieReadRepository()
                .getAssignedBusinessContext(new GetAssignedBusinessContextRequest(request.getRequester())
                        .withTopLevelAsbiepId(request.getTopLevelAsbiepId()))
                .getBusinessContextIdList();

        List<BieUpliftingMapping> mappingList = request.getCustomMappingTable();
        BieUpliftingCustomMappingTable customMappingTable = new BieUpliftingCustomMappingTable(
                sourceBieDocument.getCcDocument(),
                targetCcDocument,
                mappingList);

        BigInteger sourceReleaseId = sourceBieDocument.getCcDocument().getAsccpManifest(sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId()).getReleaseId();
        BigInteger targetReleaseId = targetCcDocument.getAsccpManifest(targetAsccpManifestId).getReleaseId();

        ValueDomainReadRepository valueDomainReadRepository = scoreRepositoryFactory.createValueDomainReadRepository();
        List<CodeListManifest> sourceCodeListManifestList = valueDomainReadRepository.getCodeListManifestList(sourceReleaseId);
        List<CodeList> sourceCodeListList = valueDomainReadRepository.getCodeListList(sourceReleaseId);
        List<CodeListManifest> targetCodeListManifestList = valueDomainReadRepository.getCodeListManifestList(targetReleaseId);
        List<CodeList> targetCodeListList = valueDomainReadRepository.getCodeListList(targetReleaseId);

        List<AgencyIdListManifest> sourceAgencyIdListManifestList = valueDomainReadRepository.getAgencyIdListManifestList(sourceReleaseId);
        List<AgencyIdList> sourceAgencyIdListList = valueDomainReadRepository.getAgencyIdListList(sourceReleaseId);
        List<AgencyIdListManifest> targetAgencyIdListManifestList = valueDomainReadRepository.getAgencyIdListManifestList(targetReleaseId);
        List<AgencyIdList> targetAgencyIdListList = valueDomainReadRepository.getAgencyIdListList(targetReleaseId);

        Map<BigInteger, BdtPriRestri> sourceBdtPriRestriMap = valueDomainReadRepository.getBdtPriRestriMap(sourceReleaseId);
        Map<BigInteger, List<BdtPriRestri>> targetBdtPriRestriBdtManifestIdMap = valueDomainReadRepository.getBdtPriRestriByBdtManifestIdMap(targetReleaseId);

        Map<BigInteger, BdtScPriRestri> sourceBdtScPriRestriMap = valueDomainReadRepository.getBdtScPriRestriMap(sourceReleaseId);
        Map<BigInteger, List<BdtScPriRestri>> targetBdtScPriRestriBdtScManifestIdMap = valueDomainReadRepository.getBdtScPriRestriByBdtScManifestIdMap(targetReleaseId);

        BieUpliftingHandler upliftingHandler =
                new BieUpliftingHandler(request.getRequester(), bizCtxIds, customMappingTable,
                        sourceBieDocument, targetCcDocument, targetAsccpManifestId,
                        sourceCodeListManifestList, sourceCodeListList,
                        targetCodeListManifestList, targetCodeListList,
                        sourceBdtPriRestriMap, targetBdtPriRestriBdtManifestIdMap,
                        sourceBdtScPriRestriMap, targetBdtScPriRestriBdtScManifestIdMap,
                        sourceAgencyIdListManifestList, sourceAgencyIdListList,
                        targetAgencyIdListManifestList, targetAgencyIdListList);
        BigInteger targetTopLevelAsbiepId = upliftingHandler.uplift();

        UpliftBieResponse response = new UpliftBieResponse();
        response.setTopLevelAsbiepId(targetTopLevelAsbiepId);
        return response;
    }

    public UpliftValidationResponse validateBieUplifting(UpliftValidationRequest request) {
        UpliftValidationResponse response = new UpliftValidationResponse();
        List<BieUpliftingValidation> validations = new ArrayList<>();

        ReleaseReadRepository releaseReadRepository = scoreRepositoryFactory.createReleaseReadRepository();
        Release sourceRelease = releaseReadRepository.getRelease(new GetReleaseRequest(request.getRequester())
                        .withTopLevelAsbiepId(request.getTopLevelAsbiepId()))
                .getRelease();
        Release targetRelease = releaseReadRepository.getRelease(new GetReleaseRequest(request.getRequester())
                        .withReleaseId(request.getTargetReleaseId()))
                .getRelease();

        if (sourceRelease.compareTo(targetRelease) >= 0) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        CcDocument targetCcDocument = new CcDocumentImpl(scoreRepositoryFactory.createCcReadRepository()
                .getCcPackage(new GetCcPackageRequest(request.getRequester())
                        .withAsccpManifestId(request.getTargetAsccpManifestId()))
                .getCcPackage());

        ValueDomainReadRepository valueDomainReadRepository = scoreRepositoryFactory.createValueDomainReadRepository();
        List<CodeListManifest> sourceCodeListManifestList = valueDomainReadRepository.getCodeListManifestList(sourceRelease.getReleaseId());
        List<CodeList> sourceCodeListList = valueDomainReadRepository.getCodeListList(sourceRelease.getReleaseId());
        List<CodeListManifest> targetCodeListManifestList = valueDomainReadRepository.getCodeListManifestList(targetRelease.getReleaseId());
        List<CodeList> targetCodeListList = valueDomainReadRepository.getCodeListList(request.getTargetReleaseId());

        List<AgencyIdListManifest> sourceAgencyIdListManifestList = valueDomainReadRepository.getAgencyIdListManifestList(sourceRelease.getReleaseId());
        List<AgencyIdList> sourceAgencyIdListList = valueDomainReadRepository.getAgencyIdListList(sourceRelease.getReleaseId());
        List<AgencyIdListManifest> targetAgencyIdListManifestList = valueDomainReadRepository.getAgencyIdListManifestList(targetRelease.getReleaseId());
        List<AgencyIdList> targetAgencyIdListList = valueDomainReadRepository.getAgencyIdListList(request.getTargetReleaseId());

        Map<BigInteger, BdtPriRestri> sourceBdtPriRestriMap = valueDomainReadRepository.getBdtPriRestriMap(sourceRelease.getReleaseId());
        Map<BigInteger, List<BdtPriRestri>> targetBdtPriRestriBdtManifestIdMap = valueDomainReadRepository.getBdtPriRestriByBdtManifestIdMap(request.getTargetReleaseId());

        Map<BigInteger, BdtScPriRestri> sourceBdtScPriRestriMap = valueDomainReadRepository.getBdtScPriRestriMap(sourceRelease.getReleaseId());
        Map<BigInteger, List<BdtScPriRestri>> targetBdtScPriRestriBdtScManifestIdMap = valueDomainReadRepository.getBdtScPriRestriByBdtScManifestIdMap(request.getTargetReleaseId());

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
                    Bbie bbie = sourceBieDocument.getBbie(mapping.getBieId());
                    BigInteger bccManifestId = mapping.getTargetManifestId();
                    if (bccManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    BccManifest bccManifest = targetCcDocument.getBccManifest(bccManifestId);
                    BccpManifest bccpManifest = targetCcDocument.getBccpManifest(bccManifest.getToBccpManifestId());
                    DtManifest dtManifest = targetCcDocument.getDtManifest(bccpManifest.getBdtManifestId());

                    if (bbie.getBdtPriRestriId() != null) {
                        validation.setMessage(checkBdtPriRestriIdMappable(bbie.getBdtPriRestriId(), dtManifest.getDtManifestId(), sourceBdtPriRestriMap, targetBdtPriRestriBdtManifestIdMap));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbie.getCodeListManifestId() != null) {
                        CodeListManifest sourceCodeListManifest = sourceCodeListManifestList.stream().filter(codeListManifest -> codeListManifest.getCodeListManifestId().equals(bbie.getCodeListManifestId())).findFirst().orElse(null);
                        CodeList sourceCodeList = sourceCodeListList.stream().filter(codeList -> codeList.getCodeListId().equals(sourceCodeListManifest.getCodeListId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtCodeListManifestIdMappable(
                                sourceCodeListManifest, sourceCodeList, dtManifest.getDtManifestId(), targetBdtPriRestriBdtManifestIdMap, targetCodeListManifestList, targetCodeListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else {
                        AgencyIdListManifest sourceAgencyIdListManifest = sourceAgencyIdListManifestList.stream().filter(agencyIdListManifest -> agencyIdListManifest.getAgencyIdListManifestId().equals(bbie.getAgencyIdListManifestId())).findFirst().orElse(null);
                        AgencyIdList sourceAgencyIdList = sourceAgencyIdListList.stream().filter(agencyIdList -> agencyIdList.getAgencyIdListId().equals(sourceAgencyIdListManifest.getAgencyIdListId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtAgencyIdListManifestIdMappable(
                                sourceAgencyIdListManifest, sourceAgencyIdList, dtManifest.getDtManifestId(), targetBdtPriRestriBdtManifestIdMap, targetAgencyIdListManifestList, targetAgencyIdListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    }
                    break;
                case "BBIE_SC":
                    BbieSc bbieSc = sourceBieDocument.getBbieSc(mapping.getBieId());
                    BigInteger dtScManifestId = mapping.getTargetManifestId();
                    if (dtScManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    DtScManifest dtScManifest = targetCcDocument.getDtScManifest(dtScManifestId);

                    if (bbieSc.getDtScPriRestriId() != null) {
                        validation.setMessage(checkBdtScPriRestriIdMappable(bbieSc.getDtScPriRestriId(), dtScManifest.getDtScManifestId(), sourceBdtScPriRestriMap, targetBdtScPriRestriBdtScManifestIdMap));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbieSc.getCodeListManifestId() != null) {
                        CodeListManifest sourceCodeListManifest = sourceCodeListManifestList.stream().filter(codeListManifest -> codeListManifest.getCodeListManifestId().equals(bbieSc.getCodeListManifestId())).findFirst().orElse(null);
                        CodeList sourceCodeList = sourceCodeListList.stream().filter(codeList -> codeList.getCodeListId().equals(sourceCodeListManifest.getCodeListId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtScCodeListIdMappable(
                                sourceCodeListManifest, sourceCodeList, dtScManifest.getDtScManifestId(), targetBdtScPriRestriBdtScManifestIdMap, targetCodeListManifestList, targetCodeListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else {
                        AgencyIdListManifest sourceAgencyIdListManifest = sourceAgencyIdListManifestList.stream().filter(agencyIdListManifest -> agencyIdListManifest.getAgencyIdListManifestId().equals(bbieSc.getAgencyIdListManifestId())).findFirst().orElse(null);
                        AgencyIdList sourceAgencyIdList = sourceAgencyIdListList.stream().filter(agencyIdList -> agencyIdList.getAgencyIdListId().equals(sourceAgencyIdListManifest.getAgencyIdListId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtScAgencyIdListIdMappable(
                                sourceAgencyIdListManifest, sourceAgencyIdList, dtScManifest.getDtScManifestId(), targetBdtScPriRestriBdtScManifestIdMap, targetAgencyIdListManifestList, targetAgencyIdListList));
                        validation.setValid(validation.getMessage().isEmpty());
                    }
                    break;
            }
            validations.add(validation);
        });
        response.setValidations(validations);
        return response;
    }

    private String checkBdtPriRestriIdMappable(BigInteger bdtPriRestriId,
                                               BigInteger targetBdtManifestId,
                                               Map<BigInteger, BdtPriRestri> sourceMap,
                                               Map<BigInteger, List<BdtPriRestri>> targetMap) {
        BdtPriRestri source = sourceMap.get(bdtPriRestriId);
        List<BdtPriRestri> availableBdtPriRestriList = targetMap.get(targetBdtManifestId);

        if (!availableBdtPriRestriList.isEmpty() && availableBdtPriRestriList.stream().anyMatch(e -> e.getXbtId() != null && e.getXbtId().equals(source.getXbtId()))) {
            return "";
        }
        return "Primitive value '" + source.getXbtName() + "' is not allowed in the target node. Uplifted node will use its default primitive in the domain value restriction.";
    }

    private String checkBdtScPriRestriIdMappable(BigInteger bdtScPriRestriId,
                                                 BigInteger targetBdtScManifestId,
                                                 Map<BigInteger, BdtScPriRestri> sourceMap,
                                                 Map<BigInteger, List<BdtScPriRestri>> targetMap) {
        BdtScPriRestri source = sourceMap.get(bdtScPriRestriId);
        List<BdtScPriRestri> availableBdtScPriRestriList = targetMap.get(targetBdtScManifestId);

        if (!availableBdtScPriRestriList.isEmpty() && availableBdtScPriRestriList.stream().anyMatch(e -> e.getXbtId() != null && e.getXbtId().equals(source.getXbtId()))) {
            return "";
        }
        return "Primitive value '" + source.getXbtName() + "' is not allowed in the target node. Uplifted node will use its default primitive in the domain value restriction.";
    }

    private String checkBdtCodeListManifestIdMappable(CodeListManifest sourceCodeListManifest,
                                                      CodeList sourceCodeList,
                                                      BigInteger targetBdtManifestId,
                                                      Map<BigInteger, List<BdtPriRestri>> targetMap,
                                                      List<CodeListManifest> targetCodeListManifestList,
                                                      List<CodeList> targetCodeListList) {
        CodeListManifest targetCodeListManifest = getTargetCodeListManifest(
                sourceCodeListManifest, sourceCodeList, targetCodeListManifestList, targetCodeListList);
        if (targetCodeListManifest != null) {
            return "";
        }
        return "Code List '" + sourceCodeList.getName() + "' is not allowed in the target node or the system cannot find the exact match code list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtScCodeListIdMappable(CodeListManifest sourceCodeListManifest,
                                                CodeList sourceCodeList,
                                                BigInteger targetBdtScManifestId,
                                                Map<BigInteger, List<BdtScPriRestri>> targetMap,
                                                List<CodeListManifest> targetCodeListManifestList,
                                                List<CodeList> targetCodeListList) {
        CodeListManifest targetCodeListManifest = getTargetCodeListManifest(
                sourceCodeListManifest, sourceCodeList, targetCodeListManifestList, targetCodeListList);
        if (targetCodeListManifest != null) {
            return "";
        }
        return "Code List '" + sourceCodeList.getName() + "' is not allowed in the target node or the system cannot find the exact match code list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtAgencyIdListManifestIdMappable(AgencyIdListManifest sourceAgencyIdListManifest,
                                                          AgencyIdList sourceAgencyIdList,
                                                          BigInteger targetBdtManifestId,
                                                          Map<BigInteger, List<BdtPriRestri>> targetMap,
                                                          List<AgencyIdListManifest> targetAgencyIdListManifestList,
                                                          List<AgencyIdList> targetAgencyIdListList) {
        AgencyIdListManifest targetAgencyIdListManifest = getTargetAgencyIdListManifest(
                sourceAgencyIdListManifest, sourceAgencyIdList, targetAgencyIdListManifestList, targetAgencyIdListList);
        if (targetAgencyIdListManifest != null) {
            return "";
        }
        return "Agency ID List '" + sourceAgencyIdList.getName() + "' is not allowed in the target node or the system cannot find the exact match agency ID list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    private String checkBdtScAgencyIdListIdMappable(AgencyIdListManifest sourceAgencyIdListManifest,
                                                    AgencyIdList sourceAgencyIdList,
                                                    BigInteger targetBdtScManifestId,
                                                    Map<BigInteger, List<BdtScPriRestri>> targetMap,
                                                    List<AgencyIdListManifest> targetAgencyIdListManifestList,
                                                    List<AgencyIdList> targetAgencyIdListList) {
        AgencyIdListManifest targetAgencyIdListManifest =
                getTargetAgencyIdListManifest(sourceAgencyIdListManifest, sourceAgencyIdList, targetAgencyIdListManifestList, targetAgencyIdListList);
        if (targetAgencyIdListManifest != null) {
            return "";
        }
        return "Agency ID List '" + sourceAgencyIdList.getName() + "' is not allowed in the target node or the system cannot find the exact match agency ID list in the target release, uplifted node will use a default primitive in the domain value restriction.";
    }

    public CodeListManifest getTargetCodeListManifest(
            CodeListManifest sourceCodeListManifest, CodeList sourceCodeList,
            List<CodeListManifest> targetCodeListManifestList, List<CodeList> targetCodeListList) {
        if (sourceCodeList == null) {
            return null;
        }

        CodeList targetCodeList = targetCodeListList.stream()
                .filter(e -> e.getGuid().equals(sourceCodeList.getGuid()))
                .findFirst().orElse(null);
        if (targetCodeList == null) {
            // Issue #1356
            // End-user code list assigned to a source BIE node can be carried into the uplifted BIE only
            // if the end-user code list with the same name, list ID, and agency ID exists (or has been uplifted)
            // in the target release and it is allowed by the target BIE node.
            targetCodeList = targetCodeListList.stream()
                    .filter(e -> StringUtils.equals(sourceCodeList.getName(), e.getName()) &&
                            StringUtils.equals(sourceCodeList.getListId(), e.getListId()) &&
                            StringUtils.equals(sourceCodeList.getAgencyName(), e.getAgencyName()) &&
                            StringUtils.equals(sourceCodeList.getVersionId(), e.getVersionId()))
                    .findFirst().orElse(null);
        }

        if (targetCodeList == null) {
            return null;
        }

        CodeList finalTargetCodeList = targetCodeList;
        return targetCodeListManifestList.stream()
                .filter(e -> e.getCodeListId().equals(finalTargetCodeList.getCodeListId()))
                .findFirst().orElse(null);
    }

    public AgencyIdListManifest getTargetAgencyIdListManifest(
            AgencyIdListManifest sourceAgencyIdListManifest, AgencyIdList sourceAgencyIdList,
            List<AgencyIdListManifest> targetAgencyIdListManifestList, List<AgencyIdList> targetAgencyIdListList) {
        if (sourceAgencyIdList == null) {
            return null;
        }

        AgencyIdList targetAgencyIdList = targetAgencyIdListList.stream()
                .filter(e -> e.getGuid().equals(sourceAgencyIdList.getGuid()))
                .findFirst().orElse(null);
        if (targetAgencyIdList == null) {
            // Issue #1356
            // End-user agency ID list assigned to a source BIE node can be carried into the uplifted BIE only
            // if the end-user agency ID list with the list ID, agency ID, and version exists (or has been uplifted)
            // in the target release and it is allowed by the target BIE node.
            targetAgencyIdList = targetAgencyIdListList.stream()
                    .filter(e -> StringUtils.equals(sourceAgencyIdList.getName(), e.getName()) &&
                            StringUtils.equals(sourceAgencyIdList.getListId(), e.getListId()) &&
                            StringUtils.equals(sourceAgencyIdList.getAgencyIdListValueName(), e.getAgencyIdListValueName()) &&
                            StringUtils.equals(sourceAgencyIdList.getVersionId(), e.getVersionId()))
                    .findFirst().orElse(null);
        }

        if (targetAgencyIdList == null) {
            return null;
        }

        AgencyIdList finalTargetAgencyIdList = targetAgencyIdList;
        return targetAgencyIdListManifestList.stream()
                .filter(e -> e.getAgencyIdListId().equals(finalTargetAgencyIdList.getAgencyIdListId()))
                .findFirst().orElse(null);
    }

}
