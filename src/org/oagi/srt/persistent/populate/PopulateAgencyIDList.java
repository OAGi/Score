package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.Agency_ID_ListVO;
import org.oagi.srt.persistence.dto.Agency_ID_List_ValueVO;
import org.oagi.srt.startup.SRTInitializer;
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
	public void AgencyIDList() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = Utility.filepath()+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);
			
		setup();
		
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
		agencyidlistVO.setAgencyID(6);//�����ؾ���
		agencyidlistVO.setVersionID("D08B");
		agencyidlistVO.setDefinition("Schema agency:UN/CEFACT   Schema version:3.3		Schema date:15 September 2009	Code list name:Agency Identification Code   	Code list agency: UNECE    Code list version:D08B");		
		dao.insertObject(agencyidlistVO);
		
		System.out.println("###END#####");

	}
	
	public static void AgencyIDListValue() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = Utility.filepath()+"IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
		XPathHandler xh = new XPathHandler(path1);

		setup();

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("Agency_ID_List_Value");
		Agency_ID_List_ValueVO agencyidlistvalueVO = new Agency_ID_List_ValueVO();
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		SRTDAO dao2 = df2.getDAO("Agency_ID_List");
		ArrayList<SRTObject> agency_id_fk = dao2.findObjects();

		for(SRTObject dvo : agency_id_fk) {
			Agency_ID_ListVO svo = (Agency_ID_ListVO) dvo;//Agency_id_list table���� ȣ��

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
	
	private static void setup() throws SRTInitializerException {
		ServerProperties props = ServerProperties.getInstance();
		String _propFile = "/" + SRTConstants.SRT_PROPERTIES_FILE_NAME;
		try {
			InputStream is = SRTInitializer.class.getResourceAsStream(_propFile);
			if (is == null) {
				throw new SRTInitializerException(_propFile + " not found!");
			}
			try {
				props.load(is, true);
			} catch (IOException e) {
				throw new SRTInitializerException(_propFile + " cannot be read...");
			}
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Getting "
					+ SRTConstants.SRT_PROPERTIES_FILE_NAME + " URL : "
					+ e.toString());
		}
		try {
			ConnectionPoolManager cpm = ConnectionPoolManager.getInstance();
			String poolName = cpm.getDefaultPoolName();
			System.out.println("DefaultPoolName:" + poolName);
			Connection dbConnection = cpm.getConnection(poolName);
			dbConnection.close();
			System.out.println("DB Connection Pool initialized...");
			cpm.release();
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
			e.printStackTrace();
			throw new SRTInitializerException("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
		}
	
	}
	
	public static void main (String args[]) throws SRTInitializerException, SRTDAOException, FileNotFoundException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		setup();
		PopulateAgencyIDList p = new PopulateAgencyIDList();
		p.AgencyIDList();		
	}

}
