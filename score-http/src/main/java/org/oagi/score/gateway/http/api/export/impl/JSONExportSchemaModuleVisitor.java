package org.oagi.score.gateway.http.api.export.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.export.model.*;
import org.oagi.score.gateway.http.api.module_management.model.ModuleCcDocument;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.util.Utility;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.api.export.model.ConnectSpecNameResolvers.agencyIdListNameResolver;
import static org.oagi.score.gateway.http.api.export.model.ConnectSpecNameResolvers.codeListNameResolver;
import static org.oagi.score.gateway.http.api.export.model.ConnectSpecNameResolvers.dtNameResolver;
import static org.oagi.score.gateway.http.common.ScoreConstants.ANY_ASCCP_DEN;

public class JSONExportSchemaModuleVisitor implements ExportSchemaModuleVisitor {

    private static final String JSON_SCHEMA_URI = "https://json-schema.org/draft/2020-12/schema";

    private final CcDocument ccDocument;
    private final ModuleCcDocument moduleCcDocument;
    private final ObjectMapper mapper;

    private File baseDir;
    private SchemaModule schemaModule;
    private File moduleFile;
    private byte[] content;

    private LinkedHashMap<String, Object> document;
    private LinkedHashMap<String, Object> definitions;
    private LinkedHashMap<String, Object> globalElementProperties;
    private final Map<String, String> externalModuleRefMap = new LinkedHashMap<>();

    public JSONExportSchemaModuleVisitor(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
        this.moduleCcDocument = (ccDocument instanceof ModuleCcDocument moduleCcDocument) ? moduleCcDocument : null;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    @Override
    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        this.schemaModule = schemaModule;
        this.content = null;
        this.externalModuleRefMap.clear();
        this.document = new LinkedHashMap<>();
        this.definitions = new LinkedHashMap<>();
        this.globalElementProperties = new LinkedHashMap<>();

        this.moduleFile = new File(baseDir, schemaModule.getPath() + ".json").getCanonicalFile();

        document.put("$schema", JSON_SCHEMA_URI);
        document.put("$id", FilenameUtils.separatorsToUnix(schemaModule.getPath()) + ".json");
        document.put("$defs", definitions);
    }

    @Override
    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception {
        externalModuleRefMap.put(includeSchemaModule.getNamespace().getNamespaceUri(), getRelativeSchemaLocation(includeSchemaModule));
    }

    @Override
    public void visitImportModule(SchemaModule importSchemaModule) throws Exception {
        externalModuleRefMap.put(importSchemaModule.getNamespace().getNamespaceUri(), getRelativeSchemaLocation(importSchemaModule));
    }

    @Override
    public void visitAgencyId(AgencyId agencyId) throws Exception {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");

        List<String> values = agencyId.getValues().stream()
                .map(value -> value.value())
                .filter(StringUtils::hasLength)
                .toList();
        if (!values.isEmpty()) {
            schema.put("enum", values);
        }

        applyMetadata(schema, agencyId.getDefinition(), agencyId.getTypeName());
        definitions.put(agencyId.getTypeName(), schema);
    }

    @Override
    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        if (!schemaCodeList.getValues().isEmpty()) {
            schema.put("enum", new ArrayList<>(schemaCodeList.getValues()));
        }
        definitions.put(schemaCodeList.getName() + "ContentType", schema);
    }

    @Override
    public void visitXBTSimpleType(XBTSimpleType xbtSimpleType) throws Exception {
        definitions.put(xbtSimpleType.getName(), readXbtSchema(xbtSimpleType.getJsonSchemaDefinition()));
    }

    @Override
    public void visitBDTSimpleType(BDTSimpleType bdtSimpleType) throws Exception {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>(buildDtContentSchema(bdtSimpleType.getDataType()));
        applyMetadata(schema, definitionOf(bdtSimpleType.getDataType()), bdtSimpleType.getName());
        definitions.put(bdtSimpleType.getName(), schema);
    }

    @Override
    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        applyMetadata(schema, definitionOf(bdtSimpleContent.getDataType()), bdtSimpleContent.getName());

        LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        LinkedHashSet<String> required = new LinkedHashSet<>();

        properties.put("content", buildDtContentSchema(bdtSimpleContent.getDataType()));
        required.add("content");

        for (BDTSC dtSc : bdtSimpleContent.getDtScList()) {
            String propertyName = jsonPropertyName(dtSc);
            properties.put(propertyName, buildDtScSchema(dtSc));
            if (dtSc.getMinCardinality() > 0) {
                required.add(propertyName);
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", new ArrayList<>(required));
        }
        definitions.put(bdtSimpleContent.getName(), schema);
    }

    @Override
    public void visitBCCP(BCCP bccp) throws Exception {
        LinkedHashMap<String, Object> schema = withRefFirst(
                definitionRef(bccp.getTypeName(), referencedModulePathForDt(bccp.dtManifestId()), bccp.getTypeNamespaceId()),
                buildMetadata(bccp.getDefinition(), bccp.getName()));
        if (bccp.isNillable()) {
            schema = new LinkedHashMap<>(applyNillableTypeUnion(schema, true));
        }
        if (StringUtils.hasLength(bccp.getDefaultValue())) {
            schema.put("default", bccp.getDefaultValue());
        }
        registerGlobalElement(jsonPropertyName(bccp.getPropertyTerm()), schema);
    }

    @Override
    public void visitACCComplexType(ACCComplexType accComplexType) throws Exception {
        definitions.put(accComplexType.getTypeName(), buildAccSchema(accComplexType));
    }

    @Override
    public void visitACCGroup(ACCGroup accGroup) throws Exception {
        definitions.put(accGroup.getTypeName(), buildAccSchema(accGroup));
    }

    @Override
    public void visitASCCPComplexType(ASCCPComplexType asccpComplexType) throws Exception {
        registerGlobalElement(jsonPropertyName(asccpComplexType.getPropertyTerm()), buildAsccpSchema(asccpComplexType));
    }

    @Override
    public void visitASCCPGroup(ASCCPGroup asccpGroup) throws Exception {
        registerGlobalElement(jsonPropertyName(asccpGroup.getPropertyTerm()), buildAsccpSchema(asccpGroup));
    }

    @Override
    public void visitBlobContent(byte[] content) throws Exception {
        this.content = content;
    }

    @Override
    public File endSchemaModule(SchemaModule schemaModule) throws Exception {
        FileUtils.forceMkdirParent(this.moduleFile);

        if (content != null) {
            FileUtils.writeByteArrayToFile(this.moduleFile, content);
            return this.moduleFile;
        }

        if (!globalElementProperties.isEmpty()) {
            applyGlobalElementRoot();
        } else {
            String rootRef = selectRootRef(schemaModule);
            if (StringUtils.hasLength(rootRef)) {
                document.put("$ref", rootRef);
            }
        }
        mapper.writeValue(this.moduleFile, orderedDocument());
        return this.moduleFile;
    }

    private LinkedHashMap<String, Object> orderedDocument() {
        LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
        copyIfPresent(ordered, "$schema");
        copyIfPresent(ordered, "$id");
        copyIfPresent(ordered, "type");
        copyIfPresent(ordered, "additionalProperties");
        copyIfPresent(ordered, "minProperties");
        copyIfPresent(ordered, "maxProperties");
        copyIfPresent(ordered, "properties");
        copyIfPresent(ordered, "required");
        copyIfPresent(ordered, "$ref");
        copyIfPresent(ordered, "$defs");
        document.forEach(ordered::putIfAbsent);
        return ordered;
    }

    private void copyIfPresent(Map<String, Object> target, String key) {
        if (document.containsKey(key)) {
            target.put(key, document.get(key));
        }
    }

    private Map<String, Object> buildAccSchema(ACC acc) {
        if (isExtensionAcc(acc)) {
            LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("additionalProperties", true);
            applyMetadata(schema, acc.getDefinition(), acc.getTypeName());
            if (acc.isAbstract()) {
                schema.put("x-abstract", true);
            }
            return schema;
        }

        if (acc.getOagisComponentType() == OagisComponentType.Embedded) {
            LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("additionalProperties", true);
            applyMetadata(schema, acc.getDefinition(), acc.getTypeName());
            return schema;
        }

        ObjectSchemaBuilder builder = new ObjectSchemaBuilder();
        for (SeqKeySupportable association : ccDocument.getAssociationListByFromAccManifestId(acc.accManifestId())) {
            addAssociation(builder, association);
        }

        boolean extendable = !ccDocument.getAccListByBasedAccManifestId(acc.accManifestId()).isEmpty();
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        if (acc.getBasedACC() != null) {
            List<Object> allOf = new ArrayList<>();
            allOf.add(withRefFirst(definitionRef(
                    acc.getBasedACC().getTypeName(),
                    referencedModulePathForAcc(acc.getBasedACC().accManifestId()),
                    acc.getBasedACC().getTypeNamespaceId()), new LinkedHashMap<>()));

            LinkedHashMap<String, Object> localSchema = builder.toSchema();
            if (!localSchema.isEmpty()) {
                allOf.add(localSchema);
            }
            schema.put("allOf", allOf);
        } else {
            schema.putAll(builder.toSchema());
        }

        if (!builder.additionalProperties && !extendable) {
            schema.put("unevaluatedProperties", false);
        }
        applyMetadata(schema, acc.getDefinition(), acc.getTypeName());
        if (acc.isAbstract()) {
            schema.put("x-abstract", true);
        }
        return schema;
    }

    private void addAssociation(ObjectSchemaBuilder builder, SeqKeySupportable association) {
        if (association instanceof AsccSummaryRecord ascc) {
            if (ascc.cardinality().max() == 0) {
                return;
            }

            AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
            if (asccp == null) {
                return;
            }
            if (ANY_ASCCP_DEN.equals(asccp.den()) || ascc.den().endsWith("Any Structured Content")) {
                builder.additionalProperties = true;
                return;
            }

            String propertyName = jsonPropertyName(asccp.propertyTerm());
            builder.addProperty(propertyName, buildAsccPropertySchema(ascc, asccp), ascc.cardinality().min() > 0);
            return;
        }

        BccSummaryRecord bcc = (BccSummaryRecord) association;
        if (bcc.cardinality().max() == 0) {
            return;
        }

        BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());
        if (bccp == null) {
            return;
        }

        String propertyName = jsonPropertyName(bccp.propertyTerm());
        builder.addProperty(propertyName, buildBccPropertySchema(bcc, bccp), bcc.cardinality().min() > 0);
    }

    private Map<String, Object> buildAsccpSchema(ASCCP asccp) {
        LinkedHashMap<String, Object> schema = withRefFirst(
                definitionRef(asccp.getTypeName(), referencedModulePathForAcc(asccp.roleOfAccManifestId()), asccp.getTypeNamespaceId()),
                buildMetadata(asccp.getDefinition(), asccp.getName()));
        return applyNillableTypeUnion(schema, asccp.isNillable());
    }

    private Map<String, Object> buildAsccPropertySchema(AsccSummaryRecord ascc, AsccpSummaryRecord asccp) {
        String definitionName;
        String targetModulePath;
        NamespaceId targetNamespaceId;
        if (asccp.reusable()) {
            definitionName = Utility.toCamelCase(asccp.den().substring((asccp.propertyTerm() + ". ").length())) + "Type";
            targetModulePath = referencedModulePathForAcc(asccp.roleOfAccManifestId());
            targetNamespaceId = ccDocument.getAcc(asccp.roleOfAccManifestId()).namespaceId();
        } else {
            definitionName = Utility.toCamelCase(asccp.den().substring((asccp.propertyTerm() + ". ").length())) + "Type";
            targetModulePath = referencedModulePathForAcc(asccp.roleOfAccManifestId());
            targetNamespaceId = ccDocument.getAcc(asccp.roleOfAccManifestId()).namespaceId();
        }
        LinkedHashMap<String, Object> schema = withRefFirst(
                definitionRef(definitionName, targetModulePath, targetNamespaceId),
                buildMetadata(firstNonBlank(definitionOf(ascc), definitionOf(asccp)), ascc.den()));
        return applyArrayCardinality(
                applyNillableTypeUnion(schema, asccp.nillable()),
                ascc.cardinality().min(),
                ascc.cardinality().max());
    }

    private Map<String, Object> buildBccPropertySchema(BccSummaryRecord bcc, BccpSummaryRecord bccp) {
        DtSummaryRecord dt = ccDocument.getDt(bccp.dtManifestId());
        LinkedHashMap<String, Object> schema = withRefFirst(
                definitionRef(dtNameResolver.apply(dt), referencedModulePathForDt(dt.dtManifestId()), dt.namespaceId()),
                buildMetadata(firstNonBlank(definitionOf(bcc), definitionOf(bccp)), bcc.den()));
        applyValueConstraint(schema, (bcc.valueConstraint() != null) ? bcc.valueConstraint() : bccp.valueConstraint());
        return applyArrayCardinality(
                applyNillableTypeUnion(schema, bcc.nillable() || bccp.nillable()),
                bcc.cardinality().min(),
                bcc.cardinality().max());
    }

    private Map<String, Object> buildDtContentSchema(DtSummaryRecord dt) {
        DtAwdPriSummaryRecord defaultDtAwdPri = getDefaultDtAwdPri(dt);
        if (defaultDtAwdPri == null) {
            if (dt.basedDtManifestId() != null) {
                DtSummaryRecord basedDt = ccDocument.getDt(dt.basedDtManifestId());
                return withRefFirst(definitionRef(dtNameResolver.apply(basedDt), referencedModulePathForDt(basedDt.dtManifestId()), basedDt.namespaceId()), new LinkedHashMap<>());
            }
            return stringTypeSchema();
        }

        if (defaultDtAwdPri.codeListManifestId() != null) {
            CodeListSummaryRecord codeList = ccDocument.getCodeList(defaultDtAwdPri.codeListManifestId());
            return withRefFirst(definitionRef(
                    codeListNameResolver.apply(codeList) + "ContentType",
                    referencedModulePathForCodeList(codeList.codeListManifestId()),
                    codeList.namespaceId()), new LinkedHashMap<>());
        }
        if (defaultDtAwdPri.agencyIdListManifestId() != null) {
            var agencyIdList = ccDocument.getAgencyIdList(defaultDtAwdPri.agencyIdListManifestId());
            String typeName = agencyIdListNameResolver.apply(agencyIdList) + "ContentType";
            return withRefFirst(definitionRef(
                    typeName,
                    referencedModulePathForAgencyIdList(agencyIdList.agencyIdListManifestId()),
                    agencyIdList.namespaceId()), new LinkedHashMap<>());
        }
        if (defaultDtAwdPri.xbtManifestId() != null) {
            XbtSummaryRecord xbt = ccDocument.getXbt(defaultDtAwdPri.xbtManifestId());
            return xbtSchema(xbt);
        }
        if (dt.basedDtManifestId() != null) {
            DtSummaryRecord basedDt = ccDocument.getDt(dt.basedDtManifestId());
            return withRefFirst(definitionRef(dtNameResolver.apply(basedDt), referencedModulePathForDt(basedDt.dtManifestId()), basedDt.namespaceId()), new LinkedHashMap<>());
        }
        return stringTypeSchema();
    }

    private Map<String, Object> buildDtScSchema(BDTSC dtSc) {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        applyMetadata(schema, dtSc.getDefinition(), dtSc.getName());
        applyValueConstraint(schema, dtSc.getDtSc().valueConstraint());

        if (dtSc.getCodeList() != null) {
            schema = withRefFirst(
                    definitionRef(
                            codeListNameResolver.apply(dtSc.getCodeList()) + "ContentType",
                            referencedModulePathForCodeList(dtSc.getCodeList().codeListManifestId()),
                            dtSc.getCodeList().namespaceId()),
                    schema);
        } else if (dtSc.getAgencyIdList() != null) {
            String typeName = agencyIdListNameResolver.apply(dtSc.getAgencyIdList()) + "ContentType";
            schema = withRefFirst(definitionRef(
                    typeName,
                    referencedModulePathForAgencyIdList(dtSc.getAgencyIdList().agencyIdListManifestId()),
                    dtSc.getAgencyIdList().namespaceId()), schema);
        } else if (dtSc.getXbt() != null) {
            schema = mergeSchema(xbtSchema(dtSc.getXbt()), schema);
        }

        return applyArrayCardinality(schema, dtSc.getMinCardinality(), dtSc.getMaxCardinality());
    }

    private Map<String, Object> xbtSchema(XbtSummaryRecord xbt) {
        if (xbt == null) {
            return stringTypeSchema();
        }
        if (xbt.builtInType().startsWith("xsd:")) {
            return readXbtSchema(xbt.jbt202012Map());
        }
        return withRefFirst(
                definitionRef(xbt.builtInType(), referencedModulePathForXbt(xbt.xbtManifestId()), null),
                new LinkedHashMap<>());
    }

    private DtAwdPriSummaryRecord getDefaultDtAwdPri(DtSummaryRecord dt) {
        return ccDocument.getDtAwdPriList(dt.dtManifestId()).stream()
                .filter(DtAwdPriSummaryRecord::isDefault)
                .findFirst()
                .orElse(null);
    }

    private DtScAwdPriSummaryRecord getDefaultDtScAwdPri(DtScSummaryRecord dtSc) {
        return ccDocument.getDtScAwdPriList(dtSc.dtScManifestId()).stream()
                .filter(DtScAwdPriSummaryRecord::isDefault)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> readXbtSchema(String jsonSchemaDefinition) {
        if (!StringUtils.hasLength(jsonSchemaDefinition)) {
            return stringTypeSchema();
        }

        try {
            return mapper.readValue(jsonSchemaDefinition, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse JSON schema definition.", e);
        }
    }

    private LinkedHashMap<String, Object> stringTypeSchema() {
        LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        return schema;
    }

    private Map<String, Object> applyArrayCardinality(Map<String, Object> schema, int min, int max) {
        if (max == 1) {
            return schema;
        }

        LinkedHashMap<String, Object> arraySchema = new LinkedHashMap<>();
        Object description = schema.remove("description");
        Object title = schema.remove("title");
        if (description != null) {
            arraySchema.put("description", description);
        }
        if (title != null) {
            arraySchema.put("title", title);
        }
        arraySchema.put("type", "array");
        if (min > 0) {
            arraySchema.put("minItems", min);
        }
        if (max > 0) {
            arraySchema.put("maxItems", max);
        }
        arraySchema.put("items", schema);
        return arraySchema;
    }

    private LinkedHashMap<String, Object> applyNillableTypeUnion(Map<String, Object> schema, boolean nillable) {
        if (!nillable) {
            return new LinkedHashMap<>(schema);
        }

        Object type = schema.get("type");
        if (type instanceof String typeName) {
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>(schema);
            copy.put("type", List.of(typeName, "null"));
            return copy;
        }
        if (type instanceof Collection<?> typeCollection) {
            List<Object> typeList = new ArrayList<>(typeCollection);
            if (!typeList.contains("null")) {
                typeList.add("null");
            }
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>(schema);
            copy.put("type", typeList);
            return copy;
        }

        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        copy.put("anyOf", List.of(schema, Map.of("type", "null")));
        return copy;
    }

    private void applyValueConstraint(Map<String, Object> schema, ValueConstraint valueConstraint) {
        if (valueConstraint == null) {
            return;
        }
        if (valueConstraint.hasFixedValue()) {
            schema.put("enum", List.of(valueConstraint.fixedValue()));
        } else if (valueConstraint.hasDefaultValue()) {
            schema.put("default", valueConstraint.defaultValue());
        }
    }

    private void applyMetadata(Map<String, Object> schema, String description, String title) {
        if (StringUtils.hasLength(description)) {
            schema.put("description", description);
        }
    }

    private LinkedHashMap<String, Object> buildMetadata(String description, String title) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        applyMetadata(metadata, description, title);
        return metadata;
    }

    private LinkedHashMap<String, Object> withRefFirst(String ref, Map<String, Object> schema) {
        LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
        ordered.put("$ref", ref);
        for (Map.Entry<String, Object> entry : schema.entrySet()) {
            if (!"$ref".equals(entry.getKey())) {
                ordered.put(entry.getKey(), entry.getValue());
            }
        }
        return ordered;
    }

    private LinkedHashMap<String, Object> mergeSchema(Map<String, Object> base, Map<String, Object> overlay) {
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>(base);
        merged.putAll(overlay);
        return merged;
    }

    private String definitionRef(String definitionName, String targetModulePath, NamespaceId namespaceId) {
        String normalizedTargetModulePath = normalizeModulePath(targetModulePath);
        if (StringUtils.hasLength(normalizedTargetModulePath) &&
                !normalizedTargetModulePath.equals(normalizeModulePath(schemaModule.getPath()))) {
            return relativeModuleRef(normalizedTargetModulePath, definitionName);
        }
        return definitionRef(definitionName, namespaceId);
    }

    private String definitionRef(String definitionName, NamespaceId namespaceId) {
        if (namespaceId == null || namespaceId.equals(schemaModule.getNamespace().getNamespaceId())) {
            return localRef(definitionName);
        }

        NamespaceSummaryRecord namespace = ccDocument.getNamespace(namespaceId);
        if (namespace == null) {
            return localRef(definitionName);
        }

        String relativeModulePath = externalModuleRefMap.get(namespace.uri());
        if (!StringUtils.hasLength(relativeModulePath)) {
            return localRef(definitionName);
        }

        return relativeModulePath + "#/$defs/" + escapeJsonPointerToken(definitionName);
    }

    private String relativeModuleRef(String targetModulePath, String definitionName) {
        return getRelativeSchemaLocation(targetModulePath) + "#/$defs/" + escapeJsonPointerToken(definitionName);
    }

    private String localRef(String definitionName) {
        return "#/$defs/" + escapeJsonPointerToken(definitionName);
    }

    private String escapeJsonPointerToken(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    private String getRelativeSchemaLocation(SchemaModule targetSchemaModule) throws IOException {
        File targetModuleFile = new File(baseDir, targetSchemaModule.getPath());
        Path pathAbsolute = Paths.get(targetModuleFile.getCanonicalPath());
        Path pathBase = Paths.get(this.moduleFile.getParentFile().getCanonicalPath());
        Path pathRelative = pathBase.relativize(pathAbsolute);
        return FilenameUtils.separatorsToUnix(pathRelative.toString()) + ".json";
    }

    private String getRelativeSchemaLocation(String targetModulePath) {
        try {
            File targetModuleFile = new File(baseDir, normalizeModulePath(targetModulePath));
            Path pathAbsolute = Paths.get(targetModuleFile.getCanonicalPath());
            Path pathBase = Paths.get(this.moduleFile.getParentFile().getCanonicalPath());
            Path pathRelative = pathBase.relativize(pathAbsolute);
            return FilenameUtils.separatorsToUnix(pathRelative.toString()) + ".json";
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve relative JSON schema location.", e);
        }
    }

    private String normalizeModulePath(String modulePath) {
        if (!StringUtils.hasLength(modulePath)) {
            return modulePath;
        }
        return FilenameUtils.separatorsToSystem(modulePath).replaceFirst("^[\\\\/]+", "");
    }

    private String referencedModulePathForAcc(org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId accManifestId) {
        if (moduleCcDocument == null || accManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleAcc(accManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String referencedModulePathForAsccp(org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId asccpManifestId) {
        if (moduleCcDocument == null || asccpManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleAsccp(asccpManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String referencedModulePathForDt(org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId dtManifestId) {
        if (moduleCcDocument == null || dtManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleDt(dtManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String referencedModulePathForCodeList(org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId codeListManifestId) {
        if (moduleCcDocument == null || codeListManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleCodeList(codeListManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String referencedModulePathForAgencyIdList(org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId agencyIdListManifestId) {
        if (moduleCcDocument == null || agencyIdListManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleAgencyIdList(agencyIdListManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String referencedModulePathForXbt(org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId xbtManifestId) {
        if (moduleCcDocument == null || xbtManifestId == null) {
            return null;
        }
        ModuleCCID<?> moduleCCID = moduleCcDocument.getModuleXbt(xbtManifestId);
        return (moduleCCID != null) ? moduleCCID.path() : null;
    }

    private String selectRootRef(SchemaModule schemaModule) {
        for (Pair<SchemaModule.OrderType, String> order : schemaModule.getOrders()) {
            SchemaModule.OrderType type = order.getFirst();
            String guid = order.getSecond();
            switch (type) {
                case ASCCP:
                    ASCCP asccp = schemaModule.getASCCPMap().get(guid);
                    if (asccp != null) {
                        return localRef(asccp.getTypeName());
                    }
                    break;
                case BCCP:
                    BCCP bccp = schemaModule.getBCCPMap().get(guid);
                    if (bccp != null) {
                        return localRef(bccp.getTypeName());
                    }
                    break;
                case ACC:
                    ACC acc = schemaModule.getACCMap().get(guid);
                    if (acc != null) {
                        return localRef(acc.getTypeName());
                    }
                    break;
                case BDTSimple:
                    BDTSimple bdtSimple = schemaModule.getBDTSimpleMap().get(guid);
                    if (bdtSimple != null) {
                        return localRef(bdtSimple.getName());
                    }
                    break;
                case CodeList:
                    SchemaCodeList codeList = schemaModule.getCodeListMap().get(guid);
                    if (codeList != null) {
                        return localRef(codeList.getName() + "ContentType");
                    }
                    break;
                case AgencyId:
                    AgencyId agencyId = schemaModule.getAgencyIdMap().get(guid);
                    if (agencyId != null) {
                        return localRef(agencyId.getTypeName());
                    }
                    break;
                case XBTSimple:
                    XBTSimpleType xbtSimpleType = schemaModule.getXBTSimpleTypeMap().get(guid);
                    if (xbtSimpleType != null) {
                        return localRef(xbtSimpleType.getName());
                    }
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    private boolean isExtensionAcc(ACC acc) {
        String modulePath = FilenameUtils.separatorsToUnix(schemaModule.getPath());
        if (!modulePath.endsWith("/Extension/Extensions")) {
            return false;
        }
        String typeName = acc.getTypeName();
        return "OpenUserAreaType".equals(typeName)
                || "AnyUserAreaType".equals(typeName)
                || "AllExtensionType".equals(typeName)
                || typeName.endsWith("ExtensionType");
    }

    private void registerGlobalElement(String propertyName, Map<String, Object> schema) {
        globalElementProperties.putIfAbsent(
                propertyName,
                new LinkedHashMap<>(schema));
    }

    private void applyGlobalElementRoot() {
        document.put("type", "object");
        document.put("additionalProperties", false);
        document.put("properties", new LinkedHashMap<>(globalElementProperties));
    }

    private String jsonPropertyName(String term) {
        return Helper.convertIdentifierToId(Helper.camelCase(term));
    }

    private String jsonPropertyName(BDTSC dtSc) {
        DtScSummaryRecord dtScRecord = dtSc.getDtSc();
        return Helper.convertIdentifierToId(Helper.toName(
                dtScRecord.propertyTerm(),
                dtScRecord.representationTerm(),
                representationTerm -> "Text".equals(representationTerm) ? "" : representationTerm,
                true));
    }

    private String definitionOf(Object object) {
        if (object instanceof AsccSummaryRecord ascc) {
            return (ascc.definition() != null) ? ascc.definition().content() : null;
        }
        if (object instanceof AsccpSummaryRecord asccp) {
            return (asccp.definition() != null) ? asccp.definition().content() : null;
        }
        if (object instanceof BccSummaryRecord bcc) {
            return (bcc.definition() != null) ? bcc.definition().content() : null;
        }
        if (object instanceof BccpSummaryRecord bccp) {
            return (bccp.definition() != null) ? bccp.definition().content() : null;
        }
        if (object instanceof DtSummaryRecord dt) {
            return (dt.definition() != null) ? dt.definition().content() : null;
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasLength(value)) {
                return value;
            }
        }
        return null;
    }

    private static final class ObjectSchemaBuilder {
        private final LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
        private final LinkedHashSet<String> required = new LinkedHashSet<>();
        private boolean additionalProperties;

        void addProperty(String propertyName, Map<String, Object> schema, boolean requiredIndicator) {
            properties.put(propertyName, schema);
            if (requiredIndicator) {
                required.add(propertyName);
            }
        }

        LinkedHashMap<String, Object> toSchema() {
            LinkedHashMap<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", new ArrayList<>(required));
            }
            return schema;
        }
    }
}
