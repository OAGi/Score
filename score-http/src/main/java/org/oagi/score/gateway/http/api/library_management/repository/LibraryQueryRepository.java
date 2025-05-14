package org.oagi.score.gateway.http.api.library_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying library-related information.
 * Provides methods to retrieve read-only status of a library.
 */
public interface LibraryQueryRepository {

    List<LibrarySummaryRecord> getLibrarySummaryList();

    LibrarySummaryRecord getLibrarySummaryByName(String name);

    LibraryDetailsRecord getLibraryDetails(LibraryId libraryId);

    ResultAndCount<LibraryListEntry> getLibraryList(LibraryListFilterCriteria filterCriteria, PageRequest pageRequest);

    /**
     * Determines if a specific library is marked as read-only.
     *
     * @param libraryId the unique identifier of the library.
     * @return {@code true} if the library is read-only, otherwise {@code false}.
     */
    boolean isReadOnly(LibraryId libraryId);

    boolean exists(LibraryId libraryId);

    boolean hasDuplicateName(String name);

    boolean hasDuplicateNameExcludingCurrent(LibraryId libraryId, String name);
}
