package org.oagi.score.service.log.model;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
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

    public Map<String, Object> getAcc(ULong accId) {
        if (accId == null || accId.longValue() <= 0L) {
            return new HashMap();
        }

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accId))
                .fetchOptional().orElse(null);
        if (accRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", accRecord.getGuid());
        userProperties.put("objectClassTerm", accRecord.getObjectClassTerm());
        userProperties.put("den", accRecord.getDen());
        return userProperties;
    }

    public Map<String, Object> getAsccp(ULong asccpId) {
        if (asccpId == null || asccpId.longValue() <= 0L) {
            return new HashMap();
        }

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpId))
                .fetchOptional().orElse(null);
        if (asccpRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", asccpRecord.getGuid());
        userProperties.put("propertyTerm", asccpRecord.getPropertyTerm());
        userProperties.put("den", asccpRecord.getDen());
        return userProperties;
    }

    public Map<String, Object> getBccp(ULong bccpId) {
        if (bccpId == null || bccpId.longValue() <= 0L) {
            return new HashMap();
        }

        BccpRecord bccpRecord = dslContext.selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(bccpId))
                .fetchOptional().orElse(null);
        if (bccpRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", bccpRecord.getGuid());
        userProperties.put("propertyTerm", bccpRecord.getPropertyTerm());
        userProperties.put("den", bccpRecord.getDen());
        return userProperties;
    }

    public Map<String, Object> getDt(ULong dtId) {
        if (dtId == null || dtId.longValue() <= 0L) {
            return new HashMap();
        }

        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtId))
                .fetchOptional().orElse(null);
        if (dtRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", dtRecord.getGuid());
        userProperties.put("dataTypeTerm", dtRecord.getDataTypeTerm());
        userProperties.put("den", dtRecord.getDen());
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

    public Map<String, Object> getCodeList(ULong codeListId) {
        if (codeListId == null || codeListId.longValue() <= 0L) {
            return new HashMap();
        }

        CodeListRecord codeListRecord = dslContext.selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListId))
                .fetchOptional().orElse(null);
        if (codeListRecord == null) {
            return new HashMap();
        }

        Map<String, Object> userProperties = new HashMap();
        userProperties.put("guid", codeListRecord.getGuid());
        userProperties.put("name", codeListRecord.getName());
        userProperties.put("listId", codeListRecord.getListId());
        userProperties.put("agencyIdListValueId", getAgencyIdListValue(codeListRecord.getAgencyIdListValueId()));
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

    public Map<String, Object> getAgencyIdListValue(ULong agencyIdListValueId) {
        if (agencyIdListValueId == null || agencyIdListValueId.longValue() <= 0L) {
            return new HashMap();
        }

        AgencyIdListValueRecord agencyIdListValueRecord = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(agencyIdListValueId))
                .fetchOptional().orElse(null);
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
