package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.Agency_ID_ListVO;
import org.oagi.srt.persistence.dto.Agency_ID_List_ValueVO;
import org.oagi.srt.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class PopulateAgencyIDList {
	public void agencyIDList() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("AgencyID")+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("Agency_ID_List");
		Agency_ID_ListVO agencyidlistVO = new Agency_ID_ListVO();
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
		agencyidlistVO.setAgencyID(6);
		agencyidlistVO.setVersionID("D08B");
		agencyidlistVO.setDefinition("Schema agency:UN/CEFACT   Schema version:3.3		Schema date:15 September 2009	Code list name:Agency Identification Code   	Code list agency: UNECE    Code list version:D08B");		
		dao.insertObject(agencyidlistVO);
		System.out.println("###END#####");

	}
	
	public void agencyIDListValue() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("AgencyID")+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("Agency_ID_List_Value");
		Agency_ID_List_ValueVO agencyidlistvalueVO = new Agency_ID_List_ValueVO();
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		SRTDAO dao2 = df2.getDAO("Agency_ID_List");
		ArrayList<SRTObject> agency_id_fk = dao2.findObjects();

		for(SRTObject dvo : agency_id_fk) {
			Agency_ID_ListVO svo = (Agency_ID_ListVO) dvo;
			NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration") ;
			NodeList name = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration//ccts:Name") ;
			NodeList definition = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration//ccts:Definition") ;
			for(int i = 0; i < enumeration.getLength(); i++) {
			    Element enum_element = (Element)enumeration.item(i);
			    agencyidlistvalueVO.setValue(enum_element.getAttribute("value"));		    
			    
			    Element name_element = (Element)name.item(i);
			    agencyidlistvalueVO.setName(name_element.getTextContent());
			    
			    Element definition_element = (Element)definition.item(i);
			    agencyidlistvalueVO.setDefinition(definition_element.getTextContent());
			    
			    agencyidlistvalueVO.setOwnerAgencyIDListID(svo.getAgencyIDListID());
		       dao.insertObject(agencyidlistvalueVO);
			}
		}
		System.out.println("###END#####");

	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();

		PopulateAgencyIDList agencyidlist = new PopulateAgencyIDList();
		agencyidlist.agencyIDList();
		agencyidlist.agencyIDListValue();
	}
}
