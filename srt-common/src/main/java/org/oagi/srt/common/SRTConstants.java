package org.oagi.srt.common;

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
	public static final String OAGI_GUID_PREFIX = "oagis-id-";

	public static final String TAB_TOP_LEVEL_ABIE_SELECT_BC = "select_bc";
	public static final String TAB_TOP_LEVEL_ABIE_CREATE_UC_BIE = "create_u_bie";
	public static final String TAB_TOP_LEVEL_ABIE_COPY_UC_BIE = "edit_bod";

	/*
	 * To choose OAGIS version
	 */
	public static final double OAGIS_VERSION = 10.2D;

	public static final String FOREIGNKEY_ERROR_MSG = "a foreign key constraint fails";
	public static final String CANNOT_DELETE_CONTEXT_CATEGORTY = "Fail to delete. The context category is referenced by the following context schemes: ";
	public static final String CANNOT_DELETE_CONTEXT_SCHEME = "Fail to delete. Some of values of the context scheme are referenced by the following business contexts: ";

	public static final String ANY_ASCCP_DEN = "Any Property. Any Structured Content";
}
