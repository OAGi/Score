delete from abie;
/*delete from acc_business_term;*/
update Agency_ID_List set agency_id = null;
delete from agency_id_list_value;
delete from agency_id_list;
delete from asbie;
delete from asbiep;
/*delete from asccp_business_term;*/
delete from bbie;
delete from bbiep;
delete from bbie_sc;
delete from bdt_pri_restri;
delete from bdt_sc_pri_restri;

delete from biz_ctx_value;
delete from biz_ctx;
delete from cdt_awd_pri_xps_type_map;
delete from cdt_awd_pri;
delete from cdt_sc_awd_pri_xps_type_map;
delete from cdt_sc_awd_pri;
delete from cdt_pri;
delete from code_list_value;
delete from code_list;
delete from ctx_scheme_value;
delete from classification_ctx_scheme;
delete from ctx_category;

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
delete from app_user;
update xbt set subtype_of_xbt_id = null;
delete from xbt;


delete from asbie;
delete from asbiep;
delete from bbiep;
delete from bbie_sc;
delete from abie;
delete from bbie;