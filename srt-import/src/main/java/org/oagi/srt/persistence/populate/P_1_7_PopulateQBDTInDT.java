package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSSchema;
import com.sun.xml.internal.xsom.XSType;
import com.sun.xml.internal.xsom.parser.SchemaDocument;
import com.sun.xml.internal.xsom.parser.XSOMParser;
import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.persistence.populate.helper.ElementDecl;
import org.oagi.srt.persistence.populate.helper.TypeDecl;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import java.io.File;
import java.util.*;

import static org.oagi.srt.common.ImportConstants.PLATFORM_PATH;
import static org.oagi.srt.common.SRTConstants.AGENCY_ID_LIST_NAME;
import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_7_PopulateQBDTInDT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ImportUtil importUtil;

    @Autowired
    private P_1_5_PopulateDefaultAndUnqualifiedBDT populateDefaultAndUnqualifiedBDT;

    private XPathHandler fields_xsd;

    private class DataTypeInfoHolder {
        private String typeName;
        private String guid;
        private String definition;
        private String baseTypeName;
        private Element typeElement;
        private String uri;
        private Module module;

        public DataTypeInfoHolder(String typeName, String guid, String definition,
                                  String baseTypeName, Element typeElement, String uri, Module module) {
            this.typeName = typeName;
            this.guid = guid;
            this.definition = definition;
            this.baseTypeName = baseTypeName;
            this.typeElement = typeElement;
            this.uri = uri;
            this.module = module;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getGuid() {
            return guid;
        }

        public String getDefinition() {
            return definition;
        }

        public String getBaseTypeName() {
            return baseTypeName;
        }

        public Module getModule() {
            return module;
        }

        @Override
        public String toString() {
            return "DataTypeInfoHolder{" +
                    "typeName='" + typeName + '\'' +
                    ", guid='" + guid + '\'' +
                    ", definition='" + definition + '\'' +
                    ", baseTypeName='" + baseTypeName + '\'' +
                    ", typeElement=" + typeElement +
                    ", uri='" + uri + '\'' +
                    ", module='" + module + '\'' +
                    '}';
        }
    }

    private Map<String, DataTypeInfoHolder> dtiHolderMap;

    private void prepareForBCCP(String... systemIds) throws Exception {
        dtiHolderMap = new HashMap();

        XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
        for (String systemId : systemIds) {
            xsomParser.parse(systemId);
        }

        for (SchemaDocument schemaDocument : xsomParser.getDocuments()) {
            XSSchema xsSchema = schemaDocument.getSchema();
            Iterator<XSType> xsTypeIterator = xsSchema.iterateTypes();
            while (xsTypeIterator.hasNext()) {
                XSType xsType = xsTypeIterator.next();
                String typeName = xsType.getName();
                if (dtiHolderMap.containsKey(typeName)) {
                    return;
                }

                Locator locator = xsType.getLocator();
                if (locator == null) {
                    return;
                }
                String systemId = locator.getSystemId();
                Document xmlDocument = Context.loadDocument(systemId);

                XPathExpression xPathExpression = Context.xPath.compile("//xsd:complexType[@name='" + typeName + "']");
                Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
                if (node == null) {
                    xPathExpression = Context.xPath.compile("//xsd:simpleType[@name='" + typeName + "']");
                    node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
                    if(node == null) {
                        return;
                    }
                }
                Node idAttribute = node.getAttributes().getNamedItem("id");
                if (idAttribute == null) {
                    return;
                }

                String dtGuid = idAttribute.getNodeValue();

                Node definitionNode = (Node) Context.xPath.compile(
                        "./xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]")
                        .evaluate(node, XPathConstants.NODE);
                String definition = (definitionNode != null) ? definitionNode.getTextContent() : null;
                if (definition == null) {
                    definitionNode = (Node) Context.xPath.compile("./xsd:annotation/xsd:documentation")
                            .evaluate(node, XPathConstants.NODE);
                    definition = (definitionNode != null) ? definitionNode.getTextContent() : null;
                }
                if (StringUtils.isEmpty(definition)) {
                    definition = null;
                }

                Node baseTypeNode = (Node) Context.xPath.compile(".//*[@base]").evaluate(node, XPathConstants.NODE);
                String baseTypeName = ((baseTypeNode != null) ? baseTypeNode.getAttributes().getNamedItem("base").getNodeValue() : null);

                Module module = moduleRepository.findByModule(Utility.extractModuleName(systemId));
                DataTypeInfoHolder dtiHolder = new DataTypeInfoHolder(typeName, dtGuid, definition, baseTypeName, (Element) node, systemId, module);
                dtiHolderMap.put(typeName, dtiHolder);
            }
        }
    }

    private void populate() throws Exception {
        fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);

        prepareForBCCP(ImportConstants.FIELDS_XSD_FILE_PATH,
                ImportConstants.META_XSD_FILE_PATH,
                ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH,
                ImportConstants.COMPONENTS_XSD_FILE_PATH);

        insertCodeContentTypeDT();
        insertIDContentTypeDT();

        List<File> files = new ArrayList();
        files.addAll(Arrays.asList(
                new File(ImportConstants.FIELDS_XSD_FILE_PATH),
                new File(ImportConstants.META_XSD_FILE_PATH),
                new File(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH),
                new File(ImportConstants.COMPONENTS_XSD_FILE_PATH)));
        for (File directory : Arrays.asList(
                new File(ImportConstants.MODEL_FOLDER_PATH, "BODs"),
                new File(ImportConstants.MODEL_FOLDER_PATH, "Nouns"),
                new File(ImportConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/BODs"),
                new File(ImportConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/Nouns"))) {
            files.addAll(Arrays.asList(directory.listFiles((dir, name) -> name.endsWith(".xsd") && !name.endsWith("IST.xsd"))));
        }

        for (File file : files) {
            insertDTAndBCCP(file);
        }

        insertDTwithoutElement();
    }

    private List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> getCdtSCAPMap(int cdtSCAllowedPrimitiveId) throws Exception {
        return cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriId(cdtSCAllowedPrimitiveId);
    }

    private void insertDTwithoutElement() throws Exception {
        NodeList complexTypesFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:complexType");
        for (int i = 0; i < complexTypesFromFieldsXSD.getLength(); i++) {
            Node typeNode = complexTypesFromFieldsXSD.item(i);

            String type = ((Element) typeNode).getAttribute("name");
            DataTypeInfoHolder dataTypeInfoHolder = dtiHolderMap.get(type);
            if (dataTypeInfoHolder == null) {
                throw new IllegalStateException("Unknown QBDT: " + type);
            }

            DataType dataType = dataTypeRepository.findOneByGuid(dataTypeInfoHolder.getGuid());
            if (dataType == null) {
                // add new QBDT
                dataType = addToDT(dataTypeInfoHolder, type, fields_xsd);
                if (dataType == null) {
                    continue;
                }

                // add DT_SC
                addToDTSC(fields_xsd, type, dataType);
            }
        }
    }

    private void insertDTAndBCCP(File file) throws Exception {
        Context context = new Context(file, moduleRepository);
        XPathHandler xHandler = new XPathHandler(file);
        NodeList elements = xHandler.getNodeList("/xsd:schema/xsd:element");
        for (int i = 0, len = elements.getLength(); i < len; ++i) {
            Element element = (Element) elements.item(i);
            String bccp = element.getAttribute("name");
            if (StringUtils.isEmpty(bccp)) {
                continue;
            }
            String guid = element.getAttribute("id");
            if (StringUtils.isEmpty(guid)) {
                continue;
            }
            XSElementDecl xsElementDecl = context.getXSElementDecl(SRTConstants.OAGI_NS, bccp);
            if (xsElementDecl == null) {
                throw new IllegalStateException("Could not find " + bccp + ", GUID " + guid + " BCCP");
            }
            ElementDecl elementDecl = new ElementDecl(context, xsElementDecl, element);
            TypeDecl typeDecl = elementDecl.getTypeDecl();
            boolean canBccp = typeDecl.isSimpleType() || typeDecl.isComplexType() && typeDecl.hasSimpleContent();
            if (canBccp) {
                String typeName = typeDecl.getName();

                DataTypeInfoHolder dataTypeInfoHolder = dtiHolderMap.get(typeName);
                if (dataTypeInfoHolder == null) {
                    throw new IllegalStateException("Unknown QBDT: " + typeName);
                }

                DataType dataType = dataTypeRepository.findOneByGuid(dataTypeInfoHolder.getGuid());
                if (dataType == null) {
                    /*
                     * @TODO
                     * Section 3.1.1.13 Import BCCPs
                     *
                     * Assuming the target xsd:element is a BCCP,
                     * the xsd:element/@type shall be a BDT that has already been imported.
                     * If it cannot be found the system should throw an error that
                     * indicates something is wrong either in the import logic or its implementation.
                     */
                    // add new QBDT
                    XPathHandler typeXPathHandler = new XPathHandler(typeDecl.getModuleAsFile());
                    dataType = addToDT(dataTypeInfoHolder, typeName, typeXPathHandler);
                    if (dataType == null) {
                        throw new IllegalStateException("Could add DataType for BCCP type '" + typeName + "', GUID " + typeDecl.getId());
                    }

                    // add DT_SC
                    addToDTSC(typeXPathHandler, typeName, dataType);
                }

                Node documentationFromXSD = xHandler.getNode(element, ".//xsd:documentation | .//*[local-name()=\"ccts_Definition\"]");
                String definition = (documentationFromXSD != null) ? documentationFromXSD.getTextContent() : null;

                // add BCCP
                addToBCCP(guid, bccp, dataType, definition, elementDecl);
            }
        }
    }

    private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> getBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO) throws Exception {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = bdtScPriRestriRepository.findByBdtScId(dtscVO.getBasedDtScId());
        if (bdtscs.isEmpty()) {
            if (dtscVO.getBasedDtScId() == 0) {
                return Collections.emptyList();
            }
            DataTypeSupplementaryComponent vo = dtScRepository.findOne(dtscVO.getBasedDtScId());
            bdtscs = getBDTSCPrimitiveRestriction(vo);
        }
        return bdtscs;
    }

    private void insertBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO,
                                                 int mode, String name, String type) throws Exception {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriListForSaving =
                new ArrayList();

        // if (SC = inherit from the base BDT)
        if (mode == 1) {
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
            for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction parent : bdtscs) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                        new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtScPriRestri.setBdtScId(dtscVO.getDtScId());
                bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(parent.getCdtScAwdPriXpsTypeMapId());
                bdtScPriRestri.setCodeListId(parent.getCodeListId());
                bdtScPriRestri.setDefault(parent.isDefault());
                bdtScPriRestri.setAgencyIdListId(parent.getAgencyIdListId());
                bdtScPriRestriListForSaving.add(bdtScPriRestri);
            }

        }
        else { // else if (new SC)
            populateDefaultAndUnqualifiedBDT.populateBDTSCPrimitiveRestrictionWithAttribute(dtscVO, type);
        }

        if (!bdtScPriRestriListForSaving.isEmpty()) {
            bdtScPriRestriRepository.save(bdtScPriRestriListForSaving);
        }
    }

    private DataType addToDT(DataTypeInfoHolder dataTypeInfoHolder, String type, XPathHandler xHandler) throws Exception {
        String base = dataTypeInfoHolder.getBaseTypeName();
        if (base == null) { // if it isn't QBDT
            return null;
        }

        DataType dataType = new DataType();
        String guid = dataTypeInfoHolder.getGuid();
        dataType.setGuid(guid);
        dataType.setType(1);
        dataType.setVersionNum("1.0");

        DataType baseDataType;

        if (xHandler != null) {
            Node simpleContentNode = xHandler.getNode("//xsd:complexType[@name='" + type + "']/xsd:simpleContent");
            if (simpleContentNode == null) {
                return null;
            }
        }

        String baseDen = Utility.typeToDen(base);
        baseDataType = getDataTypeWithDen(baseDen);

        if (baseDataType == null) {
            DataTypeInfoHolder baseDataTypeInfoHolder = dtiHolderMap.get(base);
            if (baseDataTypeInfoHolder == null) {
                throw new IllegalStateException("Unknown QBDT: " + base);
            }

            // QBDT is based on Other QBDT
            baseDataType = getDataTypeWithGUID(baseDataTypeInfoHolder.getGuid());
            if (baseDataType == null) {
                baseDataType = addToDT(baseDataTypeInfoHolder, base, xHandler);
                if (baseDataType == null) {
                    return null;
                }
                addToDTSC(xHandler, base, baseDataType);
            }
        }

        dataType.setBasedDtId(baseDataType.getDtId());
        dataType.setDataTypeTerm(baseDataType.getDataTypeTerm());

        String qualifier = Utility.qualifier(type, baseDataType.getDen(), baseDataType.getDataTypeTerm());
        if (StringUtils.isEmpty(qualifier)) {
            throw new IllegalStateException("!!Null Qualifier Detected During Import QBDT " + type + " based on Den:" + baseDataType.getDen());
        }

        dataType.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, baseDataType.getDen());
        dataType.setDen(den);
        dataType.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");

        String definition = dataTypeInfoHolder.getDefinition();
        dataType.setDefinition(definition);
        dataType.setState(Published);
        dataType.setCreatedBy(importUtil.getUserId());
        dataType.setLastUpdatedBy(importUtil.getUserId());
        dataType.setOwnerUserId(importUtil.getUserId());
        dataType.setRevisionNum(0);
        dataType.setRevisionTrackingNum(0);
        dataType.setDeprecated(false);
        dataType.setReleaseId(importUtil.getReleaseId());
        Module module = dataTypeInfoHolder.getModule();
        dataType.setModule(module);
        dataTypeRepository.saveAndFlush(dataType);

        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(dataType, base);

        return dataType;
    }

    private void insertBDTPrimitiveRestriction(DataType dataType, String base) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(dataType.getBasedDtId());
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriListForSaving = new ArrayList();

        String dataTypeTerm = dataType.getDataTypeTerm();

        //we need 3 cases : CodeContentQBDTs, IDContentQBDT, and other QBDTs
        if (base.endsWith("CodeContentType") && base.startsWith("oacl")) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(dataType.getDtId());
            long codeListId = getCodeListId(base.substring(0, base.indexOf("CodeContentType")));
            bdtPriRestri.setCodeListId(codeListId);
            bdtPriRestri.setDefault(false);
            bdtPriRestriListForSaving.add(bdtPriRestri); //for CodeList

            for (BusinessDataTypePrimitiveRestriction baseBDTPriRestri : al) {
                BusinessDataTypePrimitiveRestriction inheritedBdtPriRestri = new BusinessDataTypePrimitiveRestriction();
                inheritedBdtPriRestri.setBdtId(dataType.getDtId());
                inheritedBdtPriRestri.setCdtAwdPriXpsTypeMapId(baseBDTPriRestri.getCdtAwdPriXpsTypeMapId());
                inheritedBdtPriRestri.setDefault(baseBDTPriRestri.isDefault());
                bdtPriRestriListForSaving.add(inheritedBdtPriRestri);
            }//For inherited
        } else if (dataTypeTerm.equalsIgnoreCase("Identifier") && base.endsWith("IDContentType")) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(dataType.getDtId());
            bdtPriRestri.setAgencyIdListId(getAgencyListID());
            bdtPriRestri.setDefault(false);
            bdtPriRestriListForSaving.add(bdtPriRestri); //For AgencyIdList

            for (BusinessDataTypePrimitiveRestriction baseBDTPriRestri : al) {
                BusinessDataTypePrimitiveRestriction inheritedBdtPriRestri = new BusinessDataTypePrimitiveRestriction();
                inheritedBdtPriRestri.setBdtId(dataType.getDtId());
                inheritedBdtPriRestri.setCdtAwdPriXpsTypeMapId(baseBDTPriRestri.getCdtAwdPriXpsTypeMapId());
                inheritedBdtPriRestri.setDefault(baseBDTPriRestri.isDefault());
                bdtPriRestriListForSaving.add(inheritedBdtPriRestri);
            }//For inherited
        } else {
            for (BusinessDataTypePrimitiveRestriction baseBDTPriRestri : al) {
                BusinessDataTypePrimitiveRestriction inheritedBdtPriRestri = new BusinessDataTypePrimitiveRestriction();
                inheritedBdtPriRestri.setBdtId(dataType.getDtId());
                inheritedBdtPriRestri.setCdtAwdPriXpsTypeMapId(baseBDTPriRestri.getCdtAwdPriXpsTypeMapId());
                inheritedBdtPriRestri.setDefault(baseBDTPriRestri.isDefault());
                inheritedBdtPriRestri.setCodeListId(baseBDTPriRestri.getCodeListId());
                bdtPriRestriListForSaving.add(inheritedBdtPriRestri);
            }
        }

        if (!bdtPriRestriListForSaving.isEmpty()) {
            bdtPriRestriRepository.save(bdtPriRestriListForSaving);
        }
    }

    private void addToBCCP(String guid, String name, DataType dataType, String definition, ElementDecl elementDecl) throws Exception {
        if (bccpRepository.existsByGuid(guid)) {
            return;
        }

        BasicCoreComponentProperty bccp = new BasicCoreComponentProperty();
        bccp.setGuid(guid);

        String propertyTerm = Utility.spaceSeparator(name);
        bccp.setPropertyTerm(propertyTerm);
        bccp.setRepresentationTerm(dataType.getDataTypeTerm());
        bccp.setBdtId(dataType.getDtId());
        bccp.setDen(Utility.firstToUpperCase(propertyTerm) + ". " + dataType.getDataTypeTerm());
        bccp.setDefinition(definition);
        bccp.setState(Published);
        bccp.setCreatedBy(importUtil.getUserId());
        bccp.setLastUpdatedBy(importUtil.getUserId());
        bccp.setOwnerUserId(importUtil.getUserId());
        bccp.setDeprecated(false);
        bccp.setReleaseId(importUtil.getReleaseId());
        Module module = elementDecl.getModule();
        bccp.setModule(module);
        bccp.setNamespaceId(importUtil.getNamespaceId());
        bccp.setNillable(elementDecl.isNillable());
        bccp.setDefaultValue(elementDecl.getDefaultValue());
        bccpRepository.saveAndFlush(bccp);
    }

    private void addToDTSC(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        long ownerDtId = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());

        // new SC
        NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        if (attributeList != null && attributeList.getLength() > 0) {
            String dt_sc_guid = "";
            String property_term = "";
            String representation_term = "";

            String definition;
            int min_cardinality = 0;
            int max_cardinality = 1;

            for (int i = 0; i < attributeList.getLength(); i++) {
                boolean isNew = true;
                Node attribute = attributeList.item(i);
                Element attrElement = (Element) attribute;
                dt_sc_guid = attrElement.getAttribute("id");

                if (attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
                    min_cardinality = 0;
                else if (attrElement.getAttribute("use").equalsIgnoreCase("required"))
                    min_cardinality = 1;

                if (attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("required"))
                    max_cardinality = 1;
                else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
                    max_cardinality = 0;

                String attrName = attrElement.getAttribute("name");

                // Property Term
                if (attrName.endsWith("Code"))
                    property_term = attrName.substring(0, attrName.indexOf("Code"));
                else if (attrName.endsWith("ID"))
                    property_term = attrName.substring(0, attrName.indexOf("ID"));
                else if (attrName.endsWith("Value"))
                    property_term = attrName.substring(0, attrName.indexOf("Value"));
                else
                    property_term = Utility.spaceSeparator(attrName);

                if (property_term.trim().length() == 0)
                    property_term = attrName;

                property_term = Utility.firstToUpperCase(property_term);

                // Representation Term
                if (attrName.endsWith("Code") || attrName.endsWith("code")) {
                    representation_term = "Code";
                } else if (attrName.endsWith("Number")) {
                    representation_term = "Number";
                } else if (attrName.endsWith("ID")) {
                    representation_term = "Identifier";
                } else if (attrName.endsWith("DateTime")) {
                    representation_term = "Date Time";
                } else if (attrName.endsWith("Value")) {
                    representation_term = "Value";
                } else if (attrName.endsWith("Name") || attrName.endsWith("name")) {
                    representation_term = "Name";
                } else {
                    String attrType = attrElement.getAttribute("type");
                    if (attrType.equals("xsd:string") || attrType.equals("xsd:normalizedString") || attrType.equals("xsd:token"))
                        representation_term = "Text";
                    else if (attrType.equals("xbt_BooleanTrueFalseType"))
                        representation_term = "Indicator";
                    else if (attrName.equals("preferred")){
                        representation_term = "Indicator";
                    }
                }

                Node documentationNode = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation");
                if (documentationNode != null) {
                    Node documentationFromCCTS = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
                    if (documentationFromCCTS != null)
                        definition = documentationFromCCTS.getTextContent();
                    else
                        definition = documentationNode.getTextContent();
                } else {
                    definition = null;
                }

                DataTypeSupplementaryComponent dtSc = new DataTypeSupplementaryComponent();
                dtSc.setGuid(dt_sc_guid);
                dtSc.setPropertyTerm(Utility.spaceSeparator(property_term));
                dtSc.setRepresentationTerm(representation_term);
                dtSc.setDefinition(definition);
                dtSc.setOwnerDtId(ownerDtId);

                dtSc.setCardinalityMin(min_cardinality);
                dtSc.setCardinalityMax(max_cardinality);

//				Both based dtsc and target dtsc have listAgencyID
//				target dtsc inherits all attrs from the base
//				since the attr name is the same, it just update the guid
//				in this case, the target dtsc is new? or not?

                logger.trace(attrName + " " + representation_term);

                for(int j=0; j<dtsc_vos.size(); j++){
                    if(dtSc.getPropertyTerm().equals(dtsc_vos.get(j).getPropertyTerm()) &&
                       dtSc.getRepresentationTerm().equals(dtsc_vos.get(j).getRepresentationTerm())){
                        dtSc.setBasedDtScId(dtsc_vos.get(j).getDtScId());
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    dtScRepository.saveAndFlush(dtSc);

                    // populate CDT_SC_Allowed_Primitives
                    String representationTerm = dtSc.getRepresentationTerm();
                    DataType dtVO = getDataTypeWithRepresentationTerm(representationTerm);
                    if (dtVO == null) {
                        throw new IllegalStateException();
                    }

                    List<CoreDataTypeAllowedPrimitive> cdtAwdPriList = getCdtAwdPriList(dtVO.getDtId());
                    for (CoreDataTypeAllowedPrimitive svo : cdtAwdPriList) {
                        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri =
                                new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                        cdtScAwdPri.setCdtScId(dtSc.getDtScId());
                        cdtScAwdPri.setCdtPriId(svo.getCdtPriId());
                        cdtScAwdPri.setDefault(svo.isDefault());
                        cdtScAwdPriRepository.saveAndFlush(cdtScAwdPri);

                        // populate CDT_SC_Allowed_Primitive_Expression_Type_Map
                        long cdtScAwdPriId =
                                cdtScAwdPriRepository
                                        .findOneByCdtScIdAndCdtPriId(cdtScAwdPri.getCdtScId(), cdtScAwdPri.getCdtPriId())
                                        .getCdtScAwdPriId();

                        List<String> xbtList = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtScAwdPri.getCdtPriId()));
                        for (String xbt : xbtList) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                                    new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                            cdtScAwdPriXpsTypeMap.setCdtScAwdPriId(cdtScAwdPriId);
                            long xdtBuiltTypeId = xbtRepository.findOneByBuiltInType(xbt).getXbtId();
                            cdtScAwdPriXpsTypeMap.setXbtId(xdtBuiltTypeId);
                            cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtScAwdPriXpsTypeMap);
                        }
                    }

                    insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid, ownerDtId), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                } else {
                    dtScRepository.saveAndFlush(dtSc);
                    insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid, ownerDtId), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                }
            }
        }

        for (DataTypeSupplementaryComponent baseDtsc : dtsc_vos) {
            DataTypeSupplementaryComponent inheritedDtSc = new DataTypeSupplementaryComponent();
            inheritedDtSc.setGuid(Utility.generateGUID());
            inheritedDtSc.setPropertyTerm(baseDtsc.getPropertyTerm());
            inheritedDtSc.setRepresentationTerm(baseDtsc.getRepresentationTerm());
            inheritedDtSc.setOwnerDtId(ownerDtId);

            DataTypeSupplementaryComponent duplicate = checkDuplicate(inheritedDtSc);

            if(duplicate==null) {

                inheritedDtSc.setCardinalityMin(baseDtsc.getCardinalityMin());
                inheritedDtSc.setCardinalityMax(baseDtsc.getCardinalityMax());
                inheritedDtSc.setBasedDtScId(baseDtsc.getDtScId());

                dtScRepository.saveAndFlush(inheritedDtSc);

                insertBDTSCPrimitiveRestriction(inheritedDtSc, 1, "", "");
            }
        }
    }

    private DataTypeSupplementaryComponent checkDuplicate(DataTypeSupplementaryComponent dtVO) throws Exception {
        return dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
                dtVO.getOwnerDtId(), dtVO.getPropertyTerm(), dtVO.getRepresentationTerm());
    }


    public long getCodeListId(String codeName) throws Exception {
        List<CodeList> al = codeListRepository.findByNameContaining(codeName.trim());
        int minStrLen = Integer.MAX_VALUE;
        long minInd = -1L;
        for (CodeList codelistVO : al) {
            if (minStrLen > codelistVO.getName().length()) {
                minStrLen = codelistVO.getName().length();
                minInd = codelistVO.getCodeListId();
            }
        }
        return minInd;
    }

    public long getAgencyListID() throws Exception {
        return agencyIdListRepository.findOneByName(AGENCY_ID_LIST_NAME).getAgencyIdListId();
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid) throws Exception {
        return dtScRepository.findOneByGuid(guid);
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid, long ownerId) throws Exception {
        return dtScRepository.findOneByGuidAndOwnerDtId(guid, ownerId);
    }

    public long getDtId(String DataTypeTerm) throws Exception {
        DataType dtVO = dataTypeRepository.findOneByDataTypeTermAndType(DataTypeTerm, 0);
        long id = dtVO.getDtId();
        return id;
    }

    public String getRepresentationTerm(String Guid) throws Exception {
        DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuid(Guid);
        String term = dtscVO.getRepresentationTerm();
        return term;
    }

    public String getPrimitiveName(long CdtPriId) throws Exception {
        return cdtPriRepository.findOne(CdtPriId).getName();
    }


    public long getCdtPriId(String name) throws Exception {
        return cdtPriRepository.findOneByName(name).getCdtPriId();
    }

    public List<CoreDataTypeAllowedPrimitive> getCdtAwdPriList(long cdt_id) throws Exception {
        return cdtAwdPriRepository.findByCdtId(cdt_id);
    }

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> getCdtSCAllowedPrimitiveID(long dt_sc_id) throws Exception {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> res = cdtScAwdPriRepository.findByCdtScId(dt_sc_id);
        if (res.isEmpty()) {
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOne(dt_sc_id);
            res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDtScId());
        }
        return res;
    }

    public long getXbtId(String BuiltIntype) throws Exception {
        return xbtRepository.findOneByBuiltInType(BuiltIntype).getXbtId();
    }

    public boolean checkTokenofXBT(long cdt_awd_pri_xps_type_map_id) throws Exception {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aCoreDataTypeAllowedPrimitiveExpressionTypeMap =
                cdtAwdPriXpsTypeMapRepository.findOne(cdt_awd_pri_xps_type_map_id);
        XSDBuiltInType aXSDBuiltInType = xbtRepository.findOne(
                aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getName().equalsIgnoreCase("token"))
            return true;
        else
            return false;
    }

    private DataType getDataTypeWithDen(String den) throws Exception {
        return dataTypeRepository.findOneByTypeAndDen(1, den);
    }

    private DataType getDataTypeWithRepresentationTerm(String representationTerm) throws Exception {
        return dataTypeRepository.findOneByDataTypeTermAndType(representationTerm, 0);
    }

    private DataType getDataTypeWithGUID(String guid) throws Exception {
        return dataTypeRepository.findOneByGuid(guid);
    }

    private void insertCodeContentTypeDT() throws Exception {
        String dataType = "CodeContentType";
        NodeList simpleTypesFromFieldsXSD = fields_xsd.getNodeList("//xsd:simpleType");

        insertContentTypeFromNodeList(dataType, simpleTypesFromFieldsXSD);
    }

    private void insertIDContentTypeDT() throws Exception {
        String dataType = "IDContentType";
        NodeList simpleTypesFromFieldsXSD = fields_xsd.getNodeList("//xsd:simpleType");

        insertContentTypeFromNodeList(dataType, simpleTypesFromFieldsXSD);
    }

    private void insertContentTypeFromNodeList(String dataType, NodeList simpleTypesFromFieldsXSD) throws Exception {
        for (int i = 0; i < simpleTypesFromFieldsXSD.getLength(); i++) {
            Node typeNode = simpleTypesFromFieldsXSD.item(i);
            String type = ((Element) typeNode).getAttribute("name");
            if (!type.endsWith(dataType) || type.equals(dataType)) {
                continue;
            }
            DataTypeInfoHolder dataTypeInfoHolder = dtiHolderMap.get(type);
            if (dataTypeInfoHolder == null) {
                throw new IllegalStateException("Unknown QBDT: " + type);
            }

            DataType dtVO = dataTypeRepository.findOneByGuid(dataTypeInfoHolder.getGuid());
            if (dtVO == null) {
                dtVO = addToDTForContentType(dataTypeInfoHolder, fields_xsd);
                // add DT_SC
                addToDTSCForContentType(fields_xsd, dataTypeInfoHolder.getTypeName(), dtVO);
            }
        }
    }

    private DataType addToDTForContentType(DataTypeInfoHolder dataTypeInfoHolder, XPathHandler xHandler) throws Exception {
        DataType dataType = new DataType();
        String guid = dataTypeInfoHolder.getGuid();
        dataType.setGuid(guid);
        dataType.setType(1);
        dataType.setVersionNum("1.0");

        String base = dataTypeInfoHolder.getBaseTypeName();

        DataType dVO;
        if (base.endsWith("CodeContentType")) {
            dVO = getDataTypeWithDen("Code Content. Type");
        } else {
            dVO = getDataTypeWithDen("Identifier Content. Type");
            base = "IDContentType";
        }

        dataType.setBasedDtId(dVO.getDtId());
        dataType.setDataTypeTerm(dVO.getDataTypeTerm());

        String type = dataTypeInfoHolder.getTypeName();
        String qualifier = Utility.qualifier(type, dVO.getDen(), dVO.getDataTypeTerm());
        if (StringUtils.isEmpty(qualifier)) {
            throw new IllegalStateException("!!Null Qualifier Detected During Import QBDT " + type + " based on Den: " + dVO.getDen());
        }

        dataType.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, dVO.getDen());
        dataType.setDen(den);
        dataType.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");
        String definition = dataTypeInfoHolder.getDefinition();
        dataType.setDefinition(definition);
        dataType.setState(Published);
        dataType.setCreatedBy(importUtil.getUserId());
        dataType.setLastUpdatedBy(importUtil.getUserId());
        dataType.setOwnerUserId(importUtil.getUserId());
        dataType.setRevisionNum(0);
        dataType.setRevisionTrackingNum(0);
        dataType.setDeprecated(false);
        dataType.setReleaseId(importUtil.getReleaseId());
        Module module = dataTypeInfoHolder.getModule();
        dataType.setModule(module);
        dataType = dataTypeRepository.saveAndFlush(dataType);

        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(dataType, base);

        return dataType;
    }

    private void addToDTSCForContentType(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        long owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setGuid(Utility.generateGUID());
            vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
            vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
            vo.setOwnerDtId(owner_dT_iD);
            vo.setDefinition(dtsc_vo.getDefinition());
            vo.setCardinalityMin(dtsc_vo.getCardinalityMin());
            vo.setCardinalityMax(0);
            vo.setBasedDtScId(dtsc_vo.getDtScId());

            dtScRepository.saveAndFlush(vo);

            insertBDTSCPrimitiveRestriction(vo, 1, "", "");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.7 Start");
        printTitle("Populate Qualified BDTs");

        populate();

        logger.info("### 1.7 End");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_7_PopulateQBDTInDT populateQBDTInDT = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            populateQBDTInDT.run(ctx);
        }
    }
}
