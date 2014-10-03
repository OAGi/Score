package org.oagi.srt.persistent.populate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BCCPVO;
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

public class PopulateBCCP {
	public void bccp(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BOD")+ fileinput;
				
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory bccp = DAOFactory.getDAOFactory();
		SRTDAO dao = bccp.getDAO("BCCP");
		BCCPVO bccpVO = new BCCPVO();

		NodeList result = xh.getNodeList("//xsd:attribute");

		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    bccpVO.setBCCPGUID("NEW");
		    
		    String tmpTerm = tmp.getAttribute("name");
		    if(tmpTerm.contains("ID")){
		    	tmpTerm = tmpTerm.replace("ID", "Identifier");
		    }
		    bccpVO.setPropertyTerm(spaceSeparator(tmpTerm));
		    
		    bccpVO.setBDTID(0);
		    bccpVO.setRepresentationTerm("");
		    bccpVO.setDEN(bccpVO.getPropertyTerm()+"."+bccpVO.getRepresentationTerm());
		    
		    Node definition = xh.getNode("//xsd:element[@name = '" + tmp.getAttribute("name") + "']//xsd:annotation//xsd:documentation");
		    Element definition_element = (Element)definition;
		    if(definition_element != null) {
		    	bccpVO.setDefinition(definition.getTextContent());
		    }
		    else {
		    	bccpVO.setDefinition("");
		    }
		    
		    int id = getUserID("oagis");
		    bccpVO.setCreatedByUserId(id);
		    bccpVO.setLastUpdatedByUserId(id);
		    bccpVO.setCreationTimestamp(current_stamp);
		    bccpVO.setLastUpdateTimestamp(current_stamp);
		    //dao.insertObject(bccpVO);
		    }
		System.out.println("###"+fileinput+" is ended#####");
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
	
	private String spaceSeparator(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i)) && i != 0) {
				if (i > 0 && i < str.length()
						&& Character.isUpperCase(str.charAt(i - 1)))
					if (i < str.length() - 1
							&& Character.isLowerCase(str.charAt(i + 1)))
						sb.append(" " + str.charAt(i));
					else
						sb.append(str.charAt(i));
				else
					sb.append(" " + str.charAt(i));
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateBCCP bccp = new PopulateBCCP();
		
		String path1 = SRTConstants.filepath("BOD");
		File path = new File(path1);
		String files[] = path.list();
		for(int i = 0; i < 1/*files.length*/; i++){
			System.out.println(files[i]+" is proceeded...");
			bccp.bccp(files[i]);
		}
	}
}
