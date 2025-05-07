package org.oagi.score.gateway.http.api.library_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

public interface LibraryCommandRepository {

    LibraryId create(String type, String name, String organization, String description,
                     String link, String domain, String state);

    boolean update(LibraryId libraryId,
                   String type, String name, String organization, String description,
                   String link, String domain, String state);

    boolean delete(LibraryId libraryId);

}
