package org.oagi.score.service.log.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.common.data.BCCEntityType;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LogSerializer {

    private final Gson gson;

    @Autowired
    private LogSnapshotResolver resolver;

    public LogSerializer() {
        this.gson = new Gson();
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(AccManifestRecord accManifestRecord, AccRecord accRecord,
                            List<AsccManifestRecord> asccManifestRecords, List<BccManifestRecord> bccManifestRecords,
                            List<AsccRecord> asccRecords, List<BccRecord> bccRecords,
                            List<SeqKeyRecord> seqKeyRecords) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "acc");
        properties.put("guid", accRecord.getGuid());
        properties.put("objectClassTerm", accRecord.getObjectClassTerm());
        properties.put("objectClassQualifier", accRecord.getObjectClassQualifier());
        properties.put("definition", accRecord.getDefinition());
        properties.put("definitionSource", accRecord.getDefinitionSource());
        properties.put("componentType", OagisComponentType.valueOf(accRecord.getOagisComponentType()).name());
        properties.put("state", accRecord.getState());
        properties.put("deprecated", (byte) 1 == accRecord.getIsDeprecated());
        properties.put("abstract", (byte) 1 == accRecord.getIsAbstract());

        properties.put("basedAcc", resolver.getAccByManifestId(accManifestRecord.getBasedAccManifestId()));
        properties.put("ownerUser", resolver.getUser(accRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(accRecord.getNamespaceId()));

        List<Map<String, Object>> associations = new ArrayList();
        properties.put("associations", associations);

        for (AssocRecord assocRecord : sort(asccManifestRecords, bccManifestRecords, asccRecords, bccRecords, seqKeyRecords)) {
            if (assocRecord.isAssociation()) {
                associations.add(serialize(
                        (AsccManifestRecord) assocRecord.getDelegatedManifest(),
                        (AsccRecord) assocRecord.getDelegatedComponent()));
            } else {
                associations.add(serialize(
                        (BccManifestRecord) assocRecord.getDelegatedManifest(),
                        (BccRecord) assocRecord.getDelegatedComponent()));
            }
        }

        properties.put("_metadata", toMetadata(accManifestRecord, accRecord,
                asccManifestRecords, bccManifestRecords,
                asccRecords, bccRecords,
                seqKeyRecords));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(AccManifestRecord accManifestRecord, AccRecord accRecord,
                                           List<AsccManifestRecord> asccManifestRecords, List<BccManifestRecord> bccManifestRecords,
                                           List<AsccRecord> asccRecords, List<BccRecord> bccRecords,
                                           List<SeqKeyRecord> seqKeyRecords) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("accManifest", toMetadata(accManifestRecord));
        metadata.put("acc", toMetadata(accRecord));
        List<Map<String, Object>> associations = new ArrayList();
        metadata.put("associations", associations);
        for (AssocRecord assocRecord : sort(asccManifestRecords, bccManifestRecords, asccRecords, bccRecords, seqKeyRecords)) {
            Map<String, Object> assocMetadata = new HashMap();
            if (assocRecord.isAssociation()) {
                assocMetadata.put("asccManifest", toMetadata((AsccManifestRecord) assocRecord.getDelegatedManifest()));
                assocMetadata.put("ascc", toMetadata((AsccRecord) assocRecord.getDelegatedComponent()));
            } else {
                assocMetadata.put("bccManifest", toMetadata((BccManifestRecord) assocRecord.getDelegatedManifest()));
                assocMetadata.put("bcc", toMetadata((BccRecord) assocRecord.getDelegatedComponent()));
            }
            assocMetadata.put("seqKey", toMetadata(assocRecord.getSeqKeyRecord()));
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

    private List<AssocRecord> sort(List<AsccManifestRecord> asccManifestRecords, List<BccManifestRecord> bccManifestRecords,
                                   List<AsccRecord> asccRecords, List<BccRecord> bccRecords,
                                   List<SeqKeyRecord> seqKeyRecords) {

        Map<ULong, AsccManifestRecord> asccManifestRecordMap = asccManifestRecords.stream().collect(
                Collectors.toMap(AsccManifestRecord::getAsccManifestId, Function.identity()));
        Map<ULong, BccManifestRecord> bccManifestRecordMap = bccManifestRecords.stream().collect(
                Collectors.toMap(BccManifestRecord::getBccManifestId, Function.identity()));

        Map<ULong, AsccRecord> asccRecordMap = asccRecords.stream().collect(
                Collectors.toMap(AsccRecord::getAsccId, Function.identity()));
        Map<ULong, BccRecord> bccRecordMap = bccRecords.stream().collect(
                Collectors.toMap(BccRecord::getBccId, Function.identity()));

        List<AssocRecord> sortedRecords = new ArrayList();
        if (!seqKeyRecords.isEmpty()) {
            Map<ULong, SeqKeyRecord> seqKeyRecordMap = seqKeyRecords.stream().collect(
                    Collectors.toMap(SeqKeyRecord::getSeqKeyId, Function.identity()));
            SeqKeyRecord node = seqKeyRecords.stream().filter(e -> e.getPrevSeqKeyId() == null).findAny().get();
            while (node != null) {
                if (node.getAsccManifestId() != null) {
                    AsccManifestRecord manifest = asccManifestRecordMap.get(node.getAsccManifestId());
                    AsccRecord component = asccRecordMap.get(manifest.getAsccId());
                    sortedRecords.add(new AssocRecord(manifest, component, node));
                } else {
                    BccManifestRecord manifest = bccManifestRecordMap.get(node.getBccManifestId());
                    BccRecord component = bccRecordMap.get(manifest.getBccId());
                    sortedRecords.add(new AssocRecord(manifest, component, node));
                }
                node = seqKeyRecordMap.get(node.getNextSeqKeyId());
            }
        }

        return sortedRecords;
    }

    private class AssocRecord {

        private final LocalDateTime timestamp;
        private final Object delegatedManifest;
        private final Object delegatedComponent;
        private final SeqKeyRecord seqKeyRecord;
        private final boolean association;

        AssocRecord(AsccManifestRecord manifest, AsccRecord ascc, SeqKeyRecord seqKeyRecord) {
            this.timestamp = ascc.getLastUpdateTimestamp();
            this.delegatedManifest = manifest;
            this.delegatedComponent = ascc;
            this.seqKeyRecord = seqKeyRecord;
            this.association = true;
        }

        AssocRecord(BccManifestRecord manifest, BccRecord bcc, SeqKeyRecord seqKeyRecord) {
            this.timestamp = bcc.getLastUpdateTimestamp();
            this.delegatedManifest = manifest;
            this.delegatedComponent = bcc;
            this.seqKeyRecord = seqKeyRecord;
            this.association = false;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Object getDelegatedManifest() {
            return delegatedManifest;
        }

        public Object getDelegatedComponent() {
            return delegatedComponent;
        }

        public SeqKeyRecord getSeqKeyRecord() {
            return seqKeyRecord;
        }

        public boolean isAssociation() {
            return association;
        }
    }

    @SneakyThrows(JsonIOException.class)
    public Map<String, Object> serialize(AsccManifestRecord asccManifestRecord, AsccRecord asccRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "ascc");
        properties.put("guid", asccRecord.getGuid());
        properties.put("cardinalityMin", asccRecord.getCardinalityMin());
        properties.put("cardinalityMax", asccRecord.getCardinalityMax());
        properties.put("definition", asccRecord.getDefinition());
        properties.put("definitionSource", asccRecord.getDefinitionSource());
        properties.put("deprecated", (byte) 1 == asccRecord.getIsDeprecated());
        properties.put("toAsccp", resolver.getAsccpByManifestId(asccManifestRecord.getToAsccpManifestId()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public Map<String, Object> serialize(BccManifestRecord bccManifestRecord, BccRecord bccRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "bcc");
        properties.put("guid", bccRecord.getGuid());
        properties.put("cardinalityMin", bccRecord.getCardinalityMin());
        properties.put("cardinalityMax", bccRecord.getCardinalityMax());
        properties.put("entityType", BCCEntityType.valueOf(bccRecord.getEntityType()).name());
        properties.put("definition", bccRecord.getDefinition());
        properties.put("definitionSource", bccRecord.getDefinitionSource());
        properties.put("defaultValue", bccRecord.getDefaultValue());
        properties.put("fixedValue", bccRecord.getFixedValue());
        properties.put("deprecated", (byte) 1 == bccRecord.getIsDeprecated());
        properties.put("nillable", (byte) 1 == bccRecord.getIsNillable());
        properties.put("toBccp", resolver.getBccpByManifestId(bccManifestRecord.getToBccpManifestId()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(AsccpManifestRecord asccpManifestRecord, AsccpRecord asccpRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "asccp");
        properties.put("guid", asccpRecord.getGuid());
        properties.put("propertyTerm", asccpRecord.getPropertyTerm());
        properties.put("definition", asccpRecord.getDefinition());
        properties.put("definitionSource", asccpRecord.getDefinitionSource());
        properties.put("state", asccpRecord.getState());
        properties.put("reusable", (byte) 1 == asccpRecord.getReusableIndicator());
        properties.put("deprecated", (byte) 1 == asccpRecord.getIsDeprecated());
        properties.put("nillable", (byte) 1 == asccpRecord.getIsNillable());

        properties.put("roleOfAcc", resolver.getAccByManifestId(asccpManifestRecord.getRoleOfAccManifestId()));
        properties.put("ownerUser", resolver.getUser(asccpRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(asccpRecord.getNamespaceId()));

        properties.put("_metadata", toMetadata(asccpManifestRecord, asccpRecord));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(AsccpManifestRecord asccpManifestRecord, AsccpRecord asccpRecord) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("asccpManifest", toMetadata(asccpManifestRecord));
        metadata.put("asccp", toMetadata(asccpRecord));

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(BccpManifestRecord bccpManifestRecord, BccpRecord bccpRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "bccp");
        properties.put("guid", bccpRecord.getGuid());
        properties.put("propertyTerm", bccpRecord.getPropertyTerm());
        properties.put("representationTerm", bccpRecord.getRepresentationTerm());
        properties.put("definition", bccpRecord.getDefinition());
        properties.put("definitionSource", bccpRecord.getDefinitionSource());
        properties.put("defaultValue", bccpRecord.getDefaultValue());
        properties.put("fixedValue", bccpRecord.getFixedValue());
        properties.put("state", bccpRecord.getState());
        properties.put("deprecated", (byte) 1 == bccpRecord.getIsDeprecated());
        properties.put("nillable", (byte) 1 == bccpRecord.getIsNillable());

        properties.put("bdt", resolver.getDtByManifestId(bccpManifestRecord.getBdtManifestId()));
        properties.put("ownerUser", resolver.getUser(bccpRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(bccpRecord.getNamespaceId()));

        properties.put("_metadata", toMetadata(bccpManifestRecord, bccpRecord));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(BccpManifestRecord bccpManifestRecord, BccpRecord bccpRecord) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("bccpManifest", toMetadata(bccpManifestRecord));
        metadata.put("bccp", toMetadata(bccpRecord));

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(DtManifestRecord dtManifestRecord, DtRecord dtRecord,
                            List<DtScManifestRecord> dtScManifestRecords, List<DtScRecord> dtScRecords) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "dt");
        properties.put("guid", dtRecord.getGuid());
        properties.put("representationTerm", dtRecord.getRepresentationTerm());
        properties.put("dataTypeTerm", dtRecord.getDataTypeTerm());
        properties.put("qualifier", dtRecord.getQualifier_());
        properties.put("definition", dtRecord.getDefinition());
        properties.put("definitionSource", dtRecord.getDefinitionSource());
        properties.put("contentComponentDefinition", dtRecord.getContentComponentDefinition());
        properties.put("sixDigitId", dtRecord.getSixDigitId());
        properties.put("state", dtRecord.getState());
        properties.put("deprecated", (byte) 1 == dtRecord.getIsDeprecated());

        properties.put("basedDt", resolver.getDtByManifestId(dtManifestRecord.getBasedDtManifestId()));
        properties.put("ownerUser", resolver.getUser(dtRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(dtRecord.getNamespaceId()));

        List<Map<String, Object>> supplementaryComponents = new ArrayList();
        properties.put("supplementaryComponents", supplementaryComponents);

        Map<ULong, DtScRecord> dtScRecordMap = dtScRecords.stream().collect(
                Collectors.toMap(DtScRecord::getDtScId, Function.identity()));
        for (DtScManifestRecord dtScManifestRecord : dtScManifestRecords) {
            DtScRecord dtScRecord = dtScRecordMap.get(dtScManifestRecord.getDtScId());
            supplementaryComponents.add(serialize(dtScManifestRecord, dtScRecord));
        }

        properties.put("_metadata", toMetadata(dtManifestRecord, dtRecord,
                dtScManifestRecords, dtScRecords));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(DtManifestRecord dtManifestRecord, DtRecord dtRecord,
                              List<DtScManifestRecord> dtScManifestRecords, List<DtScRecord> dtScRecords) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("dtManifest", toMetadata(dtManifestRecord));
        metadata.put("dt", toMetadata(dtRecord));
        List<Map<String, Object>> supplementaryComponents = new ArrayList();
        metadata.put("supplementaryComponents", supplementaryComponents);

        Map<ULong, DtScRecord> dtScRecordMap = dtScRecords.stream().collect(
                Collectors.toMap(DtScRecord::getDtScId, Function.identity()));
        for (DtScManifestRecord dtScManifestRecord : dtScManifestRecords) {
            DtScRecord dtScRecord = dtScRecordMap.get(dtScManifestRecord.getDtScId());
            Map<String, Object> dtScMetadata = new HashMap();
            dtScMetadata.put("dtScManifest", toMetadata(dtScManifestRecord));
            dtScMetadata.put("dtSc", toMetadata(dtScRecord));
            supplementaryComponents.add(dtScMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(DtScManifestRecord dtScManifestRecord, DtScRecord dtScRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "dtSc");
        properties.put("guid", dtScRecord.getGuid());
        properties.put("propertyTerm", dtScRecord.getPropertyTerm());
        properties.put("representationTerm", dtScRecord.getRepresentationTerm());
        properties.put("cardinalityMin", dtScRecord.getCardinalityMin());
        properties.put("cardinalityMax", dtScRecord.getCardinalityMax());
        properties.put("defaultValue", dtScRecord.getDefaultValue());
        properties.put("fixedValue", dtScRecord.getFixedValue());
        properties.put("definition", dtScRecord.getDefinition());
        properties.put("definitionSource", dtScRecord.getDefinitionSource());
        properties.put("basedDtSc", resolver.getDtSc(dtScRecord.getBasedDtScId()));

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(CodeListManifestRecord codeListManifestRecord,
                            CodeListRecord codeListRecord,
                            List<CodeListValueManifestRecord> codeListValueManifestRecords,
                            List<CodeListValueRecord> codeListValueRecords) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "codeList");
        properties.put("guid", codeListRecord.getGuid());
        properties.put("name", codeListRecord.getName());
        properties.put("listId", codeListRecord.getListId());
        properties.put("agencyId", resolver.getAgencyIdListValueByAgencyIdListValueManifestId(
                codeListManifestRecord.getAgencyIdListValueManifestId()));
        properties.put("versionId", codeListRecord.getVersionId());
        properties.put("remark", codeListRecord.getRemark());
        properties.put("definition", codeListRecord.getDefinition());
        properties.put("definitionSource", codeListRecord.getDefinitionSource());
        properties.put("state", codeListRecord.getState());
        properties.put("deprecated", (byte) 1 == codeListRecord.getIsDeprecated());
        properties.put("extensible", (byte) 1 == codeListRecord.getExtensibleIndicator());

        properties.put("basedCodeList", resolver.getCodeListByCodeListManifestId(
                codeListManifestRecord.getBasedCodeListManifestId()));
        properties.put("ownerUser", resolver.getUser(codeListRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(codeListRecord.getNamespaceId()));

        List<Map<String, Object>> values = new ArrayList();
        properties.put("values", values);

        Map<ULong, CodeListValueRecord> codeListValueRecordMap = codeListValueRecords.stream().collect(
                Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));
        for (CodeListValueManifestRecord codeListValueManifestRecord : codeListValueManifestRecords) {
            CodeListValueRecord codeListValueRecord = codeListValueRecordMap.get(codeListValueManifestRecord.getCodeListValueId());
            values.add(serialize(codeListValueManifestRecord, codeListValueRecord));
        }

        properties.put("_metadata", toMetadata(codeListManifestRecord, codeListRecord,
                codeListValueManifestRecords, codeListValueRecords));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(CodeListManifestRecord codeListManifestRecord,
                                           CodeListRecord codeListRecord,
                                           List<CodeListValueManifestRecord> codeListValueManifestRecords,
                                           List<CodeListValueRecord> codeListValueRecords) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("codeListManifest", toMetadata(codeListManifestRecord));
        metadata.put("codeList", toMetadata(codeListRecord));
        List<Map<String, Object>> values = new ArrayList();
        metadata.put("values", values);

        Map<ULong, CodeListValueRecord> codeListValueRecordMap = codeListValueRecords.stream().collect(
                Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));
        for (CodeListValueManifestRecord codeListValueManifestRecord : codeListValueManifestRecords) {
            CodeListValueRecord codeListValueRecord = codeListValueRecordMap.get(codeListValueManifestRecord.getCodeListValueId());
            Map<String, Object> codeListValueMetadata = new HashMap();
            codeListValueMetadata.put("codeListValueManifest", toMetadata(codeListValueManifestRecord));
            codeListValueMetadata.put("codeListValue", toMetadata(codeListValueRecord));
            values.add(codeListValueMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(CodeListValueManifestRecord codeListValueManifestRecord,
                                          CodeListValueRecord codeListValueRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "codeListValue");
        properties.put("guid", codeListValueRecord.getGuid());
        properties.put("value", codeListValueRecord.getValue());
        properties.put("meaning", codeListValueRecord.getMeaning());
        properties.put("definition", codeListValueRecord.getDefinition());
        properties.put("definitionSource", codeListValueRecord.getDefinitionSource());
        properties.put("deprecated", (byte) 1 == codeListValueRecord.getIsDeprecated());

        return properties;
    }

    @SneakyThrows(JsonIOException.class)
    public String serialize(AgencyIdListManifestRecord agencyIdListManifestRecord, AgencyIdListRecord agencyIdListRecord,
                            List<AgencyIdListValueManifestRecord> agencyIdListValueManifestRecords, List<AgencyIdListValueRecord> agencyIdListValueRecords) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "agencyIdList");
        properties.put("guid", agencyIdListRecord.getGuid());
        properties.put("enumTypeGuid", agencyIdListRecord.getEnumTypeGuid());
        properties.put("agencyIdListValue", resolver.getAgencyIdListValueByAgencyIdListValueManifestId(
                agencyIdListManifestRecord.getAgencyIdListValueManifestId()));
        properties.put("name", agencyIdListRecord.getName());
        properties.put("listId", agencyIdListRecord.getListId());
        properties.put("versionId", agencyIdListRecord.getVersionId());
        properties.put("definition", agencyIdListRecord.getDefinition());
        properties.put("state", agencyIdListRecord.getState());
        properties.put("deprecated", (byte) 1 == agencyIdListRecord.getIsDeprecated());

        properties.put("basedAgencyIdList", resolver.getAgencyIdList(agencyIdListRecord.getBasedAgencyIdListId()));
        properties.put("ownerUser", resolver.getUser(agencyIdListRecord.getOwnerUserId()));
        properties.put("namespace", resolver.getNamespace(agencyIdListRecord.getNamespaceId()));

        List<Map<String, Object>> values = new ArrayList();
        properties.put("values", values);

        Map<ULong, AgencyIdListValueRecord> agencyIdListValueRecordMap = agencyIdListValueRecords.stream().collect(
                Collectors.toMap(AgencyIdListValueRecord::getAgencyIdListValueId, Function.identity()));
        for (AgencyIdListValueManifestRecord agencyIdListValueManifestRecord : agencyIdListValueManifestRecords) {
            AgencyIdListValueRecord agencyIdListValueRecord = agencyIdListValueRecordMap.get(agencyIdListValueManifestRecord.getAgencyIdListValueId());
            values.add(serialize(agencyIdListValueManifestRecord, agencyIdListValueRecord));
        }

        properties.put("_metadata", toMetadata(agencyIdListManifestRecord, agencyIdListRecord,
                agencyIdListValueManifestRecords, agencyIdListValueRecords));

        return gson.toJson(properties, HashMap.class);
    }

    private Map<String, Object> toMetadata(AgencyIdListManifestRecord agencyIdListManifestRecord,
                                           AgencyIdListRecord agencyIdListRecord,
                                           List<AgencyIdListValueManifestRecord> agencyIdListValueManifestRecords,
                                           List<AgencyIdListValueRecord> agencyIdListValueRecords) {
        Map<String, Object> metadata = new HashMap();

        metadata.put("agencyIdListManifest", toMetadata(agencyIdListManifestRecord));
        metadata.put("agencyIdList", toMetadata(agencyIdListRecord));
        List<Map<String, Object>> values = new ArrayList();
        metadata.put("values", values);

        Map<ULong, AgencyIdListValueRecord> agencyIdListValueRecordMap = agencyIdListValueRecords.stream().collect(
                Collectors.toMap(AgencyIdListValueRecord::getAgencyIdListValueId, Function.identity()));
        for (AgencyIdListValueManifestRecord agencyIdListValueManifestRecord : agencyIdListValueManifestRecords) {
            AgencyIdListValueRecord agencyIdListValueRecord = agencyIdListValueRecordMap.get(agencyIdListValueManifestRecord.getAgencyIdListValueId());
            Map<String, Object> agencyIdListValueMetadata = new HashMap();
            agencyIdListValueMetadata.put("agencyIdListValueManifest", toMetadata(agencyIdListValueManifestRecord));
            agencyIdListValueMetadata.put("agencyIdListValue", toMetadata(agencyIdListValueRecord));
            values.add(agencyIdListValueMetadata);
        }

        return metadata;
    }

    @SneakyThrows(JsonIOException.class)
    private Map<String, Object> serialize(AgencyIdListValueManifestRecord agencyIdListValueManifestRecord,
                                          AgencyIdListValueRecord agencyIdListValueRecord) {
        Map<String, Object> properties = new HashMap();

        properties.put("component", "agencyIdListValue");
        properties.put("guid", agencyIdListValueRecord.getGuid());
        properties.put("value", agencyIdListValueRecord.getValue());
        properties.put("name", agencyIdListValueRecord.getName());
        properties.put("definition", agencyIdListValueRecord.getDefinition());
        properties.put("deprecated", (byte) 1 == agencyIdListValueRecord.getIsDeprecated());

        return properties;
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
        properties.put("ownerUser", resolver.getUser(xbtRecord.getOwnerUserId()));

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
