package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.info_management.model.SummaryBie;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.DiscardLibraryCheckResponse;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryReleaseDependenciesRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDependencySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Service
@Transactional
public class LibraryCommandService {

    private static final String DEFAULT_LIBRARY_DEPENDENCY_NAME = "CCTS Data Type Catalogue v3";
    private static final String DEFAULT_LIBRARY_DEPENDENCY_RELEASE_NUM = "3.1";

    @Autowired
    private RepositoryFactory repositoryFactory;

    private LibraryCommandRepository command(ScoreUser requester) {
        return repositoryFactory.libraryCommandRepository(requester);
    }

    private LibraryQueryRepository query(ScoreUser requester) {
        return repositoryFactory.libraryQueryRepository(requester);
    }

    private ReleaseCommandRepository releaseCommand(ScoreUser requester) {
        return repositoryFactory.releaseCommandRepository(requester);
    }

    private ReleaseQueryRepository releaseQuery(ScoreUser requester) {
        return repositoryFactory.releaseQueryRepository(requester);
    }

    public LibraryId create(ScoreUser requester, CreateLibraryRequest request) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can create the library.");
        }

        if (query(requester).hasDuplicateName(request.name())) {
            throw new IllegalArgumentException("The library name '" + request.name() + "' already exists.");
        }
        if (!hasLength(request.namespaceUri())) {
            throw new IllegalArgumentException("'namespaceUri' is required.");
        }

        LibraryId libraryId = command(requester).create(
                request.type(), request.name(), request.organization(), request.description(),
                request.link(), request.domain(), null);

        NamespaceId namespaceId = repositoryFactory.namespaceCommandRepository(requester).create(
                libraryId,
                request.namespaceUri(),
                request.namespacePrefix(),
                null,
                true);

        ReleaseId workingReleaseId = releaseCommand(requester).create(
                libraryId,
                namespaceId,
                "Working",
                null,
                null);
        repositoryFactory.ccCommandRepository(requester).createXbtManifestRecords(workingReleaseId);
        ReleaseId defaultDependencyReleaseId = getDefaultLibraryDependencyReleaseId(requester);
        if (defaultDependencyReleaseId != null) {
            releaseCommand(requester).createDeps(workingReleaseId, List.of(defaultDependencyReleaseId));
        }

        return libraryId;
    }

    public boolean update(ScoreUser requester, UpdateLibraryRequest request) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can update the library.");
        }

        var query = query(requester);

        if (!query.exists(request.libraryId())) {
            throw new IllegalArgumentException("'" + request.libraryId() + "' does not exist.");
        }
        if (query.hasDuplicateNameExcludingCurrent(request.libraryId(), request.name())) {
            throw new IllegalArgumentException("The library name '" + request.name() + "' already exists.");
        }

        return command(requester).update(
                request.libraryId(),
                request.type(), request.name(), request.organization(), request.description(),
                request.link(), request.domain(), request.state(), request.isDefault());
    }

    public DiscardLibraryCheckResponse checkDiscard(ScoreUser requester, LibraryId libraryId) {
        String blockMessage = getDiscardBlockMessage(requester, libraryId);
        return new DiscardLibraryCheckResponse(blockMessage == null, blockMessage == null ? "" : blockMessage);
    }

    public void updateReleaseDependencies(ScoreUser requester,
                                          LibraryId libraryId,
                                          UpdateLibraryReleaseDependenciesRequest request) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can update library release dependencies.");
        }

        if (!query(requester).exists(libraryId)) {
            throw new IllegalArgumentException("Library with ID '" + libraryId + "' does not exist.");
        }

        ReleaseSummaryRecord workingRelease = releaseQuery(requester).getReleaseSummary(libraryId, "Working");
        if (workingRelease == null) {
            throw new IllegalArgumentException("The library does not have an editable release.");
        }

        Set<ReleaseId> dependencyReleaseIds = request.releaseIds() == null ? Set.of() :
                request.releaseIds().stream().collect(Collectors.toCollection(LinkedHashSet::new));
        List<ReleaseSummaryRecord> dependencyReleases = releaseQuery(requester).getReleaseSummaryList(dependencyReleaseIds);
        if (dependencyReleases.size() != dependencyReleaseIds.size()) {
            throw new IllegalArgumentException("One or more selected release dependencies do not exist.");
        }
        if (dependencyReleases.stream().map(ReleaseSummaryRecord::libraryId).distinct().count() != dependencyReleases.size()) {
            throw new IllegalArgumentException("Only one release dependency can be selected from each library.");
        }

        for (ReleaseSummaryRecord dependencyRelease : dependencyReleases) {
            if (libraryId.equals(dependencyRelease.libraryId())) {
                throw new IllegalArgumentException("A library cannot depend on one of its own releases.");
            }
            if (dependencyRelease.state() != ReleaseState.Published) {
                throw new IllegalArgumentException("Only published releases can be assigned as dependencies.");
            }
            boolean introducesCycle = releaseQuery(requester).getIncludedReleaseSummaryList(dependencyRelease.releaseId())
                    .stream()
                    .anyMatch(release -> release.releaseId().equals(workingRelease.releaseId()));
            if (introducesCycle) {
                throw new IllegalArgumentException("The selected dependencies would create a circular release dependency.");
            }
        }

        Map<LibraryId, String> libraryNameMap = query(requester).getLibrarySummaryList().stream()
                .collect(Collectors.toMap(LibrarySummaryRecord::libraryId, LibrarySummaryRecord::name));
        List<ReleaseDependencySummaryRecord> currentDependencies =
                releaseQuery(requester).getReleaseDependencySummaryList(workingRelease.releaseId());
        validateAndRemapDependencyChanges(
                requester, workingRelease, currentDependencies, dependencyReleases, libraryNameMap);

        Set<ReleaseId> currentDependencyReleaseIds = currentDependencies.stream()
                .map(ReleaseDependencySummaryRecord::releaseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<ReleaseId> dependencyReleaseIdsToCreate = dependencyReleaseIds.stream()
                .filter(releaseId -> !currentDependencyReleaseIds.contains(releaseId))
                .collect(Collectors.toList());
        Set<ReleaseId> dependencyReleaseIdsToDelete = currentDependencyReleaseIds.stream()
                .filter(releaseId -> !dependencyReleaseIds.contains(releaseId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        releaseCommand(requester).deleteDeps(workingRelease.releaseId(), dependencyReleaseIdsToDelete);
        releaseCommand(requester).createDeps(workingRelease.releaseId(), dependencyReleaseIdsToCreate);
    }

    private void validateAndRemapDependencyChanges(
            ScoreUser requester,
            ReleaseSummaryRecord workingRelease,
            List<ReleaseDependencySummaryRecord> currentDependencies,
            List<ReleaseSummaryRecord> selectedDependencies,
            Map<LibraryId, String> libraryNameMap) {

        Map<LibraryId, ReleaseDependencySummaryRecord> currentByLibrary = currentDependencies.stream()
                .collect(Collectors.toMap(ReleaseDependencySummaryRecord::libraryId, dependency -> dependency,
                        (left, right) -> left, LinkedHashMap::new));
        Map<LibraryId, ReleaseSummaryRecord> selectedByLibrary = selectedDependencies.stream()
                .collect(Collectors.toMap(ReleaseSummaryRecord::libraryId, dependency -> dependency,
                        (left, right) -> left, LinkedHashMap::new));

        var ccCommand = repositoryFactory.ccCommandRepository(requester);
        Map<ReleaseId, ReleaseDetailsRecord> releaseDetailsCache = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (Map.Entry<LibraryId, ReleaseDependencySummaryRecord> entry : currentByLibrary.entrySet()) {
            LibraryId dependencyLibraryId = entry.getKey();
            ReleaseDependencySummaryRecord currentDependency = entry.getValue();
            ReleaseSummaryRecord selectedDependency = selectedByLibrary.get(dependencyLibraryId);

            if (selectedDependency == null) {
                Map<String, Integer> referenceCounts =
                        ccCommand.getCrossReleaseReferenceCounts(workingRelease.releaseId(), currentDependency.releaseId());
                if (sumCounts(referenceCounts) > 0) {
                    Map<String, List<String>> referenceDetails =
                            ccCommand.getCrossReleaseReferenceDetails(workingRelease.releaseId(), currentDependency.releaseId());
                    errors.add(buildRemovalErrorMessage(currentDependency, libraryNameMap, referenceCounts, referenceDetails));
                }
                continue;
            }

            if (currentDependency.releaseId().equals(selectedDependency.releaseId())) {
                continue;
            }

            Map<String, Integer> referenceCounts =
                    ccCommand.getCrossReleaseReferenceCounts(workingRelease.releaseId(), currentDependency.releaseId());
            if (sumCounts(referenceCounts) == 0) {
                continue;
            }
            Map<String, List<String>> referenceDetails =
                    ccCommand.getCrossReleaseReferenceDetails(workingRelease.releaseId(), currentDependency.releaseId());

            if (currentDependency.isWorkingRelease() || selectedDependency.isWorkingRelease()) {
                errors.add(buildWorkingDependencyErrorMessage(
                        currentDependency, selectedDependency, libraryNameMap, referenceCounts, referenceDetails));
                continue;
            }

            ReleaseChangeRelation releaseChangeRelation = classifyReleaseChangeRelation(
                    requester, currentDependency.releaseId(), selectedDependency.releaseId(), releaseDetailsCache);
            if (releaseChangeRelation == ReleaseChangeRelation.UNRELATED) {
                errors.add(buildUnknownReleaseChainErrorMessage(
                        currentDependency, selectedDependency, libraryNameMap, referenceCounts, referenceDetails));
                continue;
            }

            if (releaseChangeRelation == ReleaseChangeRelation.DOWNGRADE) {
                errors.add(buildDowngradeErrorMessage(
                        currentDependency, selectedDependency, libraryNameMap, referenceCounts, referenceDetails));
                continue;
            }

            ccCommand.remapCrossReleaseReferences(
                    workingRelease.releaseId(), currentDependency.releaseId(), selectedDependency.releaseId());

            Map<String, Integer> remainingReferenceCounts =
                    ccCommand.getCrossReleaseReferenceCounts(workingRelease.releaseId(), currentDependency.releaseId());
            if (sumCounts(remainingReferenceCounts) > 0) {
                Map<String, List<String>> remainingReferenceDetails =
                        ccCommand.getCrossReleaseReferenceDetails(workingRelease.releaseId(), currentDependency.releaseId());
                errors.add(buildUpgradeGapErrorMessage(
                        currentDependency, selectedDependency, libraryNameMap,
                        referenceCounts, remainingReferenceCounts, remainingReferenceDetails));
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Release dependency update is blocked.\n\n"
                    + String.join("\n\n", errors));
        }
    }

    private ReleaseChangeRelation classifyReleaseChangeRelation(
            ScoreUser requester,
            ReleaseId currentReleaseId,
            ReleaseId selectedReleaseId,
            Map<ReleaseId, ReleaseDetailsRecord> releaseDetailsCache) {
        if (currentReleaseId.equals(selectedReleaseId)) {
            return ReleaseChangeRelation.UNCHANGED;
        }
        if (isReachableByNextRelease(requester, currentReleaseId, selectedReleaseId, releaseDetailsCache)) {
            return ReleaseChangeRelation.UPGRADE;
        }
        if (isReachableByPrevRelease(requester, currentReleaseId, selectedReleaseId, releaseDetailsCache)) {
            return ReleaseChangeRelation.DOWNGRADE;
        }
        return ReleaseChangeRelation.UNRELATED;
    }

    private boolean isReachableByNextRelease(
            ScoreUser requester,
            ReleaseId startReleaseId,
            ReleaseId targetReleaseId,
            Map<ReleaseId, ReleaseDetailsRecord> releaseDetailsCache) {
        return isReachableInReleaseChain(
                requester, startReleaseId, targetReleaseId, releaseDetailsCache, true);
    }

    private boolean isReachableByPrevRelease(
            ScoreUser requester,
            ReleaseId startReleaseId,
            ReleaseId targetReleaseId,
            Map<ReleaseId, ReleaseDetailsRecord> releaseDetailsCache) {
        return isReachableInReleaseChain(
                requester, startReleaseId, targetReleaseId, releaseDetailsCache, false);
    }

    private boolean isReachableInReleaseChain(
            ScoreUser requester,
            ReleaseId startReleaseId,
            ReleaseId targetReleaseId,
            Map<ReleaseId, ReleaseDetailsRecord> releaseDetailsCache,
            boolean followNextRelease) {
        Set<ReleaseId> visitedReleaseIds = new HashSet<>();
        ReleaseId currentReleaseId = startReleaseId;
        while (currentReleaseId != null && visitedReleaseIds.add(currentReleaseId)) {
            ReleaseDetailsRecord releaseDetails = releaseDetailsCache.computeIfAbsent(
                    currentReleaseId, releaseId -> releaseQuery(requester).getReleaseDetails(releaseId));
            ReleaseSummaryRecord adjacentRelease = followNextRelease ? releaseDetails.next() : releaseDetails.prev();
            if (adjacentRelease == null) {
                return false;
            }
            if (targetReleaseId.equals(adjacentRelease.releaseId())) {
                return true;
            }
            currentReleaseId = adjacentRelease.releaseId();
        }
        return false;
    }

    private int sumCounts(Map<String, Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private String buildRemovalErrorMessage(
            ReleaseDependencySummaryRecord currentDependency,
            Map<LibraryId, String> libraryNameMap,
            Map<String, Integer> referenceCounts,
            Map<String, List<String>> referenceDetails) {
        return "Removing dependency " + dependencyLabel(currentDependency, libraryNameMap)
                + " would leave Working release references in place.\n"
                + formatReferenceBreakdown(referenceCounts, referenceDetails);
    }

    private String buildWorkingDependencyErrorMessage(
            ReleaseDependencySummaryRecord currentDependency,
            ReleaseSummaryRecord selectedDependency,
            Map<LibraryId, String> libraryNameMap,
            Map<String, Integer> referenceCounts,
            Map<String, List<String>> referenceDetails) {
        return "Changing dependency " + dependencyLabel(currentDependency, libraryNameMap)
                + " to " + dependencyLabel(selectedDependency, libraryNameMap)
                + " is blocked because Working dependencies are not remapped automatically.\n"
                + formatReferenceBreakdown(referenceCounts, referenceDetails);
    }

    private String buildUnknownReleaseChainErrorMessage(
            ReleaseDependencySummaryRecord currentDependency,
            ReleaseSummaryRecord selectedDependency,
            Map<LibraryId, String> libraryNameMap,
            Map<String, Integer> referenceCounts,
            Map<String, List<String>> referenceDetails) {
        return "Changing dependency " + dependencyLabel(currentDependency, libraryNameMap)
                + " to " + dependencyLabel(selectedDependency, libraryNameMap)
                + " is blocked because the selected release is not on the same upgrade path.\n"
                + formatReferenceBreakdown(referenceCounts, referenceDetails);
    }

    private String buildDowngradeErrorMessage(
            ReleaseDependencySummaryRecord currentDependency,
            ReleaseSummaryRecord selectedDependency,
            Map<LibraryId, String> libraryNameMap,
            Map<String, Integer> referenceCounts,
            Map<String, List<String>> referenceDetails) {
        return "Downgrading dependency from " + dependencyLabel(currentDependency, libraryNameMap)
                + " to " + dependencyLabel(selectedDependency, libraryNameMap)
                + " is blocked while Working release references still exist.\n"
                + formatReferenceBreakdown(referenceCounts, referenceDetails);
    }

    private String buildUpgradeGapErrorMessage(
            ReleaseDependencySummaryRecord currentDependency,
            ReleaseSummaryRecord selectedDependency,
            Map<LibraryId, String> libraryNameMap,
            Map<String, Integer> originalReferenceCounts,
            Map<String, Integer> remainingReferenceCounts,
            Map<String, List<String>> remainingReferenceDetails) {
        int originalCount = sumCounts(originalReferenceCounts);
        int remainingCount = sumCounts(remainingReferenceCounts);
        int remappedCount = originalCount - remainingCount;
        return "Upgrading dependency from " + dependencyLabel(currentDependency, libraryNameMap)
                + " to " + dependencyLabel(selectedDependency, libraryNameMap)
                + " could not remap every Working release reference.\n"
                + "Remapped references: " + remappedCount + "\n"
                + "Unresolved references: " + remainingCount + "\n"
                + formatReferenceBreakdown(remainingReferenceCounts, remainingReferenceDetails);
    }

    private ReleaseId getDefaultLibraryDependencyReleaseId(ScoreUser requester) {
        LibrarySummaryRecord cctsLibrary = query(requester).getLibrarySummaryByName(DEFAULT_LIBRARY_DEPENDENCY_NAME);
        if (cctsLibrary == null) {
            return null;
        }
        ReleaseSummaryRecord cctsRelease = releaseQuery(requester)
                .getReleaseSummary(cctsLibrary.libraryId(), DEFAULT_LIBRARY_DEPENDENCY_RELEASE_NUM);
        if (cctsRelease == null) {
            return null;
        }
        return cctsRelease.releaseId();
    }

    private String dependencyLabel(ReleaseSummaryRecord dependency, Map<LibraryId, String> libraryNameMap) {
        return libraryNameMap.getOrDefault(dependency.libraryId(), "Library") + " " + dependency.releaseNum();
    }

    private String dependencyLabel(ReleaseDependencySummaryRecord dependency, Map<LibraryId, String> libraryNameMap) {
        return libraryNameMap.getOrDefault(dependency.libraryId(), "Library") + " " + dependency.releaseNum();
    }

    private String formatReferenceBreakdown(
            Map<String, Integer> referenceCounts,
            Map<String, List<String>> referenceDetails) {
        return referenceCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> formatReferenceBreakdownLine(entry.getKey(), entry.getValue(), referenceDetails))
                .collect(Collectors.joining("\n"));
    }

    private String formatReferenceBreakdownLine(
            String referenceType,
            Integer count,
            Map<String, List<String>> referenceDetails) {
        String line = "- " + referenceType + ": " + count;
        List<String> details = referenceDetails.get(referenceType);
        if (details == null || details.isEmpty()) {
            return line;
        }
        if (details.size() <= 2) {
            return line + " (DENs: " + String.join(", ", details) + ")";
        }
        return line + " (DENs: " + String.join(", ", details.subList(0, 2))
                + ", and " + (details.size() - 2) + " more)";
    }

    private enum ReleaseChangeRelation {
        UNCHANGED,
        UPGRADE,
        DOWNGRADE,
        UNRELATED
    }

    public boolean discard(ScoreUser requester, LibraryId libraryId) {
        String blockMessage = getDiscardBlockMessage(requester, libraryId);
        if (blockMessage != null) {
            throw new IllegalArgumentException(blockMessage);
        }

        List<ReleaseSummaryRecord> releases = releaseQuery(requester).getReleaseSummaryList(libraryId);
        ReleaseSummaryRecord workingRelease = releases.stream()
                .filter(ReleaseSummaryRecord::isWorkingRelease)
                .findFirst()
                .orElse(null);
        if (workingRelease != null) {
            discardWorkingRelease(requester, libraryId, workingRelease.releaseId());
        }

        var moduleSetQuery = repositoryFactory.moduleSetQueryRepository(requester);
        var moduleSetCommand = repositoryFactory.moduleSetCommandRepository(requester);
        moduleSetQuery.getModuleSetSummaryList(libraryId).forEach(
                moduleSet -> moduleSetCommand.delete(moduleSet.moduleSetId()));

        return command(requester).delete(libraryId);
    }

    private String getDiscardBlockMessage(ScoreUser requester, LibraryId libraryId) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            return "Only administrators can discard the library.";
        }

        if (!query(requester).exists(libraryId)) {
            return "Library with ID '" + libraryId + "' does not exist.";
        }

        List<ReleaseSummaryRecord> releases = releaseQuery(requester).getReleaseSummaryList(libraryId);
        List<ReleaseSummaryRecord> publishedNonWorkingReleases = releases.stream()
                .filter(release -> !release.isWorkingRelease())
                .filter(release -> release.state() == ReleaseState.Published)
                .collect(Collectors.toList());
        if (!publishedNonWorkingReleases.isEmpty()) {
            return "This library cannot be discarded because it has published releases. Please contact an administrator.";
        }

        List<ReleaseSummaryRecord> nonWorkingReleases = releases.stream()
                .filter(release -> !release.isWorkingRelease())
                .collect(Collectors.toList());
        if (!nonWorkingReleases.isEmpty()) {
            return "This library cannot be discarded because it has releases other than the current editable branch. Remove those releases first.";
        }

        ReleaseSummaryRecord workingRelease = releases.stream()
                .filter(ReleaseSummaryRecord::isWorkingRelease)
                .findFirst()
                .orElse(null);
        if (workingRelease == null) {
            return null;
        }

        List<ReleaseSummaryRecord> dependingReleases = releaseQuery(requester)
                .getReleaseSummaryListDependingOn(workingRelease.releaseId()).stream()
                .filter(release -> !libraryId.equals(release.libraryId()))
                .collect(Collectors.toList());
        if (!dependingReleases.isEmpty()) {
            return "This library cannot be discarded because other releases depend on it. Unlink the release dependencies first.";
        }

        return null;
    }

    private void discardWorkingRelease(ScoreUser requester, LibraryId libraryId, ReleaseId workingReleaseId) {
        var moduleSetReleaseQuery = repositoryFactory.moduleSetReleaseQueryRepository(requester);
        var moduleSetReleaseCommand = repositoryFactory.moduleSetReleaseCommandRepository(requester);
        moduleSetReleaseQuery.getModuleSetReleaseSummaryList(workingReleaseId).forEach(
                moduleSetRelease -> moduleSetReleaseCommand.delete(moduleSetRelease.moduleSetReleaseId()));

        List<SummaryBie> summaryBies = repositoryFactory.bieQueryRepository(requester)
                .getSummaryBieList(libraryId, workingReleaseId, false, List.of());
        if (!summaryBies.isEmpty()) {
            repositoryFactory.bieCommandRepository(requester).deleteByTopLevelAsbiepIdList(summaryBies.stream()
                    .map(SummaryBie::getTopLevelAsbiepId)
                    .collect(Collectors.toList()));
        }

        var ccCommand = repositoryFactory.ccCommandRepository(requester);
        ccCommand.clearReplacement(workingReleaseId);
        ccCommand.cleanUp(workingReleaseId);
        ccCommand.delete(workingReleaseId);

        releaseCommand(requester).deleteDeps(workingReleaseId);
        releaseCommand(requester).delete(workingReleaseId);
    }

}
