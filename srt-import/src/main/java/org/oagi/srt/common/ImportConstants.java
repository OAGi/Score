package org.oagi.srt.common;

import java.io.File;
import java.io.IOException;

import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;

public class ImportConstants {

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

    public static final String BASE_DATA_PATH;
    public static final String PLATFORM_PATH;
    public static final String CODELIST_CHARACTER_SET_CODE_IANA_FILENAME;

    public static final String IDENTIFIER_SCHEME_AGENCY_IDENTIFICATION_FILENAME;
    public static final String AGENCY_IDENTIFICATION_NAME;
    public static final String AGENCY_IDENTIFICATION_LIST_ID;
    public static final String AGENCY_IDENTIFICATION_VERSION_ID;

    static {
        if (OAGIS_VERSION == 10.1D) {
            BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_1_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_1").getPath();
            PLATFORM_PATH = "/Platform/2_1";
            CODELIST_CHARACTER_SET_CODE_IANA_FILENAME = "CodeList_CharacterSetCode_IANA_20070514";

            IDENTIFIER_SCHEME_AGENCY_IDENTIFICATION_FILENAME = "IdentifierScheme_AgencyIdentification_3055_D08B";
            AGENCY_IDENTIFICATION_NAME = "clm63055D08B_AgencyIdentification";
            AGENCY_IDENTIFICATION_LIST_ID = "3055";
            AGENCY_IDENTIFICATION_VERSION_ID = "D08B";
        } else if (OAGIS_VERSION == 10.2D) {
            BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_2_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_2").getPath();
            PLATFORM_PATH = "/Platform/2_2";
            CODELIST_CHARACTER_SET_CODE_IANA_FILENAME = "CodeList_CharacterSetCode_IANA_20131220";

            IDENTIFIER_SCHEME_AGENCY_IDENTIFICATION_FILENAME = "IdentifierScheme_AgencyIdentification_3055_D08B";
            AGENCY_IDENTIFICATION_NAME = "clm63055D08B_AgencyIdentification";
            AGENCY_IDENTIFICATION_LIST_ID = "3055";
            AGENCY_IDENTIFICATION_VERSION_ID = "D08B";
        } else if (OAGIS_VERSION == 10.3D) {
            BASE_DATA_PATH = new File(DATA_PATH, "OAGIS_10_3_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_3").getPath();
            PLATFORM_PATH = "/Platform/2_3";
            CODELIST_CHARACTER_SET_CODE_IANA_FILENAME = "CodeList_CharacterSetCode_IANA_20131220";

            IDENTIFIER_SCHEME_AGENCY_IDENTIFICATION_FILENAME = "IdentifierScheme_AgencyIdentification_3055_D16B";
            AGENCY_IDENTIFICATION_NAME = "clm63055D16B_AgencyIdentification";
            AGENCY_IDENTIFICATION_LIST_ID = "3055";
            AGENCY_IDENTIFICATION_VERSION_ID = "D16B";
        } else {
            throw new UnsupportedOperationException("Unsupported version: " + OAGIS_VERSION);
        }
    }

    public static final String MODEL_FOLDER_PATH = BASE_DATA_PATH + "/Model";
    public static final String BOD_FILE_PATH_01 = MODEL_FOLDER_PATH + PLATFORM_PATH + "/BODs/";
    public static final String BOD_FILE_PATH_02 = MODEL_FOLDER_PATH + "/BODs/";
    public static final String NOUN_FILE_PATH_01 = MODEL_FOLDER_PATH + "/Nouns/";
    public static final String NOUN_FILE_PATH_02 = MODEL_FOLDER_PATH + PLATFORM_PATH + "/Nouns/";

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
        if (OAGIS_VERSION == 10.1D) {
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
        } else if (OAGIS_VERSION == 10.2D) {
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
        } else if (OAGIS_VERSION == 10.3D) {
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
        } else {
            throw new UnsupportedOperationException("Unsupported version: " + OAGIS_VERSION);
        }
    }

}
