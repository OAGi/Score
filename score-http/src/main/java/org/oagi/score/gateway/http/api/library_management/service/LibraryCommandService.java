package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.cc_management.service.CcCommandService;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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

    @Autowired
    private CcCommandService ccCommandService;

    public LibraryId create(ScoreUser requester, CreateLibraryRequest request) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can create the library.");
        }

        if (query(requester).hasDuplicateName(request.name())) {
            throw new IllegalArgumentException("The library meaning '" + request.name() + "' already exists.");
        }

        LibraryId libraryId = command(requester).create(
                request.type(), request.name(), request.organization(), request.description(),
                request.link(), request.domain(), null);

        // @TODO: Create 'Working' Release for a new library.

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
            throw new IllegalArgumentException("The library meaning '" + request.name() + "' already exists.");
        }

        return command(requester).update(
                request.libraryId(),
                request.type(), request.name(), request.organization(), request.description(),
                request.link(), request.domain(), null);
    }

    public boolean discard(ScoreUser requester, LibraryId libraryId) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can update the library.");
        }

        List<ReleaseSummaryRecord> releases = releaseQuery(requester).getReleaseSummaryList(libraryId);
        if (!releases.isEmpty()) {
            // Delete BIEs
            Collection<ReleaseId> releaseIdSet = releases.stream().map(e -> e.releaseId()).collect(Collectors.toSet());

            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
//            List<TopLevelAsbiepId> topLevelAsbiepIdList = topLevelAsbiepQuery.getTopLevelAsbiepSummaryList(releaseIdSet);
//            repositoryFactory.bieCommandRepository(requester).deleteByTopLevelAsbiepIdList(topLevelAsbiepIdList);

            // Delete CCs
            ccCommandService.discardCoreComponents(requester, releaseIdSet);

            // Delete Releases
            releaseCommand(requester).delete(releaseIdSet);
        }

        return command(requester).delete(libraryId);
    }

}
