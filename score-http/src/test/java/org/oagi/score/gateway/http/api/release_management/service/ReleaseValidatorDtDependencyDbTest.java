package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The release validation of the Data Type dependencies, run on the 'Working' release of the libraries of the development
 * database with every Data Type, Code List and Agency ID List assigned (read only):
 * <pre>
 *   mvn test -Dtest=ReleaseValidatorDtDependencyDbTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * Nothing is reported for the components of the library, except that they are not yet {@code Candidate} and that a component has no namespace (another check).
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseValidatorDtDependencyDbTest {

    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

    private static TestDataSource dataSource;
    private Connection connection;
    private DSLContext dsl;
    private RepositoryFactory factory;

    @BeforeAll
    static void open() {
        dataSource = TestDataSource.open(2);
    }

    @AfterAll
    static void close() {
        dataSource.close();
    }

    @BeforeEach
    void begin() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        factory = new RepositoryFactory(dsl);
    }

    @AfterEach
    void rollback() throws SQLException {
        connection.rollback();
        connection.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"connectSpec"})
    void nothingIsReportedForAWorkingReleaseWithEverythingAssigned(String library) {
        long working = dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = ? and r.release_num = 'Working'", library).get(0, Long.class);

        ReleaseValidator validator = new ReleaseValidator(REQUESTER, new ReleaseId(BigInteger.valueOf(working)), factory);
        validator.setAssignedDtComponentManifestIds(dsl.fetch("select dt_manifest_id from dt_manifest "
                + "where release_id = ?", working).map(r -> new DtManifestId(r.get(0, BigInteger.class))));
        validator.setAssignedCodeListComponentManifestIds(dsl.fetch("select code_list_manifest_id "
                + "from code_list_manifest where release_id = ?", working)
                .map(r -> new CodeListManifestId(r.get(0, BigInteger.class))));
        validator.setAssignedAgencyIdListComponentManifestIds(dsl.fetch("select agency_id_list_manifest_id "
                + "from agency_id_list_manifest where release_id = ?", working)
                .map(r -> new AgencyIdListManifestId(r.get(0, BigInteger.class))));
        ReleaseValidationResponse response = validator.validate();

        List<String> errors = new ArrayList<>();
        List.of(response.getStatusMapForDt(), response.getStatusMapForCodeList(), response.getStatusMapForAgencyIdList())
                .forEach(map -> map.values().forEach(set -> set.stream()
                        .filter(m -> m.getLevel() == ReleaseValidationResponse.ValidationMessageLevel.Error)
                        .filter(m -> !m.getMessage().contains("should be in 'Candidate'"))
                        .filter(m -> !m.getMessage().equals("Namespace is required."))
                        .forEach(m -> errors.add(m.getMessage()))));
        System.out.println(library + ": " + errors.size() + " error(s) other than 'should be in Candidate'");
        errors.stream().limit(15).forEach(e -> System.out.println("  " + e));
        assertTrue(errors.isEmpty(), errors.toString());
    }
}
