delete from abie;
delete from acc_business_term;
update Agency_ID_List set agency_id = null;
delete from agency_id_list_value;
delete from agency_id_list;
delete from asbie;
delete from asbiep;
delete from asccp_business_term;
delete from bbie;
delete from bbiep;
delete from bbie_sc;
delete from bdt_primitive_restriction;
delete from bdt_sc_primitive_restriction;

delete from business_context_value;
delete from business_context;
delete from cdt_allowed_primitive_expression_type_map;
delete from cdt_allowed_primitive;
delete from cdt_sc_allowed_primitive_expression_type_map;
delete from cdt_sc_allowed_primitive;
delete from cdt_primitive;
delete from code_list_value;
delete from code_list;
delete from context_scheme_value;
delete from context_scheme;
delete from context_category;

update dt set based_dt_id = null;
update dt_sc set based_dt_sc_id = null;
delete from ascc;
delete from bcc;
delete from bccp;
delete from asccp;
update acc set based_acc_id = null;
delete from acc;
delete from dt_sc;
delete from dt;
delete from user;
update xsd_builtin_type set Subtype_Of_XSD_BuiltIn_Type_ID = null;
delete from xsd_builtin_type;


delete from asbie;
delete from asbiep;
delete from bbiep;
delete from bbie_sc;
delete from abie;
delete from bbie;