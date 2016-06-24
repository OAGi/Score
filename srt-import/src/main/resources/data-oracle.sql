-- 3.1.1.2  Create an OAGIS User
INSERT INTO APP_USER (APP_USER_ID, LOGIN_ID, PASSWORD, NAME, ORGANIZATION, OAGIS_DEVELOPER_INDICATOR) VALUES
  (APP_USER_ID_SEQ.NEXTVAL, 'oagis', 'oagis', 'Open Applications Group Developer', 'Open Applications Group', 1);

COMMIT;


-- 3.1.1.3	Populate the namespace table
INSERT INTO NAMESPACE (NAMESPACE_ID, URI, PREFIX, DESCRIPTION, 
                       IS_STD_NMSP, OWNER_USER_ID, CREATED_BY, LAST_UPDATED_BY, 
                       CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP)
  SELECT
    NAMESPACE_ID_SEQ.NEXTVAL, 'http://www.openapplications.org/oagis/10', '', 'OAGIS release 10 namespace',
    APP_USER_ID, APP_USER_ID, APP_USER_ID, APP_USER_ID,
    TIMESTAMP '2014-06-27 00:00:00 -05:00',
    TIMESTAMP '2014-06-27 00:00:00 -05:00'
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

COMMIT;


-- 3.1.1.4	Populate the release table
INSERT INTO RELEASE (RELEASE_ID, RELEASE_NUM, NAMESPACE_ID, RELEASE_NOTE) VALUES
  (RELEASE_ID_SEQ.NEXTVAL, '10.1', 1,
   'Open Applications Group\nInterface Specification XMLSchemas and Sample XML Files\n\nOAGIS Release 10_1\n\n27 June 2014\n\n\nOAGIS Release 10_1 is a general availability release of OAGIS the release\ndate is 27 June 2014.\n\nThis release is the continuation of the focus on enabling integration that\nthe Open Applications Group and its members are known.\n\nPlease provide all feedback to the OAGI Architecture Team via the Feedback\nForum at: oagis@openapplications.org\n\nThese XML reference files continue to evolve.  Please feel\nfree to use them, but check www.openapplications.org for the most\nrecent updates.\n\nOAGIS Release 10_1 includes:\n\n  - Addition of more Open Parties and Quantities from implementation feedback.\n  - Updates to the ConfirmBOD to make easier to use.\n  - Addtion of DocumentReferences and Attachments for PartyMaster\n  - Support for UN/CEFACT Core Components 3.0.\n  - Support for UN/CEFACT XML Naming and Design Rules 3.0\n  - Support for UN/CEFACT Data Type Catalog 3.1\n  - Support for Standalone BODs using Local elements.\n\n\nNOTICE: We recommend that you install on your root directory drive as the\npaths may be too long otherwise.\n\nAs with all OAGIS releases OAGIS Release 10_1 contains XML Schema. To view\nXML Schema it is recommended that you use an XML IDE, as the complete structure\nof the Business Object Documents are not viewable from a single file.\n\nNote that the sample files were used to verify the XMLSchema\ndevelopment, and do not necessarily reflect actual business\ntransactions.  In many cases,the data entered in the XML files are just\nplaceholder text.  Real-world examples for each transaction will be\nprovided as they become available. If you are interested in providing\nreal-world examples please contact oagis@openapplications.org\n\nPlease send suggestions or bug reports to oagis@openapplications.org\n\nThank you for your interest and support.\n\nBest Regards,\nThe Open Applications Group Architecture Council\n');

COMMIT;


-- 3.1.1.5	Populate CDT data
-- 3.1.1.5.1	Populate the xbt table
INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID) VALUES
  (XBT_ID_SEQ.NEXTVAL, 'any type', 'xsd:anyType', NULL);

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'any simple type', 'xsd:anySimpleType', XBT_ID
  FROM XBT
  WHERE NAME = 'any type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'duration', 'xsd:duration', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'date time', 'xsd:dateTime', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'time', 'xsd:time', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'date', 'xsd:date', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'gregorian year month', 'xsd:gYearMonth', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'gregorian year', 'xsd:gYear', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'gregorian month day', 'xsd:gMonthDay', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'gregorian day', 'xsd:gDay', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'gregorian month', 'xsd:gMonth', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'string', 'xsd:string', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'normalized string', 'xsd:normalizedString', XBT_ID
  FROM XBT
  WHERE NAME = 'string';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'token', 'xsd:token', XBT_ID
  FROM XBT
  WHERE NAME = 'normalized string';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'boolean', 'xsd:boolean', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'base64 binary', 'xsd:base64Binary', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'hex binary', 'xsd:hexBinary', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'float', 'xsd:float', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'decimal', 'xsd:decimal', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'integer', 'xsd:integer', XBT_ID
  FROM XBT
  WHERE NAME = 'decimal';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'non negative integer', 'xsd:nonNegativeInteger', XBT_ID
  FROM XBT
  WHERE NAME = 'integer';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'positive integer', 'xsd:positiveInteger', XBT_ID
  FROM XBT
  WHERE NAME = 'non negative integer';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'double', 'xsd:double', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'any uri', 'xsd:anyURI', XBT_ID
  FROM XBT
  WHERE NAME = 'any simple type';

INSERT INTO XBT (XBT_ID, NAME, BUILTIN_TYPE, SUBTYPE_OF_XBT_ID)
  SELECT
    XBT_ID_SEQ.NEXTVAL, 'xbt boolean true or false', 'xbt_BooleanTrueFalseType', XBT_ID
  FROM XBT
  WHERE NAME = 'boolean';

COMMIT;


-- 3.1.1.5.2	Populate the cdt_pri table
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Binary');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Boolean');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Decimal');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Double');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Float');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Integer');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'NormalizedString');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'String');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'TimeDuration');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'TimePoint');
INSERT INTO CDT_PRI (CDT_PRI_ID, NAME) VALUES (CDT_PRI_ID_SEQ.NEXTVAL, 'Token');

COMMIT;


-- 3.1.1.5.3	Populate CDTs in the dt table
INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-3bfbbc07cffc47a886496961b0f6b292', 0, '1.0', NULL,
    'Amount', NULL, NULL, 'Amount. Type',
    'Amount. Content',
    'CDT V3.1. An amount is a number of monetary units specified in a currency.',
    'A number of monetary units.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-689935c2c40445dab6ca3d19043cf71d', 0, '1.0', NULL,
    'Binary Object', NULL, NULL, 'Binary Object. Type', 'Binary Object. Content',
    'CDT V3.1. A binary object is a sequence of binary digits (bits).',
    'A finite sequence of binary digits (bits).', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-efd26bf9a65b429294356dcc9d22c4fe', 0, '1.0', NULL,
    'Code', NULL, NULL, 'Code. Type', 'Code. Content',
    'CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences); and symbols. It represents a definitive value,\na method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.',
    'A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-9ff8dc0294a24f7292b9fe6a8ab3a3eb', 0, '1.0', NULL,
    'Date', NULL, NULL, 'Date. Type', 'Date. Content',
    'CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.',
    'The particular point in the progression of date.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-c15c79d7332a47939f717943e5ed9e67', 0, '1.0', NULL,
    'Date Time', NULL, NULL, 'Date Time. Type', 'Date Time. Content',
    'CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of\nsecond.',
    'The particular date and time point in the progression of time.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-fc2c841359814eb08104f29e02673f65', 0, '1.0', NULL,
    'Duration', NULL, NULL, 'Duration. Type', 'Duration. Content',
    'CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,\nWeek, Day) and Hours, Minutes or Seconds.',
    'The particular representation of duration.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-a308345fa0614437b2593fd7cb0d9f20', 0, '1.0', NULL,
    'Graphic', NULL, NULL, 'Graphic. Type', 'Graphic. Content',
    'CDT V3.1. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).',
    'A finite sequence of binary digits (bits) for graphics.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-6b35560a5ca14a1f801e9d32ff5eb502', 0, '1.0', NULL,
    'Identifier', NULL, NULL, 'Identifier. Type', 'Identifier. Content',
    'CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an\nagency.',
    'A character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-df7fdf7c96394523a533b51854544269', 0, '1.0', NULL,
    'Indicator', NULL, NULL, 'Indicator. Type', 'Indicator. Content',
    'CDT V3.1. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.',
    'The value of the Indicator.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-e9f34d8fe0a34d37aca628b612c2b3ce', 0, '1.0', NULL,
    'Measure', NULL, NULL, 'Measure. Type', 'Measure. Content',
    'CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.',
    'The numeric value determined by measuring an object.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-8710513002c241dca953ddee1695ff1f', 0, '1.0', NULL,
    'Name', NULL, NULL, 'Name. Type', 'Name. Content',
    'CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.',
    'A word or phrase that represents a designation of a person, place, thing or concept.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-d90b1011eb27451e9fedccbe7af51e76', 0, '1.0', NULL,
    'Number', NULL, NULL, 'Number. Type', 'Number. Content',
    'CDT V3.1. A mathematical number that is assigned or is determined by calculation.',
    'Mathematical number that is assigned or is determined by calculation.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-6f55788519e94b9fb41c371d5c98c0ca', 0, '1.0', NULL,
    'Ordinal', NULL, NULL, 'Ordinal. Type', 'Ordinal. Content',
    'CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.',
    'An assigned mathematical number that represents order or sequence', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-32ef31dcc7d94cc1ac34dd0067a626ed', 0, '1.0', NULL,
    'Percent', NULL, NULL, 'Percent. Type', 'Percent. Content',
    'CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.',
    'Numeric information that is assigned or is determined by percent.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-1ed683abe54f496f9ba86f7ae35b703d', 0, '1.0', NULL,
    'Picture', NULL, NULL, 'Picture. Type', 'Picture. Content',
    'CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).',
    'A finite sequence of binary digits (bits) for pictures.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-d05b0412061b477a82bfa4e14c2d5216', 0, '1.0', NULL,
    'Quantity', NULL, NULL, 'Quantity. Type', 'Quantity. Content',
    'CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.',
    'A counted number of non-monetary units possibly including fractions.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-4238ca186cf6482c94b1b95fd18f2305', 0, '1.0', NULL,
    'Rate', NULL, NULL, 'Rate. Type', 'Rate. Content',
    'CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.',
    'The numerical value of the rate.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-783f8d5944054871b574e75da7dd258a', 0, '1.0', NULL,
    'Ratio', NULL, NULL, 'Ratio. Type', 'Ratio. Content',
    'CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a\nquotient showing the number of times one value contains or is contained within the other, or as a proportion.',
    'The quotient or proportion between two independent quantities of the same unit of measure or currency.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-b75fa46a2b884d57a46a4934cbd208c7', 0, '1.0', NULL,
    'Sound', NULL, NULL, 'Sound. Type', 'Sound. Content',
    'CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).',
    'A finite sequence of binary digits (bits) for sounds.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-e5a81582d7604fc2a441d57fd6cdf5ab', 0, '1.0', NULL,
    'Text', NULL, NULL, 'Text. Type', 'Text. Content',
    'CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.',
    'A character string generally in the form of words of a language.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-e80da4c59dbb441b915d70255427f33e', 0, '1.0', NULL,
    'Time', NULL, NULL, 'Time. Type', 'Time. Content',
    'CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.',
    'The particular point in the progression of time.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-a06e726d98274399bd6c76fe82a5fbdc', 0, '1.0', NULL,
    'Value', NULL, NULL, 'Value. Type', 'Value. Content',
    'CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.',
    'Numeric information that is assigned or is determined by value.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

INSERT INTO DT (DT_ID, GUID, TYPE, VERSION_NUM, PREVIOUS_VERSION_DT_ID,
                DATA_TYPE_TERM, QUALIFIER, BASED_DT_ID, DEN, CONTENT_COMPONENT_DEN,
                DEFINITION,
                CONTENT_COMPONENT_DEFINITION, REVISION_DOC, STATE,
                CREATED_BY, OWNER_USER_ID, LAST_UPDATED_BY, CREATION_TIMESTAMP, LAST_UPDATE_TIMESTAMP,
                REVISION_NUM, REVISION_TRACKING_NUM, REVISION_ACTION, RELEASE_ID, CURRENT_BDT_ID, IS_DEPRECATED)
  SELECT
    DT_ID_SEQ.NEXTVAL, 'oagis-id-ce7635625e75420d973bcda56bb80a9f', 0, '1.0', NULL,
    'Video', NULL, NULL, 'Video. Type', 'Video. Content',
    'CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).',
    'A finite sequence of binary digits (bits) for videos.', NULL, 3,
    APP_USER_ID, APP_USER_ID, APP_USER_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    0, 0, NULL, (SELECT RELEASE_ID FROM RELEASE WHERE RELEASE_NUM = '10.1'), NULL, 0
  FROM APP_USER
  WHERE LOGIN_ID = 'oagis';

COMMIT;


-- 3.1.1.5.4	Populate the cdt_awd_pri table
INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Duration' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimeDuration'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Indicator' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Boolean'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ordinal' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Time' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'), 0);

INSERT INTO CDT_AWD_PRI (CDT_AWD_PRI_ID, CDT_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0),
   (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'), 1);

COMMIT;


-- 3.1.1.5.5	Populate the cdt_awd_pri_xps_type_map
INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:base64Binary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:hexBinary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:normalizedString'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:date'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:time'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gYearMonth'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gYear'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gMonthDay'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gDay'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gMonth'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:dateTime'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:date'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:time'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gYearMonth'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gYear'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gMonthDay'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gDay'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:gMonth'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Duration' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimeDuration'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Duration' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimeDuration'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:duration'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:base64Binary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:hexBinary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:normalizedString'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Indicator' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Boolean'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xbt_BooleanTrueFalseType'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:normalizedString'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Number' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ordinal' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ordinal' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ordinal' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Percent' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:base64Binary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:hexBinary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Ratio' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:base64Binary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:hexBinary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:normalizedString'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Time' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'TimePoint'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:time'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Decimal'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:decimal'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:double'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Double'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Float'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:float'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:integer'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:positiveInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Integer'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:nonNegativeInteger'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'NormalizedString'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:normalizedString'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'String'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:string'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Value' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Token'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:token'));


INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:base64Binary'));

INSERT INTO CDT_AWD_PRI_XPS_TYPE_MAP (CDT_AWD_PRI_XPS_TYPE_MAP_ID, CDT_AWD_PRI_ID, XBT_ID) VALUES
  (CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ.NEXTVAL,
   (SELECT CDT_AWD_PRI_ID FROM CDT_AWD_PRI WHERE CDT_ID =
                                                 (SELECT DT_ID FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0)
                                                 AND
                                                 (CDT_PRI_ID = (SELECT CDT_PRI_ID FROM CDT_PRI WHERE NAME = 'Binary'))),
   (SELECT XBT_ID FROM XBT WHERE BUILTIN_TYPE = 'xsd:hexBinary'));

COMMIT;


-- 3.1.1.5.6	Populate CDTs’ supplementary component in the dt_sc table
INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-01497a9692d14c48afa9c242fd1e3155', 'Currency', 'Code',
    'The currency of the amount', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Amount' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-8a5de05a440d4f8892f64365c5274320', 'MIME', 'Code',
   'The Multipurpose Internet Mail Extensions(MIME) media type of the binary object', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-b7c12169dfe74e3ca7a9801715fe9046', 'Character Set', 'Code',
   'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-d1ac8d694e014a3e847ee786ac119c17', 'Filename', 'Name',
   'The filename of the binary object', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Binary Object' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-5154262bf5d04819b6cdb925c2f3bcec', 'List', 'Identifier',
   'The identification of a list of codes', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-e029e54603b046f9934b07f2b6b0f547', 'List Agency', 'Identifier',
   'The identification of the agency that manages the code list.', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-bca59a04d77f425282f0c8c2585042fb', 'List Version', 'Identifier',
   'The identification of the version of the list of codes.', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Code' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-a2773a52c35c45b5915bcb5e3d75e1e6', 'Time Zone', 'Code',
   'The time zone to which the date time refers', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-e4928d370c044f46a9745c8519d268ca', 'Daylight Saving', 'Indicator',
   'The indication of whether or not this Date Time is in daylight saving', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Date Time' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-0aa9a43f2ee847c2b1669bd6a398addc', 'MIME', 'Code',
   'The Multipurpose Internet Mail Extensions (MIME) media type of the graphic.', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-e2c9956699c946d597663f462e55393d', 'Character Set', 'Code',
   'The character set of the graphic if the Multipurpose Internet Mail Extensions (MIME) type is text.', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-2aa51dda93d84142902314829e3690b6', 'Filename', 'Name',
   'The filename of the graphic', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Graphic' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-1e47a8f55c9d4e82a52857d5e6346a60', 'Scheme', 'Identifier',
   'The identification of the identifier scheme.', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-f411b50cf37144319bac82ff3d19878c', 'Scheme Version', 'Identifier',
   'The identification of the version of the identifier scheme', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-ca9936a202944ee7aac2ff81570b4cb9', 'Scheme Agency', 'Identifier',
   'The identification of the agency that manages the identifier scheme', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Identifier' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-942bdc150d3546eca25814553e83e3e6', 'Unit', 'Code',
   'The unit of measure', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Measure' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-88c3ae617a7b4e5596c4b657207de059', 'Language', 'Code',
   'The language used in the corresponding text string', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Name' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-a9671a29fe784fd0a5ef6db7b3b5554f', 'MIME', 'Code',
   'The Multipurpose Internet Mail Extensions(MIME) media type of the picture', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-a4242ce5ef014e2bb8a779648896e81f', 'Character Set', 'Code',
   'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-699804a9ed244848838527e44427ad9f', 'Filename', 'Name',
   'The filename of the picture', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Picture' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-64adaa84e5bd437ea4f3f924e5fd6d84', 'Unit', 'Code',
   'The unit of measure in which the quantity is expressed', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Quantity' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-7d044cbaaeee4869bc1374e1479527b5', 'Multiplier', 'Value',
   'The multiplier of the Rate. Unit. Code or Rate. Currency. Code', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-2fa74710524d4d3096c54cdbec1f97eb', 'Unit', 'Code',
   'The unit of measure of the numerator', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-90d9371cdebc40ed84993349e67fcc65', 'Currency', 'Code',
   'The currency of the numerator', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-0e9a4909e55c4a8099f571c0f2211b9a', 'Base Multiplier', 'Value',
   'The multiplier of the Rate. Base Unit. Code or Rate. Base Currency. Code', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-44a50dbd3991450b888d2f4c5b02479d', 'Base Unit', 'Code',
   'The unit of measure of the denominator', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-5a052ccfdd2f4821b2a5cc0e10f17d78', 'Base Currency', 'Code',
   'The currency of the denominator', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Rate' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-e30bcb85d0804fba8b9134e4807bef78', 'MIME', 'Code',
   'The Multipurpose Internet Mail Extensions(MIME) media type of the sound', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-592cb12352174039adf23f4778513d68', 'Character Set', 'Code',
   'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-5d08fe4397404aa8a9e2dce3d8443172', 'Filename', 'Name',
   'The filename of the sound', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Sound' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-ecef05f632a84f658a4012ccb4756a80', 'Language', 'Code',
   'The language used in the corresponding text string', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Text' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-bedf3bf5c5dd4dd1981ed6e9bbdb716f', 'MIME', 'Code',
   'The Multipurpose Internet Mail Extensions(MIME) media type of the video', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-2332e1819b294faa8837e9e86f4890b7', 'Character Set', 'Code',
   'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0;

INSERT INTO DT_SC (DT_SC_ID, GUID, PROPERTY_TERM, REPRESENTATION_TERM,
                   DEFINITION, OWNER_DT_ID, MIN_CARDINALITY, MAX_CARDINALITY)
  SELECT
    DT_SC_ID_SEQ.NEXTVAL, 'oagis-id-3f192e1c1b724debb53a178bd2a2a497', 'Filename', 'Name',
   'The filename of the video', DT_ID, 0, 1
  FROM DT WHERE DATA_TYPE_TERM = 'Video' AND TYPE = 0;

COMMIT;


-- 3.1.1.5.7	Populate the cdt_sc_awd_pri table
INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Daylight Saving'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Boolean'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Double'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Float'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Decimal'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Double'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Float'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Integer'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'NormalizedString'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'String'), 0);

INSERT INTO CDT_SC_AWD_PRI (CDT_SC_AWD_PRI_ID, CDT_SC_ID, CDT_PRI_ID, IS_DEFAULT) VALUES
  (CDT_SC_AWD_PRI_ID_SEQ.NEXTVAL,
   (SELECT DT_SC_ID
    FROM DT_SC
      JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
    WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename'),
   (SELECT CDT_PRI_ID
    FROM CDT_PRI
    WHERE NAME = 'Token'), 1);

COMMIT;


-- 3.1.1.5.8	Populate the cdt_sc_awd_pri_xps_type_map table
INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Amount' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Binary Object' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Code' AND DT_SC.PROPERTY_TERM = 'List Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Time Zone') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Date Time' AND DT_SC.PROPERTY_TERM = 'Daylight Saving') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Boolean')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'xbt boolean true or false'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Graphic' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Version') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Identifier' AND DT_SC.PROPERTY_TERM = 'Scheme Agency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Measure' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Name' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Picture' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Quantity' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Decimal')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'decimal'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Double')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'double'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Double')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'float'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Float')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'float'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'non negative integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'positive integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Decimal')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'decimal'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Double')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'double'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Double')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'float'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Float')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'float'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'non negative integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Multiplier') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Integer')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'positive integer'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Unit') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Rate' AND DT_SC.PROPERTY_TERM = 'Base Currency') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Sound' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Text' AND DT_SC.PROPERTY_TERM = 'Language') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'MIME') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Character Set') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'NormalizedString')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'normalized string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'String')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'string'));

INSERT INTO CDT_SC_AWD_PRI_XPS_TYPE_MAP (CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, CDT_SC_AWD_PRI, XBT_ID) VALUES
  (CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ.NEXTVAL,
   (SELECT CDT_SC_AWD_PRI_ID
    FROM CDT_SC_AWD_PRI
    WHERE CDT_SC_ID = (SELECT DT_SC_ID
                       FROM DT_SC
                         JOIN DT ON DT.DT_ID = DT_SC.OWNER_DT_ID
                       WHERE DT.DATA_TYPE_TERM = 'Video' AND DT_SC.PROPERTY_TERM = 'Filename') AND
          CDT_PRI_ID = (SELECT CDT_PRI_ID
                        FROM CDT_PRI
                        WHERE NAME = 'Token')),
   (SELECT XBT_ID
    FROM XBT
    WHERE NAME = 'token'));

COMMIT;