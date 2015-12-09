/*
insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Amount', 'Amount. Type', 'Amount. Content', 'CDT V3.1. An amount is a number of monetary units specified in a currency.', 'A number of monetary units.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Binary Object', 'Binary Object. Type', 'Binary Object. Content', 'CDT V3.1. A binary object is a sequence of binary digits (bits).', 'A finite sequence of binary digits (bits).', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Code', 'Code. Type', 'Code. Content', 'CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences), and symbols. It represents a definitive value,
a method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.', 'A character string (letters, figures or symbols) that for brevity
and/or language independence may be used to represent or
replace a definitive value or text of an attribute.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Date', 'Date. Type', 'Date. Content', 'CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.', 'The particular point in the progression of date.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Date Time', 'Date Time. Type', 'Date Time. Content', 'CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of
second.', 'The particular date and time point in the progression of time.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Duration', 'Duration. Type', 'Duration. Content', 'CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,
Week, Day) and Hours, Minutes or Seconds.', 'The particular representation of duration.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Graphic', 'Graphic. Type', 'Graphic. Content', 'CDT V3.. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).', 'A finite sequence of binary digits (bits) for graphics.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Identifier', 'Identifier. Type', 'Identifier. Content', 'CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an
agency.', 'A character string used to uniquely identify one instance of
an object within an identification scheme that is managed
by an agency.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Indicator', 'Indicator. Type', 'Indicator. Content', 'CDT V3.. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.', 'The value of the Indicator.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Measure', 'Measure. Type', 'Measure. Content', 'CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.', 'The numeric value determined by measuring an object.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Name', 'Name. Type', 'Name. Content', 'CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.', 'A word or phrase that represents a designation of a person, place, thing or concept.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Number', 'Number. Type', 'Number. Content', 'CDT V3.1. A mathematical number that is assigned or is determined by calculation.', 'Mathematical number that is assigned or is determined by calculation.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Ordinal', 'Ordinal. Type', 'Ordinal. Content', 'CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.', 'An assigned mathematical number that represents order or sequence', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Percent', 'Percent. Type', 'Percent. Content', 'CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.', 'Numeric information that is assigned or is determined by percent.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Picture', 'Picture. Type', 'Picture. Content', 'CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).', 'A finite sequence of binary digits (bits) for pictures.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Quantity', 'Quantity. Type', 'Quantity. Content', 'CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.', 'A counted number of non-monetary units possibly including
fractions.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Rate', 'Rate. Type', 'Rate. Content', 'CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.', 'The numerical value of the rate.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Ratio', 'Ratio. Type', 'Ratio. Content', 'CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a
quotient showing the number of times one value contains or is contained within the other, or as a proportion.', 'The quotient or proportion between two independent quantities of the
same unit of measure or currency.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Sound', 'Sound. Type', 'Sound. Content', 'CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).', 'A finite sequence of binary digits (bits) for
sounds.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Text', 'Text. Type', 'Text. Content', 'CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.', 'A character string generally in the form of words of a
language.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Time', 'Time. Type', 'Time. Content', 'CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.', 'The particular point in the progression of time.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Value', 'Value. Type', 'Value. Content', 'CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.', 'Numeric information that is assigned or is determined by
value.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

insert into dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_Documentation, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Video', 'Video. Type', 'Video. Content', 'CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).', 'A finite sequence of binary digits (bits) for videos.', '', '1', User_ID, User_ID, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from user where User_Name = 'oagis' ;

select * from dt;


*/

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Amount', 'Amount. Type', 'Amount. Content', 'CDT V3.1. An amount is a number of monetary units specified in a currency.', 'A number of monetary units.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Binary Object', 'Binary Object. Type', 'Binary Object. Content', 'CDT V3.1. A binary object is a sequence of binary digits (bits).', 'A finite sequence of binary digits (bits).', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Code', 'Code. Type', 'Code. Content', 'CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences), and symbols. It represents a definitive value,
a method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.', 'A character string (letters, figures or symbols) that for brevity
and/or language independence may be used to represent or
replace a definitive value or text of an attribute.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Date', 'Date. Type', 'Date. Content', 'CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.', 'The particular point in the progression of date.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Date Time', 'Date Time. Type', 'Date Time. Content', 'CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of
second.', 'The particular date and time point in the progression of time.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Duration', 'Duration. Type', 'Duration. Content', 'CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,
Week, Day) and Hours, Minutes or Seconds.', 'The particular representation of duration.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Graphic', 'Graphic. Type', 'Graphic. Content', 'CDT V3.. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).', 'A finite sequence of binary digits (bits) for graphics.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Identifier', 'Identifier. Type', 'Identifier. Content', 'CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an
agency.', 'A character string used to uniquely identify one instance of
an object within an identification scheme that is managed
by an agency.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Indicator', 'Indicator. Type', 'Indicator. Content', 'CDT V3.. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.', 'The value of the Indicator.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Measure', 'Measure. Type', 'Measure. Content', 'CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.', 'The numeric value determined by measuring an object.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Name', 'Name. Type', 'Name. Content', 'CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.', 'A word or phrase that represents a designation of a person, place, thing or concept.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Number', 'Number. Type', 'Number. Content', 'CDT V3.1. A mathematical number that is assigned or is determined by calculation.', 'Mathematical number that is assigned or is determined by calculation.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Ordinal', 'Ordinal. Type', 'Ordinal. Content', 'CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.', 'An assigned mathematical number that represents order or sequence', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Percent', 'Percent. Type', 'Percent. Content', 'CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.', 'Numeric information that is assigned or is determined by percent.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Picture', 'Picture. Type', 'Picture. Content', 'CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).', 'A finite sequence of binary digits (bits) for pictures.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Quantity', 'Quantity. Type', 'Quantity. Content', 'CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.', 'A counted number of non-monetary units possibly including
fractions.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Rate', 'Rate. Type', 'Rate. Content', 'CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.', 'The numerical value of the rate.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Ratio', 'Ratio. Type', 'Ratio. Content', 'CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a
quotient showing the number of times one value contains or is contained within the other, or as a proportion.', 'The quotient or proportion between two independent quantities of the
same unit of measure or currency.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Sound', 'Sound. Type', 'Sound. Content', 'CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).', 'A finite sequence of binary digits (bits) for
sounds.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Text', 'Text. Type', 'Text. Content', 'CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.', 'A character string generally in the form of words of a
language.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Time', 'Time. Type', 'Time. Content', 'CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.', 'The particular point in the progression of time.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Value', 'Value. Type', 'Value. Content', 'CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.', 'Numeric information that is assigned or is determined by
value.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

insert into dt (guid, type, version_num,  revision_type, data_type_term, den, content_component_den, definition, content_component_definition, revision_documentation, revision_state, created_by, last_updated_by, creation_timestamp, last_update_timestamp) 
	select concat('oagis-id-',REPLACE(UUID(),'-','')), '0', '1.0', '0', 'Video', 'Video. Type', 'Video. Content', 'CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).', 'A finite sequence of binary digits (bits) for videos.', '', '1', app_user_id, app_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from app_user where login_id = 'oagis' ;

select * from dt;