/**
 *
 * @author Nasif Sikder
 * @author Jaehun Lee
 * @author Yunsu Lee
 * @version 1.0
 *
 */

/*
INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Daylight Saving'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Boolean'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Ratio' and dt_sc.Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;
*/

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Daylight Saving'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Boolean'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) SELECT (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1;


