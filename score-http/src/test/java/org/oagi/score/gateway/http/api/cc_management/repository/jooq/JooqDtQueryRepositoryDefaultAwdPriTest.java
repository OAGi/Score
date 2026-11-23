package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The default allowed primitive of every data type and supplementary component of a release, as the rule of
 * {@code DefaultAwdPriRule} picks it: the terms come from the database, the allowed primitives from the summaries.
 */
class JooqDtQueryRepositoryDefaultAwdPriTest {

    private static final ReleaseId RELEASE = new ReleaseId(BigInteger.valueOf(3));

    private static final DtId TEXT = new DtId(BigInteger.valueOf(1));
    private static final DtId DATE = new DtId(BigInteger.valueOf(2));
    private static final DtId NO_PRIMITIVE = new DtId(BigInteger.valueOf(3));

    private static DtAwdPriSummaryRecord awdPri(long id, DtId dt, String xbtName, boolean isDefault) {
        return new DtAwdPriSummaryRecord(new DtAwdPriId(BigInteger.valueOf(id)), RELEASE, dt, null, null, xbtName,
                null, null, null, null, isDefault);
    }

    private static DtScAwdPriSummaryRecord scAwdPri(long id, DtScId sc, String xbtName, boolean isDefault) {
        return new DtScAwdPriSummaryRecord(new DtScAwdPriId(BigInteger.valueOf(id)), RELEASE, sc, null, null, xbtName,
                null, null, null, null, isDefault);
    }

    private static DtSummaryRecord dt(DtId id, String term) {
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dt.dtId()).thenReturn(id);
        when(dt.dataTypeTerm()).thenReturn(term);
        return dt;
    }

    private static DtScSummaryRecord dtSc(DtScId id, String term) {
        DtScSummaryRecord sc = mock(DtScSummaryRecord.class);
        when(sc.dtScId()).thenReturn(id);
        when(sc.representationTerm()).thenReturn(term);
        return sc;
    }

    /** The summaries and the allowed primitives are what the repository reads elsewhere; the rule is applied to them here. */
    private JooqDtQueryRepository repository(List<DtSummaryRecord> dts, List<DtScSummaryRecord> dtScs,
                                             List<DtAwdPriSummaryRecord> dtAwdPris, List<DtScAwdPriSummaryRecord> dtScAwdPris) {
        DSLContext dsl = DSL.using(new MockConnection(ctx -> new MockResult[0]), SQLDialect.MYSQL);
        ScoreUser user = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com",
                false, List.of(ScoreRole.END_USER));
        return new JooqDtQueryRepository(dsl, user, mock(RepositoryFactory.class)) {
            @Override
            public List<DtSummaryRecord> getDtSummaryList(ReleaseId releaseId) {
                return dts;
            }

            @Override
            public List<DtScSummaryRecord> getDtScSummaryList(Collection<ReleaseId> releaseIdList) {
                return dtScs;
            }

            @Override
            public List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(ReleaseId releaseId) {
                return dtAwdPris;
            }

            @Override
            public List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(ReleaseId releaseId) {
                return dtScAwdPris;
            }
        };
    }

    @Test
    void aDataTypeStartsWithItsDefaultPrimitiveOrWithThePrimitiveOfItsNameWhenItIsADateOrATime() {
        var textDefault = awdPri(10, TEXT, "token", true);
        var textOther = awdPri(11, TEXT, "string", false);
        var dateDefault = awdPri(20, DATE, "token", true);
        var dateNamed = awdPri(21, DATE, "date time", false);
        var repository = repository(List.of(dt(TEXT, "Text"), dt(DATE, "Date Time")), List.of(),
                List.of(textOther, textDefault, dateDefault, dateNamed), List.of());

        Map<DtId, DtAwdPriSummaryRecord> defaults = repository.getDefaultDtAwdPriSummaryMap(RELEASE);

        assertEquals(textDefault, defaults.get(TEXT));
        assertEquals(dateNamed, defaults.get(DATE));
        assertEquals(2, defaults.size());
    }

    @Test
    void aDataTypeThatCannotStartWithAPrimitiveHasNoDefaultAndDoesNotStopTheOthers() {
        var noDefault = awdPri(30, NO_PRIMITIVE, "string", false);
        var dateWithoutItsPrimitive = awdPri(31, DATE, "token", true);
        var textDefault = awdPri(10, TEXT, "token", true);
        var repository = repository(List.of(dt(TEXT, "Text"), dt(DATE, "Date"), dt(NO_PRIMITIVE, "Code")), List.of(),
                List.of(noDefault, dateWithoutItsPrimitive, textDefault), List.of());

        Map<DtId, DtAwdPriSummaryRecord> defaults = repository.getDefaultDtAwdPriSummaryMap(RELEASE);

        assertNull(defaults.get(NO_PRIMITIVE));
        assertNull(defaults.get(DATE));
        assertEquals(textDefault, defaults.get(TEXT));
    }

    @Test
    void aSupplementaryComponentIsJudgedByItsRepresentationTerm() {
        var sc = new DtScId(BigInteger.valueOf(5));
        var scDefault = scAwdPri(50, sc, "token", true);
        var scOther = scAwdPri(51, sc, "string", false);
        var repository = repository(List.of(), List.of(dtSc(sc, "Code")), List.of(), List.of(scOther, scDefault));

        assertEquals(Map.of(sc, scDefault), repository.getDefaultDtScAwdPriSummaryMap(RELEASE));
    }
}
