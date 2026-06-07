package org.oagi.score.gateway.http.api.bie_management.service;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieAssociation;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.*;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.model.bie_package.BieManifestDetail.newBieManifestDetail;
import static org.oagi.score.gateway.http.api.bie_management.model.bie_package.BieManifestSummary.newBieManifestSummary;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BiePackageManifestService {

    // Bumped 0.2 -> 0.3 for the package-level revisionReason field added per issue #1733.
    private static final String BIE_PACKAGE_MANIFEST_VERSION = "0.3";

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BieReadService bieReadService;

    public BiePackageManifestResponse getBiePackageManifest(ScoreUser requester,
                                                            BiePackageId biePackageId, String pathDelimiter) {
        return getBiePackageManifest(requester, biePackageId, pathDelimiter, Collections.emptyMap());
    }

    public BiePackageManifestResponse getBiePackageManifest(ScoreUser requester,
                                                            BiePackageId biePackageId,
                                                            String pathDelimiter,
                                                            Map<TopLevelAsbiepId, String> generatedFilesByTopLevelAsbiepId) {

        BiePackageQueryRepository biePackageQueryRepository = repositoryFactory.biePackageQueryRepository(requester);
        BiePackageSummaryRecord currentPackage = biePackageQueryRepository.getBiePackageSummary(biePackageId);

        TopLevelAsbiepQueryRepository topLevelAsbiepQueryRepository = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        var asbiepQueryRepository = repositoryFactory.asbiepQueryRepository(requester);
        BusinessContextQueryRepository businessContextQueryRepository =
                repositoryFactory.businessContextQueryRepository(requester);
        ContextSchemeQueryRepository contextSchemeQueryRepository =
                repositoryFactory.contextSchemeQueryRepository(requester);

        Collection<BieManifestSummary> newBiesFromPriorPackageVersion = new ArrayList<>();
        Collection<BieManifestSummary> removedBiesFromPriorPackageVersion = new ArrayList<>();
        Collection<BieManifestSummary> changedBiesFromPriorPackageVersion = new ArrayList<>();
        Collection<BieManifestSummary> deprecatedBiesFromPriorPackageVersion = new ArrayList<>();
        Map<LibraryId, Collection<ReleaseSummaryRecord>> releaseMapByLibraryId = new HashMap<>();

        BiePackageSummaryRecord prevPackage =
                (currentPackage.prevBiePackageId() != null) ?
                        biePackageQueryRepository.getBiePackageSummary(currentPackage.prevBiePackageId()) : null;

        List<BiePackageManifestEntry> biePackageManifestEntryList = new ArrayList<>();
        List<TopLevelAsbiepSummaryRecord> currentTopLevelAsbiepList =
                biePackageQueryRepository.getTopLevelAsbiepIdListInBiePackage(currentPackage.biePackageId())
                        .stream().map(e -> topLevelAsbiepQueryRepository.getTopLevelAsbiepSummary(e))
                        .collect(Collectors.toList());
        List<TopLevelAsbiepSummaryRecord> prevTopLevelAsbiepList = (prevPackage != null) ?
                biePackageQueryRepository.getTopLevelAsbiepIdListInBiePackage(prevPackage.biePackageId())
                        .stream().map(e -> topLevelAsbiepQueryRepository.getTopLevelAsbiepSummary(e))
                        .collect(Collectors.toList()) : Collections.emptyList();

        for (TopLevelAsbiepSummaryRecord currentTopLevelAsbiep : currentTopLevelAsbiepList) {

            releaseMapByLibraryId.putIfAbsent(currentTopLevelAsbiep.library().libraryId(), new ArrayList<>());
            Collection<ReleaseSummaryRecord> releaseSummaryRecords = releaseMapByLibraryId.get(currentTopLevelAsbiep.library().libraryId());
            if (releaseSummaryRecords.stream().filter(e -> e.releaseId().equals(currentTopLevelAsbiep.release().releaseId())).count() == 0) {
                releaseSummaryRecords.add(currentTopLevelAsbiep.release());
            }

            BiePackageManifestEntry biePackageManifestEntry = null;
            for (TopLevelAsbiepSummaryRecord prevTopLevelAsbiep : prevTopLevelAsbiepList) {
                BieTrackContext context = diff(requester, currentTopLevelAsbiep, prevTopLevelAsbiep, pathDelimiter);
                if (context != null) {
                    biePackageManifestEntry = new BiePackageManifestEntry(
                            newBieManifestDetail(currentTopLevelAsbiep,
                                    resolveRemark(asbiepQueryRepository, currentTopLevelAsbiep),
                                    resolveBusinessContexts(
                                            businessContextQueryRepository,
                                            contextSchemeQueryRepository,
                                            currentTopLevelAsbiep),
                                    generatedFilesByTopLevelAsbiepId.get(currentTopLevelAsbiep.topLevelAsbiepId())),
                            prevTopLevelAsbiep.guid(),
                            prevTopLevelAsbiep.version(),
                            backwardCompatibilityOf(context),
                            true,
                            context.added,
                            context.removed,
                            context.changed,
                            context.deprecated
                    );
                    break;
                }
            }

            if (biePackageManifestEntry != null) { // found a BIE replaced from the prior package version
                BieManifestSummary bieManifestSummary = newBieManifestSummary(currentTopLevelAsbiep);
                if (!biePackageManifestEntry.addedComponentsFromPriorPackageVersion().isEmpty() ||
                    !biePackageManifestEntry.removedComponentsFromPriorPackageVersion().isEmpty() ||
                    !biePackageManifestEntry.changedComponentsFromPriorPackageVersion().isEmpty() ||
                    !biePackageManifestEntry.deprecatedComponentsFromPriorPackageVersion().isEmpty()) {
                    changedBiesFromPriorPackageVersion.add(bieManifestSummary);

                    if (currentTopLevelAsbiep.deprecated()) {
                        deprecatedBiesFromPriorPackageVersion.add(bieManifestSummary);
                    }
                }
            } else {
                biePackageManifestEntry = new BiePackageManifestEntry(
                        newBieManifestDetail(currentTopLevelAsbiep,
                                resolveRemark(asbiepQueryRepository, currentTopLevelAsbiep),
                                resolveBusinessContexts(
                                        businessContextQueryRepository,
                                        contextSchemeQueryRepository,
                                        currentTopLevelAsbiep),
                                generatedFilesByTopLevelAsbiepId.get(currentTopLevelAsbiep.topLevelAsbiepId())),
                        null,
                        null,
                        new BackwardCompatibility(false, false, false),
                        false,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );
            }

            biePackageManifestEntryList.add(biePackageManifestEntry);
        }

        for (TopLevelAsbiepSummaryRecord prevTopLevelAsbiep : prevTopLevelAsbiepList) {
            boolean matched = false;
            for (TopLevelAsbiepSummaryRecord currentTopLevelAsbiep : currentTopLevelAsbiepList) {
                BieDocument current = bieReadService.getBieDocument(requester, currentTopLevelAsbiep.topLevelAsbiepId());
                BieDocument previous = bieReadService.getBieDocument(requester, prevTopLevelAsbiep.topLevelAsbiepId());

                Asbiep currentAsbiep = current.getRootAsbiep();
                Asbiep prevAsbiep = previous.getRootAsbiep();

                AsccpSummaryRecord currentAsccp = current.getCcDocument().getAsccp(currentAsbiep.getBasedAsccpManifestId());
                AsccpSummaryRecord prevAsccp = previous.getCcDocument().getAsccp(prevAsbiep.getBasedAsccpManifestId());

                if (currentAsccp.guid().equals(prevAsccp.guid())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                removedBiesFromPriorPackageVersion.add(newBieManifestSummary(prevTopLevelAsbiep));
            }
        }

        LibraryQueryRepository libraryQueryRepository = repositoryFactory.libraryQueryRepository(requester);
        Collection<LibraryCompatibility> libraryCompatibilityCollection = new ArrayList<>();
        for (LibraryId libraryId : releaseMapByLibraryId.keySet()) {
            LibraryDetailsRecord libraryDetailsRecord = libraryQueryRepository.getLibraryDetails(libraryId);
            Collection<ReleaseSummaryRecord> releases = releaseMapByLibraryId.get(libraryId);
            ReleaseSummaryRecord latestReleasesInCollection = findLatestReleasesIn(requester, libraryId, releases);
            libraryCompatibilityCollection.add(new LibraryCompatibility(
                    libraryDetailsRecord.name(),
                    latestReleasesInCollection.releaseNum()
            ));
        }

        BiePackageManifest biePackageMetadata = new BiePackageManifest(
                currentPackage.guid(),
                currentPackage.versionGuid(),
                currentPackage.name(),
                currentPackage.versionId(),
                currentPackage.versionName(),
                (prevPackage != null) ? prevPackage.guid(): null,
                (prevPackage != null) ? prevPackage.versionGuid() : null,
                (prevPackage != null) ? prevPackage.versionId() : null,
                currentPackage.revisionReason(),
                newBiesFromPriorPackageVersion,
                removedBiesFromPriorPackageVersion,
                changedBiesFromPriorPackageVersion,
                deprecatedBiesFromPriorPackageVersion,
                libraryCompatibilityCollection,
                biePackageManifestEntryList);
        BiePackageManifestResponse biePackageManifest =
                new BiePackageManifestResponse(BIE_PACKAGE_MANIFEST_VERSION, biePackageMetadata);

        return biePackageManifest;
    }

    private String resolveRemark(org.oagi.score.gateway.http.api.bie_management.repository.AsbiepQueryRepository asbiepQueryRepository,
                                 TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = asbiepQueryRepository.getAsbiepSummary(topLevelAsbiep.asbiepId());
        return (asbiep != null) ? asbiep.remark() : null;
    }

    private Collection<BusinessContextManifest> resolveBusinessContexts(
            BusinessContextQueryRepository businessContextQueryRepository,
            ContextSchemeQueryRepository contextSchemeQueryRepository,
            TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        List<BusinessContextSummaryRecord> businessContextSummaries =
                businessContextQueryRepository.getBusinessContextSummaryList(topLevelAsbiep.topLevelAsbiepId());
        if (businessContextSummaries == null || businessContextSummaries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<ContextSchemeId, ContextSchemeDetailsRecord> contextSchemeDetailsById = new HashMap<>();
        List<BusinessContextManifest> businessContexts = new ArrayList<>();
        for (BusinessContextSummaryRecord businessContextSummary : businessContextSummaries) {
            List<BusinessContextValueRecord> businessContextValueRecords =
                    businessContextQueryRepository.getBusinessContextValueList(businessContextSummary.businessContextId());
            List<BusinessContextValueManifest> businessContextValues = new ArrayList<>();
            if (businessContextValueRecords != null) {
                for (BusinessContextValueRecord businessContextValueRecord : businessContextValueRecords) {
                    ContextSchemeDetailsRecord contextSchemeDetails = contextSchemeDetailsById.computeIfAbsent(
                            businessContextValueRecord.contextSchemeId(),
                            contextSchemeQueryRepository::getContextSchemeDetails);
                    businessContextValues.add(new BusinessContextValueManifest(
                            businessContextValueRecord.contextCategoryName(),
                            new ContextSchemeManifest(
                                    (contextSchemeDetails != null) ? contextSchemeDetails.schemeId() : null,
                                    (contextSchemeDetails != null) ? contextSchemeDetails.schemeName() : businessContextValueRecord.contextSchemeName(),
                                    (contextSchemeDetails != null) ? contextSchemeDetails.schemeAgencyId() : null,
                                    (contextSchemeDetails != null) ? contextSchemeDetails.schemeVersionId() : null),
                            businessContextValueRecord.contextSchemeValue()));
                }
            }

            businessContexts.add(new BusinessContextManifest(
                    businessContextSummary.name(),
                    businessContextValues));
        }

        return businessContexts;
    }

    private ReleaseSummaryRecord findLatestReleasesIn(ScoreUser requester,
                                                      LibraryId libraryId,
                                                      Collection<ReleaseSummaryRecord> releases) {
        if (releases == null || releases.isEmpty()) {
            return null;
        }

        ReleaseQueryRepository releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
        List<ReleaseDetailsRecord> releaseDetailsRecords =
                releaseQueryRepository.getReleaseSummaryList(libraryId).stream()
                        .map(e -> releaseQueryRepository.getReleaseDetails(e.releaseId()))
                        .collect(Collectors.toList());
        Map<ReleaseId, ReleaseDetailsRecord> releaseInDatabaseMap = releaseDetailsRecords.stream()
                .collect(Collectors.toMap(ReleaseDetailsRecord::releaseId, Function.identity()));
        ReleaseDetailsRecord currentRelease = releaseDetailsRecords.stream()
                .filter(e -> e.next() == null).findFirst().orElse(null);

        Map<ReleaseId, ReleaseSummaryRecord> releaseInCollectionMap = releases.stream()
                .collect(Collectors.toMap(ReleaseSummaryRecord::releaseId, Function.identity()));

        while (currentRelease != null) {
            if (releaseInCollectionMap.containsKey(currentRelease.releaseId())) {
                return releaseInCollectionMap.get(currentRelease.releaseId());
            }

            if (currentRelease.prev() != null) {
                currentRelease = releaseInDatabaseMap.get(currentRelease.prev().releaseId());
            } else {
                break;
            }
        }

        throw new IllegalStateException("No latest release in the provided collection.");
    }

    private class BieTrackContext {

        ScoreUser requester;
        String delimiter;

        BieDocument currentBieDocument;
        BieDocument prevBieDocument;

        Collection<BieComponentChange> added = new ArrayList<>();
        Collection<BieComponentChange> removed = new ArrayList<>();
        Collection<BieComponentChange> changed = new ArrayList<>();
        Collection<BieComponentChange> deprecated = new ArrayList<>();

        // Issue #1733: schema-level (Type-A) backward-incompatibility accumulated across the whole BIE tree
        // (including reused/nested subtrees), tracked per target syntax via the pure rules' accumulator.
        final BieBackwardCompatibilityRules.Accumulator compatibility = new BieBackwardCompatibilityRules.Accumulator();

        void recordBreak(boolean xmlSchema, boolean jsonSchema) {
            this.compatibility.recordBreak(xmlSchema, jsonSchema);
        }

        void recordStructuralBreak() {
            this.compatibility.recordStructuralBreak();
        }

        public BieTrackContext(ScoreUser requester,
                               BieDocument currentBieDocument, BieDocument prevBieDocument) {
            this.requester = requester;

            this.currentBieDocument = currentBieDocument;
            this.prevBieDocument = prevBieDocument;
        }

        public void setPathDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }
    }

    public BieTrackContext diff(ScoreUser requester,
                                TopLevelAsbiepSummaryRecord currentTopLevelAsbiep,
                                TopLevelAsbiepSummaryRecord prevTopLevelAsbiep,
                                String pathDelimiter) {

        BieDocument current = bieReadService.getBieDocument(requester, currentTopLevelAsbiep.topLevelAsbiepId());
        BieDocument previous = bieReadService.getBieDocument(requester, prevTopLevelAsbiep.topLevelAsbiepId());

        BieTrackContext context = new BieTrackContext(requester, current, previous);
        context.setPathDelimiter(pathDelimiter);

        Asbiep currentAsbiep = current.getRootAsbiep();
        Asbiep prevAsbiep = previous.getRootAsbiep();

        AsccpSummaryRecord currentAsccp = current.getCcDocument().getAsccp(currentAsbiep.getBasedAsccpManifestId());
        AsccpSummaryRecord prevAsccp = previous.getCcDocument().getAsccp(prevAsbiep.getBasedAsccpManifestId());

        if (currentAsccp.guid().equals(prevAsccp.guid())) {
            traverse(context, current.getAbie(currentAsbiep), previous.getAbie(prevAsbiep),
                    path(context, null, currentAsccp.propertyTerm()));
            return context;
        }

        return null;
    }

    private void traverse(BieTrackContext context, @Nullable Asbie currentAsbie, @Nullable Asbie prevAsbie, String parentPath) {
        Asbiep currentAsbiep = null;
        AsccpSummaryRecord currentAsccp = null;
        if (currentAsbie != null) {
            currentAsbiep = context.currentBieDocument.getAsbiep(currentAsbie);
            currentAsccp = context.currentBieDocument.getCcDocument().getAsccp(currentAsbiep.getBasedAsccpManifestId());
        }

        Asbiep prevAsbiep = null;
        AsccpSummaryRecord prevAsccp = null;
        if (prevAsbie != null) {
            prevAsbiep = context.prevBieDocument.getAsbiep(prevAsbie);
            prevAsccp = context.prevBieDocument.getCcDocument().getAsccp(prevAsbiep.getBasedAsccpManifestId());
        }

        if (currentAsccp == null && prevAsccp == null) {
            return;
        }

        if (currentAsbie != null && prevAsbie != null) {
            List<String> changes = changes(context, currentAsbie, currentAsbiep, prevAsbie, prevAsbiep);

            if (!changes.isEmpty()) {
                context.changed.add(newBieElementChange(context, currentAsbie, parentPath, changes));
            }

            evaluateAsbieCompatibility(context, currentAsbie, prevAsbie);

            if (currentAsbie.isDeprecated() && !prevAsbie.isDeprecated()) {
                context.deprecated.add(newBieElementChange(context, currentAsbie, parentPath, Collections.emptyList()));
            }
        }

        traverse(context,
                context.currentBieDocument.getAbie(currentAsbiep),
                context.prevBieDocument.getAbie(prevAsbiep),
                path(context, parentPath, (currentAsccp != null) ? currentAsccp.propertyTerm() : prevAsccp.propertyTerm()));
    }

    private String path(BieTrackContext context, String parentPath, String term) {
        if (hasLength(term)) {
            term = term.replaceAll("\\s+", "");
        }
        if (!hasLength(parentPath)) {
            return term;
        }

        return String.join(context.delimiter, parentPath, term);
    }

    private void traverse(BieTrackContext context, @Nullable Bbie currentBbie, @Nullable Bbie prevBbie, String parentPath) {

        Bbiep currentBbiep = null;
        BccpSummaryRecord currentBccp = null;

        if (currentBbie != null) {
            currentBbiep = context.currentBieDocument.getBbiep(currentBbie);
            currentBccp = context.currentBieDocument.getCcDocument().getBccp(currentBbiep.getBasedBccpManifestId());
        }

        Bbiep prevBbiep = null;
        BccpSummaryRecord prevBccp = null;
        if (prevBbie != null) {
            prevBbiep = context.prevBieDocument.getBbiep(prevBbie);
            prevBccp = context.prevBieDocument.getCcDocument().getBccp(prevBbiep.getBasedBccpManifestId());
        }

        if (currentBccp == null && prevBccp == null) {
            return;
        }

        if (currentBbie != null && prevBbie != null) {
            List<String> changes = changes(context, currentBbie, currentBbiep, prevBbie, prevBbiep);

            if (!changes.isEmpty()) {
                context.changed.add(newBieElementChange(context, currentBbie, parentPath, changes));
            }

            evaluateBbieCompatibility(context, currentBbie, prevBbie);

            if (currentBbie.isDeprecated() && !prevBbie.isDeprecated()) {
                context.deprecated.add(newBieElementChange(context, currentBbie, parentPath, Collections.emptyList()));
            }
        }

        List<BbieSc> currentBbieScs = context.currentBieDocument.getBbieScList(currentBbie);
        List<BbieSc> prevBbieScs = context.prevBieDocument.getBbieScList(prevBbie);

        if (currentBbie != null && prevBbie != null) {
            // JSON renders a BBIE with used supplementary components as an object and one without as a bare scalar;
            // crossing that boundary (gaining the first / losing the last used SC) invalidates old JSON documents.
            evaluateJsonScWrapperFlip(context, !currentBbieScs.isEmpty(), !prevBbieScs.isEmpty());
        }

        if (currentBccp != null) {
            String bbiepPath = path(context, parentPath, currentBccp.propertyTerm());

            for (BbieSc currentBbieSc : currentBbieScs) {

                BbieSc prevBbieSc = prevBbieScs.stream()
                        .filter(e -> matches(currentBbieSc, context.currentBieDocument, e, context.prevBieDocument))
                        .findFirst().orElse(null);
                boolean matched = (prevBbieSc != null);
                if (matched) {
                    List<String> changes = changes(context, currentBbieSc, prevBbieSc);

                    if (!changes.isEmpty()) {
                        context.changed.add(newBieElementChange(context, currentBbieSc, bbiepPath, changes));
                    }

                    evaluateBbieScCompatibility(context, currentBbieSc, prevBbieSc);

                    if (currentBbieSc.isDeprecated() && !prevBbieSc.isDeprecated()) {
                        context.deprecated.add(newBieElementChange(context, currentBbieSc, bbiepPath, Collections.emptyList()));
                    }
                } else {
                    context.added.add(newBieElementChange(context, currentBbieSc, bbiepPath, Collections.emptyList()));
                    // boundary add of a REQUIRED supplementary component (parent BBIE already existed): a new
                    // required component in the structure -> rejected by both syntaxes; structure changes.
                    if (prevBbie != null && currentBbieSc.getCardinalityMin() > 0) {
                        context.recordStructuralBreak();
                    }
                }
            }
        }

        for (BbieSc prevBbieSc : prevBbieScs) {
            String bbiepPath = path(context, parentPath, prevBccp.propertyTerm());

            BbieSc currentBbieSc = currentBbieScs.stream()
                    .filter(e -> matches(prevBbieSc, context.prevBieDocument, e, context.currentBieDocument))
                    .findFirst().orElse(null);
            boolean matched = (currentBbieSc != null);
            if (!matched) {
                context.removed.add(newBieElementChange(context, prevBbieSc, bbiepPath, Collections.emptyList()));
                // boundary removal (parent BBIE still exists): a supplementary component disappears from the
                // structure -> any old document that used it becomes invalid (closed XSD restriction / JSON
                // additionalProperties:false), required or not; structure changes.
                if (currentBbie != null) {
                    context.recordStructuralBreak();
                }
            }
        }
    }

    private List<String> changes(BieTrackContext context,
                                 Asbie a, Asbiep aAsbiep,
                                 Asbie b, Asbiep bAsbiep) {
        List<String> changes = new ArrayList<>();

        // check 'cardinality'
        if (a.getCardinalityMin() != b.getCardinalityMin() ||
            a.getCardinalityMax() != b.getCardinalityMax()) {
            changes.add("cardinality");
        }

        // check 'nillable'
        if (a.isNillable() != b.isNillable()) {
            changes.add("nillable");
        }

        // check 'remark'
        if (!Objects.equals(a.getRemark(), b.getRemark()) ||
            !Objects.equals(aAsbiep.getRemark(), bAsbiep.getRemark())) {
            changes.add("remark");
        }

        // check 'definition'
        if (!Objects.equals(a.getDefinition(), b.getDefinition()) ||
            !Objects.equals(aAsbiep.getDefinition(), bAsbiep.getDefinition())) {
            changes.add("definition");
        }

        return changes;
    }

    private List<String> changes(BieTrackContext context,
                                 Bbie a, Bbiep aBbiep,
                                 Bbie b, Bbiep bBbiep) {
        List<String> changes = new ArrayList<>();

        // check 'cardinality'
        if (a.getCardinalityMin() != b.getCardinalityMin() ||
            a.getCardinalityMax() != b.getCardinalityMax()) {
            changes.add("cardinality");
        }

        // check 'value domain'
        if (!hasSameValueDomain(context, a, b,
                Bbie::getXbtManifestId, Bbie::getCodeListManifestId, Bbie::getAgencyIdListManifestId)) {
            changes.add("value domain");
        }

        // check 'value constraint'
        if (hasLength(a.getFixedValue()) && !Objects.equals(a.getFixedValue(), b.getFixedValue())) {
            changes.add("value constraint");
        } else if (hasLength(a.getDefaultValue()) && !Objects.equals(a.getDefaultValue(), b.getDefaultValue())) {
            changes.add("value constraint");
        } else if (hasLength(b.getFixedValue()) || hasLength(b.getDefaultValue())) {
            changes.add("value constraint");
        }

        // check 'facet'
        if (!Objects.equals(a.getFacetMinLength(), b.getFacetMinLength()) ||
            !Objects.equals(a.getFacetMaxLength(), b.getFacetMaxLength()) ||
            !Objects.equals(a.getFacetPattern(), b.getFacetPattern())) {
            changes.add("facet");
        }

        // check 'nillable'
        if (a.isNillable() != b.isNillable()) {
            changes.add("nillable");
        }

        // check 'remark'
        if (!Objects.equals(a.getRemark(), b.getRemark()) ||
            !Objects.equals(aBbiep.getRemark(), bBbiep.getRemark())) {
            changes.add("remark");
        }

        // check 'definition'
        if (!Objects.equals(a.getDefinition(), b.getDefinition()) ||
            !Objects.equals(aBbiep.getDefinition(), bBbiep.getDefinition())) {
            changes.add("definition");
        }

        // check 'example'
        if (!Objects.equals(a.getExample(), b.getExample())) {
            changes.add("example");
        }

        return changes;
    }

    private List<String> changes(BieTrackContext context,
                                 BbieSc a, BbieSc b) {
        List<String> changes = new ArrayList<>();

        // check 'cardinality'
        if (a.getCardinalityMin() != b.getCardinalityMin() ||
            a.getCardinalityMax() != b.getCardinalityMax()) {
            changes.add("cardinality");
        }

        // check 'value domain'
        if (!hasSameValueDomain(context, a, b,
                BbieSc::getXbtManifestId, BbieSc::getCodeListManifestId, BbieSc::getAgencyIdListManifestId)) {
            changes.add("value domain");
        }

        // check 'value constraint'
        if (hasLength(a.getFixedValue()) && !Objects.equals(a.getFixedValue(), b.getFixedValue())) {
            changes.add("value constraint");
        } else if (hasLength(a.getDefaultValue()) && !Objects.equals(a.getDefaultValue(), b.getDefaultValue())) {
            changes.add("value constraint");
        } else if (hasLength(b.getFixedValue()) || hasLength(b.getDefaultValue())) {
            changes.add("value constraint");
        }

        // check 'facet'
        if (!Objects.equals(a.getFacetMinLength(), b.getFacetMinLength()) ||
            !Objects.equals(a.getFacetMaxLength(), b.getFacetMaxLength()) ||
            !Objects.equals(a.getFacetPattern(), b.getFacetPattern())) {
            changes.add("facet");
        }

        // check 'nillable'
        if (a.isNillable() != b.isNillable()) {
            changes.add("nillable");
        }

        // check 'remark'
        if (!Objects.equals(a.getRemark(), b.getRemark())) {
            changes.add("remark");
        }

        // check 'definition'
        if (!Objects.equals(a.getDefinition(), b.getDefinition())) {
            changes.add("definition");
        }

        // check 'example'
        if (!Objects.equals(a.getExample(), b.getExample())) {
            changes.add("example");
        }

        return changes;
    }

    private boolean matches(BbieSc a, BieDocument aDoc,
                            BbieSc b, BieDocument bDoc) {
        DtScSummaryRecord aDtSc = aDoc.getCcDocument().getDtSc(a.getBasedDtScManifestId());
        DtScSummaryRecord bDtSc = bDoc.getCcDocument().getDtSc(b.getBasedDtScManifestId());
        return aDtSc.guid().equals(bDtSc.guid());
    }

    private boolean matches(BieAssociation a, BieDocument aDoc,
                            BieAssociation b, BieDocument bDoc) {
        if (a.isAsbie() && b.isBbie() ||
                a.isBbie() && b.isAsbie()) {
            return false;
        }
        if (a.isAsbie()) {
            AsccSummaryRecord aAscc =
                    aDoc.getCcDocument().getAscc(((Asbie) a).getBasedAsccManifestId());
            AsccSummaryRecord bAscc =
                    bDoc.getCcDocument().getAscc(((Asbie) b).getBasedAsccManifestId());

            return aAscc.guid().equals(bAscc.guid());
        } else {
            BccSummaryRecord aBcc =
                    aDoc.getCcDocument().getBcc(((Bbie) a).getBasedBccManifestId());
            BccSummaryRecord bBcc =
                    bDoc.getCcDocument().getBcc(((Bbie) b).getBasedBccManifestId());

            return aBcc.guid().equals(bBcc.guid());
        }
    }

    private void traverse(BieTrackContext context, @Nullable Abie currentAbie, @Nullable Abie prevAbie, String parentPath) {
        Collection<BieAssociation> currentList = context.currentBieDocument.getAssociations(currentAbie);
        Collection<BieAssociation> prevList = context.prevBieDocument.getAssociations(prevAbie);

        for (BieAssociation current : currentList) {
            BieAssociation matchedPrev = prevList.stream()
                    .filter(prev -> matches(current, context.currentBieDocument, prev, context.prevBieDocument))
                    .findFirst().orElse(null);

            boolean matched = (matchedPrev != null);
            if (current.isAsbie()) {
                if (!matched) {
                    context.added.add(newBieElementChange(context, (Asbie) current, parentPath, Collections.emptyList()));
                    // boundary add of a REQUIRED element (parent ABIE already existed): a new required element in
                    // the structure -> rejected by both syntaxes and breaks the syntax-independent structure.
                    if (prevAbie != null && ((Asbie) current).getCardinalityMin() > 0) {
                        context.recordStructuralBreak();
                    }
                }
                traverse(context, (Asbie) current, (matchedPrev != null) ? ((Asbie) matchedPrev) : null, parentPath);
            } else {
                if (!matched) {
                    context.added.add(newBieElementChange(context, (Bbie) current, parentPath, Collections.emptyList()));
                    // boundary add of a REQUIRED element (parent ABIE already existed): a new required element in
                    // the structure -> rejected by both syntaxes and breaks the syntax-independent structure.
                    if (prevAbie != null && ((Bbie) current).getCardinalityMin() > 0) {
                        context.recordStructuralBreak();
                    }
                }
                traverse(context, (Bbie) current, (matchedPrev != null) ? ((Bbie) matchedPrev) : null, parentPath);
            }
        }

        for (BieAssociation prev : prevList) {
            BieAssociation matchedCurrent = currentList.stream()
                    .filter(current -> matches(prev, context.prevBieDocument, current, context.currentBieDocument))
                    .findFirst().orElse(null);

            boolean matched = (matchedCurrent != null);
            if (prev.isAsbie()) {
                if (!matched) {
                    context.removed.add(newBieElementChange(context, (Asbie) prev, parentPath, Collections.emptyList()));
                    // boundary removal (parent ABIE still exists): an element disappears from the structure -> any
                    // old document that used it is invalid (closed XSD sequence / JSON additionalProperties:false),
                    // required or not, and the syntax-independent structure changes.
                    if (currentAbie != null) {
                        context.recordStructuralBreak();
                    }
                    traverse(context, null, (Asbie) prev, parentPath);
                }
            } else {
                if (!matched) {
                    context.removed.add(newBieElementChange(context, (Bbie) prev, parentPath, Collections.emptyList()));
                    // boundary removal (parent ABIE still exists): an element disappears from the structure -> any
                    // old document that used it is invalid (closed XSD sequence / JSON additionalProperties:false),
                    // required or not, and the syntax-independent structure changes.
                    if (currentAbie != null) {
                        context.recordStructuralBreak();
                    }
                    traverse(context, null, (Bbie) prev, parentPath);
                }
            }
        }
    }

    private <T> boolean hasSameValueDomain(
            BieTrackContext context,
            T current,
            T previous,
            Function<T, XbtManifestId> xbtIdGetter,
            Function<T, CodeListManifestId> codeListIdGetter,
            Function<T, AgencyIdListManifestId> agencyIdListIdGetter) {

        return equals(
                () -> context.currentBieDocument.getCcDocument().getXbt(xbtIdGetter.apply(current)),
                () -> context.prevBieDocument.getCcDocument().getXbt(xbtIdGetter.apply(previous)),
                XbtSummaryRecord::guid
        ) || equals(
                () -> context.currentBieDocument.getCcDocument().getCodeList(codeListIdGetter.apply(current)),
                () -> context.prevBieDocument.getCcDocument().getCodeList(codeListIdGetter.apply(previous)),
                CodeListSummaryRecord::guid
        ) || equals(
                () -> context.currentBieDocument.getCcDocument().getAgencyIdList(agencyIdListIdGetter.apply(current)),
                () -> context.prevBieDocument.getCcDocument().getAgencyIdList(agencyIdListIdGetter.apply(previous)),
                AgencyIdListSummaryRecord::guid
        );
    }

    private <T> boolean equals(Supplier<T> currentSupplier, Supplier<T> prevSupplier, Function<T, Guid> guidGetter) {
        try {
            T current = currentSupplier.get();
            T prev = prevSupplier.get();
            if (current != null && prev != null) {
                return guidGetter.apply(current).equals(guidGetter.apply(prev));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private BieComponentChange newBieElementChange(BieTrackContext context, Asbie asbie, String parentPath, List<String> changes) {
        AccQueryRepository accQueryRepository = repositoryFactory.accQueryRepository(context.requester);
        AsccSummaryRecord basedAscc = accQueryRepository.getAsccSummary(asbie.getBasedAsccManifestId());

        AsccpQueryRepository asccpQueryRepository = repositoryFactory.asccpQueryRepository(context.requester);
        AsccpSummaryRecord toAsccp = asccpQueryRepository.getAsccpSummary(basedAscc.toAsccpManifestId());

        return new BieComponentChange(
                toAsccp.propertyTerm(),
                parentPath,
                changes);
    }

    private BieComponentChange newBieElementChange(BieTrackContext context, Bbie bbie, String parentPath, List<String> changes) {
        AccQueryRepository accQueryRepository = repositoryFactory.accQueryRepository(context.requester);
        BccSummaryRecord basedBcc = accQueryRepository.getBccSummary(bbie.getBasedBccManifestId());

        BccpQueryRepository bccpQueryRepository = repositoryFactory.bccpQueryRepository(context.requester);
        BccpSummaryRecord toBccp = bccpQueryRepository.getBccpSummary(basedBcc.toBccpManifestId());

        return new BieComponentChange(
                toBccp.propertyTerm(),
                parentPath,
                changes);
    }

    private BieComponentChange newBieElementChange(BieTrackContext context, BbieSc bbieSc, String parentPath, List<String> changes) {
        DtQueryRepository dtQueryRepository = repositoryFactory.dtQueryRepository(context.requester);
        DtScSummaryRecord dtSc = dtQueryRepository.getDtScSummary(bbieSc.getBasedDtScManifestId());

        return new BieComponentChange(
                dtScName(dtSc),
                parentPath,
                changes);
    }

    private String dtScName(DtScSummaryRecord dtSc) {
        return String.join(" ", Arrays.asList(dtSc.propertyTerm(), dtSc.representationTerm()));
    }

    /*
     * Issue #1733: builds the schema-level (Type-A) backward-compatibility indicator for a BIE that was replaced
     * from the prior package version. syntaxIndependent reflects breaks that apply regardless of rendering;
     * xmlSchema / jsonSchema additionally reflect breaks specific to each syntax. Service-level (Type-B)
     * compatibility (e.g. movement of data, code-list semantics) is out of scope.
     */
    private BackwardCompatibility backwardCompatibilityOf(BieTrackContext context) {
        return context.compatibility.toBackwardCompatibility();
    }

    // ----- Issue #1733: per-element backward-compatibility evaluation for MATCHED elements -----

    private void evaluateAsbieCompatibility(BieTrackContext context, Asbie current, Asbie prev) {
        // ASBIE carries occurrence + nillable. Cardinality tightening and nillable removal break both syntaxes;
        // crossing the JSON array/object boundary breaks JSON only.
        if (isMoreRestrictiveCardinality(current.getCardinalityMin(), current.getCardinalityMax(),
                prev.getCardinalityMin(), prev.getCardinalityMax())) {
            context.recordBreak(true, true);
        }
        evaluateJsonArrayFlip(context, current.getCardinalityMax(), prev.getCardinalityMax());
        evaluateNillable(context, current.isNillable(), prev.isNillable(), true); // ASBIE always renders as an element
    }

    private void evaluateBbieCompatibility(BieTrackContext context, Bbie current, Bbie prev) {
        // Resolve the based BCC once: its entity type drives both the entity-type flip check and the
        // element-vs-attribute scoping of the nillable check.
        var accQueryRepository = repositoryFactory.accQueryRepository(context.requester);
        BccSummaryRecord currentBcc = safeGet(() -> accQueryRepository.getBccSummary(current.getBasedBccManifestId()));
        BccSummaryRecord prevBcc = safeGet(() -> accQueryRepository.getBccSummary(prev.getBasedBccManifestId()));
        boolean currentIsElement = (currentBcc == null) || currentBcc.entityType() == EntityType.Element;

        if (isMoreRestrictiveCardinality(current.getCardinalityMin(), current.getCardinalityMax(),
                prev.getCardinalityMin(), prev.getCardinalityMax())) {
            context.recordBreak(true, true);
        }
        evaluateJsonArrayFlip(context, current.getCardinalityMax(), prev.getCardinalityMax());
        evaluateNillable(context, current.isNillable(), prev.isNillable(), currentIsElement);
        evaluateValueConstraint(context, current.getFixedValue(), prev.getFixedValue());
        evaluateFacet(context, current.getFacetMinLength(), current.getFacetMaxLength(), current.getFacetPattern(),
                prev.getFacetMinLength(), prev.getFacetMaxLength(), prev.getFacetPattern());
        evaluateValueDomain(context,
                current.getXbtManifestId(), current.getCodeListManifestId(), current.getAgencyIdListManifestId(),
                prev.getXbtManifestId(), prev.getCodeListManifestId(), prev.getAgencyIdListManifestId());
        // Entity-type flip (Element <-> Attribute, possible across releases): the XSD shape (xsd:element vs
        // xsd:attribute) and the name's first-letter casing change, so old documents are invalid; the JSON
        // generator ignores entity type entirely (every BBIE is a camelCase property) -> XML-only break.
        if (currentBcc != null && prevBcc != null && currentBcc.entityType() != prevBcc.entityType()) {
            context.recordBreak(true, false);
        }
    }

    private void evaluateBbieScCompatibility(BieTrackContext context, BbieSc current, BbieSc prev) {
        if (isMoreRestrictiveCardinality(current.getCardinalityMin(), current.getCardinalityMax(),
                prev.getCardinalityMin(), prev.getCardinalityMax())) {
            context.recordBreak(true, true);
        }
        // A supplementary component renders as an XSD attribute (no xsi:nil), so nillable is JSON-only.
        evaluateNillable(context, current.isNillable(), prev.isNillable(), false);
        evaluateValueConstraint(context, current.getFixedValue(), prev.getFixedValue());
        evaluateFacet(context, current.getFacetMinLength(), current.getFacetMaxLength(), current.getFacetPattern(),
                prev.getFacetMinLength(), prev.getFacetMaxLength(), prev.getFacetPattern());
        evaluateValueDomain(context,
                current.getXbtManifestId(), current.getCodeListManifestId(), current.getAgencyIdListManifestId(),
                prev.getXbtManifestId(), prev.getCodeListManifestId(), prev.getAgencyIdListManifestId());
    }

    /*
     * nillable true -> false drops the null representation, invalidating documents that relied on it. In XSD only
     * elements carry nillable (xsi:nil); an xsd:attribute cannot be nillable. The JSON generator emits a "null"
     * type union for any nillable leaf regardless of entity type. So on an element the removal breaks both
     * syntaxes; on an attribute / supplementary component (where XSD never carried nillable) it breaks JSON only.
     * Turning nillable on is non-breaking.
     */
    private void evaluateNillable(BieTrackContext context, boolean newNillable, boolean oldNillable,
                                 boolean xsdNillableApplies) {
        if (BieBackwardCompatibilityRules.nillableRemoved(newNillable, oldNillable)) {
            context.recordBreak(xsdNillableApplies, true);
        }
    }

    /*
     * Adding a fixed value where none existed, or changing an existing fixed value, rejects previously valid
     * instances in both XSD (@fixed) and JSON ("const"). Removing a fixed value is a loosening; a default value
     * never constrains instances and is therefore ignored.
     */
    private void evaluateValueConstraint(BieTrackContext context, String newFixed, String oldFixed) {
        if (BieBackwardCompatibilityRules.fixedValueBreaks(newFixed, oldFixed)) {
            context.recordBreak(true, true);
        }
    }

    /*
     * Tightening a length/pattern facet (raising minLength, lowering maxLength, or adding/changing a pattern)
     * rejects previously valid values. Both XSD and the JSON generator emit these facets for string types, so a
     * tightening is treated as syntax-independent. Loosening / removing facets is non-breaking.
     */
    private void evaluateFacet(BieTrackContext context,
                               BigInteger newMinLength, BigInteger newMaxLength, String newPattern,
                               BigInteger oldMinLength, BigInteger oldMaxLength, String oldPattern) {
        if (BieBackwardCompatibilityRules.facetTightened(newMinLength, newMaxLength, newPattern,
                oldMinLength, oldMaxLength, oldPattern)) {
            context.recordBreak(true, true);
        }
    }

    /*
     * Value-domain (primitive XBT / code list / agency id list) narrowing. This is the main per-syntax case:
     * a primitive restriction (e.g. xsd:normalizedString -> xsd:token) narrows the XSD value space but maps to the
     * identical JSON "string", so it breaks XML only. A code-list value removal or a type narrowing that also
     * narrows the JSON type/bounds breaks both.
     */
    private void evaluateValueDomain(BieTrackContext context,
                                     XbtManifestId newXbtId, CodeListManifestId newClId, AgencyIdListManifestId newAlId,
                                     XbtManifestId oldXbtId, CodeListManifestId oldClId, AgencyIdListManifestId oldAlId) {
        var curCc = context.currentBieDocument.getCcDocument();
        var prevCc = context.prevBieDocument.getCcDocument();

        boolean newIsCl = (newClId != null), newIsAl = (!newIsCl && newAlId != null);
        boolean oldIsCl = (oldClId != null), oldIsAl = (!oldIsCl && oldAlId != null);
        boolean newIsXbt = (!newIsCl && !newIsAl), oldIsXbt = (!oldIsCl && !oldIsAl);

        // enum -> enum of the same kind: compatible iff the new value set is a superset of the old one.
        if (oldIsCl && newIsCl) {
            Set<String> oldValues = codeListValues(safeGet(() -> prevCc.getCodeList(oldClId)));
            Set<String> newValues = codeListValues(safeGet(() -> curCc.getCodeList(newClId)));
            if (!newValues.containsAll(oldValues)) {
                context.recordBreak(true, true); // a code value was removed -> enum rejects it in both syntaxes
            }
            return;
        }
        if (oldIsAl && newIsAl) {
            Set<String> oldValues = agencyIdListValues(safeGet(() -> prevCc.getAgencyIdList(oldAlId)));
            Set<String> newValues = agencyIdListValues(safeGet(() -> curCc.getAgencyIdList(newAlId)));
            if (!newValues.containsAll(oldValues)) {
                context.recordBreak(true, true);
            }
            return;
        }
        // primitive -> enum adds an enumeration restriction (narrowing) in both syntaxes.
        if (oldIsXbt && (newIsCl || newIsAl)) {
            context.recordBreak(true, true);
            return;
        }
        // enum -> primitive removes the enumeration (widening) -> compatible.
        if ((oldIsCl || oldIsAl) && newIsXbt) {
            return;
        }
        // code list <-> agency id list, or any other kind switch involving enums -> conservative break.
        if (!(oldIsXbt && newIsXbt)) {
            context.recordBreak(true, true);
            return;
        }

        // primitive XBT -> primitive XBT.
        XbtSummaryRecord oldXbt = (oldXbtId != null) ? safeGet(() -> prevCc.getXbt(oldXbtId)) : null;
        XbtSummaryRecord newXbt = (newXbtId != null) ? safeGet(() -> curCc.getXbt(newXbtId)) : null;
        if (oldXbt == null || newXbt == null || oldXbt.guid().equals(newXbt.guid())) {
            return; // unresolved or unchanged
        }
        // XSD: walk the built-in type derivation lattice. A WIDENING (new is a super-type of old) accepts every old
        // value -> compatible. A RESTRICTION (new is a sub-type of old, e.g. normalizedString -> token) constrains
        // the value space -> break. A CROSS-BRANCH change breaks unless the new type accepts any character data
        // (a universal text type such as xsd:string), e.g. xsd:integer -> xsd:string stays XSD-compatible.
        boolean newIsSuperTypeOfOld = isAncestorOrSame(newXbt.guid(), oldXbt, prevCc);
        boolean newIsSubTypeOfOld = isAncestorOrSame(oldXbt.guid(), newXbt, curCc);
        boolean breaksXml = BieBackwardCompatibilityRules.xbtBreaksXml(
                newIsSuperTypeOfOld, newIsSubTypeOfOld, newXbt.builtInType());
        boolean breaksJson = BieBackwardCompatibilityRules.jsonTypeNarrows(
                oldXbt.jbt202012Map(), newXbt.jbt202012Map());
        if (breaksXml || breaksJson) {
            context.recordBreak(breaksXml, breaksJson);
        }
    }

    /*
     * Walks the XBT restriction lattice (subtype_of chain) of {@code descendant} within its release document and
     * returns true if {@code ancestorGuid} is the descendant itself or one of its super-types — i.e. the new type
     * is a widening of (or equal to) the old type. GUIDs are stable across releases, so this is release-safe.
     */
    private boolean isAncestorOrSame(Guid ancestorGuid, XbtSummaryRecord descendant,
                                     org.oagi.score.gateway.http.api.cc_management.model.CcDocument ccDocument) {
        XbtSummaryRecord node = descendant;
        int guard = 0;
        while (node != null && guard++ < 64) {
            if (ancestorGuid.equals(node.guid())) {
                return true;
            }
            XbtManifestId parentId = node.subTypeOfXbtId();
            node = (parentId != null) ? safeGet(() -> ccDocument.getXbt(parentId)) : null;
        }
        return false;
    }

    private Set<String> codeListValues(CodeListSummaryRecord codeList) {
        Set<String> values = new HashSet<>();
        if (codeList != null && codeList.valueList() != null) {
            codeList.valueList().forEach(v -> {
                if (v.value() != null) {
                    values.add(v.value());
                }
            });
        }
        return values;
    }

    private Set<String> agencyIdListValues(AgencyIdListSummaryRecord agencyIdList) {
        Set<String> values = new HashSet<>();
        if (agencyIdList != null && agencyIdList.valueList() != null) {
            agencyIdList.valueList().forEach(v -> {
                if (v.value() != null) {
                    values.add(v.value());
                }
            });
        }
        return values;
    }

    private <T> T safeGet(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * JSON renders a repeatable element (max unbounded or > 1) as an array and a non-repeatable one (max 0 or 1)
     * as a bare value, whereas XSD keeps the same element shape (only minOccurs/maxOccurs differ). Crossing that
     * boundary therefore breaks JSON only: a single-object document is invalid against an array schema and vice
     * versa. The array -> scalar direction is also a cardinality tightening already recorded as a
     * syntax-independent break; the scalar -> array direction is a cardinality loosening that breaks JSON alone.
     */
    private void evaluateJsonArrayFlip(BieTrackContext context, int newMax, int oldMax) {
        if (BieBackwardCompatibilityRules.jsonArrayFlip(newMax, oldMax)) {
            context.recordBreak(false, true);
        }
    }

    /*
     * JSON renders a BBIE that has used supplementary components as an object ({"content": ..., "<sc>": ...}) and
     * one without any used SC as a bare scalar, whereas XSD keeps the element shape (gaining an optional attribute
     * is compatible). Crossing that boundary breaks JSON. (Losing all SCs additionally breaks XSD via the removed
     * attributes, which the per-SC removal logic already records; gaining the first SC breaks JSON only.)
     */
    private void evaluateJsonScWrapperFlip(BieTrackContext context, boolean newHasUsedSc, boolean oldHasUsedSc) {
        if (newHasUsedSc != oldHasUsedSc) {
            context.recordBreak(false, true);
        }
    }

    /*
     * Issue #1733: returns true when the new occurrence constraint is strictly more restrictive than the old one,
     * i.e. the minimum was raised or the maximum was lowered. A cardinality_max of -1 means "unbounded".
     */
    private boolean isMoreRestrictiveCardinality(int newMin, int newMax, int oldMin, int oldMax) {
        return BieBackwardCompatibilityRules.cardinalityMoreRestrictive(newMin, newMax, oldMin, oldMax);
    }

}
