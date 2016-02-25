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


select * from cdt_awd_pri;