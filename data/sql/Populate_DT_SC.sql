/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Currency', 'Code', 'The currency of the amount', DT_ID, 0, 1 FROM dt WHERE data_type_term ='Amount' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the binary object', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Filename', 'Code', 'The filename of the binary object', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'List', 'Identifier', 'The identification of a list of codes', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'List Agency', 'Identifier', 'The identification of the agency that manages the code list.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'List Version', 'Identifier', 'The identification of the version of the list of codes.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Time Zone', 'Code', 'The time zone to which the date time refers', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Date Time' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Scheme', 'Identifier', 'The identification of the identifier scheme.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Scheme Version', 'Identifier', 'The identification of the version of the identifier scheme', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Scheme Agency', 'Identifier', 'The identification of the agency that manages the identifier scheme', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Unit', 'Code', 'The unit of measure', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Measure' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Language', 'Code', 'The language used in the corresponding text string', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Name' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Multiplier', 'Value', 'The multiplier of the Rate. Unit. Code or Rate. Currency. Code', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Base Multiplier', 'Value', 'The multiplier of the Rate. Base Unit. Code or Rate. Base Currency. Code', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Base Unit', 'Code', 'The unit of measure of the denominator', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and dt_type = 0;

INSERT INTO dt_sc (DT_SC_GUID, Property_Term, Representation_Term, Definition, Owner_DT_ID, Min_Cardinality, Max_Cardinality) SELECT CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 'Base Currency', 'Code', 'The currency of the denominator', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and dt_type = 0;