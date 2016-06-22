package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSSchema;
import com.sun.xml.internal.xsom.XSType;
import com.sun.xml.internal.xsom.parser.SchemaDocument;
import com.sun.xml.internal.xsom.parser.XSOMParser;
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

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_7_PopulateQBDTInDT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

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
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    private XPathHandler fields_xsd;

    private class DataTypeInfoHolder {
        private String typeName;
        private String guid;
        private String definition;
        private String baseTypeName;
        private Element typeElement;
        private String uri;
        private String module;

        public DataTypeInfoHolder(String typeName, String guid, String definition,
                                  String baseTypeName, Element typeElement, String uri, String module) {
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

        public String getModule() {
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

    private int userId;
    private int releaseId;
    private int namespaceId;

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

                XPathExpression xPathExpression = Context.xPath.compile("//*[@name='" + typeName + "']");
                Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
                if (node == null) {
                    return;
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

                String module = Utility.extractModuleName(systemId);
                DataTypeInfoHolder dtiHolder = new DataTypeInfoHolder(typeName, dtGuid, definition, baseTypeName, (Element) node, systemId, module);
                dtiHolderMap.put(typeName, dtiHolder);
            }
        }
    }

    private void populate() throws Exception {
        fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);

        userId = userRepository.findAppUserIdByLoginId("oagis");
        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");
        namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");

        prepareForBCCP(SRTConstants.FIELDS_XSD_FILE_PATH,
                SRTConstants.META_XSD_FILE_PATH,
                SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH,
                SRTConstants.COMPONENTS_XSD_FILE_PATH);

        insertCodeContentTypeDT();
        insertIDContentTypeDT();

        List<File> files = new ArrayList();
        files.addAll(Arrays.asList(
                new File(SRTConstants.FIELDS_XSD_FILE_PATH),
                new File(SRTConstants.META_XSD_FILE_PATH),
                new File(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH),
                new File(SRTConstants.COMPONENTS_XSD_FILE_PATH)));
        for (File directory : Arrays.asList(
                new File(SRTConstants.MODEL_FOLDER_PATH, "BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Nouns"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/Nouns"))) {
            files.addAll(Arrays.asList(directory.listFiles((dir, name) -> name.endsWith(".xsd") && !name.endsWith("IST.xsd"))));
        }

        for (File file : files) {
            insertDTAndBCCP(file);
        }

        insertDTwithoutElement();
    }

    private List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> getCdtSCAPMap(int cdtSCAllowedPrimitiveId) throws Exception {
        return cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPri(cdtSCAllowedPrimitiveId);
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
        Context context = new Context(file);
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

            XSElementDecl xsElementDecl = context.getElementDecl(SRTConstants.OAGI_NS, bccp);
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

                String module = elementDecl.getModule();
                // add BCCP
                addToBCCP(guid, bccp, dataType, definition, module);
            }
        }
    }

    private void insertDTAndBCCP(NodeList elementsFromXSD, XPathHandler xHandler) throws Exception {
        for (int i = 0; i < elementsFromXSD.getLength(); i++) {//ElementsFromXSD don't have CodeContentType, IDContentType
            Element element = (Element) elementsFromXSD.item(i);
            String bccp = element.getAttribute("name");
            String guid = element.getAttribute("id");
            String type = element.getAttribute("type");

            DataTypeInfoHolder dataTypeInfoHolder = dtiHolderMap.get(type);
            if (dataTypeInfoHolder == null) {
                throw new IllegalStateException("Unknown QBDT: " + type);
            }

            DataType dataType = dataTypeRepository.findOneByGuid(dataTypeInfoHolder.getGuid());
            if (dataType == null) {
                // add new QBDT
                dataType = addToDT(dataTypeInfoHolder, type, xHandler);
                if (dataType == null) {
                    continue;
                }

                // add DT_SC
                addToDTSC(xHandler, type, dataType);
            }

            Node documentationFromXSD = xHandler.getNode(element, ".//xsd:documentation | .//*[local-name()=\"ccts_Definition\"]");
            String definition = (documentationFromXSD != null) ? documentationFromXSD.getTextContent() : null;

            // add BCCP
            addToBCCP(guid, bccp, dataType, definition, null);
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

        } else { // else if (new SC)
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri;

            List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = getCdtSCAllowedPrimitiveID(dtscVO.getDtScId());
            for (CoreDataTypeSupplementaryComponentAllowedPrimitive svo : cdtScAwdPriList) {
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> maps = getCdtSCAPMap(svo.getCdtScAwdPriId());
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap vo : maps) {
                    bdtScPriRestri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bdtScPriRestri.setBdtScId(dtscVO.getDtScId());
                    bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(vo.getCdtScAwdPriXpsTypeMapId());

                    if (type.equalsIgnoreCase("NumberType_B98233")) {
                        if (svo.getCdtPriId() == getCdtPriId("Decimal") && vo.getXbtId() == getXbtId("xsd:decimal"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("CodeType_1E7368") ||
                            type.equalsIgnoreCase("CodeContentType") ||
                            name.equalsIgnoreCase("countryCode")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("StringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("String") && vo.getXbtId() == getXbtId("xsd:string"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("NormalizedStringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (name.equalsIgnoreCase("listID") ||
                            name.equalsIgnoreCase("listVersionID") ||
                            name.equalsIgnoreCase("unitCodeListVersionID")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("DateTimeType")) {
                        if (svo.getCdtPriId() == getCdtPriId("TimePoint") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("IndicatorType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Boolean") && vo.getXbtId() == getXbtId("xsd:boolean"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.equalsIgnoreCase("ValueType_E7171E")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (name.equalsIgnoreCase("name")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (type.contains("CodeContentType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);

                    } else if (name.equalsIgnoreCase("listAgencyID")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtScPriRestri.setDefault(true);
                        else
                            bdtScPriRestri.setDefault(false);
                    }

                    bdtScPriRestriListForSaving.add(bdtScPriRestri);
                }
            }

            if (type.contains("CodeContentType")) {
                // add code_list id for this case
                bdtScPriRestri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtScPriRestri.setBdtScId(dtscVO.getDtScId());
                bdtScPriRestri.setCodeListId(getCodeListId(type.substring(0, type.indexOf("CodeContentType"))));
                bdtScPriRestri.setDefault(false);
                bdtScPriRestriListForSaving.add(bdtScPriRestri);
            }

            if (name.equalsIgnoreCase("listAgencyID")) {
                // add agency_id_list id for this case
                bdtScPriRestri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtScPriRestri.setBdtScId(dtscVO.getDtScId());
                bdtScPriRestri.setAgencyIdListId(getAgencyListID());
                bdtScPriRestri.setDefault(false);
                bdtScPriRestriListForSaving.add(bdtScPriRestri);
            }
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
        if (base.endsWith("CodeContentType")) {
            baseDataType = getDataTypeWithDen("Code. Type");
        } else {
            if (xHandler != null) {
                Node simpleContentNode = xHandler.getNode("//xsd:complexType[@name='" + type + "']/xsd:simpleContent");
                if (simpleContentNode == null) {
                    return null;
                }
            }

            String baseDen = Utility.typeToDen(base);
            baseDataType = getDataTypeWithDen(baseDen);

            // QBDT is based on another QBDT
            if (baseDataType == null) {
                DataTypeInfoHolder baseDataTypeInfoHolder = dtiHolderMap.get(base);
                if (baseDataTypeInfoHolder == null) {
                    throw new IllegalStateException("Unknown QBDT: " + base);
                }

                baseDataType = getDataTypeWithGUID(baseDataTypeInfoHolder.getGuid());
                if (baseDataType == null) {
                    baseDataType = addToDT(baseDataTypeInfoHolder, base, xHandler);
                    if (baseDataType == null) {
                        return null;
                    }
                }
            }
        }

        dataType.setBasedDtId(baseDataType.getDtId());
        dataType.setDataTypeTerm(baseDataType.getDataTypeTerm());

        String qualifier = Utility.qualifier(type, baseDataType);
        if (StringUtils.isEmpty(qualifier)) {
            throw new IllegalStateException("!!Null Qualifier Detected During Import QBDT " + type + " based on Den:" + baseDataType.getDen());
        }

        dataType.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, baseDataType.getDen());
        dataType.setDen(den);
        dataType.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");

        String definition = dataTypeInfoHolder.getDefinition();
        dataType.setDefinition(definition);
        dataType.setState(3);
        dataType.setCreatedBy(userId);
        dataType.setLastUpdatedBy(userId);
        dataType.setOwnerUserId(userId);
        dataType.setRevisionNum(0);
        dataType.setRevisionTrackingNum(0);
        dataType.setDeprecated(false);
        dataType.setReleaseId(releaseId);
        dataType.setModule(dataTypeInfoHolder.getModule());
        dataTypeRepository.saveAndFlush(dataType);

        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(dataType, base);

        return dataType;
    }

    private void insertBDTPrimitiveRestriction(DataType dataType, String base) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(dataType.getBasedDtId());
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriListForSaving = new ArrayList();

        String dataTypeTerm = dataType.getDataTypeTerm();

//		//the previous condition below cannot classify the cases correctly.
//		//we need 3 cases : CodeContentQBDTs, IDContentQBDT, and other QBDTs
        if (dataTypeTerm.equalsIgnoreCase("Code") &&
                !(dataTypeTerm.equalsIgnoreCase("Code") &&
                        base.equalsIgnoreCase("CodeType"))) {
            //same as (DataTypeTerm = "Code") & (base != "CodeType")
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(dataType.getDtId());
            if (base.endsWith("CodeContentType")) {
                bdtPriRestri.setCodeListId(getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
            } else {//MatchCodeType, ResponseCodeType
                for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
                    if (aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
                        bdtPriRestri.setCodeListId(aBusinessDataTypePrimitiveRestriction.getCodeListId());
                        break;
                    }
                }
            }
            bdtPriRestri.setDefault(false);
            bdtPriRestriListForSaving.add(bdtPriRestri);
        }

        if (dataTypeTerm.equalsIgnoreCase("Identifier") && base.endsWith("IDContentType")) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(dataType.getDtId());
            bdtPriRestri.setAgencyIdListId(getAgencyListID());
            bdtPriRestri.setDefault(false);
            bdtPriRestriListForSaving.add(bdtPriRestri);
        }

        if (!dataTypeTerm.equalsIgnoreCase("Code") ||
                (dataTypeTerm.equalsIgnoreCase("Code") && base.endsWith("CodeType")) ||
                (dataTypeTerm.equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))) {
            //
            //third or condition is not fine because we might apply this code to base = "CodeContentType" not only end-with "CodeContentType"
            for (BusinessDataTypePrimitiveRestriction bdtPriRestri : al) {
                BusinessDataTypePrimitiveRestriction inheritedBdtPriRestri = new BusinessDataTypePrimitiveRestriction();
                inheritedBdtPriRestri.setBdtId(dataType.getDtId());
                inheritedBdtPriRestri.setCdtAwdPriXpsTypeMapId(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
                inheritedBdtPriRestri.setDefault(bdtPriRestri.isDefault());
                bdtPriRestriListForSaving.add(inheritedBdtPriRestri);
            }
        }

        if (!bdtPriRestriListForSaving.isEmpty()) {
            bdtPriRestriRepository.save(bdtPriRestriListForSaving);
        }
    }

    private void addToBCCP(String guid, String name, DataType dataType, String definition, String module) throws Exception {
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
        bccp.setState(3);
        bccp.setCreatedBy(userId);
        bccp.setLastUpdatedBy(userId);
        bccp.setOwnerUserId(userId);
        bccp.setDeprecated(false);
        bccp.setReleaseId(releaseId);
        bccp.setModule(module);
        bccp.setNamespaceId(namespaceId);
        bccpRepository.save(bccp);
    }

    private void addToDTSC(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        int ownerDtId = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            DataTypeSupplementaryComponent inheritedDtSc = new DataTypeSupplementaryComponent();
            inheritedDtSc.setGuid(Utility.generateGUID());
            inheritedDtSc.setPropertyTerm(dtsc_vo.getPropertyTerm());
            inheritedDtSc.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
            inheritedDtSc.setOwnerDtId(ownerDtId);

            inheritedDtSc.setMinCardinality(dtsc_vo.getMinCardinality());
            inheritedDtSc.setMaxCardinality(dtsc_vo.getMaxCardinality());
            inheritedDtSc.setBasedDtScId(dtsc_vo.getDtScId());

            dtScRepository.saveAndFlush(inheritedDtSc);

            insertBDTSCPrimitiveRestriction(inheritedDtSc, 1, "", "");
        }

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
                    if (attrType.equals("StringType") || attrType.equals("NormalizedStringType") || attrType.equals("TokenType"))
                        representation_term = "Text";
                    else if (attrType.equals("IndicatorType"))
                        representation_term = "Indicator";
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

                dtSc.setMinCardinality(min_cardinality);
                dtSc.setMaxCardinality(max_cardinality);

//				Both based dtsc and target dtsc have listAgencyID
//				target dtsc inherits all attrs from the base
//				since the attr name is the same, it just update the guid
//				in this case, the target dtsc is new? or not?

                DataTypeSupplementaryComponent duplicate = checkDuplicate(dtSc);
                if (duplicate == null) {
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
                        cdtScAwdPriRepository.save(cdtScAwdPri);

                        // populate CDT_SC_Allowed_Primitive_Expression_Type_Map
                        int cdtScAwdPriId =
                                cdtScAwdPriRepository
                                        .findOneByCdtScIdAndCdtPriId(cdtScAwdPri.getCdtScId(), cdtScAwdPri.getCdtPriId())
                                        .getCdtScAwdPriId();

                        List<String> xbtList = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtScAwdPri.getCdtPriId()));
                        for (String xbt : xbtList) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                                    new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                            cdtScAwdPriXpsTypeMap.setCdtScAwdPri(cdtScAwdPriId);
                            int xdtBuiltTypeId = xbtRepository.findOneByBuiltInType(xbt).getXbtId();
                            cdtScAwdPriXpsTypeMap.setXbtId(xdtBuiltTypeId);
                            cdtScAwdPriXpsTypeMapRepository.save(cdtScAwdPriXpsTypeMap);
                        }
                    }

                    insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid, ownerDtId), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                } else {
                    dtSc.setDtScId(duplicate.getDtScId());
                    dtSc.setBasedDtScId(duplicate.getBasedDtScId());
                    dtScRepository.save(dtSc);
                }
            }
        }
    }

    private DataTypeSupplementaryComponent checkDuplicate(DataTypeSupplementaryComponent dtVO) throws Exception {
        return dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
                dtVO.getOwnerDtId(), dtVO.getPropertyTerm(), dtVO.getRepresentationTerm());
    }


    public int getCodeListId(String codeName) throws Exception {
        List<CodeList> al = codeListRepository.findByNameContaining(codeName.trim());
        int minStrLen = Integer.MAX_VALUE;
        int minInd = -1;
        for (CodeList codelistVO : al) {
            if (minStrLen > codelistVO.getName().length()) {
                minStrLen = codelistVO.getName().length();
                minInd = codelistVO.getCodeListId();
            }
        }
        return minInd;
    }

    public int getAgencyListID() throws Exception {
        return agencyIdListRepository.findOneByName("Agency Identification").getAgencyIdListId();
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid) throws Exception {
        return dtScRepository.findOneByGuid(guid);
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid, int ownerId) throws Exception {
        return dtScRepository.findOneByGuidAndOwnerDtId(guid, ownerId);
    }

    public int getDtId(String DataTypeTerm) throws Exception {
        DataType dtVO = dataTypeRepository.findOneByDataTypeTermAndType(DataTypeTerm, 0);
        int id = dtVO.getDtId();
        return id;
    }

    public String getRepresentationTerm(String Guid) throws Exception {
        DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuid(Guid);
        String term = dtscVO.getRepresentationTerm();
        return term;
    }

    public String getPrimitiveName(int CdtPriId) throws Exception {
        return cdtPriRepository.findOne(CdtPriId).getName();
    }


    public int getCdtPriId(String name) throws Exception {
        return cdtPriRepository.findOneByName(name).getCdtPriId();
    }

    public List<CoreDataTypeAllowedPrimitive> getCdtAwdPriList(int cdt_id) throws Exception {
        return cdtAwdPriRepository.findByCdtId(cdt_id);
    }

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> getCdtSCAllowedPrimitiveID(int dt_sc_id) throws Exception {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> res = cdtScAwdPriRepository.findByCdtScId(dt_sc_id);
        if (res.isEmpty()) {
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOne(dt_sc_id);
            res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDtScId());
        }
        return res;
    }

    public int getXbtId(String BuiltIntype) throws Exception {
        return xbtRepository.findOneByBuiltInType(BuiltIntype).getXbtId();
    }

    public boolean checkTokenofXBT(int cdt_awd_pri_xps_type_map_id) throws Exception {
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
        String qualifier = Utility.qualifier(type, dVO);
        if (StringUtils.isEmpty(qualifier)) {
            throw new IllegalStateException("!!Null Qualifier Detected During Import QBDT " + type + " based on Den: " + dVO.getDen());
        }

        dataType.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, dVO.getDen());
        dataType.setDen(den);
        dataType.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");
        String definition = dataTypeInfoHolder.getDefinition();
        dataType.setDefinition(definition);
        dataType.setState(3);
        dataType.setCreatedBy(userId);
        dataType.setLastUpdatedBy(userId);
        dataType.setOwnerUserId(userId);
        dataType.setRevisionNum(0);
        dataType.setRevisionTrackingNum(0);
        dataType.setDeprecated(false);
        dataType.setReleaseId(releaseId);
        dataType.setModule(dataTypeInfoHolder.getModule());
        dataType = dataTypeRepository.saveAndFlush(dataType);

        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(dataType, base);

        return dataType;
    }

    private void addToDTSCForContentType(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        int owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setGuid(Utility.generateGUID());
            vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
            vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
            vo.setOwnerDtId(owner_dT_iD);
            vo.setDefinition(dtsc_vo.getDefinition());
            vo.setMinCardinality(dtsc_vo.getMinCardinality());
            vo.setMaxCardinality(0);
            vo.setBasedDtScId(dtsc_vo.getDtScId());

            dtScRepository.saveAndFlush(vo);

            insertBDTSCPrimitiveRestriction(vo, 1, "", "");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.debug("### 1.7 Start");

        populate();

        logger.debug("### 1.7 End");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_7_PopulateQBDTInDT populateQBDTInDT = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            populateQBDTInDT.run(ctx);
        }
    }
}
