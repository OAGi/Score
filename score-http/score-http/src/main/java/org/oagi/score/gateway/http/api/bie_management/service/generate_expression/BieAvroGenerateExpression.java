package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.BCCEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.toName;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieAvroGenerateExpression implements BieGenerateExpression, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper mapper;

    private AvroObject root;
    private GenerateExpressionOption option;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

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
    public GenerationContext generateContext(List<TopLevelAsbiep> topLevelAsbieps, GenerateExpressionOption option) {
        List<TopLevelAsbiep> mergedTopLevelAsbieps = new ArrayList(topLevelAsbieps);

        if (mergedTopLevelAsbieps.size() == 0) {
            throw new IllegalArgumentException("Cannot found BIEs.");
        }

        return applicationContext.getBean(GenerationContext.class, mergedTopLevelAsbieps);
    }

    @Override
    public void generate(TopLevelAsbiep topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private void generateTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
            fillProperties(asbiep, typeAbie, generationContext);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void fillProperties(ASBIEP asbiep, ABIE abie,
                                GenerationContext generationContext) {

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = Utility.first(asccp.getDen(), true);
        NamespaceList namespaceList = generationContext.findNamespace(asccp.getNamespaceId());
        String namespace = null;
        if (namespaceList != null) {
            URI uri;
            try {
                uri = new URI(namespaceList.getUri());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid URI in the namespace: " + namespaceList.getUri(), e);
            }

            List<String> tokensInHostname = Arrays.asList(uri.getHost().replace("www.", "").split("[\\.]"));
            Collections.reverse(tokensInHostname);
            namespace = String.join(".", tokensInHostname);
        }

        root = new AvroObject();
        root.setNamespace(namespace);
        root.setType("record");
        root.setName(name);
        root.setDocumentation(asbiep.getDefinition());

        fillProperties(root, abie, generationContext);
    }

    private void fillProperties(AvroObject parent,
                                ABIE abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        for (BIE bie : children) {
            if (bie instanceof BBIE) {
                BBIE bbie = (BBIE) bie;
                fillProperties(parent, bbie, generationContext);
            } else {
                ASBIE asbie = (ASBIE) bie;

                if (Helper.isAnyProperty(asbie, generationContext)) {
                    AvroObject anyPropertyObj = new AvroObject(parent);
                    anyPropertyObj.setName("anyProperty");
                    anyPropertyObj.setType("bytes");
                } else {
                    ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);

                    generationContext.referenceCounter().increase(asbiep)
                            .ifNotCircularReference(asbiep,
                                    () -> fillProperties(parent, asbie, generationContext))
                            .decrease(asbiep);
                }
            }
        }
    }

    private void fillProperties(AvroObject parent,
                                BBIE bbie,
                                GenerationContext generationContext) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        BCCP bccp = generationContext.queryToBCCP(bcc);
        if (bccp == null) {
            throw new IllegalStateException();
        }
        DT bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.getCardinalityMin();
        int maxVal = bbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = bbie.isNillable();

        boolean isAttribute = bcc.getEntityType() == BCCEntityType.Attribute.getValue();
        String name = Utility.second(bcc.getDen(), !isAttribute);
        List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.getCardinalityMax() != 0).collect(Collectors.toList());

        AvroObject avroObj = new AvroObject(parent);
        String type = getType(bbie, generationContext);
        if (!bbieScList.isEmpty()) {
            AvroObject contentObj = new AvroObject(avroObj);
            contentObj.setType(type);
            contentObj.setName("content");

            for (BBIESC bbieSc : bbieScList) {
                fillProperties(avroObj, bbieSc, generationContext);
            }

            type = "record";
        }

        avroObj.setType(type);
        avroObj.setName(name);
        avroObj.setDocumentation(bbie.getDefinition());
        avroObj.setArray(isArray);
        avroObj.setNullable(isNillable || minVal == 0);
    }

    private String getType(BBIE bbie, GenerationContext generationContext) {
        Xbt xbt;
        if (bbie.getBdtPriRestriId() == null) {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);
            xbt = Helper.getXbt(generationContext, bdtPriRestri);
        } else {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
            xbt = Helper.getXbt(generationContext, bdtPriRestri);
        }

        return (String) toProperties(xbt).get("type");
    }

    private Map<String, Object> toProperties(Xbt xbt) {
        String avroMap = xbt.getAvroMap();
        try {
            return mapper.readValue(avroMap, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fillProperties(AvroObject parent,
                                BBIESC bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.getCardinalityMin();
        int maxVal = bbieSc.getCardinalityMax();
        if (maxVal == 0) {
            return;
        }

        DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        String name = toName(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            if ("Identifier".equals(rt)) {
                return "ID";
            }
            return rt;
        }, false);

        String type = getType(bbieSc, generationContext);

        AvroObject avroObj = new AvroObject(parent);
        avroObj.setName(name);
        avroObj.setDocumentation(bbieSc.getDefinition());
        avroObj.setType(type);
        avroObj.setNullable(minVal == 0);
    }

    private String getType(BBIESC bbieSc, GenerationContext generationContext) {
        Xbt xbt;
        if (bbieSc.getDtScPriRestriId() == null) {
            BdtScPriRestri bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);
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

        return (String) toProperties(xbt).get("type");
    }

    private void fillProperties(AvroObject parent,
                                ASBIE asbie,
                                GenerationContext generationContext) {

        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = Utility.first(asccp.getDen(), true);

        int minVal = asbie.getCardinalityMin();
        int maxVal = asbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = asbie.isNillable();

        boolean reused = !asbie.getOwnerTopLevelAsbiepId().equals(asbiep.getOwnerTopLevelAsbiepId());
        ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
        ASCC ascc = generationContext.queryBasedASCC(asbie);

        AvroObject avroObj = new AvroObject(parent);
        String type = "record";
        fillProperties(avroObj, typeAbie, generationContext);

        avroObj.setType(type);
        avroObj.setName(name);
        avroObj.setDocumentation(asbie.getDefinition());
        avroObj.setArray(isArray);
        avroObj.setNullable(isNillable || minVal == 0);
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(ScoreGuid.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".avsc");

        mapper.writeValue(tempFile, root.toProperties());
        logger.info("Avro Schema is generated: " + tempFile);

        return tempFile;
    }

    private class AvroObject {

        private AvroObject parent;

        private String type;

        private String name;

        private String recordName;

        private String namespace;

        private String documentation;

        private boolean array;

        private boolean nullable;

        private List<AvroObject> children = new ArrayList<>();

        public AvroObject() {
            this(null);
        }

        public AvroObject(AvroObject parent) {
            setParent(parent);
            if (parent != null) {
                parent.addChild(this);
            }
        }

        public AvroObject getParent() {
            return parent;
        }

        public void setParent(AvroObject parent) {
            this.parent = parent;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRecordName() {
            if (recordName == null) {
                if (this.getParent() != null) {
                    recordName = this.getParent().getRecordName() + getName();
                } else {
                    recordName = getName();
                }
            }
            return recordName;
        }

        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            this.documentation = documentation;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public boolean isRecord() {
            return "record".equals(this.getType());
        }

        public List<AvroObject> getChildren() {
            return children;
        }

        public void addChild(AvroObject child) {
            this.children.add(child);
        }

        public Map<String, Object> toProperties() {
            Map<String, Object> properties = new LinkedHashMap<>();
            if (getNamespace() != null) {
                properties.put("namespace", getNamespace());
            }
            properties.put("type", getType());

            boolean isRecord = isRecord();
            boolean isArray = isArray();
            boolean isNullable = isNullable();
            if (isRecord) {
                properties.put("name", getRecordName());
            } else {
                properties.put("name", getName());
            }
            if (option.isBieDefinition() && StringUtils.hasLength(getDocumentation())) {
                properties.put("doc", getDocumentation());
            }
            if (isRecord) {
                properties.put("fields", getChildren().stream()
                        .map(e -> e.toProperties()).collect(Collectors.toList()));
            }

            if (isArray) {
                Map<String, Object> arrayProperties = new LinkedHashMap<>();
                arrayProperties.put("type", "array");
                arrayProperties.put("name", getName());
                arrayProperties.put("items", properties);
                properties = arrayProperties;
            }

            boolean isRoot = this.parent == null;
            if (!isRoot) {
                if (isRecord || isArray) {
                    Map<String, Object> wrappedProperties = new LinkedHashMap<>();
                    wrappedProperties.put("name", getName());
                    wrappedProperties.put("type", (isNullable) ? Arrays.asList("null", properties) : properties);
                    properties = wrappedProperties;
                } else if (isNullable) {
                    properties.put("type", Arrays.asList("null", properties.get("type")));
                }
            }

            return properties;
        }
    }
}
