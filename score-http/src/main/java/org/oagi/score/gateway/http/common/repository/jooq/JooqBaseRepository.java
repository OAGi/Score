package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Id;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Library;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Release;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.LibraryRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ReleaseRecord;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public abstract class JooqBaseRepository {

    private final DSLContext dslContext;
    private final ScoreUser requester;
    private RepositoryFactory repositoryFactory;

    public JooqBaseRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        this.dslContext = dslContext;
        this.requester = requester;
        this.repositoryFactory = repositoryFactory;
    }

    public DSLContext dslContext() {
        return dslContext;
    }

    public ScoreUser requester() {
        return requester;
    }

    public RepositoryFactory repositoryFactory() {
        return repositoryFactory;
    }

    /** Has the repository ask for the other repositories of a factory that is not the one that made it: the one that is wrapped (see AccessControlAspect). */
    public void useRepositoryFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public final ULong valueOf(Id id) {
        return (id != null) ? ULong.valueOf(id.value()) : null;
    }

    public final List<ULong> valueOf(Collection<? extends Id> idList) {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }
        return idList.stream().map(id -> valueOf(id)).collect(Collectors.toList());
    }

    public final void setForeignKeyChecks(boolean checks) {
        dslContext.query("SET FOREIGN_KEY_CHECKS = " + ((checks) ? "1" : "0")).execute();
    }

    public final Stream<Field<?>> fields(Field<?>... fields) {
        return Arrays.stream(fields);
    }

    public final Field<?>[] concat(Stream<? extends Field<?>>... streams) {
        return Stream.of(streams)
                .flatMap(Function.identity())
                .toArray(Field[]::new);
    }

    public final Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public final Stream<? extends Field<?>> creatorFields() {
        return Arrays.asList(
                creatorTablePk().as("creator_user_id"),
                creatorTable().LOGIN_ID.as("creator_login_id"),
                creatorTable().NAME.as("creator_name"),
                creatorTable().IS_DEVELOPER.as("creator_is_developer"),
                creatorTable().IS_ADMIN.as("creator_is_admin")
        ).stream();
    }

    public final AppUser creatorTable() {
        return APP_USER.as("creator");
    }

    public final TableField<AppUserRecord, ULong> creatorTablePk() {
        return creatorTable().APP_USER_ID;
    }

    public final UserId fetchCreaterUserId(Record record) {
        return new UserId(record.get(creatorTablePk().as("creator_user_id")).toBigInteger());
    }

    public final UserSummaryRecord fetchCreatorSummary(Record record) {
        return new UserSummaryRecord(
                fetchCreaterUserId(record),
                record.get(creatorTable().LOGIN_ID.as("creator_login_id")),
                record.get(creatorTable().LOGIN_ID.as("creator_name")),
                ((byte) 1 == record.get(creatorTable().IS_ADMIN.as("creator_is_admin"))) ?
                        Arrays.asList(
                                ((byte) 1 == record.get(creatorTable().IS_DEVELOPER.as("creator_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                ScoreRole.ADMINISTRATOR
                        ) :
                        Arrays.asList(
                                ((byte) 1 == record.get(creatorTable().IS_DEVELOPER.as("creator_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                        )
        );
    }

    public final Stream<? extends Field<?>> updaterFields() {
        return Arrays.asList(
                updaterTablePk().as("updater_user_id"),
                updaterTable().LOGIN_ID.as("updater_login_id"),
                updaterTable().NAME.as("updater_name"),
                updaterTable().IS_DEVELOPER.as("updater_is_developer"),
                updaterTable().IS_ADMIN.as("updater_is_admin")
        ).stream();
    }

    public final AppUser updaterTable() {
        return APP_USER.as("updater");
    }

    public final TableField<AppUserRecord, ULong> updaterTablePk() {
        return updaterTable().APP_USER_ID;
    }

    public final UserId fetchUpdaterUserId(Record record) {
        return new UserId(record.get(updaterTablePk().as("updater_user_id")).toBigInteger());
    }

    public final UserSummaryRecord fetchUpdaterSummary(Record record) {
        return new UserSummaryRecord(
                fetchUpdaterUserId(record),
                record.get(updaterTable().LOGIN_ID.as("updater_login_id")),
                record.get(updaterTable().LOGIN_ID.as("updater_name")),
                ((byte) 1 == record.get(updaterTable().IS_ADMIN.as("updater_is_admin"))) ?
                        Arrays.asList(
                                ((byte) 1 == record.get(updaterTable().IS_DEVELOPER.as("updater_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                ScoreRole.ADMINISTRATOR
                        ) :
                        Arrays.asList(
                                ((byte) 1 == record.get(updaterTable().IS_DEVELOPER.as("updater_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                        )
        );
    }

    public final Stream<? extends Field<?>> ownerFields() {
        return Arrays.asList(
                ownerTablePk().as("owner_user_id"),
                ownerTable().LOGIN_ID.as("owner_login_id"),
                ownerTable().NAME.as("owner_name"),
                ownerTable().IS_DEVELOPER.as("owner_is_developer"),
                ownerTable().IS_ADMIN.as("owner_is_admin")
        ).stream();
    }

    public final AppUser ownerTable() {
        return APP_USER.as("owner");
    }

    public final TableField<AppUserRecord, ULong> ownerTablePk() {
        return ownerTable().APP_USER_ID;
    }

    public final UserId fetchOwnerUserId(Record record) {
        return new UserId(record.get(ownerTablePk().as("owner_user_id")).toBigInteger());
    }

    public final UserSummaryRecord fetchOwnerSummary(Record record) {
        return new UserSummaryRecord(
                fetchOwnerUserId(record),
                record.get(ownerTable().LOGIN_ID.as("owner_login_id")),
                record.get(ownerTable().LOGIN_ID.as("owner_name")),
                ((byte) 1 == record.get(ownerTable().IS_ADMIN.as("owner_is_admin"))) ?
                        Arrays.asList(
                                ((byte) 1 == record.get(ownerTable().IS_DEVELOPER.as("owner_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                ScoreRole.ADMINISTRATOR
                        ) :
                        Arrays.asList(
                                ((byte) 1 == record.get(ownerTable().IS_DEVELOPER.as("owner_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                        )
        );
    }

    public SeqKeyHandler seqKeyHandler(AsccSummaryRecord ascc) {
        SeqKeyHandler seqKeyHandler = new SeqKeyHandler(
                repositoryFactory().seqKeyQueryRepository(requester()),
                repositoryFactory().seqKeyCommandRepository(requester()));
        seqKeyHandler.init(ascc.fromAccManifestId(), ascc.seqKeyId(), ascc.asccManifestId());
        return seqKeyHandler;
    }

    public SeqKeyHandler seqKeyHandler(BccSummaryRecord bcc) {
        SeqKeyHandler seqKeyHandler = new SeqKeyHandler(
                repositoryFactory().seqKeyQueryRepository(requester()),
                repositoryFactory().seqKeyCommandRepository(requester()));
        seqKeyHandler.init(bcc.fromAccManifestId(), bcc.seqKeyId(), bcc.bccManifestId());
        return seqKeyHandler;
    }

    public Library libraryTable() {
        return LIBRARY;
    }

    public TableField<LibraryRecord, ULong> libraryTablePk() {
        return libraryTable().LIBRARY_ID;
    }

    public final Stream<? extends Field<?>> libraryFields() {
        return Arrays.asList(
                libraryTablePk(),
                libraryTable().NAME.as("library_name"),
                libraryTable().STATE.as("library_state"),
                libraryTable().IS_READ_ONLY,
                libraryTable().IS_DEFAULT
        ).stream();
    }

    public LibrarySummaryRecord fetchLibrarySummary(Record record) {
        return new LibrarySummaryRecord(
                new LibraryId(record.get(libraryTablePk()).toBigInteger()),
                record.get(libraryTable().NAME.as("library_name")),
                record.get(libraryTable().STATE.as("library_state")),
                (byte) 1 == record.get(libraryTable().IS_READ_ONLY),
                (byte) 1 == record.get(libraryTable().IS_DEFAULT)
        );
    }

    public Release releaseTable() {
        return RELEASE;
    }

    public TableField<ReleaseRecord, ULong> releaseTablePk() {
        return releaseTable().RELEASE_ID;
    }

    public final Stream<? extends Field<?>> releaseFields() {
        return Arrays.asList(
                releaseTablePk(),
                releaseTable().RELEASE_NUM,
                releaseTable().STATE.as("release_state")
        ).stream();
    }

    public ReleaseSummaryRecord fetchReleaseSummary(Record record) {
        return new ReleaseSummaryRecord(
                new ReleaseId(record.get(releaseTablePk()).toBigInteger()),
                new LibraryId(record.get(libraryTablePk()).toBigInteger()),
                record.get(releaseTable().RELEASE_NUM),
                ReleaseState.valueOf(record.get(releaseTable().STATE.as("release_state")))
        );
    }

}
