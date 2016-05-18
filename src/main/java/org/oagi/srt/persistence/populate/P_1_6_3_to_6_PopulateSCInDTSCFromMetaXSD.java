package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * @author Jaehun Lee
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public void importDTSCFromMeta() throws Exception {
        DataTypeSupplementaryComponentRepository dtscDao = repositoryFactory.dataTypeSupplementaryComponentRepository();
        DataTypeRepository dtDao = repositoryFactory.dataTypeRepository();
        CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtSCAPDao = repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveRepository();
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtSCAPMapDao = repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository();
        XSDBuiltInTypeRepository xbtDao = repositoryFactory.xsdBuiltInTypeRepository();
        BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtSCPRDao = repositoryFactory.businessDataTypeSupplementaryComponentPrimitiveRestrictionRepository();
        CodeListRepository codeListDao = repositoryFactory.codeListRepository();

        DataType dtVO_01 = dtDao.findOneByDen("Text_62S0B4. Type");

        DataType dtVO_012 = dtDao.findOneByDtId(dtVO_01.getBasedDtId());

        DataTypeSupplementaryComponent dtscVO_01 = dtscDao.findByOwnerDtId(dtVO_012.getDtId()).get(0);
        DataTypeSupplementaryComponent textBDT_dtscVO = dtscDao.findByOwnerDtId(dtVO_01.getDtId()).get(0);

        XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        NodeList complexTypes = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
        for (int i = 0; i < complexTypes.getLength(); i++) {
            Element ele = (Element) complexTypes.item(i);
            String eleGuid = ele.getAttribute("id");
            System.out.println("");
            System.out.println("Populating DT SCs from complextype whose name is " + ele.getAttribute("name"));
            // inherit all values from default Text BDT with two exceptions (max cardinality and based DTSC Id)
            DataType dtVO_011 = dtDao.findOneByGuid(eleGuid);

            DataTypeSupplementaryComponent dtscVO_02 = new DataTypeSupplementaryComponent();
            dtscVO_02.setBasedDtScId(textBDT_dtscVO.getDtScId());
            dtscVO_02.setDefinition(dtscVO_01.getDefinition());
            dtscVO_02.setGuid(dtscVO_01.getGuid());
            dtscVO_02.setMaxCardinality(0);
            dtscVO_02.setMinCardinality(dtscVO_01.getMinCardinality());
            dtscVO_02.setOwnerDtId(dtVO_011.getDtId());
            dtscVO_02.setPropertyTerm(dtscVO_01.getPropertyTerm());
            dtscVO_02.setRepresentationTerm(dtscVO_01.getRepresentationTerm());

            dtscDao.save(dtscVO_02);

            DataTypeSupplementaryComponent dtsc = dtscDao.findOneByGuidAndOwnerDtId(dtscVO_01.getGuid(), dtVO_011.getDtId());
            int bdtSCId = dtsc.getDtScId();
            // populate BDT_SC_Primitive_Restriction table for language code
            System.out.println("Inherit BDT SC which is " + textBDT_dtscVO.getPropertyTerm() + textBDT_dtscVO.getRepresentationTerm());
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = bdtSCPRDao.findByBdtScId(textBDT_dtscVO.getDtScId());
            for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction parent : bdtscs) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPRVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtSCPRVO.setBdtScId(bdtSCId);
                bdtSCPRVO.setCdtScAwdPriXpsTypeMapId(parent.getCdtScAwdPriXpsTypeMapId());
                bdtSCPRVO.setCodeListId(parent.getCodeListId());
                bdtSCPRVO.setDefault(parent.isDefault());
                bdtSCPRDao.save(bdtSCPRVO);
                System.out.println("bdt sc id = " + bdtSCPRVO.getBdtScId() + " cdt sc allow pri ex type map id = " +
                        bdtSCPRVO.getCdtScAwdPriXpsTypeMapId() + " code list id = " + bdtSCPRVO.getCodeListId() +
                        " is_default = " + bdtSCPRVO.isDefault() + " max cardinality of dt_sc = " + dtsc.getMaxCardinality() +
                        " min cardinality of dt_sc = " + dtsc.getMinCardinality());
            }

            // populate using attributes
            DataType dtVO_02 = dtDao.findOneByGuid(eleGuid);

            NodeList attributes = ele.getElementsByTagName("xsd:attribute");
            for (int j = 0; j < attributes.getLength(); j++) {
                Element attr = (Element) attributes.item(j);
                String attrName = attr.getAttribute("name");

                DataTypeSupplementaryComponent dtscVO_03 = new DataTypeSupplementaryComponent();
                dtscVO_03.setGuid(attr.getAttribute("id"));

                String use = attr.getAttribute("use");
                int minCardinality = 0;
                int maxCardinality = 1;
                minCardinality = (use == null) ? 0 : (use.equalsIgnoreCase("required")) ? 1 : 0;
                maxCardinality = (use == null) ? 1 : (use.equalsIgnoreCase("prohibited")) ? 0 : 1;
                dtscVO_03.setMaxCardinality(maxCardinality);
                dtscVO_03.setMinCardinality(minCardinality);

                dtscVO_03.setOwnerDtId(dtVO_02.getDtId());
                dtscVO_03.setPropertyTerm(Utility.spaceSeparator(attrName));
                dtscVO_03.setRepresentationTerm((attrName.equalsIgnoreCase("expressionLanguage")) ? "Text" : "Code");
                System.out.println("Populating xsd:attribute [@name = " + attrName + "]");
                dtscDao.save(dtscVO_03);

                // populate CDT_SC for this new dt_sc
                String[] name = {"NormalizedString", "String", "Token"};
                int dt_sc_id = 0;
                for (int k = 0; k < name.length; k++) {
                    DataTypeSupplementaryComponent dtscVO_04 = dtscDao.findOneByGuid(attr.getAttribute("id"));
                    dt_sc_id = dtscVO_04.getDtScId();
                    CoreDataTypeSupplementaryComponentAllowedPrimitive aCoreDataTypeSupplementaryComponentAllowedPrimitive = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                    aCoreDataTypeSupplementaryComponentAllowedPrimitive.setCdtScId(dt_sc_id);

                    int cdtPrimitiveID = getCDTPrimitiveID(name[k]);
                    aCoreDataTypeSupplementaryComponentAllowedPrimitive.setCdtPriId(cdtPrimitiveID);
                    aCoreDataTypeSupplementaryComponentAllowedPrimitive.setDefault((name[k].equalsIgnoreCase("Token")) ? true : false);
                    System.out.println("Populating CDT SC Primitives... cdt sc id = " + aCoreDataTypeSupplementaryComponentAllowedPrimitive.getCdtScId() +
                            " cdt primitive = " + name[k] + " is_default = " + aCoreDataTypeSupplementaryComponentAllowedPrimitive.isDefault());
                    cdtSCAPDao.save(aCoreDataTypeSupplementaryComponentAllowedPrimitive);

                    int cdtSCAllowedPrimitiveId =
                            cdtSCAPDao.findOneByCdtScIdAndCdtPriId(dt_sc_id, cdtPrimitiveID).getCdtScAwdPriId();

                    // populate CDT_SC_Allowed_Primitive_Expression_Type_Map
                    List<String> xsdbs = Types.getCorrespondingXSDBuiltType(name[k]);
                    for (String xbt : xsdbs) {
                        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap mapVO = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                        mapVO.setCdtScAwdPri(cdtSCAllowedPrimitiveId);
                        int xdtBuiltTypeId = xbtDao.findOneByBuiltInType(xbt).getXbtId();
                        mapVO.setXbtId(xdtBuiltTypeId);
                        System.out.println("Populating CDT SC Allowed Primitive Expression Type map .. xdt built in type id = " + xdtBuiltTypeId +
                                " cdt sc id = " + aCoreDataTypeSupplementaryComponentAllowedPrimitive.getCdtScId() + " cdt primitive = " + name[k] +
                                " is_default = " + aCoreDataTypeSupplementaryComponentAllowedPrimitive.isDefault());
                        cdtSCAPMapDao.save(mapVO);

                        int mapId = cdtSCAPMapDao.findOneByCdtScAwdPriAndXbtId(cdtSCAllowedPrimitiveId, xdtBuiltTypeId).getCdtScAwdPriXpsTypeMapId();

                        // populate BDT_SC_Primitive_Restriction table for expressionLanguage and actionCode
                        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPRVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                        bdtSCPRVO.setBdtScId(dt_sc_id);
                        bdtSCPRVO.setCdtScAwdPriXpsTypeMapId(mapId);
                        if (attrName.equalsIgnoreCase("expressionLanguage")) {
                            bdtSCPRVO.setDefault((name[k].equalsIgnoreCase("Token")) ? true : false);
                        } else if (attrName.equalsIgnoreCase("actionCode")) {
                            bdtSCPRVO.setDefault(false);
                        }
                        System.out.println("Populating BDT SC Primitive for " + attrName);
                        bdtSCPRDao.save(bdtSCPRVO);
                    }
                }

                // populate BDT_SC_Primitive_Restriction table for actionCode (add codeList field)
                if (attrName.equalsIgnoreCase("actionCode")) {
                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPRVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bdtSCPRVO.setBdtScId(dt_sc_id);
                    bdtSCPRVO.setDefault(true);
                    bdtSCPRVO.setCodeListId(codeListDao.findOneByName("oacl_ActionCode").getCodeListId());
                    System.out.println("Populating BDT SC Primitive for " + attrName);
                    bdtSCPRDao.save(bdtSCPRVO);
                }
            }
        }
    }

    public int getCDTPrimitiveID(String name) throws Exception {
        CoreDataTypePrimitiveRepository dao = repositoryFactory.coreDataTypePrimitiveRepository();
        return dao.findOneByName(name).getCdtPriId();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.6.3-6 Start");

        importDTSCFromMeta();

        System.out.println("### 1.6.3-6 End");
    }

    public static void main(String args[]) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD populateSCInDTSCFromMetaXSD = ctx.getBean(P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD.class);
            populateSCInDTSCFromMetaXSD.run(ctx);
        }
    }
}
