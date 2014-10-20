package org.oagi.srt.persistent.populate;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.BODElementVO;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.DTVO;

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

		File[] listOfF1 = getBODs(f1);
		File[] listOfF2 = getBODs(f2);

		for (File file : listOfF1) {
			//if(file.getName().equals("AcknowledgeField.xsd"))
				insertASCCP(file);
		}

		for (File file : listOfF2) {
			//if(file.getName().equals("AcknowledgeField.xsd"))
				insertASCCP(file);
		}
	} 
	
	private void insertASCCP(File file) throws Exception {
		bodPath = file.getAbsolutePath();
		bodSchemaHandler = new BODSchemaHandler(bodPath);
		XSElementDecl element = bodSchemaHandler.getGlobalElementDeclaration();
		XSComplexTypeDecl complexType = bodSchemaHandler.getComplexTypeDefinition(element);
		if(bodSchemaHandler.isComplexWithoutSimpleContent(complexType.getTypeName())) {
			insertASCCP(element, complexType);
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void insertASCCP(XSElementDecl element, XSComplexTypeDecl complexType) throws Exception {

		String name = element.getName();
		System.out.println("### asccp name: " + name);

		if(complexType != null) {
			String asccpGuid = element.getFId();
			String propertyTerm = Utility.spaceSeparator(name);
			String definition = bodSchemaHandler.getAnnotation(element);

			int roleOfAccId;
			QueryCondition qc = new QueryCondition();
			qc.add("acc_guid", complexType.getFId());
			ACCVO accVO = (ACCVO)accDao.findObject(qc);
			if(accVO == null) {
				insertACC(complexType, bodPath);
				accVO = (ACCVO)accDao.findObject(qc);
			} 
			roleOfAccId = accVO.getACCID();

			String den = propertyTerm + ". " + Utility.first(accVO.getDEN());
			int state = 4;
			String module = bodPath.substring(bodPath.lastIndexOf(File.separator) + 1, bodPath.lastIndexOf("."));

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

			asccpDao.insertObject(accpVO);

		}
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void insertASCC(BODElementVO bodVO, XSComplexTypeDecl complexType, ASCCPVO asccpVO) throws Exception {

		String parentGuid = complexType.getFId();
		QueryCondition qc = new QueryCondition();
		qc.add("acc_guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc);
		accVO = (ACCVO)accDao.findObject(qc);
		int assocFromACCId = accVO.getACCID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("ascc_guid", bodVO.getId());
		qc1.add("assco_to_asccp_id", asccpVO.getASCCPID());
		qc1.add("assoc_from_acc_id", assocFromACCId);
		if(asccDao.findObject(qc1) == null) {
		
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
	
			asccDao.insertObject(asscVO);
		}
	}

	private void insertBCC(BODElementVO bodVO, XSComplexTypeDecl complexType, BCCPVO bccpVO) throws Exception {
		
		String bccGuid = bodVO.getId();
		int assocToBCCPID = bccpVO.getBCCPID();
		
		String parentGuid = complexType.getFId();
		QueryCondition qc = new QueryCondition();
		qc.add("acc_guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc);
		accVO = (ACCVO)accDao.findObject(qc);
		int assocFromACCId = accVO.getACCID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("bcc_guid", bccGuid);
		qc1.add("assoc_to_bccp_id", assocToBCCPID);
		qc1.add("assoc_from_acc_id", assocFromACCId);
		if(bccDao.findObject(qc1) == null) {
		
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
			aBCCVO.setSequencingkey(sequenceKey);
			aBCCVO.setEntityType(entityType);
			aBCCVO.setDEN(den);
	
			bccDao.insertObject(aBCCVO);
		}

	}

	private void insertBCCWithAttr(XSAttributeDecl xad, XSComplexTypeDecl complexType) throws Exception {
		String bccGuid = xad.getFId();
		
		int cardinalityMin = (xad.getFUse() == null) ? 0 : (xad.getFUse().equals("optional") || xad.getFUse().equals("prohibited")) ? 0 : (xad.getFUse().equals("required")) ? 1 : 0;
		int cardinalityMax = (xad.getFUse() == null) ? 1 : (xad.getFUse().equals("optional") || xad.getFUse().equals("required")) ? 1 : (xad.getFUse().equals("prohibited")) ? 0 : 0;
		int sequenceKey = 0;

		XSSimpleTypeDecl xtd = (XSSimpleTypeDecl)xad.getTypeDefinition();
		
		int assocToBCCPID;
		QueryCondition qc = new QueryCondition();
		qc.add("property_term", Utility.spaceSeparator(xad.getName()));
		BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc);
		if(bccpVO == null) {
			bccpVO = insertBCCP(xad.getName(), xtd.getFId());
		}
		assocToBCCPID =  bccpVO.getBCCPID();
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("bcc_guid", bccGuid);
		qc2.add("assoc_to_bccp_id", assocToBCCPID);
		if(!xad.getName().equals("responseCode") && bccDao.findObject(qc2) == null) {

			String parentGuid = complexType.getFId();
			QueryCondition qc1 = new QueryCondition();
			qc1.add("acc_guid", parentGuid);
			ACCVO accVO = (ACCVO)accDao.findObject(qc1);
			int assocFromACCId = accVO.getACCID();
	
			int entityType = 0; 
			String den = Utility.first(accVO.getDEN()) + ". " + bccpVO.getDEN();
	
			BCCVO aBCCVO = new BCCVO();
			aBCCVO.setBCCGUID(bccGuid);
			aBCCVO.setCardinalityMin(cardinalityMin);
			aBCCVO.setCardinalityMax(cardinalityMax);
			aBCCVO.setAssocToBCCPID(assocToBCCPID);
			aBCCVO.setAssocFromACCID(assocFromACCId);
			aBCCVO.setSequencingkey(sequenceKey);
			aBCCVO.setEntityType(entityType);
			aBCCVO.setDEN(den);
	
			bccDao.insertObject(aBCCVO);
		}
	}

	private BCCPVO insertBCCP(String name, String id) throws Exception {
		String bccpGuid = Utility.generateGUID();
		String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

		System.out.println("### BCCP: " + name + " " + id);
		if(id == null) {
			id = "oagis-id-89be97039be04d6f9cfda107d75926b4"; // TODO check why dt is null and change this line
		}
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", id);
		qc.add("dt_type", 1);
		DTVO dtVO = (DTVO)dtDao.findObject(qc);
		if(dtVO == null) {
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_guid", "oagis-id-89be97039be04d6f9cfda107d75926b4"); // TODO check why dt is null and change this line
			dtVO = (DTVO)dtDao.findObject(qc1);
		}

		int bdtId = dtVO.getDTID();
		String representationTerm = dtVO.getDataTypeTerm();
		String den = propertyTerm + ". " + representationTerm;

		BCCPVO bccpVO = new BCCPVO();
		bccpVO.setBCCPGUID(bccpGuid);
		bccpVO.setPropertyTerm(propertyTerm);
		bccpVO.setBDTID(bdtId);
		bccpVO.setRepresentationTerm(representationTerm);
		bccpVO.setDEN(den);
		bccpVO.setCreatedBy(1);
		bccpVO.setLastUpdatedByUserId(1);

		bccpDao.insertObject(bccpVO);

		QueryCondition qc1 = new QueryCondition();
		qc1.add("bccp_guid", bccpGuid);
		return (BCCPVO)bccpDao.findObject(qc1);
	}



	private ArrayList<String> insertACC(XSComplexTypeDecl complexType, String fullFilePath) throws Exception {
		
		ArrayList<String> elements = new ArrayList<String>();
		System.out.println("### acc type: " + complexType.getName());

		String accGuid = complexType.getFId();
		String objectClassName = Utility.spaceSeparator(complexType.getName().substring(0, complexType.getName().indexOf("Type")));
		String den = objectClassName + ". Details";
		String definition = bodSchemaHandler.getAnnotation(complexType);

		int basedAccId = -1;
		String base = complexType.getBaseType().getName();
		System.out.println("### base type: " + base + " - " + complexType.getBaseType().getTypeCategory());
		if(base != null && !base.equals("anyType") && complexType.getBaseType().getTypeCategory() != 16) {
			XSComplexTypeDecl baseType = bodSchemaHandler.getComplexTypeDefinition(base);

			QueryCondition qc = new QueryCondition();
			qc.add("acc_guid", baseType.getFId());
			ACCVO accVO = (ACCVO)accDao.findObject(qc);
			if(accVO == null) {
				elements = insertACC(baseType, fullFilePath);
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

		int state = 4;
		String module = fullFilePath.substring(fullFilePath.lastIndexOf(File.separator) + 1, fullFilePath.lastIndexOf("."));

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

		accDao.insertObject(aACCVO);
		
		XSParticle particle = complexType.getParticle();
		if(particle != null) {
			ArrayList<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
			
			for(BODElementVO bodVO : al) {
				if(!elements.contains(bodVO.getName())) {
					elements.add(bodVO.getName());
					
					if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
						QueryCondition qc2 = new QueryCondition();
						qc2.add("acc_guid", bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
						ACCVO accVO = (ACCVO)accDao.findObject(qc2);
						if(accVO == null) {
							insertACC(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()), fullFilePath);
						} 
					}
					
					QueryCondition qc = new QueryCondition();
					qc.add("asccp_guid", bodVO.getId());
					ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc);
	
					QueryCondition qc1 = new QueryCondition();
					qc1.add("bccp_guid", bodVO.getId());
					BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc1);
					
					if(asccpVO != null) {
						System.out.println("####################### match to ascc - " + bodVO.getName()); // TODO how to differentiate between particles in type and from extension?
						insertASCC(bodVO, complexType, asccpVO);
					} else if(bccpVO != null) {
						System.out.println("####################### match to bccp - " + bodVO.getName());
						insertBCC(bodVO, complexType, bccpVO);
					} else {
						System.out.println("####################### no match case - " + bodVO.getName());
						if(bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
							insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getElement()));
							QueryCondition qc3 = new QueryCondition();
							qc3.add("asccp_guid", bodVO.getId());
							ASCCPVO asccpVO1 = (ASCCPVO)asccpDao.findObject(qc3);
							insertASCC(bodVO, complexType, asccpVO1);
						}
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		XSObjectList xol = complexType.getAttributeUses();
		for( int i = 0; i < xol.getLength(); i++) {
			XSAttributeUseImpl xui = (XSAttributeUseImpl)xol.get(i);
			XSAttributeDecl xad = (XSAttributeDecl)xui.getAttrDeclaration();
			if(!xad.getName().equals("releaseID") && !xad.getName().equals("versionID") && !xad.getName().equals("systemEnvironmentCode")) {
				System.out.println("####################### attribute: " + complexType.getName() + " | " + xad.getName());
				insertBCCWithAttr(xad, complexType);
			}
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

	public static void main(String[] args) throws Exception{
		Utility.dbSetup();

		P_1_8_PopulateAccAsccpBccAscc q = new P_1_8_PopulateAccAsccpBccAscc();
		q.populate();
	}
}
