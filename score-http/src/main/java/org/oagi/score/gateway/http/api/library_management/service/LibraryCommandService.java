package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.info_management.model.SummaryBie;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.DiscardLibraryCheckResponse;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryReleaseDependenciesRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

@Service
@Transactional
public class LibraryCommandService {

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

        LibraryId libraryId = command(requester).create(
                request.type(), request.name(), request.organization(), request.description(),
                request.link(), request.domain(), null);

        ReleaseId workingReleaseId = releaseCommand(requester).create(libraryId);
        repositoryFactory.ccCommandRepository(requester).createXbtManifestRecords(workingReleaseId);

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
                request.releaseIds().stream().collect(Collectors.toSet());
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

        releaseCommand(requester).deleteDeps(workingRelease.releaseId());
        releaseCommand(requester).createDeps(workingRelease.releaseId(), dependencyReleaseIds);
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
