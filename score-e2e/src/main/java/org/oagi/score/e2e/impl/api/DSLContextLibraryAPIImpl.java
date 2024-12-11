package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.LibraryAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.LibraryRecord;
import org.oagi.score.e2e.obj.LibraryObject;

import java.math.BigInteger;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.LIBRARY;

public class DSLContextLibraryAPIImpl implements LibraryAPI {

    private final DSLContext dslContext;

    public DSLContextLibraryAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public LibraryObject getLibraryById(BigInteger libraryId) {
        LibraryRecord libraryRecord = dslContext.selectFrom(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId)))
                .fetchOptional().orElse(null);
        return mapper(libraryRecord);
    }

    @Override
    public LibraryObject getLibraryByName(String name) {
        LibraryRecord libraryRecord = dslContext.selectFrom(LIBRARY)
                .where(LIBRARY.NAME.eq(name))
                .fetchOptional().orElse(null);
        return mapper(libraryRecord);
    }

    private LibraryObject mapper(LibraryRecord record) {
        if (record == null) {
            return null;
        }

        LibraryObject library = new LibraryObject();
        library.setLibraryId(record.getLibraryId().toBigInteger());
        library.setLibraryName(record.getName());
        return library;
    }

}
