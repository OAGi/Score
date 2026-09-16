package org.oagi.score.gateway.http.api.bie_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.*;
import org.oagi.score.gateway.http.api.bie_management.service.BieAssociationPaths.Association;

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
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.*;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.BieAssociationPaths.getAssociationsRegardingBases;
import static org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable.extractManifestId;
import static org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable.getLastTag;
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
    private CodeListQueryService codeListQueryService;

    @Autowired
    private AgencyIdListQueryService agencyIdListQueryService;

    @Autowired
    private ReleaseQueryService releaseQueryService;

    private final CcMatchingService ccMatchingService;

    @Autowired
    BieUpliftingService(CcMatchingService ccMatchingService) {
        this.ccMatchingService = Objects.requireNonNull(ccMatchingService);
    }

    private String appendPath(String parentPath, String segment) {
        return hasLength(parentPath) ? parentPath + ">" + segment : segment;
    }

    /**
     * Selects the highest scoring target from a set of candidates.
     *
     * The analysis and generation traversals intentionally use the same matching
     * semantics.  Keeping candidate filtering, scoring, and empty-result handling
     * here prevents the two traversals from drifting apart.
     *
     * @param candidates non-null candidates in the same order used by the caller;
     *                   an empty collection produces the existing zero-score result
     */
    private <T, U extends CoreComponent<?>> CcMatchingScore<T> bestMatch(
            CcDocument sourceDocument,
            T source,
            CcDocument targetDocument,
            Collection<T> candidates,
            Predicate<T> candidateFilter,
            BiFunction<CcDocument, T, U> componentMapper) {
        Objects.requireNonNull(candidates, "candidates");
        return candidates.stream()
                .filter(candidateFilter)
                .map(candidate -> ccMatchingService.score(
                        sourceDocument, source, targetDocument, candidate, componentMapper))
                .max(Comparator.comparing(CcMatchingScore::getScore))
                .orElse(new CcMatchingScore<>(0.0d, null, null));
    }

    /** One state per immutable source occurrence path; target paths may change during assembly. */
    private static final class UpliftOccurrence {
        private String targetPath;
        private AsccpSummaryRecord targetAsccp;
        private BccpSummaryRecord targetBccp;
        private List<Association> targetAssociations = List.of();
        private List<DtScSummaryRecord> targetDtSc = List.of();
        private WrappedAsbie asbie;
        private WrappedAsbiep asbiep;
        private Abie abie;
        private WrappedBbie bbie;
        private WrappedBbieSc bbieSc;
        private AsbiepId sourceAsbiepId;
    }

    private abstract class UpliftTraversal implements BieVisitor {
        protected final Map<String, UpliftOccurrence> occurrences = new LinkedHashMap<>();

        protected UpliftOccurrence occurrence(BieVisitContext context) {
            return occurrences.computeIfAbsent(context.getOccurrencePath(), path -> new UpliftOccurrence());
        }

        protected UpliftOccurrence parent(BieVisitContext context) {
            String path = context.getParentOccurrencePath();
            return path == null ? null : occurrences.computeIfAbsent(path, ignored -> new UpliftOccurrence());
        }

        protected Association sourceAssociation(BieVisitContext context, CcAssociation association) {
            String path = context.getOccurrencePath();
            return new Association(path.substring(0, path.lastIndexOf('>')), association);
        }
    }

    private class BieDiff extends UpliftTraversal {

        private List<BieUpliftingListener> listeners = new ArrayList();

        private BieDocument sourceBieDocument;
        private CcDocument targetCcDocument;
        private AsccpManifestId targetAsccpManifestId;

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
        }

        @Override
        public void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {

        }

        @Override
        public BieVisitResult visitAbie(Abie abie, BieVisitContext context) {

            AsccpSummaryRecord parentAsccp = parent(context).targetAsccp;
            AccSummaryRecord targetAcc = parentAsccp == null ? null : targetCcDocument.getAcc(parentAsccp.roleOfAccManifestId());
            if (targetAcc != null) { // found matched acc
                String targetPath = parent(context).targetPath;
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(targetPath, targetCcDocument, targetAcc);
                occurrence(context).targetAssociations = targetAssociations;
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            AsccSummaryRecord sourceAscc = sourceCcDocument.getAscc(asbie.getBasedAsccManifestId());
            Association sourceAssociation = sourceAssociation(context, sourceAscc);

            List<Association> targetAssociations =
                    parent(context).targetAssociations;
            CcMatchingScore<Association> matchingScore = bestMatch(
                    sourceCcDocument,
                    sourceAssociation,
                    targetCcDocument,
                    targetAssociations,
                    e -> e.getCcAssociation().isAscc(),
                    (ccDocument, association) -> ccDocument.getAscc(
                            ((AsccSummaryRecord) association.getCcAssociation()).asccManifestId()));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedAsbie(asbie,
                            (AsccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition());
                });

                occurrence(context).targetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedAsbie(asbie,
                            (AsccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), asbie.getDefinition(),
                            (AsccSummaryRecord) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                occurrence(context).targetPath = targetAssociation.getPath();

                AsccpSummaryRecord toAsccp = targetCcDocument.getAsccp(
                        ((AsccSummaryRecord) targetAssociation.getCcAssociation()).toAsccpManifestId());
                occurrence(context).targetAsccp = toAsccp;
            }

            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccSummaryRecord sourceBcc = sourceCcDocument.getBcc(bbie.getBasedBccManifestId());
            Association sourceAssociation = sourceAssociation(context, sourceBcc);

            List<Association> targetAssociations =
                    parent(context).targetAssociations;
            CcMatchingScore<Association> matchingScore = bestMatch(
                    sourceCcDocument,
                    sourceAssociation,
                    targetCcDocument,
                    targetAssociations,
                    e -> e.getCcAssociation().isBcc(),
                    (ccDocument, association) -> ccDocument.getBcc(
                            ((BccSummaryRecord) association.getCcAssociation()).bccManifestId()));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                    listener.notFoundMatchedBbie(bbie,
                            (BccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition());
                });

                occurrence(context).targetPath = null;
            } else {
                Association targetAssociation = (Association) matchingScore.getTarget();
                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbie(bbie,
                            (BccSummaryRecord) sourceAssociation.getCcAssociation(), sourceAssociation.getPath(), bbie.getDefinition(),
                            (BccSummaryRecord) targetAssociation.getCcAssociation(), targetAssociation.getPath());
                });

                occurrence(context).targetPath = targetAssociation.getPath();

                BccpSummaryRecord toBccp = targetCcDocument.getBccp(
                        ((BccSummaryRecord) targetAssociation.getCcAssociation()).toBccpManifestId());
                occurrence(context).targetBccp = toBccp;
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            AsccpSummaryRecord targetAsccp = context.getParentOccurrencePath() == null
                    ? targetCcDocument.getAsccp(targetAsccpManifestId) : parent(context).targetAsccp;
            occurrence(context).targetAsccp = targetAsccp;
            if (targetAsccp != null) { // found matched asccp
                occurrence(context).targetPath = appendPath(
                        parent(context) == null ? null : parent(context).targetPath,
                        "ASCCP-" + targetAsccp.asccpManifestId());
            } else {
                occurrence(context).targetPath = null;
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbiep(Bbiep bbiep, BieVisitContext context) {

            BccpSummaryRecord targetBccp = occurrence(context).targetBccp;
            if (targetBccp != null) {
                DtManifestId targetBdtManifestId = targetBccp.dtManifestId();
                DtSummaryRecord targetDt = targetCcDocument.getDt(targetBdtManifestId);
                occurrence(context).targetDtSc =
                        targetCcDocument.getDtScList(targetDt.dtManifestId());
                occurrence(context).targetPath = appendPath(
                        occurrence(context).targetPath,
                        "BCCP-" + targetBccp.bccpManifestId());
            } else {
                occurrence(context).targetPath = null;
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScSummaryRecord sourceDtSc = sourceCcDocument.getDtSc(bbieSc.getBasedDtScManifestId());
            UpliftOccurrence owner = parent(context);
            String sourcePath = context.getOccurrencePath();
            CcMatchingScore<DtScSummaryRecord> matchingScore = bestMatch(
                    sourceCcDocument,
                    sourceDtSc,
                    targetCcDocument,
                    owner.targetDtSc,
                    ignored -> true,
                    (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest.dtScManifestId()));

            if (matchingScore.getScore() == 0.0d || matchingScore.getTarget() == null) {
                this.listeners.forEach(listener -> {
                            listener.notFoundMatchedBbieSc(bbieSc, sourceDtSc, sourcePath, bbieSc.getDefinition());
                });
            } else {
                DtScSummaryRecord targetDtSc = (DtScSummaryRecord) matchingScore.getTarget();
                String targetPath = appendPath(owner.targetPath,
                        "DT-" + targetDtSc.ownerDtManifestId() + ">DT_SC-" + targetDtSc.dtScManifestId());

                this.listeners.forEach(listener -> {
                    listener.foundBestMatchedBbieSc(bbieSc,
                            sourceDtSc, sourcePath, bbieSc.getDefinition(),
                            targetDtSc, targetPath);
                });
            }
            return BieVisitResult.CONTINUE;
        }
    }

    public AsccpSummaryRecord findTargetAsccpManifest(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {

        return findTargetAsccp(requester, topLevelAsbiepId, targetReleaseId).getKey();
    }

    private Pair<AsccpSummaryRecord, BieDocument> findTargetAsccp(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, ReleaseId targetReleaseId) {
        if (topLevelAsbiepId == null || targetReleaseId == null) {
            throw new IllegalArgumentException("Source BIE and target release are required.");
        }
        var topLevelAsbiep = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                .getTopLevelAsbiepSummary(topLevelAsbiepId);
        var releaseQuery = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord sourceRelease = (topLevelAsbiep == null || topLevelAsbiep.release() == null)
                ? null : releaseQuery.getReleaseSummary(topLevelAsbiep.release().releaseId());
        ReleaseSummaryRecord targetRelease = releaseQuery.getReleaseSummary(targetReleaseId);

        if (sourceRelease == null || sourceRelease.releaseId() == null) {
            throw new IllegalArgumentException("Source BIE release record not found.");
        }
        if (targetRelease == null || targetRelease.releaseId() == null) {
            throw new IllegalArgumentException("Target release record not found.");
        }

        if (!releaseQueryService.isLaterRelease(requester,
                sourceRelease.releaseId(), targetRelease.releaseId())) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(requester, topLevelAsbiepId);
        if (sourceBieDocument == null || sourceBieDocument.getRootAsbiep() == null) {
            throw new IllegalArgumentException("Source BIE record not found.");
        }
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

    private class BieUpliftingHandler extends UpliftTraversal {

        private ScoreUser requester;
        private List<BusinessContextId> bizCtxIds;
        private BieUpliftingCustomMappingTable customMappingTable;

        private BieDocument sourceBieDocument;
        private CcDocument targetCcDocument;
        private AsccpManifestId targetAsccpManifestId;

        private List<XbtSummaryRecord> sourceXbtList;
        private List<XbtSummaryRecord> targetXbtList;

        private List<CodeListSummaryRecord> sourceCodeListList;
        private List<CodeListSummaryRecord> targetCodeListList;

        private List<AgencyIdListSummaryRecord> sourceAgencyIdListList;
        private List<AgencyIdListSummaryRecord> targetAgencyIdListList;

        private final Map<String, Abie> targetAbies = new LinkedHashMap<>();
        private final Map<String, Bbie> targetBbies = new LinkedHashMap<>();
        private WrappedAsbiep rootAsbiep;
        private TopLevelAsbiepId targetTopLevelAsbiepId;

        BieUpliftingHandler(ScoreUser requester, List<BusinessContextId> bizCtxIds,
                            BieUpliftingCustomMappingTable customMappingTable,
                            BieDocument sourceBieDocument, CcDocument targetCcDocument,
                            AsccpManifestId targetAsccpManifestId,
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
            List<WrappedAsbie> emptySourceAsbieList = new ArrayList();
            List<WrappedBbie> emptySourceBbieList = new ArrayList();
            List<WrappedBbieSc> emptySourceBbieScList = new ArrayList();

            Function<String, Abie> getOrCreateAbie = path -> targetAbies.computeIfAbsent(path, key -> {
                AccSummaryRecord targetAcc = targetCcDocument.getAcc(
                        new AccManifestId(extractManifestId(getLastTag(key))));
                Abie abie = new Abie();
                abie.setGuid(ScoreGuidUtils.randomGuid());
                abie.setBasedAccManifestId(targetAcc.accManifestId());
                abie.setPath(key);
                abie.setHashPath(sha256(key));
                return abie;
            });

            this.customMappingTable.getMappingList().stream()
                    .filter(e -> !hasLength(e.getSourcePath()))
                    .forEach(mapping -> {
                        String mappingType = mapping.getBieType().toUpperCase(Locale.ROOT);
                        switch (mappingType) {
                            case "ASBIE":
                            case "BBIE":
                                String targetFromAbiePath = extractAbiePath(mapping.getTargetPath());
                                getOrCreateAbie.apply(targetFromAbiePath);
                                break;
                        }

                        switch (mappingType) {
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


                                String targetRoleOfAccPath = targetAsbiep.getPath() + ">" + "ACC-" + targetRoleOfAcc.accManifestId();
                                getOrCreateAbie.apply(targetRoleOfAccPath);

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
                                                .filter(Objects::nonNull)
                                                .filter(e -> e.isDefault())
                                                .findFirst().orElseThrow(() -> new IllegalStateException("Target DT has no default primitive."));

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
                                targetBbies.put(targetBbie.getPath(), targetBbie);

                                Bbiep targetBbiep = new Bbiep();
                                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                                targetBbiep.setBasedBccpManifestId(targetBccp.bccpManifestId());
                                targetBbiep.setPath(targetBbie.getPath() + ">" + "BCCP-" + targetBccp.bccpManifestId());
                                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));

                                wrappedBbie.setToBbiep(targetBbiep);


                                break;

                            case "BBIE_SC":
                                DtScSummaryRecord targetDtSc =
                                        targetCcDocument.getDtSc(new DtScManifestId(mapping.getTargetManifestId()));
                                DtScAwdPriSummaryRecord targetDefaultDtScAwdPri =
                                targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                                .filter(Objects::nonNull)
                                                .filter(e -> e.isDefault())
                                                .findFirst().orElseThrow(() -> new IllegalStateException("Target DT_SC has no default primitive."));

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
            createBieRequest.setTopLevelAsbiep(rootAsbiep);
            createBieRequest.setAsbieList(
                    Stream.concat(occurrences.values().stream().map(e -> e.asbie).filter(Objects::nonNull), emptySourceAsbieList.stream())
                            .map(asbie -> {
                                if (asbie.getFromAbie() == null) {
                                    String targetFromAbiePath = extractAbiePath(asbie.getAsbie().getPath());
                                    Abie targetFromAbie = getOrCreateAbie.apply(targetFromAbiePath);
                                    asbie.setFromAbie(targetFromAbie);
                                }

                                WrappedAsbiep asbiep = asbie.getToAsbiep();
                                if (asbiep == null) {
                                    String path = asbie.getAsbie().getPath();
                                    BieUpliftingMapping mapping = customMappingTable.getMappingByTargetPath(path);
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
                                        Abie targetRoleOfAbie = getOrCreateAbie.apply(targetRoleOfAbiePath);
                                        asbiep.setRoleOfAbie(targetRoleOfAbie);
                                    }
                                }

                                return asbie;
                            })
                            .filter(e -> e != null)
                            .collect(Collectors.toList()));
            createBieRequest.setBbieList(
                    Stream.concat(occurrences.values().stream().map(e -> e.bbie).filter(Objects::nonNull),
                                    emptySourceBbieList.stream())
                            .map(bbie -> {
                                if (bbie.getFromAbie() == null) {
                                    String targetFromAbiePath = extractAbiePath(bbie.getBbie().getPath());
                                    Abie targetFromAbie = getOrCreateAbie.apply(targetFromAbiePath);
                                    bbie.setFromAbie(targetFromAbie);
                                }

                                return bbie;
                            })
                            .collect(Collectors.toList()));
            createBieRequest.setBbieScList(
                    Stream.concat(occurrences.values().stream().map(e -> e.bbieSc).filter(Objects::nonNull),
                                    emptySourceBbieScList.stream())
                            .map(bbieSc -> {
                                if (bbieSc.getBbie() == null) {
                                    String targetBbiePath = extractBbiePath(bbieSc.getBbieSc().getPath());
                                    Bbie targetBbie = targetBbies.get(targetBbiePath);
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

            // Issue #1659
            occurrences.values().stream().filter(e -> e.asbiep != null).forEach(e ->
                    repositoryFactory.asbiepCommandRepository(requester)
                            .copyAsbiepSupportingDocumentation(e.sourceAsbiepId, e.asbiep.getAsbiep().getAsbiepId()));
        }

        @Override
        public BieVisitResult visitAbie(Abie abie, BieVisitContext context) {

            AsccpSummaryRecord parentAsccp = parent(context).targetAsccp;
            AccSummaryRecord targetAcc = parentAsccp == null ? null : targetCcDocument.getAcc(parentAsccp.roleOfAccManifestId());
            if (targetAcc != null) { // found matched acc
                String targetPath = parent(context).targetPath;
                List<Association> targetAssociations =
                        getAssociationsRegardingBases(targetPath, targetCcDocument, targetAcc);
                occurrence(context).targetAssociations = targetAssociations;

                targetPath = appendPath(targetPath, "ACC-" + targetAcc.accManifestId());
                Abie targetAbie = new Abie();
                targetAbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAbie.setBasedAccManifestId(targetAcc.accManifestId());
                targetAbie.setPath(targetPath);
                targetAbie.setHashPath(sha256(targetPath));
                targetAbie.setDefinition(abie.getDefinition());
                targetAbie.setRemark(abie.getRemark());
                targetAbie.setBizTerm(abie.getBizTerm());

                WrappedAsbiep targetAsbiep = parent(context).asbiep;
                if (targetAsbiep != null) {
                    targetAsbiep.setRoleOfAbie(targetAbie);
                }
                targetAbies.put(targetAbie.getPath(), targetAbie);
                occurrence(context).abie = targetAbie;
                occurrence(context).targetPath = targetPath;
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            AsccSummaryRecord sourceAsccManifest = sourceCcDocument.getAscc(asbie.getBasedAsccManifestId());
            Association sourceAssociation = sourceAssociation(context, sourceAsccManifest);

            AsccSummaryRecord targetAscc = null;
            BieUpliftingMapping targetAsccMapping =
                    this.customMappingTable.getMapping(context.getOccurrencePath());
            if (targetAsccMapping == null &&
                    this.customMappingTable.isAutoMappingSuppressed(context.getOccurrencePath())) {
                return BieVisitResult.CONTINUE;
            }
            if (targetAsccMapping != null) {
                occurrence(context).targetPath = targetAsccMapping.getTargetPath();
                targetAscc = targetCcDocument.getAscc(new AsccManifestId(targetAsccMapping.getTargetManifestId()));
            } else {
                List<Association> targetAssociations =
                    parent(context).targetAssociations;
                CcMatchingScore<Association> matchingScore = bestMatch(
                        sourceCcDocument,
                        sourceAssociation,
                        targetCcDocument,
                        targetAssociations,
                        e -> e.getCcAssociation().isAscc(),
                        (ccDocument, association) -> ccDocument.getAscc(
                                ((AsccSummaryRecord) association.getCcAssociation()).asccManifestId()));
                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    occurrence(context).targetPath = targetAssociation.getPath();
                    targetAscc = (AsccSummaryRecord) targetAssociation.getCcAssociation();
                }
            }

            if (targetAscc != null) {
                // Issue #1735: when the ASBIE is uplifted as a reuse reference
                // (the user mapped it to another top-level BIE via the reuse '!'),
                // create the reference ASBIE but do NOT descend into its subtree.
                // The subtree lives in the referenced BIE; re-traversing it would
                // create an inline copy in addition to the selected reference.
                boolean reuseReference =
                        (targetAsccMapping != null && targetAsccMapping.getRefTopLevelAsbiepId() != null);
                if (!reuseReference) {
                    AsccpSummaryRecord toAsccp = targetCcDocument.getAsccp(
                            targetAscc.toAsccpManifestId());
                    occurrence(context).targetAsccp = toAsccp;
                }

                Asbie targetAsbie = new Asbie();
                targetAsbie.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbie.setBasedAsccManifestId(targetAscc.asccManifestId());
                targetAsbie.setPath(occurrence(context).targetPath);
                targetAsbie.setHashPath(sha256(targetAsbie.getPath()));
                targetAsbie.setCardinalityMin(asbie.getCardinalityMin());
                targetAsbie.setCardinalityMax(asbie.getCardinalityMax());
                targetAsbie.setNillable(asbie.isNillable());
                targetAsbie.setDefinition(asbie.getDefinition());
                targetAsbie.setRemark(asbie.getRemark());
                targetAsbie.setDeprecated(asbie.isDeprecated());
                targetAsbie.setUsed(asbie.isUsed());

                WrappedAsbie upliftingAsbie = new WrappedAsbie();
                // A custom target may belong to a different branch. Resolve its owner
                // from the target path after all target occurrences have been assembled.
                if (targetAsccMapping == null) {
                    upliftingAsbie.setFromAbie(parent(context).abie);
                }

                upliftingAsbie.setAsbie(targetAsbie);

                if (targetAsccMapping != null) {
                    upliftingAsbie.setRefTopLevelAsbiepId(targetAsccMapping.getRefTopLevelAsbiepId());
                }
                occurrence(context).asbie = upliftingAsbie;

                // Skip descent into the reuse target's subtree; descend otherwise.
                return reuseReference ? BieVisitResult.SKIP_SUBTREE : BieVisitResult.CONTINUE;
            }

            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            BccSummaryRecord sourceBcc = sourceCcDocument.getBcc(bbie.getBasedBccManifestId());
            Association sourceAssociation = sourceAssociation(context, sourceBcc);

            BccSummaryRecord targetBcc = null;
            BieUpliftingMapping targetBccMapping =
                    this.customMappingTable.getMapping(context.getOccurrencePath());
            if (targetBccMapping == null &&
                    this.customMappingTable.isAutoMappingSuppressed(context.getOccurrencePath())) {
                return BieVisitResult.CONTINUE;
            }
            if (targetBccMapping != null) {
                occurrence(context).targetPath = targetBccMapping.getTargetPath();
                targetBcc = targetCcDocument.getBcc(
                        new BccManifestId(targetBccMapping.getTargetManifestId()));
            } else {
                List<Association> targetAssociations =
                    parent(context).targetAssociations;
                CcMatchingScore<Association> matchingScore = bestMatch(
                        sourceCcDocument,
                        sourceAssociation,
                        targetCcDocument,
                        targetAssociations,
                        e -> e.getCcAssociation().isBcc(),
                        (ccDocument, association) -> ccDocument.getBcc(
                                ((BccSummaryRecord) association.getCcAssociation()).bccManifestId()));

                if (matchingScore.getScore() > 0.0d) {
                    Association targetAssociation = (Association) matchingScore.getTarget();
                    occurrence(context).targetPath = targetAssociation.getPath();
                    targetBcc = (BccSummaryRecord) targetAssociation.getCcAssociation();
                }
            }

            if (targetBcc != null) {
                BccpSummaryRecord toBccp = targetCcDocument.getBccp(
                        targetBcc.toBccpManifestId());
                occurrence(context).targetBccp = toBccp;

                Bbie targetBbie = new Bbie();
                targetBbie.setGuid(ScoreGuidUtils.randomGuid());
                targetBbie.setBasedBccManifestId(targetBcc.bccManifestId());
                targetBbie.setPath(occurrence(context).targetPath);
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
                if (targetBccMapping == null) {
                    upliftingBbie.setFromAbie(parent(context).abie);
                }
                upliftingBbie.setBbie(targetBbie);

                occurrence(context).bbie = upliftingBbie;
                targetBbies.put(targetBbie.getPath(), targetBbie);
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            AsccpSummaryRecord targetAsccp = context.getParentOccurrencePath() == null
                    ? targetCcDocument.getAsccp(targetAsccpManifestId) : parent(context).targetAsccp;
            occurrence(context).targetAsccp = targetAsccp;
            if (targetAsccp != null) { // found matched asccp
                occurrence(context).targetPath = appendPath(
                        parent(context) == null ? null : parent(context).targetPath,
                        "ASCCP-" + targetAsccp.asccpManifestId());

                Asbiep targetAsbiep = new Asbiep();
                targetAsbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetAsbiep.setBasedAsccpManifestId(targetAsccp.asccpManifestId());
                targetAsbiep.setPath(occurrence(context).targetPath);
                targetAsbiep.setHashPath(sha256(targetAsbiep.getPath()));
                targetAsbiep.setDefinition(asbiep.getDefinition());
                targetAsbiep.setRemark(asbiep.getRemark());
                targetAsbiep.setBizTerm(asbiep.getBizTerm());
                targetAsbiep.setDisplayName(asbiep.getDisplayName());

                WrappedAsbiep upliftingAsbiep = new WrappedAsbiep();
                upliftingAsbiep.setAsbiep(targetAsbiep);

                WrappedAsbie parentAsbie = parent(context) == null ? null : parent(context).asbie;
                if (parentAsbie != null) {
                    parentAsbie.setToAsbiep(upliftingAsbiep);
                }
                occurrence(context).asbiep = upliftingAsbiep;
                occurrence(context).sourceAsbiepId = asbiep.getAsbiepId();
                if (context.getParentOccurrencePath() == null) {
                    rootAsbiep = upliftingAsbiep;
                }
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbiep(Bbiep bbiep, BieVisitContext context) {

            BccpSummaryRecord targetBccp = occurrence(context).targetBccp;
            if (targetBccp != null) {
                occurrence(context).targetPath = appendPath(
                        occurrence(context).targetPath,
                        "BCCP-" + targetBccp.bccpManifestId());

                DtManifestId targetBdtManifestId = targetBccp.dtManifestId();
                DtSummaryRecord targetDt = targetCcDocument.getDt(targetBdtManifestId);
                occurrence(context).targetDtSc =
                        targetCcDocument.getDtScList(targetDt.dtManifestId());

                Bbiep targetBbiep = new Bbiep();
                targetBbiep.setGuid(ScoreGuidUtils.randomGuid());
                targetBbiep.setBasedBccpManifestId(targetBccp.bccpManifestId());
                targetBbiep.setPath(occurrence(context).targetPath);
                targetBbiep.setHashPath(sha256(targetBbiep.getPath()));
                targetBbiep.setDefinition(bbiep.getDefinition());
                targetBbiep.setRemark(bbiep.getRemark());
                targetBbiep.setBizTerm(bbiep.getBizTerm());
                targetBbiep.setDisplayName(bbiep.getDisplayName());

                WrappedBbie targetBbie = occurrence(context).bbie;
                if (targetBbie != null) {
                    targetBbie.setToBbiep(targetBbiep);
                }
            }
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            CcDocument sourceCcDocument = context.getBieDocument().getCcDocument();
            DtScSummaryRecord sourceDtSc = sourceCcDocument.getDtSc(bbieSc.getBasedDtScManifestId());
            UpliftOccurrence owner = parent(context);
            String sourcePath = context.getOccurrencePath();
            DtScSummaryRecord targetDtSc = null;
            String targetPath = null;
            BieUpliftingMapping targetDtScMapping =
                    this.customMappingTable.getMapping(sourcePath);
            if (targetDtScMapping == null &&
                    this.customMappingTable.isAutoMappingSuppressed(sourcePath)) {
                return BieVisitResult.CONTINUE;
            }
            if (targetDtScMapping != null) {
                targetDtSc = targetCcDocument.getDtSc(
                        new DtScManifestId(targetDtScMapping.getTargetManifestId()));
                targetPath = targetDtScMapping.getTargetPath();
            } else {
                CcMatchingScore<DtScSummaryRecord> matchingScore = bestMatch(
                        sourceCcDocument,
                        sourceDtSc,
                        targetCcDocument,
                        owner.targetDtSc,
                        ignored -> true,
                        (ccDocument, dtScManifest) -> ccDocument.getDtSc(dtScManifest.dtScManifestId()));
                if (matchingScore.getScore() > 0.0d) {
                    targetDtSc = (DtScSummaryRecord) matchingScore.getTarget();
                    DtSummaryRecord targetDt = targetCcDocument.getDt(targetDtSc.ownerDtManifestId());
                    targetPath = appendPath(owner.targetPath,
                            "DT-" + targetDt.dtManifestId() + ">DT_SC-" + targetDtSc.dtScManifestId());
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
                if (targetDtScMapping == null) {
                    if (owner.bbie == null) {
                        return BieVisitResult.CONTINUE;
                    }
                    upliftingBbieSc.setBbie(owner.bbie.getBbie());
                }
                // Explicit SC mappings resolve their target BBIE in visitEnd,
                // including when it belongs to a later source occurrence.
                upliftingBbieSc.setBbieSc(targetBbieSc);

                occurrence(context).bbieSc = upliftingBbieSc;
            }
            return BieVisitResult.CONTINUE;
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
                XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.xbtManifestId(), sourceBbie.getXbtManifestId())).findAny().orElse(null);
                XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
                // Only carry the source primitive if it is ALLOWED on the target node (its DT approved-primitive
                // list); otherwise leave it null so the default-primitive block below assigns the target node's
                // default. See the BBIE_SC branch for rationale (#29.1.9.c "default disallowed values").
                if (targetXbt != null &&
                        targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                .filter(Objects::nonNull)
                                .anyMatch(e -> Objects.equals(targetXbt.xbtManifestId(), e.xbtManifestId()))) {
                    targetBbie.setXbtManifestId(targetXbt.xbtManifestId());
                }
            } else if (sourceBbie.getCodeListManifestId() != null) {
                CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.codeListManifestId(), sourceBbie.getCodeListManifestId())).findAny().orElse(null);
                // The availability service applies the target DT restriction and
                // includes permitted derived lists; a release-wide cache would
                // incorrectly bypass both rules.
                List<CodeListSummaryRecord> candidates = availableCodeLists(targetDt.dtManifestId());
                CodeListSummaryRecord targetCodeList = findTargetCodeListMatch(
                        sourceCodeList, candidates).target();
                if (targetCodeList != null) {
                    targetBbie.setCodeListManifestId(targetCodeList.codeListManifestId());
                }
            } else if (sourceBbie.getAgencyIdListManifestId() != null) {
                AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.agencyIdListManifestId(), sourceBbie.getAgencyIdListManifestId())).findFirst().orElse(null);
                List<AgencyIdListSummaryRecord> candidates = availableAgencyIdLists(targetDt.dtManifestId());
                AgencyIdListSummaryRecord targetAgencyIdList = findTargetAgencyIdListMatch(
                        sourceAgencyIdList, candidates).target();
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
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "date time"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT has no compatible default primitive."));
                } else if ("Date".equals(targetDt.dataTypeTerm())) {
                            targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "date"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT has no compatible default primitive."));
                } else if ("Time".equals(targetDt.dataTypeTerm())) {
                            targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "time"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT has no compatible default primitive."));
                } else {
                            targetDefaultDtAwdPri =
                            targetCcDocument.getDtAwdPriList(targetDt.dtManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> e.isDefault())
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT has no default primitive."));
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
                XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.xbtManifestId(), sourceBbieSc.getXbtManifestId())).findAny().orElse(null);
                XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
                // Only carry the source primitive if it is ALLOWED on the target node (its DT_SC approved-primitive
                // list). If it is not allowed (or absent in the target release), leave it null so the default-primitive
                // block below assigns the target node's default. Implements #29.1.9.c "default disallowed values":
                // getTargetXbtManifest only checks the primitive exists somewhere in the release, not that it is
                // allowed on this specific node, so without this gate a disallowed primitive would be carried verbatim.
                if (targetXbt != null &&
                        targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                .filter(Objects::nonNull)
                                .anyMatch(e -> Objects.equals(targetXbt.xbtManifestId(), e.xbtManifestId()))) {
                    targetBbieSc.setXbtManifestId(targetXbt.xbtManifestId());
                }
            } else if (sourceBbieSc.getCodeListManifestId() != null) {
                CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.codeListManifestId(), sourceBbieSc.getCodeListManifestId())).findAny().orElse(null);
                // DT_SC follows the same candidate policy as DT.
                List<CodeListSummaryRecord> candidates = availableCodeLists(targetDtSc.dtScManifestId());
                CodeListSummaryRecord targetCodeList = findTargetCodeListMatch(
                        sourceCodeList, candidates).target();
                if (targetCodeList != null) {
                    targetBbieSc.setCodeListManifestId(targetCodeList.codeListManifestId());
                }
            } else if (sourceBbieSc.getAgencyIdListManifestId() != null) {
                AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(Objects::nonNull).filter(e -> Objects.equals(e.agencyIdListManifestId(), sourceBbieSc.getAgencyIdListManifestId())).findFirst().orElse(null);
                List<AgencyIdListSummaryRecord> candidates = availableAgencyIdLists(targetDtSc.dtScManifestId());
                AgencyIdListSummaryRecord targetAgencyIdListManifest = findTargetAgencyIdListMatch(
                        sourceAgencyIdList, candidates).target();
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
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "date time"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT_SC has no compatible default primitive."));
                } else if ("Date".equals(targetDtSc.representationTerm())) {
                            targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "date"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT_SC has no compatible default primitive."));
                } else if ("Time".equals(targetDtSc.representationTerm())) {
                            targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> isXbtNamed(targetCcDocument, e.xbtManifestId(), "time"))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT_SC has no compatible default primitive."));
                } else {
                            targetDefaultDtScAwdPri =
                            targetCcDocument.getDtScAwdPriList(targetDtSc.dtScManifestId()).stream()
                                    .filter(Objects::nonNull)
                                    .filter(e -> e.isDefault())
                                    .findFirst().orElseThrow(() -> new IllegalStateException("Target DT_SC has no default primitive."));
                }
                targetBbieSc.setXbtManifestId(targetDefaultDtScAwdPri.xbtManifestId());
            }
            return targetBbieSc;
        }

        private List<CodeListSummaryRecord> availableCodeLists(DtManifestId dtManifestId) {
            if (targetCodeListList == null) {
                return codeListQueryService.availableCodeListListByDtManifestId(requester, dtManifestId);
            }
            return codeListQueryService.availableCodeListListByDtManifestId(
                    requester, dtManifestId, targetCodeListList,
                    targetCcDocument.getDtAwdPriList(dtManifestId));
        }

        private List<CodeListSummaryRecord> availableCodeLists(DtScManifestId dtScManifestId) {
            if (targetCodeListList == null) {
                return codeListQueryService.availableCodeListListByDtScManifestId(requester, dtScManifestId);
            }
            return codeListQueryService.availableCodeListListByDtScManifestId(
                    requester, dtScManifestId, targetCodeListList,
                    targetCcDocument.getDtScAwdPriList(dtScManifestId));
        }

        private List<AgencyIdListSummaryRecord> availableAgencyIdLists(DtManifestId dtManifestId) {
            if (targetAgencyIdListList == null) {
                return agencyIdListQueryService.availableAgencyIdListListByDtManifestId(requester, dtManifestId);
            }
            return agencyIdListQueryService.availableAgencyIdListListByDtManifestId(
                    requester, dtManifestId, targetAgencyIdListList,
                    targetCcDocument.getDtAwdPriList(dtManifestId));
        }

        private List<AgencyIdListSummaryRecord> availableAgencyIdLists(DtScManifestId dtScManifestId) {
            if (targetAgencyIdListList == null) {
                return agencyIdListQueryService.availableAgencyIdListListByDtScManifestId(requester, dtScManifestId);
            }
            return agencyIdListQueryService.availableAgencyIdListListByDtScManifestId(
                    requester, dtScManifestId, targetAgencyIdListList,
                    targetCcDocument.getDtScAwdPriList(dtScManifestId));
        }

    }

    @Transactional
    public UpliftBieResponse upliftBie(ScoreUser requester, UpliftBieRequest request) {

        if (request == null || request.getTopLevelAsbiepId() == null ||
                (request.getTargetAsccpManifestId() == null && request.getTargetReleaseId() == null)) {
            throw new IllegalArgumentException("Source BIE and target release/ASCCP are required.");
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);

        AsccpManifestId targetAsccpManifestId = request.getTargetAsccpManifestId();
        if (targetAsccpManifestId == null) {
            AsccpSummaryRecord nextTargetAsccp = asccpQuery.findNextAsccpManifest(
                    request.getTopLevelAsbiepId(), request.getTargetReleaseId());
            if (nextTargetAsccp == null) {
                throw new IllegalArgumentException("No target ASCCP exists in the requested release.");
            }
            targetAsccpManifestId = nextTargetAsccp.asccpManifestId();
        }

        AsccpSummaryRecord targetAsccp = asccpQuery.getAsccpSummary(targetAsccpManifestId);
        if (targetAsccp == null) {
            throw new IllegalArgumentException("Target ASCCP record not found.");
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        if (sourceBieDocument == null || sourceBieDocument.getRootAsbiep() == null
                || sourceBieDocument.getCcDocument() == null
                || sourceBieDocument.getCcDocument().getAsccp(sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId()) == null) {
            throw new IllegalArgumentException("Source BIE record not found.");
        }
        if (targetAsccp.release() == null || targetAsccp.release().releaseId() == null) {
            throw new IllegalArgumentException("Target ASCCP release record not found.");
        }
        CcDocument targetCcDocument = new CcDocumentImpl(requester, repositoryFactory, targetAsccp.release().releaseId());

        List<BusinessContextId> bizCtxIds = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                .getAssignedBusinessContextList(request.getTopLevelAsbiepId());

        List<BieUpliftingMapping> mappingList = request.getCustomMappingTable();
        validateTargetManifestIds(mappingList);
        validateTargetMappingShapes(targetCcDocument, mappingList);
        validateReuseMappings(requester, targetAsccp.release().releaseId(), targetCcDocument, mappingList);
        validateSourceMappings(sourceBieDocument, mappingList);
        BieUpliftingCustomMappingTable customMappingTable = new BieUpliftingCustomMappingTable(mappingList);

        AsccpSummaryRecord sourceRootAsccp = sourceBieDocument.getCcDocument()
                .getAsccp(sourceBieDocument.getRootAsbiep().getBasedAsccpManifestId());
        if (sourceRootAsccp.release() == null || sourceRootAsccp.release().releaseId() == null) {
            throw new IllegalArgumentException("Source BIE release record not found.");
        }
        ReleaseId sourceReleaseId = sourceRootAsccp.release().releaseId();
        ReleaseId targetReleaseId = targetAsccp.release().releaseId();
        if (!releaseQueryService.isLaterRelease(requester, sourceReleaseId, targetReleaseId)) {
            throw new IllegalArgumentException("A BIE can only be uplifted to a newer release.");
        }
        if (request.getTargetReleaseId() != null && !request.getTargetReleaseId().equals(targetReleaseId)) {
            throw new IllegalArgumentException("Target ASCCP does not belong to the requested release.");
        }

        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);

        List<XbtSummaryRecord> sourceXbtList = xbtQuery.getXbtSummaryList(sourceReleaseId);
        List<XbtSummaryRecord> targetXbtList = xbtQuery.getXbtSummaryList(targetReleaseId);

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);

        List<CodeListSummaryRecord> sourceCodeListList = codeListQuery.getCodeListSummaryList(sourceReleaseId);
        List<CodeListSummaryRecord> targetCodeListList = codeListQuery.getCodeListSummaryList(targetAsccp.release().releaseId());

        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);

        List<AgencyIdListSummaryRecord> sourceAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(sourceReleaseId);
        List<AgencyIdListSummaryRecord> targetAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(targetAsccp.release().releaseId());

        BieUpliftingHandler upliftingHandler =
                new BieUpliftingHandler(request.getRequester(), bizCtxIds, customMappingTable,
                        sourceBieDocument, targetCcDocument, targetAsccpManifestId,
                        sourceXbtList, targetXbtList,
                        sourceCodeListList, targetCodeListList,
                        sourceAgencyIdListList, targetAgencyIdListList);
        TopLevelAsbiepId targetTopLevelAsbiepId = upliftingHandler.uplift();

        UpliftBieResponse response = new UpliftBieResponse();
        response.setTopLevelAsbiepId(targetTopLevelAsbiepId);
        return response;
    }

    static void validateTargetManifestIds(List<BieUpliftingMapping> mappings) {
        if (mappings == null) {
            return; // Required-list validation belongs to the request/mapping table.
        }
        for (BieUpliftingMapping mapping : mappings) {
            // Root ABIE is path metadata; the request's target ASCCP determines its ACC.
            if (mapping != null && !"ABIE".equalsIgnoreCase(mapping.getBieType())
                    && hasLength(mapping.getTargetPath()) && mapping.getTargetManifestId() == null) {
                throw new IllegalArgumentException("Mapped target paths require a target manifest ID.");
            }
        }
    }

    private void validateMappingTypes(List<BieUpliftingMapping> mappings) {
        for (BieUpliftingMapping mapping : mappings) {
            if (mapping == null || !hasLength(mapping.getBieType()) ||
                    !Set.of("ABIE", "ASBIE", "BBIE", "BBIE_SC")
                            .contains(mapping.getBieType().toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Unsupported BIE mapping type: " +
                        (mapping == null ? null : mapping.getBieType()));
            }
        }
    }

    private void validateTargetMappingShapes(CcDocument targetCcDocument,
                                             List<BieUpliftingMapping> mappings) {
        if (mappings == null) {
            return;
        }
        for (BieUpliftingMapping mapping : mappings) {
            if (mapping == null || !hasLength(mapping.getBieType())) {
                throw new IllegalArgumentException("Each uplift mapping requires a BIE type.");
            }
            String bieType = mapping.getBieType().toUpperCase(Locale.ROOT);
            if (!Set.of("ABIE", "ASBIE", "BBIE", "BBIE_SC").contains(bieType)) {
                throw new IllegalArgumentException("Unsupported BIE mapping type: " + mapping.getBieType());
            }
            if (mapping == null || !hasLength(mapping.getTargetPath()) ||
                    mapping.getTargetManifestId() == null ||
                    "ABIE".equals(bieType)) {
                continue;
            }
            String lastTag = BieUpliftingCustomMappingTable.getLastTag(mapping.getTargetPath());
            BigInteger pathId;
            try {
                pathId = BieUpliftingCustomMappingTable.extractManifestId(lastTag);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Target mapping path is malformed.", e);
            }
            validateTargetPathComponents(targetCcDocument, mapping.getTargetPath());
            if (!pathId.equals(mapping.getTargetManifestId())) {
                throw new IllegalArgumentException("Target mapping path and manifest ID must refer to the same component.");
            }
            boolean exists = false;
            switch (bieType) {
                case "ASBIE":
                    exists = lastTag.startsWith("ASCC-")
                            && targetCcDocument.getAscc(new AsccManifestId(mapping.getTargetManifestId())) != null;
                    break;
                case "BBIE":
                    exists = lastTag.startsWith("BCC-")
                            && targetCcDocument.getBcc(new BccManifestId(mapping.getTargetManifestId())) != null;
                    break;
                case "BBIE_SC":
                    exists = lastTag.startsWith("DT_SC-")
                            && targetCcDocument.getDtSc(new DtScManifestId(mapping.getTargetManifestId())) != null;
                    break;
            }
            if (!exists) {
                throw new IllegalArgumentException("Target mapping component does not exist or has the wrong type.");
            }
        }
    }

    private void validateTargetPathComponents(CcDocument targetCcDocument, String path) {
        String[] tags = path.split(">");
        for (String tag : tags) {
            try {
                BigInteger id = BieUpliftingCustomMappingTable.extractManifestId(tag);
                boolean exists;
                if (tag.startsWith("ASCCP-")) {
                    exists = targetCcDocument.getAsccp(new AsccpManifestId(id)) != null;
                } else if (tag.startsWith("ACC-")) {
                    exists = targetCcDocument.getAcc(new AccManifestId(id)) != null;
                } else if (tag.startsWith("ASCC-")) {
                    exists = targetCcDocument.getAscc(new AsccManifestId(id)) != null;
                } else if (tag.startsWith("BCC-")) {
                    exists = targetCcDocument.getBcc(new BccManifestId(id)) != null;
                } else if (tag.startsWith("BCCP-")) {
                    exists = targetCcDocument.getBccp(new BccpManifestId(id)) != null;
                } else if (tag.startsWith("DT_SC-")) {
                    exists = targetCcDocument.getDtSc(new DtScManifestId(id)) != null;
                } else if (tag.startsWith("DT-")) {
                    exists = targetCcDocument.getDt(new DtManifestId(id)) != null;
                } else {
                    throw new IllegalArgumentException("Target mapping path contains an unknown component.");
                }
                if (!exists) {
                    throw new IllegalArgumentException("Target mapping path contains a missing component.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Target mapping path contains an invalid manifest ID.", e);
            }
        }
        for (int i = 0; i + 1 < tags.length; i++) {
            String current = tags[i];
            String next = tags[i + 1];
            BigInteger currentId = extractManifestId(current);
            BigInteger nextId = extractManifestId(next);
            // Every path edge must be one of the canonical component relationships
            // below.  Starting with false prevents an otherwise valid pair of
            // existing components (for example ASCCP>BCC) from being accepted
            // merely because the pair was not recognized.
            boolean related = false;
            if (current.startsWith("ASCCP-") && next.startsWith("ACC-")) {
                AsccpSummaryRecord asccp = targetCcDocument.getAsccp(new AsccpManifestId(currentId));
                related = asccp != null && asccp.roleOfAccManifestId() != null && nextId.equals(asccp.roleOfAccManifestId().value());
            } else if (current.startsWith("ACC-") && next.startsWith("ACC-")) {
                // Inherited and group ACCs are represented as a chain of ACC
                // segments before the leaf ASCC/BCC.  The later ACC is based
                // on the preceding ACC in the canonical server path.  The
                // uplift UI may expose the same lineage from the concrete ACC
                // toward its base ACC, so accept either direction while still
                // requiring a direct based-ACC relationship.
                AccSummaryRecord currentAcc = targetCcDocument.getAcc(new AccManifestId(currentId));
                AccSummaryRecord nextAcc = targetCcDocument.getAcc(new AccManifestId(nextId));
                related = (nextAcc != null && nextAcc.basedAccManifestId() != null
                        && currentId.equals(nextAcc.basedAccManifestId().value()))
                        || (currentAcc != null && currentAcc.basedAccManifestId() != null
                        && nextId.equals(currentAcc.basedAccManifestId().value()));
            } else if (current.startsWith("ACC-") && next.startsWith("ASCC-")) {
                AsccSummaryRecord ascc = targetCcDocument.getAscc(new AsccManifestId(nextId));
                related = ascc != null && ascc.fromAccManifestId() != null && currentId.equals(ascc.fromAccManifestId().value());
            } else if (current.startsWith("ASCC-") && next.startsWith("ASCCP-")) {
                AsccSummaryRecord ascc = targetCcDocument.getAscc(new AsccManifestId(currentId));
                related = ascc != null && ascc.toAsccpManifestId() != null && nextId.equals(ascc.toAsccpManifestId().value());
            } else if (current.startsWith("ACC-") && next.startsWith("BCC-")) {
                BccSummaryRecord bcc = targetCcDocument.getBcc(new BccManifestId(nextId));
                related = bcc != null && bcc.fromAccManifestId() != null && currentId.equals(bcc.fromAccManifestId().value());
            } else if (current.startsWith("BCC-") && next.startsWith("BCCP-")) {
                BccSummaryRecord bcc = targetCcDocument.getBcc(new BccManifestId(currentId));
                related = bcc != null && bcc.toBccpManifestId() != null && nextId.equals(bcc.toBccpManifestId().value());
            } else if (current.startsWith("BCCP-") && next.startsWith("DT-")) {
                BccpSummaryRecord bccp = targetCcDocument.getBccp(new BccpManifestId(currentId));
                related = bccp != null && bccp.dtManifestId() != null && nextId.equals(bccp.dtManifestId().value());
            } else if (current.startsWith("DT-") && next.startsWith("DT_SC-")) {
                DtScSummaryRecord dtSc = targetCcDocument.getDtSc(new DtScManifestId(nextId));
                related = dtSc != null && dtSc.ownerDtManifestId() != null && currentId.equals(dtSc.ownerDtManifestId().value());
            }
            if (!related) {
                throw new IllegalArgumentException("Target mapping path contains unrelated components.");
            }
        }
    }

    private void validateReuseMappings(ScoreUser requester, ReleaseId targetReleaseId,
                                       CcDocument targetCcDocument,
                                       List<BieUpliftingMapping> mappings) {
        if (mappings == null) {
            return;
        }
        for (BieUpliftingMapping mapping : mappings) {
            if (mapping == null || mapping.getRefTopLevelAsbiepId() == null) {
                continue;
            }
            if (!"ASBIE".equalsIgnoreCase(mapping.getBieType()) ||
                    mapping.getTargetManifestId() == null) {
                throw new IllegalArgumentException("Reuse references require an ASBIE target mapping.");
            }
            AsccSummaryRecord targetAscc = targetCcDocument.getAscc(
                    new AsccManifestId(mapping.getTargetManifestId()));
            if (targetAscc == null || targetAscc.toAsccpManifestId() == null) {
                throw new IllegalArgumentException("Reuse target association was not found.");
            }
            var referenced = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                    .getTopLevelAsbiepSummary(mapping.getRefTopLevelAsbiepId());
            if (referenced == null || referenced.release() == null ||
                    referenced.release().releaseId() == null ||
                    !targetReleaseId.equals(referenced.release().releaseId())) {
                throw new IllegalArgumentException("Reuse BIE must belong to the target release.");
            }
            BieDocument referencedDocument = bieReadService.getBieDocument(
                    requester, mapping.getRefTopLevelAsbiepId());
            if (referencedDocument == null || referencedDocument.getRootAsbiep() == null ||
                    !targetAscc.toAsccpManifestId().equals(
                            referencedDocument.getRootAsbiep().getBasedAsccpManifestId())) {
                throw new IllegalArgumentException("Reuse BIE is not compatible with the target ASCCP.");
            }
        }
    }

    private void validateSourceMappings(BieDocument sourceBieDocument,
                                        List<BieUpliftingMapping> mappings) {
        if (mappings == null) {
            return;
        }
        Map<String, String> sourceTypes = new HashMap<>();
        Map<String, BigInteger> sourceIds = new HashMap<>();
        sourceBieDocument.accept(new BieVisitor() {
            @Override
            public BieVisitResult visitAsbie(Asbie value, BieVisitContext context) {
                sourceTypes.put(context.getOccurrencePath(), "ASBIE");
                sourceIds.put(context.getOccurrencePath(), value.getAsbieId().value());
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbie(Bbie value, BieVisitContext context) {
                sourceTypes.put(context.getOccurrencePath(), "BBIE");
                sourceIds.put(context.getOccurrencePath(), value.getBbieId().value());
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbieSc(BbieSc value, BieVisitContext context) {
                sourceTypes.put(context.getOccurrencePath(), "BBIE_SC");
                sourceIds.put(context.getOccurrencePath(), value.getBbieScId().value());
                return BieVisitResult.CONTINUE;
            }
        });
        for (BieUpliftingMapping mapping : mappings) {
            if (mapping == null || !hasLength(mapping.getSourcePath()) ||
                    mapping.getBieId() == null || "ABIE".equalsIgnoreCase(mapping.getBieType())) {
                continue;
            }
            String path = mapping.getSourcePath();
            String type = sourceTypes.get(path);
            if (type == null || !type.equalsIgnoreCase(mapping.getBieType()) ||
                    !mapping.getBieId().equals(sourceIds.get(path))) {
                throw new IllegalArgumentException("Source mapping path, type, and BIE ID do not match.");
            }
        }
    }

    public UpliftValidationResponse validateBieUplifting(ScoreUser requester, UpliftValidationRequest request) {
        if (request == null || request.getTopLevelAsbiepId() == null ||
                request.getTargetReleaseId() == null || request.getMappingList() == null) {
            throw new IllegalArgumentException("Source BIE, target release, and mapping list are required.");
        }
        if (request.getMappingList().stream().anyMatch(mapping -> mapping == null
                || !hasLength(mapping.getBieType()) || mapping.getBieId() == null)) {
            throw new IllegalArgumentException("Each uplift mapping requires a BIE type and BIE ID.");
        }
        validateMappingTypes(request.getMappingList());
        validateTargetManifestIds(request.getMappingList());
        UpliftValidationResponse response = new UpliftValidationResponse();
        List<BieUpliftingValidation> validations = new ArrayList<>();

        var topLevelAsbiep = repositoryFactory.topLevelAsbiepQueryRepository(requester)
                .getTopLevelAsbiepSummary(request.getTopLevelAsbiepId());
        var releaseQuery = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord sourceRelease = (topLevelAsbiep == null || topLevelAsbiep.release() == null)
                ? null : releaseQuery.getReleaseSummary(topLevelAsbiep.release().releaseId());
        ReleaseSummaryRecord targetRelease = releaseQuery.getReleaseSummary(request.getTargetReleaseId());
        if (sourceRelease == null || sourceRelease.releaseId() == null) {
            throw new IllegalArgumentException("Source BIE release record not found.");
        }
        if (targetRelease == null || targetRelease.releaseId() == null) {
            throw new IllegalArgumentException("Target release record not found.");
        }

        if (!releaseQueryService.isLaterRelease(requester,
                sourceRelease.releaseId(), targetRelease.releaseId())) {
            throw new IllegalArgumentException();
        }

        BieDocument sourceBieDocument = bieReadService.getBieDocument(request.getRequester(), request.getTopLevelAsbiepId());
        if (sourceBieDocument == null || sourceBieDocument.getCcDocument() == null) {
            throw new IllegalArgumentException("Source BIE record not found.");
        }
        CcDocument targetCcDocument = new CcDocumentImpl(requester, repositoryFactory, request.getTargetReleaseId());
        validateTargetMappingShapes(targetCcDocument, request.getMappingList());
        validateReuseMappings(requester, request.getTargetReleaseId(), targetCcDocument, request.getMappingList());
        validateSourceMappings(sourceBieDocument, request.getMappingList());

        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);

        List<XbtSummaryRecord> sourceXbtList = xbtQuery.getXbtSummaryList(sourceRelease.releaseId());
        List<XbtSummaryRecord> targetXbtList = xbtQuery.getXbtSummaryList(targetRelease.releaseId());

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);

        List<CodeListSummaryRecord> sourceCodeListList = codeListQuery.getCodeListSummaryList(sourceRelease.releaseId());
        List<CodeListSummaryRecord> targetCodeListList = codeListQuery.getCodeListSummaryList(targetRelease.releaseId());

        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);

        List<AgencyIdListSummaryRecord> sourceAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(sourceRelease.releaseId());
        List<AgencyIdListSummaryRecord> targetAgencyIdListList = agencyIdListQuery.getAgencyIdListSummaryList(targetRelease.releaseId());

        request.getMappingList().forEach(mapping -> {
            BieUpliftingValidation validation = new BieUpliftingValidation();
            validation.setBieId(mapping.getBieId());
            validation.setBieType(mapping.getBieType());
            validation.setSourcePath(mapping.getSourcePath());
            switch (mapping.getBieType().toUpperCase()) {
                case "ABIE":
                    validation.setValid(true);
                    break;
                case "ASBIE":
                    validation.setValid(true);
                    break;
                case "BBIE":
                    Bbie bbie = sourceBieDocument.getBbie(new BbieId(mapping.getBieId()));
                    if (bbie == null) {
                        throw new IllegalArgumentException("Source BBIE mapping record not found.");
                    }
                    BccManifestId bccManifestId = (mapping.getTargetManifestId() != null) ? new BccManifestId(mapping.getTargetManifestId()) : null;
                    if (bccManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    BccSummaryRecord bcc = targetCcDocument.getBcc(bccManifestId);
                    if (bcc == null || bcc.toBccpManifestId() == null) {
                        throw new IllegalArgumentException("Target BBIE mapping record not found.");
                    }
                    BccpSummaryRecord bccp = targetCcDocument.getBccp(bcc.toBccpManifestId());
                    if (bccp == null || bccp.dtManifestId() == null) {
                        throw new IllegalArgumentException("Target BBIE property record not found.");
                    }
                    DtSummaryRecord dt = targetCcDocument.getDt(bccp.dtManifestId());
                    if (dt == null) {
                        throw new IllegalArgumentException("Target data type record not found.");
                    }

                    if (bbie.getXbtManifestId() != null) {
                        XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(Objects::nonNull).filter(xbt -> Objects.equals(xbt.xbtManifestId(), bbie.getXbtManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtPriRestriIdMappable(
                                sourceXbt, targetCcDocument.getDtAwdPriList(dt.dtManifestId()), targetXbtList,
                                DtAwdPriSummaryRecord::xbtManifestId));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbie.getCodeListManifestId() != null) {
                        CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(Objects::nonNull).filter(codeList -> Objects.equals(codeList.codeListManifestId(), bbie.getCodeListManifestId())).findFirst().orElse(null);
                        ValueDomainValidationResult result = checkBdtCodeListMappable(
                                sourceCodeList,
                                codeListQueryService.availableCodeListListByDtManifestId(
                                        requester, dt.dtManifestId(), targetCodeListList,
                                        targetCcDocument.getDtAwdPriList(dt.dtManifestId())));
                        validation.setMessage(result.issue());
                        validation.setStatus(result.status());
                        validation.setValid(result.valid());
                    } else {
                        AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(Objects::nonNull).filter(agencyIdList -> Objects.equals(agencyIdList.agencyIdListManifestId(), bbie.getAgencyIdListManifestId())).findFirst().orElse(null);
                        ValueDomainValidationResult result = checkBdtAgencyIdListMappable(
                                sourceAgencyIdList,
                                agencyIdListQueryService.availableAgencyIdListListByDtManifestId(
                                        requester, dt.dtManifestId(), targetAgencyIdListList,
                                        targetCcDocument.getDtAwdPriList(dt.dtManifestId())));
                        validation.setMessage(result.issue());
                        validation.setStatus(result.status());
                        validation.setValid(result.valid());
                    }
                    break;
                case "BBIE_SC":
                    BbieSc bbieSc = sourceBieDocument.getBbieSc(new BbieScId(mapping.getBieId()));
                    if (bbieSc == null) {
                        throw new IllegalArgumentException("Source BBIE_SC mapping record not found.");
                    }
                    DtScManifestId dtScManifestId = (mapping.getTargetManifestId() != null) ? new DtScManifestId(mapping.getTargetManifestId()) : null;
                    if (dtScManifestId == null) {
                        validation.setValid(true);
                        break;
                    }
                    DtScSummaryRecord dtSc = targetCcDocument.getDtSc(dtScManifestId);
                    if (dtSc == null) {
                        throw new IllegalArgumentException("Target supplementary data type record not found.");
                    }

                    if (bbieSc.getXbtManifestId() != null) {
                        XbtSummaryRecord sourceXbt = sourceXbtList.stream().filter(Objects::nonNull).filter(xbt -> Objects.equals(xbt.xbtManifestId(), bbieSc.getXbtManifestId())).findFirst().orElse(null);
                        validation.setMessage(checkBdtPriRestriIdMappable(
                                sourceXbt, targetCcDocument.getDtScAwdPriList(dtSc.dtScManifestId()), targetXbtList,
                                DtScAwdPriSummaryRecord::xbtManifestId));
                        validation.setValid(validation.getMessage().isEmpty());
                    } else if (bbieSc.getCodeListManifestId() != null) {
                        CodeListSummaryRecord sourceCodeList = sourceCodeListList.stream().filter(Objects::nonNull).filter(codeList -> Objects.equals(codeList.codeListManifestId(), bbieSc.getCodeListManifestId())).findFirst().orElse(null);
                        ValueDomainValidationResult result = checkBdtCodeListMappable(
                                sourceCodeList,
                                codeListQueryService.availableCodeListListByDtScManifestId(
                                        requester, dtSc.dtScManifestId(), targetCodeListList,
                                        targetCcDocument.getDtScAwdPriList(dtSc.dtScManifestId())));
                        validation.setMessage(result.issue());
                        validation.setStatus(result.status());
                        validation.setValid(result.valid());
                    } else {
                        AgencyIdListSummaryRecord sourceAgencyIdList = sourceAgencyIdListList.stream().filter(Objects::nonNull).filter(agencyIdList -> Objects.equals(agencyIdList.agencyIdListManifestId(), bbieSc.getAgencyIdListManifestId())).findFirst().orElse(null);
                        ValueDomainValidationResult result = checkBdtAgencyIdListMappable(
                                sourceAgencyIdList,
                                agencyIdListQueryService.availableAgencyIdListListByDtScManifestId(
                                        requester, dtSc.dtScManifestId(), targetAgencyIdListList,
                                        targetCcDocument.getDtScAwdPriList(dtSc.dtScManifestId())));
                        validation.setMessage(result.issue());
                        validation.setStatus(result.status());
                        validation.setValid(result.valid());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported BIE mapping type: " + mapping.getBieType());
            }
            validations.add(validation);
        });
        response.setValidations(validations);
        return response;
    }

    private <P> String checkBdtPriRestriIdMappable(XbtSummaryRecord sourceXbt,
                                                   List<P> targetAllowedPrimitives,
                                                   List<XbtSummaryRecord> targetXbtList,
                                                   Function<P, XbtManifestId> xbtManifestIdExtractor) {
        XbtSummaryRecord targetXbt = getTargetXbtManifest(sourceXbt, targetXbtList);
        if (targetXbt != null && targetAllowedPrimitives != null && targetAllowedPrimitives.stream()
                .filter(Objects::nonNull)
                .map(xbtManifestIdExtractor)
                .anyMatch(targetXbt.xbtManifestId()::equals)) {
            return "";
        }
        return "Primitive value '" + sourceName(sourceXbt) + "' is not allowed in the target node. Uplifted node will use its default primitive in the domain value restriction.";
    }

    private ValueDomainValidationResult checkBdtCodeListMappable(
            CodeListSummaryRecord sourceCodeList,
            List<CodeListSummaryRecord> targetCodeListList) {
        CodeListMatch targetCodeListMatch = findTargetCodeListMatch(sourceCodeList, targetCodeListList);
        if (targetCodeListMatch.target() != null) {
            return ValueDomainValidationResult.matched(
                    "Target Code List '" + targetCodeListMatch.target().name() + "' selected by " + targetCodeListMatch.matchType().description + ".");
        }
        return ValueDomainValidationResult.unmatched(
                "Target default primitive selected because no matching Code List is available in the target node.",
                "Code List '" + sourceName(sourceCodeList) + "' is not allowed in the target node or the system cannot find the exact match code list in the target release, uplifted node will use a default primitive in the domain value restriction.");
    }

    private ValueDomainValidationResult checkBdtAgencyIdListMappable(
            AgencyIdListSummaryRecord sourceAgencyIdList,
            List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        AgencyIdListMatch targetAgencyIdListMatch = findTargetAgencyIdListMatch(sourceAgencyIdList, targetAgencyIdListList);
        if (targetAgencyIdListMatch.target() != null) {
            return ValueDomainValidationResult.matched(
                    "Target Agency ID List '" + targetAgencyIdListMatch.target().name() + "' selected by " + targetAgencyIdListMatch.matchType().description + ".");
        }
        return ValueDomainValidationResult.unmatched(
                "Target default primitive selected because no matching Agency ID List is available in the target node.",
                "Agency ID List '" + sourceName(sourceAgencyIdList) + "' is not allowed in the target node or the system cannot find the exact match agency ID list in the target release, uplifted node will use a default primitive in the domain value restriction.");
    }


    public XbtSummaryRecord getTargetXbtManifest(
            XbtSummaryRecord sourceXbt,
            List<XbtSummaryRecord> targetXbtList) {
        if (sourceXbt == null || sourceXbt.xbtId() == null || targetXbtList == null) {
            return null;
        }

        // XBT_MANIFEST_ID is release-scoped. The stable identity that must be
        // carried across releases is the shared XBT_ID.
        return targetXbtList.stream()
                .filter(e -> e != null && sourceXbt.xbtId().equals(e.xbtId()))
                .findFirst().orElse(null);
    }

    private enum MatchType {
        GUID("GUID"),
        IDENTIFIERS("name, list ID, and version ID");

        private final String description;

        MatchType(String description) {
            this.description = description;
        }
    }

    record CodeListMatch(CodeListSummaryRecord target, MatchType matchType) {
    }

    record AgencyIdListMatch(AgencyIdListSummaryRecord target, MatchType matchType) {
    }

    private record ManifestMatch<T>(T target, MatchType matchType) {
    }

    private record ValueDomainValidationResult(boolean valid, String status, String issue) {

        private static ValueDomainValidationResult matched(String status) {
            return new ValueDomainValidationResult(true, status, "");
        }

        private static ValueDomainValidationResult unmatched(String status, String issue) {
            return new ValueDomainValidationResult(false, status, issue);
        }
    }

    public CodeListSummaryRecord getTargetCodeListManifest(
            CodeListSummaryRecord sourceCodeList,
            List<CodeListSummaryRecord> targetCodeListList) {
        return findTargetCodeListMatch(sourceCodeList, targetCodeListList).target();
    }

    CodeListMatch findTargetCodeListMatch(
            CodeListSummaryRecord sourceCodeList,
            List<CodeListSummaryRecord> targetCodeListList) {
        return findTargetCodeListMatch(sourceCodeList, targetCodeListList, null);
    }

    CodeListMatch findTargetCodeListMatch(
            CodeListSummaryRecord sourceCodeList,
            List<CodeListSummaryRecord> targetCodeListList,
            Set<CodeListManifestId> allowedManifestIds) {
        ManifestMatch<CodeListSummaryRecord> match = findManifestMatch(
                sourceCodeList,
                targetCodeListList,
                allowedManifestIds,
                CodeListSummaryRecord::codeListId,
                CodeListSummaryRecord::codeListManifestId,
                e -> e.codeListId() != null,
                (source, target) -> StringUtils.equals(source.name(), target.name()) &&
                        StringUtils.equals(source.listId(), target.listId()) &&
                        StringUtils.equals(source.versionId(), target.versionId()));
        return new CodeListMatch(match.target(), match.matchType());
    }

    public AgencyIdListSummaryRecord getTargetAgencyIdListManifest(
            AgencyIdListSummaryRecord sourceAgencyIdList,
            List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        return findTargetAgencyIdListMatch(sourceAgencyIdList, targetAgencyIdListList).target();
    }

    AgencyIdListMatch findTargetAgencyIdListMatch(
            AgencyIdListSummaryRecord sourceAgencyIdList,
            List<AgencyIdListSummaryRecord> targetAgencyIdListList) {
        return findTargetAgencyIdListMatch(sourceAgencyIdList, targetAgencyIdListList, null);
    }

    AgencyIdListMatch findTargetAgencyIdListMatch(
            AgencyIdListSummaryRecord sourceAgencyIdList,
            List<AgencyIdListSummaryRecord> targetAgencyIdListList,
            Set<AgencyIdListManifestId> allowedManifestIds) {
        ManifestMatch<AgencyIdListSummaryRecord> match = findManifestMatch(
                sourceAgencyIdList,
                targetAgencyIdListList,
                allowedManifestIds,
                AgencyIdListSummaryRecord::agencyIdListId,
                AgencyIdListSummaryRecord::agencyIdListManifestId,
                e -> e.agencyIdListId() != null,
                (source, target) -> StringUtils.equals(source.name(), target.name()) &&
                        StringUtils.equals(source.listId(), target.listId()) &&
                        StringUtils.equals(source.agencyIdListValueName(), target.agencyIdListValueName()) &&
                        StringUtils.equals(source.versionId(), target.versionId()));
        return new AgencyIdListMatch(match.target(), match.matchType());
    }

    private <S extends CoreComponent<?>, T extends CoreComponent<?>, I, M> ManifestMatch<T> findManifestMatch(
            S source,
            List<T> targets,
            Set<M> allowedManifestIds,
            Function<T, I> logicalId,
            Function<T, M> manifestId,
            Predicate<T> hasLogicalId,
            BiPredicate<S, T> fallbackMatch) {
        if (source == null || targets == null) {
            return new ManifestMatch<>(null, null);
        }

        T targetCandidate = targets.stream()
                .filter(Objects::nonNull)
                .filter(hasLogicalId)
                .filter(target -> ccMatchingService.score(source, target) == 1.0d)
                .findFirst().orElse(null);
        MatchType matchType = MatchType.GUID;
        if (targetCandidate == null) {
            targetCandidate = targets.stream()
                    .filter(Objects::nonNull)
                    .filter(hasLogicalId)
                    .filter(target -> fallbackMatch.test(source, target))
                    .findFirst().orElse(null);
            matchType = MatchType.IDENTIFIERS;
        }
        if (targetCandidate == null) {
            return new ManifestMatch<>(null, null);
        }

        I targetLogicalId = logicalId.apply(targetCandidate);
        T selected = targets.stream()
                .filter(Objects::nonNull)
                .filter(target -> manifestId.apply(target) != null)
                .filter(target -> Objects.equals(logicalId.apply(target), targetLogicalId))
                .filter(target -> allowedManifestIds == null || allowedManifestIds.contains(manifestId.apply(target)))
                .findFirst().orElse(null);
        return new ManifestMatch<>(selected, matchType);
    }

    private String sourceName(CodeListSummaryRecord sourceCodeList) {
        return sourceCodeList != null ? sourceCodeList.name() : "unknown";
    }

    private String sourceName(AgencyIdListSummaryRecord sourceAgencyIdList) {
        return sourceAgencyIdList != null ? sourceAgencyIdList.name() : "unknown";
    }

    private String sourceName(XbtSummaryRecord sourceXbt) {
        return sourceXbt != null ? sourceXbt.name() : "unknown";
    }

    private boolean isXbtNamed(CcDocument ccDocument, XbtManifestId xbtManifestId, String name) {
        XbtSummaryRecord xbt = xbtManifestId != null ? ccDocument.getXbt(xbtManifestId) : null;
        return xbt != null && xbt.name() != null && xbt.name().equalsIgnoreCase(name);
    }

}
