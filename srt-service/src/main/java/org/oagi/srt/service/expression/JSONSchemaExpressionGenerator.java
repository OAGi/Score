package org.oagi.srt.service.expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.service.expression.Helper.*;

class JSONSchemaExpressionGenerator implements SchemaExpressionGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // In schema version draft-04, it used "id" for dereferencing.
    // However, in draft-06, it changes to "$id".
    private static final String ID_KEYWORD = "id";

    private ObjectMapper mapper;
    private Map<String, Object> root;

    public JSONSchemaExpressionGenerator() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void generate(GenerationContext generationContext,
                         TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) {
        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();

        AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie);
        AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE(asbiep);

        Map<String, Object> definitions;
        if (root == null) {
            root = new LinkedHashMap();
            root.put("$schema", "http://json-schema.org/draft-04/schema#");
            root.put(ID_KEYWORD, "http://www.openapplications.org/oagis/10/");

            root.put("required", new ArrayList());
            root.put("additionalProperties", false);

            Map<String, Object> properties = new LinkedHashMap();
            root.put("properties", properties);
            definitions = new LinkedHashMap();
            root.put("definitions", definitions);
        } else {
            definitions = (Map<String, Object>) root.get("definitions");
        }

        fillProperties(root, definitions, asbiep, typeAbie, generationContext, option);
    }

    private String camelCase(String... terms) {
        String term = Arrays.stream(terms).filter(e -> !StringUtils.isEmpty(e))
                .collect(Collectors.joining());
        if (StringUtils.isEmpty(term)) {
            throw new IllegalArgumentException();
        }
        String s = term.replaceAll(" ", "");

        if (s.length() > 3 &&
                Character.isUpperCase(s.charAt(0)) &&
                Character.isUpperCase(s.charAt(1)) &&
                Character.isUpperCase(s.charAt(2))) {
            return s;
        }

        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                AssociationBusinessInformationEntity asbie,
                                GenerationContext generationContext,
                                ProfileBODGenerationOption option) {
        AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
        AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE2(asbiep);

        AssociationCoreComponent ascc = generationContext.queryBasedASCC(asbie);
        boolean isArray = (ascc.getCardinalityMax() != 1);
        int minVal = asbie.getCardinalityMin();
        int maxVal = asbie.getCardinalityMax();
        boolean isNillable = asbie.isNillable();

        AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
        String name = camelCase(asccp.getPropertyTerm());
        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }
        ((Map<String, Object>) parent.get("properties")).put(name, properties);

        String definition = asbie.getDefinition();
        if (!StringUtils.isEmpty(definition)) {
            properties.put("description", definition);
        }

        if (isNillable) {
            properties.put("type", Arrays.asList(isArray ? "array" : "object", "null"));
        } else {
            properties.put("type", isArray ? "array" : "object");
        }

        if (isArray) {
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }

            properties.put("additionalItems", false);

            Map<String, Object> items = new LinkedHashMap();
            properties.put("items", items);
            items.put("type", "object");

            properties = items;
        }

        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);
        properties.put("properties", new LinkedHashMap<String, Object>());

        fillProperties(properties, definitions, typeAbie, generationContext, option);

        if (((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                AssociationBusinessInformationEntityProperty asbiep,
                                AggregateBusinessInformationEntity abie,
                                GenerationContext generationContext,
                                ProfileBODGenerationOption option) {

        AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
        String name = camelCase(asccp.getPropertyTerm());

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }
        ((Map<String, Object>) parent.get("properties")).put(name, properties);

        String definition = abie.getDefinition();
        if (!StringUtils.isEmpty(definition)) {
            properties.put("description", definition);
        }
        properties.put("type", "object");
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        fillProperties(properties, definitions, abie, generationContext, option);

        if (((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }
    }

    private Map<String, Object> toProperties(XSDBuiltInType xbt) {
        String jbtDraft05Map = xbt.getJbtDraft05Map();
        try {
            return mapper.readValue(jbtDraft05Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   XSDBuiltInType xbt,
                                   GenerationContext generationContext) {
        String builtInType = xbt.getBuiltInType();
        if (builtInType.startsWith("xsd:")) {
            builtInType = builtInType.substring(4);
        }
        if (!definitions.containsKey(builtInType)) {
            Map<String, Object> content = toProperties(xbt);
            definitions.put(builtInType, content);
        }

        return "#/definitions/" + builtInType;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BasicBusinessInformationEntity bbie,
                                   CodeList codeList,
                                   GenerationContext generationContext) {
        DataType bdt = generationContext.queryAssocBDT(bbie);
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());

        Map<String, Object> properties;
        if (bdtPriRestri.getCodeListId() != 0) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList, generationContext);
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                   CodeList codeList,
                                   GenerationContext generationContext) {
        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());

        Map<String, Object> properties;
        if (bdtScPriRestri.getCodeListId() != 0) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList, generationContext);
    }

    private String fillDefinitions(Map<String, Object> properties,
                                   Map<String, Object> definitions,
                                   CodeList codeList,
                                   GenerationContext generationContext) {
        String codeListName = camelCase(getCodeListTypeName(codeList));
        if (!definitions.containsKey(codeListName)) {
            List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            properties.put("enum", enumerations);

            definitions.put(codeListName, properties);
        }

        return "#/definitions/" + codeListName;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   AgencyIdList agencyIdList,
                                   GenerationContext generationContext) {
        AgencyIdListValue agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueId());
        String agencyListTypeName = camelCase(getAgencyListTypeName(agencyIdList, agencyIdListValue));
        if (!definitions.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValue> agencyIdListValues =
                    generationContext.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            properties.put("enum", enumerations);

            definitions.put(agencyListTypeName, properties);
        }

        return "#/definitions/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                AggregateBusinessInformationEntity abie,
                                GenerationContext generationContext,
                                ProfileBODGenerationOption option) {

        List<BusinessInformationEntity> children = generationContext.queryChildBIEs(abie);
        for (BusinessInformationEntity bie : children) {
            if (bie instanceof BasicBusinessInformationEntity) {
                BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) bie;
                fillProperties(parent, definitions, bbie, generationContext, option);
            } else {
                AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) bie;
                if (isAnyProperty(asbie, generationContext)) {
                    parent.put("additionalProperties", true);
                } else {
                    fillProperties(parent, definitions, asbie, generationContext, option);
                }
            }
        }
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                BasicBusinessInformationEntity bbie,
                                GenerationContext generationContext,
                                ProfileBODGenerationOption option) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        BasicCoreComponentProperty bccp = generationContext.queryToBCCP(bcc);
        DataType bdt = generationContext.queryBDT(bccp);

        boolean isArray = (bcc.getCardinalityMax() != 1);
        int minVal = bbie.getCardinalityMin();
        int maxVal = bbie.getCardinalityMax();
        /*
         * When a bbie is based on a bcc, whose entity type is 'attribute',
         * XML schema generation shouldn't generate the nillable="true",
         * even if the user specified the bbie to be nillable.
         */
        boolean isNillable;
        if (bcc.getEntityType() == Attribute) {
            isNillable = false;
        } else {
            isNillable = bbie.isNillable();
        }

        String name = camelCase(bccp.getPropertyTerm());

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }
        ((Map<String, Object>) parent.get("properties")).put(name, properties);

        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        String definition = bbie.getDefinition();
        if (!StringUtils.isEmpty(definition)) {
            properties.put("description", definition);
        }

        if (isNillable) {
            properties.put("type", Arrays.asList(isArray ? "array" : "object", "null"));
        } else {
            properties.put("type", isArray ? "array" : "object");
        }

        if (isArray) {
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }

            properties.put("additionalItems", false);

            Map<String, Object> items = new LinkedHashMap();
            properties.put("items", items);
            items.put("type", "object");

            properties = items;
        }

        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);
        properties.put("properties", new LinkedHashMap<String, Object>());

        CodeList codeList = getCodeList(generationContext, bbie, bdt);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbie, codeList, generationContext);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList, generationContext);
            } else {
                BusinessDataTypePrimitiveRestriction bdtPriRestri =
                        generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
                XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
                ref = fillDefinitions(definitions, xbt, generationContext);
            }
        }

        ((List<String>) properties.get("required")).add("content");
        ((Map<String, Object>) properties.get("properties"))
                .put("content", ImmutableMap.<String, Object>builder()
                        .put("$ref", ref)
                        .build());

        List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
        if (!bbieScList.isEmpty()) {
            for (BasicBusinessInformationEntitySupplementaryComponent bbieSc : bbieScList) {
                fillProperties(properties, definitions, bbieSc, generationContext, option);
            }
        }
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                GenerationContext generationContext,
                                ProfileBODGenerationOption option) {
        int minVal = bbieSc.getCardinalityMin();
        int maxVal = bbieSc.getCardinalityMax();
        if (maxVal == 0) {
            return;
        }

        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        String name = camelCase(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm());
        Map<String, Object> properties = new LinkedHashMap();
        ((Map<String, Object>) parent.get("properties")).put(name, properties);

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        String definition = bbieSc.getDefinition();
        if (!StringUtils.isEmpty(definition)) {
            properties.put("description", definition);
        }

        CodeList codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbieSc, codeList, generationContext);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList, generationContext);
            } else {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                        generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                ref = fillDefinitions(definitions, xbt, generationContext);
            }
        }

        properties.put("$ref", ref);
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }

        // The "required" property for the root element of schema should has only one child.
        if (((List<String>) root.get("required")).size() > 1) {
            root.remove("required");
        }

        //
        Map<String, Object> properties = (Map<String, Object>) root.get("properties");
        for (String key : properties.keySet()) {
            Map<String, Object> copied = new LinkedHashMap();
            copied.put(ID_KEYWORD, "#" + key);
            copied.putAll(((Map<String, Object>) properties.get(key)));
            properties.put(key, copied);
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(Utility.generateGUID(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".json");

        mapper.writeValue(tempFile, root);
        logger.info("JSON Schema is generated: " + tempFile);

        return tempFile;
    }
}
