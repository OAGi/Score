package org.oagi.srt.persistent.populate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
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
import org.oagi.srt.persistence.dto.BDT_Primitive_RestrictionVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_PrimitiveVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_Primitive_Expression_Type_MapVO;
import org.oagi.srt.persistence.dto.Code_ListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DT_SCVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Yunsu Lee
* @version 1.0
*
*/

public class PopulateAccAsccpBccAscc {
	
	DAOFactory df;
	SRTDAO accDao;
	SRTDAO asccpDao;
	SRTDAO bccpDao;
	SRTDAO bccDao;
	SRTDAO asccDao;
	SRTDAO dtDao;
	
	File f1 = new File("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/BODs/");
	File f2 = new File("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/BODs/");
	
	public PopulateAccAsccpBccAscc() throws SRTDAOException {
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
			insertASCCP(file, true);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (File file : listOfF2) {
			insertASCCP(file, true);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void insertASCC(Element eleRef, XPathHandler xh, ASCCPVO asccpVO) throws Exception {
		String asccGuid = eleRef.getAttribute("id");
		int cardinalityMin = (eleRef.getAttribute("minOccurs") == null) ? 1 : Integer.parseInt(eleRef.getAttribute("minOccurs"));
		int cardinalityMax = (eleRef.getAttribute("maxOccurs") == null) ? 1 : (eleRef.getAttribute("maxOccurs").equalsIgnoreCase("unbounded")) ? -1 : Integer.parseInt(eleRef.getAttribute("maxOccurs"));
		int sequenceKey = xh.getNodeList("//xsd:element[@id = '" + asccGuid + "']/preceding-sibling::*").getLength() + 1;
		
		Element parent = (Element)xh.getNode("//xsd:element[@id = '" + asccGuid + "']/ancestor::xsd:complexType");
		String parentGuid = parent.getAttribute("id");
		QueryCondition qc = new QueryCondition();
		qc.add("acc_guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc);
		accVO = (ACCVO)accDao.findObject(qc);
		int assocFromACCId = accVO.getACCID();
		
		//QueryCondition qc1 = new QueryCondition();
		//qc1.addLikeClause("den", "%" + eleRef.getAttribute("ref") + "%");
		//ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc);
		int assocToASCCPId =  asccpVO.getASCCPID();
		String den = Utility.first(accVO.getDEN()) + ". " + asccpVO.getDEN();
		
		Element def = (Element)xh.getNode("//xsd:element[@id = '" + asccGuid + "']/xsd:annotation/xsd:document");
		String definition = (def != null) ? def.getTextContent() : null;
		
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
	
	private void insertBCC(Element eleRef, XPathHandler xh, BCCPVO bccpVO) throws Exception {
		String bccGuid = eleRef.getAttribute("id");
		int cardinalityMin = (eleRef.getAttribute("minOccurs") == null) ? 1 : Integer.parseInt(eleRef.getAttribute("minOccurs")); //TODO
		int cardinalityMax = (eleRef.getAttribute("maxOccurs") == null) ? 1 : (eleRef.getAttribute("maxOccurs").equalsIgnoreCase("unbounded")) ? -1 : Integer.parseInt(eleRef.getAttribute("maxOccurs")); //TODO
		int sequenceKey = xh.getNodeList("//xsd:element[@id = '" + bccGuid + "']/preceding-sibling::*").getLength() + 1;
		
		int assocToBCCPID = bccpVO.getBCCPID();
		
		Element parent = (Element)xh.getNode("//xsd:element[@id = '" + bccGuid + "']/ancestor::xsd:complexType");
		String parentGuid = parent.getAttribute("id");
		QueryCondition qc = new QueryCondition();
		qc.add("acc_guid", parentGuid);
		ACCVO accVO = (ACCVO)accDao.findObject(qc);
		accVO = (ACCVO)accDao.findObject(qc);
		int assocFromACCId = accVO.getACCID();
		
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
	
	private void insertBCCWithAttr(Element attr, XPathHandler xh) throws Exception {
		String bccGuid = attr.getAttribute("id");
		int cardinalityMin = (attr.getAttribute("use") == null) ? 0 : (attr.getAttribute("use").equals("optional") || attr.getAttribute("use").equals("prohibited")) ? 0 : (attr.getAttribute("use").equals("required")) ? 1 : 0;
		int cardinalityMax = (attr.getAttribute("use") == null) ? 1 : (attr.getAttribute("use").equals("optional") || attr.getAttribute("use").equals("required")) ? 1 : (attr.getAttribute("use").equals("prohibited")) ? 0 : 0;
		int sequenceKey = xh.getNodeList("//xsd:element[@id = '" + bccGuid + "']/preceding-sibling::*").getLength() + 1;
		
		int assocToBCCPID;
		QueryCondition qc = new QueryCondition();
		qc.addLikeClause("den", "%" + attr.getAttribute("ref") + "%");
		BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc);
		if(bccpVO == null) {
			bccpVO = insertBCCP(attr.getAttribute("name"), attr.getAttribute("id"));
		}
		assocToBCCPID =  bccpVO.getBCCPID();
		
		Element parent = (Element)xh.getNode("//xsd:attribute[@id = '" + bccGuid + "']/ancestor::xsd:complexType");
		String parentGuid = parent.getAttribute("id");
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
	
	private BCCPVO insertBCCP(String name, String id) throws Exception {
		String bccpGuid = Utility.generateGUID();
		String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");
		
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", id);
		qc.add("dt_type", 1);
		DTVO dtVO = (DTVO)dtDao.findObject(qc);
		
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
		return (BCCPVO)bccpDao.findObject(qc);
	}
	
	private void insertASCCP(File file, boolean first) throws Exception {
		
		XPathHandler xh = new XPathHandler(file.getAbsolutePath());
		NodeList eleList = xh.getNodeList("//xsd:element");
		for(int i = 0; i < eleList.getLength(); i++) {
			Element ele = (Element)eleList.item(i);
			String name = ele.getAttribute("name");
			String type = ele.getAttribute("type");
			
			Element def = (Element)xh.getNode("//xsd:element[@id = '" + ele.getAttribute("id") + "']/xsd:annotation/xsd:documentation");
			
			System.out.println("### asccp name: " + name);
			
			Element complexType = (Element)xh.getNode("//xsd:complexType[@name = '" + type + "' and count(xsd:simpleContent) = 0] ");
			if(complexType != null) {
				String asccpGuid = ele.getAttribute("id");
				String propertyTerm = Utility.spaceSeparator(name);
				String definition = def.getTextContent();
				
				int roleOfAccId;
				QueryCondition qc = new QueryCondition();
				qc.add("acc_guid", complexType.getAttribute("id"));
				ACCVO accVO = (ACCVO)accDao.findObject(qc);
				if(accVO == null) {
					insertACC(type, file.getAbsolutePath());
					accVO = (ACCVO)accDao.findObject(qc);
				} 
				roleOfAccId = accVO.getACCID();
				
				String den = propertyTerm + ". " + Utility.first(accVO.getDEN());
				int state = 4;
				String module = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1, file.getAbsolutePath().lastIndexOf("."));
				
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
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(first) {
			NodeList eleRefList = xh.getNodeList("//xsd:element[count(@ref) = 1]");
			for(int i = 0; i < eleRefList.getLength(); i++) {
				Element eleRef = (Element)eleRefList.item(i);
				QueryCondition qc = new QueryCondition();
				qc.add("asccp_guid", eleRef.getAttribute("id"));
				ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc);
				
				QueryCondition qc1 = new QueryCondition();
				qc1.add("bccp_guid", eleRef.getAttribute("id"));
				BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc1);
				if(asccpVO != null) {
					System.out.println("####################### match to ascc");
					insertASCC(eleRef, xh, asccpVO);
				} else if(bccpVO != null) {
					System.out.println("####################### match to bccp");
					insertBCC(eleRef, xh, bccpVO);
				} else {
					// TODO
					System.out.println("####################### no match case");
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			NodeList attrList = xh.getNodeList("//xsd:attribute");
			for(int i = 0; i < attrList.getLength(); i++) {
				insertBCCWithAttr((Element)attrList.item(i), xh);
			}
			
		}
		
	}
	
	private void insertACC(String type, String fullFilePath) throws Exception {
		System.out.println("### acc type: " + type);
		
		XPathHandler xh = new XPathHandler(fullFilePath);
		Element complexType = (Element)xh.getNode("//xsd:complexType[@name = '" + type + "']");
		Element cDef = (Element)xh.getNode("//xsd:complexType[@name = '" + type + "']/xsd:annotation/xsd:documentation");
		Element ext = (Element)xh.getNode("//xsd:complexType[@name = '" + type + "']/xsd:extension");
		
		String accGuid = complexType.getAttribute("id");
		String objectClassName = Utility.spaceSeparator(complexType.getAttribute("name").substring(0, complexType.getAttribute("name").indexOf("Type")));
		String den = objectClassName + ". Details";
		String definition = (cDef != null) ? cDef.getTextContent() : "";
		
		int basedAccId = -1;
		if(ext != null) {
			String base = ext.getAttribute("base");
			Utility.spaceSeparator(base.substring(0, base.indexOf("Type")));
			
			QueryCondition qc = new QueryCondition();
			qc.add("den", Utility.spaceSeparator(base.substring(0, base.indexOf("Type"))) + ". Details");
			ACCVO accVO = (ACCVO)accDao.findObject(qc);
			if(accVO == null) {
				insertACC(base, fullFilePath);
			} else {
				basedAccId = accVO.getACCID();
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
		String module = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1, fullFilePath.lastIndexOf("."));
		
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
		
		PopulateAccAsccpBccAscc q = new PopulateAccAsccpBccAscc();
		q.populate();
	}
}
