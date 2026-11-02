package org.oagi.score.gateway.http.api.log_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.oagi.score.gateway.http.api.account_management.model.AppUserAuthority.DEVELOPER_GRANTED_AUTHORITY;
import static org.oagi.score.gateway.http.api.account_management.model.AppUserAuthority.END_USER_GRANTED_AUTHORITY;

public class LogSnapshotResolver {

    private DSLContext dslContext;

    private RepositoryFactory repositoryFactory;

    public LogSnapshotResolver(DSLContext dslContext, RepositoryFactory repositoryFactory) {
        this.dslContext = dslContext;
        this.repositoryFactory = repositoryFactory;
    }

    public Map<String, Object> getNamespace(NamespaceSummaryRecord namespace) {
        if (namespace == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("uri", namespace.uri());
        userProperties.put("standard", namespace.standard());
        return userProperties;
    }

    public Map<String, Object> getUser(UserSummaryRecord user) {
        if (user == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("username", user.loginId());
        userProperties.put("roles", Arrays.asList(user.isDeveloper() ?
                DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY));
        return userProperties;
    }

    public Map<String, Object> getAcc(AccSummaryRecord acc) {
        if (acc == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", acc.guid().value());
        userProperties.put("objectClassTerm", acc.objectClassTerm());
        userProperties.put("den", acc.den());
        return userProperties;
    }

    public Map<String, Object> getAsccp(AsccpSummaryRecord asccp) {
        if (asccp == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", asccp.guid().value());
        userProperties.put("propertyTerm", asccp.propertyTerm());
        userProperties.put("den", asccp.den());
        return userProperties;
    }

    public Map<String, Object> getBccp(BccpSummaryRecord bccp) {
        if (bccp == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", bccp.guid().value());
        userProperties.put("propertyTerm", bccp.propertyTerm());
        userProperties.put("den", bccp.den());
        return userProperties;
    }

    public Map<String, Object> getDt(DtSummaryRecord dt) {
        if (dt == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", dt.guid().value());
        userProperties.put("dataTypeTerm", dt.dataTypeTerm());
        userProperties.put("den", dt.den());
        return userProperties;
    }

    public Map<String, Object> getDtSc(DtScSummaryRecord dtSc) {
        if (dtSc == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", dtSc.guid().value());
        userProperties.put("propertyTerm", dtSc.propertyTerm());
        userProperties.put("representationTerm", dtSc.representationTerm());
        return userProperties;
    }

    public Map<String, Object> getCodeList(
            ScoreUser requester, CodeListSummaryRecord codeList, AgencyIdListValueSummaryRecord agencyIdListValue) {
        if (codeList == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", codeList.guid().value());
        userProperties.put("name", codeList.name());
        userProperties.put("listId", codeList.listId());
        userProperties.put("agencyIdListValue", getAgencyIdListValue(agencyIdListValue));
        userProperties.put("versionId", codeList.versionId());
        return userProperties;
    }

    public Map<String, Object> getAgencyIdList(AgencyIdListSummaryRecord agencyIdList) {
        if (agencyIdList == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", agencyIdList.guid().value());
        userProperties.put("name", agencyIdList.name());
        userProperties.put("listId", agencyIdList.listId());
        userProperties.put("versionId", agencyIdList.versionId());
        return userProperties;
    }

    public Map<String, Object> getAgencyIdListValue(
            AgencyIdListValueSummaryRecord agencyIdListValue) {
        if (agencyIdListValue == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", agencyIdListValue.guid().value());
        userProperties.put("value", agencyIdListValue.value());
        userProperties.put("name", agencyIdListValue.name());
        return userProperties;
    }

    public Map<String, Object> getXbt(XbtSummaryRecord xbt) {
        if (xbt == null) {
            return new LinkedHashMap();
        }

        Map<String, Object> userProperties = new LinkedHashMap();
        userProperties.put("guid", xbt.guid().value());
        userProperties.put("name", xbt.name());
        return userProperties;
    }
}
