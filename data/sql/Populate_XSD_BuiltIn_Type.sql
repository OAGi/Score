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
	Select 'positive integer', 'xsd:positiveeInteger', xsd_builtin_type_id from xsd_builtin_type where name = 'non negative integer';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'double', 'xsd:double', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

insert into xsd_builtin_type (name, builtin_type, Subtype_Of_XSD_BuiltIn_Type_ID) 
	Select 'any uri', 'xsd:anyURI', xsd_builtin_type_id from xsd_builtin_type where name = 'any simple type';

