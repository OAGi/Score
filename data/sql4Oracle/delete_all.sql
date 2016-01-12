create or replace TRIGGER abie_abie_id_TRG BEFORE INSERT ON abie
FOR EACH ROW
BEGIN
	SELECT  abie_abie_id_SEQ.NEXTVAL INTO :new.abie_id FROM DUAL;
END ;
/

create or replace TRIGGER acc_acc_id_TRG BEFORE INSERT ON acc
FOR EACH ROW
BEGIN
	SELECT  acc_acc_id_SEQ.NEXTVAL INTO :new.acc_id FROM DUAL;
END ;
/

create or replace TRIGGER AGENCY_ID_LIST_AGENCY_ID_LIS_1 BEFORE INSERT ON agency_id_list
FOR EACH ROW
BEGIN
	SELECT  agency_id_list_agency_id_list_.NEXTVAL INTO :new.agency_id_list_id FROM DUAL;
END ;
/

create or replace TRIGGER AGENCY_ID_LIST_VALUE_AGENCY__1 BEFORE INSERT ON agency_id_list_value
FOR EACH ROW
BEGIN
	SELECT  agency_id_list_value_agency_id.NEXTVAL INTO :new.agency_id_list_value_id FROM DUAL;
END ;
/

create or replace TRIGGER app_user_app_user_id_TRG BEFORE INSERT ON app_user
FOR EACH ROW
BEGIN
	SELECT  app_user_app_user_id_SEQ.NEXTVAL INTO :new.app_user_id FROM DUAL;
END ;
/

create or replace TRIGGER asbie_asbie_id_TRG BEFORE INSERT ON asbie
FOR EACH ROW
BEGIN
	SELECT  asbie_asbie_id_SEQ.NEXTVAL INTO :new.asbie_id FROM DUAL;
END ;
/

create or replace TRIGGER asbiep_asbiep_id_TRG BEFORE INSERT ON asbiep
FOR EACH ROW
BEGIN
	SELECT  asbiep_asbiep_id_SEQ.NEXTVAL INTO :new.asbiep_id FROM DUAL;
END ;
/

create or replace TRIGGER ascc_ascc_id_TRG BEFORE INSERT ON ascc
FOR EACH ROW
BEGIN
	SELECT  ascc_ascc_id_SEQ.NEXTVAL INTO :new.ascc_id FROM DUAL;
END ;
/

create or replace TRIGGER asccp_asccp_id_TRG BEFORE INSERT ON asccp
FOR EACH ROW
BEGIN
	SELECT  asccp_asccp_id_SEQ.NEXTVAL INTO :new.asccp_id FROM DUAL;
END ;
/

create or replace TRIGGER bbie_bbie_id_TRG BEFORE INSERT ON bbie
FOR EACH ROW
BEGIN
	SELECT  bbie_bbie_id_SEQ.NEXTVAL INTO :new.bbie_id FROM DUAL;
END ;
/

create or replace TRIGGER bbie_sc_bbie_sc_id_TRG BEFORE INSERT ON bbie_sc
FOR EACH ROW
BEGIN
	SELECT  bbie_sc_bbie_sc_id_SEQ.NEXTVAL INTO :new.bbie_sc_id FROM DUAL;
END ;
/

create or replace TRIGGER bbiep_bbiep_id_TRG BEFORE INSERT ON bbiep
FOR EACH ROW
BEGIN
	SELECT  bbiep_bbiep_id_SEQ.NEXTVAL INTO :new.bbiep_id FROM DUAL;
END ;
/

create or replace TRIGGER bcc_bcc_id_TRG BEFORE INSERT ON bcc
FOR EACH ROW
BEGIN
	SELECT  bcc_bcc_id_SEQ.NEXTVAL INTO :new.bcc_id FROM DUAL;
END ;
/

create or replace TRIGGER bccp_bccp_id_TRG BEFORE INSERT ON bccp
FOR EACH ROW
BEGIN
	SELECT  bccp_bccp_id_SEQ.NEXTVAL INTO :new.bccp_id FROM DUAL;
END ;
/

create or replace TRIGGER BDT_PRI_RESTRI_BDT_PRI_RESTR_1 BEFORE INSERT ON bdt_pri_restri
FOR EACH ROW
BEGIN
	SELECT  bdt_pri_restri_bdt_pri_restri_.NEXTVAL INTO :new.bdt_pri_restri_id FROM DUAL;
END ;
/

create or replace TRIGGER BDT_SC_PRI_RESTRI_BDT_SC_PRI_1 BEFORE INSERT ON bdt_sc_pri_restri
FOR EACH ROW
BEGIN
	SELECT  bdt_sc_pri_restri_bdt_sc_pri_r.NEXTVAL INTO :new.bdt_sc_pri_restri_id FROM DUAL;
END ;
/

create or replace TRIGGER biz_ctx_biz_ctx_id_TRG BEFORE INSERT ON biz_ctx
FOR EACH ROW
BEGIN
	SELECT  biz_ctx_biz_ctx_id_SEQ.NEXTVAL INTO :new.biz_ctx_id FROM DUAL;
END ;
/

create or replace TRIGGER BIZ_CTX_VALUE_BIZ_CTX_VALUE__1 BEFORE INSERT ON biz_ctx_value
FOR EACH ROW
BEGIN
	SELECT  biz_ctx_value_biz_ctx_value_id.NEXTVAL INTO :new.biz_ctx_value_id FROM DUAL;
END ;
/

create or replace TRIGGER cdt_awd_pri_cdt_awd_pri_id_TRG BEFORE INSERT ON cdt_awd_pri
FOR EACH ROW
BEGIN
	SELECT  cdt_awd_pri_cdt_awd_pri_id_SEQ.NEXTVAL INTO :new.cdt_awd_pri_id FROM DUAL;
END ;
/

create or replace TRIGGER CDT_AWD_PRI_XPS_TYPE_MAP_CDT_1 BEFORE INSERT ON cdt_awd_pri_xps_type_map
FOR EACH ROW
BEGIN
	SELECT  cdt_awd_pri_xps_type_map_cdt_a.NEXTVAL INTO :new.cdt_awd_pri_xps_type_map_id FROM DUAL;
END ;
/

create or replace TRIGGER cdt_pri_cdt_pri_id_TRG BEFORE INSERT ON cdt_pri
FOR EACH ROW
BEGIN
	SELECT  cdt_pri_cdt_pri_id_SEQ.NEXTVAL INTO :new.cdt_pri_id FROM DUAL;
END ;
/

create or replace TRIGGER CDT_SC_AWD_PRI_CDT_SC_AWD_PR_1 BEFORE INSERT ON cdt_sc_awd_pri
FOR EACH ROW
BEGIN
	SELECT  cdt_sc_awd_pri_cdt_sc_awd_pri_.NEXTVAL INTO :new.cdt_sc_awd_pri_id FROM DUAL;
END ;
/

create or replace TRIGGER CDT_SC_AWD_PRI_XPS_TYPE_MAP__1 BEFORE INSERT ON cdt_sc_awd_pri_xps_type_map
FOR EACH ROW
BEGIN
	SELECT  cdt_sc_awd_pri_xps_type_map_cd.NEXTVAL INTO :new.cdt_sc_awd_pri_xps_type_map_id FROM DUAL;
END ;
/

create or replace TRIGGER CLASSIFICATION_CTX_SCHEME_CL_1 BEFORE INSERT ON classification_ctx_scheme
FOR EACH ROW
BEGIN
	SELECT  classification_ctx_scheme_clas.NEXTVAL INTO :new.classification_ctx_scheme_id FROM DUAL;
END ;
/

create or replace TRIGGER code_list_code_list_id_TRG BEFORE INSERT ON code_list
FOR EACH ROW
BEGIN
	SELECT  code_list_code_list_id_SEQ.NEXTVAL INTO :new.code_list_id FROM DUAL;
END ;
/

create or replace TRIGGER CODE_LIST_VALUE_CODE_LIST_VA_1 BEFORE INSERT ON code_list_value
FOR EACH ROW
BEGIN
	SELECT  code_list_value_code_list_valu.NEXTVAL INTO :new.code_list_value_id FROM DUAL;
END ;
/

create or replace TRIGGER ctx_category_ctx_category_id_T BEFORE INSERT ON ctx_category
FOR EACH ROW
BEGIN
	SELECT  ctx_category_ctx_category_id_S.NEXTVAL INTO :new.ctx_category_id FROM DUAL;
END ;
/

create or replace TRIGGER CTX_SCHEME_VALUE_CTX_SCHEME__1 BEFORE INSERT ON ctx_scheme_value
FOR EACH ROW
BEGIN
	SELECT  ctx_scheme_value_ctx_scheme_va.NEXTVAL INTO :new.ctx_scheme_value_id FROM DUAL;
END ;
/

create or replace TRIGGER dt_dt_id_TRG BEFORE INSERT ON dt
FOR EACH ROW
BEGIN
	SELECT  dt_dt_id_SEQ.NEXTVAL INTO :new.dt_id FROM DUAL;
END ;
/

create or replace TRIGGER dt_sc_dt_sc_id_TRG BEFORE INSERT ON dt_sc
FOR EACH ROW
BEGIN
	SELECT  dt_sc_dt_sc_id_SEQ.NEXTVAL INTO :new.dt_sc_id FROM DUAL;
END ;
/

create or replace TRIGGER xbt_xbt_id_TRG BEFORE INSERT ON xbt
FOR EACH ROW
BEGIN
	SELECT  xbt_xbt_id_SEQ.NEXTVAL INTO :new.xbt_id FROM DUAL;
END ;
/


ALTER TABLE agency_id_list_value DISABLE CONSTRAINT agency_id_list_fk;
ALTER TABLE agency_id_list DISABLE CONSTRAINT agency_id_fk;

delete from abie;
/*delete from acc_business_term;*/
delete from agency_id_list_value;
delete from agency_id_list;

ALTER TABLE agency_id_list_value ENABLE CONSTRAINT agency_id_list_fk;
ALTER TABLE agency_id_list ENABLE CONSTRAINT agency_id_fk;


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


delete from ascc;
delete from bcc;
delete from bccp;
delete from asccp;
delete from acc;
delete from dt_sc;
delete from dt;
delete from app_user;
delete from xbt;


delete from asbie;
delete from asbiep;
delete from bbiep;
delete from bbie_sc;
delete from abie;
delete from bbie;


DROP SEQUENCE "OAGSRT_REVISION"."ABIE_ABIE_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ABIE_ABIE_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."ACC_ACC_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ACC_ACC_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."AGENCY_ID_LIST_AGENCY_ID_LIST_";
CREATE SEQUENCE  "OAGSRT_REVISION"."AGENCY_ID_LIST_AGENCY_ID_LIST_"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."AGENCY_ID_LIST_VALUE_AGENCY_ID";
CREATE SEQUENCE  "OAGSRT_REVISION"."AGENCY_ID_LIST_VALUE_AGENCY_ID"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."APP_USER_APP_USER_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."APP_USER_APP_USER_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."ASBIE_ASBIE_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ASBIE_ASBIE_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."ASBIEP_ASBIEP_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ASBIEP_ASBIEP_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."ASCC_ASCC_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ASCC_ASCC_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."ASCCP_ASCCP_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."ASCCP_ASCCP_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BBIE_BBIE_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BBIE_BBIE_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BBIE_SC_BBIE_SC_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BBIE_SC_BBIE_SC_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BBIEP_BBIEP_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BBIEP_BBIEP_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BCC_BCC_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BCC_BCC_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BCCP_BCCP_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BCCP_BCCP_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BDT_PRI_RESTRI_BDT_PRI_RESTRI_";
CREATE SEQUENCE  "OAGSRT_REVISION"."BDT_PRI_RESTRI_BDT_PRI_RESTRI_"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BDT_SC_PRI_RESTRI_BDT_SC_PRI_R";
CREATE SEQUENCE  "OAGSRT_REVISION"."BDT_SC_PRI_RESTRI_BDT_SC_PRI_R"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BIZ_CTX_BIZ_CTX_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."BIZ_CTX_BIZ_CTX_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."BIZ_CTX_VALUE_BIZ_CTX_VALUE_ID";
CREATE SEQUENCE  "OAGSRT_REVISION"."BIZ_CTX_VALUE_BIZ_CTX_VALUE_ID"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CDT_AWD_PRI_CDT_AWD_PRI_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."CDT_AWD_PRI_CDT_AWD_PRI_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CDT_AWD_PRI_XPS_TYPE_MAP_CDT_A";
CREATE SEQUENCE  "OAGSRT_REVISION"."CDT_AWD_PRI_XPS_TYPE_MAP_CDT_A"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CDT_PRI_CDT_PRI_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."CDT_PRI_CDT_PRI_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CDT_SC_AWD_PRI_CDT_SC_AWD_PRI_";
CREATE SEQUENCE  "OAGSRT_REVISION"."CDT_SC_AWD_PRI_CDT_SC_AWD_PRI_"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CDT_SC_AWD_PRI_XPS_TYPE_MAP_CD";
CREATE SEQUENCE  "OAGSRT_REVISION"."CDT_SC_AWD_PRI_XPS_TYPE_MAP_CD"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CLASSIFICATION_CTX_SCHEME_CLAS";
CREATE SEQUENCE  "OAGSRT_REVISION"."CLASSIFICATION_CTX_SCHEME_CLAS"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CODE_LIST_CODE_LIST_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."CODE_LIST_CODE_LIST_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CODE_LIST_VALUE_CODE_LIST_VALU";
CREATE SEQUENCE  "OAGSRT_REVISION"."CODE_LIST_VALUE_CODE_LIST_VALU"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CTX_CATEGORY_CTX_CATEGORY_ID_S";
CREATE SEQUENCE  "OAGSRT_REVISION"."CTX_CATEGORY_CTX_CATEGORY_ID_S"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."CTX_SCHEME_VALUE_CTX_SCHEME_VA";
CREATE SEQUENCE  "OAGSRT_REVISION"."CTX_SCHEME_VALUE_CTX_SCHEME_VA"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."DT_DT_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."DT_DT_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."DT_SC_DT_SC_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."DT_SC_DT_SC_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."RELEASE_RELEASE_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."RELEASE_RELEASE_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

DROP SEQUENCE "OAGSRT_REVISION"."XBT_XBT_ID_SEQ";
CREATE SEQUENCE  "OAGSRT_REVISION"."XBT_XBT_ID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;


