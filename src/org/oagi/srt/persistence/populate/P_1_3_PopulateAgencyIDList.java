package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.AgencyIDListValueVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaehun Lee
 * @version 1.0
 *
 */

public class P_1_3_PopulateAgencyIDList {
	
	public void agencyIDList() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("AgencyID")+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
		AgencyIDListVO agencyidlistVO = new AgencyIDListVO();
		NodeList result = xh.getNodeList("//xsd:simpleType");
		for(int i = 0; i < result.getLength(); i++) {
			Element tmp = (Element)result.item(i);
			if(tmp.getAttribute("name").endsWith("IdentificationContentType")){
				agencyidlistVO.setAgencyIDListGUID(tmp.getAttribute("id"));
			}
			if(tmp.getAttribute("name").endsWith("EnumerationType")){
				agencyidlistVO.setEnumerationTypeGUID(tmp.getAttribute("id"));
			}	
		}
		agencyidlistVO.setName("Agency Identification");
		agencyidlistVO.setListID("3055");
		//agencyidlistVO.setAgencyID(6);
		agencyidlistVO.setVersionID("D08B");
		agencyidlistVO.setDefinition("Schema agency:UN/CEFACT   Schema version:3.3		Schema date:15 September 2009	Code list name:Agency Identification Code   	Code list agency: UNECE    Code list version:D08B");		
		dao.insertObject(agencyidlistVO);

	}
	
	public void updateAgencyIDList() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDListValue");
		QueryCondition qc = new QueryCondition();
		qc.add("value", 6);
		AgencyIDListValueVO aAgencyIDListValueVO = ((AgencyIDListValueVO)dao.findObject(qc));
		
		SRTDAO dao1 = df.getDAO("AgencyIDList");
		AgencyIDListVO aAgencyIDListVO = (AgencyIDListVO)(dao1.findObjects().get(0));
		aAgencyIDListVO.setAgencyID(aAgencyIDListValueVO.getAgencyIDListValueID());
		dao1.updateObject(aAgencyIDListVO);

	}

	public void agencyIDListValue() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("AgencyID")+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDListValue");
		AgencyIDListValueVO agencyidlistvalueVO = new AgencyIDListValueVO();

		DAOFactory df2 = DAOFactory.getDAOFactory();
		SRTDAO dao2 = df2.getDAO("AgencyIDList");
		ArrayList<SRTObject> agency_id_fk = dao2.findObjects();

		for(SRTObject dvo : agency_id_fk) {
			AgencyIDListVO svo = (AgencyIDListVO) dvo;
			NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration") ;

			for(int i = 0; i < enumeration.getLength(); i++) {
				Element enum_element = (Element)enumeration.item(i);
				agencyidlistvalueVO.setValue(enum_element.getAttribute("value"));	
				
				Node name = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration//*[local-name()=\"ccts_Name\"]") ;
				Node definition = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration//*[local-name()=\"ccts_Definition\"]") ;
				
				agencyidlistvalueVO.setName(((Element)name).getTextContent());
				agencyidlistvalueVO.setDefinition(((Element)definition).getTextContent());
				agencyidlistvalueVO.setOwnerAgencyIDListID(svo.getAgencyIDListID());
				dao.insertObject(agencyidlistvalueVO);
			}
		}
	}
	
	public void run() throws Exception {
		System.out.println("### 1.3 Start");
		agencyIDList();
		agencyIDListValue();
		updateAgencyIDList();
		System.out.println("### 1.3 End");
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();

		P_1_3_PopulateAgencyIDList agencyidlist = new P_1_3_PopulateAgencyIDList();
		agencyidlist.agencyIDList();
		agencyidlist.agencyIDListValue();
		agencyidlist.updateAgencyIDList();
	}
}
