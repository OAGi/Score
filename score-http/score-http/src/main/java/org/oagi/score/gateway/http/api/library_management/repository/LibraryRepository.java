package org.oagi.score.gateway.http.api.library_management.repository;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.library_management.data.Library;
import org.oagi.score.gateway.http.api.library_management.data.LibraryList;
import org.oagi.score.gateway.http.api.library_management.data.LibraryListRequest;
import org.oagi.score.gateway.http.api.release_management.data.ReleaseDetail;
import org.oagi.score.gateway.http.api.release_management.data.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

@Repository
public class LibraryRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BieService bieService;

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private LogRepository logRepository;

    public List<Library> getLibraries() {
        return selectOnConditionStep()
                .fetch(mapper());
    }

    public Library getLibraryById(BigInteger libraryId) {
        return selectOnConditionStep()
                .where(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId)))
                .fetchOne(mapper());
    }

    private SelectOnConditionStep<Record19<
            ULong, String, String, String, String,
            String, String, ULong, String, String,
            Byte, Byte, ULong, String, String,
            Byte, Byte, LocalDateTime, LocalDateTime>> selectOnConditionStep() {
        return dslContext.select(
                        LIBRARY.LIBRARY_ID,
                        LIBRARY.NAME,
                        LIBRARY.ORGANIZATION,
                        LIBRARY.LINK,
                        LIBRARY.DOMAIN,
                        LIBRARY.DESCRIPTION,
                        LIBRARY.STATE,
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").NAME.as("creator_name"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").NAME.as("updater_name"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"),
                        LIBRARY.CREATION_TIMESTAMP,
                        LIBRARY.LAST_UPDATE_TIMESTAMP
                )
                .from(LIBRARY)
                .join(APP_USER.as("creator")).on(LIBRARY.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(LIBRARY.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<org.jooq.Record, Library> mapper() {
        return record -> {
            Library library = new Library();

            library.setLibraryId(record.getValue(LIBRARY.LIBRARY_ID).toBigInteger());
            library.setName(record.getValue(LIBRARY.NAME));
            library.setOrganization(record.getValue(LIBRARY.ORGANIZATION));
            library.setLink(record.getValue(LIBRARY.LINK));
            library.setDomain(record.getValue(LIBRARY.DOMAIN));
            library.setDescription(record.getValue(LIBRARY.DESCRIPTION));
            library.setState(record.getValue(LIBRARY.STATE));

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            library.setCreatedBy((isCreatorAdmin) ? new ScoreUser(record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(), record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")), record.get(APP_USER.as("creator").NAME.as("creator_name")), Arrays.asList(creatorRole, ADMINISTRATOR)) : new ScoreUser(record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(), record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")), record.get(APP_USER.as("creator").NAME.as("creator_name")), creatorRole));

            ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
            boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
            library.setLastUpdatedBy((isUpdaterAdmin) ? new ScoreUser(record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(), record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")), record.get(APP_USER.as("updater").NAME.as("updater_name")), Arrays.asList(updaterRole, ADMINISTRATOR)) : new ScoreUser(record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(), record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")), record.get(APP_USER.as("updater").NAME.as("updater_name")), updaterRole));

            library.setCreationTimestamp(Date.from(record.get(LIBRARY.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            library.setLastUpdateTimestamp(Date.from(record.get(LIBRARY.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));

            return library;
        };
    }

    public BigInteger create(ScoreUser requester, Library library) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can create the library.");
        }

        if (isLibraryNameAlreadyExist(library.getName(), null)) {
            throw new IllegalArgumentException("The library name '" + library.getName() + "' already exists.");
        }

        ULong userId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        LibraryRecord libraryRecord = new LibraryRecord();
        libraryRecord.setName(library.getName());
        libraryRecord.setOrganization(library.getOrganization());
        libraryRecord.setLink(library.getLink());
        libraryRecord.setDomain(library.getDomain());
        libraryRecord.setDescription(library.getDescription());
        libraryRecord.setState(null);
        libraryRecord.setCreatedBy(userId);
        libraryRecord.setLastUpdatedBy(userId);
        libraryRecord.setCreationTimestamp(timestamp);
        libraryRecord.setLastUpdateTimestamp(timestamp);

        libraryRecord.setLibraryId(dslContext.insertInto(LIBRARY)
                .set(libraryRecord)
                .returning(LIBRARY.LIBRARY_ID)
                .fetchOne().getLibraryId());

        // Create 'Working' Release.
        ReleaseDetail releaseDetail = new ReleaseDetail();
        releaseDetail.setLibraryId(libraryRecord.getLibraryId().toBigInteger());
        releaseDetail.setGuid(ScoreGuid.randomGuid());
        releaseDetail.setReleaseNum("Working");
        releaseDetail = releaseService.createRelease(requester, releaseDetail).getReleaseDetail();
        ULong releaseId = ULong.valueOf(releaseDetail.getReleaseId());
        dslContext.update(RELEASE)
                .set(RELEASE.STATE, ReleaseState.Published.name())
                .where(RELEASE.RELEASE_ID.eq(releaseId))
                .execute();

        // Make manifests for CDTs
        List<DtRecord> cdtList = dslContext.selectFrom(DT)
                .where(DT.BASED_DT_ID.isNull())
                .fetch();

        for (DtRecord cdt : cdtList) {
            DtManifestRecord cdtManifestRecord = new DtManifestRecord();
            cdtManifestRecord.setReleaseId(releaseId);
            cdtManifestRecord.setDtId(cdt.getDtId());
            cdtManifestRecord.setDen(cdt.getDataTypeTerm() + ". Type");
            LogRecord logRecord = dslContext.selectFrom(LOG)
                    .where(LOG.REFERENCE.eq(cdt.getGuid()))
                    .orderBy(LOG.LOG_ID.asc())
                    .limit(1)
                    .fetchOptional().orElse(null);
            cdtManifestRecord.setLogId(logRecord.getLogId());

            cdtManifestRecord.setDtManifestId(dslContext.insertInto(DT_MANIFEST)
                    .set(cdtManifestRecord)
                    .returning(DT_MANIFEST.DT_MANIFEST_ID)
                    .fetchOne().getDtManifestId());

            List<DtScRecord> cdtScRecordList = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.OWNER_DT_ID.eq(cdt.getDtId()))
                    .fetch();
            for (DtScRecord cdtSc : cdtScRecordList) {
                DtScManifestRecord cdtScManifestRecord = new DtScManifestRecord();
                cdtScManifestRecord.setReleaseId(releaseId);
                cdtScManifestRecord.setDtScId(cdtSc.getDtScId());
                cdtScManifestRecord.setOwnerDtManifestId(cdtManifestRecord.getDtManifestId());

                cdtScManifestRecord.setDtScManifestId(dslContext.insertInto(DT_SC_MANIFEST)
                        .set(cdtScManifestRecord)
                        .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                        .fetchOne().getDtScManifestId());
            }

            if (logRecord == null) {
                logRecord = logRepository.insertBdtLog(cdtManifestRecord, cdt, LogAction.Added, userId, timestamp);
                cdtManifestRecord.setLogId(logRecord.getLogId());
                dslContext.update(DT_MANIFEST)
                        .set(DT_MANIFEST.LOG_ID, logRecord.getLogId())
                        .where(DT_MANIFEST.DT_MANIFEST_ID.eq(cdtManifestRecord.getDtManifestId()))
                        .execute();
            }
        }

        List<XbtRecord> defaultXbtList = dslContext.selectFrom(XBT)
                .where(XBT.BUILTIN_TYPE.startsWith("xsd:"))
                .fetch();

        for (XbtRecord defaultXbt : defaultXbtList) {
            XbtManifestRecord xbtManifestRecord = new XbtManifestRecord();
            xbtManifestRecord.setReleaseId(releaseId);
            xbtManifestRecord.setXbtId(defaultXbt.getXbtId());
            LogRecord logRecord = dslContext.selectFrom(LOG)
                    .where(LOG.REFERENCE.eq(defaultXbt.getGuid()))
                    .orderBy(LOG.LOG_ID.asc())
                    .limit(1)
                    .fetchOptional().orElse(null);
            xbtManifestRecord.setLogId(logRecord.getLogId());
            xbtManifestRecord.setXbtManifestId(
                    dslContext.insertInto(XBT_MANIFEST)
                            .set(xbtManifestRecord)
                            .returning(XBT_MANIFEST.XBT_MANIFEST_ID)
                            .fetchOne().getXbtManifestId());
        }

        return libraryRecord.getLibraryId().toBigInteger();
    }

    private boolean isLibraryNameAlreadyExist(String name, ULong libraryId) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(LIBRARY.NAME.eq(name));
        if (libraryId != null) {
            conditions.add(LIBRARY.LIBRARY_ID.notEqual(libraryId));
        }

        return dslContext.selectCount()
                .from(LIBRARY)
                .where(conditions)
                .fetchOneInto(Integer.class) > 0;
    }

    public void update(ScoreUser requester, Library library) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can update the library.");
        }

        LibraryRecord libraryRecord = dslContext.selectFrom(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())))
                .fetchOptional().orElse(null);
        if (libraryRecord == null) {
            throw new IllegalArgumentException("Cannot find the library with the ID: " + library.getLibraryId());
        }

        if (isLibraryNameAlreadyExist(library.getName(), libraryRecord.getLibraryId())) {
            throw new IllegalArgumentException("The library name '" + library.getName() + "' already exists.");
        }

        ULong userId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        int res = dslContext.update(LIBRARY)
                .set(LIBRARY.NAME, library.getName())
                .set(LIBRARY.ORGANIZATION, library.getOrganization())
                .set(LIBRARY.LINK, library.getLink())
                .set(LIBRARY.DOMAIN, library.getDomain())
                .set(LIBRARY.DESCRIPTION, library.getDescription())
                .set(LIBRARY.STATE, library.getState())
                .set(LIBRARY.LAST_UPDATED_BY, userId)
                .set(LIBRARY.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(LIBRARY.LIBRARY_ID.eq(libraryRecord.getLibraryId()))
                .execute();

        if (res != 1) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    public void discard(ScoreUser requester, BigInteger libraryId) {
        if (!requester.hasRole(ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only administrators can update the library.");
        }

        LibraryRecord libraryRecord = dslContext.selectFrom(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId)))
                .fetchOptional().orElse(null);
        if (libraryRecord == null) {
            throw new EmptyResultDataAccessException(1);
        }

        List<ReleaseRecord> releaseRecords = dslContext.selectFrom(RELEASE)
                .where(RELEASE.LIBRARY_ID.eq(libraryRecord.getLibraryId()))
                .fetch();
        if (!releaseRecords.isEmpty()) {
            Collection<ULong> releaseIdSet = releaseRecords.stream().map(e -> e.getReleaseId()).collect(Collectors.toSet());
            List<BigInteger> topLevelAsbiepIds = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                    .from(TOP_LEVEL_ASBIEP)
                    .where(TOP_LEVEL_ASBIEP.RELEASE_ID.in(releaseIdSet))
                    .fetchInto(BigInteger.class);
            bieService.deleteBieList(requester, topLevelAsbiepIds);

            dslContext.query("SET FOREIGN_KEY_CHECKS = 0").execute();
            dslContext.deleteFrom(ACC_MANIFEST).where(ACC_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(ASCC_MANIFEST).where(ASCC_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(BCC_MANIFEST).where(BCC_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(ASCCP_MANIFEST).where(ASCCP_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(BCCP_MANIFEST).where(BCCP_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(DT_MANIFEST).where(DT_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(DT_SC_MANIFEST).where(DT_SC_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(CODE_LIST_MANIFEST).where(CODE_LIST_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(CODE_LIST_VALUE_MANIFEST).where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(AGENCY_ID_LIST_MANIFEST).where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST).where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();
            dslContext.deleteFrom(XBT_MANIFEST).where(XBT_MANIFEST.RELEASE_ID.in(releaseIdSet)).execute();

            dslContext.deleteFrom(RELEASE).where(RELEASE.RELEASE_ID.in(releaseIdSet)).execute();
        }

        libraryRecord.delete();
    }

    public PageResponse<LibraryList> fetch(ScoreUser requester,
                                           LibraryListRequest request) {

        PageRequest pageRequest = request.getPageRequest();
        SelectOnConditionStep<Record9<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String>> selectOnConditionStep =
                dslContext.select(LIBRARY.LIBRARY_ID, LIBRARY.NAME, LIBRARY.ORGANIZATION,
                                LIBRARY.DESCRIPTION, LIBRARY.LINK, LIBRARY.DOMAIN, LIBRARY.STATE,
                                LIBRARY.LAST_UPDATE_TIMESTAMP,
                                APP_USER.as("updater").LOGIN_ID.as("last_update_user"))
                        .from(LIBRARY)
                        .join(APP_USER.as("updater"))
                        .on(APP_USER.as("updater").APP_USER_ID.eq(LIBRARY.LAST_UPDATED_BY));

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(request.getName())) {
            conditions.add(LIBRARY.NAME.containsIgnoreCase(request.getName()));
        }
        if (StringUtils.hasLength(request.getOrganization())) {
            conditions.add(LIBRARY.ORGANIZATION.containsIgnoreCase(request.getOrganization()));
        }
        if (StringUtils.hasLength(request.getDescription())) {
            conditions.add(LIBRARY.DESCRIPTION.containsIgnoreCase(request.getDescription()));
        }
        if (StringUtils.hasLength(request.getDomain())) {
            conditions.add(LIBRARY.DOMAIN.containsIgnoreCase(request.getDomain()));
        }
        if (StringUtils.hasLength(request.getState())) {
            conditions.add(LIBRARY.STATE.containsIgnoreCase(request.getState()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(LIBRARY.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                    new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(LIBRARY.LAST_UPDATE_TIMESTAMP.lessThan(
                    new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        SelectConditionStep<Record9<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String>> conditionStep = selectOnConditionStep.where(conditions);

        int length = dslContext.fetchCount(conditionStep);

        SortField sortField = null;
        if (StringUtils.hasLength(pageRequest.getSortActive())) {
            Field field = null;
            switch (pageRequest.getSortActive()) {
                case "name":
                    field = LIBRARY.NAME;
                    break;

                case "organization":
                    field = LIBRARY.ORGANIZATION;
                    break;

                case "description":
                    field = LIBRARY.DESCRIPTION;
                    break;

                case "domain":
                    field = LIBRARY.DOMAIN;
                    break;

                case "state":
                    field = LIBRARY.STATE;
                    break;

                case "lastUpdateTimestamp":
                    field = LIBRARY.LAST_UPDATE_TIMESTAMP;
                    break;
            }

            if (field != null) {
                if ("asc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.asc();
                } else if ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.desc();
                }
            }
        }

        ResultQuery<Record9<
                ULong, String, String, String, String,
                String, String, LocalDateTime, String>> query;
        if (sortField != null) {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.orderBy(sortField)
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep.orderBy(sortField);
            }
        } else {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep;
            }
        }

        List<LibraryList> results = query.fetch(record -> {
            LibraryList libraryList = new LibraryList();
            libraryList.setLibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger());
            libraryList.setName(record.get(LIBRARY.NAME));
            libraryList.setOrganization(record.get(LIBRARY.ORGANIZATION));
            libraryList.setDescription(record.get(LIBRARY.DESCRIPTION));
            libraryList.setLink(record.get(LIBRARY.LINK));
            libraryList.setDomain(record.get(LIBRARY.DOMAIN));
            libraryList.setState(record.get(LIBRARY.STATE));
            libraryList.setLastUpdateTimestamp(Date.from(record.get(LIBRARY.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            libraryList.setLastUpdateUser(record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")));
            return libraryList;
        });

        PageResponse<LibraryList> response = new PageResponse();
        response.setList(results);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(length);

        return response;
    }

}
