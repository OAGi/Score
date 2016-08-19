package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
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

    private int NormalizedStringCDTPrimitiveId;
    private int StringCDTPrimitiveId;
    private int TokenCDTPrimitiveId;
    private int NormalizedStringXBTId;
    private int StringXBTId;
    private int TokenXBTId;

    private XPathHandler meta_xsd;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.6. Start");
        NormalizedStringCDTPrimitiveId = cdtPriRepository.findOneByName("NormalizedString").getCdtPriId();
        StringCDTPrimitiveId = cdtPriRepository.findOneByName("String").getCdtPriId();
        TokenCDTPrimitiveId = cdtPriRepository.findOneByName("Token").getCdtPriId();

        NormalizedStringXBTId= xbtRepository.findOneByBuiltInType("xsd:normalizedString").getXbtId();
        StringXBTId= xbtRepository.findOneByBuiltInType("xsd:string").getXbtId();
        TokenXBTId= xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();
        meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);

        importAdditionalBDT();

        logger.info("### 1.6. End");
    }

    public void importAdditionalBDT() throws Exception {
        NodeList result = meta_xsd.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.META_XSD_FILE_PATH));

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
            dataType.setState(3);
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
            dataTypeRepository.save(dataType);

            // BDT_Primitive_Restriction
            bdtPriRestris.addAll(
                    loadBDTPrimitiveRestrictions(dtVO_01.getDtId(), dataType.getDtId())
            );

            populateDTSC(dataType);
        }

        bdtPriRestriRepository.save(bdtPriRestris);
    }

    private List<BusinessDataTypePrimitiveRestriction> loadBDTPrimitiveRestrictions(
            int basedBdtId, int bdtId) throws Exception {
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

            dtScRepository.save(vo);

            DataTypeSupplementaryComponent insertedSC = dtScRepository.findOneByGuid(vo.getGuid());

            populateCDTSCAwdPri(insertedSC);

            populateBDTSCPrimitiveRestriction(insertedSC);

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
        dtScRepository.save(vo);

        DataTypeSupplementaryComponent insertedSC = dtScRepository.findOneByGuid(vo.getGuid());

        populateBDTSCPrimitiveRestriction(insertedSC);

    }

    public void populateCDTSCAwdPri(DataTypeSupplementaryComponent dtsc){
        int cdtSCAPId = -1;

        //For NormalizedString Primitive
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();

        cdtSCAP.setCdtScId(dtsc.getDtScId());
        cdtSCAP.setCdtPriId(NormalizedStringCDTPrimitiveId);
        if(dtsc.getPropertyTerm().equals("Expression Language")){
            cdtSCAP.setDefault(true);
        }
        else {
            cdtSCAP.setDefault(false);
        }
        cdtScAwdPriRepository.save(cdtSCAP);
        cdtSCAPId = cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(dtsc.getDtScId(), NormalizedStringCDTPrimitiveId).getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(NormalizedStringXBTId);
        cdtScAwdPriXpsTypeMapRepository.save(cdtSCAPXTypeMap);

        //For String Primitive
        cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtSCAP.setCdtScId(dtsc.getDtScId());
        cdtSCAP.setCdtPriId(StringCDTPrimitiveId);
        cdtSCAP.setDefault(false);
        cdtScAwdPriRepository.save(cdtSCAP);
        cdtSCAPId = cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(dtsc.getDtScId(), StringCDTPrimitiveId).getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(StringXBTId);
        cdtScAwdPriXpsTypeMapRepository.save(cdtSCAPXTypeMap);

        //For Token Primitive
        cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtSCAP.setCdtScId(dtsc.getDtScId());
        cdtSCAP.setCdtPriId(TokenCDTPrimitiveId);
        if(dtsc.getPropertyTerm().equals("Action")){
            cdtSCAP.setDefault(true);
        }
        else {
            cdtSCAP.setDefault(false);
        }
        cdtScAwdPriRepository.save(cdtSCAP);
        cdtSCAPId = cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(dtsc.getDtScId(), TokenCDTPrimitiveId).getCdtScAwdPriId();
        cdtSCAPXTypeMap = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtSCAPXTypeMap.setCdtScAwdPriId(cdtSCAPId);
        cdtSCAPXTypeMap.setXbtId(TokenXBTId);
        cdtScAwdPriXpsTypeMapRepository.save(cdtSCAPXTypeMap);
    }


    public void populateBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtsc){
        int dtscId = dtsc.getDtScId();
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

        if(dtsc.getPropertyTerm().equals("Language")){

            CodeList cdl = codeListRepository.findOneByName("clm56392A20081107_LanguageCode");
            bdtSCPri.setCodeListId(cdl.getCodeListId());
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(0);
            bdtSCPri.setDefault(false);
            bdtSCPri.setBdtScId(dtscId);
            bdtScPriRestriRepository.save(bdtSCPri);

            inheritLanguageCode(dtsc);


        }
        else if (dtsc.getPropertyTerm().equals("Expression Language")){

            populateBDTSCPrimitiveFromCDTSC(dtsc.getDtScId(), NormalizedStringCDTPrimitiveId);

        }
        else if(dtsc.getPropertyTerm().equals("Action")){


            DataType baseDT = dataTypeRepository.findOne(dtsc.getOwnerDtId());
            bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtsc.getDtScId());
            bdtSCPri.setDefault(false);

            if(baseDT.getDen().equals("Action Expression. Type")){
                CodeList actionCode = codeListRepository.findOneByName("oacl_ActionCode");
                bdtSCPri.setCodeListId(actionCode.getCodeListId());
                bdtScPriRestriRepository.save(bdtSCPri);

            }
            else if (baseDT.getDen().equals("Response Expression. Type")){
                CodeList responseActionCode = codeListRepository.findOneByName("oacl_ResponseActionCode");
                bdtSCPri.setCodeListId(responseActionCode.getCodeListId());
                bdtScPriRestriRepository.save(bdtSCPri);
            }

            populateBDTSCPrimitiveFromCDTSC(dtsc.getDtScId(), TokenCDTPrimitiveId);


        }
        else {
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
            System.out.println("************************************************************ERROR***********************************");
        }
    }

    public void populateBDTSCPrimitiveFromCDTSC(int dtscId, int defaultCDTPriIndex){
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtSCAwdPriList = cdtScAwdPriRepository.findByCdtScId(dtscId);
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

        for(int i=0; i<cdtSCAwdPriList.size(); i++){
            bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtscId);

            if(cdtSCAwdPriList.get(i).getCdtPriId()==defaultCDTPriIndex){
                bdtSCPri.setDefault(true);
            }
            else {
                bdtSCPri.setDefault(false);
            }

            int xbtId = -1;
            if(cdtSCAwdPriList.get(i).getCdtPriId()==NormalizedStringCDTPrimitiveId){
                xbtId = NormalizedStringXBTId;
            }
            else if (cdtSCAwdPriList.get(i).getCdtPriId()==StringCDTPrimitiveId){
                xbtId = StringXBTId;
            }
            else if (cdtSCAwdPriList.get(i).getCdtPriId()==TokenCDTPrimitiveId){
                xbtId = TokenXBTId;
            }

            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtSCAwdPriXpsTypeMap = cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriIdAndXbtId(cdtSCAwdPriList.get(i).getCdtScAwdPriId(), xbtId);
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(cdtSCAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId());
            bdtScPriRestriRepository.save(bdtSCPri);
        }

    }

    public void inheritLanguageCode(DataTypeSupplementaryComponent dtsc){

        DataType baseBDT = dataTypeRepository.findOne(dtsc.getOwnerDtId());
        DataType baseDefaultTextBDT = dataTypeRepository.findOne(baseBDT.getBasedDtId());

        DataTypeSupplementaryComponent languageCodeSC = dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(baseDefaultTextBDT.getDtId(), "Language", "Code");

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> languageBDTSCPriList = bdtScPriRestriRepository.findByBdtScId(languageCodeSC.getDtScId());

        for(int i=0; i<languageBDTSCPriList.size(); i++){
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction fromBdtSCPri = languageBDTSCPriList.get(i);
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

            bdtSCPri.setBdtScId(dtsc.getDtScId());
            bdtSCPri.setDefault(fromBdtSCPri.isDefault());
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(fromBdtSCPri.getCdtScAwdPriXpsTypeMapId());
            bdtScPriRestriRepository.save(bdtSCPri);
        }


    }

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_6_PopulateDTFromMeta populateDTFromMetaXSD = ctx.getBean(P_1_6_PopulateDTFromMeta.class);
            populateDTFromMetaXSD.run(ctx);
        }
    }
}
