package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A blank Definition, Definition Source or Remark clears the stored text instead of being skipped. */
class CodeListClearTextFieldsTest {

    private static final CodeListManifestId MANIFEST = new CodeListManifestId(BigInteger.ONE);
    private static final CodeListValueManifestId VALUE_MANIFEST = new CodeListValueManifestId(BigInteger.TWO);

    private final List<String> sql = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();
    private JooqCodeListCommandRepository repository;

    @BeforeEach
    void setUp() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(Arrays.asList(ctx.bindings()));
            return new MockResult[]{new MockResult(1, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        CodeListQueryRepository query = mock(CodeListQueryRepository.class);
        CodeListDetailsRecord details = mock(CodeListDetailsRecord.class);
        when(details.codeListId()).thenReturn(new CodeListId(BigInteger.TEN));
        when(query.getCodeListDetails(MANIFEST)).thenReturn(details);
        CodeListValueDetailsRecord valueDetails = mock(CodeListValueDetailsRecord.class);
        when(valueDetails.codeListValueId()).thenReturn(new CodeListValueId(BigInteger.valueOf(20)));
        when(query.getCodeListValueDetails(VALUE_MANIFEST)).thenReturn(valueDetails);

        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.codeListQueryRepository(any())).thenReturn(query);
        when(repositories.agencyIdListQueryRepository(any())).thenReturn(mock(AgencyIdListQueryRepository.class));
        when(repositories.logCommandRepository(any())).thenReturn(mock(LogCommandRepository.class));
        ScoreUser requester = new ScoreUser(new UserId(BigInteger.ONE), "u", "U", null, false, List.of(ScoreRole.DEVELOPER));
        repository = new JooqCodeListCommandRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), requester, repositories);
    }

    private int indexOfUpdateOf(String table) {
        for (int i = 0; i < sql.size(); i++) {
            if (sql.get(i).startsWith("update `" + table + "`")) {
                return i;
            }
        }
        throw new AssertionError(sql.toString());
    }

    @Test
    void blankDefinitionDefinitionSourceAndRemarkAreWrittenAsNull() {
        repository.update(MANIFEST, "name", null, null, null, new Definition("", ""), "", null, null, null);

        int i = indexOfUpdateOf("code_list");
        assertTrue(sql.get(i).contains("`definition` = ?"), sql.get(i));
        assertTrue(sql.get(i).contains("`definition_source` = ?"), sql.get(i));
        assertTrue(sql.get(i).contains("`remark` = ?"), sql.get(i));
        assertEquals(3, bindings.get(i).stream().filter(b -> b == null).count(), bindings.get(i).toString());
    }

    @Test
    void blankValueDefinitionAndSourceAreWrittenAsNull() {
        repository.updateValue(VALUE_MANIFEST, "v", "m", new Definition(null, ""), null);

        int i = indexOfUpdateOf("code_list_value");
        assertTrue(sql.get(i).contains("`definition` = ?"), sql.get(i));
        assertTrue(sql.get(i).contains("`definition_source` = ?"), sql.get(i));
        assertEquals(2, bindings.get(i).stream().filter(b -> b == null).count(), bindings.get(i).toString());
    }
}
