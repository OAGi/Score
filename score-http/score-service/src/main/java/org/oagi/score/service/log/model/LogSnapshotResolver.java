package org.oagi.score.service.log.model;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.configuration.AppUserAuthority.DEVELOPER_GRANTED_AUTHORITY;
import static org.oagi.score.service.configuration.AppUserAuthority.END_USER_GRANTED_AUTHORITY;

@Repository
public class LogSnapshotResolver {

    @Autowired
    private DSLContext dslContext;

    public Map<String, Object> getNamespace(ULong namespaceId) {
        if (namespaceId == null || namespaceId.longValue() <= 0L) {
            return new HashMap();
        }

        NamespaceRecord namespaceRecord = dslContext.selectFrom(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(namespaceId))
                .fetchOptional().orElse(null);
        if (namespaceRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("uri", namespaceRecord.getUri());
        userProperties.put("standard", (byte) 1 == namespaceRecord.getIsStdNmsp());
        return userProperties;
    }

    public Map<String, Object> getUser(ULong userId) {
        if (userId == null || userId.longValue() <= 0L) {
            return new HashMap();
        }

        AppUserRecord userRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(userId))
                .fetchOptional().orElse(null);
        if (userRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("username", userRecord.getLoginId());
        userProperties.put("roles", Arrays.asList(((byte) 1 == userRecord.getIsDeveloper()) ?
                DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY));
        return userProperties;
    }

    public Map<String, Object> getAccByManifestId(ULong accManifestId) {
        if (accManifestId == null || accManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        Record3<String, String, String> record =
                dslContext.select(ACC.GUID, ACC.OBJECT_CLASS_TERM, ACC_MANIFEST.DEN)
                        .from(ACC)
                        .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                        .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestId))
                        .fetchOptional().orElse(null);
        if (record == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", record.get(ACC.GUID));
        userProperties.put("objectClassTerm", record.get(ACC.OBJECT_CLASS_TERM));
        userProperties.put("den", record.get(ACC_MANIFEST.DEN));
        return userProperties;
    }

    public Map<String, Object> getAsccpByManifestId(ULong asccpManifestId) {
        if (asccpManifestId == null || asccpManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        Record3<String, String, String> record =
                dslContext.select(ASCCP.GUID, ASCCP.PROPERTY_TERM, ASCCP_MANIFEST.DEN)
                        .from(ASCCP)
                        .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestId))
                        .fetchOptional().orElse(null);
        if (record == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", record.get(ASCCP.GUID));
        userProperties.put("propertyTerm", record.get(ASCCP.PROPERTY_TERM));
        userProperties.put("den", record.get(ASCCP_MANIFEST.DEN));
        return userProperties;
    }

    public Map<String, Object> getBccpByManifestId(ULong bccpManifestId) {
        if (bccpManifestId == null || bccpManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        Record3<String, String, String> record =
                dslContext.select(BCCP.GUID, BCCP.PROPERTY_TERM, BCCP_MANIFEST.DEN)
                        .from(BCCP)
                        .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestId))
                        .fetchOptional().orElse(null);
        if (record == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", record.get(BCCP.GUID));
        userProperties.put("propertyTerm", record.get(BCCP.PROPERTY_TERM));
        userProperties.put("den", record.get(BCCP_MANIFEST.DEN));
        return userProperties;
    }

    public Map<String, Object> getDtByManifestId(ULong dtManifestId) {
        if (dtManifestId == null || dtManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        Record3<String, String, String> record =
                dslContext.select(DT.GUID, DT.DATA_TYPE_TERM, DT_MANIFEST.DEN)
                        .from(DT)
                        .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                        .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestId))
                        .fetchOptional().orElse(null);
        if (record == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", record.get(DT.GUID));
        userProperties.put("dataTypeTerm", record.get(DT.DATA_TYPE_TERM));
        userProperties.put("den", record.get(DT_MANIFEST.DEN));
        return userProperties;
    }

    public Map<String, Object> getDtSc(ULong dtScId) {
        if (dtScId == null || dtScId.longValue() <= 0L) {
            return new HashMap();
        }

        DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(dtScId))
                .fetchOptional().orElse(null);
        if (dtScRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", dtScRecord.getGuid());
        userProperties.put("propertyTerm", dtScRecord.getPropertyTerm());
        userProperties.put("representationTerm", dtScRecord.getRepresentationTerm());
        return userProperties;
    }

    public Map<String, Object> getCodeListByCodeListManifestId(ULong codeListManifestId) {
        if (codeListManifestId == null || codeListManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        CodeListRecord codeListRecord = dslContext.select(CODE_LIST.fields())
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestId))
                .fetchOptionalInto(CodeListRecord.class).orElse(null);
        if (codeListRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", codeListRecord.getGuid());
        userProperties.put("name", codeListRecord.getName());
        userProperties.put("listId", codeListRecord.getListId());
        userProperties.put("agencyIdListValue", getAgencyIdListValueByAgencyIdListValueManifestId(
                dslContext.select(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(CODE_LIST_MANIFEST)
                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(codeListManifestId))
                        .fetchOneInto(ULong.class)
        ));
        userProperties.put("versionId", codeListRecord.getVersionId());
        return userProperties;
    }

    public Map<String, Object> getAgencyIdList(ULong agencyIdListId) {
        if (agencyIdListId == null || agencyIdListId.longValue() <= 0L) {
            return new HashMap();
        }

        AgencyIdListRecord agencyIdListRecord = dslContext.selectFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListId))
                .fetchOptional().orElse(null);
        if (agencyIdListRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", agencyIdListRecord.getGuid());
        userProperties.put("name", agencyIdListRecord.getName());
        userProperties.put("listId", agencyIdListRecord.getListId());
        userProperties.put("versionId", agencyIdListRecord.getVersionId());
        return userProperties;
    }

    public Map<String, Object> getAgencyIdListValueByAgencyIdListValueManifestId(
            ULong agencyIdListValueManifestId) {
        if (agencyIdListValueManifestId == null || agencyIdListValueManifestId.longValue() <= 0L) {
            return new HashMap();
        }

        AgencyIdListValueRecord agencyIdListValueRecord = dslContext.select(AGENCY_ID_LIST_VALUE.fields())
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(agencyIdListValueManifestId))
                .fetchOptionalInto(AgencyIdListValueRecord.class).orElse(null);
        if (agencyIdListValueRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", agencyIdListValueRecord.getGuid());
        userProperties.put("value", agencyIdListValueRecord.getValue());
        userProperties.put("name", agencyIdListValueRecord.getName());
        return userProperties;
    }

    public Map<String, Object> getXbt(ULong xbtId) {
        if (xbtId == null || xbtId.longValue() <= 0L) {
            return new HashMap();
        }

        XbtRecord xbtRecord = dslContext.selectFrom(XBT)
                .where(XBT.XBT_ID.eq(xbtId))
                .fetchOptional().orElse(null);
        if (xbtRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", xbtRecord.getGuid());
        userProperties.put("name", xbtRecord.getName());
        return userProperties;
    }
}
