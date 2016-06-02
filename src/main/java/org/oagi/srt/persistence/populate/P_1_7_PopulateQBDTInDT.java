package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.XSSchema;
import com.sun.xml.internal.xsom.XSType;
import com.sun.xml.internal.xsom.parser.SchemaDocument;
import com.sun.xml.internal.xsom.parser.XSOMParser;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_7_PopulateQBDTInDT {

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

    private XPathHandler fields_xsd;
    private XPathHandler meta_xsd;
    private XPathHandler businessDataType_xsd;
    private XPathHandler component_xsd;

    private class DataTypeInfoHolder {
        private String typeName;
        private String guid;
        private String definition;
        private String baseTypeName;
        private Element typeElement;
        private String uri;

        public DataTypeInfoHolder(String typeName, String guid, String definition, String baseTypeName, Element typeElement, String uri) {
            this.typeName = typeName;
            this.guid = guid;
            this.definition = definition;
            this.baseTypeName = baseTypeName;
            this.typeElement = typeElement;
            this.uri = uri;
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

        @Override
        public String toString() {
            return "DataTypeInfoHolder{" +
                    "typeName='" + typeName + '\'' +
                    ", guid='" + guid + '\'' +
                    ", definition='" + definition + '\'' +
                    ", baseTypeName='" + baseTypeName + '\'' +
                    ", typeElement=" + typeElement +
                    ", uri='" + uri + '\'' +
                    '}';
        }
    }

    private int userId;
    private Map<String, DataTypeInfoHolder> dtiHolderMap;

    @PostConstruct
    public void init() throws Exception {
        fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        component_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);

        userId = userRepository.findAppUserIdByLoginId("oagis");
        prepare(SRTConstants.FIELDS_XSD_FILE_PATH,
                SRTConstants.META_XSD_FILE_PATH,
                SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH,
                SRTConstants.COMPONENTS_XSD_FILE_PATH);
    }

    private void prepare(String... systemIds) throws Exception {
        dtiHolderMap = new HashMap();

        XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
        for (String systemId : systemIds) {
            xsomParser.parse(systemId);
        }

        Map<String, Document> documentMap = new HashMap();

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OAGiNamespaceContext());

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

                Document xmlDocument;
                if (!documentMap.containsKey(systemId)) {
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    builderFactory.setNamespaceAware(true);
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    try (InputStream inputStream = new URI(systemId).toURL().openStream()) {
                        xmlDocument = builder.parse(inputStream);
                    }
                    documentMap.put(systemId, xmlDocument);
                } else {
                    xmlDocument = documentMap.get(systemId);
                }

                XPathExpression xPathExpression = xPath.compile("//*[@name='" + typeName + "']");
                Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
                if (node == null) {
                    return;
                }
                Node idAttribute = node.getAttributes().getNamedItem("id");
                if (idAttribute == null) {
                    return;
                }

                String dtGuid = idAttribute.getNodeValue();

                Node definitionNode = (Node) xPath.compile(
                        "./xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]")
                        .evaluate(node, XPathConstants.NODE);
                String definition = (definitionNode != null) ? definitionNode.getTextContent() : null;
                if (definition == null) {
                    definitionNode = (Node) xPath.compile("./xsd:annotation/xsd:documentation")
                            .evaluate(node, XPathConstants.NODE);
                    definition = (definitionNode != null) ? definitionNode.getTextContent() : null;
                }
                if (StringUtils.isEmpty(definition)) {
                    definition = null;
                }

                Node baseTypeNode = (Node) xPath.compile(".//*[@base]").evaluate(node, XPathConstants.NODE);
                String baseTypeName = ((baseTypeNode != null) ? baseTypeNode.getAttributes().getNamedItem("base").getNodeValue() : null);
                DataTypeInfoHolder dtiHolder = new DataTypeInfoHolder(typeName, dtGuid, definition, baseTypeName, (Element) node, systemId);
                dtiHolderMap.put(typeName, dtiHolder);
            }
        }
    }

    private void populate() throws Exception {
        NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
        NodeList elementsFromMetaXSD = meta_xsd.getNodeList("/xsd:schema/xsd:element");
        NodeList elementsFromComponentsXSD = component_xsd.getNodeList("/xsd:schema/xsd:element");

        insertCodeContentTypeDT();
        insertIDContentTypeDT();

        insertDTAndBCCP(elementsFromFieldsXSD, fields_xsd, 0);
        insertDTAndBCCP(elementsFromMetaXSD, meta_xsd, 1); // found that no QBDT from Meta.xsd, maybe because already imported in additional BDT
        insertDTAndBCCP(elementsFromComponentsXSD, component_xsd, 2);

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

    private void insertDTAndBCCP(NodeList elementsFromXSD, XPathHandler xHandler, int xsdType) throws Exception {
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
            addToBCCP(guid, bccp, dataType, definition);
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

    private void insertBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO, int mode, String name, String type) throws Exception {
        // if (SC = inherit from the base BDT)
        if (mode == 1) {
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
            for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction parent : bdtscs) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPRVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtSCPRVO.setBdtScId(dtscVO.getDtScId());
                bdtSCPRVO.setCdtScAwdPriXpsTypeMapId(parent.getCdtScAwdPriXpsTypeMapId());
                bdtSCPRVO.setCodeListId(parent.getCodeListId());
                bdtSCPRVO.setDefault(parent.isDefault());
                bdtSCPRVO.setAgencyIdListId(parent.getAgencyIdListId());
                bdtScPriRestriRepository.save(bdtSCPRVO);
            }

        } else { // else if (new SC)
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtscprimitiverestionvo;

            List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = getCdtSCAllowedPrimitiveID(dtscVO.getDtScId());
            for (CoreDataTypeSupplementaryComponentAllowedPrimitive svo : cdtScAwdPriList) {
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> maps = getCdtSCAPMap(svo.getCdtScAwdPriId());
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap vo : maps) {
                    bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                    bdtscprimitiverestionvo.setCdtScAwdPriXpsTypeMapId(vo.getCdtScAwdPriXpsTypeMapId());

                    if (type.equalsIgnoreCase("NumberType_B98233")) {
                        if (svo.getCdtPriId() == getCdtPriId("Decimal") && vo.getXbtId() == getXbtId("xsd:decimal"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("CodeType_1E7368") || type.equalsIgnoreCase("CodeContentType") || name.equalsIgnoreCase("countryCode")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("StringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("String") && vo.getXbtId() == getXbtId("xsd:string"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("NormalizedStringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("String") && vo.getXbtId() == getXbtId("xsd:string"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (name.equalsIgnoreCase("listID") || name.equalsIgnoreCase("listVersionID") || name.equalsIgnoreCase("unitCodeListVersionID")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("DateTimeType")) {
                        if (svo.getCdtPriId() == getCdtPriId("TimePoint") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("IndicatorType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Boolean") && vo.getXbtId() == getXbtId("xsd:boolean"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("ValueType_E7171E")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (name.equalsIgnoreCase("name")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.contains("CodeContentType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (name.equalsIgnoreCase("listAgencyID")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);
                    }

                    bdtScPriRestriRepository.save(bdtscprimitiverestionvo);

                }
            }

            if (type.contains("CodeContentType")) {
                // add code_list id for this case
                bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                bdtscprimitiverestionvo.setCodeListId(getCodeListId(type.substring(0, type.indexOf("CodeContentType"))));
                bdtscprimitiverestionvo.setDefault(false);
                bdtScPriRestriRepository.save(bdtscprimitiverestionvo);
            }

            if (name.equalsIgnoreCase("listAgencyID")) {
                // add agency_id_list id for this case
                bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                bdtscprimitiverestionvo.setAgencyIdListId(getAgencyListID());
                bdtscprimitiverestionvo.setDefault(false);
                bdtScPriRestriRepository.save(bdtscprimitiverestionvo);
            }
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
        dataTypeRepository.save(dataType);

        DataType res = dataTypeRepository.findOneByGuid(guid);
        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(res, base);

        return res;
    }

    private void insertBDTPrimitiveRestriction(DataType dVO, String base) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(dVO.getBasedDtId());

//		//the previous condition below cannot classify the cases correctly.
//		//we need 3 cases : CodeContentQBDTs, IDContentQBDT, and other QBDTs
//		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
        if (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
            //same as (DataTypeTerm = "Code") & (base != "CodeType")
            BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
            theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
            if (base.endsWith("CodeContentType")) {
                theBDT_Primitive_RestrictionVO.setCodeListId(getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
            } else {//MatchCodeType, ResponseCodeType
                for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
                    if (aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
                        theBDT_Primitive_RestrictionVO.setCodeListId(aBusinessDataTypePrimitiveRestriction.getCodeListId());
                        break;
                    }
                }
            }
            theBDT_Primitive_RestrictionVO.setDefault(false);
            bdtPriRestriRepository.save(theBDT_Primitive_RestrictionVO);
        }

        if (dVO.getDataTypeTerm().equalsIgnoreCase("Identifier") && base.endsWith("IDContentType")) {
            BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
            theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
            theBDT_Primitive_RestrictionVO.setAgencyIdListId(getAgencyListID());
            theBDT_Primitive_RestrictionVO.setDefault(false);
            bdtPriRestriRepository.save(theBDT_Primitive_RestrictionVO);
        }

        if (!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeType")) || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))) {
            //
            //third or condition is not fine because we might apply this code to base = "CodeContentType" not only end-with "CodeContentType"
            for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
                BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
                theBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
                theBDT_Primitive_RestrictionVO.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
                bdtPriRestriRepository.save(theBDT_Primitive_RestrictionVO);

            }
        }
    }

    private void addToBCCP(String guid, String bccp, DataType dtVO, String definition) throws Exception {
        BasicCoreComponentProperty bccpVO = new BasicCoreComponentProperty();
        bccpVO.setGuid(guid);

        String propertyTerm = Utility.spaceSeparator(bccp.replaceAll("ID", "Identifier"));
        bccpVO.setPropertyTerm(propertyTerm);
        bccpVO.setRepresentationTerm(dtVO.getDataTypeTerm());
        bccpVO.setBdtId(dtVO.getDtId());
        bccpVO.setDen(Utility.firstToUpperCase(propertyTerm) + ". " + dtVO.getDataTypeTerm());
        bccpVO.setDefinition(definition);
        bccpVO.setState(3);
        bccpVO.setCreatedBy(userId);
        bccpVO.setLastUpdatedBy(userId);
        bccpVO.setOwnerUserId(userId);
        bccpRepository.save(bccpVO);
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

        if (attributeList == null || attributeList.getLength() == 0) {
            //System.out.println("##### " + "//xsd:"+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        } else {
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
                    if (attrType.equals("StringType") || attrType.equals("NormalizedStringType"))
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

                DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                vo.setGuid(dt_sc_guid);
                vo.setPropertyTerm(Utility.spaceSeparator(property_term));
                vo.setRepresentationTerm(representation_term);
                vo.setDefinition(definition);
                vo.setOwnerDtId(ownerDtId);

                vo.setMinCardinality(min_cardinality);
                vo.setMaxCardinality(max_cardinality);

//				Both based dtsc and target dtsc have listAgencyID
//				target dtsc inherits all attrs from the base
//				since the attr name is the same, it just update the guid
//				in this case, the target dtsc is new? or not?

                DataTypeSupplementaryComponent duplicate = checkDuplicate(vo);
                if (duplicate == null) {
                    dtScRepository.save(vo);

                    // populate CDT_SC_Allowed_Primitives
                    DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuidAndOwnerDtId(vo.getGuid(), vo.getOwnerDtId());
                    String representationTerm = dtscVO.getRepresentationTerm();
                    DataType dtVO = getDataTypeWithRepresentationTerm(representationTerm);

                    CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAllowedVO = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                    cdtSCAllowedVO.setCdtScId(dtscVO.getDtScId());
                    List<CoreDataTypeAllowedPrimitive> cdtallowedprimitivelist = getCDTAllowedPrimitiveIDs(dtVO.getDtId());
                    for (CoreDataTypeAllowedPrimitive svo : cdtallowedprimitivelist) {
                        cdtSCAllowedVO.setCdtPriId(svo.getCdtPriId());
                        cdtSCAllowedVO.setDefault(svo.isDefault());
                        cdtScAwdPriRepository.save(cdtSCAllowedVO);

                        // populate CDT_SC_Allowed_Primitive_Expression_Type_Map
                        int cdtSCAllowedPrimitiveId =
                                cdtScAwdPriRepository
                                        .findOneByCdtScIdAndCdtPriId(cdtSCAllowedVO.getCdtScId(), cdtSCAllowedVO.getCdtPriId())
                                        .getCdtScAwdPriId();

                        List<String> xsdbs = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtSCAllowedVO.getCdtPriId()));
                        for (String xbt : xsdbs) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap mapVO =
                                    new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                            mapVO.setCdtScAwdPri(cdtSCAllowedPrimitiveId);
                            int xdtBuiltTypeId = xbtRepository.findOneByBuiltInType(xbt).getXbtId();
                            mapVO.setXbtId(xdtBuiltTypeId);
                            cdtScAwdPriXpsTypeMapRepository.save(mapVO);
                        }
                    }

                    insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid, ownerDtId), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                } else {
                    vo.setDtScId(duplicate.getDtScId());
                    vo.setBasedDtScId(duplicate.getBasedDtScId());
                    dtScRepository.save(vo);
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

    public List<CoreDataTypeAllowedPrimitive> getCDTAllowedPrimitiveIDs(int cdt_id) throws Exception {
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
            DataTypeInfoHolder dataTypeInfoHolder = dtiHolderMap.get(dataType);
            if (dataTypeInfoHolder == null) {
                throw new IllegalStateException("Unknown QBDT: " + dataType);
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
        DataType dtVO = new DataType();
        String guid = dataTypeInfoHolder.getGuid();
        dtVO.setGuid(guid);
        dtVO.setType(1);
        dtVO.setVersionNum("1.0");

        String base = dataTypeInfoHolder.getBaseTypeName();

        DataType dVO;
        if (base.endsWith("CodeContentType")) {
            dVO = getDataTypeWithDen("Code Content. Type");
        } else { //else if (base.endsWith("IDContentType")){
            dVO = getDataTypeWithDen("Identifier Content. Type");
            base = "IDContentType";
        }

        dtVO.setBasedDtId(dVO.getDtId());
        dtVO.setDataTypeTerm(dVO.getDataTypeTerm());

        String type = dataTypeInfoHolder.getTypeName();
        String qualifier = Utility.qualifier(type, dVO);
        if (qualifier.length() == 0 || qualifier.isEmpty() || qualifier == null) {
            System.out.println("!!Null Qualifier Detected During Import QBDT " + type + " based on Den: " + dVO.getDen());
        }

        dtVO.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, dVO.getDen());
        dtVO.setDen(den);
        dtVO.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");
        String definition = null;
        Node definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation");
        if (definitionNode != null)
            definition = definitionNode.getTextContent();
        else if (xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]") != null) {
            definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
            definition = definitionNode.getTextContent();
        } else
            definition = null;
        dtVO.setDefinition(definition);
        dtVO.setState(3);
        dtVO.setCreatedBy(userId);
        dtVO.setLastUpdatedBy(userId);
        dtVO.setOwnerUserId(userId);
        dtVO.setRevisionNum(0);
        dtVO.setRevisionTrackingNum(0);
        dtVO.setDeprecated(false);
        dataTypeRepository.save(dtVO);

        DataType res = dataTypeRepository.findOneByGuid(guid);
        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(res, base);

        return res;
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
        System.out.println("### 1.7 Start");

        populate();

        System.out.println("### 1.7 End");
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_7_PopulateQBDTInDT populateQBDTInDT = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            populateQBDTInDT.run(ctx);
        }
    }
}
