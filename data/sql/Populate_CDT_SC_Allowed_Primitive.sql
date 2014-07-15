/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'MIME'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Character Set'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Filename'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'List Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Time Zone'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Version'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Scheme Agency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Language'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Decimal'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Double'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Float'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Multiplier'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Integer'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Unit'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'NormalizedString'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'String'), 0;

INSERT INTO cdt_sc_allowed_primitive (CDT_SC_ID, CDT_Primitive_ID, isDefault) SELECT (SELECT DT_SC_ID FROM dt_sc WHERE Property_Term = 'Base Currency'), (SELECT CDT_Primitive_ID FROM cdt_primitive WHERE Name = 'Token'), 1;