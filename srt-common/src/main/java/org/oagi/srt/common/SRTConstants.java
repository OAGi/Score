package org.oagi.srt.common;

import java.io.File;
import java.io.IOException;

/**
 * @version 1.0
 * @author Yunsu Lee
 */

public class SRTConstants {

	public static final int DT_TYPE = 0;
	public static final String PRODUCT_NAME = "OAGi Semantic Refinement Tool";

	public static final String NS_CCTS_PREFIX = "ccts";
	public static final String NS_XSD_PREFIX = "xsd";
	public static final String NS_CCTS = "urn:un:unece:uncefact:documentation:1.1";
	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";

	public static final String OAGI_NS = "http://www.openapplications.org/oagis/10";

	public static final String TAB_TOP_LEVEL_ABIE_SELECT_BC = "select_bc";
	public static final String TAB_TOP_LEVEL_ABIE_CREATE_UC_BIE = "create_u_bie";
	public static final String TAB_TOP_LEVEL_ABIE_COPY_UC_BIE = "edit_bod";

	public static final int TOP_LEVEL_ABIE_STATE_EDITING = 2;
	public static final int TOP_LEVEL_ABIE_STATE_PUBLISHED = 4;

	public static final String CODE_LIST_STATE_EDITING = "Editing";
	public static final String CODE_LIST_STATE_PUBLISHED = "Published";
	public static final String CODE_LIST_STATE_DISCARDED = "Discarded";
	public static final String CODE_LIST_STATE_DELETED = "Deleted";

	public static final String AGENCY_ID_LIST_NAME = "clm63055D08B_AgencyIdentification";

	public static final String DATA_PATH;
	static {
		File dataPath = new File("data");
		if (!dataPath.exists()) {
			dataPath = new File("..", "data");
			if (!dataPath.exists()) {
				throw new IllegalStateException("Could not find 'data' directory. Check your environments.");
			}
		}
		try {
			DATA_PATH = dataPath.getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static final String BOD_FILE_PATH = new File(DATA_PATH, "xsd").getPath();

	/*
	 * To choose OAGIS version
	 */
	public static final String OAGIS_VERSION = "10.1";

	public static final String BASE_DATA_PATH;
	public static final String PLATFORM_PATH;
	public static final String CODELIST_CHARACTER_SET_CODE_IANA_FILENAME;
	static {
		switch (OAGIS_VERSION) {
			case "10.1":
				BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_1_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_1").getPath();
				PLATFORM_PATH = "/Platform/2_1";
				CODELIST_CHARACTER_SET_CODE_IANA_FILENAME = "CodeList_CharacterSetCode_IANA_20070514";
				break;
			case "10.2":
				BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_2_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_2").getPath();
				PLATFORM_PATH = "/Platform/2_2";
				CODELIST_CHARACTER_SET_CODE_IANA_FILENAME = "CodeList_CharacterSetCode_IANA_20131220";
				break;
			default:
				throw new UnsupportedOperationException("Unsupported version: " + OAGIS_VERSION);
		}
	}
	public static final String MODEL_FOLDER_PATH = BASE_DATA_PATH + "/Model";
	public static final String BOD_FILE_PATH_01 = MODEL_FOLDER_PATH + PLATFORM_PATH + "/BODs/";
	public static final String BOD_FILE_PATH_02 = MODEL_FOLDER_PATH + "/BODs/";

	public static final String FIELDS_XSD_FILE_PATH = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Common/Components/Fields.xsd";
	public static final String META_XSD_FILE_PATH = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Common/Components/Meta.xsd";
	public static final String BUSINESS_DATA_TYPE_XSD_FILE_PATH = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Common/DataTypes/BusinessDataType_1.xsd";
	public static final String COMPONENTS_XSD_FILE_PATH = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Common/Components/Components.xsd";
	public static final String XBT_FILE_PATH = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Common/DataTypes/XMLSchemaBuiltinType_1.xsd";



	public static String filepath(String list) {
		String prefix_filepath = MODEL_FOLDER_PATH;
		String origin_filepath = PLATFORM_PATH + "/Common/";
		if (list.equals("AgencyID")) {
			origin_filepath = PLATFORM_PATH + "/Common/IdentifierScheme/";
		} else if (list.equals("CodeList")) {
			origin_filepath = PLATFORM_PATH + "/Common/CodeLists/";
		} else if (list.equals("DT")) {
			origin_filepath = PLATFORM_PATH + "/Common/Components/";
		} else if (list.equals("DT_SC")) {
			origin_filepath = PLATFORM_PATH + "/Common/Components/";
		} else if (list.equals("BDT_Primitive_Restriction")) {
			origin_filepath = PLATFORM_PATH + "/Common/Components/";
		} else if (list.equals("BOD")) {
			origin_filepath = "/BODs/";
		} else if (list.equals("Nouns")) {
			origin_filepath = PLATFORM_PATH + "/Nouns/";
		}

		return prefix_filepath + origin_filepath;
	}


	public static final String OAGIS_RELEASE_NOTE;
	static {
		switch (OAGIS_VERSION) {
			case "10.1":
				OAGIS_RELEASE_NOTE = "Open Applications Group\n" +
						"Interface Specification XMLSchemas and Sample XML Files\n" +
						"\n" +
						"OAGIS Release 10_1\n" +
						"\n" +
						"27 June 2014\n" +
						"\n" +
						"\n" +
						"OAGIS Release 10_1 is a general availability release of OAGIS the release\n" +
						"date is 27 June 2014.\n" +
						"\n" +
						"This release is the continuation of the focus on enabling integration that\n" +
						"the Open Applications Group and its members are known.\n" +
						"\n" +
						"Please provide all feedback to the OAGI Architecture Team via the Feedback\n" +
						"Forum at: oagis@openapplications.org\n" +
						"\n" +
						"These XML reference files continue to evolve.  Please feel\n" +
						"free to use them, but check www.openapplications.org for the most\n" +
						"recent updates.\n" +
						"\n" +
						"OAGIS Release 10_1 includes:\n" +
						"\n" +
						"  - Addition of more Open Parties and Quantities from implementation feedback.\n" +
						"  - Updates to the ConfirmBOD to make easier to use.\n" +
						"  - Addtion of DocumentReferences and Attachments for PartyMaster\n" +
						"  - Support for UN/CEFACT Core Components 3.0.\n" +
						"  - Support for UN/CEFACT XML Naming and Design Rules 3.0\n" +
						"  - Support for UN/CEFACT Data Type Catalog 3.1\n" +
						"  - Support for Standalone BODs using Local elements.\n" +
						"\n" +
						"\n" +
						"NOTICE: We recommend that you install on your root directory drive as the\n" +
						"paths may be too long otherwise.\n" +
						"\n" +
						"As with all OAGIS releases OAGIS Release 10_1 contains XML Schema. To view\n" +
						"XML Schema it is recommended that you use an XML IDE, as the complete structure\n" +
						"of the Business Object Documents are not viewable from a single file.\n" +
						"\n" +
						"Note that the sample files were used to verify the XMLSchema\n" +
						"development, and do not necessarily reflect actual business\n" +
						"transactions.  In many cases,the data entered in the XML files are just\n" +
						"placeholder text.  Real-world examples for each transaction will be\n" +
						"provided as they become available. If you are interested in providing\n" +
						"real-world examples please contact oagis@openapplications.org\n" +
						"\n" +
						"Please send suggestions or bug reports to oagis@openapplications.org\n" +
						"\n" +
						"Thank you for your interest and support.\n" +
						"\n" +
						"Best Regards,\n" +
						"The Open Applications Group Architecture Council\n";
				break;
			case "10.2":
				OAGIS_RELEASE_NOTE = "Open Applications Group\n" +
						"Interface Specification XMLSchemas and Sample XML Files\n" +
						"\n" +
						"OAGIS Release 10_2  \n" +
						"\n" +
						"10 June 2016\n" +
						"\n" +
						"\n" +
						"OAGIS Release 10_2 is a general availability release of OAGIS the release\n" +
						"date is 10 June 2016. \n" +
						"\n" +
						"This release is the continuation of the focus on enabling integration that \n" +
						"the Open Applications Group and its members are known.\n" +
						"\n" +
						"Please provide all feedback to the OAGI Architecture Team via the Feedback \n" +
						"Forum at: oagis@openapplications.org\n" +
						"\n" +
						"These XML reference files continue to evolve.  Please feel\n" +
						"free to use them, but check www.openapplications.org for the most \n" +
						"recent updates.\n" +
						"\n" +
						"OAGIS Release 10_2 includes:\n" +
						"\n" +
						"  - Update Fields and Components to have a typeCode attribute in order to \n" +
						"    further qualify the use of the given use.\n" +
						"  - Update the IANACharacterSetCode Code List to include the last version \n" +
						"    from 20 Dec 2013.\n" +
						"\n" +
						"\n" +
						"NOTICE: We recommend that you install on your root directory drive as the \n" +
						"paths may be too long otherwise.\n" +
						"\t\n" +
						"As with all OAGIS releases OAGIS Release 10_2 contains XML Schema. To view \n" +
						"XML Schema it is recommended that you use an XML IDE, as the complete structure \n" +
						"of the Business Object Documents are not viewable from a single file.\n" +
						"\n" +
						"Note that the sample files were used to verify the XMLSchema \n" +
						"development, and do not necessarily reflect actual business \n" +
						"transactions.  In many cases,the data entered in the XML files are just \n" +
						"placeholder text.  Real-world examples for each transaction will be \n" +
						"provided as they become available. If you are interested in providing \n" +
						"real-world examples please contact oagis@openapplications.org\n" +
						"\n" +
						"Please send suggestions or bug reports to oagis@openapplications.org\n" +
						"\n" +
						"Thank you for your interest and support.\n" +
						"\n" +
						"Best Regards,\n" +
						"The Open Applications Group Architecture Council\n";
				break;
			default:
				throw new UnsupportedOperationException("Unsupported version: " + OAGIS_VERSION);
		}
	}

	public static final String FOREIGNKEY_ERROR_MSG = "a foreign key constraint fails";
	public static final String CANNOT_DELETE_CONTEXT_CATEGORTY = "Fail to delete. The context category is referenced by the following context schemes: ";
	public static final String CANNOT_DELETE_CONTEXT_SCHEME = "Fail to delete. Some of values of the context scheme are referenced by the following business contexts: ";
}
