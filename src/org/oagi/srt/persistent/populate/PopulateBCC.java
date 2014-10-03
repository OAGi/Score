package org.oagi.srt.persistent.populate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.UserVO;
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

public class PopulateBCC {
    int seq = 0;

	public void bcc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BOD")+ fileinput;
				
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory bcc = DAOFactory.getDAOFactory();
		SRTDAO dao = bcc.getDAO("BCC");
		BCCVO bccVO = new BCCVO();

		NodeList result;
		Node tmpParent = null;

		for(int j = 0; j < 2; j++) {
			if(j == 0) {
				result = xh.getNodeList("//xsd:complexType//xsd:element");
			}
			else {
				result = xh.getNodeList("//xsd:complexType//xsd:attribute");
			}
			
			for(int i = 0; i < result.getLength(); i++) {
			    Element tmp = (Element)result.item(i);
			    bccVO.setBCCGUID(tmp.getAttribute("id"));
			    
			    if(j == 0){
				    if(tmp.getAttribute("minOccurs").isEmpty()){
				    	bccVO.setCardinalityMin(1);
				    }
				    else {
				    	bccVO.setCardinalityMin(Integer.parseInt(tmp.getAttribute("minOccurs")));
				    }
			    }
			    else {
					Node use = xh.getNode("//xsd:complexType[@id = '" + bccVO.getBCCGUID() + "']//xsd:attribute");
				    Element use_element = (Element)use;
				    bccVO.setCardinalityMin(getMinCardinality(use_element.getAttribute("use")));
			    }
			    
			    if(j == 0){
				    if(tmp.getAttribute("maxOccurs").isEmpty()){
				    	bccVO.setCardinalityMax(1);
				    }
				    else if(tmp.getAttribute("maxOccurs").equals("unbounded")){
				    	
				    }
				    else {
				    	bccVO.setCardinalityMax(Integer.parseInt(tmp.getAttribute("maxOccurs")));
				    }
			    }
			    else {
					Node use = xh.getNode("//xsd:complexType[@id = '" + bccVO.getBCCGUID() + "']//xsd:attribute");
				    Element use_element = (Element)use;
				    bccVO.setCardinalityMax(getMaxCardinality(use_element.getAttribute("use")));
			    }
			    
			    if(tmp.getParentNode() != tmpParent){
			    	seq++;
				    tmpParent = tmp.getParentNode();
			    }
		    	bccVO.setSequencingkey(seq);
		    	
		    	if(j == 0) {
		    		bccVO.setEntityType(1);
		    	}
		    	else {
		    		bccVO.setEntityType(0);
		    	}

			    bccVO.setDEN("");
			    //dao.insertObject(bccVO);
			}
		}
		System.out.println("###"+fileinput+" is ended#####");
	}
	
	public int getMinCardinality(String use) {
		if (use.equals("optional")) {
			return 0;
		} 
		else if (use.equals("required")) {
			return 1;
		} 
		else if (use.equals("prohibited")) {
			return 0;
		} 
		else {
			return 0;
		}
	}
	
	public int getMaxCardinality(String use) {
		if (use.equals("optional")) {
			return 1;
		} 
		else if (use.equals("required")) {
			return 1;
		} 
		else if (use.equals("prohibited")) {
			return 0;
		} 
		else {
			return 1;
		}
	}
	
	public int getUserID(String userName) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("User");
    	QueryCondition qc = new QueryCondition();
		qc.add("User_Name", new String(userName));
		UserVO userVO = (UserVO)dao.findObject(qc);
		int id = userVO.getUserID();
		return id;
	}
	
	public int getOAGISComponentType(String DEN) {
		if(DEN.endsWith("Base")){
			return 0;
		}
		else if(DEN.equals("Open User Area") || DEN.equals("Any User Area") || DEN.equals("All Extension") || DEN.endsWith("Extension") ) {
			return 2;
		}
		else if(DEN.endsWith("Group")) {
			return 3;
		}
		else {
			return 1;
		}		
	}
	
	private String spaceSeparator(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i)) && i != 0) {
				sb.append(" " + str.charAt(i));
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}
	
	private String first(String den) {
		return den.substring(0, den.indexOf(".")).replace("_", " ");
	}
	

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateBCC bcc = new PopulateBCC();
		
		String path1 = SRTConstants.filepath("BOD");
		File path = new File(path1);
		String files[] = path.list();
		for(int i = 0; i < 1/*files.length*/; i++){
			System.out.println(files[i]+" is proceeded...");
			bcc.bcc(files[i]);
		}
	}
}
