package org.oagi.srt.persistent.populate;

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
import org.oagi.srt.persistence.dto.DTVO;
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

public class P_1_6_1_PopulateDT {
	public void dt(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("DT")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		DTVO dtVO = new DTVO();

		NodeList result = xh.getNodeList("//xsd:complexType");
	    
		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    if(tmp.getAttribute("name").equals("ExpressionType") || tmp.getAttribute("name").equals("ActionExpressionType") || tmp.getAttribute("name").equals("ResponseExpressionType") ) {
			    dtVO.setDTGUID(tmp.getAttribute("id"));
			    dtVO.setDTType(1);
			    dtVO.setVersionNumber("1.0");
			    //dtVO.setPreviousVersionDTID();//Leave BLANK
			    dtVO.setRevisionType(0);
			    dtVO.setBasedDTID(getBasedDTIDwDen("Text", "Text_0F0ZL2. Type"));
			    dtVO.setDataTypeTerm(getDataTypeTerm(dtVO.getBasedDTID()));
			    //dtVO.setQualifier("");//Leave BLANK
			    String tmpDEN = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("Type"))+".Type";
			    if(tmpDEN.contains("ID")){
			    	tmpDEN = tmpDEN.replace("ID", "Identifier");
			    }
			    dtVO.setDEN(tmpDEN);
			    dtVO.setContentComponentDEN(tmpDEN + ".Content");
			    
			    Node definition = xh.getNode("//xsd:complexType[@id = '" + dtVO.getDTGUID() + "']//xsd:annotation//xsd:documentation") ;
			    Element definition_element = (Element)definition;
			    if(definition_element != null) {
				    dtVO.setDefinition(definition.getTextContent());
			    }
			    else {
			    	dtVO.setDefinition(null);
			    }
			    
			    dtVO.setContentComponentDefinition(null);
			    dtVO.setRevisionDocumentation(null);
			    dtVO.setRevisionState(1);
			    
			    int id = getUserID("oagis");
				dtVO.setCreatedByUserId(id);
				dtVO.setLastUpdatedByUserId(id);
				dtVO.setLastUpdateTimestamp(current_stamp);
				dtVO.setRevisionDocumentation("");
			    dao.insertObject(dtVO);
		    }
	    }
		System.out.println("###END#####");
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
	
	public int getBasedDTID(String DataType) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(DataType));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
	
	public int getBasedDTIDwDen(String DataType, String Den) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(DataType));
		qc.add("DEN", new String(Den));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
	
	public String getDataTypeTerm(int BasedDTID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("DT_ID", BasedDTID);
		DTVO dtVO = (DTVO)dao.findObject(qc);
		String DataTypeTerm = dtVO.getDataTypeTerm();
		return DataTypeTerm;
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_1_PopulateDT dt = new P_1_6_1_PopulateDT();
		String filename = "Meta" + ".xsd";
		dt.dt(filename);

	}
}
