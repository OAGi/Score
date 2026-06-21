package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.library_management.controller.payload.LibraryReleaseDependenciesResponse;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.model.LibraryReleaseDependencyRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDependencySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LibraryQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private LibraryQueryRepository query(ScoreUser requester) {
        return repositoryFactory.libraryQueryRepository(requester);
    }

    private ReleaseQueryRepository releaseQuery(ScoreUser requester) {
        return repositoryFactory.releaseQueryRepository(requester);
    }

    public List<LibrarySummaryRecord> getLibrarySummaryList(ScoreUser requester) {
        return query(requester).getLibrarySummaryList();
    }

    public LibraryDetailsRecord getLibraryDetails(ScoreUser requester, LibraryId libraryId) {
        return query(requester).getLibraryDetails(libraryId);
    }

    public ResultAndCount<LibraryListEntry> getLibraryList(
            ScoreUser requester, LibraryListFilterCriteria filterCriteria, PageRequest pageRequest) {
        return query(requester).getLibraryList(filterCriteria, pageRequest);
    }

    public LibraryReleaseDependenciesResponse getLibraryReleaseDependencies(ScoreUser requester, LibraryId libraryId) {
        Map<LibraryId, String> libraryNameMap = query(requester).getLibrarySummaryList().stream()
                .collect(Collectors.toMap(LibrarySummaryRecord::libraryId, LibrarySummaryRecord::name));

        ReleaseSummaryRecord workingRelease = releaseQuery(requester).getReleaseSummary(libraryId, "Working");
        List<LibraryReleaseDependencyRecord> currentDependencies = (workingRelease != null) ?
                releaseQuery(requester).getReleaseDependencySummaryList(workingRelease.releaseId()).stream()
                        .map(release -> toDependencyRecord(release, libraryNameMap))
                        .collect(Collectors.toList()) :
                List.of();

        List<LibraryReleaseDependencyRecord> availableDependencies = releaseQuery(requester).getReleaseDetailsList().stream()
                .filter(release -> !libraryId.equals(release.libraryId()))
                .filter(release -> release.state() == ReleaseState.Published)
                .map(release -> toDependencyRecord(release, libraryNameMap))
                .sorted(dependencyComparator())
                .collect(Collectors.toList());

        return new LibraryReleaseDependenciesResponse(currentDependencies, availableDependencies);
    }

    private LibraryReleaseDependencyRecord toDependencyRecord(
            ReleaseDetailsRecord release, Map<LibraryId, String> libraryNameMap) {
        return new LibraryReleaseDependencyRecord(
                null,
                release.releaseId(),
                release.libraryId(),
                libraryNameMap.getOrDefault(release.libraryId(), ""),
                release.releaseNum(),
                release.state(),
                release.isWorkingRelease());
    }

    private LibraryReleaseDependencyRecord toDependencyRecord(
            ReleaseSummaryRecord release, Map<LibraryId, String> libraryNameMap) {
        return new LibraryReleaseDependencyRecord(
                null,
                release.releaseId(),
                release.libraryId(),
                libraryNameMap.getOrDefault(release.libraryId(), ""),
                release.releaseNum(),
                release.state(),
                release.isWorkingRelease());
    }

    private LibraryReleaseDependencyRecord toDependencyRecord(
            ReleaseDependencySummaryRecord release, Map<LibraryId, String> libraryNameMap) {
        return new LibraryReleaseDependencyRecord(
                release.releaseDepId(),
                release.releaseId(),
                release.libraryId(),
                libraryNameMap.getOrDefault(release.libraryId(), ""),
                release.releaseNum(),
                release.state(),
                release.isWorkingRelease());
    }

    private Comparator<LibraryReleaseDependencyRecord> dependencyComparator() {
        return Comparator.comparing(LibraryReleaseDependencyRecord::libraryName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LibraryReleaseDependencyRecord::releaseNum, String.CASE_INSENSITIVE_ORDER);
    }

}
