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
import org.oagi.srt.persistence.dto.BCCPVO;
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
	
	File f1 = new File("C:/Work/Project/OAG/Development/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/BODs/");
	File f2 = new File("C:/Work/Project/OAG/Development/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/BODs/");

	private void populate() throws Exception {
		
		File[] listOfF1 = getBODs(f1);
		File[] listOfF2 = getBODs(f2);

		for (File file : listOfF1) {
			insertASCCP(file);
		}
		
		for (File file : listOfF1) {
		}
	}
	
	private void insertASCCP(File file) throws Exception {
		
		XPathHandler xh = new XPathHandler(file.getAbsolutePath());
		Element ele = (Element)xh.getNode("/xsd:schema/xsd:element");
		Element def = (Element)xh.getNode("/xsd:schema/xsd:element/xsd:annotation/xsd:documentation");
		String name = ele.getAttribute("name");
		String type = ele.getAttribute("type");
		
		Element complexType = (Element)xh.getNode("/xsd:schema/xsd:complexType[@name = '" + type + "' and count(xsd:simpleContent) = 0] ");
		if(complexType != null) {
			String asccpId = ele.getAttribute("id");
			String propertyTerm = Utility.spaceSeparator(name);
			String definition = def.getTextContent();
			
			
			int roleOfAccId;
			
			String den = propertyTerm + ". " + ;
			
			
			int state = 4;
			String module = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/"), file.getAbsolutePath().lastIndexOf("."));
		}
		
		System.out.println(complexType.getAttribute("name"));
	}
	
	private void insertACC(String type, String fullFilePath) throws Exception {
		XPathHandler xh = new XPathHandler(fullFilePath);
		Element complexType = (Element)xh.getNode("/xsd:schema/xsd:complexType[@name = '" + type + "']");
		Element cDef = (Element)xh.getNode("/xsd:schema/xsd:complexType[@name = '" + type + "']/xsd:annotation/xsd:documentation");
		Element ext = (Element)xh.getNode("/xsd:schema/xsd:complexType[@name = '" + type + "']/xsd:extension");
		
		String accGuid = complexType.getAttribute("id");
		String objectClassName = Utility.spaceSeparator(complexType.getAttribute("name").substring(0, complexType.getAttribute("name").indexOf("Type")));
		String den = objectClassName + ". Details";
		String definition = cDef.getTextContent();
		
		int basedAccId;
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
		} else {
			basedAccId = -1;
		}
		
		int oagisComponentType = 1;
		if(Utility.first(den).endsWith("Base"))
			oagisComponentType = 0;
		else if(Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
			oagisComponentType = 2;
		else if(Utility.first(den).endsWith("Group"))
			oagisComponentType = 3;
		
		int state = 4;
		String module = fullFilePath.substring(fullFilePath.lastIndexOf("/"), fullFilePath.lastIndexOf("."));
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
