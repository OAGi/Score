package org.oagi.srt.persistence.populate;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.util.ArrayList;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.BODElementVO;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.w3c.dom.NodeList;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */

public class P_1_8_PopulateAccAsccpBccAscc {

	private DAOFactory df;
	private SRTDAO accDao;
	private SRTDAO asccpDao;
	private SRTDAO bccpDao;
	private SRTDAO bccDao;
	private SRTDAO asccDao;
	private SRTDAO dtDao;
	private BODSchemaHandler bodSchemaHandler;
	private String bodPath;

	private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
	private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);	
	
	public P_1_8_PopulateAccAsccpBccAscc() throws SRTDAOException {
		df = DAOFactory.getDAOFactory();
		accDao = df.getDAO("ACC");
		asccDao = df.getDAO("ASCC");
		asccpDao = df.getDAO("ASCCP");
		bccpDao = df.getDAO("BCCP");
		bccDao = df.getDAO("BCC");
		dtDao = df.getDAO("DT");
	}

	private void populate() throws Exception {
		populate1();
		populate2();
	}
	
	private void populate1() throws Exception {

		File[] listOfF1 = getBODs(f1);
		File[] listOfF2 = getBODs(f2);

		for (File file : listOfF1) {
			if(file.getName().endsWith("AcknowledgeInvoice.xsd")){	
				System.out.println(file.getName()+" ing...");
				insertASCCP(file);
			}
		}

		for (File file : listOfF2) {
			if(file.getName().endsWith("AcknowledgeInvoice.xsd")){		
				System.out.println(file.getName()+" ing...");
				insertASCCP(file);
			}
		}
	} 
	
	private void populate2() throws Exception {

		File[] listOfF1 = getBODs(f1);
		File[] listOfF2 = getBODs(f2);

		for (File file : listOfF1) {
			if(!file.getName().endsWith("AcknowledgeInvoice.xsd")){	
				System.out.println(file.getName()+" ing...");
				insertASCCP(file);
			}
		}

		for (File file : listOfF2) {
			if(!file.getName().endsWith("AcknowledgeInvoice.xsd")){		
				System.out.println(file.getName()+" ing...");
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
		if(bodSchemaHandler.isComplexWithoutSimpleContent(complexType.getTypeName())) {
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

		if(complexType != null) {
			String asccpGuid = element.getFId();
			String propertyTerm = Utility.spaceSeparator(name);
			String definition = bodSchemaHandler.getAnnotation(element);

			int roleOfAccId;
			QueryCondition qc = new QueryCondition();
			qc.add("guid", complexType.getFId());
			ACCVO accVO = (ACCVO)accDao.findObject(qc, conn);
			if(accVO == null) {
				insertACC(complexType, bodPath);
				accVO = (ACCVO)accDao.findObject(qc, conn);
			} 
			roleOfAccId = accVO.getACCID();

			String den = propertyTerm + ". " + Utility.spaceSeparator(Utility.first(accVO.getDEN()));
			int state = 3;	
			String module = bodPath.substring(bodPath.indexOf("Model"));
			module= module.replace("\\", "/");
			
			ASCCPVO accpVO = new ASCCPVO();
			accpVO.setASCCPGUID(asccpGuid);
			accpVO.setPropertyTerm(propertyTerm);
			accpVO.setDefinition(definition);
			accpVO.setRoleOfACCID(roleOfAccId);
			accpVO.setDEN(den);
			accpVO.setState(state);
			accpVO.setModule(module);
			accpVO.setCreatedByUserId(1);
			accpVO.setLastUpdatedByUserId(1);
			accpVO.setIs_deprecated(false);
			accpVO.setRevisionAction(true);
			accpVO.setNamespaceId(1); //tmp
			accpVO.setReleaseId(1);//tmp
			asccpDao.insertObject(accpVO);

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

		QueryCondition qc = new QueryCondition();
		qc.add("guid", bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
		ACCVO accVO = (ACCVO)accDao.findObject(qc, conn);
		int roleOfAccId = accVO.getACCID();

		String den = propertyTerm + ". " + Utility.first(accVO.getDEN());
		int state = 3;
		String module = bodPath.substring(bodPath.indexOf("Model"));
		module= module.replace("\\", "/");
		
		ASCCPVO accpVO = new ASCCPVO();
		accpVO.setASCCPGUID(asccpGuid);
		accpVO.setPropertyTerm(propertyTerm);
		accpVO.setDefinition(definition);
		accpVO.setRoleOfACCID(roleOfAccId);
		accpVO.setDEN(den);
		accpVO.setState(state);
		accpVO.setModule(module);
		accpVO.setCreatedByUserId(1);
		accpVO.setLastUpdatedByUserId(1);
		accpVO.setIs_deprecated(false);
		accpVO.setRevisionAction(true);
		accpVO.setNamespaceId(1); //tmp
		accpVO.setReleaseId(1);//tmp
		asccpDao.insertObject(accpVO);

//		try {
//			Thread.sleep(150);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	private void insertASCC(BODElementVO bodVO, String parentGuid, ASCCPVO asccpVO) throws Exception {

		QueryCondition qc = new QueryCondition();
		qc.add("guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc, conn);
		int assocFromACCId = accVO.getACCID();
		if(assocFromACCId == 71687)
			System.out.println("bod ref ="+bodVO.getRef()+"   bod id = "+bodVO.getId());
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId());
		//qc1.add("assco_to_asccp_id", asccpVO.getASCCPID());
		//qc1.add("from_acc_id", assocFromACCId);

		if(asccDao.findObject(qc1, conn) == null) {
		
			String asccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
			int cardinalityMin = bodVO.getMinOccur();
			int cardinalityMax = bodVO.getMaxOccur();
			int sequenceKey = bodVO.getOrder();
			int assocToASCCPId =  asccpVO.getASCCPID();
			String den = Utility.first(accVO.getDEN()) + ". " + asccpVO.getDEN();
	
			String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());
	
			ASCCVO asscVO = new ASCCVO();
			asscVO.setASCCGUID(asccGuid);
			asscVO.setCardinalityMin(cardinalityMin);
			asscVO.setCardinalityMax(cardinalityMax);
			asscVO.setSequencingKey(sequenceKey);
			asscVO.setAssocFromACCID(assocFromACCId);
			asscVO.setAssocToASCCPID(assocToASCCPId);
			asscVO.setDEN(den);
			asscVO.setDefinition(definition);
			asscVO.setIs_deprecated(false);
			asscVO.setRevisionAction(true);
			asscVO.setReleaseId(1);//tmp
			asccDao.insertObject(asscVO);
			
//			
//			
//			// TODO think about this
//			if(bodVO.getGroupId() != null) {
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("guid", bodVO.getGroupRef());
//				if(asccDao.findObject(qc2) == null) {
//					ASCCVO asscVO1 = new ASCCVO();
//					asscVO1.setASCCGUID(bodVO.getGroupRef());
//					asscVO1.setCardinalityMin(1);
//					asscVO1.setCardinalityMax(1);
//					asscVO1.setSequencingKey(sequenceKey); // TODO check this
//					
//					ACCVO accVO1 = getACC(bodVO.getGroupId());
//					ASCCPVO asccVO1 = getACC(bodVO.getGroupId());
//					if(accVO1 != null) {
//						assocFromACCId = accVO1.getACCID();
//						assocToASCCPId
//					} else {
//						assocFromACCId = insertACCWithGroup();
//					}
//					
//					if(bodVO.getGroupParent() == null) {
//						ACCVO accVO1 = getACC(bodVO.getGroupId());
//						if(accVO1 != null) {
//							assocFromACCId = accVO1.getACCID();
//						} else {
//							assocFromACCId = insertACCWithGroup();
//						}
//					}
//					
//					asscVO1.setAssocFromACCID(assocFromACCId);
//					asscVO1.setAssocToASCCPID(assocToASCCPId);
//					asscVO1.setDEN(den);
//					asscVO1.setDefinition(definition);
//			
//					asccDao.insertObject(asscVO);
//				}
//				
//			}
		}
	}

	private void modifySequeceKeyforGroup() throws SRTDAOException{
		//modify sequenceKey for group
		QueryCondition qc = new QueryCondition();
		qc.add("Definition", "Group");
		ArrayList<SRTObject> groupobjects = asccDao.findObjects(qc, conn); 
		ArrayList<SRTObject> checked_groupobjects = new ArrayList<SRTObject>();
		for(SRTObject asccVO : groupobjects) {
			int ElementsInGroup = 0;
			QueryCondition qc2 = new QueryCondition();
			qc2.add("From_ACC_ID", ((ASCCVO)asccVO).getAssocFromACCID());
			ArrayList<SRTObject> asccObjects = asccDao.findObjects(qc2, conn); 
			for(SRTObject asccObject : asccObjects) {
				ASCCVO ascc = (ASCCVO)asccObject;
				QueryCondition qc3 = new QueryCondition();
				qc3.add("ASCCP_ID", ascc.getAssocToASCCPID());
				ASCCPVO asccp = (ASCCPVO) asccpDao.findObject(qc3, conn);
				if(asccp.getDefinition() != null && asccp.getDefinition().equalsIgnoreCase("Group") && ((ASCCVO)asccVO).getSequencingKey() > ((ASCCVO)asccObject).getSequencingKey()){
					String groupname = asccp.getPropertyTerm().replaceAll(" ", "");
					QueryCondition qc4 = new QueryCondition();
					qc4.addLikeClause("DEN", groupname+"%");
					ArrayList<SRTObject> asccObjectsinGroup = asccDao.findObjects(qc4, conn);
					ArrayList<SRTObject> bccObjectsinGroup = bccDao.findObjects(qc4, conn);
					ElementsInGroup += (asccObjectsinGroup.size()+bccObjectsinGroup.size());
					if((asccObjectsinGroup.size()+bccObjectsinGroup.size()) > 0){
						ElementsInGroup--;
					}
				}
			}
			int new_seq = ((ASCCVO)asccVO).getSequencingKey() - ElementsInGroup;
			((ASCCVO)asccVO).setSequencingKey(new_seq);
			asccDao.updateObject(asccVO);
		}
	}
	
	private void modifySequeceKeyforGroup_temp() throws SRTDAOException{
		//modify sequenceKey for group
		System.out.println("Modifying sequence key for groups temporarily");
		QueryCondition qc = new QueryCondition();
		qc.addLikeClause("DEN", "%Actual Resource Group%");
		ArrayList<SRTObject> groupobjects = asccDao.findObjects(qc, conn); 
		for(SRTObject asccVO : groupobjects) {
			QueryCondition qc2 = new QueryCondition();
			qc2.add("From_ACC_ID", ((ASCCVO)asccVO).getAssocFromACCID());
			ArrayList<SRTObject> asccObjects = asccDao.findObjects(qc2, conn); 
			ArrayList<SRTObject> bccObjects = bccDao.findObjects(qc2, conn);
			for(int i = 0 ; i < asccObjects.size(); i++) {
				ASCCVO ascc = (ASCCVO)asccObjects.get(i);
				if(ascc.getDEN()!= null && ascc.getDEN().contains("Actual Resource Group")) {
					if(i+1 < asccObjects.size()) {
						ASCCVO ascc2 = (ASCCVO)asccObjects.get(i+1);
						if(ascc2.getDEN()!= null && ascc2.getDEN().contains("Free Form Text Group")){
							ascc2.setSequencingKey(ascc.getSequencingKey()+1);
							asccDao.updateObject(ascc2);
							for(int j = 0 ; j < asccObjects.size(); j ++) {
								ASCCVO changed_ascc = (ASCCVO)asccObjects.get(j);
								if(changed_ascc.getSequencingKey() >= ascc2.getSequencingKey() && changed_ascc.getASCCID() != ascc2.getASCCID()){
									changed_ascc.setSequencingKey(changed_ascc.getSequencingKey()+1);
									asccDao.updateObject(changed_ascc);
								}
							}
							for(int j = 0 ; j < bccObjects.size(); j ++) {
								BCCVO changed_bcc = (BCCVO)bccObjects.get(j);
								if(changed_bcc.getSequencingKey() >= ascc2.getSequencingKey()){
									changed_bcc.setSequencingKey(changed_bcc.getSequencingKey()+1);
									bccDao.updateObject(changed_bcc);
								}
							}
							break;
						}
					}
				}
			}
		}
	}
	
	private ACCVO getACC(String guid) throws SRTDAOException {
		QueryCondition qc = new QueryCondition();
		qc.add("guid", guid);
		return (ACCVO)accDao.findObject(qc, conn);
	}
	
	private ASCCPVO getASCCP(String guid) throws SRTDAOException {
		QueryCondition qc = new QueryCondition();
		qc.add("guid", guid);
		return (ASCCPVO)asccpDao.findObject(qc, conn);
	}
	
	private ASCCVO getASCC(String guid) throws SRTDAOException {
		QueryCondition qc = new QueryCondition();
		qc.add("guid", guid);
		return (ASCCVO)asccDao.findObject(qc, conn);
	}
	
	private void insertBCC(BODElementVO bodVO, String parentGuid, BCCPVO bccpVO) throws Exception {
		
		if(bccpVO == null)
			return;
		
		//String bccGuid = bodVO.getId();
		String bccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
		int assocToBCCPID = bccpVO.getBCCPID();
		
		QueryCondition qc = new QueryCondition();
		qc.add("guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc, conn);
		int assocFromACCId = accVO.getACCID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", bccGuid);
		qc1.add("to_bccp_id", assocToBCCPID);
		qc1.add("from_acc_id", assocFromACCId);
		if(bccDao.findObject(qc1, conn) == null) {
		
			int cardinalityMin = bodVO.getMinOccur();
			int cardinalityMax = bodVO.getMaxOccur();
			int sequenceKey = bodVO.getOrder();
	
			int entityType = 1; 
			String den = Utility.first(accVO.getDEN()) + ". " + bccpVO.getDEN();
	
			BCCVO aBCCVO = new BCCVO();
			aBCCVO.setBCCGUID(bccGuid);
			aBCCVO.setCardinalityMin(cardinalityMin);
			aBCCVO.setCardinalityMax(cardinalityMax);
			aBCCVO.setAssocToBCCPID(assocToBCCPID);
			aBCCVO.setAssocFromACCID(assocFromACCId);
			aBCCVO.setSequencingKey(sequenceKey);
			aBCCVO.setEntityType(entityType);
			aBCCVO.setDEN(den);
			aBCCVO.setIs_deprecated(false);
			aBCCVO.setRevisionAction(true);
			aBCCVO.setReleaseId(1);//tmp
			bccDao.insertObject(aBCCVO);
		}

	}

	private void insertBCCWithAttr(XSAttributeDecl xad, XSComplexTypeDecl complexType) throws Exception {//check 
		String bccGuid = xad.getFId();
		
		int cardinalityMin = (xad.getFUse() == null) ? 0 : (xad.getFUse().equals("optional") || xad.getFUse().equals("prohibited")) ? 0 : (xad.getFUse().equals("required")) ? 1 : 0;
		int cardinalityMax = (xad.getFUse() == null) ? 1 : (xad.getFUse().equals("optional") || xad.getFUse().equals("required")) ? 1 : (xad.getFUse().equals("prohibited")) ? 0 : 0;
		int sequenceKey = 0;

		XSSimpleTypeDecl xtd = (XSSimpleTypeDecl)xad.getTypeDefinition();
		
		int assocToBCCPID;
		QueryCondition qc = new QueryCondition();
		qc.add("property_term", Utility.spaceSeparator(xad.getName()).replace("ID", "Identifier"));
		BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc, conn);
		if(bccpVO == null) {
			bccpVO = insertBCCP(xad.getName(), xtd.getFId());
		}
		if(bccpVO == null){ 
			System.out.println("BCCP creation is failed in BCC attriute creation, Note -> Name : "+xad.getName()+"   guid : "+xtd.getFId());
			return;//check
		}
		assocToBCCPID =  bccpVO.getBCCPID();
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", bccGuid);
		qc2.add("to_bccp_id", assocToBCCPID);
		if(!xad.getName().equals("responseCode") && bccDao.findObject(qc2, conn) == null) {

			String parentGuid = complexType.getFId();
			QueryCondition qc1 = new QueryCondition();
			qc1.add("guid", parentGuid);
			ACCVO accVO = (ACCVO)accDao.findObject(qc1, conn);
			int assocFromACCId = accVO.getACCID();
	
			int entityType = 0; 
			String den = Utility.first(accVO.getDEN()) + ". " + bccpVO.getDEN();
	
			BCCVO aBCCVO = new BCCVO();
			aBCCVO.setBCCGUID(bccGuid);
			aBCCVO.setCardinalityMin(cardinalityMin);
			aBCCVO.setCardinalityMax(cardinalityMax);
			aBCCVO.setAssocToBCCPID(assocToBCCPID);
			aBCCVO.setAssocFromACCID(assocFromACCId);
			aBCCVO.setSequencingKey(sequenceKey);
			aBCCVO.setEntityType(entityType);
			aBCCVO.setDEN(den);
			aBCCVO.setIs_deprecated(false);
			aBCCVO.setRevisionAction(true);
			aBCCVO.setReleaseId(1);//tmp
			bccDao.insertObject(aBCCVO);
		}
	}

	private BCCPVO insertBCCP(String name, String id) throws Exception {
		String bccpGuid = Utility.generateGUID();
		String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

		//System.out.println("### BCCP: " + name + " " + id);
		if(id == null) {
			System.out.println("!!!! id is null where name is  = " + name);
			//id = "oagis-id-89be97039be04d6f9cfda107d75926b4"; // TODO check why dt is null and change this line
			return null;
		}
		QueryCondition qc = new QueryCondition();
		qc.add("guid", id);
		qc.add("type", 1);
		DTVO dtVO = (DTVO)dtDao.findObject(qc);
		if(dtVO == null) {
			System.out.println("!!!! DT is null where name is  = " + name+" and id is = " + id);
			return null;
			//QueryCondition qc1 = new QueryCondition();
			//qc1.add("guid", "oagis-id-89be97039be04d6f9cfda107d75926b4"); // TODO check why dt is null and change this line
			//dtVO = (DTVO)dtDao.findObject(qc1);
		}

		int bdtId = dtVO.getDTID();
		String representationTerm = dtVO.getDataTypeTerm();
		String den = Utility.firstToUpperCase(propertyTerm) + ". " + representationTerm;

		BCCPVO bccpVO = new BCCPVO();
		bccpVO.setBCCPGUID(bccpGuid);
		bccpVO.setPropertyTerm(propertyTerm);
		bccpVO.setBDTID(bdtId);
		bccpVO.setRepresentationTerm(representationTerm);
		bccpVO.setDEN(den);
		bccpVO.setCreatedByUserId(1);
		bccpVO.setLastUpdatedByUserId(1);
		bccpVO.setIs_deprecated(false);
		bccpVO.setRevisionAction(true);
		bccpVO.setReleaseId(1);//tmp
		bccpDao.insertObject(bccpVO);

		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", bccpGuid);
		return (BCCPVO)bccpDao.findObject(qc1, conn);
	}
	 
	
	private void insertForGroup(BODElementVO bodVO, String fullFilePath, String complexTypeId, int cnt) throws SRTDAOException {
		//System.out.println("------------------------" + bodVO.getId() + " | " + bodVO.getName() + " | " + bodVO.getGroupId() + " | " + bodVO.getGroupName());
		
		//System.out.println("### type: " + bodVO.getOrder() + " | name: " + bodVO.getName() + " | id: " + bodVO.getId() + " | ref: " + bodVO.getRef() + " | group?: " + bodVO.isGroup() + " | groupid: " + bodVO.getGroupId() + " | groupref: " + bodVO.getGroupRef() + " | grouparent: " + bodVO.getGroupParent());
		
		
		String objectClassName = Utility.spaceSeparator(bodVO.getGroupName().substring(0, (bodVO.getGroupName().indexOf("Type") > 0) ? bodVO.getGroupName().indexOf("Type") : bodVO.getGroupName().length()));
		String den = objectClassName + ". Details";
		int oagisComponentType = 1;
		if(Utility.first(den).endsWith("Base"))
			oagisComponentType = 0;
		else if(Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
			oagisComponentType = 2;
		else if(Utility.first(den).endsWith("Group") || objectClassName.equalsIgnoreCase("Common Time Reporting"))
			oagisComponentType = 3;
		String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
		module= module.replace("\\", "/");
		insertACCForGroup(bodVO, objectClassName, den, oagisComponentType, module);
		
		QueryCondition qc = new QueryCondition();
		qc.add("guid", bodVO.getGroupId());
		int groupAccId= ((ACCVO)accDao.findObject(qc, conn)).getACCID();
		insertASCCPForGroup(bodVO, groupAccId, den, module);
		
		inserASCCForGroup(bodVO, complexTypeId, cnt);
	}
	
	private void insertACCForGroup(BODElementVO bodVO, String objectClassName, String accDen, int oagisComponentType, String module) throws SRTDAOException {
		if(getACC(bodVO.getGroupId()) == null) {
			ACCVO aACCVO = new ACCVO();
			aACCVO.setACCGUID(bodVO.getGroupId());
			aACCVO.setObjectClassTerm(objectClassName);
			aACCVO.setDEN(accDen);
			aACCVO.setDefinition("Group");
			aACCVO.setOAGISComponentType(oagisComponentType);
			aACCVO.setBasedACCID(-1);
			aACCVO.setCreatedByUserId(1);
			aACCVO.setLastUpdatedByUserId(1);
			aACCVO.setState(3);
			aACCVO.setModule(module);
			aACCVO.setIs_deprecated(false);
			aACCVO.setRevisionAction(true);
			aACCVO.setNamespaceId(1); //tmp
			aACCVO.setReleaseId(1);//tmp
			accDao.insertObject(aACCVO);
		}
	}
	
	private void insertASCCPForGroup(BODElementVO bodVO, int groupAccId, String accDen, String module) throws SRTDAOException {
		if(getASCCP(bodVO.getGroupId()) == null) {
			String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());
	
			ASCCPVO asccpVO = new ASCCPVO();
			asccpVO.setASCCPGUID(bodVO.getGroupId());
			asccpVO.setPropertyTerm(propertyTerm);
			asccpVO.setDefinition("Group");
			
//			int roleOfACCID = -1;
//			if (bodVO.getGroupParent() != null) {
//				roleOfACCID = getACC(bodVO.getGroupParent()).getACCID();
//			} else {
//				roleOfACCID = getACC(complexTypeId).getACCID(); 
//			}
			
			asccpVO.setRoleOfACCID(groupAccId);
			asccpVO.setDEN(Utility.spaceSeparator(propertyTerm + ". " + Utility.first(accDen)));
			asccpVO.setState(3);
			asccpVO.setModule(module);
			asccpVO.setCreatedByUserId(1);
			asccpVO.setLastUpdatedByUserId(1);
			asccpVO.setIs_deprecated(false);
			asccpVO.setRevisionAction(true);
			asccpVO.setNamespaceId(1); //tmp
			asccpVO.setReleaseId(1);//tmp
			asccpDao.insertObject(asccpVO);
		}
	}
	
	private void inserASCCForGroup(BODElementVO bodVO, String complexTypeId, int cnt) throws SRTDAOException {
		//if(getASCC(bodVO.getGroupRef()) == null) {
			ACCVO accVO = getACC(bodVO.getGroupId());
			ASCCPVO asccpVO1 = getASCCP(bodVO.getGroupId());
			int assocToASCCPId =  asccpVO1.getASCCPID();
				
			int accId = 0;
			if(bodVO.getGroupParent() == null) {
				accId = getACC(complexTypeId).getACCID();
			} else {
				accId = getACC(bodVO.getGroupParent()).getACCID();
			}

			QueryCondition qc = new QueryCondition();
			qc.add("acc_id", accId);
			ACCVO accVO2 = (ACCVO)accDao.findObject(qc, conn);

			ASCCVO asscVO = new ASCCVO();
			asscVO.setASCCGUID(bodVO.getGroupRef());
			asscVO.setCardinalityMin(1);
			asscVO.setCardinalityMax(1);
			asscVO.setAssocFromACCID(accId);
			asscVO.setSequencingKey(cnt); // TODO check this
			asscVO.setAssocToASCCPID(assocToASCCPId);

			asscVO.setDEN(Utility.first(accVO2.getDEN()) + ". " + asccpVO1.getDEN());
			asscVO.setDefinition("Group");
			asscVO.setIs_deprecated(false);
			asscVO.setRevisionAction(true);
			asscVO.setReleaseId(1);//tmp
			asccDao.insertObject(asscVO);
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
		if(base != null && !base.equals("anyType") && complexType.getBaseType().getTypeCategory() != 16) {
			XSComplexTypeDecl baseType = bodSchemaHandler.getComplexTypeDefinition(base);

			QueryCondition qc = new QueryCondition();
			qc.add("guid", baseType.getFId());
			ACCVO accVO = (ACCVO)accDao.findObject(qc, conn);
			if(accVO == null) {
				elements = insertACC(baseType, fullFilePath);
				accVO = (ACCVO)accDao.findObject(qc, conn);
				basedAccId = accVO.getACCID();
			} else {
				basedAccId = accVO.getACCID();
				XSParticle particle = bodSchemaHandler.getComplexTypeDefinition(base).getParticle();
				if(particle != null) {
					ArrayList<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
					for(BODElementVO bodVO : al) {
						elements.add(bodVO.getName());
					}
				}
			}
		}

		int oagisComponentType = 1;
		if(Utility.first(den).endsWith("Base"))
			oagisComponentType = 0;
		else if(Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
			oagisComponentType = 2;
		else if(Utility.first(den).endsWith("Group"))
			oagisComponentType = 3;

		int state = 3;
		String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
		module= module.replace("\\", "/");
		
		ACCVO aACCVO = new ACCVO();
		aACCVO.setACCGUID(accGuid);
		aACCVO.setObjectClassTerm(objectClassName);
		aACCVO.setDEN(den);
		aACCVO.setDefinition(definition);
		aACCVO.setBasedACCID(basedAccId);
		aACCVO.setOAGISComponentType(oagisComponentType);
		aACCVO.setCreatedByUserId(1);
		aACCVO.setLastUpdatedByUserId(1);
		aACCVO.setState(state);
		aACCVO.setModule(module);
		aACCVO.setIs_deprecated(false);
		aACCVO.setRevisionAction(true);
		aACCVO.setNamespaceId(1); //tmp
		aACCVO.setReleaseId(1);//tmp
		accDao.insertObject(aACCVO);
		
		XSParticle particle = complexType.getParticle();
		if(particle != null) {
			ArrayList<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
			String tempGroupId = "";
			int cnt = 1;
			for(BODElementVO bodVO : al) {
				if(!elements.contains(bodVO.getName())) {
					elements.add(bodVO.getName());
					
					if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) { 
						QueryCondition qc2 = new QueryCondition();
						qc2.add("guid", bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
						ACCVO accVO = (ACCVO)accDao.findObject(qc2, conn);
						if(accVO == null) {
							insertACC(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()), fullFilePath);
						} 
					}
					
					
					// insert ACC with group id, if group parent == null, then complexType is based ACC, else parent is based ACC
					// insert ASCCP with group id, from ACC group
					// insert ASCC with group ref, from ACC is parent or CT, to ASCCP is group id 
					
					if(bodVO.getGroupId() != null && !tempGroupId.equals(bodVO.getGroupId())) {
						//System.out.println("--- eleID: " + bodVO.getElement().getFId() + " | name: " + bodVO.getName() + " | id: " + bodVO.getId() + " | ref: " + bodVO.getRef() + " | group?: " + bodVO.isGroup() + " | groupid: " + bodVO.getGroupId() + " | groupref: " + bodVO.getGroupRef() + " | grouparent: " + bodVO.getGroupParent());
						
						tempGroupId = bodVO.getGroupId();
						insertForGroup(bodVO, fullFilePath, complexType.getFId(), cnt);
					}
					
					QueryCondition qc = new QueryCondition();
					//System.out.println("#######################XX bodVO.getName() " + bodVO.getName()); 
					qc.add("guid", bodVO.getId());
					ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc, conn);
	
					QueryCondition qc1 = new QueryCondition();
					qc1.add("guid", bodVO.getId());
					BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc1, conn);
					
					if(asccpVO != null) {
						//System.out.println("####################### match to ascc - " + bodVO.getName()); 
						insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO);
					} else if(bccpVO != null) {
						//System.out.println("####################### match to bccp - " + bodVO.getName());
						insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
					} else {
						//System.out.println("####################### no match case - " + bodVO.getName());
						//if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
							//insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getElement()));
						if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
							if(bodVO.getGroupId() == null) {
								insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()));
							} else {
								
								String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());
								den = propertyTerm + ". " + Utility.first(den);
								insertASCCPUnderGroup(bodVO);
							}
							
							QueryCondition qc3 = new QueryCondition();
							qc3.add("guid", bodVO.getId());
							ASCCPVO asccpVO1 = (ASCCPVO)asccpDao.findObject(qc3, conn);
							insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO1);
						}
						else if(bodSchemaHandler.isComplexWithSimpleContent(bodVO.getTypeName())){
							bccpVO = insertBCCP(bodVO.getName(), bodVO.getId());
							insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
						}
					}
//					try {
//						Thread.sleep(200);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				cnt++;
				}
			}
		}
		
		XSObjectList xol = complexType.getAttributeUses();
		for( int i = 0; i < xol.getLength(); i++) {
			XSAttributeUseImpl xui = (XSAttributeUseImpl)xol.get(i);
			XSAttributeDecl xad = (XSAttributeDecl)xui.getAttrDeclaration();
			//if(!xad.getName().equals("releaseID") && !xad.getName().equals("versionID") && !xad.getName().equals("systemEnvironmentCode")) {
				//System.out.println("####################### attribute: " + complexType.getName() + " | " + xad.getName());
				insertBCCWithAttr(xad, complexType);
			//}
		}
		
		return elements;
	}

	private File[] getBODs(File f) {
		return f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*.xsd");
			}
		});
	}
	
	private static Connection conn = null;
	
	public void run() throws Exception{
		System.out.println("### 1.8 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		P_1_8_PopulateAccAsccpBccAscc q = new P_1_8_PopulateAccAsccpBccAscc();
		q.populate();
		
		tx.close();
		conn.close();
		
		System.out.println("### 1.8 End");
	}

	public static void main(String[] args) throws Exception{
		Utility.dbSetup();

		P_1_8_PopulateAccAsccpBccAscc q = new P_1_8_PopulateAccAsccpBccAscc();
		q.run();
	}
}