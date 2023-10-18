package org.oagi.score.repo;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBccpNode;
import org.oagi.score.gateway.http.api.info.data.SummaryCc;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExt;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.log.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Ascc.ASCC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Bcc.BCC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Bccp.BCCP;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;

@Repository
public class CoreComponentRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    public AccManifestRecord getAccManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public List<AccManifestRecord> getAccManifestByBasedAccManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(manifestId))
                .fetch();
    }

    public AsccpManifestRecord getAsccpManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public List<AsccpManifestRecord> getAsccpManifestByRolOfAccManifestId(ULong roleOfAccManifestId) {
        if (roleOfAccManifestId == null || roleOfAccManifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(roleOfAccManifestId))
                .fetch();
    }

    public BccpManifestRecord getBccpManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public AsccManifestRecord getAsccManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public List<AsccManifestRecord> getAsccManifestByFromAccManifestId(ULong accManifestId) {
        if (accManifestId == null || accManifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestId))
                .fetch();
    }

    public List<AsccManifestRecord> getAsccManifestByToAsccpManifestId(ULong asccpManifestId) {
        if (asccpManifestId == null || asccpManifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestId))
                .fetch();
    }

    public BccManifestRecord getBccManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public List<BccManifestRecord> getBccManifestByFromAccManifestId(ULong accManifestId) {
        if (accManifestId == null || accManifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestId))
                .fetch();
    }

    public List<BccManifestRecord> getBccManifestByToBccpManifestId(ULong bccpManifestId) {
        if (bccpManifestId == null || bccpManifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(bccpManifestId))
                .fetch();
    }

    public DtManifestRecord getDtManifestByManifestId(ULong manifestId) {
        if (manifestId == null || manifestId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(manifestId))
                .fetchOptional().orElse(null);
    }

    public DtManifestRecord getBdtManifestByBdtId(ULong bdtId, ULong releaseId) {
        if (bdtId == null || bdtId.longValue() <= 0L || releaseId == null || releaseId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(DT_MANIFEST)
                .where(and(DT_MANIFEST.DT_ID.eq(bdtId), DT_MANIFEST.RELEASE_ID.eq(releaseId)))
                .fetchOptional().orElse(null);
    }

    public AccRecord getAccById(ULong accId) {
        if (accId == null || accId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accId))
                .fetchOptional().orElse(null);
    }

    public AsccRecord getAsccById(ULong asccId) {
        if (asccId == null || asccId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.eq(asccId))
                .fetchOptional().orElse(null);
    }

    public BccRecord getBccById(ULong bccId) {
        if (bccId == null || bccId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.eq(bccId))
                .fetchOptional().orElse(null);
    }

    public AsccpRecord getAsccpById(ULong asccpId) {
        if (asccpId == null || asccpId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpId))
                .fetchOptional().orElse(null);
    }

    public BccpRecord getBccpById(ULong bccpId) {
        if (bccpId == null || bccpId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(bccpId))
                .fetchOptional().orElse(null);
    }

    public DtRecord getBdtById(ULong bdtId) {
        if (bdtId == null || bdtId.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(bdtId))
                .fetchOptional().orElse(null);
    }

    public CcBccpNode getBccpNodeByBccpId(AuthenticatedPrincipal user, long bccpId) {
        return dslContext.select(
                BCCP.BCCP_ID,
                BCCP.GUID,
                BCCP.PROPERTY_TERM.as("name"),
                BCCP.STATE,
                BCCP.BDT_ID,
                BCCP.OWNER_USER_ID,
                BCCP.PREV_BCCP_ID,
                BCCP.NEXT_BCCP_ID)
                .from(BCCP)
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(CcBccpNode.class);
    }

    public List<SummaryCc> getSummaryCcList(AppUser requester) {
        List<SummaryCc> unionOfSummaryCcList = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID.as("manifestId"),
                        inline("ACC").as("type"),
                        ACC.LAST_UPDATE_TIMESTAMP,
                        ACC.STATE,
                        ACC.OWNER_USER_ID,
                        APP_USER.LOGIN_ID.as("ownerUsername"),
                        ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .where(and(RELEASE.RELEASE_NUM.eq("Working"), ACC.STATE.in("WIP", "Draft", "Candidate")))
                .union(dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifestId"),
                                inline("ASCCP").as("type"),
                                ASCCP.LAST_UPDATE_TIMESTAMP,
                                ASCCP.STATE,
                                ASCCP.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                ASCCP_MANIFEST.DEN)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(RELEASE.RELEASE_NUM.eq("Working"), ASCCP.STATE.in("WIP", "Draft", "Candidate"))))
                .union(dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifestId"),
                                inline("BCCP").as("type"),
                                BCCP.LAST_UPDATE_TIMESTAMP,
                                BCCP.STATE,
                                BCCP.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                BCCP_MANIFEST.DEN)
                        .from(BCCP_MANIFEST)
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(RELEASE.RELEASE_NUM.eq("Working"), BCCP.STATE.in("WIP", "Draft", "Candidate"))))
                .union(dslContext.select(DT_MANIFEST.DT_MANIFEST_ID.as("manifestId"),
                                inline("BDT").as("type"),
                                DT.LAST_UPDATE_TIMESTAMP,
                                DT.STATE,
                                DT.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                DT_MANIFEST.DEN)
                        .from(DT_MANIFEST)
                        .join(DT).on(and(DT_MANIFEST.DT_ID.eq(DT.DT_ID), DT_MANIFEST.BASED_DT_MANIFEST_ID.isNotNull()))
                        .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(RELEASE.RELEASE_NUM.eq("Working"), DT.STATE.in("WIP", "Draft", "Candidate"))))
                .fetchInto(SummaryCc.class);

        return unionOfSummaryCcList;
    }

    public List<SummaryCcExt> getSummaryCcExtList(BigInteger releaseId) {
        List<ULong> uegAccIds;
        if (releaseId.longValue() > 0) {
            uegAccIds = dslContext.select(max(ACC.ACC_ID).as("id"))
                            .from(ACC)
                            .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                            .where(and(
                                    ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                                    ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))
                            ))
                            .groupBy(ACC.GUID)
                            .fetchInto(ULong.class);

        } else {
            uegAccIds = dslContext.select(max(ACC.ACC_ID).as("id"))
                            .from(ACC)
                            .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                            .where(and(
                                    ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                                    ACC_MANIFEST.RELEASE_ID.greaterThan(ULong.valueOf(0))
                            ))
                            .groupBy(ACC.GUID)
                            .fetchInto(ULong.class);
        }

        return dslContext.select(ACC.ACC_ID,
                ACC.OBJECT_CLASS_TERM,
                ACC.STATE,
                ACC.OWNER_USER_ID,
                APP_USER.LOGIN_ID)
                .from(ACC)
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .where(ACC.ACC_ID.in(uegAccIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(ACC.ACC_ID).toBigInteger());
                    item.setObjectClassTerm(e.get(ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(ACC.STATE)));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(ACC.OWNER_USER_ID).toBigInteger());
                    return item;
                }).collect(Collectors.toList());
    }

    public BigInteger getGlobalExtensionAccManifestId(BigInteger extensionAccManifestId) {
        ULong releaseId = dslContext.select(ACC_MANIFEST.RELEASE_ID)
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(extensionAccManifestId)))
                .fetchOneInto(ULong.class);

        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC)
                .on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(releaseId),
                        ACC.OBJECT_CLASS_TERM.eq("All Extension")
                ))
                .fetchOneInto(BigInteger.class);
    }
}
