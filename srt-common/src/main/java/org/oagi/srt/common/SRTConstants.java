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

	public static final String MODEL_FOLDER_PATH = new File(DATA_PATH, "OAGIS_10_1_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_1/Model").getPath();
	public static final String BOD_FILE_PATH_01 = MODEL_FOLDER_PATH + "/Platform/2_1/BODs/";
	public static final String BOD_FILE_PATH_02 = MODEL_FOLDER_PATH + "/BODs/";

	public static final String FIELDS_XSD_FILE_PATH = MODEL_FOLDER_PATH + "/Platform/2_1/Common/Components/Fields.xsd";
	public static final String META_XSD_FILE_PATH = MODEL_FOLDER_PATH + "/Platform/2_1/Common/Components/Meta.xsd";
	public static final String BUSINESS_DATA_TYPE_XSD_FILE_PATH = MODEL_FOLDER_PATH + "/Platform/2_1/Common/DataTypes/BusinessDataType_1.xsd";
	public static final String COMPONENTS_XSD_FILE_PATH = MODEL_FOLDER_PATH + "/Platform/2_1/Common/Components/Components.xsd";
	public static final String XBT_FILE_PATH = MODEL_FOLDER_PATH + "/Platform/2_1/Common/DataTypes/XMLSchemaBuiltinType_1.xsd";

	public static final String BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_1_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_1").getPath();

	public static String filepath(String list) {
		String prefix_filepath = MODEL_FOLDER_PATH;
		String origin_filepath = "/Platform/2_1/Common/";
		if (list.equals("AgencyID")) {
			origin_filepath = "/Platform/2_1/Common/IdentifierScheme/";
		} else if (list.equals("CodeList")) {
			origin_filepath = "/Platform/2_1/Common/CodeLists/";
		} else if (list.equals("DT")) {
			origin_filepath = "/Platform/2_1/Common/Components/";
		} else if (list.equals("DT_SC")) {
			origin_filepath = "/Platform/2_1/Common/Components/";
		} else if (list.equals("BDT_Primitive_Restriction")) {
			origin_filepath = "/Platform/2_1/Common/Components/";
		} else if (list.equals("BOD")) {
			origin_filepath = "/BODs/";
		} else if (list.equals("Nouns")) {
			origin_filepath = "/Platform/2_1/Nouns/";
		}

		return prefix_filepath + origin_filepath;
	}

	public static final String FOREIGNKEY_ERROR_MSG = "a foreign key constraint fails";
	public static final String CANNOT_DELETE_CONTEXT_CATEGORTY = "Fail to delete. The context category is referenced by the following context schemes: ";
	public static final String CANNOT_DELETE_CONTEXT_SCHEME = "Fail to delete. Some of values of the context scheme are referenced by the following business contexts: ";
}
