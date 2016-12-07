/* Search for an ACC (a complex type) using case insenstive part of the name or documentation. */
select * from ACC where LOWER(ACC.DEN) like '%purchase%' or LOWER(ACC.DEFINITION) like '%purchase%';

/* Search for an ASCCP (an element) using case insensitive part of the name or documentation. */
select * from ASCCP where LOWER(ASCCP.DEN) like '%purchase%' or LOWER(ASCCP.DEFINITION) like '%purchase%';

/* Search for a BCCP (an element with no structure) using case insensitive part of the name or documentation. */
select * from BCCP where LOWER(BCCP.DEN) like '%purchase%' or LOWER(BCCP.DEFINITION) like '%purchase%';

/* Retrieve ACCs that are semantic group 
These are other component types 0 = Base, 1 = Semantics, 2 = Extension, 3 = Semantic Group */

select * from ACC where OAGIS_COMPONENT_TYPE = 3; 

/* Retrieve BDTs. Note that TYPE =1 is BDT and TYPE = 0 is CDT. */
select * from DT where TYPE = 1;

/* Search for a BDT by name. */
select * from DT where TYPE = 1 and LOWER(DT.DEN) like '%amount%';

/* Retrieve children of an ACC (a type definition). See also the 'where used' queries below. */
select ASCC.DEN as ASSOC_DEN from ASCC inner join ACC on ASCC.FROM_ACC_ID = ACC.ACC_ID where ACC.DEN like '%Customer Party.%' 
union
select BCC.DEN as ASSOC_DEN from BCC inner join ACC on BCC.FROM_ACC_ID = ACC.ACC_ID where ACC.DEN like '%Customer Party.%' ; /* This will show direct associations/children of the CustomerPartyType */

select ASCC.DEN as ASSOC_DEN from ASCC inner join ACC on ASCC.FROM_ACC_ID = ACC.ACC_ID where ACC.DEN like 'Party Base.%' 
union
select BCC.DEN as ASSOC_DEN from BCC inner join ACC on BCC.FROM_ACC_ID = ACC.ACC_ID where ACC.DEN like 'Party Base.%' ;/* This will show associations/children of the PartyBaseType */

/*Retrieve all children associations (both bcc and ascc) of an ACC including those inherited. Below gives an example for the Customer Party ACC. 
Replace the two ACC_ID values to get children of a different ACC. */

SELECT
  ACC.OBJECT_CLASS_TERM, 
  ASCC.DEN as ASSOC_DEN
FROM (
  SELECT *
  FROM ACC
  START WITH ACC_ID = 115
  CONNECT BY ACC_ID = PRIOR BASED_ACC_ID
  ORDER BY ACC_ID 
) acc JOIN ASCC ON ACC.ACC_ID = ASCC.FROM_ACC_ID      
union
SELECT
  ACC.OBJECT_CLASS_TERM, 
 BCC.DEN as ASSOC_DEN
FROM (
  SELECT *
  FROM ACC
  START WITH ACC_ID = 115
  CONNECT BY ACC_ID = PRIOR BASED_ACC_ID
  ORDER BY ACC_ID 
) acc JOIN BCC ON ACC.ACC_ID = BCC.FROM_ACC_ID;

/********** Where used *********/

/* We can easily find this using the ASCC and/or BCC records.
Recall that per CCTS, DEN of ASCC and BCC contains [Object Class Term of parent component]. [BCCP or ASCCP property term]. [Data Type Term or target ACC Object Class Term].
So we can search for where-used using the ASCC's and BCC's DEN. */

/* Where is an ACC (type) or ASCCP (element) used? Supposed we want to find 'where used' for the ManufacturingParty type or element. Notice that the names in the database are space-separated. */
/* The result shows that there is no ManufacturingPartyType. */
select ascc.den from ascc where ascc.den like '%Manufacturing Party%';

/* Where is the "Party" used*/
select ascc.den from ascc where ascc.den like '%Party%'; /* This will show "Party" or other components having "Party" in the name as a parent as well as a child. */
select ascc.den from ascc where ascc.den like '%.%Party%.%'; /* This one specifically looks for "Party" in the name as child. */ 

select ascc.den from ascc where ascc.den like '%.%. Party'; /* This one specifically looks for "Party" ACC (type) used as the content of the child. */

/* Where is a BCCP (simple content element) used? */
select bcc.den from bcc where bcc.den like '%.%Type Code.%'; /* Where the TypeCode element or attribute is used.*/

/* We can also search across ASCCP and BCCP */
select ASCC.DEN from ASCC where ASCC.DEN like '%Party%' union all select BCC.DEN from BCC where BCC.DEN like '%Party%';

/* Search for an element using documentation. */
SELECT *
FROM (
SELECT ASCCP.DEN as ELEMENT_DEN, DBMS_LOB.SUBSTR(ASCCP.DEFINITION, 4000) as DOCUMENTATION FROM ASCCP
UNION
SELECT BCCP.DEN as ELEMENT_DEN, DBMS_LOB.SUBSTR(BCCP.DEFINITION, 4000) as DOCUMENTATION FROM BCCP
) tmp
WHERE LOWER(tmp.DOCUMENTATION) LIKE '%customer%';

/*************** End where used. ******************/

/* Search for something having a particular term in the DEN or the documentation. */
select ASCC.DEN as ASSOC_DEN from ASCC join ASCCP on ASCC.TO_ASCCP_ID = ASCCP.ASCCP_ID where LOWER(ASCC.DEN) like '%customer%' or LOWER(ASCC.DEFINITION) like '%customer%' or LOWER(ASCCP.DEFINITION) like '%customer%'
union
select BCC.DEN as ASSOC_DEN from BCC join BCCP on BCC.TO_BCCP_ID = BCCP.BCCP_ID where LOWER(BCC.DEN) like '%customer%' or LOWER(BCC.DEFINITION) like '%customer%' or LOWER(BCCP.DEFINITION) like '%customer%';

/* Search for code lists */       
/* Agency ID list can be search in a similar way. */
select * from CODE_LIST where NAME = 'oacl_ActionCode';
select * from CODE_LIST where NAME like '%Action%';

/* view code list values */
select CODE_LIST.NAME as CODE_LIST_NAME, CODE_LIST_VALUE.VALUE AS CODE, CODE_LIST_VALUE.DEFINITION AS DEFINITION from CODE_LIST_VALUE inner join CODE_LIST on CODE_LIST.CODE_LIST_ID = CODE_LIST_VALUE.CODE_LIST_ID where CODE_LIST.CODE_LIST_ID = 1;

/* domain value restriction of a DT */
select XBT.NAME as VALUE_DOMAIN from 
(
   BDT_PRI_RESTRI 
   inner join 
   ( 
      (
         CDT_AWD_PRI_XPS_TYPE_MAP 
         inner join 
         CDT_AWD_PRI on CDT_AWD_PRI.CDT_AWD_PRI_ID = CDT_AWD_PRI_XPS_TYPE_MAP. CDT_AWD_PRI_ID 
      ) 
      inner join 
      XBT on CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID = XBT.XBT_ID
   )
   on
   BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID = CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID
)
where BDT_PRI_RESTRI.BDT_ID = 95
union
select CODE_LIST.NAME as VALUE_DOMAIN from 
  BDT_PRI_RESTRI inner join CODE_LIST on BDT_PRI_RESTRI.CODE_LIST_ID = CODE_LIST.CODE_LIST_ID
  where BDT_PRI_RESTRI.BDT_ID = 95
union
select AGENCY_ID_LIST.NAME as VALUE_DOMAIN from 
  BDT_PRI_RESTRI inner join AGENCY_ID_LIST on BDT_PRI_RESTRI.AGENCY_ID_LIST_ID = AGENCY_ID_LIST.AGENCY_ID_LIST_ID
  where BDT_PRI_RESTRI.BDT_ID = 95;
  
/* get supplementary components of a DT */
select * from DT_SC where DT_SC.OWNER_DT_ID = 1 and DT_SC.CARDINALITY_MAX > 0;






  
  
  
  
  
  
  
  
  
  
  
