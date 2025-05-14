package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LibraryQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private LibraryQueryRepository query(ScoreUser requester) {
        return repositoryFactory.libraryQueryRepository(requester);
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

}
