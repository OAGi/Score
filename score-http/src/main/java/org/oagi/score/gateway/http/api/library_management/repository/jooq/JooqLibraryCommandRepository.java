package org.oagi.score.gateway.http.api.library_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.LibraryRecord;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LIBRARY;

public class JooqLibraryCommandRepository extends JooqBaseRepository implements LibraryCommandRepository {

    public JooqLibraryCommandRepository(DSLContext dslContext, ScoreUser requester,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public LibraryId create(String type, String name, String organization, String description,
                            String link, String domain, String state) {

        LocalDateTime timestamp = LocalDateTime.now();

        LibraryRecord libraryRecord = new LibraryRecord();
        libraryRecord.setType(type);
        libraryRecord.setName(name);
        libraryRecord.setOrganization(organization);
        libraryRecord.setLink(link);
        libraryRecord.setDomain(domain);
        libraryRecord.setDescription(description);
        libraryRecord.setState(state);
        libraryRecord.setCreatedBy(valueOf(requester().userId()));
        libraryRecord.setLastUpdatedBy(valueOf(requester().userId()));
        libraryRecord.setCreationTimestamp(timestamp);
        libraryRecord.setLastUpdateTimestamp(timestamp);

        return new LibraryId(
                dslContext().insertInto(LIBRARY)
                        .set(libraryRecord)
                        .returning(LIBRARY.LIBRARY_ID)
                        .fetchOne().getLibraryId().toBigInteger()
        );
    }

    @Override
    public boolean update(LibraryId libraryId,
                          String type, String name, String organization, String description,
                          String link, String domain, String state) {

        LocalDateTime timestamp = LocalDateTime.now();

        int res = dslContext().update(LIBRARY)
                .set(LIBRARY.TYPE, type)
                .set(LIBRARY.NAME, name)
                .set(LIBRARY.ORGANIZATION, organization)
                .set(LIBRARY.LINK, link)
                .set(LIBRARY.DOMAIN, domain)
                .set(LIBRARY.DESCRIPTION, description)
                .set(LIBRARY.STATE, state)
                .set(LIBRARY.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(LIBRARY.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .execute();
        return res == 1;
    }

    @Override
    public boolean delete(LibraryId libraryId) {

        int res = dslContext().deleteFrom(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .execute();
        return res == 1;
    }
}
