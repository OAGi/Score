package org.oagi.srt.gateway.http.api.bie_management.service.generate_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.oagi.srt.data.*;
import org.oagi.srt.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.srt.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieJSONGenerateExpression implements BieGenerateExpression, InitializingBean {

    private static final String ID_KEYWORD = "$id";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper mapper;

    private Map<String, Object> root;
    private GenerateExpressionOption option;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    private GenerationContext generationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = null;
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    @Override
    public GenerationContext generateContext(List<TopLevelAbie> topLevelAbies, GenerateExpressionOption option) {
        List<TopLevelAbie> mergedTopLevelAbies = new ArrayList(topLevelAbies);

        /* Issue 587 */
        if (option.isIncludeMetaHeaderForJson()) {
            TopLevelAbie metaHeaderTopLevelAbie =
                    topLevelAbieRepository.findById(option.getMetaHeaderTopLevelAbieId());
            mergedTopLevelAbies.add(metaHeaderTopLevelAbie);
        }
        if (option.isIncludePaginationResponseForJson()) {
            TopLevelAbie paginationResponseTopLevelAbie =
                    topLevelAbieRepository.findById(option.getPaginationResponseTopLevelAbieId());
            mergedTopLevelAbies.add(paginationResponseTopLevelAbie);
        }

        return applicationContext.getBean(GenerationContext.class, mergedTopLevelAbies);
    }

    @Override
    public void generate(TopLevelAbie topLevelAbie, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAbie(topLevelAbie);
    }

    private void generateTopLevelAbie(TopLevelAbie topLevelAbie) {
        ABIE abie = generationContext.findAbie(topLevelAbie.getAbieId());

        ASBIEP asbiep = generationContext.receiveASBIEP(abie);
        ABIE typeAbie = generationContext.queryTargetABIE(asbiep);

        Map<String, Object> definitions;
        if (root == null) {
            root = new LinkedHashMap();
            root.put("$schema", "http://json-schema.org/draft-04/schema#");

            root.put("required", new ArrayList());
            root.put("additionalProperties", false);

            Map<String, Object> properties = new LinkedHashMap();
            root.put("properties", properties);
            definitions = new LinkedHashMap();
            root.put("definitions", definitions);
        } else {
            definitions = (Map<String, Object>) root.get("definitions");
        }

        /*
         * Issue #587
         */
        if (option.isIncludeMetaHeaderForJson()) {
            TopLevelAbie metaHeaderTopLevelAbie =
                    topLevelAbieRepository.findById(option.getMetaHeaderTopLevelAbieId());
            fillProperties(root, definitions, metaHeaderTopLevelAbie, generationContext);
        }
        if (option.isIncludePaginationResponseForJson()) {
            TopLevelAbie paginationResponseTopLevelAbie =
                    topLevelAbieRepository.findById(option.getPaginationResponseTopLevelAbieId());
            fillProperties(root, definitions, paginationResponseTopLevelAbie, generationContext);
        }

        fillProperties(root, definitions, asbiep, typeAbie, generationContext);
    }

    private String _camelCase(String term) {
        return Arrays.stream(term.split(" ")).filter(e -> !StringUtils.isEmpty(e))
                .map(e -> {
                    if (e.length() > 1) {
                        return Character.toUpperCase(e.charAt(0)) + e.substring(1).toLowerCase();
                    } else {
                        return e.toUpperCase();
                    }
                }).collect(Collectors.joining());
    }

    private String camelCase(String... terms) {
        String term = Arrays.stream(terms).collect(Collectors.joining());
        if (terms.length == 1) {
            term = _camelCase(terms[0]);
        } else if (term.contains(" ")) {
            term = Arrays.stream(terms).map(e -> _camelCase(e)).collect(Collectors.joining());
        }

        if (StringUtils.isEmpty(term)) {
            throw new IllegalArgumentException();
        }

        return Character.toLowerCase(term.charAt(0)) + term.substring(1);
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                TopLevelAbie topLevelAbie,
                                GenerationContext generationContext) {

        ABIE abie = generationContext.findAbie(topLevelAbie.getAbieId());
        ASBIEP asbiep = generationContext.receiveASBIEP(abie);
        ABIE typeAbie = generationContext.queryTargetABIE(asbiep);

        fillProperties(parent, definitions, asbiep, typeAbie, generationContext);
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ASBIE asbie,
                                GenerationContext generationContext) {
        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        ABIE typeAbie = generationContext.queryTargetABIE2(asbiep);

        ASCC ascc = generationContext.queryBasedASCC(asbie);

        int minVal = asbie.getCardinalityMin();
        int maxVal = asbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = asbie.isNillable();

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
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

        if (option.isBieDefinition()) {
            String definition = asbie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }
        }

        properties.put("type", "object");
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);
        properties.put("properties", new LinkedHashMap<String, Object>());

        fillProperties(properties, definitions, typeAbie, generationContext);

        if (((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        properties = oneOf(allOf(properties), isNillable);

        if (isArray) {
            Map<String, Object> items = new LinkedHashMap();
            items.putAll(properties);

            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (!StringUtils.isEmpty(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ASBIEP asbiep, ABIE abie,
                                GenerationContext generationContext) {

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = camelCase(asccp.getPropertyTerm());

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = abie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }
        }

        /*
         * Issue #550
         */
        boolean isArray = option.isArrayForJsonExpression();
        if (!isArray) {
            properties.put("type", "object");
        }
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        fillProperties(properties, definitions, abie, generationContext);

        if (((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        /*
         * Issue #575
         */
        if (isArray) {
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (!StringUtils.isEmpty(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> toProperties(Xbt xbt) {
        String jbtDraft05Map = xbt.getJbtDraft05Map();
        try {
            return mapper.readValue(jbtDraft05Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   Xbt xbt) {
        String builtInType = xbt.getBuiltinType();
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
                                   BBIE bbie, CodeList codeList) {
        DT bdt = generationContext.queryAssocBDT(bbie);
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());

        Map<String, Object> properties;
        if (bdtPriRestri.getCodeListId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BBIESC bbieSc, CodeList codeList) {
        DTSC dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        BdtScPriRestri bdtScPriRestri =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());

        Map<String, Object> properties;
        if (bdtScPriRestri.getCodeListId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> properties,
                                   Map<String, Object> definitions,
                                   CodeList codeList) {

        String codeListName = getCodeListTypeName(codeList);
        /*
         * Issue #589
         */
        codeListName = Stream.of(codeListName.split("_"))
                .map(e -> camelCase(e)).collect(Collectors.joining("_"));

        if (!definitions.containsKey(codeListName)) {
            List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(codeListName, properties);
        }

        return "#/definitions/" + codeListName;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   AgencyIdList agencyIdList) {
        AgencyIdListValue agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueId());
        String agencyListTypeName = getAgencyListTypeName(agencyIdList, agencyIdListValue);
        /*
         * Issue #589
         */
        agencyListTypeName = Stream.of(agencyListTypeName.split("_"))
                .map(e -> camelCase(e)).collect(Collectors.joining("_"));
        if (!definitions.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValue> agencyIdListValues =
                    generationContext.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(agencyListTypeName, properties);
        }

        return "#/definitions/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ABIE abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        for (BIE bie : children) {
            if (bie instanceof BBIE) {
                BBIE bbie = (BBIE) bie;
                fillProperties(parent, definitions, bbie, generationContext);
            } else {
                ASBIE asbie = (ASBIE) bie;
                if (isAnyProperty(asbie, generationContext)) {
                    parent.put("additionalProperties", true);
                } else {
                    fillProperties(parent, definitions, asbie, generationContext);
                }
            }
        }
    }

    private Object readJsonValue(String textContent) {
        try {
            return mapper.readValue(textContent, Object.class);
        } catch (Exception e) {
            logger.warn("Can't read JSON value from given text: " + textContent, e);
        }
        return null;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BBIE bbie,
                                GenerationContext generationContext) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        BCCP bccp = generationContext.queryToBCCP(bcc);
        DT bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.getCardinalityMin();
        int maxVal = bbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = bbie.isNillable();

        String name = camelCase(bccp.getPropertyTerm());

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = bbie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        // Issue #596
        if (!StringUtils.isEmpty(bbie.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbie.getFixedValue()));
        } else if (!StringUtils.isEmpty(bbie.getDefaultValue())) {
            properties.put("default", bbie.getDefaultValue());
        }

        // Issue #692
        String exampleText = bbie.getExample();
        if (!StringUtils.isEmpty(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        // Issue #564
        String ref = getReference(definitions, bbie, bdt, generationContext);
        List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.getCardinalityMax() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            properties.put("$ref", ref);
            properties = oneOf(allOf(properties), isNillable);
        } else {
            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            Map<String, Object> contentProperties = new LinkedHashMap();
            contentProperties.put("$ref", ref);
            for (String key : Arrays.asList("enum", "default", "examples")) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", oneOf(allOf(contentProperties), isNillable));

            for (BBIESC bbieSc : bbieScList) {
                fillProperties(properties, definitions, bbieSc, generationContext);
            }
        }

        if (isArray) {
            String description = (String) properties.remove("description");
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();
            if (!StringUtils.isEmpty(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> allOf(Map<String, Object> properties) {
        if (properties.containsKey("$ref") && properties.size() > 1) {
            Map<String, Object> prop = new LinkedHashMap();
            Map<String, Object> refMap = ImmutableMap.<String, Object>builder()
                    .put("$ref", properties.remove("$ref"))
                    .build();
            prop.put("allOf", (properties.isEmpty()) ? Arrays.asList(refMap) : Arrays.asList(refMap, properties));

            return prop;
        }

        return properties;
    }

    private Map<String, Object> oneOf(Map<String, Object> properties,
                                      boolean isNillable) {
        if (isNillable) {
            Map<String, Object> prop = new LinkedHashMap();
            prop.put("oneOf", Arrays.asList(
                    ImmutableMap.builder()
                            .put("type", "null")
                            .build(),
                    properties
            ));

            return prop;
        }

        return properties;
    }

    private String getReference(Map<String, Object> definitions, BBIE bbie, DT bdt,
                                GenerationContext generationContext) {
        CodeList codeList = getCodeList(generationContext, bbie, bdt);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbie, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                Xbt xbt;
                if (bbie.getBdtPriRestriId() == null) {
                    BdtPriRestri bdtPriRestri =
                            generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
                    xbt = getXbt(generationContext, bdtPriRestri);
                } else {
                    BdtPriRestri bdtPriRestri =
                            generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
                    xbt = getXbt(generationContext, bdtPriRestri);
                }

                ref = fillDefinitions(definitions, xbt);
            }
        }

        return ref;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BBIESC bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.getCardinalityMin();
        int maxVal = bbieSc.getCardinalityMax();
        if (maxVal == 0) {
            return;
        }

        DTSC dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        String name = toName(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            return rt;
        }, true);
        Map<String, Object> properties = new LinkedHashMap();

        if (option.isBieDefinition()) {
            String definition = bbieSc.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596
        if (!StringUtils.isEmpty(bbieSc.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbieSc.getFixedValue()));
        } else if (!StringUtils.isEmpty(bbieSc.getDefaultValue())) {
            properties.put("default", bbieSc.getDefaultValue());
        }

        // Issue #692
        String exampleText = bbieSc.getExample();
        if (!StringUtils.isEmpty(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        CodeList codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbieSc, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                Xbt xbt;
                if (bbieSc.getDtScPriRestriId() == null) {
                    BdtScPriRestri bdtScPriRestri =
                            generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());
                    CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                            generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                } else {
                    BdtScPriRestri bdtScPriRestri =
                            generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
                    CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                            generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                }

                ref = fillDefinitions(definitions, xbt);
            }
        }

        properties.put("$ref", ref);
        properties = allOf(properties);

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
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
            copied.putAll(((Map<String, Object>) properties.get(key)));
            properties.put(key, copied);
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(SrtGuid.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".json");

        mapper.writeValue(tempFile, root);
        logger.info("JSON Schema is generated: " + tempFile);

        return tempFile;
    }
}
