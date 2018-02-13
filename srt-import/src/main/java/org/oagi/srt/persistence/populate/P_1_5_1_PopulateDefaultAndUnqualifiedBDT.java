package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.populate.backup.P_1_5_PopulateDefaultAndUnqualifiedBDT;
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.DataTypeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.persistence.populate.script.oracle.OracleDataImportScriptPrinter.printTitle;
import static org.oagi.srt.repository.entity.DataTypeType.BusinessDataType;

/**
 * Created by hakju on 6/24/2016.
 */
@Component
public class P_1_5_1_PopulateDefaultAndUnqualifiedBDT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private ImportUtil importUtil;

    @Autowired
    private DataTypeDAO dataTypeDAO;

    @Autowired
    private P_1_2_PopulateCDTandCDTSC populateCDTandCDTSC;

    @Autowired
    private P_1_5_PopulateDefaultAndUnqualifiedBDT populateDefaultAndUnqualifiedBDT;

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_5_1_PopulateDefaultAndUnqualifiedBDT populateBDTsInDT = ctx.getBean(P_1_5_1_PopulateDefaultAndUnqualifiedBDT.class);
            populateBDTsInDT.run(ctx);
        }
    }

    private class BDTContext {
        private Document document;
        private Element element;
        private Module module;
        private Long userId;
        private Long releaseId;
        private int seq;

        private DataType dataType;
        private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = new ArrayList();
        private List<DataTypeSupplementaryComponent> dtScList = new ArrayList();
        private List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = new ArrayList();
        private List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList = new ArrayList();
        private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = new ArrayList();

        public BDTContext(DataType dataType) {
            this.dataType = dataType;
            this.bdtPriRestriList = bdtPriRestriRepository.findByBdtId(dataType.getDtId());
            this.dtScList = dtScRepository.findByOwnerDtId(dataType.getDtId());
        }

        public BDTContext(Document document, Element element, Module module, Long userId, Long releaseId, int seq) {
            this.document = document;
            this.element = element;
            this.module = module;
            this.userId = userId;
            this.releaseId = releaseId;
            this.seq = seq;
        }

        public boolean isComplexType() {
            return element.getNodeName().contains("complexType");
        }

        public boolean isSimpleType() {
            return element.getNodeName().contains("simpleType");
        }

        public Element element() {
            return element;
        }

        public boolean isDefaultBDT() {
            return module.getModule().contains("BusinessDataType_1");
        }

        public Element evaluate(String xPathExpression) {
            return evaluate(xPathExpression, element);
        }

        public Element evaluate(String xPathExpression, Object item) {
            try {
                return (Element) Context.xPath.evaluate(xPathExpression, item, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }
        }

        public NodeList evaluateNodeList(String xPathExpression) {
            try {
                return (NodeList) Context.xPath.evaluate(xPathExpression, element, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }
        }

        public String getDen() {
            return dataType.getDen();
        }

        public String getAttribute(String name) {
            return element.getAttribute(name);
        }

        private String toDataTypeTerm(String typeName) {
            String basedDtDataTypeTerm = typeName.substring(0, typeName.indexOf("Type"));
            return basedDtDataTypeTerm.replaceAll("([A-Z][a-z])", " $1")
                    .replaceAll("ID", "Identifier").trim();
        }

        public void ensureSimpleTypeDefaultBDT() {
            Element restriction = evaluate(".//xsd:restriction");
            if (restriction != null) { // if this BDT is an union type, it has not xsd:restriction
                String typeName = restriction.getAttribute("base");
                if (xbtRepository.findOneByBuiltInType(typeName) == null) {
                    throw new IllegalStateException();
                }
            }
        }

        public DataType buildDataType() {
            return buildDataType(null);
        }

        public DataType buildDataType(BDTContext basedBDTContext) {
            String typeName = getAttribute("name");
            String id = getAttribute("id");
            String basedDtDataTypeTerm = toDataTypeTerm(typeName);

            DataType basedDt = (basedBDTContext != null) ? basedBDTContext.dataType : null;
            if (basedDt == null) {
                basedDt = dataTypeRepository.findOneByDataTypeTermAndType(basedDtDataTypeTerm, DataTypeType.CoreDataType);
            }
            if (basedDt == null) {
                throw new IllegalStateException();
            }

            String den = Utility.typeToDen(typeName);
            String contentComponentDen = Utility.typeToContent(typeName);

            dataType = new DataType();

            dataType.setGuid(id);
            dataType.setType(BusinessDataType);
            dataType.setVersionNum("1.0");
            dataType.setDataTypeTerm(basedDt.getDataTypeTerm());
            dataType.setBasedDtId(basedDt.getDtId());
            dataType.setDen(den);
            dataType.setContentComponentDen(contentComponentDen);
            dataType.setState(CoreComponentState.Published);
            dataType.setCreatedBy(userId);
            dataType.setLastUpdatedBy(userId);
            dataType.setOwnerUserId(userId);
            dataType.setRevisionDoc(null);
            dataType.setRevisionNum(0);
            dataType.setRevisionTrackingNum(0);
            dataType.setDeprecated(false);
            dataType.setReleaseId(releaseId);
            dataType.setModule(module);

            Element documentationNode = evaluate(".//xsd:annotation/xsd:documentation");
            if (documentationNode != null) {
                String definition = ImportUtil.getCctsDefinition(documentationNode);
                if (definition == null) {
                    definition = ImportUtil.toString(documentationNode.getChildNodes());
                }
                dataType.setDefinition(definition);

                String definitionSource = documentationNode.getAttribute("source");
                if (!StringUtils.isEmpty(definitionSource)) {
                    dataType.setDefinitionSource(definitionSource);
                }
            }

            Element ccDocumentationNode = evaluate(
                    ".//xsd:extension/xsd:annotation/xsd:documentation | .//xsd:restriction/xsd:annotation/xsd:documentation | .//xsd:union/xsd:annotation/xsd:documentation");
            if (ccDocumentationNode != null) {
                String ccDefinition = ImportUtil.getCctsDefinition(ccDocumentationNode);
                if (ccDefinition == null) {
                    ccDefinition = ImportUtil.toString(ccDocumentationNode.getChildNodes());
                }
                dataType.setContentComponentDefinition(ccDefinition);

                String definitionSource = ccDocumentationNode.getAttribute("source");
                if (!StringUtils.isEmpty(definitionSource)) {
                    dataType.setDefinitionSource(definitionSource);
                }
            }

            dataType = dataTypeDAO.save(dataType);
            return dataType;
        }

        public List<XSDBuiltInType> findXSDBuiltInType() {
            return findXSDBuiltInType(element);
        }

        private List<XSDBuiltInType> findXSDBuiltInType(Element element) {
            Element unionNode = evaluate(".//xsd:union", element);
            String xsdBuiltinType;
            if (unionNode != null) {
                String memberTypes = unionNode.getAttribute("memberTypes");
                StringTokenizer tokenizer = new StringTokenizer(memberTypes, " ");
                List<XSDBuiltInType> xbtList = new ArrayList();
                while (tokenizer.hasMoreTokens()) {
                    xsdBuiltinType = tokenizer.nextToken();
                    xbtList.add(xbtRepository.findOneByBuiltInType(xsdBuiltinType));
                }
                return xbtList;
            } else {
                Element scNode = evaluate(".//xsd:simpleContent/xsd:extension", element);
                if (scNode != null) {
                    xsdBuiltinType = scNode.getAttribute("base");

                    if (!xsdBuiltinType.startsWith("xsd") && !xsdBuiltinType.startsWith("xbt")) {
                        Element baseNode = evaluate(
                                "//xsd:simpleType[@name='" + xsdBuiltinType + "'] | //xsd:complexType[@name='" + xsdBuiltinType + "']", document);
                        return findXSDBuiltInType(baseNode);
                    }
                } else {
                    scNode = evaluate(".//xsd:restriction", element);
                }
                if (scNode == null) {
                    throw new IllegalStateException();
                }

                xsdBuiltinType = scNode.getAttribute("base");
            }

            return Arrays.asList(xbtRepository.findOneByBuiltInType(xsdBuiltinType));
        }

        public List<BusinessDataTypePrimitiveRestriction> buildBdtPriRestriList(BDTContext basedBDTContext) {
            if (basedBDTContext != null) {
                for (BusinessDataTypePrimitiveRestriction basedBdtPriRestri : basedBDTContext.bdtPriRestriList) {
                    BusinessDataTypePrimitiveRestriction bdtPriRestri = basedBdtPriRestri.clone();
                    bdtPriRestri.setBdtId(dataType.getDtId());
                    bdtPriRestriList.add(bdtPriRestri);
                }

                bdtPriRestriList = bdtPriRestriRepository.save(bdtPriRestriList);
                return bdtPriRestriList;
            } else {
                return buildBdtPriRestriList();
            }
        }

        public List<BusinessDataTypePrimitiveRestriction> buildBdtPriRestriList() {
            List<XSDBuiltInType> xbtList = findXSDBuiltInType();
            if (xbtList.isEmpty()) {
                throw new IllegalStateException();
            }

            Long cdtId = dataType.getBasedDtId();
            List<CoreDataTypeAllowedPrimitive> cdtAwdPriList = cdtAwdPriRepository.findByCdtId(cdtId);
            if (cdtAwdPriList.isEmpty()) {
                throw new IllegalStateException();
            }

            Long bdtId = dataType.getDtId();

            boolean isUnion = (xbtList.size() != 1);
            List<Long> xbtAwdIds = xbtList.stream().map(XSDBuiltInType::getXbtId).distinct().collect(Collectors.toList());
            XSDBuiltInType tokenXbt = xbtRepository.findOneByBuiltInType("xsd:token");

            // 'xsd:float' has two mapped in *_xps_type_map, 'Double' and 'Float'. Do choose more specific one.
            boolean isFloat = (isUnion) ? false : "xsd:float".equals(xbtList.get(0).getBuiltInType());
            List<Long> cdtAwdPriIds = cdtAwdPriList.stream().map(e -> e.getCdtAwdPriId()).distinct().collect(Collectors.toList());
            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList =
                    cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriIdIn(cdtAwdPriIds);
            cdtAwdPriXpsTypeMapList = cdtAwdPriXpsTypeMapList.stream()
                    .filter(e -> {
                        if (isUnion) {
                            if (tokenXbt.getXbtId() == e.getXbtId()) {
                                return true;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());

            if (isFloat && cdtAwdPriXpsTypeMapList.size() > 1) {
                cdtAwdPriXpsTypeMapList = cdtAwdPriXpsTypeMapList.stream()
                        .filter(e -> {
                            Long cdtPriId = cdtAwdPriRepository.findOne(e.getCdtAwdPriId()).getCdtPriId();
                            CoreDataTypePrimitive cdtPri = cdtPriRepository.findOne(cdtPriId);
                            return "Float".equals(cdtPri.getName());
                        }).collect(Collectors.toList());
            }

            for (CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap : cdtAwdPriXpsTypeMapList) {
                BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
                bdtPriRestri.setBdtId(bdtId);
                bdtPriRestri.setCdtAwdPriXpsTypeMapId(cdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId());
                boolean isDefault;
                if (isFloat) {
                    Long cdtPriId = cdtAwdPriRepository.findOne(cdtAwdPriXpsTypeMap.getCdtAwdPriId()).getCdtPriId();
                    CoreDataTypePrimitive cdtPri = cdtPriRepository.findOne(cdtPriId);
                    isDefault = "Float".equals(cdtPri.getName());
                } else {
                    isDefault = (isUnion) ? (cdtAwdPriXpsTypeMap.getXbtId() == tokenXbt.getXbtId()) :
                            (xbtList.get(0).getXbtId() == cdtAwdPriXpsTypeMap.getXbtId());
                }

                bdtPriRestri.setDefault(isDefault);
                bdtPriRestriList.add(bdtPriRestri);
            }

            if (bdtPriRestriList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
                throw new IllegalStateException("Only one BusinessDataTypePrimitiveRestriction can allow to have that isDefault value is true.");
            }

            bdtPriRestriList = bdtPriRestriRepository.save(bdtPriRestriList);
            return bdtPriRestriList;
        }

        private List<DataTypeSupplementaryComponent> buildDtScList() {
            return buildDtScList(null);
        }

        private List<DataTypeSupplementaryComponent> buildDtScList(BDTContext basedBDTContext) {
            List<DataTypeSupplementaryComponent> basedDtScList;
            if (basedBDTContext == null) {
                long cdtId = dataType.getBasedDtId();
                basedDtScList = dtScRepository.findByOwnerDtId(cdtId);
            } else {
                basedDtScList = basedBDTContext.dtScList;
            }

            Map<String, DataTypeSupplementaryComponent> basedDtScMap =
                    basedDtScList.stream()
                            .collect(Collectors.toMap(e -> e.getPropertyTerm() + " " + e.getRepresentationTerm(), Function.identity()));

            if (isComplexType()) {
                NodeList attributesNodeList = evaluateNodeList(".//xsd:simpleContent/xsd:extension/xsd:attribute");

                for (int i = 0, len = attributesNodeList.getLength(); i < len; ++i) {
                    Element attributeElement = (Element) attributesNodeList.item(i);
                    String id = attributeElement.getAttribute("id");
                    String name = attributeElement.getAttribute("name");

                    String propertyTerm = Utility.spaceSeparator(name);
                    String representationTerm = (propertyTerm.contains(" ")) ? Utility.getRepresentationTerm(name) : "Text";
                    if (!"Text".equals(representationTerm)) {
                        int idx = propertyTerm.lastIndexOf(" ");
                        if (idx != -1) {
                            propertyTerm = propertyTerm.substring(0, idx);
                        }
                    }

                    DataTypeSupplementaryComponent basedDtSc = basedDtScMap.remove(propertyTerm + " " + representationTerm);
                    boolean isNewSupplementaryComponent = (basedDtSc == null);
                    DataTypeSupplementaryComponent dtSc = (basedDtSc != null) ? basedDtSc.clone() : new DataTypeSupplementaryComponent();

                    dtSc.setPropertyTerm(propertyTerm);
                    dtSc.setRepresentationTerm(representationTerm);
                    dtSc.setGuid((StringUtils.isEmpty(id)) ? Utility.generateGUID() : id);

                    Element documentationNode = evaluate(".//xsd:annotation/xsd:documentation", attributeElement);
                    if (documentationNode != null) {
                        String definition = ImportUtil.getCctsDefinition(documentationNode);
                        if (definition == null) {
                            definition = ImportUtil.toString(documentationNode.getChildNodes());
                        }
                        dtSc.setDefinition(definition);

                        String definitionSource = documentationNode.getAttribute("source");
                        if (!StringUtils.isEmpty(definitionSource)) {
                            dtSc.setDefinitionSource(definitionSource);
                        }
                    }

                    dtSc.setOwnerDtId(dataType.getDtId());

                    String use = attributeElement.getAttribute("use");
                    int cardinalityMin = 0;
                    int cardinalityMax = 1;
                    if (!StringUtils.isEmpty(use)) {
                        switch (use) {
                            case "optional":
                                cardinalityMin = 0;
                                cardinalityMax = 1;
                                break;
                            case "required":
                                cardinalityMin = 1;
                                cardinalityMax = 1;
                                break;
                            case "prohibited":
                                cardinalityMin = 0;
                                cardinalityMax = 0;
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                    }

                    dtSc.setCardinalityMin(cardinalityMin);
                    dtSc.setCardinalityMax(cardinalityMax);

                    if (basedDtSc != null) {
                        dtSc.setBasedDtScId(basedDtSc.getDtScId());
                    }

                    dtSc = dataTypeDAO.save(dtSc);
                    dtScList.add(dtSc);

                    List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = new ArrayList();
                    if (isNewSupplementaryComponent) {
                        cdtScAwdPriList = buildCdtScAwdPri(dtSc, representationTerm);
                    }

                    if (!cdtScAwdPriList.isEmpty()) {
                        this.cdtScAwdPriList.addAll(cdtScAwdPriList);
                    }

                    String type = attributeElement.getAttribute("type");
                    if (type.endsWith("ContentType")) {
                        type = type.substring(0, type.lastIndexOf("ContentType"));
                    }

                    buildBdtScPriRestri(type, dtSc, cdtScAwdPriList);
                }
            }

            List<DataTypeSupplementaryComponent> remainingBasedDtScList = new ArrayList(basedDtScMap.values());
            Collections.sort(remainingBasedDtScList, Comparator.comparingLong(DataTypeSupplementaryComponent::getDtScId));

            for (DataTypeSupplementaryComponent basedDtSc : remainingBasedDtScList) {
                DataTypeSupplementaryComponent dtSc = basedDtSc.clone();
                dtSc.setGuid(Utility.generateGUID());
                dtSc.setOwnerDtId(dataType.getDtId());
                if (isDefaultBDT()) {
                    dtSc.setCardinalityMin(0);
                    dtSc.setCardinalityMax(0);
                }
                dtSc.setBasedDtScId(basedDtSc.getDtScId());

                dtSc = dataTypeDAO.save(dtSc);
                dtScList.add(dtSc);

                buildBdtScPriRestriByBasedBDT(dtSc);
            }

            if (!dtScList.isEmpty() && bdtScPriRestriList.isEmpty()) {
                throw new IllegalStateException();
            }

            return dtScList;
        }

        private List<CoreDataTypeSupplementaryComponentAllowedPrimitive> buildCdtScAwdPri(
                DataTypeSupplementaryComponent dtSc, String representationTerm) {
            List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = new ArrayList();

            DataType cdt = dataTypeRepository.findOneByDataTypeTermAndType(representationTerm, DataTypeType.CoreDataType);
            List<CoreDataTypeAllowedPrimitive> cdtAwdPriList = cdtAwdPriRepository.findByCdtId(cdt.getDtId());

            for (CoreDataTypeAllowedPrimitive cdtAwdPri : cdtAwdPriList) {
                // build cdt_sc_awd_pri
                CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                cdtScAwdPri.setCdtScId(dtSc.getDtScId());
                cdtScAwdPri.setCdtPriId(cdtAwdPri.getCdtPriId());
                cdtScAwdPri.setDefault(cdtAwdPri.isDefault());
                cdtScAwdPri = cdtScAwdPriRepository.saveAndFlush(cdtScAwdPri);

                cdtScAwdPriList.add(cdtScAwdPri);

                CoreDataTypePrimitive cdtPri = cdtPriRepository.findOne(cdtAwdPri.getCdtPriId());
                List<XSDBuiltInType> xbtList = populateCDTandCDTSC.findXSDBuiltInTypesByCdtPri(cdtPri);
                if (xbtList.isEmpty()) {
                    throw new IllegalStateException();
                }

                // build cdt_sc_awd_pri_xps_type_map
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList = new ArrayList();
                for (XSDBuiltInType xbt : xbtList) {
                    CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                            new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                    cdtScAwdPriXpsTypeMap.setCdtScAwdPriId(cdtScAwdPri.getCdtScAwdPriId());
                    cdtScAwdPriXpsTypeMap.setXbtId(xbt.getXbtId());
                    cdtScAwdPriXpsTypeMap = cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtScAwdPriXpsTypeMap);

                    cdtScAwdPriXpsTypeMapList.add(cdtScAwdPriXpsTypeMap);
                }
            }

            return cdtScAwdPriList;
        }

        private void buildBdtScPriRestri(String type, DataTypeSupplementaryComponent dtSc,
                                         List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList) {
            CodeList codeList = codeListRepository.findOneByName(type);
            AgencyIdList agencyIdList = null;
            XSDBuiltInType xbt = null;
            if (codeList == null) {
                agencyIdList = agencyIdListRepository.findOneByName(type);
                if (agencyIdList == null) {
                    xbt = xbtRepository.findOneByBuiltInType(type);
                    if (xbt == null) {
                        throw new IllegalStateException();
                    }
                }
            }

            if (agencyIdList != null) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                        new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

                bdtScPriRestri.setBdtScId(dtSc.getDtScId());
                bdtScPriRestri.setDefault(false);
                bdtScPriRestri.setAgencyIdListId(agencyIdList.getAgencyIdListId());
                bdtScPriRestri = bdtScPriRestriRepository.saveAndFlush(bdtScPriRestri);
                bdtScPriRestriList.add(bdtScPriRestri);

                buildBdtScPriRestriByToken(dtSc, cdtScAwdPriList);
            } else if (codeList != null) {

                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                        new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

                bdtScPriRestri.setBdtScId(dtSc.getDtScId());
                bdtScPriRestri.setDefault(false);
                bdtScPriRestri.setCodeListId(codeList.getCodeListId());
                bdtScPriRestri = bdtScPriRestriRepository.saveAndFlush(bdtScPriRestri);
                bdtScPriRestriList.add(bdtScPriRestri);

                buildBdtScPriRestriByToken(dtSc, cdtScAwdPriList);
            } else {
                buildBdtScPriRestriByBasedBDT(dtSc, cdtScAwdPriList, xbt);
            }
        }

        private void buildBdtScPriRestriByToken(DataTypeSupplementaryComponent dtSc,
                                                List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList) {
            if (cdtScAwdPriList.isEmpty()) {
                cdtScAwdPriList = findCdtScAwdPriList(dtSc);
            }

            Long xbtId = xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap = null;
            for (CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri : cdtScAwdPriList) {
                cdtScAwdPriXpsTypeMap = cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriIdAndXbtId(
                        cdtScAwdPri.getCdtScAwdPriId(), xbtId);
                if (cdtScAwdPriXpsTypeMap != null) {
                    break;
                }
            }

            if (cdtScAwdPriXpsTypeMap == null) {
                throw new IllegalStateException();
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

            bdtScPriRestri.setBdtScId(dtSc.getDtScId());
            bdtScPriRestri.setDefault(true);
            bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId());
            bdtScPriRestri = bdtScPriRestriRepository.saveAndFlush(bdtScPriRestri);

            bdtScPriRestriList.add(bdtScPriRestri);
        }

        private List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findCdtScAwdPriList(DataTypeSupplementaryComponent dtSc) {
            DataTypeSupplementaryComponent basedDtSc = dtScRepository.findOne(dtSc.getBasedDtScId());
            List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = Collections.emptyList();
            while (basedDtSc != null) {
                cdtScAwdPriList = cdtScAwdPriRepository.findByCdtScId(basedDtSc.getDtScId());
                if (cdtScAwdPriList.isEmpty()) {
                    basedDtSc = dtScRepository.findOne(basedDtSc.getBasedDtScId());
                } else {
                    break;
                }
            }

            if (cdtScAwdPriList.isEmpty()) {
                throw new IllegalStateException();
            }

            return cdtScAwdPriList;
        }

        private void buildBdtScPriRestriByBasedBDT(DataTypeSupplementaryComponent dtSc) {
            buildBdtScPriRestriByBasedBDT(dtSc, Collections.emptyList(), null);
        }

        private void buildBdtScPriRestriByBasedBDT(DataTypeSupplementaryComponent dtSc,
                                                   List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList,
                                                   XSDBuiltInType defaultXbt) {

            if (cdtScAwdPriList.isEmpty()) {
                cdtScAwdPriList = findCdtScAwdPriList(dtSc);
            }

            List<Long> cdtScAwdPriIds = cdtScAwdPriList.stream().map(e -> e.getCdtScAwdPriId()).collect(Collectors.toList());
            List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList =
                    cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriIdIn(cdtScAwdPriIds);

            Map<Long, CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriMap = cdtScAwdPriList.stream().collect(Collectors.toMap(
                    CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId, Function.identity()));

            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = new ArrayList();
            for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap : cdtScAwdPriXpsTypeMapList) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                        new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

                bdtScPriRestri.setBdtScId(dtSc.getDtScId());
                bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId());
                if (defaultXbt != null) {
                    if ("xsd:float".equals(defaultXbt.getBuiltInType())) {
                        if (cdtScAwdPriXpsTypeMap.getXbtId() == defaultXbt.getXbtId()) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri =
                                    cdtScAwdPriRepository.findOne(cdtScAwdPriXpsTypeMap.getCdtScAwdPriId());
                            CoreDataTypePrimitive cdtPri = cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId());
                            bdtScPriRestri.setDefault("Float".equals(cdtPri.getName()));
                        }

                    } else {
                        bdtScPriRestri.setDefault(cdtScAwdPriXpsTypeMap.getXbtId() == defaultXbt.getXbtId());
                    }
                } else {
                    CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri =
                            cdtScAwdPriMap.get(cdtScAwdPriXpsTypeMap.getCdtScAwdPriId());
                    bdtScPriRestri.setDefault(cdtScAwdPri.isDefault());
                }

                bdtScPriRestri = bdtScPriRestriRepository.saveAndFlush(bdtScPriRestri);

                bdtScPriRestriList.add(bdtScPriRestri);
            }

            if (bdtScPriRestriList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
                throw new IllegalStateException("Only one BusinessDataTypeSupplementaryComponentPrimitiveRestriction can allow to have that isDefault value is true.");
            }

            this.bdtScPriRestriList.addAll(bdtScPriRestriList);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(dataType).append("\n");

            if (!bdtPriRestriList.isEmpty()) {
                for (BusinessDataTypePrimitiveRestriction bdtPriRestri : bdtPriRestriList) {
                    sb.append(bdtPriRestri).append("\n");
                }
                sb.append("\n");
            }

            if (!dtScList.isEmpty()) {
                for (DataTypeSupplementaryComponent dtSc : dtScList) {
                    sb.append(dtSc).append("\n");
                }
                sb.append("\n");
            }

            if (!cdtScAwdPriList.isEmpty()) {
                for (CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri : cdtScAwdPriList) {
                    sb.append(cdtScAwdPri).append("\n");
                }
                sb.append("\n");
            }

            if (!cdtScAwdPriXpsTypeMapList.isEmpty()) {
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap : cdtScAwdPriXpsTypeMapList) {
                    sb.append(cdtScAwdPriXpsTypeMap).append("\n");
                }
                sb.append("\n");
            }

            if (!bdtScPriRestriList.isEmpty()) {
                for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri : bdtScPriRestriList) {
                    sb.append(bdtScPriRestri).append("\n");
                }
                sb.append("\n");
            }

            sb.append("\n");

            return sb.toString();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.5 Start");

        populateCDTandCDTSC.init();

        printTitle("Populate default and unqualified BDTs");

        importDefaultBDTs();
        importUnqualifiedBDTs();

        populateDefaultAndUnqualifiedBDT.importCodeContentType();
        populateDefaultAndUnqualifiedBDT.importIDContentType();

        logger.info("### 1.5 End");
    }

    private void importDefaultBDTs() throws Exception {
        File defaultBDTFile = new File(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH).getCanonicalFile();
        String moduleName = defaultBDTFile.getName();
        moduleName = moduleName.substring(0, moduleName.indexOf(".xsd"));
        Module module = moduleRepository.findByModuleEndsWith(moduleName);
        Long userId = importUtil.getUserId();
        Long releaseId = importUtil.getReleaseId();

        Document businessDataTypeXsd = Context.loadDocument(defaultBDTFile);
        NodeList nodeList = (NodeList) Context.xPath.evaluate(
                "//xsd:complexType | //xsd:simpleType", businessDataTypeXsd, XPathConstants.NODESET);

        List<BDTContext> bdtContexts = new ArrayList();
        int seq = 0;
        for (int i = 0, len = nodeList.getLength(); i < len; ++i) {
            Element defaultBDTElement = (Element) nodeList.item(i);

            BDTContext bdtContext = new BDTContext(businessDataTypeXsd, defaultBDTElement, module, userId, releaseId, ++seq);
            bdtContexts.add(bdtContext);
        }

        List<BDTContext> defaultSimpleTypeBDTContexts = bdtContexts.stream()
                .filter(e -> e.isSimpleType())
                .collect(Collectors.toList());

        List<BDTContext> defaultComplexTypeBDTContexts = bdtContexts.stream()
                .filter(e -> e.isComplexType())
                .collect(Collectors.toList());

        if (bdtContexts.size() != (defaultSimpleTypeBDTContexts.size() + defaultComplexTypeBDTContexts.size())) {
            throw new IllegalStateException();
        }

        // populate for default simpleType BDT
        for (BDTContext bdtContext : defaultSimpleTypeBDTContexts) {
            bdtContext.ensureSimpleTypeDefaultBDT();

            bdtContext.buildDataType();
            bdtContext.buildBdtPriRestriList();
            bdtContext.buildDtScList();
        }

        // populate for default complexType BDT
        Map<String, BDTContext> defaultSimpleTypeBDTContextMap = defaultSimpleTypeBDTContexts.stream()
                .collect(Collectors.toMap(BDTContext::getDen, Function.identity()));

        for (BDTContext bdtContext : defaultComplexTypeBDTContexts) {
            Element extension = bdtContext.evaluate(".//xsd:simpleContent/xsd:extension");
            String typeName = extension.getAttribute("base");

            BDTContext basedBDTContext = null;
            if (!typeName.startsWith("xsd")) {
                String basedDataTypeDen = Utility.typeToDen(typeName);
                basedBDTContext = defaultSimpleTypeBDTContextMap.get(basedDataTypeDen);
            }

            bdtContext.buildDataType(basedBDTContext);
            bdtContext.buildBdtPriRestriList(basedBDTContext);
            bdtContext.buildDtScList(basedBDTContext);
        }
    }

    private void importUnqualifiedBDTs() throws Exception {
        File unqualifiedBDTFile = new File(ImportConstants.FIELDS_XSD_FILE_PATH).getCanonicalFile();
        String moduleName = unqualifiedBDTFile.getName();
        moduleName = moduleName.substring(0, moduleName.indexOf(".xsd"));
        Module module = moduleRepository.findByModuleEndsWith(moduleName);
        Long userId = importUtil.getUserId();
        Long releaseId = importUtil.getReleaseId();

        Document fieldsXsd = Context.loadDocument(unqualifiedBDTFile);

        List<BDTContext> bdtContexts = new ArrayList();
        int seq = 0;
        for (String cctsDataType : Types.dataTypeList) {
            Element unqualifiedBDTElement = (Element) Context.xPath.evaluate(
                    "//xsd:complexType[@name='" + cctsDataType + "'] | //xsd:simpleType[@name='" + cctsDataType + "']", fieldsXsd, XPathConstants.NODE);
            if (unqualifiedBDTElement == null) {
                throw new IllegalStateException();
            }

            BDTContext bdtContext = new BDTContext(fieldsXsd, unqualifiedBDTElement, module, userId, releaseId, ++seq);
            String base;
            if (bdtContext.isComplexType()) {
                base = bdtContext.evaluate(".//xsd:simpleContent/xsd:extension").getAttribute("base");
            } else {
                base = bdtContext.evaluate(".//xsd:restriction").getAttribute("base");
            }

            String den = Utility.typeToDen(base);
            List<DataType> basedDataType = dataTypeRepository.findByDen(den);
            if (basedDataType.size() != 1) {
                throw new IllegalStateException();
            }

            BDTContext basedBDTContext = new BDTContext(basedDataType.get(0));
            bdtContext.buildDataType(basedBDTContext);
            bdtContext.buildBdtPriRestriList(basedBDTContext);
            bdtContext.buildDtScList(basedBDTContext);

            bdtContexts.add(bdtContext);
        }
    }

}