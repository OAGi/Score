Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'decimal'))),
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
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Binary' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'binary'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:base64Binary'));

Insert into cdt_allowed_primitive_expression_type_map (CDT_Allowed_Primitive_ID, XSD_BuiltIn_Type_ID) 
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Binary' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'binary'))),
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
	values ((select CDT_Allowed_Primitive_ID from CDT_Allowed_Primitive where CDT_ID = (select DT_ID from dt where Data_Type_Term = 'Ordinal' and DT_Type = 0) AND (CDT_Primitive_ID = (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'))),
			(select XSD_BuiltIn_Type_ID from xsd_builtin_type where BuiltIn_Type = 'xsd:integer'));

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

