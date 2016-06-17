insert into app_user (login_id, password, name, organization, oagis_developer_indicator) values ('oagis', 'oagis','Open Applications Group Developer', 'Open Applications Group', '1');
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
	
insert into cdt_pri (name) values ('Binary');
insert into cdt_pri (name) values ('Boolean');
insert into cdt_pri (name) values ('Decimal'); 
insert into cdt_pri (name) values ('Double'); 
insert into cdt_pri (name) values ('Float');
insert into cdt_pri (name) values ('Integer');
insert into cdt_pri (name) values ('NormalizedString');
insert into cdt_pri (name) values ('String');
insert into cdt_pri (name) values ('TimeDuration');
insert into cdt_pri (name) values ('TimePoint');
insert into cdt_pri (name) values ('Token');

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Amount', 'Amount. Type', 'Amount. Content', 'CDT V3.1. An amount is a number of monetary units specified in a currency.', 'A number of monetary units.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Binary Object', 'Binary Object. Type', 'Binary Object. Content', 'CDT V3.1. A binary object is a sequence of binary digits (bits).', 'A finite sequence of binary digits (bits).', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Code', 'Code. Type', 'Code. Content', 'CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences), and symbols. It represents a definitive value,
a method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.', 'A character string (letters, figures or symbols) that for brevity
and/or language independence may be used to represent or
replace a definitive value or text of an attribute.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Date', 'Date. Type', 'Date. Content', 'CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.', 'The particular point in the progression of date.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Date Time', 'Date Time. Type', 'Date Time. Content', 'CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of
second.', 'The particular date and time point in the progression of time.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Duration', 'Duration. Type', 'Duration. Content', 'CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,
Week, Day) and Hours, Minutes or Seconds.', 'The particular representation of duration.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Graphic', 'Graphic. Type', 'Graphic. Content', 'CDT V3.. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).', 'A finite sequence of binary digits (bits) for graphics.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Identifier', 'Identifier. Type', 'Identifier. Content', 'CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an
agency.', 'A character string used to uniquely identify one instance of
an object within an identification scheme that is managed
by an agency.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Indicator', 'Indicator. Type', 'Indicator. Content', 'CDT V3.. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.', 'The value of the Indicator.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Measure', 'Measure. Type', 'Measure. Content', 'CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.', 'The numeric value determined by measuring an object.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Name', 'Name. Type', 'Name. Content', 'CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.', 'A word or phrase that represents a designation of a person, place, thing or concept.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Number', 'Number. Type', 'Number. Content', 'CDT V3.1. A mathematical number that is assigned or is determined by calculation.', 'Mathematical number that is assigned or is determined by calculation.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Ordinal', 'Ordinal. Type', 'Ordinal. Content', 'CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.', 'An assigned mathematical number that represents order or sequence', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Percent', 'Percent. Type', 'Percent. Content', 'CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.', 'Numeric information that is assigned or is determined by percent.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Picture', 'Picture. Type', 'Picture. Content', 'CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).', 'A finite sequence of binary digits (bits) for pictures.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Quantity', 'Quantity. Type', 'Quantity. Content', 'CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.', 'A counted number of non-monetary units possibly including
fractions.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Rate', 'Rate. Type', 'Rate. Content', 'CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.', 'The numerical value of the rate.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Ratio', 'Ratio. Type', 'Ratio. Content', 'CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a
quotient showing the number of times one value contains or is contained within the other, or as a proportion.', 'The quotient or proportion between two independent quantities of the
same unit of measure or currency.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Sound', 'Sound. Type', 'Sound. Content', 'CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).', 'A finite sequence of binary digits (bits) for
sounds.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Text', 'Text. Type', 'Text. Content', 'CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.', 'A character string generally in the form of words of a
language.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Time', 'Time. Type', 'Time. Content', 'CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.', 'The particular point in the progression of time.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Value', 'Value. Type', 'Value. Content', 'CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.', 'Numeric information that is assigned or is determined by
value.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num, data_type_term, den, content_component_den, definition, content_component_definition, revision_doc, state, created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, revision_num, revision_tracking_num, revision_action, is_deprecated) 
	select concat('oagis-id-',sys_guid()), '0', '1.0', 'Video', 'Video. Type', 'Video. Content', 'CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).', 'A finite sequence of binary digits (bits) for videos.', '', '3', app_user_id, app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, '1', '0' from app_user where login_id = 'oagis' ;
	
Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 


Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Binary Object' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Binary'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'NormalizedString'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Token'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Date' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'TimePoint'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Date Time' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'TimePoint'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Duration' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'TimeDuration'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Graphic' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Binary'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'NormalizedString'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Token'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Indicator' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Boolean'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 


Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0'); 


Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'NormalizedString'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Token'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ordinal' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 


Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Picture' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Binary'), '1'); 


Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0');
 
Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0');
 
Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0');
 
Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Sound' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Binary'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'NormalizedString'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Token'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Time' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'TimePoint'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Decimal'), '1'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Double'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Float'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default)  
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Integer'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'NormalizedString'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'String'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Token'), '0'); 

Insert into cdt_awd_pri (cdt_id, cdt_pri_id, is_default) 
	values ((select DT_ID from dt where Data_Type_Term = 'Video' and Type = 0), (select cdt_pri_id from cdt_pri where name = 'Binary'), '1'); 

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Binary Object' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Binary Object' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:hexBinary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Code' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Code' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Code' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Token'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:date'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:time'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gYearMonth'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gYear'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gMonthDay'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gDay'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gMonth'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:dateTime'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:date'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:time'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gYearMonth'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gYear'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gMonthDay'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gDay'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Date Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:gMonth'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Duration' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimeDuration'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Duration' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimeDuration'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:duration'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Graphic' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Graphic' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:hexBinary'));


Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Identifier' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Identifier' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Identifier' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Token'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Indicator' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Boolean'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:boolean'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Name' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Name' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Name' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Token'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Picture' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Picture' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:hexBinary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Sound' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Sound' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:hexBinary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Text' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Text' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Text' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Token'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Time' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'TimePoint'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:time'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:string'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Token'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:token'));


Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Video' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Video' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:hexBinary'));

INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Currency', 'Code', 'The currency of the amount', DT_ID, 0, 1 FROM dt WHERE data_type_term ='Amount' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the binary object', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary Object' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary Object' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Filename', 'Name', 'The filename of the binary object', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Binary Object' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'List', 'Identifier', 'The identification of a list of codes', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'List Agency', 'Identifier', 'The identification of the agency that manages the code list.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'List Version', 'Identifier', 'The identification of the version of the list of codes.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Code' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Time Zone', 'Code', 'The time zone to which the date time refers', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Date Time' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Daylight Saving', 'Indicator', 'The indication of whether or not this Date Time is in daylight saving', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Date Time' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions (MIME) media type of the graphic.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Graphic' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Character Set', 'Code', 'The character set of the graphic if the Multipurpose Internet Mail Extensions (MIME) type is text.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Graphic' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Filename', 'Name', 'The filename of the graphic', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Graphic' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Scheme', 'Identifier', 'The identification of the identifier scheme.', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Scheme Version', 'Identifier', 'The identification of the version of the identifier scheme', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Scheme Agency', 'Identifier', 'The identification of the agency that manages the identifier scheme', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Identifier' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Unit', 'Code', 'The unit of measure', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Measure' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Language', 'Code', 'The language used in the corresponding text string', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Name' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the picture', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Picture' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Picture' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Filename', 'Name', 'The filename of the picture', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Picture' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Unit', 'Code', 'The unit of measure in which the quantity is expressed', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Quantity' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Multiplier', 'Value', 'The multiplier of the Rate. Unit. Code or Rate. Currency. Code', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Unit', 'Code', 'The unit of measure of the numerator', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Currency', 'Code', 'The currency of the numerator', DT_ID, 0, 1 FROM dt WHERE data_type_term ='Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Base Multiplier', 'Value', 'The multiplier of the Rate. Base Unit. Code or Rate. Base Currency. Code', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Base Unit', 'Code', 'The unit of measure of the denominator', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Base Currency', 'Code', 'The currency of the denominator', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Rate' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the sound', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Sound' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Sound' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Filename', 'Name', 'The filename of the sound', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Sound' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Language', 'Code', 'The language used in the corresponding text string', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Text' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the video', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Video' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Video' and type =  0;
INSERT INTO dt_sc (guid, property_term, representation_term, definition, owner_dt_id, min_cardinality, max_cardinality) SELECT CONCAT('oagis-id-', sys_guid()), 'Filename', 'Name', 'The filename of the video', DT_ID, 0, 1 FROM dt WHERE data_type_term = 'Video' and type =  0;

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Daylight Saving'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Boolean'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Decimal'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Double'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Float'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Integer'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Decimal'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Double'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Float'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Integer'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'NormalizedString'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'String'), 0);

INSERT INTO cdt_sc_awd_pri (cdt_sc_id, cdt_pri_id, is_default) values ((SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename'), (SELECT cdt_pri_id FROM cdt_pri WHERE Name = 'Token'), 1);

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID = (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID = (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID = (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Amount' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID = (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Binary Object' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Code' and dt_sc.Property_Term = 'List Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Time Zone') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Date Time' and dt_sc.Property_Term = 'Daylight Saving') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Boolean')), (SELECT xbt_id FROM xbt WHERE name = 'boolean'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Graphic' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Version') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Identifier' and dt_sc.Property_Term = 'Scheme Agency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Measure' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Name' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Picture' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Quantity' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Decimal')), (SELECT xbt_id FROM xbt WHERE name = 'decimal'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Double')), (SELECT xbt_id FROM xbt WHERE name = 'double'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Double')), (SELECT xbt_id FROM xbt WHERE name = 'float'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Float')), (SELECT xbt_id FROM xbt WHERE name = 'float'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'non negative integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'positive integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Decimal')), (SELECT xbt_id FROM xbt WHERE name = 'decimal'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Double')), (SELECT xbt_id FROM xbt WHERE name = 'double'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Double')), (SELECT xbt_id FROM xbt WHERE name = 'float'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Float')), (SELECT xbt_id FROM xbt WHERE name = 'float'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'non negative integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Multiplier') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Integer')), (SELECT xbt_id FROM xbt WHERE name = 'positive integer'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Unit') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Rate' and dt_sc.Property_Term = 'Base Currency') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Sound' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Text' and dt_sc.Property_Term = 'Language') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'MIME') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Character Set') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'NormalizedString')), (SELECT xbt_id FROM xbt WHERE name = 'normalized string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'String')), (SELECT xbt_id FROM xbt WHERE name = 'string'));

INSERT INTO cdt_sc_awd_pri_xps_type_map (cdt_sc_awd_pri, xbt_id) VALUES ((SELECT cdt_sc_awd_pri_id FROM cdt_sc_awd_pri WHERE CDT_SC_ID =  (SELECT DT_SC_ID FROM dt_sc join dt on dt.dt_id = dt_sc.Owner_DT_ID where dt.Data_Type_Term = 'Video' and dt_sc.Property_Term = 'Filename') AND cdt_pri_id = (SELECT cdt_pri_id FROM cdt_pri WHERE name = 'Token')), (SELECT xbt_id FROM xbt WHERE name = 'token'));

insert into namespace (namespace_id, uri, prefix, description, owner_user_id, created_by, last_updated_by, creation_timestamp, last_updated_timestamp) values (1, 'http://www.openapplications.org/oagis/10', '', 'OAGIS release 10 namespace', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);  

insert into release (release_num, release_note, namespace_id) select '10.1', 'Open Applications Group
Interface Specification XMLSchemas and Sample XML Files

OAGIS Release 10_1  

27 June 2014


OAGIS Release 10_1 is a general availability release of OAGIS the release
date is 27 June 2014. 

This release is the continuation of the focus on enabling integration that 
the Open Applications Group and its members are known.

Please provide all feedback to the OAGI Architecture Team via the Feedback 
Forum at: oagis@openapplications.org

These XML reference files continue to evolve.  Please feel
free to use them, but check www.openapplications.org for the most 
recent updates.

OAGIS Release 10_1 includes:

  - Addition of more Open Parties and Quantities from implementation feedback.
  - Updates to the ConfirmBOD to make easier to use.
  - Addtion of DocumentReferences and Attachments for PartyMaster 
  - Support for UN/CEFACT Core Components 3.0.
  - Support for UN/CEFACT XML Naming and Design Rules 3.0
  - Support for UN/CEFACT Data Type Catalog 3.1
  - Support for Standalone BODs using Local elements.


NOTICE: We recommend that you install on your root directory drive as the 
paths may be too long otherwise.
	
As with all OAGIS releases OAGIS Release 10_1 contains XML Schema. To view 
XML Schema it is recommended that you use an XML IDE, as the complete structure 
of the Business Object Documents are not viewable from a single file.

Note that the sample files were used to verify the XMLSchema 
development, and do not necessarily reflect actual business 
transactions.  In many cases,the data entered in the XML files are just 
placeholder text.  Real-world examples for each transaction will be 
provided as they become available. If you are interested in providing 
real-world examples please contact oagis@openapplications.org

Please send suggestions or bug reports to oagis@openapplications.org

Thank you for your interest and support.

Best Regards,
The Open Applications Group Architecture Council
' , namespace_id from namespace where prescription = 'OAGIS release 10 namespace';