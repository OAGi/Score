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