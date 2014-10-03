package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDT_Primitive_RestrictionVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_Primitive_Expression_Type_MapVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSD_BuiltIn_TypeVO;
import org.oagi.srt.startup.SRTInitializerException;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class PopulateBDT_Primitive_Restriction {
	public void bdt_primitive_restriction(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDT_Primitive_Restriction");
		BDT_Primitive_RestrictionVO bdt_primitive_restrictionVO = new BDT_Primitive_RestrictionVO();
		
	    ArrayList<SRTObject> bdtidlist = new ArrayList<SRTObject>();
	    bdtidlist = getBDTID("TEXT", getTokenID("Text_0F0ZL2. Type"));
	    ArrayList<SRTObject> cdtallowedprimitiveexpressiontypemapidlist = new ArrayList<SRTObject>();
	    int builtintypeid = getBuiltInTypeID("token");
	    cdtallowedprimitiveexpressiontypemapidlist = getCdt_Allowed_Primitive_Expression_Type_MapID(builtintypeid);

	    for(SRTObject dvo : bdtidlist) {
			DTVO svo = (DTVO) dvo;
			for(SRTObject dvo2 : cdtallowedprimitiveexpressiontypemapidlist){
				CDT_Allowed_Primitive_Expression_Type_MapVO svo2 = (CDT_Allowed_Primitive_Expression_Type_MapVO) dvo2;
				bdt_primitive_restrictionVO.setBDTID(svo.getDTID());
				bdt_primitive_restrictionVO.setCDTPrimitiveExpressionTypeMapID(svo2.getCDTPrimitiveExpressionTypeMapID());
				//bdt_primitive_restrictionVO.setCodeListID(0); BLANK
				if(svo2.getXSDBuiltInTypeID() == builtintypeid) {
					bdt_primitive_restrictionVO.setisDefault(true);
				}
				else {
					bdt_primitive_restrictionVO.setisDefault(false);
				}
			}
		}
	    dao.insertObject(bdt_primitive_restrictionVO);
		System.out.println("###END#####");
	}
	
	public ArrayList<SRTObject> getBDTID(String dataType, int basedDTID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(dataType));
		qc.add("Based_DT_ID", basedDTID);
		ArrayList<SRTObject> bdtidlist = new ArrayList<SRTObject>();
		bdtidlist = dao.findObjects(qc);
		return bdtidlist;
	}
	
	public int getTokenID(String DEN) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("DEN", new String(DEN));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
		
	public int getBuiltInTypeID(String name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSD_BuiltIn_Type");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(name));
		XSD_BuiltIn_TypeVO XSD_BuiltIn_TypeVO = (XSD_BuiltIn_TypeVO)dao.findObject(qc);
		int id = XSD_BuiltIn_TypeVO.getXSDBuiltInTypeID();
		return id;
	}
	
	public ArrayList<SRTObject> getCdt_Allowed_Primitive_Expression_Type_MapID(int xsd_builtin_type_id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDT_Allowed_Primitive_Expression_Type_Map");
    	QueryCondition qc = new QueryCondition();
		qc.add("XSD_BuiltIn_Type_ID", xsd_builtin_type_id);
		ArrayList<SRTObject> cdtallowedprimitiveexpressiontypemapidlist = new ArrayList<SRTObject>();
		cdtallowedprimitiveexpressiontypemapidlist = dao.findObjects(qc);
		return cdtallowedprimitiveexpressiontypemapidlist;
		
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateBDT_Primitive_Restriction bdt_primitive_restriction = new PopulateBDT_Primitive_Restriction();
		String filename = "Meta" + ".xsd";
		bdt_primitive_restriction.bdt_primitive_restriction(filename);
	}
}
