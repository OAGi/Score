package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.DTSCVO;
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
public class P_1_6_4_PopulateCDTSCAllowedPrimitiveFromMetaXSD {
	public void scindt_sc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BDT_Primitive_Restriction")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitive");
		CDTSCAllowedPrimitiveVO cdt_sc_allowedVO = new CDTSCAllowedPrimitiveVO();

		NodeList ref = xh.getNodeList("//xsd:complexType");
		for (int j = 0; j < ref.getLength(); j++) {
			Element ref_tmp = (Element) ref.item(j);
		    if(ref_tmp.getAttribute("name").equals("ExpressionType") || ref_tmp.getAttribute("name").equals("ActionExpressionType") || ref_tmp.getAttribute("name").equals("ResponseExpressionType") ) {
				NodeList result = xh.getNodeList("//xsd:complexType[@id = '"+ ref_tmp.getAttribute("id") + "']//xsd:attribute");
				for (int i = 0; i < result.getLength(); i++) {
					Element tmp = (Element) result.item(i);
					cdt_sc_allowedVO.setCDTSCID(getDTSCID(tmp.getAttribute("id")));
					
					if (tmp.getAttribute("name").equals("actionCode") || tmp.getAttribute("name").equals("expressionLanguage")) {
						cdt_sc_allowedVO.setCDTPrimitiveID(getCDTPrimitiveID("NormalizedString"));
						cdt_sc_allowedVO.setisDefault(false);
						dao.insertObject(cdt_sc_allowedVO);
						
						cdt_sc_allowedVO.setCDTPrimitiveID(getCDTPrimitiveID("String"));
						cdt_sc_allowedVO.setisDefault(false);
						dao.insertObject(cdt_sc_allowedVO);
						
						cdt_sc_allowedVO.setCDTPrimitiveID(getCDTPrimitiveID("Token"));
						cdt_sc_allowedVO.setisDefault(true);
						dao.insertObject(cdt_sc_allowedVO);
					}
				}
		    }
		}
		System.out.println("###END#####");
	}
	
	public int getDTSCID(String DTSCGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("DT_SC_GUID", new String(DTSCGUID));
		DTSCVO dtscVO = (DTSCVO)dao.findObject(qc);
		int id = dtscVO.getDTSCID();
		return id;
	}
	
	public int getCDTPrimitiveID(String Name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(Name));
		CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO)dao.findObject(qc);
		int id = cdtPrimitiveVO.getCDTPrimitiveID();
		return id;
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_4_PopulateCDTSCAllowedPrimitiveFromMetaXSD scindt_sc = new P_1_6_4_PopulateCDTSCAllowedPrimitiveFromMetaXSD();
		String filename = "Meta" + ".xsd";
		scindt_sc.scindt_sc(filename);
	}
}
