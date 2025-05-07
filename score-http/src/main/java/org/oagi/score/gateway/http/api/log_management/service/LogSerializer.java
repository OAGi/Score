package org.oagi.score.gateway.http.api.log_management.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueDetailsRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class LogSerializer {

    private final Gson gson;

    private DSLContext dslContext;

    private LogSnapshotResolver resolver;

    private RepositoryFactory repositoryFactory;

    public LogSerializer(DSLContext dslContext,
                         LogSnapshotResolver resolver,
                         RepositoryFactory repositoryFactory) {
        this.gson = new Gson();
        this.dslContext = dslContext;
        this.resolver = resolver;
        this.repositoryFactory = repositoryFactory;
    }

    public String serialize(ScoreUser requester, AccDetailsRecord acc) {

        Map<String, Object> properties = new HashMap();

        properties.put("component", "acc");
        properties.put("guid", acc.guid().value());
        properties.put("objectClassTerm", acc.objectClassTerm());
        properties.put("objectClassQualifier", acc.objectClassQualifier());
        if (acc.definition() != null) {
            properties.put("definition", acc.definition().content());
            properties.put("definitionSource", acc.definition().source());
        }
        properties.put("componentType", acc.componentType().name());
        properties.put("state", acc.state().name());
        properties.put("deprecated", acc.deprecated());
        properties.put("abstract", acc.isAbstract());

        properties.put("basedAcc", resolver.getAcc(acc.based()));
        properties.put("ownerUser", resolver.getUser(acc.owner()));
        properties.put("namespace", resolver.getNamespace(acc.namespace()));

        List<Map<String, Object>> associations = new ArrayList();
        properties.put("associations", associations);

        var query = repositoryFactory.accQueryRepository(requester);
        List<CcAssociation> assocList = query.getAssociationSummaryList(acc.accManifestId());
        for (CcAssociation assoc : assocList) {
            if (assoc.isAscc()) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) assoc;
                AsccDetailsRecord asccDetails = query.getAsccDetails(ascc.asccManifestId());
                associations.add(serialize(asccDetails));
            } else {
                BccSummaryRecord bcc = (BccSummaryRecord) assoc;
                BccDetailsRecord bccDetails = query.getBccDetails(bcc.bccManifestId());
                associations.add(serialize(bccDetails));
            }
        }

        properties.put("_metadata", toMetadata(acc, assocList));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(AccDetailsRecord acc, List<CcAssociation> assocList) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("accManifest", toMetadata(
                dslContext.selectFrom(ACC_MANIFEST)
                        .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(acc.accManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("acc", toMetadata(
                dslContext.selectFrom(ACC)
                        .where(ACC.ACC_ID.eq(ULong.valueOf(acc.accId().value())))
                        .fetchOne()
        ));
        List<Map<String, Object>> associations = new ArrayList();
        metadata.put("associations", associations);
        for (CcAssociation assoc : assocList) {
            Map<String, Object> assocMetadata = new HashMap();
            if (assoc.isAscc()) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) assoc;
                assocMetadata.put("asccManifest", toMetadata(
                        dslContext.selectFrom(ASCC_MANIFEST)
                                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(ascc.asccManifestId().value())))
                                .fetchOne()
                ));
                assocMetadata.put("ascc", toMetadata(
                        dslContext.selectFrom(ASCC)
                                .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.asccId().value())))
                                .fetchOne()
                ));
                assocMetadata.put("seqKey", toMetadata(
                        dslContext.selectFrom(SEQ_KEY)
                                .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(ascc.seqKeyId().value())))
                                .fetchOne()
                ));
            } else {
                BccSummaryRecord bcc = (BccSummaryRecord) assoc;
                assocMetadata.put("bccManifest", toMetadata(
                        dslContext.selectFrom(BCC_MANIFEST)
                                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bcc.bccManifestId().value())))
                                .fetchOne()
                ));
                assocMetadata.put("bcc", toMetadata(
                        dslContext.selectFrom(BCC)
                                .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.bccId().value())))
                                .fetchOne()
                ));
                assocMetadata.put("seqKey", toMetadata(
                        dslContext.selectFrom(SEQ_KEY)
                                .where(SEQ_KEY.SEQ_KEY_ID.eq(ULong.valueOf(bcc.seqKeyId().value())))
                                .fetchOne()
                ));
            }
            associations.add(assocMetadata);
        }

        return metadata;
    }

    private Map<String, Object> toMetadata(Record record) {
        if (record == null) {
            return new HashMap();
        }
        Map<String, Object> properties = new HashMap();
        for (Field field : record.fields()) {
            String name = field.getName();
            // ignore deprecated columns
            if ("seq_key".equals(name)) {
                continue;
            }
            Object value = record.getValue(field);
            if (value instanceof Byte) {
                properties.put(name, (byte) 1 == (Byte) value);
            } else if (value instanceof ULong) {
                properties.put(name, ((ULong) value).toBigInteger());
            } else if (value instanceof LocalDateTime) {
                properties.put(name, ((LocalDateTime) value).format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                properties.put(name, value);
            }
        }
        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public Map<String, Object> serialize(AsccDetailsRecord ascc) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "ascc");
        properties.put("guid", ascc.guid().value());
        properties.put("cardinalityMin", ascc.cardinality().min());
        properties.put("cardinalityMax", ascc.cardinality().max());
        if (ascc.definition() != null) {
            properties.put("definition", ascc.definition().content());
            properties.put("definitionSource", ascc.definition().source());
        }
        properties.put("deprecated", ascc.deprecated());
        properties.put("toAsccp", resolver.getAsccp(ascc.toAsccp()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public Map<String, Object> serialize(BccDetailsRecord bcc) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "bcc");
        properties.put("guid", bcc.guid().value());
        properties.put("cardinalityMin", bcc.cardinality().min());
        properties.put("cardinalityMax", bcc.cardinality().max());
        properties.put("entityType", bcc.entityType().name());
        if (bcc.definition() != null) {
            properties.put("definition", bcc.definition().content());
            properties.put("definitionSource", bcc.definition().source());
        }
        if (bcc.valueConstraint() != null) {
            properties.put("defaultValue", bcc.valueConstraint().defaultValue());
            properties.put("fixedValue", bcc.valueConstraint().fixedValue());
        }
        properties.put("deprecated", bcc.deprecated());
        properties.put("nillable", bcc.nillable());
        properties.put("toBccp", resolver.getBccp(bcc.toBccp()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(ScoreUser requester, AsccpDetailsRecord asccp) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "asccp");
        properties.put("guid", asccp.guid().value());
        properties.put("propertyTerm", asccp.propertyTerm());
        if (asccp.definition() != null) {
            properties.put("definition", asccp.definition().content());
            properties.put("definitionSource", asccp.definition().source());
        }
        properties.put("state", asccp.state().name());
        properties.put("reusable", asccp.reusable());
        properties.put("deprecated", asccp.deprecated());
        properties.put("nillable", asccp.nillable());

        properties.put("roleOfAcc", resolver.getAcc(asccp.roleOfAcc()));
        properties.put("ownerUser", resolver.getUser(asccp.owner()));
        properties.put("namespace", resolver.getNamespace(asccp.namespace()));

        properties.put("_metadata", toMetadata(asccp));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(AsccpDetailsRecord asccp) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("asccpManifest", toMetadata(
                dslContext.selectFrom(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccp.asccpManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("asccp", toMetadata(
                dslContext.selectFrom(ASCCP)
                        .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccp.asccpId().value())))
                        .fetchOne()
        ));

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(ScoreUser requester, BccpDetailsRecord bccp) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "bccp");
        properties.put("guid", bccp.guid().value());
        properties.put("propertyTerm", bccp.propertyTerm());
        properties.put("representationTerm", bccp.representationTerm());
        if (bccp.definition() != null) {
            properties.put("definition", bccp.definition().content());
            properties.put("definitionSource", bccp.definition().source());
        }
        if (bccp.valueConstraint() != null) {
            properties.put("defaultValue", bccp.valueConstraint().defaultValue());
            properties.put("fixedValue", bccp.valueConstraint().fixedValue());
        }
        properties.put("state", bccp.state().name());
        properties.put("deprecated", bccp.deprecated());
        properties.put("nillable", bccp.nillable());

        properties.put("bdt", resolver.getDt(bccp.dt()));
        properties.put("ownerUser", resolver.getUser(bccp.owner()));
        properties.put("namespace", resolver.getNamespace(bccp.namespace()));

        properties.put("_metadata", toMetadata(bccp));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(BccpDetailsRecord bccp) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("bccpManifest", toMetadata(
                dslContext.selectFrom(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccp.bccpManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("bccp", toMetadata(
                dslContext.selectFrom(BCCP)
                        .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccp.bccpId().value())))
                        .fetchOne()
        ));

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(ScoreUser requester, DtDetailsRecord dt) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "dt");
        properties.put("guid", dt.guid().value());
        properties.put("representationTerm", dt.representationTerm());
        properties.put("dataTypeTerm", dt.dataTypeTerm());
        properties.put("qualifier", dt.qualifier());
        if (dt.definition() != null) {
            properties.put("definition", dt.definition().content());
            properties.put("definitionSource", dt.definition().source());
        }
        properties.put("contentComponentDefinition", dt.contentComponentDefinition());
        properties.put("sixDigitId", dt.sixDigitId());
        properties.put("state", dt.state().name());
        properties.put("deprecated", dt.deprecated());

        properties.put("basedDt", resolver.getDt(dt.based()));
        properties.put("ownerUser", resolver.getUser(dt.owner()));
        properties.put("namespace", resolver.getNamespace(dt.namespace()));

        List<Map<String, Object>> supplementaryComponents = new ArrayList();
        properties.put("supplementaryComponents", supplementaryComponents);

        var query = repositoryFactory.dtQueryRepository(requester);
        List<DtScDetailsRecord> dtScList = query.getDtScDetailsList(dt.dtManifestId());
        for (DtScDetailsRecord dtSc : dtScList) {
            supplementaryComponents.add(serialize(dtSc));
        }

        properties.put("_metadata", toMetadata(dt, dtScList));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(DtDetailsRecord dt, List<DtScDetailsRecord> dtScList) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("dtManifest", toMetadata(
                dslContext.selectFrom(DT_MANIFEST)
                        .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dt.dtManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("dt", toMetadata(
                dslContext.selectFrom(DT)
                        .where(DT.DT_ID.eq(ULong.valueOf(dt.dtId().value())))
                        .fetchOne()
        ));
        List<Map<String, Object>> supplementaryComponents = new ArrayList();
        metadata.put("supplementaryComponents", supplementaryComponents);

        for (DtScDetailsRecord dtSc : dtScList) {
            Map<String, Object> dtScMetadata = new HashMap();
            dtScMetadata.put("dtScManifest", toMetadata(
                    dslContext.selectFrom(DT_SC_MANIFEST)
                            .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtSc.dtScManifestId().value())))
                            .fetchOne()
            ));
            dtScMetadata.put("dtSc", toMetadata(
                    dslContext.selectFrom(DT_SC)
                            .where(DT_SC.DT_SC_ID.eq(ULong.valueOf(dtSc.dtScId().value())))
                            .fetchOne()
            ));
            supplementaryComponents.add(dtScMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(DtScDetailsRecord dtSc) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "dtSc");
        properties.put("guid", dtSc.guid().value());
        properties.put("propertyTerm", dtSc.propertyTerm());
        properties.put("representationTerm", dtSc.representationTerm());
        properties.put("cardinalityMin", dtSc.cardinality().min());
        properties.put("cardinalityMax", dtSc.cardinality().max());
        if (dtSc.valueConstraint() != null) {
            properties.put("defaultValue", dtSc.valueConstraint().defaultValue());
            properties.put("fixedValue", dtSc.valueConstraint().fixedValue());
        }
        if (dtSc.definition() != null) {
            properties.put("definition", dtSc.definition().content());
            properties.put("definitionSource", dtSc.definition().source());
        }
        properties.put("basedDtSc", resolver.getDtSc(dtSc.based()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(ScoreUser requester, CodeListDetailsRecord codeList) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "codeList");
        properties.put("guid", codeList.guid().value());
        properties.put("name", codeList.name());
        properties.put("listId", codeList.listId());
        properties.put("agencyId", resolver.getAgencyIdListValue(codeList.agencyIdListValue()));
        properties.put("versionId", codeList.versionId());
        properties.put("remark", codeList.remark());
        if (codeList.definition() != null) {
            properties.put("definition", codeList.definition().content());
            properties.put("definitionSource", codeList.definition().source());
        }
        properties.put("state", codeList.state().name());
        properties.put("deprecated", codeList.deprecated());
        properties.put("extensible", codeList.extensible());
        properties.put("basedCodeList", resolver.getCodeList(requester, codeList.based(), codeList.agencyIdListValue()));
        properties.put("ownerUser", resolver.getUser(codeList.owner()));
        properties.put("namespace", resolver.getNamespace(codeList.namespace()));

        List<Map<String, Object>> values = new ArrayList();
        properties.put("values", values);

        for (CodeListValueDetailsRecord codeListValue : codeList.valueList()) {
            values.add(serialize(codeListValue));
        }

        properties.put("_metadata", toMetadata(codeList));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(CodeListDetailsRecord codeList) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("codeListManifest", toMetadata(
                dslContext.selectFrom(CODE_LIST_MANIFEST)
                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeList.codeListManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("codeList", toMetadata(
                dslContext.selectFrom(CODE_LIST)
                        .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeList.codeListId().value())))
                        .fetchOne()
        ));
        List<Map<String, Object>> values = new ArrayList();
        metadata.put("values", values);

        Map<ULong, CodeListValueRecord> codeListValueRecordMap = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        codeList.valueList().stream().map(e -> ULong.valueOf(e.codeListValueId().value())).collect(Collectors.toSet())
                ))
                .fetch().stream().collect(
                        Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));
        for (CodeListValueManifestRecord codeListValueManifestRecord : dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.in(
                        codeList.valueList().stream().map(e -> ULong.valueOf(e.codeListValueManifestId().value())).collect(Collectors.toSet())
                )).fetch()) {
            CodeListValueRecord codeListValueRecord = codeListValueRecordMap.get(codeListValueManifestRecord.getCodeListValueId());
            Map<String, Object> codeListValueMetadata = new HashMap();
            codeListValueMetadata.put("codeListValueManifest", toMetadata(codeListValueManifestRecord));
            codeListValueMetadata.put("codeListValue", toMetadata(codeListValueRecord));
            values.add(codeListValueMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(CodeListValueDetailsRecord codeListValue) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "codeListValue");
        properties.put("guid", codeListValue.guid().value());
        properties.put("value", codeListValue.value());
        properties.put("meaning", codeListValue.meaning());
        if (codeListValue.definition() != null) {
            properties.put("definition", codeListValue.definition().content());
            properties.put("definitionSource", codeListValue.definition().source());
        }
        properties.put("deprecated", codeListValue.deprecated());

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(ScoreUser requester, AgencyIdListDetailsRecord agencyIdListDetails) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "agencyIdList");
        properties.put("guid", agencyIdListDetails.guid().value());
        properties.put("enumTypeGuid", agencyIdListDetails.enumTypeGuid());
        properties.put("agencyIdListValue", resolver.getAgencyIdListValue(
                agencyIdListDetails.agencyIdListValue()));
        properties.put("name", agencyIdListDetails.name());
        properties.put("listId", agencyIdListDetails.listId());
        properties.put("versionId", agencyIdListDetails.versionId());
        if (agencyIdListDetails.definition() != null) {
            properties.put("definition", agencyIdListDetails.definition().content());
            properties.put("definitionSource", agencyIdListDetails.definition().source());
        }
        properties.put("state", agencyIdListDetails.state().name());
        properties.put("deprecated", agencyIdListDetails.deprecated());

        properties.put("basedAgencyIdList", resolver.getAgencyIdList(agencyIdListDetails.based()));
        properties.put("ownerUser", resolver.getUser(agencyIdListDetails.owner()));
        properties.put("namespace", resolver.getNamespace(agencyIdListDetails.namespace()));

        List<Map<String, Object>> values = new ArrayList();
        properties.put("values", values);

        for (AgencyIdListValueDetailsRecord agencyIdListValue : agencyIdListDetails.valueList()) {
            values.add(serialize(agencyIdListValue));
        }

        properties.put("_metadata", toMetadata(agencyIdListDetails));

        return gson.toJson(properties, HashMap.class);
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(AgencyIdListValueDetailsRecord agencyIdListValue) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "agencyIdListValue");
        properties.put("guid", agencyIdListValue.guid().value());
        properties.put("value", agencyIdListValue.value());
        properties.put("name", agencyIdListValue.name());
        if (agencyIdListValue.definition() != null) {
            properties.put("definition", agencyIdListValue.definition().content());
            properties.put("definitionSource", agencyIdListValue.definition().source());
        }
        properties.put("deprecated", agencyIdListValue.deprecated());

        return properties;
    }

    private Map<String, Object> toMetadata(AgencyIdListDetailsRecord agencyIdList) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("agencyIdListManifest", toMetadata(
                dslContext.selectFrom(AGENCY_ID_LIST_MANIFEST)
                        .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdList.agencyIdListManifestId().value())))
                        .fetchOne()
        ));
        metadata.put("agencyIdList", toMetadata(
                dslContext.selectFrom(AGENCY_ID_LIST)
                        .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(agencyIdList.agencyIdListId().value())))
                        .fetchOne()
        ));
        List<Map<String, Object>> values = new ArrayList();
        metadata.put("values", values);

        Map<ULong, AgencyIdListValueRecord> agencyIdListValueRecordMap = dslContext.selectFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.in(
                        agencyIdList.valueList().stream().map(e -> ULong.valueOf(e.agencyIdListValueId().value())).collect(Collectors.toSet())
                ))
                .fetch().stream().collect(
                        Collectors.toMap(AgencyIdListValueRecord::getAgencyIdListValueId, Function.identity()));
        for (AgencyIdListValueManifestRecord agencyIdListValueManifestRecord : dslContext.selectFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(
                        agencyIdList.valueList().stream().map(e -> ULong.valueOf(e.agencyIdListValueManifestId().value())).collect(Collectors.toSet())
                )).fetch()) {
            AgencyIdListValueRecord agencyIdListValueRecord = agencyIdListValueRecordMap.get(agencyIdListValueManifestRecord.getAgencyIdListValueId());
            Map<String, Object> agencyIdListValueMetadata = new HashMap();
            agencyIdListValueMetadata.put("agencyIdListValueManifest", toMetadata(agencyIdListValueManifestRecord));
            agencyIdListValueMetadata.put("agencyIdListValue", toMetadata(agencyIdListValueRecord));
            values.add(agencyIdListValueMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(XbtManifestRecord xbtManifestRecord, XbtRecord xbtRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "xbt");
        properties.put("guid", xbtRecord.getGuid());
        properties.put("name", xbtRecord.getName());
        properties.put("builtInType", xbtRecord.getBuiltinType());
        properties.put("revisionDoc", xbtRecord.getRevisionDoc());
        properties.put("schemaDefinition", xbtRecord.getSchemaDefinition());
        properties.put("jbtDraft05Map", xbtRecord.getJbtDraft05Map());
        properties.put("openapi30Map", xbtRecord.getOpenapi30Map());
        properties.put("state", xbtRecord.getState());
        properties.put("deprecated", (byte) 1 == xbtRecord.getIsDeprecated());

        properties.put("subTypeOfXbt", resolver.getXbt(xbtRecord.getSubtypeOfXbtId()));
//        properties.put("ownerUser", resolver.getUser(xbtRecord.getOwnerUserId()));

        properties.put("_metadata", toMetadata(xbtManifestRecord, xbtRecord));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(XbtManifestRecord xbtManifestRecord, XbtRecord xbtRecord) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("xbtManifest", toMetadata(xbtManifestRecord));
        metadata.put("xbt", toMetadata(xbtRecord));

        return metadata;
    }

    @SneakyThrows()
    public JsonObject deserialize(String snapshot) {
        if (!StringUtils.hasLength(snapshot)) {
            return null;
        }

        JsonObject properties = this.gson.fromJson(snapshot, JsonObject.class);
        String component = properties.get("component").getAsString();
        if (!StringUtils.hasLength(component)) {
            return properties;
        }

        return properties;
    }

    public ULong getSnapshotId(JsonElement obj) {
        if (obj != null && !obj.isJsonNull()) {
            return obj.getAsJsonObject().isJsonNull() ? null :
                    ULong.valueOf(obj.getAsJsonObject().get("value").getAsBigInteger());
        }
        return null;
    }

    public String getSnapshotString(JsonElement obj) {
        if (obj != null && !obj.isJsonNull()) {
            return obj.getAsString().isEmpty() ? "" : obj.getAsString();
        }
        return "";
    }

    public Byte getSnapshotByte(JsonElement obj) {
        if (obj != null && !obj.isJsonNull()) {
            return obj.getAsBoolean() ? (byte) 1 : 0;
        }
        return 0;
    }

}
