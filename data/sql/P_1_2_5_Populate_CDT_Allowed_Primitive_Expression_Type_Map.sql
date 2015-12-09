/*
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Binary Object' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term =  = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Binary Object' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:hexBinary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:normalizedString'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:string'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Token'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:date'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:time'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gYearMonth'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gYear'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gMonthDay'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gDay'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gMonth'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:dateTime'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:date'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:time'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gYearMonth'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gYear'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gMonthDay'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gDay'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:gMonth'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Duration' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimeDuration'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Duration' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimeDuration'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:duration'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Graphic' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Graphic' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:hexBinary'));


Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:normalizedString'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:string'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Token'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Indicator' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Boolean'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:boolean'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:normalizedString'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:String'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Token'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ordinal' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ordinal' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ordinal' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Picture' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Picture' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:hexBinary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:string'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Sound' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Sound' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:hexBinary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:normalizedString'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:string'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Token'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Time' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:time'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:decimal'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:double'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Double'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Float'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:float'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:positiveInteger'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:normalizedString'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'String'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:string'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Token'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:token'));


Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Video' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Video' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:hexBinary'));

select * from cdt_allowed_primitive_expression_type_map;

*/

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'decimal'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:decimal'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:double'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'double'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'float'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:float'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Amount' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Binary Object' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'binary'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:base64Binary'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Binary Object' and Type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'binary'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Measure' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Name' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'NormalizedString'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:normalizedString'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Name' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'String'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:String'));

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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Number' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:nonNegativeInteger'));
			
Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'Integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:integer'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ordinal' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Percent' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Quantity' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Rate' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Ratio' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
			(select xbt_id from xbt where builtIn_type = 'xsd:positiveInteger'));

Insert into cdt_awd_pri_xps_type_map (cdt_awd_pri_id, xbt_id) 
	values ((select cdt_awd_pri_id from cdt_awd_pri where cdt_id = (select dt_id from dt where data_type_term = 'Value' and type = 0) AND (cdt_pri_id = (select cdt_pri_id from cdt_pri where name = 'integer'))),
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

select * from cdt_awd_pri_xps_type_map;

