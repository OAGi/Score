package org.oagi.srt.persistence.populate;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.BODElementVO;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_8_PopulateAccAsccpBccAscc {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    private AggregateCoreComponentRepository accDao;
    private AssociationCoreComponentPropertyRepository asccpDao;
    private BasicCoreComponentPropertyRepository bccpDao;
    private BasicCoreComponentRepository bccDao;
    private AssociationCoreComponentRepository asccDao;

    private BODSchemaHandler bodSchemaHandler;
    private String bodPath;

    private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
    private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);

    @PostConstruct
    public void init() throws Exception {
        accDao = repositoryFactory.aggregateCoreComponentRepository();
        asccDao = repositoryFactory.associationCoreComponentRepository();
        asccpDao = repositoryFactory.associationCoreComponentPropertyRepository();
        bccpDao = repositoryFactory.basicCoreComponentPropertyRepository();
        bccDao = repositoryFactory.basicCoreComponentRepository();
    }

    private void populate() throws Exception {
        populate1();
        populate2();
    }

    private void populate1() throws Exception {

        File[] listOfF1 = getBODs(f1);
        File[] listOfF2 = getBODs(f2);

        for (File file : listOfF1) {
            if (file.getName().endsWith("AcknowledgeInvoice.xsd")) {
                System.out.println(file.getName() + " ing...");
                insertASCCP(file);
            }
        }

        for (File file : listOfF2) {
            if (file.getName().endsWith("AcknowledgeInvoice.xsd")) {
                System.out.println(file.getName() + " ing...");
                insertASCCP(file);
            }
        }
    }

    private void populate2() throws Exception {

        File[] listOfF1 = getBODs(f1);
        File[] listOfF2 = getBODs(f2);

        for (File file : listOfF1) {
            if (!file.getName().endsWith("AcknowledgeInvoice.xsd")) {
                System.out.println(file.getName() + " ing...");
                insertASCCP(file);
            }
        }

        for (File file : listOfF2) {
            if (!file.getName().endsWith("AcknowledgeInvoice.xsd")) {
                System.out.println(file.getName() + " ing...");
                insertASCCP(file);
            }
        }
        modifySequeceKeyforGroup();
        modifySequeceKeyforGroup_temp();
    }

    private void insertASCCP(File file) throws Exception {
        bodPath = file.getAbsolutePath();
        bodSchemaHandler = new BODSchemaHandler(bodPath);
        XSElementDecl element = bodSchemaHandler.getGlobalElementDeclaration();
        XSComplexTypeDecl complexType = bodSchemaHandler.getComplexTypeDefinition(element);
        if (bodSchemaHandler.isComplexWithoutSimpleContent(complexType.getTypeName())) {
            insertASCCP(element, complexType);
//			try {
//				Thread.sleep(150);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
        }
    }

    private void insertASCCP(XSElementDecl element, XSComplexTypeDecl complexType) throws Exception {

        String name = element.getName();
        //System.out.println("### asccp name: " + name);

        if (complexType != null) {
            String asccpGuid = element.getFId();
            String propertyTerm = Utility.spaceSeparator(name);
            String definition = bodSchemaHandler.getAnnotation(element);

            int roleOfAccId;
            AggregateCoreComponent accVO = null;
            try {
                accVO = accDao.findOneByGuid(complexType.getFId());
            } catch (EmptyResultDataAccessException e) {
            }

            if (accVO == null) {
                insertACC(complexType, bodPath);
                accVO = accDao.findOneByGuid(complexType.getFId());
            }
            roleOfAccId = accVO.getAccId();

            String den = propertyTerm + ". " + Utility.spaceSeparator(Utility.first(accVO.getDen()));
            int state = 3;
            String module = bodPath.substring(bodPath.indexOf("Model"));
            module = module.replace("\\", "/");

            AssociationCoreComponentProperty accpVO = new AssociationCoreComponentProperty();
            accpVO.setGuid(asccpGuid);
            accpVO.setPropertyTerm(propertyTerm);
            accpVO.setDefinition(definition);
            accpVO.setRoleOfAccId(roleOfAccId);
            accpVO.setDen(den);
            accpVO.setState(state);
            accpVO.setModule(module);
            int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
            accpVO.setCreatedBy(userId);
            accpVO.setLastUpdatedBy(userId);
            accpVO.setOwnerUserId(userId);
            accpVO.setDeprecated(false);
            accpVO.setNamespaceId(1); //tmp
            accpVO.setReleaseId(1);//tmp
            asccpDao.save(accpVO);

        }
//		try {
//			Thread.sleep(150);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
    }

    private void insertASCCPUnderGroup(BODElementVO bodVO) throws Exception {

        String name = bodVO.getElement().getName();
        //System.out.println("### asccp name: " + name);

        String asccpGuid = bodVO.getElement().getFId();
        String propertyTerm = Utility.spaceSeparator(name);
        String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());

        AggregateCoreComponent accVO = accDao.findOneByGuid(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
        int roleOfAccId = accVO.getAccId();

        String den = propertyTerm + ". " + Utility.first(accVO.getDen());
        int state = 3;
        String module = bodPath.substring(bodPath.indexOf("Model"));
        module = module.replace("\\", "/");

        AssociationCoreComponentProperty accpVO = new AssociationCoreComponentProperty();
        accpVO.setGuid(asccpGuid);
        accpVO.setPropertyTerm(propertyTerm);
        accpVO.setDefinition(definition);
        accpVO.setRoleOfAccId(roleOfAccId);
        accpVO.setDen(den);
        accpVO.setState(state);
        accpVO.setModule(module);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        accpVO.setCreatedBy(userId);
        accpVO.setLastUpdatedBy(userId);
        accpVO.setOwnerUserId(userId);
        accpVO.setDeprecated(false);
        accpVO.setNamespaceId(1); //tmp
        accpVO.setReleaseId(1);//tmp
        asccpDao.save(accpVO);

//		try {
//			Thread.sleep(150);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
    }

    private void insertASCC(BODElementVO bodVO, String parentGuid, AssociationCoreComponentProperty asccpVO) throws Exception {

        AggregateCoreComponent accVO = accDao.findOneByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();
        if (assocFromACCId == 71687)
            System.out.println("bod ref =" + bodVO.getRef() + "   bod id = " + bodVO.getId());
        //qc1.add("assco_to_asccp_id", asccpVO.getAsccpId());
        //qc1.add("from_acc_id", assocFromACCId);

        try {
            asccDao.findOneByGuid((bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId());
        } catch (EmptyResultDataAccessException e) {
            String asccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
            int cardinalityMin = bodVO.getMinOccur();
            int cardinalityMax = bodVO.getMaxOccur();
            int sequenceKey = bodVO.getOrder();
            int assocToASCCPId = asccpVO.getAsccpId();
            String den = Utility.first(accVO.getDen()) + ". " + asccpVO.getDen();

            String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());

            AssociationCoreComponent asscVO = new AssociationCoreComponent();
            asscVO.setGuid(asccGuid);
            asscVO.setCardinalityMin(cardinalityMin);
            asscVO.setCardinalityMax(cardinalityMax);
            asscVO.setSeqKey(sequenceKey);
            asscVO.setFromAccId(assocFromACCId);
            asscVO.setToAsccpId(assocToASCCPId);
            asscVO.setDen(den);
            asscVO.setDefinition(definition);
            asscVO.setState(3);
            asscVO.setDeprecated(false);
            asscVO.setReleaseId(1);//tmp
            asccDao.save(asscVO);

//
//
//			// TODO think about this
//			if(bodVO.getGroupId() != null) {
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("guid", bodVO.getGroupRef());
//				if(asccDao.findObject(qc2) == null) {
//					AssociationCoreComponent asscVO1 = new AssociationCoreComponent();
//					asscVO1.setGuid(bodVO.getGroupRef());
//					asscVO1.setCardinalityMin(1);
//					asscVO1.setCardinalityMax(1);
//					asscVO1.setSeqKey(sequenceKey); // TODO check this
//
//					AggregateCoreComponent accVO1 = getACC(bodVO.getGroupId());
//					AssociationCoreComponentProperty asccVO1 = getACC(bodVO.getGroupId());
//					if(accVO1 != null) {
//						assocFromACCId = accVO1.getAccId();
//						assocToASCCPId
//					} else {
//						assocFromACCId = insertACCWithGroup();
//					}
//
//					if(bodVO.getGroupParent() == null) {
//						AggregateCoreComponent accVO1 = getACC(bodVO.getGroupId());
//						if(accVO1 != null) {
//							assocFromACCId = accVO1.getAccId();
//						} else {
//							assocFromACCId = insertACCWithGroup();
//						}
//					}
//
//					asscVO1.setFromAccId(assocFromACCId);
//					asscVO1.setToAsccpId(assocToASCCPId);
//					asscVO1.setDen(den);
//					asscVO1.setDefinition(definition);
//
//					asccDao.save(asscVO);
//				}
//
//			}
        }
    }

    private void modifySequeceKeyforGroup() throws Exception {
        //modify sequenceKey for group
        List<AssociationCoreComponent> groupobjects = asccDao.findByDefinition("Group");
        for (AssociationCoreComponent asccVO : groupobjects) {
            int ElementsInGroup = 0;
            List<AssociationCoreComponent> asccObjects = asccDao.findByFromAccId(asccVO.getFromAccId());
            for (AssociationCoreComponent ascc : asccObjects) {
                AssociationCoreComponentProperty asccp = asccpDao.findOneByAsccpId(ascc.getToAsccpId());
                if (asccp.getDefinition() != null &&
                        asccp.getDefinition().equalsIgnoreCase("Group") &&
                        asccVO.getSeqKey() > ascc.getSeqKey()) {
                    String groupname = asccp.getPropertyTerm().replaceAll(" ", "");
                    List<AssociationCoreComponent> asccObjectsinGroup = asccDao.findByDenStartsWith(groupname);
                    List<BasicCoreComponent> bccObjectsinGroup = bccDao.findByDenStartsWith(groupname);
                    ElementsInGroup += (asccObjectsinGroup.size() + bccObjectsinGroup.size());
                    if ((asccObjectsinGroup.size() + bccObjectsinGroup.size()) > 0) {
                        ElementsInGroup--;
                    }
                }
            }
            int new_seq = asccVO.getSeqKey() - ElementsInGroup;
            ((AssociationCoreComponent) asccVO).setSeqKey(new_seq);
            asccDao.update(asccVO);
        }
    }

    private void modifySequeceKeyforGroup_temp() throws Exception {
        //modify sequenceKey for group
        System.out.println("Modifying sequence key for groups temporarily");
        List<AssociationCoreComponent> groupobjects = asccDao.findByDenContaining("Actual Resource Group");
        for (AssociationCoreComponent asccVO : groupobjects) {
            List<AssociationCoreComponent> asccObjects = asccDao.findByFromAccId(asccVO.getFromAccId());
            List<BasicCoreComponent> bccObjects = bccDao.findByFromAccId(asccVO.getFromAccId());
            for (int i = 0; i < asccObjects.size(); i++) {
                AssociationCoreComponent ascc = asccObjects.get(i);
                if (ascc.getDen() != null && ascc.getDen().contains("Actual Resource Group")) {
                    if (i + 1 < asccObjects.size()) {
                        AssociationCoreComponent ascc2 = asccObjects.get(i + 1);
                        if (ascc2.getDen() != null && ascc2.getDen().contains("Free Form Text Group")) {
                            ascc2.setSeqKey(ascc.getSeqKey() + 1);
                            asccDao.update(ascc2);
                            for (int j = 0; j < asccObjects.size(); j++) {
                                AssociationCoreComponent changed_ascc = asccObjects.get(j);
                                if (changed_ascc.getSeqKey() >= ascc2.getSeqKey() && changed_ascc.getAsccId() != ascc2.getAsccId()) {
                                    changed_ascc.setSeqKey(changed_ascc.getSeqKey() + 1);
                                    asccDao.update(changed_ascc);
                                }
                            }
                            for (int j = 0; j < bccObjects.size(); j++) {
                                BasicCoreComponent changed_bcc = bccObjects.get(j);
                                if (changed_bcc.getSeqKey() >= ascc2.getSeqKey()) {
                                    changed_bcc.setSeqKey(changed_bcc.getSeqKey() + 1);
                                    bccDao.update(changed_bcc);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private AggregateCoreComponent getACC(String guid) throws Exception {
        try {
            return accDao.findOneByGuid(guid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private AssociationCoreComponentProperty getASCCP(String guid) throws Exception {
        try {
            return asccpDao.findOneByGuid(guid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private AssociationCoreComponent getASCC(String guid) throws Exception {
        try {
            return asccDao.findOneByGuid(guid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void insertBCC(BODElementVO bodVO, String parentGuid, BasicCoreComponentProperty bccpVO) throws Exception {
        if (bccpVO == null)
            return;

        //String bccGuid = bodVO.getId();
        String bccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
        int assocToBccpId = bccpVO.getBccpId();

        AggregateCoreComponent accVO = accDao.findOneByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();

        try {
            bccDao.findOnebyGuidAndFromAccIdAndToBccpId(bccGuid, assocFromACCId, assocToBccpId);
        } catch (EmptyResultDataAccessException e) {
            int cardinalityMin = bodVO.getMinOccur();
            int cardinalityMax = bodVO.getMaxOccur();
            int sequenceKey = bodVO.getOrder();

            int entityType = 1;
            String den = Utility.first(accVO.getDen()) + ". " + bccpVO.getDen();

            BasicCoreComponent aBasicCoreComponent = new BasicCoreComponent();
            aBasicCoreComponent.setGuid(bccGuid);
            aBasicCoreComponent.setCardinalityMin(cardinalityMin);
            aBasicCoreComponent.setCardinalityMax(cardinalityMax);
            aBasicCoreComponent.setToBccpId(assocToBccpId);
            aBasicCoreComponent.setFromAccId(assocFromACCId);
            aBasicCoreComponent.setSeqKey(sequenceKey);
            aBasicCoreComponent.setEntityType(entityType);
            aBasicCoreComponent.setDen(den);
            aBasicCoreComponent.setState(3);
            aBasicCoreComponent.setDeprecated(false);
            aBasicCoreComponent.setReleaseId(1);//tmp
            bccDao.save(aBasicCoreComponent);
        }

    }

    private void insertBCCWithAttr(XSAttributeDecl xad, XSComplexTypeDecl complexType) throws Exception {//check
        String bccGuid = xad.getFId();

        int cardinalityMin = (xad.getFUse() == null) ? 0 : (xad.getFUse().equals("optional") || xad.getFUse().equals("prohibited")) ? 0 : (xad.getFUse().equals("required")) ? 1 : 0;
        int cardinalityMax = (xad.getFUse() == null) ? 1 : (xad.getFUse().equals("optional") || xad.getFUse().equals("required")) ? 1 : (xad.getFUse().equals("prohibited")) ? 0 : 0;
        int sequenceKey = 0;

        XSSimpleTypeDecl xtd = (XSSimpleTypeDecl) xad.getTypeDefinition();

        int assocToBccpId;
        BasicCoreComponentProperty bccpVO = null;
        try {
            bccpVO = bccpDao.findOneByPropertyTerm(Utility.spaceSeparator(xad.getName()).replace("ID", "Identifier"));
        } catch (EmptyResultDataAccessException e) {
        }

        if (bccpVO == null) {
            bccpVO = insertBCCP(xad.getName(), xtd.getFId());
        }
        if (bccpVO == null) {
            System.out.println("BCCP creation is failed in BCC attriute creation, Note -> Name : " + xad.getName() + "   guid : " + xtd.getFId());
            return;//check
        }
        assocToBccpId = bccpVO.getBccpId();

        if (!xad.getName().equals("responseCode")) {

            try {
                bccDao.findOnebyGuidAndToBccpId(bccGuid, assocToBccpId);
            } catch (EmptyResultDataAccessException e) {
                String parentGuid = complexType.getFId();
                AggregateCoreComponent accVO = accDao.findOneByGuid(parentGuid);
                int assocFromACCId = accVO.getAccId();

                int entityType = 0;
                String den = Utility.first(accVO.getDen()) + ". " + bccpVO.getDen();

                BasicCoreComponent aBasicCoreComponent = new BasicCoreComponent();
                aBasicCoreComponent.setGuid(bccGuid);
                aBasicCoreComponent.setCardinalityMin(cardinalityMin);
                aBasicCoreComponent.setCardinalityMax(cardinalityMax);
                aBasicCoreComponent.setToBccpId(assocToBccpId);
                aBasicCoreComponent.setFromAccId(assocFromACCId);
                aBasicCoreComponent.setSeqKey(sequenceKey);
                aBasicCoreComponent.setEntityType(entityType);
                aBasicCoreComponent.setDen(den);
                aBasicCoreComponent.setState(3);
                aBasicCoreComponent.setDeprecated(false);
                aBasicCoreComponent.setReleaseId(1);//tmp
                bccDao.save(aBasicCoreComponent);
            }

        }
    }

    private BasicCoreComponentProperty insertBCCP(String name, String id) throws Exception {
        String bccpGuid = Utility.generateGUID();
        String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

        //System.out.println("### BCCP: " + name + " " + id);
        if (id == null) {
            System.out.println("!!!! id is null where name is  = " + name);
            //id = "oagis-id-89be97039be04d6f9cfda107d75926b4"; // TODO check why dt is null and change this line
            return null;
        }
        DataType dtVO;
        try {
            dtVO = dataTypeRepository.findOneByGuidAndType(id, 1);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("!!!! DT is null where name is  = " + name + " and id is = " + id);
            return null;
            //QueryCondition qc1 = new QueryCondition();
            //qc1.add("guid", "oagis-id-89be97039be04d6f9cfda107d75926b4"); // TODO check why dt is null and change this line
            //dtVO = (DataType)dtDao.findObject(qc1);
        }

        int bdtId = dtVO.getDtId();
        String representationTerm = dtVO.getDataTypeTerm();
        String den = Utility.firstToUpperCase(propertyTerm) + ". " + representationTerm;

        BasicCoreComponentProperty bccpVO = new BasicCoreComponentProperty();
        bccpVO.setGuid(bccpGuid);
        bccpVO.setPropertyTerm(propertyTerm);
        bccpVO.setBdtId(bdtId);
        bccpVO.setRepresentationTerm(representationTerm);
        bccpVO.setDen(den);
        bccpVO.setState(3);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        bccpVO.setCreatedBy(userId);
        bccpVO.setLastUpdatedBy(userId);
        bccpVO.setOwnerUserId(userId);
        bccpVO.setDeprecated(false);
        bccpVO.setReleaseId(1);//tmp
        bccpDao.save(bccpVO);

        return bccpDao.findOneByGuid(bccpGuid);
    }


    private void insertForGroup(BODElementVO bodVO, String fullFilePath, String complexTypeId, int cnt) throws Exception {
        //System.out.println("------------------------" + bodVO.getId() + " | " + bodVO.getName() + " | " + bodVO.getGroupId() + " | " + bodVO.getGroupName());

        //System.out.println("### type: " + bodVO.getOrder() + " | name: " + bodVO.getName() + " | id: " + bodVO.getId() + " | ref: " + bodVO.getRef() + " | group?: " + bodVO.isGroup() + " | groupid: " + bodVO.getGroupId() + " | groupref: " + bodVO.getGroupRef() + " | grouparent: " + bodVO.getGroupParent());


        String objectClassName = Utility.spaceSeparator(bodVO.getGroupName().substring(0, (bodVO.getGroupName().indexOf("Type") > 0) ? bodVO.getGroupName().indexOf("Type") : bodVO.getGroupName().length()));
        String den = objectClassName + ". Details";
        int oagisComponentType = 1;
        if (Utility.first(den).endsWith("Base"))
            oagisComponentType = 0;
        else if (Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group") || objectClassName.equalsIgnoreCase("Common Time Reporting"))
            oagisComponentType = 3;
        String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
        module = module.replace("\\", "/");
        insertACCForGroup(bodVO, objectClassName, den, oagisComponentType, module);

        int groupAccId = accDao.findOneByGuid(bodVO.getGroupId()).getAccId();
        insertASCCPForGroup(bodVO, groupAccId, den, module);

        inserASCCForGroup(bodVO, complexTypeId, cnt);
    }

    private void insertACCForGroup(BODElementVO bodVO, String objectClassName, String accDen, int oagisComponentType, String module) throws Exception {
        if (getACC(bodVO.getGroupId()) == null) {
            AggregateCoreComponent aAggregateCoreComponent = new AggregateCoreComponent();
            aAggregateCoreComponent.setGuid(bodVO.getGroupId());
            aAggregateCoreComponent.setObjectClassTerm(objectClassName);
            aAggregateCoreComponent.setDen(accDen);
            aAggregateCoreComponent.setDefinition("Group");
            aAggregateCoreComponent.setOagisComponentType(oagisComponentType);
            aAggregateCoreComponent.setBasedAccId(-1);
            int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
            aAggregateCoreComponent.setCreatedBy(userId);
            aAggregateCoreComponent.setLastUpdatedBy(userId);
            aAggregateCoreComponent.setOwnerUserId(userId);
            aAggregateCoreComponent.setState(3);
            aAggregateCoreComponent.setModule(module);
            aAggregateCoreComponent.setDeprecated(false);
            aAggregateCoreComponent.setNamespaceId(1); //tmp
            aAggregateCoreComponent.setReleaseId(1);//tmp
            accDao.save(aAggregateCoreComponent);
        }
    }

    private void insertASCCPForGroup(BODElementVO bodVO, int groupAccId, String accDen, String module) throws Exception {
        if (getASCCP(bodVO.getGroupId()) == null) {
            String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());

            AssociationCoreComponentProperty asccpVO = new AssociationCoreComponentProperty();
            asccpVO.setGuid(bodVO.getGroupId());
            asccpVO.setPropertyTerm(propertyTerm);
            asccpVO.setDefinition("Group");

//			int roleOfAccId = -1;
//			if (bodVO.getGroupParent() != null) {
//				roleOfAccId = getACC(bodVO.getGroupParent()).getAccId();
//			} else {
//				roleOfAccId = getACC(complexTypeId).getAccId();
//			}

            asccpVO.setRoleOfAccId(groupAccId);
            asccpVO.setDen(Utility.spaceSeparator(propertyTerm + ". " + Utility.first(accDen)));
            asccpVO.setState(3);
            asccpVO.setModule(module);
            int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
            asccpVO.setCreatedBy(userId);
            asccpVO.setLastUpdatedBy(userId);
            asccpVO.setOwnerUserId(userId);
            asccpVO.setDeprecated(false);
            asccpVO.setNamespaceId(1); //tmp
            asccpVO.setReleaseId(1);//tmp
            asccpDao.save(asccpVO);
        }
    }

    private void inserASCCForGroup(BODElementVO bodVO, String complexTypeId, int cnt) throws Exception {
        //if(getASCC(bodVO.getGroupRef()) == null) {
        AggregateCoreComponent accVO = getACC(bodVO.getGroupId());
        AssociationCoreComponentProperty asccpVO1 = getASCCP(bodVO.getGroupId());
        int assocToASCCPId = asccpVO1.getAsccpId();

        int accId = 0;
        if (bodVO.getGroupParent() == null) {
            accId = getACC(complexTypeId).getAccId();
        } else {
            accId = getACC(bodVO.getGroupParent()).getAccId();
        }

        AggregateCoreComponent accVO2 = accDao.findOneByAccId(accId);

        AssociationCoreComponent asscVO = new AssociationCoreComponent();
        asscVO.setGuid(bodVO.getGroupRef());
        asscVO.setCardinalityMin(1);
        asscVO.setCardinalityMax(1);
        asscVO.setFromAccId(accId);
        asscVO.setSeqKey(cnt); // TODO check this
        asscVO.setToAsccpId(assocToASCCPId);

        asscVO.setDen(Utility.first(accVO2.getDen()) + ". " + asccpVO1.getDen());
        asscVO.setDefinition("Group");
        asscVO.setState(3);
        asscVO.setDeprecated(false);
        asscVO.setReleaseId(1);//tmp
        asccDao.save(asscVO);
        //}
    }

    private ArrayList<String> insertACC(XSComplexTypeDecl complexType, String fullFilePath) throws Exception {

        ArrayList<String> elements = new ArrayList<String>();
        //System.out.println("### acc type: " + complexType.getName());

        String accGuid = complexType.getFId();
        String objectClassName = Utility.spaceSeparator(complexType.getName().substring(0, complexType.getName().indexOf("Type")));
        String den = objectClassName + ". Details";
        String definition = bodSchemaHandler.getAnnotation(complexType);

        int basedAccId = -1;
        String base = complexType.getBaseType().getName();
        //System.out.println("### base type: " + base + " - " + complexType.getBaseType().getTypeCategory());
        if (base != null && !base.equals("anyType") && complexType.getBaseType().getTypeCategory() != 16) {
            XSComplexTypeDecl baseType = bodSchemaHandler.getComplexTypeDefinition(base);

            AggregateCoreComponent accVO;
            try {
                accVO = accDao.findOneByGuid(baseType.getFId());
                basedAccId = accVO.getAccId();
                XSParticle particle = bodSchemaHandler.getComplexTypeDefinition(base).getParticle();
                if (particle != null) {
                    ArrayList<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
                    for (BODElementVO bodVO : al) {
                        elements.add(bodVO.getName());
                    }
                }
            } catch (EmptyResultDataAccessException e) {
                elements = insertACC(baseType, fullFilePath);
                accVO = accDao.findOneByGuid(baseType.getFId());
                basedAccId = accVO.getAccId();
            }
        }

        int oagisComponentType = 1;
        if (Utility.first(den).endsWith("Base"))
            oagisComponentType = 0;
        else if (Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group"))
            oagisComponentType = 3;

        int state = 3;
        String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
        module = module.replace("\\", "/");

        AggregateCoreComponent aAggregateCoreComponent = new AggregateCoreComponent();
        aAggregateCoreComponent.setGuid(accGuid);
        aAggregateCoreComponent.setObjectClassTerm(objectClassName);
        aAggregateCoreComponent.setDen(den);
        aAggregateCoreComponent.setDefinition(definition);
        aAggregateCoreComponent.setBasedAccId(basedAccId);
        aAggregateCoreComponent.setOagisComponentType(oagisComponentType);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        aAggregateCoreComponent.setCreatedBy(userId);
        aAggregateCoreComponent.setLastUpdatedBy(userId);
        aAggregateCoreComponent.setOwnerUserId(userId);
        aAggregateCoreComponent.setState(state);
        aAggregateCoreComponent.setModule(module);
        aAggregateCoreComponent.setDeprecated(false);
        aAggregateCoreComponent.setNamespaceId(1); //tmp
        aAggregateCoreComponent.setReleaseId(1);//tmp
        accDao.save(aAggregateCoreComponent);

        XSParticle particle = complexType.getParticle();
        if (particle != null) {
            ArrayList<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
            String tempGroupId = "";
            int cnt = 1;
            for (BODElementVO bodVO : al) {
                if (!elements.contains(bodVO.getName())) {
                    elements.add(bodVO.getName());

                    if (bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                        AggregateCoreComponent accVO;
                        try {
                            accVO = accDao.findOneByGuid(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
                        } catch (EmptyResultDataAccessException e) {
                            insertACC(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()), fullFilePath);
                        }
                    }


                    // insert ACC with group id, if group parent == null, then complexType is based ACC, else parent is based ACC
                    // insert ASCCP with group id, from ACC group
                    // insert ASCC with group ref, from ACC is parent or CT, to ASCCP is group id

                    if (bodVO.getGroupId() != null && !tempGroupId.equals(bodVO.getGroupId())) {
                        //System.out.println("--- eleID: " + bodVO.getElement().getFId() + " | name: " + bodVO.getName() + " | id: " + bodVO.getId() + " | ref: " + bodVO.getRef() + " | group?: " + bodVO.isGroup() + " | groupid: " + bodVO.getGroupId() + " | groupref: " + bodVO.getGroupRef() + " | grouparent: " + bodVO.getGroupParent());

                        tempGroupId = bodVO.getGroupId();
                        insertForGroup(bodVO, fullFilePath, complexType.getFId(), cnt);
                    }

                    //System.out.println("#######################XX bodVO.getName() " + bodVO.getName());
                    AssociationCoreComponentProperty asccpVO;
                    try {
                        asccpVO = asccpDao.findOneByGuid(bodVO.getId());
                        //System.out.println("####################### match to ascc - " + bodVO.getName());
                        insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO);
                    } catch (EmptyResultDataAccessException e) {
                        BasicCoreComponentProperty bccpVO;
                        try {
                            bccpVO = bccpDao.findOneByGuid(bodVO.getId());
                            //System.out.println("####################### match to bccp - " + bodVO.getName());
                            insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
                        } catch (EmptyResultDataAccessException e1) {
                            //System.out.println("####################### no match case - " + bodVO.getName());
                            //if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                            //insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getElement()));
                            if (bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                                if (bodVO.getGroupId() == null) {
                                    insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()));
                                } else {

                                    String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());
                                    den = propertyTerm + ". " + Utility.first(den);
                                    insertASCCPUnderGroup(bodVO);
                                }

                                AssociationCoreComponentProperty asccpVO1 = asccpDao.findOneByGuid(bodVO.getId());
                                insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO1);
                            } else if (bodSchemaHandler.isComplexWithSimpleContent(bodVO.getTypeName())) {
                                bccpVO = insertBCCP(bodVO.getName(), bodVO.getId());
                                insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
                            }
                        }
                    }
                    cnt++;
                }
            }
        }

        XSObjectList xol = complexType.getAttributeUses();
        for (int i = 0; i < xol.getLength(); i++) {
            XSAttributeUseImpl xui = (XSAttributeUseImpl) xol.get(i);
            XSAttributeDecl xad = (XSAttributeDecl) xui.getAttrDeclaration();
            //if(!xad.getName().equals("releaseID") && !xad.getName().equals("versionID") && !xad.getName().equals("systemEnvironmentCode")) {
            //System.out.println("####################### attribute: " + complexType.getName() + " | " + xad.getName());
            insertBCCWithAttr(xad, complexType);
            //}
        }

        return elements;
    }

    private File[] getBODs(File f) {
        return f.listFiles((dir, name) -> {
            return name.matches(".*.xsd");
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.8 Start");

        populate();

        System.out.println("### 1.8 End");
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_8_PopulateAccAsccpBccAscc populateAccAsccpBccAscc = ctx.getBean(P_1_8_PopulateAccAsccpBccAscc.class);
            populateAccAsccpBccAscc.run(ctx);
        }
    }
}