package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadJobRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class JooqReleasePackageLoadJobRepository extends JooqBaseRepository implements ReleasePackageLoadJobRepository {

    /**
     * The table of the record, by its name: it is not one of the generated classes of the schema, which are made again from the
     * database and are not to be changed by hand.
     */
    private static final Table<?> JOB = DSL.table(DSL.name("release_package_load_job"));
    private static final Field<ULong> ID = DSL.field(DSL.name("release_package_load_job_id"), SQLDataType.BIGINTUNSIGNED);
    private static final Field<String> STATE = DSL.field(DSL.name("state"), SQLDataType.VARCHAR);
    private static final Field<String> MESSAGE = DSL.field(DSL.name("message"), SQLDataType.CLOB);
    private static final Field<String> PACKAGE_FILE_NAME = DSL.field(DSL.name("package_file_name"), SQLDataType.VARCHAR);
    private static final Field<String> RELEASE_NUM = DSL.field(DSL.name("release_num"), SQLDataType.VARCHAR);
    private static final Field<String> STAGING_SCHEMA = DSL.field(DSL.name("staging_schema"), SQLDataType.VARCHAR);
    private static final Field<String> BACKUP_SCHEMA = DSL.field(DSL.name("backup_schema"), SQLDataType.VARCHAR);
    private static final Field<String> DISCARDED_SCHEMA = DSL.field(DSL.name("discarded_schema"), SQLDataType.VARCHAR);
    private static final Field<String> NODE_ID = DSL.field(DSL.name("node_id"), SQLDataType.VARCHAR);
    private static final Field<ULong> OWNER_USER_ID = DSL.field(DSL.name("owner_user_id"), SQLDataType.BIGINTUNSIGNED);
    private static final Field<LocalDateTime> CREATION_TIMESTAMP = DSL.field(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME);
    private static final Field<LocalDateTime> LAST_UPDATE_TIMESTAMP = DSL.field(DSL.name("last_update_timestamp"), SQLDataType.LOCALDATETIME);

    /** What is read of a record, with its types, so that the values are converted as they are read. */
    private static final Field<?>[] COLUMNS = {ID, STATE, MESSAGE, PACKAGE_FILE_NAME, RELEASE_NUM, STAGING_SCHEMA, BACKUP_SCHEMA,
            DISCARDED_SCHEMA, NODE_ID, OWNER_USER_ID, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP};

    private static final Set<String> ACTIVE = ReleasePackageLoadState.ACTIVE.stream().map(Enum::name).collect(Collectors.toSet());

    public JooqReleasePackageLoadJobRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public Optional<ReleasePackageLoadJobId> createUnlessActive(UserId owner, String packageFileName, String releaseNum, String nodeId) {
        LocalDateTime now = LocalDateTime.now();
        return dslContext().insertInto(JOB,
                        STATE, PACKAGE_FILE_NAME, RELEASE_NUM,
                        NODE_ID, OWNER_USER_ID,
                        CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP)
                .select(DSL.select(DSL.val(ReleasePackageLoadState.PENDING.name()), DSL.val(packageFileName), DSL.val(releaseNum),
                                DSL.val(nodeId), DSL.val(ULong.valueOf(owner.value())), DSL.val(now), DSL.val(now))
                        .whereNotExists(DSL.selectOne().from(JOB).where(STATE.in(ACTIVE))))
                .returningResult(ID)
                .fetchOptional()
                .map(record -> new ReleasePackageLoadJobId(record.value1().toBigInteger()));
    }

    @Override
    public Optional<ReleasePackageLoadJob> find(ReleasePackageLoadJobId id) {
        return dslContext().select(COLUMNS).from(JOB)
                .where(ID.eq(ULong.valueOf(id.value())))
                .fetchOptional(this::job);
    }

    @Override
    public List<ReleasePackageLoadJob> findActive() {
        return dslContext().select(COLUMNS).from(JOB)
                .where(STATE.in(ACTIVE))
                .orderBy(ID)
                .fetch(this::job);
    }

    @Override
    public List<ReleasePackageLoadJob> findRecent(int limit) {
        return dslContext().select(COLUMNS).from(JOB)
                .orderBy(ID.desc())
                .limit(limit)
                .fetch(this::job);
    }

    @Override
    public Optional<ReleasePackageLoadJob> findLatest(ReleasePackageLoadState state) {
        return dslContext().select(COLUMNS).from(JOB)
                .where(STATE.eq(state.name()))
                .orderBy(ID.desc())
                .limit(1)
                .fetchOptional(this::job);
    }

    @Override
    public List<ReleasePackageLoadJob> findBefore(ReleasePackageLoadJobId id, ReleasePackageLoadState state) {
        return dslContext().select(COLUMNS).from(JOB)
                .where(STATE.eq(state.name()))
                .and(ID.lt(ULong.valueOf(id.value())))
                .orderBy(ID.desc())
                .fetch(this::job);
    }

    @Override
    public List<ReleasePackageLoadJob> findNotChangedSince(Set<ReleasePackageLoadState> states, LocalDateTime time) {
        return dslContext().select(COLUMNS).from(JOB)
                .where(STATE.in(names(states)))
                .and(LAST_UPDATE_TIMESTAMP.lt(time))
                .orderBy(ID)
                .fetch(this::job);
    }

    @Override
    public boolean delete(ReleasePackageLoadJobId id, Set<ReleasePackageLoadState> states) {
        return dslContext().deleteFrom(JOB)
                .where(ID.eq(ULong.valueOf(id.value())))
                .and(STATE.in(names(states)))
                .execute() == 1;
    }

    private static Set<String> names(Set<ReleasePackageLoadState> states) {
        return states.stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public boolean moveState(ReleasePackageLoadJobId id, Set<ReleasePackageLoadState> from, ReleasePackageLoadState to, String message) {
        return dslContext().update(JOB)
                .set(STATE, to.name())
                .set(MESSAGE, message)
                .set(LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(ID.eq(ULong.valueOf(id.value())))
                .and(STATE.in(from.stream().map(Enum::name).collect(Collectors.toSet())))
                .execute() == 1;
    }

    @Override
    public void progress(ReleasePackageLoadJobId id, String message) {
        dslContext().update(JOB)
                .set(MESSAGE, message)
                .set(LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(ID.eq(ULong.valueOf(id.value())))
                .execute();
    }

    @Override
    public void schemas(ReleasePackageLoadJobId id, String stagingSchema, String backupSchema, String discardedSchema) {
        var update = dslContext().update(JOB)
                .set(LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        if (stagingSchema != null) {
            update = update.set(STAGING_SCHEMA, stagingSchema);
        }
        if (backupSchema != null) {
            update = update.set(BACKUP_SCHEMA, backupSchema);
        }
        if (discardedSchema != null) {
            update = update.set(DISCARDED_SCHEMA, discardedSchema);
        }
        update.where(ID.eq(ULong.valueOf(id.value()))).execute();
    }

    @Override
    public List<ReleasePackageLoadJob> lockActiveSince(LocalDateTime time) {
        return dslContext().select(COLUMNS).from(JOB)
                .where(STATE.in(ACTIVE))
                .and(LAST_UPDATE_TIMESTAMP.lt(time))
                .forUpdate()
                .fetch(this::job);
    }

    private ReleasePackageLoadJob job(Record r) {
        return new ReleasePackageLoadJob(new ReleasePackageLoadJobId(r.get(ID).toBigInteger()),
                ReleasePackageLoadState.of(r.get(STATE)), r.get(MESSAGE), r.get(PACKAGE_FILE_NAME), r.get(RELEASE_NUM),
                r.get(STAGING_SCHEMA), r.get(BACKUP_SCHEMA), r.get(DISCARDED_SCHEMA), r.get(NODE_ID),
                new UserId(r.get(OWNER_USER_ID).toBigInteger()), r.get(CREATION_TIMESTAMP), r.get(LAST_UPDATE_TIMESTAMP));
    }
}
