Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Amount' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 


Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Binary Object' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Code' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Token'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Date' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Date Time' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Duration' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'TimeDuration'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Graphic' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Identifier' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Token'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Indicator' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Boolean'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 


Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False); 


Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Measure' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Name' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Token'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Number' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ordinal' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Percent' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 


Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Picture' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'), True); 


Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False);
 
Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Quantity' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False);
 
Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Rate' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False);
 
Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Ratio' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Sound' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Text' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Token'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Time' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'TimePoint'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Decimal'), True); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Double'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Float'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Integer'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'NormalizedString'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'String'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Value' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Token'), False); 

Insert into cdt_allowed_primitive (CDT_ID, CDT_Primitive_ID, isDefault) 
	values ((select DT_ID from dt where Data_Type_Term = 'Video' and DT_Type = 0), (select CDT_Primitive_ID from cdt_primitive where name = 'Binary'), True); 


select * from cdt_allowed_primitive;