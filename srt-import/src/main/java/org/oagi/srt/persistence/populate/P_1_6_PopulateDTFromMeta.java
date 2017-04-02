package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

/**
 * Created by tnk11 on 6/27/2016.
 */
@Component
public class P_1_6_PopulateDTFromMeta {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private ImportUtil importUtil;

    private long NormalizedStringCDTPrimitiveId;
    private long StringCDTPrimitiveId;
    private long TokenCDTPrimitiveId;
    private long NormalizedStringXBTId;
    private long StringXBTId;
    private long TokenXBTId;

    private XPathHandler meta_xsd;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.6. Start");
        printTitle("Populate additional BDTs");

        NormalizedStringCDTPrimitiveId = cdtPriRepository.findOneByName("NormalizedString").getCdtPriId();
        StringCDTPrimitiveId = cdtPriRepository.findOneByName("String").getCdtPriId();
        TokenCDTPrimitiveId = cdtPriRepository.findOneByName("Token").getCdtPriId();

        NormalizedStringXBTId = xbtRepository.findOneByBuiltInType("xsd:normalizedString").getXbtId();
        StringXBTId = xbtRepository.findOneByBuiltInType("xsd:string").getXbtId();
        TokenXBTId = xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();
        meta_xsd = new XPathHandler(ImportConstants.META_XSD_FILE_PATH);

        importAdditionalBDT();

        logger.info("### 1.6. End");
    }

    public void importAdditionalBDT() throws Exception {
        NodeList result = meta_xsd.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.META_XSD_FILE_PATH));

        List<BusinessDataTypePrimitiveRestriction> bdtPriRestris = new ArrayList();
        for (int i = 0; i < result.getLength(); i++) {
            Element ele = (Element) result.item(i);
            String name = ele.getAttribute("name");

            DataType dataType = new DataType();
            dataType.setGuid(ele.getAttribute("id"));
            dataType.setType(1);
            dataType.setVersionNum("1.0");

            Node extension = meta_xsd.getNode("//xsd:complexType[@name = '" + name + "']/xsd:simpleContent/xsd:extension");
            String base = Utility.typeToDen(((Element) extension).getAttribute("base"));
            DataType dtVO_01 = dataTypeRepository.findByDen(base).get(0);

            dataType.setBasedDtId(dtVO_01.getDtId());
            dataType.setDataTypeTerm(dtVO_01.getDataTypeTerm());

            dataType.setDen(Utility.typeToDen(name));
            dataType.setContentComponentDen(Utility.typeToContent(name));

            Element definition = (Element) ele.getElementsByTagName("xsd:documentation").item(0);
            if (definition != null)
                dataType.setDefinition(definition.getTextContent());
            else
                dataType.setDefinition(null);

            dataType.setContentComponentDefinition(null);
            dataType.setRevisionDoc(null);
            dataType.setState(CoreComponentState.Published);
            dataType.setCreatedBy(importUtil.getUserId());
            dataType.setLastUpdatedBy(importUtil.getUserId());
            dataType.setOwnerUserId(importUtil.getUserId());
            dataType.setRevisionDoc(null);
            dataType.setRevisionNum(0);
            dataType.setRevisionTrackingNum(0);
            dataType.setDeprecated(false);
            dataType.setReleaseId(importUtil.getReleaseId());
            dataType.setModule(module);
            logger.debug("Populating additional BDTs from meta whose name is " + name);
            dataTypeRepository.saveAndFlush(dataType);

            // BDT_Primitive_Restriction
            bdtPriRestris.addAll(
                    loadBDTPrimitiveRestrictions(dtVO_01.getDtId(), dataType.getDtId())
            );

            populateDTSC(dataType);
        }

        bdtPriRestriRepository.save(bdtPriRestris);
    }

    private List<BusinessDataTypePrimitiveRestriction> loadBDTPrimitiveRestrictions(
            long basedBdtId, long bdtId) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> result = new ArrayList();
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(basedBdtId);

        for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(bdtId);
            bdtPriRestri.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
            bdtPriRestri.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
            logger.debug("Populating BDT Primitive Restriction for bdt id = " + bdtId + " cdt primitive expression type map = " + bdtPriRestri.getCdtAwdPriXpsTypeMapId() + " is_default = " + bdtPriRestri.isDefault());

            result.add(bdtPriRestri);
        }

        return result;
    }

    public void populateDTSC(DataType dt) throws Exception {

        //inheritance
        String denType = Utility.denToTypeName(dt.getDen());
        logger.debug("Popuating SCs for unqualified bdt with type = " + denType);
        Node extensionNode = meta_xsd.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
        if (extensionNode == null) {
            extensionNode = meta_xsd.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
        }

        //adding additional SCs for attributes
        NodeList attributeList = meta_xsd.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");

        int min_cardinality = 0;
        int max_cardinality = 1;

        //For ExpressionLanguage and ActionCode SC
        for (int i = 0; i < attributeList.getLength(); i++) {
            Element attrElement = (Element) attributeList.item(i);
            String attribute_name = attrElement.getAttribute("name");
            String attribute_id = attrElement.getAttribute("id");
            String attribute_type = attrElement.getAttribute("type");

            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setOwnerDtId(dt.getDtId());
            vo.setGuid(attribute_id);
            if (attrElement.getAttribute("use") == null) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                min_cardinality = 1;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                min_cardinality = 0;
                max_cardinality = 0;
            }

            vo.setCardinalityMin(min_cardinality);
            vo.setCardinalityMax(max_cardinality);

            String propertyTerm = "";
            String representationTerm = "";
            String definition = "";

            propertyTerm = Utility.spaceSeparatorBeforeStr(attribute_name, "Code");

            representationTerm = Utility.getRepresentationTerm(attribute_name);

            Node defNode = meta_xsd.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");

            if (defNode != null) {
                definition = defNode.getTextContent();
            }

            vo.setPropertyTerm(propertyTerm);
            vo.setRepresentationTerm(representationTerm);
            vo.setDefinition(definition);
            logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT is new from Attribute!");

            vo = dtScRepository.saveAndFlush(vo);

            populateCDTSCAwdPri(vo, attribute_type);
            populateBDTSCPrimitiveRestriction(vo, attribute_type);
        }

        //For LanguageCode SC
        DataTypeSupplementaryComponent languageCodeSC = dtScRepository.findByOwnerDtId(dt.getBasedDtId()).get(0);
        DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();

        vo.setBasedDtScId(languageCodeSC.getDtScId());
        vo.setDefinition(languageCodeSC.getDefinition());
        vo.setGuid(Utility.generateGUID());
        vo.setCardinalityMax(0);
        vo.setCardinalityMin(0);
        vo.setOwnerDtId(dt.getDtId());
        vo.setPropertyTerm(languageCodeSC.getPropertyTerm());
        vo.setRepresentationTerm(languageCodeSC.getRepresentationTerm());
        vo = dtScRepository.saveAndFlush(vo);

        populateBDTSCPrimitiveRestriction(vo, null);
    }

    public void populateCDTSCAwdPri(DataTypeSupplementaryComponent dtSc, String type) {
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP_normalizedString
                = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtSCAP_normalizedString.setCdtScId(dtSc.getDtScId());
        cdtSCAP_normalizedString.setCdtPriId(NormalizedStringCDTPrimitiveId);

        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP_string
                = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtSCAP_string.setCdtScId(dtSc.getDtScId());
        cdtSCAP_string.setCdtPriId(StringCDTPrimitiveId);

        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP_token
                = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtSCAP_token.setCdtScId(dtSc.getDtScId());
        cdtSCAP_token.setCdtPriId(TokenCDTPrimitiveId);

        if (type.equals("xsd:normalizedString")) {
            cdtSCAP_normalizedString.setDefault(true);
        } else if (type.equals("xsd:string")) {
            cdtSCAP_string.setDefault(true);
        } else if (type.equals("xsd:token") || dtSc.getPropertyTerm().equals("Action")) {
            cdtSCAP_token.setDefault(true);
        } else {
            throw new IllegalArgumentException("Not allowed 'type': " + type + " in " + dtSc);
        }

        long cdtSCAPId;
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtSCAPXTypeMap;

        cdtSCAP_normalizedString = cdtScAwdPriRepository.saveAndFlush(cdtSCAP_normalizedString);
        cdtSCAPId = cdtSCAP_normalizedString.getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(NormalizedStringXBTId);
        cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtSCAPXTypeMap);

        cdtSCAP_string = cdtScAwdPriRepository.saveAndFlush(cdtSCAP_string);
        cdtSCAPId = cdtSCAP_string.getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(StringXBTId);
        cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtSCAPXTypeMap);

        cdtSCAP_token = cdtScAwdPriRepository.saveAndFlush(cdtSCAP_token);
        cdtSCAPId = cdtSCAP_token.getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(TokenXBTId);
        cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtSCAPXTypeMap);
    }


    public void populateBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtSc, String type) {
        long dtScId = dtSc.getDtScId();
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

        if (dtSc.getPropertyTerm().equals("Language")) {

            CodeList cdl = codeListRepository.findOneByName("clm56392A20081107_LanguageCode");
            bdtSCPri.setCodeListId(cdl.getCodeListId());
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(0);
            bdtSCPri.setDefault(false);
            bdtSCPri.setBdtScId(dtScId);
            bdtScPriRestriRepository.saveAndFlush(bdtSCPri);

            inheritLanguageCode(dtSc);

        } else if (dtSc.getPropertyTerm().equals("Expression Language")) {

            if ("xsd:normalizedString".equals(type)) {
                populateBDTSCPrimitiveFromCDTSC(dtSc.getDtScId(), NormalizedStringCDTPrimitiveId);
            } else if ("xsd:string".equals(type)) {
                populateBDTSCPrimitiveFromCDTSC(dtSc.getDtScId(), StringCDTPrimitiveId);
            } else if ("xsd:token".equals(type)) {
                populateBDTSCPrimitiveFromCDTSC(dtSc.getDtScId(), TokenCDTPrimitiveId);
            } else {
                throw new IllegalArgumentException("Not allowed 'type': " + type + " in " + dtSc);
            }

        } else if (dtSc.getPropertyTerm().equals("Action")) {

            DataType baseDT = dataTypeRepository.findOne(dtSc.getOwnerDtId());
            bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtSc.getDtScId());
            bdtSCPri.setDefault(false);

            if (baseDT.getDen().equals("Action Expression. Type")) {
                CodeList actionCode = codeListRepository.findOneByName("oacl_ActionCode");
                bdtSCPri.setCodeListId(actionCode.getCodeListId());
                bdtScPriRestriRepository.saveAndFlush(bdtSCPri);

            } else if (baseDT.getDen().equals("Response Expression. Type")) {
                CodeList responseActionCode = codeListRepository.findOneByName("oacl_ResponseActionCode");
                bdtSCPri.setCodeListId(responseActionCode.getCodeListId());
                bdtScPriRestriRepository.saveAndFlush(bdtSCPri);
            }

            populateBDTSCPrimitiveFromCDTSC(dtSc.getDtScId(), TokenCDTPrimitiveId);


        } else {
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
        }
    }

    public void populateBDTSCPrimitiveFromCDTSC(long dtScId, long defaultCDTPriIndex) {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtScAwdPriList = cdtScAwdPriRepository.findByCdtScId(dtScId);
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestiList = new ArrayList();

        for (int i = 0; i < cdtScAwdPriList.size(); i++) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri =
                    new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtScId);

            CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAwdPri = cdtScAwdPriList.get(i);
            long cdtPriId = cdtSCAwdPri.getCdtPriId();

            if (cdtPriId == defaultCDTPriIndex) {
                bdtSCPri.setDefault(true);
            } else {
                bdtSCPri.setDefault(false);
            }

            long xbtId = -1L;
            if (cdtPriId == NormalizedStringCDTPrimitiveId) {
                xbtId = NormalizedStringXBTId;
            } else if (cdtPriId == StringCDTPrimitiveId) {
                xbtId = StringXBTId;
            } else if (cdtPriId == TokenCDTPrimitiveId) {
                xbtId = TokenXBTId;
            }

            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtSCAwdPriXpsTypeMap = cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriIdAndXbtId(cdtScAwdPriList.get(i).getCdtScAwdPriId(), xbtId);
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(cdtSCAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId());
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);

            bdtScPriRestiList.add(bdtSCPri);
        }

        if (bdtScPriRestiList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
            throw new IllegalStateException("BDT_SC_ID['" + dtScId + "'] has incorrect 'is_default' value in BDT_SC_PRI_RESTRI.");
        }
    }

    public void inheritLanguageCode(DataTypeSupplementaryComponent dtSc) {

        DataType baseBDT = dataTypeRepository.findOne(dtSc.getOwnerDtId());
        DataType baseDefaultTextBDT = dataTypeRepository.findOne(baseBDT.getBasedDtId());

        DataTypeSupplementaryComponent languageCodeSC = dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(baseDefaultTextBDT.getDtId(), "Language", "Code");

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> languageBDTSCPriList =
                bdtScPriRestriRepository.findByBdtScId(languageCodeSC.getDtScId());
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestiList = new ArrayList();

        for (int i = 0, len = languageBDTSCPriList.size(); i < len; i++) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction fromBdtSCPri = languageBDTSCPriList.get(i);
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri =
                    new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

            bdtSCPri.setBdtScId(dtSc.getDtScId());
            bdtSCPri.setDefault(fromBdtSCPri.isDefault());
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(fromBdtSCPri.getCdtScAwdPriXpsTypeMapId());
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);

            bdtScPriRestiList.add(bdtSCPri);
        }

        if (bdtScPriRestiList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
            throw new IllegalStateException("BDT_SC_ID['" + dtSc.getDtScId() + "'] has incorrect 'is_default' value in BDT_SC_PRI_RESTRI.");
        }
    }

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_6_PopulateDTFromMeta populateDTFromMetaXSD = ctx.getBean(P_1_6_PopulateDTFromMeta.class);
            populateDTFromMetaXSD.run(ctx);
        }
    }
}
