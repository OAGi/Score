package org.oagi.srt.persistence.validate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.XSD_BuiltIn_TypeVO;
import org.oagi.srt.persistence.validate.data.TableData;
import org.oagi.srt.startup.SRTInitializer;
import org.oagi.srt.startup.SRTInitializerException;

/**
*
* @author Yunsu Lee
* @version 1.0
*/


public class XSD_BuiltIn_TypeTest {
	private static SRTDAO dao;
	
	@BeforeClass
	public static void connectDB() throws Exception{
		Utility.dbSetup();

		DAOFactory df = DAOFactory.getDAOFactory();
		dao = df.getDAO("XSD_BuiltIn_Type");
	}
	
	@Test
	public void testNumberOfData() {
		try {
			assertEquals(TableData.XDT_BUILT_IN_TYPE.length, dao.findObjects().size());
		} catch (SRTDAOException e) {
			fail("Database error.");
		}
	}

	@Test
	public void testName() {
		for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++){
			QueryCondition qc  = new QueryCondition();
			qc.add("name", TableData.XDT_BUILT_IN_TYPE[i][0]);
			try {
				XSD_BuiltIn_TypeVO xVO = (XSD_BuiltIn_TypeVO)dao.findObject(qc);
				if(xVO.getBuiltInType() == null)
					fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");
				
			} catch (SRTDAOException e) {
				fail("Database error.");
			}
		}
	}
	
	@Test
	public void testBuiltinType() {
		for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++){
			QueryCondition qc  = new QueryCondition();
			qc.add("name", TableData.XDT_BUILT_IN_TYPE[i][0]);
			try {
				XSD_BuiltIn_TypeVO xVO = (XSD_BuiltIn_TypeVO)dao.findObject(qc);
				if(xVO.getBuiltInType() == null)
					fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");
				
				assertEquals(TableData.XDT_BUILT_IN_TYPE[i][1], xVO.getBuiltInType());
			} catch (SRTDAOException e) {
				fail("Database error.");
			}
		}
	}
	
	@Test
	public void testTypeHierarchy() {
		for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++){
			QueryCondition qc  = new QueryCondition();
			qc.add("name", TableData.XDT_BUILT_IN_TYPE[i][0]);
			try {
				XSD_BuiltIn_TypeVO xVO = (XSD_BuiltIn_TypeVO)dao.findObject(qc);
				if(xVO.getBuiltInType() == null)
					fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");
				
				QueryCondition qc1  = new QueryCondition();
				qc1.add("XSD_BuiltIn_Type_ID", xVO.getSubtypeOfXSDBuiltinTypeId());
				XSD_BuiltIn_TypeVO xVO1 = (XSD_BuiltIn_TypeVO)dao.findObject(qc1);
				
				assertEquals(TableData.XDT_BUILT_IN_TYPE[i][2], xVO1.getBuiltInType());
			} catch (SRTDAOException e) {
				fail("Database error.");
			}
		}
	}
}
