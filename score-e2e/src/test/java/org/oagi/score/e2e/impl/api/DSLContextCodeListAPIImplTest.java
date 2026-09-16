package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.jooq.exception.TooManyRowsException;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.CodeListObject;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CODE_LIST;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.CODE_LIST_MANIFEST;

class DSLContextCodeListAPIImplTest {

    @Test
    void returnsAllMatchingManifestsAndSingularLookupRejectsDuplicates() {
        DSLContext records = DSL.using(SQLDialect.MARIADB);
        LocalDateTime createdAt = LocalDateTime.of(2020, 1, 1, 0, 0);
        MockConnection connection = new MockConnection(context -> {
            String sql = context.sql().replace("`oagi`.", "");
            if (sql.contains("from `code_list_manifest`") && sql.contains("join `code_list`")) {
                assertEquals(List.of("duplicate-name", "10.8.7.1"), Arrays.asList(context.bindings()));
                Field<?>[] fields = Stream.concat(Arrays.stream(CODE_LIST_MANIFEST.fields()),
                                Arrays.stream(CODE_LIST.fields()))
                        .toArray(Field<?>[]::new);
                var result = records.newResult(fields);
                for (long manifestId : new long[]{101, 102}) {
                    long codeListId = manifestId + 1000;
                    Record record = records.newRecord(fields);
                    record.set(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, ULong.valueOf(manifestId));
                    record.set(CODE_LIST_MANIFEST.RELEASE_ID, ULong.valueOf(7));
                    record.set(CODE_LIST_MANIFEST.CODE_LIST_ID, ULong.valueOf(codeListId));
                    record.set(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                            manifestId == 101 ? ULong.valueOf(301) : null);
                    record.set(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            manifestId == 101 ? ULong.valueOf(201) : null);
                    record.set(CODE_LIST.CODE_LIST_ID, ULong.valueOf(codeListId));
                    record.set(CODE_LIST.GUID, "guid-" + manifestId);
                    record.set(CODE_LIST.ENUM_TYPE_GUID, "enum-guid-" + manifestId);
                    record.set(CODE_LIST.NAME, "duplicate-name");
                    record.set(CODE_LIST.LIST_ID, "list-id");
                    record.set(CODE_LIST.VERSION_ID, "version-id");
                    record.set(CODE_LIST.DEFINITION, "definition-" + manifestId);
                    record.set(CODE_LIST.DEFINITION_SOURCE, "source-" + manifestId);
                    record.set(CODE_LIST.REMARK, "remark-" + manifestId);
                    record.set(CODE_LIST.NAMESPACE_ID, manifestId == 101 ? ULong.valueOf(11) : null);
                    record.set(CODE_LIST.EXTENSIBLE_INDICATOR, manifestId == 101 ? (byte) 1 : (byte) 0);
                    record.set(CODE_LIST.IS_DEPRECATED, manifestId == 101 ? (byte) 0 : null);
                    record.set(CODE_LIST.STATE, manifestId == 101 ? "Published" : "WIP");
                    record.set(CODE_LIST.OWNER_USER_ID, ULong.valueOf(manifestId + 20));
                    record.set(CODE_LIST.CREATED_BY, ULong.valueOf(manifestId + 21));
                    record.set(CODE_LIST.LAST_UPDATED_BY, ULong.valueOf(manifestId + 22));
                    record.set(CODE_LIST.CREATION_TIMESTAMP, createdAt.plusDays(manifestId));
                    record.set(CODE_LIST.LAST_UPDATE_TIMESTAMP, createdAt.plusDays(manifestId + 1));
                    result.add(record);
                }
                return new MockResult[]{new MockResult(2, result)};
            }
            throw new AssertionError("Unexpected SQL: " + sql + " bindings=" + Arrays.toString(context.bindings()));
        });

        var api = new DSLContextCodeListAPIImpl(DSL.using(connection, SQLDialect.MARIADB), null);

        List<CodeListObject> codeLists = api.getCodeListsByCodeListNameAndReleaseNum("duplicate-name", "10.8.7.1");

        assertEquals(List.of(BigInteger.valueOf(101), BigInteger.valueOf(102)),
                codeLists.stream().map(CodeListObject::getCodeListManifestId).toList());
        assertEquals(List.of(BigInteger.valueOf(1101), BigInteger.valueOf(1102)),
                codeLists.stream().map(CodeListObject::getCodeListId).toList());
        assertEquals(List.of("guid-101", "guid-102"), codeLists.stream().map(CodeListObject::getGuid).toList());
        assertEquals(List.of("enum-guid-101", "enum-guid-102"),
                codeLists.stream().map(CodeListObject::getEnumTypeGuid).toList());
        assertEquals(Arrays.asList(BigInteger.valueOf(301), null),
                codeLists.stream().map(CodeListObject::getBasedCodeListManifestId).toList());
        assertEquals(Arrays.asList(BigInteger.valueOf(201), null),
                codeLists.stream().map(CodeListObject::getAgencyIdListValueManifestId).toList());
        assertEquals(List.of("duplicate-name", "duplicate-name"),
                codeLists.stream().map(CodeListObject::getName).toList());
        assertEquals(List.of("list-id", "list-id"), codeLists.stream().map(CodeListObject::getListId).toList());
        assertEquals(List.of("version-id", "version-id"),
                codeLists.stream().map(CodeListObject::getVersionId).toList());
        assertEquals(List.of("definition-101", "definition-102"),
                codeLists.stream().map(CodeListObject::getDefinition).toList());
        assertEquals(List.of("source-101", "source-102"),
                codeLists.stream().map(CodeListObject::getDefinitionSource).toList());
        assertEquals(List.of("remark-101", "remark-102"),
                codeLists.stream().map(CodeListObject::getRemark).toList());
        assertEquals(Arrays.asList(BigInteger.valueOf(11), null),
                codeLists.stream().map(CodeListObject::getNamespaceId).toList());
        assertEquals(List.of(true, false), codeLists.stream().map(CodeListObject::isExtensibleIndicator).toList());
        assertEquals(List.of("Published", "WIP"), codeLists.stream().map(CodeListObject::getState).toList());
        assertEquals(List.of(false, false), codeLists.stream().map(CodeListObject::isDeprecated).toList());
        assertEquals(List.of(BigInteger.valueOf(7), BigInteger.valueOf(7)),
                codeLists.stream().map(CodeListObject::getReleaseId).toList());
        assertEquals(List.of(BigInteger.valueOf(121), BigInteger.valueOf(122)),
                codeLists.stream().map(CodeListObject::getOwnerUserId).toList());
        assertEquals(List.of(BigInteger.valueOf(122), BigInteger.valueOf(123)),
                codeLists.stream().map(CodeListObject::getCreatedBy).toList());
        assertEquals(List.of(BigInteger.valueOf(123), BigInteger.valueOf(124)),
                codeLists.stream().map(CodeListObject::getLastUpdatedBy).toList());
        assertEquals(List.of(createdAt.plusDays(101), createdAt.plusDays(102)),
                codeLists.stream().map(CodeListObject::getCreationTimestamp).toList());
        assertEquals(List.of(createdAt.plusDays(102), createdAt.plusDays(103)),
                codeLists.stream().map(CodeListObject::getLastUpdateTimestamp).toList());
        assertThrows(TooManyRowsException.class,
                () -> api.getCodeListByCodeListNameAndReleaseNum("duplicate-name", "10.8.7.1"));
    }
}
