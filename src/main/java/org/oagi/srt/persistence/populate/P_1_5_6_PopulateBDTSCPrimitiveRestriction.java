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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_5_6_PopulateBDTSCPrimitiveRestriction {

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
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    public int getAgencyListID() throws Exception {
        AgencyIdList agencyIdList = agencyIdListRepository.findOneByName("Agency Identification");
        return agencyIdList.getAgencyIdListId();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        XPathHandler xh = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xh2 = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        System.out.println("### 1.5.6 Start");
        populateBDTSCPrimitiveRestriction(xh, xh2, true);
        System.out.println("### 1.5.6 End");
    }

    public void populateBDTSCPrimitiveRestriction(XPathHandler xh, XPathHandler xh2, boolean is_fields_xsd) throws Exception {
        List<DataTypeSupplementaryComponent> al = dtScRepository.findAll();
        List<DataTypeSupplementaryComponent> al_meta = new ArrayList();
        if (is_fields_xsd) {

        } else {
            for (DataTypeSupplementaryComponent aDataTypeSupplementaryComponent : al) {
                DataType dtVO = dataTypeRepository.findOne(aDataTypeSupplementaryComponent.getOwnerDtId());

                String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"};
                for (int k = 0; k < metalist.length; k++) {
                    if (dtVO.getDen().equalsIgnoreCase(Utility.typeToDen(metalist[k])))
                        al_meta.add(k, aDataTypeSupplementaryComponent);
                }
            }
            al = new ArrayList(al_meta);
        }

        for (DataTypeSupplementaryComponent aDataTypeSupplementaryComponent : al) {
            String tmp_guid = null;
            if (isBDTSC(aDataTypeSupplementaryComponent.getOwnerDtId())) {

                Node result = xh.getNode("//xsd:attribute[@id='" + aDataTypeSupplementaryComponent.getGuid() + "']");
                tmp_guid = aDataTypeSupplementaryComponent.getGuid();
                if (result == null) { // if result is null, then look up its based default BDT and get guid
                    result = xh2.getNode("//xsd:attribute[@id='" + aDataTypeSupplementaryComponent.getGuid() + "']");
                    tmp_guid = aDataTypeSupplementaryComponent.getGuid();

                    if (aDataTypeSupplementaryComponent.getBasedDtScId() != 0 && result == null) {
                        DataTypeSupplementaryComponent dtscVO = getDTSC(aDataTypeSupplementaryComponent.getBasedDtScId());
                        result = xh.getNode("//xsd:attribute[@id='" + dtscVO.getGuid() + "']");
                        tmp_guid = dtscVO.getGuid();
                        if (result == null) {
                            result = xh2.getNode("//xsd:attribute[@id='" + dtscVO.getGuid() + "']");
                            tmp_guid = dtscVO.getGuid();
                        }
                    }
                }

                Element ele = (Element) result;
                int codeListId = 0;
                String ele_name = "";

                System.out.print("***** " + aDataTypeSupplementaryComponent.getPropertyTerm() + "_" + aDataTypeSupplementaryComponent.getRepresentationTerm() + " Start! " + aDataTypeSupplementaryComponent.getGuid());

                if (ele != null && ele.getAttribute("type") != null) {
                    String attrTypeName = ele.getAttribute("type").replaceAll("ContentType", "");
                    String typeName = ele.getAttribute("type").replaceAll("ContentType", "");
                    CodeList codeList = codeListRepository.findOneByName(typeName);
                    if (codeList != null) {
                        codeListId = codeList.getCodeListId();
                    } else {
                        codeListId = 0;
                    }
                    ele_name = ele.getAttribute("name");
                    System.out.println(" attrTypeName= " + attrTypeName + " codelist=" + codeListId);
                } else {
                    System.out.println("");
                }

                if (!is_fields_xsd) {
                    DataType dtVO = dataTypeRepository.findOne(aDataTypeSupplementaryComponent.getOwnerDtId());
                    XSDBuiltInType xbtToken = xbtRepository.findOneByBuiltInType("xsd:token");
                    XSDBuiltInType xbtNormalizedString = xbtRepository.findOneByBuiltInType("xsd:normalizedString");

                    if (aDataTypeSupplementaryComponent.getPropertyTerm().equals("Language")) {
                        DataType defaultTextBDT = dataTypeRepository.findOne(dtVO.getBasedDtId());
                        List<DataTypeSupplementaryComponent> baseDTSCs = dtScRepository.findByOwnerDtId(defaultTextBDT.getDtId());

                        for (int i = 0; i < baseDTSCs.size(); i++) {
                            DataTypeSupplementaryComponent adtscVO = baseDTSCs.get(i);
                            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> baseBDTSCPris = bdtScPriRestriRepository.findByBdtScId(adtscVO.getDtScId());

                            for (int j = 0; j < baseBDTSCPris.size(); j++) {
                                BusinessDataTypeSupplementaryComponentPrimitiveRestriction aBDTSCPri = baseBDTSCPris.get(j);
                                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

                                bVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());
                                bVO.setCdtScAwdPriXpsTypeMapId(aBDTSCPri.getCdtScAwdPriXpsTypeMapId());

                                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap CDTSCAPX =
                                        cdtScAwdPriXpsTypeMapRepository.findOne(aBDTSCPri.getCdtScAwdPriXpsTypeMapId());

                                if (CDTSCAPX.getXbtId() == xbtToken.getXbtId()) {//if it is token
                                    bVO.setDefault(true);
                                } else {
                                    bVO.setDefault(false);
                                }

                                XSDBuiltInType xbtName = xbtRepository.findOne(CDTSCAPX.getXbtId());
                                System.out.println("     %%%%% Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + xbtName.getBuiltInType() + " is default = " + bVO.isDefault());
                                bdtScPriRestriRepository.save(bVO);
                            }
                        }
                        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bLanguageVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                        bLanguageVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());

                        CodeList clVO = codeListRepository.findOneByName("clm56392A20081107_LanguageCode");
                        bLanguageVO.setCodeListId(clVO.getCodeListId());
                        bLanguageVO.setDefault(false);
                        System.out.println("     %%%%% Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " code list id = " + bLanguageVO.getCodeListId() + " is default = " + bLanguageVO.isDefault());
                        bdtScPriRestriRepository.save(bLanguageVO);
                    } else if (aDataTypeSupplementaryComponent.getPropertyTerm().equals("Action")) {

                        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtSCAwdPris = cdtScAwdPriRepository.findByCdtScId(aDataTypeSupplementaryComponent.getDtScId());

                        for (int i = 0; i < cdtSCAwdPris.size(); i++) {
                            List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtSCMaps = cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPri(cdtSCAwdPris.get(i).getCdtScAwdPriId());

                            for (int j = 0; j < cdtSCMaps.size(); j++) {
                                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                                bVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());
                                bVO.setCdtScAwdPriXpsTypeMapId(cdtSCMaps.get(j).getCdtScAwdPriXpsTypeMapId());
                                if (cdtSCMaps.get(j).getXbtId() == xbtToken.getXbtId()) {//if it is token
                                    bVO.setDefault(true);
                                } else {
                                    bVO.setDefault(false);
                                }
                                XSDBuiltInType xbtName = xbtRepository.findOne(cdtSCMaps.get(j).getXbtId());
                                System.out.println("     %%%%% Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + xbtName.getBuiltInType() + " is default = " + bVO.isDefault());
                                bdtScPriRestriRepository.save(bVO);
                            }

                        }

                        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bActionCodeVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                        bActionCodeVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());

                        CodeList clVO = null;
                        if (dtVO.getDen().equals("Action Expression. Type")) {
                            clVO = codeListRepository.findOneByName("oacl_ActionCode");
                        } else if (dtVO.getDen().equals("Response Expression. Type")) {
                            clVO = codeListRepository.findOneByName("oacl_ResponseActionCode");
                        }

                        if (clVO != null) {
                            bActionCodeVO.setCodeListId(clVO.getCodeListId());
                            bActionCodeVO.setDefault(false);
                            System.out.println("     %%%%% Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " code list id = " + bActionCodeVO.getCodeListId() + " is default = " + bActionCodeVO.isDefault());
                            bdtScPriRestriRepository.save(bActionCodeVO);
                        }
                    } else if (aDataTypeSupplementaryComponent.getPropertyTerm().equals("Expression Language")) {
                        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtSCAwdPris = cdtScAwdPriRepository.findByCdtScId(aDataTypeSupplementaryComponent.getDtScId());

                        for (int i = 0; i < cdtSCAwdPris.size(); i++) {
                            List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtSCMaps = cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPri(cdtSCAwdPris.get(i).getCdtScAwdPriId());

                            for (int j = 0; j < cdtSCMaps.size(); j++) {
                                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                                bVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());
                                bVO.setCdtScAwdPriXpsTypeMapId(cdtSCMaps.get(j).getCdtScAwdPriXpsTypeMapId());
                                if (cdtSCMaps.get(j).getXbtId() == xbtNormalizedString.getXbtId()) {//if it is normalizedString
                                    bVO.setDefault(true);
                                } else {
                                    bVO.setDefault(false);
                                }
                                XSDBuiltInType xbtName = xbtRepository.findOne(cdtSCMaps.get(j).getXbtId());
                                System.out.println("     %%%%% Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + xbtName.getBuiltInType() + " is default = " + bVO.isDefault());
                                bdtScPriRestriRepository.save(bVO);
                            }

                        }
                    }
                } else if ((aDataTypeSupplementaryComponent.getRepresentationTerm().contains("Code") && codeListId > 0) || ele_name.contains("AgencyID")) {

                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());
                    bVO.setDefault(false);

                    if (ele.getAttribute("name").contains("AgencyID")) {
                        bVO.setAgencyIdListId(getAgencyListID());
                        System.out.println("     $$$$$ Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " agency id list id = " + bVO.getAgencyIdListId() + " is default = " + bVO.isDefault());
                    } else {
                        bVO.setCodeListId(codeListId);
                        System.out.println("     $$$$$ Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " code list id = " + bVO.getCodeListId() + " is default = " + bVO.isDefault());
                    }
                    //System.out.println("Populating bdt sc primitive restriction for bdt sc = "+aDataTypeSupplementaryComponent.getPropertyTerm()+aDataTypeSupplementaryComponent.getRepresentationTerm()+" owner dt den = "+getDen(aDataTypeSupplementaryComponent.getOwnerDtId())+" code list id = "+bVO.getCodeListId()+" agency id list id = "+bVO.getAgencyIDListID()+ " is default = "+bVO.isDefault());
                    bdtScPriRestriRepository.save(bVO);

                    int CDT_Primitive_id = cdtPriRepository.findOneByName("Token").getCdtPriId();

                    int xbt_id = xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();

                    int cdt_id = 0;
                    DataTypeSupplementaryComponent stscVO = dtScRepository.findOne(aDataTypeSupplementaryComponent.getBasedDtScId());
                    if (stscVO == null) {
                        stscVO = new DataTypeSupplementaryComponent();
                    }
                    if (stscVO.getBasedDtScId() < 1) {
                        cdt_id = aDataTypeSupplementaryComponent.getBasedDtScId();
                    } else {
                        cdt_id = stscVO.getBasedDtScId();
                    }

                    int cdt_sc_awd_pri_id = 0;
                    if (cdt_id > 0) {
                        try {
                            cdt_sc_awd_pri_id = cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdt_id, CDT_Primitive_id).getCdtScAwdPriId();
                        } catch (EmptyResultDataAccessException e) {
                        }
                    }

                    int CDTSCAllowedPrimitiveExpressionTypeMapID = 0;
                    if (cdt_sc_awd_pri_id > 0) {
                        CDTSCAllowedPrimitiveExpressionTypeMapID = cdtScAwdPriXpsTypeMapRepository.
                                findOneByCdtScAwdPriAndXbtId(cdt_sc_awd_pri_id, xbt_id).getCdtScAwdPriXpsTypeMapId();
                    }

                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO1 = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bVO1.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());

                    bVO1.setCdtScAwdPriXpsTypeMapId(CDTSCAllowedPrimitiveExpressionTypeMapID);
                    bVO1.setDefault(true);

                    CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCPAX = null;
                    if (CDTSCAllowedPrimitiveExpressionTypeMapID > 0) {
                        aCDTSCPAX = cdtScAwdPriXpsTypeMapRepository.findOne(
                                bVO1.getCdtScAwdPriXpsTypeMapId());
                    }

                    XSDBuiltInType aXBT;
                    if (aCDTSCPAX == null) {
                        aXBT = new XSDBuiltInType();
                    } else {
                        aXBT = xbtRepository.findOne(aCDTSCPAX.getXbtId());
                    }
                    System.out.println("     $$$$$ Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + aXBT.getBuiltInType() + "  is default = " + bVO1.isDefault());
                    bdtScPriRestriRepository.save(bVO1);

                } else {
                    List<CoreDataTypeSupplementaryComponentAllowedPrimitive> al3;
                    if (aDataTypeSupplementaryComponent.getBasedDtScId() < 1) { //new attributes SC
                        al3 = cdtScAwdPriRepository.findByCdtScId(aDataTypeSupplementaryComponent.getDtScId());
                    } else { // DTSC is based on other dt_sc
                        al3 = cdtScAwdPriRepository.findByCdtScId(aDataTypeSupplementaryComponent.getBasedDtScId());
                        if (al3.isEmpty()) {
                            DataTypeSupplementaryComponent dataTypeSupplementaryComponent = dtScRepository.findOne(aDataTypeSupplementaryComponent.getBasedDtScId());
                            al3 = cdtScAwdPriRepository.findByCdtScId(dataTypeSupplementaryComponent.getBasedDtScId());
                        }
                    }

                    for (CoreDataTypeSupplementaryComponentAllowedPrimitive aCDTSCAllowedPrimitiveVO : al3) {//Loop retrieved cdt_sc_awd_pri\
                        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> al4 =
                                cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPri(aCDTSCAllowedPrimitiveVO.getCdtScAwdPriId());
                        for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExVO : al4) {
                            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                            bVO.setBdtScId(aDataTypeSupplementaryComponent.getDtScId());

                            bVO.setCdtScAwdPriXpsTypeMapId(aCDTSCAllowedPrimitiveExVO.getCdtScAwdPriXpsTypeMapId());

                            XSDBuiltInType xbtVO = getXbtId(aCDTSCAllowedPrimitiveExVO.getXbtId());
                            String xdtName = xbtVO.getBuiltInType();

                            int cdtPrimitiveId = aCDTSCAllowedPrimitiveVO.getCdtPriId();

                            String representationTerm = aDataTypeSupplementaryComponent.getRepresentationTerm();
                            if (representationTerm.equalsIgnoreCase("Code") && xdtName.equalsIgnoreCase("xsd:token") && "Token".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Identifier") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Name") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Indicator") && xdtName.equalsIgnoreCase("xsd:boolean") && "Boolean".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Value") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Text") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else if (representationTerm.equalsIgnoreCase("Date Time") && xdtName.equalsIgnoreCase("xsd:token") && "Timepoint".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                bVO.setDefault(true);
                            } else {
                                bVO.setDefault(false);
                            }

                            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCPAX =
                                    cdtScAwdPriXpsTypeMapRepository.findOne(bVO.getCdtScAwdPriXpsTypeMapId());


                            XSDBuiltInType aXBT = xbtRepository.findOne(aCDTSCPAX.getXbtId());

                            //exceptional case
                            if (ele != null && ele.getAttribute("type") != null && ele.getAttribute("type").equalsIgnoreCase("NumberType_B98233")) {
                                if (representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:integer") && "Integer".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                    bVO.setDefault(true);
                                    System.out.println("     ##### Populating bdt sc primitive restriction(NumberType_B98233) for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + aXBT.getBuiltInType() + "  is default = " + bVO.isDefault());
                                } else if (representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
                                    bVO.setDefault(false);
                                    System.out.println("     ##### Populating bdt sc primitive restriction(NumberType_B98233) for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + aXBT.getBuiltInType() + "  is default = " + bVO.isDefault());
                                }
                            }
                            //System.out.println("representation term = "+representationTerm+" xbt name = "+xdtName+" cdt primitive name = "+getCDTPrimitiveName(cdtPrimitiveId));
                            System.out.println("     ##### Populating bdt sc primitive restriction for bdt sc = " + aDataTypeSupplementaryComponent.getPropertyTerm() + aDataTypeSupplementaryComponent.getRepresentationTerm() + " owner dt den = " + getDen(aDataTypeSupplementaryComponent.getOwnerDtId()) + " xbt = " + aXBT.getBuiltInType() + "  is default = " + bVO.isDefault());
                            bdtScPriRestriRepository.save(bVO);
                        }
                    }
                }
            }
        }
    }

    public DataTypeSupplementaryComponent getDTSC(int id) throws Exception {
        return dtScRepository.findOne(id);
    }

    public String getDen(int id) throws Exception {
        return dataTypeRepository.findOne(id).getDen();
    }

    public boolean isBDTSC(int id) throws Exception {
        DataType tmp = dataTypeRepository.findOne(id);

        if (tmp != null && tmp.getType() == 1)
            return true;
        return false;
    }

    public String getCDTPrimitiveName(int id) throws Exception {
        return cdtPriRepository.findOne(id).getName();
    }

    public XSDBuiltInType getXbtId(int id) throws Exception {
        return xbtRepository.findOne(id);
    }

    public static void main(String args[]) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_5_6_PopulateBDTSCPrimitiveRestriction populateBDTSCPrimitiveRestriction = ctx.getBean(P_1_5_6_PopulateBDTSCPrimitiveRestriction.class);
            populateBDTSCPrimitiveRestriction.run(ctx);
        }
    }
}