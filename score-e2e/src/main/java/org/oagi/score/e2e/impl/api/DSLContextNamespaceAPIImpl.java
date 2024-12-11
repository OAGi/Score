package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.NamespaceAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.NamespaceRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.NAMESPACE;

public class DSLContextNamespaceAPIImpl implements NamespaceAPI {

    private final DSLContext dslContext;

    public DSLContextNamespaceAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public NamespaceObject getNamespaceByURI(LibraryObject library, String uri) {
        NamespaceRecord namespaceRecord = dslContext.selectFrom(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        NAMESPACE.URI.eq(uri)
                ))
                .fetchOne();
        return mapper(namespaceRecord);
    }

    private NamespaceObject mapper(NamespaceRecord namespaceRecord) {
        NamespaceObject namespace = new NamespaceObject();
        namespace.setNamespaceId(namespaceRecord.getNamespaceId().toBigInteger());
        namespace.setLibraryId(namespaceRecord.getLibraryId().toBigInteger());
        namespace.setUri(namespaceRecord.getUri());
        namespace.setPrefix(namespaceRecord.getPrefix());
        namespace.setDescription(namespaceRecord.getDescription());
        namespace.setStandardNamespace(namespaceRecord.getIsStdNmsp() == 1);
        namespace.setOwnerUserId(namespaceRecord.getOwnerUserId().toBigInteger());
        namespace.setCreatedBy(namespaceRecord.getCreatedBy().toBigInteger());
        namespace.setLastUpdatedBy(namespaceRecord.getLastUpdatedBy().toBigInteger());
        namespace.setCreationTimestamp(namespaceRecord.getCreationTimestamp());
        namespace.setLastUpdateTimestamp(namespaceRecord.getLastUpdateTimestamp());
        return namespace;
    }

    @Override
    public NamespaceObject createRandomEndUserNamespace(AppUserObject creator, LibraryObject library) {
        if (creator.isDeveloper()) {
            throw new IllegalArgumentException("Developer cannot create an end-user namespace.");
        }
        NamespaceObject namespace = NamespaceObject.createRandomNamespace(creator);

        NamespaceRecord namespaceRecord = new NamespaceRecord();
        namespaceRecord.setLibraryId(ULong.valueOf(library.getLibraryId()));
        namespaceRecord.setUri(namespace.getUri());
        namespaceRecord.setPrefix(namespace.getPrefix());
        namespaceRecord.setDescription(namespace.getDescription());
        namespaceRecord.setIsStdNmsp((byte) (namespace.isStandardNamespace() ? 1 : 0));
        namespaceRecord.setOwnerUserId(ULong.valueOf(namespace.getOwnerUserId()));
        namespaceRecord.setCreatedBy(ULong.valueOf(namespace.getCreatedBy()));
        namespaceRecord.setLastUpdatedBy(ULong.valueOf(namespace.getLastUpdatedBy()));
        namespaceRecord.setCreationTimestamp(namespace.getCreationTimestamp());
        namespaceRecord.setLastUpdateTimestamp(namespace.getLastUpdateTimestamp());

        ULong namespaceId = dslContext.insertInto(NAMESPACE)
                .set(namespaceRecord)
                .returning(NAMESPACE.NAMESPACE_ID)
                .fetchOne().getNamespaceId();
        namespace.setNamespaceId(namespaceId.toBigInteger());
        return namespace;
    }

    @Override
    public NamespaceObject createRandomDeveloperNamespace(AppUserObject creator, LibraryObject library) {
        if (!creator.isDeveloper()) {
            throw new IllegalArgumentException("End-user cannot create a developer namespace.");
        }
        NamespaceObject namespace = NamespaceObject.createRandomNamespace(creator);

        NamespaceRecord namespaceRecord = new NamespaceRecord();
        namespaceRecord.setLibraryId(ULong.valueOf(library.getLibraryId()));
        namespaceRecord.setUri(namespace.getUri());
        namespaceRecord.setPrefix(namespace.getPrefix());
        namespaceRecord.setDescription(namespace.getDescription());
        namespaceRecord.setIsStdNmsp((byte) (namespace.isStandardNamespace() ? 1 : 0));
        namespaceRecord.setOwnerUserId(ULong.valueOf(namespace.getOwnerUserId()));
        namespaceRecord.setCreatedBy(ULong.valueOf(namespace.getCreatedBy()));
        namespaceRecord.setLastUpdatedBy(ULong.valueOf(namespace.getLastUpdatedBy()));
        namespaceRecord.setCreationTimestamp(namespace.getCreationTimestamp());
        namespaceRecord.setLastUpdateTimestamp(namespace.getLastUpdateTimestamp());

        ULong namespaceId = dslContext.insertInto(NAMESPACE)
                .set(namespaceRecord)
                .returning(NAMESPACE.NAMESPACE_ID)
                .fetchOne().getNamespaceId();
        namespace.setNamespaceId(namespaceId.toBigInteger());
        return namespace;
    }

    @Override
    public List<NamespaceObject> getStandardNamespacesURIs(LibraryObject library) {
        List<NamespaceRecord> standardNamespaces = dslContext.selectFrom(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        NAMESPACE.IS_STD_NMSP.eq((byte) 1)
                ))
                .fetchInto(NamespaceRecord.class);
        List<NamespaceObject> namespaceObjectsList = new ArrayList<>();
        for (NamespaceRecord record : standardNamespaces) {
            NamespaceObject namespace = mapper(record);
            namespaceObjectsList.add(namespace);
        }
        return namespaceObjectsList;
    }

    @Override
    public NamespaceObject getNamespaceById(BigInteger namespaceId) {
        NamespaceRecord namespaceRecord = dslContext.selectFrom(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(ULong.valueOf(namespaceId)))
                .fetchOne();
        return mapper(namespaceRecord);
    }

    @Override
    public List<NamespaceObject> getNonStandardNamespacesURIs(LibraryObject library) {
        List<NamespaceRecord> standardNamespaces = dslContext.selectFrom(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        NAMESPACE.IS_STD_NMSP.eq((byte) 0)
                ))
                .fetchInto(NamespaceRecord.class);
        List<NamespaceObject> namespaceObjectsList = new ArrayList<>();
        for (NamespaceRecord record : standardNamespaces) {
            NamespaceObject namespace = mapper(record);
            namespaceObjectsList.add(namespace);
        }
        return namespaceObjectsList;
    }
}
