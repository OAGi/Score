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
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.XSD_BuiltIn_TypeVO;
import org.oagi.srt.startup.SRTInitializer;
import org.oagi.srt.startup.SRTInitializerException;

/**
*
* @author Nasif Sikder
* @version 1.0
*
*What's left?
* The ordering shouldn't matter when testing
*  validate the ID's for subtype_Of_XSD_BuiltIn_Type_ID
*/


public class XSD_BuiltIn_TypeTest {
	private static ArrayList<SRTObject> voList;


	
	public static ArrayList<String> nameList = new ArrayList<String>(Arrays.asList(
		"any type",
		"any simple type",
		"duration",
		"date time",
		"time",
		"date",
		"gregorian year month",
		"gregorian year",
		"gregorian month day",
		"gregorian day",
		"gregorian month",
		"string",
		"normalized string",
		"token",
		"boolean",
		"base64 binary",
		"hex binary",
		"float",
		"decimal",
		"integer",
		"non negative integer",
		"double",
		"any uri"
	));
	
	public static ArrayList<String> builtIn_TypeList = new ArrayList<String>(Arrays.asList(
		"xsd:anyType",
		"xsd:anySimpleType",
		"xsd:duration",
		"xsd:dateTime",
		"xsd:time",
		"xsd:date",
		"xsd:gYearMonth",
		"xsd:gYear",
		"xsd:gMonthDay",
		"xsd:gDay",
		"xsd:gMonth",
		"xsd:string",
		"xsd:normalizedString",
		"xsd:token",
		"xsd:boolean",
		"xsd:base64Binary",
		"xsd:hexBinary",
		"xsd:float",
		"xsd:decimal",
		"xsd:integer",
		"xsd:nonNegativeInteger",
		"xsd:double",
		"xsd:anyURI"
	));
	
	public static ArrayList<Integer> subtype_Of_XSD_BuiltIn_Type_IDList = new ArrayList<Integer>(Arrays.asList(
		0,
		6,
		7,
		7,
		7,
		7,
		7,
		7,
		7,
		7,
		7,
		7,
		17,
		18,
		7,
		7,
		7,
		7,
		7,
		24,
		25,
		7,
		7
	));
	
	@BeforeClass
	public static void connectDB() throws SRTInitializerException, SRTDAOException{
		setup();

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSD_BuiltIn_Type");
		voList = (ArrayList<SRTObject>)dao.findObjects();
	}

	@Test
	public void testName() {
		String name;
		for (int i = 0; i < nameList.size(); i++){
			name = ((XSD_BuiltIn_TypeVO)voList.get(i)).getName();
			assertEquals(nameList.get(i), name);
		}
	}

	@Test
	public void testBuiltInType() {
		String builtIn_Type;
		for (int i = 0; i < 23; i++){
			builtIn_Type = ((XSD_BuiltIn_TypeVO)voList.get(i)).getBuiltInType();
			assertEquals(builtIn_TypeList.get(i), builtIn_Type);
		}
	}

	@Test
	public void testSubtype_Of_XSD_BuiltIn_Type_ID() {
		Integer subtype_Of_XSD_BuiltIn_Type_ID;
		for (int i = 0; i < subtype_Of_XSD_BuiltIn_Type_IDList.size(); i++){
			subtype_Of_XSD_BuiltIn_Type_ID = ((XSD_BuiltIn_TypeVO)voList.get(i)).getSubtype_Of_XSD_BuiltIn_Type_ID();
			assertEquals(subtype_Of_XSD_BuiltIn_Type_IDList.get(i), subtype_Of_XSD_BuiltIn_Type_ID);
		}
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

}
