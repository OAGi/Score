package org.oagi.score.repo.component.ascc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcRefactorValidationResponse;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccRecord;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest.ACC_MANIFEST;

@Repository
public class AsccReadRepository {

    @Autowired
    private DSLContext dslContext;

    public AsccRecord getAsccByManifestId(BigInteger asccManifestId) {
        return dslContext.select(ASCC.fields())
                .from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptionalInto(AsccRecord.class).orElse(null);
    }

    public AsccManifestRecord getAsccManifestById(BigInteger asccManifestId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptionalInto(AsccManifestRecord.class).orElse(null);
    }

    public AsccManifestRecord getUserExtensionAsccManifestByAsccpManifestId(BigInteger asccpManifestId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(AsccManifestRecord.class).orElse(null);
    }

    public CcRefactorValidationResponse validateAsccRefactoring(AppUser user, BigInteger asccManifestId, BigInteger accManifestId) {

        AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(
                        ULong.valueOf(asccManifestId)
                ))
                .fetchOne();

        int usedBieCount = dslContext.selectCount().from(ASBIE)
                .where(ASBIE.BASED_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be refactored.");
        }

        CcRefactorValidationResponse response = new CcRefactorValidationResponse();
        response.setType(CcType.ASCC.toString());
        response.setManifestId(asccManifestId);

        Map<ULong, List<String>> issueMap = getBlockerReasonMap(user, asccManifestRecord, ULong.valueOf(accManifestId));

        List<CcRefactorValidationResponse.IssuedCc> issuedCcList = dslContext.select(
                inline("ACC").as("type"),
                Tables.ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                Tables.ACC.ACC_ID.as("id"),
                Tables.ACC.GUID,
                Tables.ACC_MANIFEST.DEN,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                Tables.ACC.STATE,
                Tables.ACC.IS_DEPRECATED,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("appUserOwner").LOGIN_ID.as("owner"),
                APP_USER.as("appUserOwner").IS_DEVELOPER.as("owned_by_developer"),
                APP_USER.as("appUserUpdater").LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(Tables.ACC)
                .join(Tables.ACC_MANIFEST)
                .on(Tables.ACC.ACC_ID.eq(Tables.ACC_MANIFEST.ACC_ID).and(Tables.ACC_MANIFEST.RELEASE_ID.eq(asccManifestRecord.getReleaseId())))
                .join(LOG)
                .on(Tables.ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(Tables.ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(APP_USER.as("appUserOwner"))
                .on(Tables.ACC.OWNER_USER_ID.eq(APP_USER.as("appUserOwner").APP_USER_ID))
                .join(APP_USER.as("appUserUpdater"))
                .on(Tables.ACC.LAST_UPDATED_BY.eq(APP_USER.as("appUserUpdater").APP_USER_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(issueMap.keySet()))
                .fetchStream().map(row -> {
                    CcRefactorValidationResponse.IssuedCc issuedCc = new CcRefactorValidationResponse.IssuedCc();
                    issuedCc.setManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                    issuedCc.setId(row.getValue("id", ULong.class).toBigInteger());
                    issuedCc.setGuid(row.getValue("guid", String.class));
                    issuedCc.setDen(row.getValue("den", String.class));
                    issuedCc.setName(row.getValue("object_class_term", String.class));
                    Integer componentType = row.getValue("oagis_component_type", Integer.class);
                    if (componentType != null) {
                        issuedCc.setOagisComponentType(OagisComponentType.valueOf(componentType));
                    }
                    issuedCc.setState(CcState.valueOf(row.getValue("state", String.class)));
                    issuedCc.setDeprecated(row.getValue("is_deprecated", Byte.class) == 1);
                    issuedCc.setLastUpdateTimestamp(Date.from(row.getValue("last_update_timestamp", LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    issuedCc.setOwner((String) row.getValue("owner"));
                    issuedCc.setLastUpdateUser((String) row.getValue("last_update_user"));
                    issuedCc.setRevision(row.getValue(LOG.REVISION_NUM).toString());
                    issuedCc.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
                    issuedCc.setReasons(issueMap.get(ULong.valueOf(issuedCc.getManifestId())));
                    return issuedCc;
                }).collect(Collectors.toList());

        response.setIssueList(issuedCcList);

        return response;
    }

    public Map<ULong, List<String>> getBlockerReasonMap(AppUser requester, AsccManifestRecord asccManifestRecord, ULong targetAccManifestId) {

        ULong releaseId = asccManifestRecord.getReleaseId();
        List<AccManifestRecord> accManifestList = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, AccManifestRecord> accManifestMap = accManifestList.stream().collect(Collectors.toMap(AccManifestRecord::getAccManifestId, Function.identity()));
        Map<ULong, List<AccManifestRecord>> baseAccMap = accManifestList.stream().filter(e -> e.getBasedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccManifestRecord::getBasedAccManifestId));

        List<AccRecord> accList = dslContext.selectFrom(ACC).fetch();
        Map<ULong, AccRecord> accMap = accList.stream().collect(Collectors.toMap(AccRecord::getAccId, Function.identity()));

        List<AsccManifestRecord> asccList = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, List<AsccManifestRecord>> fromAccAsccMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccManifestRecord::getFromAccManifestId));

        List<ULong> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(targetAccManifestId);

        Set<ULong> accCandidates = new HashSet<>();

        for (ULong cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Map<ULong, ULong> groupMap = new HashMap<>();

        dslContext.select(ACC_MANIFEST.as("group").ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ASCC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                .join(ACC_MANIFEST.as("group")).on(ACC_MANIFEST.as("group").ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.as("group").ACC_ID.eq(ACC.ACC_ID))
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(releaseId),
                        ACC.OAGIS_COMPONENT_TYPE.in(OagisComponentType.SemanticGroup.getValue(),
                                OagisComponentType.UserExtensionGroup.getValue()),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(accCandidates)))
                .fetchStream().forEach(r -> {
            groupMap.put(r.get(ACC_MANIFEST.as("group").ACC_MANIFEST_ID), r.get(ACC_MANIFEST.ACC_MANIFEST_ID));
        });

        accCandidates.addAll(groupMap.keySet());

        Set<AsccManifestRecord> asccResult = new HashSet<>();

        for (ULong acc : accCandidates) {
            asccResult.addAll(
                    fromAccAsccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(ascc -> ascc.getToAsccpManifestId().equals(asccManifestRecord.getToAsccpManifestId())
                                    && !ascc.getAsccManifestId().equals(asccManifestRecord.getAsccManifestId()))
                            .collect(Collectors.toList()));
        }

        Map<ULong, List<String>> map = new HashMap<>();

        for (AsccManifestRecord ascc : asccResult) {
            AccManifestRecord amr = accManifestMap.get(ascc.getFromAccManifestId());
            AccRecord acc = accMap.get(amr.getAccId());
            map.computeIfAbsent(amr.getAccManifestId(), k -> new ArrayList<>());
            if (!acc.getState().equals(CcState.WIP.name())) {
                map.get(amr.getAccManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.getOwnerUserId().equals(ULong.valueOf(requester.getAppUserId()))
                    && !acc.getState().equals(CcState.Production.name())
                    && !acc.getState().equals(CcState.Published.name())) {
                map.get(amr.getAccManifestId()).add("Direct association: Ownership required.");
            }

            if (acc.getOagisComponentType().equals(OagisComponentType.SemanticGroup.getValue())
                    || acc.getOagisComponentType().equals(OagisComponentType.UserExtensionGroup.getValue())) {
                AccManifestRecord parentAccManifest = accManifestMap.get(groupMap.get(amr.getAccManifestId()));
                map.put(parentAccManifest.getAccManifestId(), map.get(amr.getAccManifestId()));
                map.remove(amr.getAccManifestId());
                map.get(parentAccManifest.getAccManifestId()).add("Ungrouping '" + acc.getObjectClassTerm() + "' required.");
            }
        }

        if (map.get(targetAccManifestId) == null) {
            AccManifestRecord amr = accManifestMap.get(targetAccManifestId);
            AccRecord acc = accMap.get(amr.getAccId());
            map.computeIfAbsent(amr.getAccManifestId(), k -> new ArrayList<>());
            if (!acc.getState().equals(CcState.WIP.name())) {
                map.get(amr.getAccManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.getOwnerUserId().equals(ULong.valueOf(requester.getAppUserId()))
                    && !acc.getState().equals(CcState.Production.name())
                    && !acc.getState().equals(CcState.Published.name())) {
                map.get(amr.getAccManifestId()).add("Direct association: Ownership required.");
            }
        }

        return map;
    }


    private List<ULong> getBaseAccManifestId(ULong accManifestId, Map<ULong, List<AccManifestRecord>> baseAccMap) {
        List<ULong> result = new ArrayList<>();
        result.add(accManifestId);
        if (baseAccMap.containsKey(accManifestId)) {
            baseAccMap.get(accManifestId).forEach(e -> {
                result.addAll(getBaseAccManifestId(e.getAccManifestId(), baseAccMap));
            });
        }
        return result;
    }
}
