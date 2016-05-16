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

select * from cdt_awd_pri_xps_type_map;

