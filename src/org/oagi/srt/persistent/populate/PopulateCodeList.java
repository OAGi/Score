package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.Code_ListVO;
import org.oagi.srt.persistence.dto.Code_List_ValueVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.startup.SRTInitializer;
import org.oagi.srt.startup.SRTInitializerException;
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

public class PopulateCodeList {

	public String filename;
	public int agency_id;
	public static int number_of_input_file = 11;
	
	public static void CodeList(String fileinput, int agencyid) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = Utility.filepath()+ fileinput;

		XPathHandler xh = new XPathHandler(path1);
			
		setup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("Code_List");
		Code_ListVO codelistVO = new Code_ListVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
	    NodeList definition = xh.getNodeList("//xsd:simpleType[xsd:annotation[xsd:documentation]]");	
    	NodeList union = xh.getNodeList("//xsd:simpleType[xsd:union]");
    	
		DAOFactory df2 = DAOFactory.getDAOFactory();
		
		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
    	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    if(tmp.getAttribute("name").endsWith("CodeContentType")){
		    	codelistVO.setCodeListGUID(tmp.getAttribute("id"));
		    	
		    	for(int j = 0; j < result.getLength(); j++) {
		    		Element tmp2 = (Element)result.item(j);
		    		

	    			if(tmp2.getAttribute("name").endsWith("EnumerationType")) {
	    				if(tmp2.getAttribute("name").substring(0, tmp2.getAttribute("name").lastIndexOf("EnumerationType")).equals(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")))
		    				) {
		    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
		    				break;
		    			}
		    		}
	    			else {
	    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
	    			}
		    			
		    	}
		   
		    	codelistVO.setName(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")));
		    	codelistVO.setListID(tmp.getAttribute("id"));
		    	codelistVO.setAgencyID(agencyid);
		    	
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		codelistVO.setVersionID("1");
		    	}
		    	else if(tmp.getAttribute("name").equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
		    		codelistVO.setVersionID("5");
		    	}
		    	else {
			    	for(int j = 0; j < tmp.getAttribute("name").length(); j++) {
				    	if(tmp.getAttribute("name").charAt(j)>47 && tmp.getAttribute("name").charAt(j)<58) {
				    		for(int k = j+1 ; k < tmp.getAttribute("name").length(); k++){
				    			if(tmp.getAttribute("name").charAt(k) == '_'){
						    		codelistVO.setVersionID(tmp.getAttribute("name").substring(j, k));
						    		break;
				    			}
				    		}
				    	break;
				    	}
			    	}		    		
		    	}
		    	
		    	for(int j = 0 ; j < definition.getLength(); j++) {
		    		Element definition_element = (Element)definition.item(j);
		    		if(definition_element.getAttribute("id") == tmp.getAttribute("id")) {
		    			Node definition_node = xh.getNode("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:annotation//xsd:documentation");	
		    			Element definition_element2 = (Element)definition_node;
		    			codelistVO.setDefinition(definition_element2.getTextContent());
				    	codelistVO.setDefinitionSource(definition_element2.getAttribute("source"));
				    	break;
		    		}
		    		
	    			codelistVO.setDefinition("NONE");
			    	codelistVO.setDefinitionSource("NONE");				    	

		    	}
		    	
		    	codelistVO.setBasedCodeListID(1);//Empty
		    	codelistVO.setExtensibleIndicator(false); 
		    	for(int j = 0; j < union.getLength(); j++) {
				    Element tmp2 = (Element)union.item(j);
				    if(tmp2.getAttribute("name").equals(tmp.getAttribute("name"))) {
				    	codelistVO.setExtensibleIndicator(true);
				    	break;
				    }
				}
		    	
		    	
		    	{
					SRTDAO dao2 = df2.getDAO("User");
			    	QueryCondition qc = new QueryCondition();
					qc.add("User_Name", new String("oagis"));
					UserVO userVO = (UserVO)dao2.findObject(qc);
					codelistVO.setCreatedByUserID(userVO.getUserID());
					codelistVO.setLastUpdatedByUserID(userVO.getUserID());
					codelistVO.setLastUpdateTimestamp(current_stamp);
		    	}
		    	dao.insertObject(codelistVO);
		    }
		}
		System.out.println("###END#####");
	}
	
	public static void CodeListValue(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = Utility.filepath() + fileinput;
		XPathHandler xh = new XPathHandler(path1);
			
		setup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("Code_List_Value");
		Code_List_ValueVO codelistvalueVO = new Code_List_ValueVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
		NodeList enumeration, typenode;
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
	    	if(tmp.getAttribute("name").endsWith("CodeContentType")) {
				
	    		typenode = xh.getNodeList("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:enumeration");
	    		if(typenode.getLength()==0) {
	    			
	    		}
	    		else {
	    			
	    			
	    		}
		    	SRTDAO dao2 = df2.getDAO("Code_List");
			   	QueryCondition qc = new QueryCondition();
				qc.add("Code_List_GUID", new String(tmp.getAttribute("id")));
				Code_ListVO codelistVO = (Code_ListVO)dao2.findObject(qc);
	    		
	    		for(int j = 0; j < result.getLength(); j++) {
		    		Element tmp2 = (Element)result.item(j);
	    			if(tmp2.getAttribute("name").endsWith("EnumerationType")) {
	    				if(tmp2.getAttribute("name").substring(0, tmp2.getAttribute("name").lastIndexOf("EnumerationType")).equals(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")))) {
	    			    	
	    		    		if(typenode.getLength()==0) {
		    					enumeration = xh.getNodeList("//xsd:simpleType[@name = '" + tmp2.getAttribute("name") + "']//xsd:enumeration");
	    		    		}
	    		    		else {
	    		    			enumeration = typenode;	    		    			
	    		    		}

	    			    	for(int k = 0; k < enumeration.getLength(); k++) {
	    			    		Element tmp3 = (Element)enumeration.item(k);
	    						codelistvalueVO.setOwnerCodeListID(codelistVO.getCodeListID());
	    			    		codelistvalueVO.setValue(tmp3.getAttribute("value"));
	    			    		codelistvalueVO.setName(tmp3.getAttribute("value"));//확인 필요
	    			    		codelistvalueVO.setUsedIndicator(true);
	    			    		codelistvalueVO.setLockedIndicator(false);
		    			    	Node name_node = xh.getNode("//xsd:simpleType[@name = '" + tmp2.getAttribute("name") + "']//xsd:enumeration[@value = '" + tmp3.getAttribute("value") + "']//xsd:documentation");	
				    			if(name_node != null) {
			    			    	Element name_element = (Element)name_node;
			    			    	codelistvalueVO.setDefinition(name_element.getTextContent());
			    			    	codelistvalueVO.setDefinitionSource(name_element.getAttribute("source"));
					    		}
				    			
				    			else {
				    				codelistvalueVO.setDefinition("");
				    				codelistvalueVO.setDefinitionSource("");
				    			}
				    		dao.insertObject(codelistvalueVO);
	    			    	}
		    				break;
		    			}
		    		}
		    			
		    	}
	    		
	    	}
		}
		System.out.println(codelistvalueVO.getValue());
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
		
		PopulateCodeList[] agencyidlist;
		agencyidlist = new PopulateCodeList[number_of_input_file+1];

		for(int i = 0; i < agencyidlist.length ; i++) {
			agencyidlist[i] = new PopulateCodeList ();
		}
		agencyidlist[0].filename = "CodeLists_1"; agencyidlist[0].agency_id = 314; 
		agencyidlist[1].filename = "CodeList_ConditionTypeCode_1"; agencyidlist[1].agency_id = 314;
		agencyidlist[2].filename = "CodeList_ConstraintTypeCode_1"; agencyidlist[2].agency_id = 314;
		agencyidlist[3].filename = "CodeList_DateFormatCode_1"; agencyidlist[3].agency_id = 314;
		agencyidlist[4].filename = "CodeList_DateTimeFormatCode_1"; agencyidlist[4].agency_id = 314;
		agencyidlist[5].filename = "CodeList_TimeFormatCode_1"; agencyidlist[5].agency_id = 314;
		agencyidlist[6].filename = "CodeList_CharacterSetCode_IANA_20070514"; agencyidlist[6].agency_id = 379;
		agencyidlist[7].filename = "CodeList_MIMEMediaTypeCode_IANA_7_04"; agencyidlist[7].agency_id = 379;
		agencyidlist[8].filename = "CodeList_CurrencyCode_ISO_7_04"; agencyidlist[8].agency_id = 5;
		agencyidlist[9].filename = "CodeList_LanguageCode_ISO_7_04"; agencyidlist[9].agency_id = 5;
		agencyidlist[10].filename = "CodeList_TimeZoneCode_1"; agencyidlist[10].agency_id = 5;
		agencyidlist[11].filename = "CodeList_UnitCode_UNECE_7_04"; agencyidlist[11].agency_id = 6;

		
		for(int i = 0; i < agencyidlist.length ; i++) {
			String filename = agencyidlist[i].filename+".xsd";
			int agencyid = agencyidlist[i].agency_id;
			CodeList(filename, agencyid);
			CodeListValue(filename);
			System.out.println(filename+" upload is completed..");
		}

	}
}
