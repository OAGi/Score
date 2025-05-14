package org.oagi.score.gateway.http.api.namespace_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.NamespaceRecord;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.NAMESPACE;

public class JooqNamespaceCommandRepository extends JooqBaseRepository implements NamespaceCommandRepository {

    public JooqNamespaceCommandRepository(DSLContext dslContext, ScoreUser requester,
                                          RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public NamespaceId create(LibraryId libraryId, String uri, String prefix, String description, boolean standard) {

        LocalDateTime timestamp = LocalDateTime.now();
        NamespaceRecord namespaceRecord = new NamespaceRecord();
        namespaceRecord.setLibraryId(valueOf(libraryId));
        namespaceRecord.setUri(uri);
        namespaceRecord.setPrefix(prefix);
        namespaceRecord.setDescription(description);
        namespaceRecord.setIsStdNmsp((byte) (standard ? 1 : 0));
        namespaceRecord.setOwnerUserId(valueOf(requester().userId()));
        namespaceRecord.setCreatedBy(valueOf(requester().userId()));
        namespaceRecord.setLastUpdatedBy(valueOf(requester().userId()));
        namespaceRecord.setCreationTimestamp(timestamp);
        namespaceRecord.setLastUpdateTimestamp(timestamp);

        return new NamespaceId(
                dslContext().insertInto(NAMESPACE)
                        .set(namespaceRecord)
                        .returning(NAMESPACE.NAMESPACE_ID)
                        .fetchOne().getNamespaceId().toBigInteger()
        );
    }

    @Override
    public boolean update(NamespaceId namespaceId, String uri, String prefix, String description) {
        LocalDateTime timestamp = LocalDateTime.now();

        int res = dslContext().update(NAMESPACE)
                .set(NAMESPACE.URI, uri)
                .set(NAMESPACE.PREFIX, prefix)
                .set(NAMESPACE.DESCRIPTION, description)
                .set(NAMESPACE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(NAMESPACE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        NAMESPACE.OWNER_USER_ID.eq(valueOf(requester().userId())),
                        NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId))
                ))
                .execute();
        return res == 1;
    }

    @Override
    public boolean delete(NamespaceId namespaceId) {

        int res = dslContext().deleteFrom(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .execute();
        return res == 1;
    }

    @Override
    public boolean updateOwnership(UserId newOwnerUserId, NamespaceId namespaceId) {
        LocalDateTime timestamp = LocalDateTime.now();

        int num = dslContext().update(NAMESPACE)
                .set(NAMESPACE.OWNER_USER_ID, valueOf(newOwnerUserId))
                .set(NAMESPACE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(NAMESPACE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .execute();

        return num == 1;
    }

}
