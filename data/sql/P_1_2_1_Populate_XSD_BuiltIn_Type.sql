/*
insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) values ('any type', 'xsd:anyType', null);

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'any simple type', 'xsd:anySimpleType', xsd_builtin_type_id from xsd_builtin_type where name = 'any type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'duration', 'xsd:duration', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'date time', 'xsd:dateTime', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'time', 'xsd:time', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'date', 'xsd:date', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'gregorian year month', 'xsd:gYearMonth', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'gregorian year', 'xsd:gYear', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'gregorian month day', 'xsd:gMonthDay', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'gregorian day', 'xsd:gDay', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'gregorian month', 'xsd:gMonth', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'string', 'xsd:string', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'normalized string', 'xsd:normalizedString', xsd_builtin_type_id from xsd_builtin_type where name = 'string';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'token', 'xsd:token', xsd_builtin_type_id from xsd_builtin_type where name = 'normalized string';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'boolean', 'xsd:boolean', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'base64 binary', 'xsd:base64Binary', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'hex binary', 'xsd:hexBinary', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'float', 'xsd:float', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'decimal', 'xsd:decimal', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'integer', 'xsd:integer', xsd_builtin_type_id from xsd_builtin_type where name = 'decimal';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'non negative integer', 'xsd:nonNegativeInteger', xsd_builtin_type_id from xsd_builtin_type where name = 'integer';
	
insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'positive integer', 'xsd:positiveInteger', xsd_builtin_type_id from xsd_builtin_type where name = 'non negative integer';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'double', 'xsd:double', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'any uri', 'xsd:anyURI', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

*/

insert into xbt (name, builtIn_type, subtype_of_xbt_id) values ('any type', 'xsd:anyType', null);

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'any simple type', 'xsd:anySimpleType', xbt_id from xbt where name = 'any type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'duration', 'xsd:duration', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'date time', 'xsd:dateTime', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'time', 'xsd:time', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'date', 'xsd:date', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'gregorian year month', 'xsd:gYearMonth', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'gregorian year', 'xsd:gYear', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'gregorian month day', 'xsd:gMonthDay', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'gregorian day', 'xsd:gDay', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'gregorian month', 'xsd:gMonth', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'string', 'xsd:string', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'normalized string', 'xsd:normalizedString', xbt_id from xbt where name = 'string';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'token', 'xsd:token', xbt_id from xbt where name = 'normalized string';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'boolean', 'xsd:boolean', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'base64 binary', 'xsd:base64Binary', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'hex binary', 'xsd:hexBinary', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'float', 'xsd:float', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'decimal', 'xsd:decimal', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'integer', 'xsd:integer', xbt_id from xbt where name = 'decimal';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'non negative integer', 'xsd:nonNegativeInteger', xbt_id from xbt where name = 'integer';
	
insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'positive integer', 'xsd:positiveInteger', xbt_id from xbt where name = 'non negative integer';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'double', 'xsd:double', xbt_id from xbt where name = 'any simple type';

insert into xbt (name, builtIn_type, subtype_of_xbt_id) 
	Select 'any uri', 'xsd:anyURI', xbt_id from xbt where name = 'any simple type';