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

    private static final String BIE_PACKAGE_MANIFEST_VERSION = "0.2";

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
                            null,
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
                        null,
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

            if (currentBbie.isDeprecated() && !prevBbie.isDeprecated()) {
                context.deprecated.add(newBieElementChange(context, currentBbie, parentPath, Collections.emptyList()));
            }
        }

        List<BbieSc> currentBbieScs = context.currentBieDocument.getBbieScList(currentBbie);
        List<BbieSc> prevBbieScs = context.prevBieDocument.getBbieScList(prevBbie);

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

                    if (currentBbieSc.isDeprecated() && !prevBbieSc.isDeprecated()) {
                        context.deprecated.add(newBieElementChange(context, currentBbieSc, bbiepPath, Collections.emptyList()));
                    }
                } else {
                    context.added.add(newBieElementChange(context, currentBbieSc, bbiepPath, Collections.emptyList()));
                }
            }
        }

        for (BbieSc prevBbieSc : prevBbieScs) {
            String bbiepPath = path(context, parentPath, prevBccp.propertyTerm());

            BbieSc currentBbieSc = currentBbieScs.stream()
                    .filter(e -> matches(prevBbieSc, context.prevBieDocument, e, context.currentBieDocument))
                    .findFirst().orElse(null);
            boolean matched = (currentBbieSc != null);
            if (!matched) context.removed.add(newBieElementChange(context, prevBbieSc, bbiepPath, Collections.emptyList()));
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
        } else if (hasLength(a.getDefaultValue()) && !!Objects.equals(a.getDefaultValue(), b.getDefaultValue())) {
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
        } else if (hasLength(a.getDefaultValue()) && !!Objects.equals(a.getDefaultValue(), b.getDefaultValue())) {
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
                }
                traverse(context, (Asbie) current, (matchedPrev != null) ? ((Asbie) matchedPrev) : null, parentPath);
            } else {
                if (!matched) {
                    context.added.add(newBieElementChange(context, (Bbie) current, parentPath, Collections.emptyList()));
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
                    traverse(context, null, (Asbie) prev, parentPath);
                }
            } else {
                if (!matched) {
                    context.removed.add(newBieElementChange(context, (Bbie) prev, parentPath, Collections.emptyList()));
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

}
