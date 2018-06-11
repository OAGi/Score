-- Add Columns on XBT table
ALTER TABLE XBT ADD JBT_DRAFT05_MAP VARCHAR2 (500 CHAR);

UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 1;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 2;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string", "pattern":"^[-]?P(?!$)(?:\\d+Y)?(?:\\d+M)?(?:\\d+D)?(?:T(?!$)(?:\\d+H)?(?:\\d+M)?(?:\\d+(?:\\.\\d+)?S)?)?$"}' WHERE XBT_ID = 3;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string", "format":"date-time"}' WHERE XBT_ID = 4;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 5;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 6;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 7;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 8;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 9;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 10;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 11;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 12;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 13;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 14;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 15;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"boolean"}' WHERE XBT_ID = 16;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 17;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 18;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"number"}' WHERE XBT_ID = 19;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"number"}' WHERE XBT_ID = 20;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"number", "multipleOf":1}' WHERE XBT_ID = 21;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"integer", "minimum":0, "exclusiveMinimum":false}' WHERE XBT_ID = 22;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"integer", "minimum":0, "exclusiveMinimum":true}' WHERE XBT_ID = 23;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"number"}' WHERE XBT_ID = 24;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string", "format":"uriref"}' WHERE XBT_ID = 25;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"boolean"}' WHERE XBT_ID = 26;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 27;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 28;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 29;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 30;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 31;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 32;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 33;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 34;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 35;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 36;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 37;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 38;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 39;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 40;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 41;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 42;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 43;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 44;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 45;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 46;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 47;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 48;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 49;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 50;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 51;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 52;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 53;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 54;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 55;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 56;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 57;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 58;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 59;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 60;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 61;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 62;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 63;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 64;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 65;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 66;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 67;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 68;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 69;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 70;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 71;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 72;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 73;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 74;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 75;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 76;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 77;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 78;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 79;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 80;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 81;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 82;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 83;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 84;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 85;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 86;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 87;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 88;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 89;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 90;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 91;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 92;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 93;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 94;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 95;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 96;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 97;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 98;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 99;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 100;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 101;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 102;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 103;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 104;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 105;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 106;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 107;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 108;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 109;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 110;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 111;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 112;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 113;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 114;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 115;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 116;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 117;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 118;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 119;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 120;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 121;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 122;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 123;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 124;
UPDATE XBT SET JBT_DRAFT05_MAP = '{"type":"string"}' WHERE XBT_ID = 125;

-- Add Columns on MODULE table
ALTER TABLE MODULE ADD (
  CREATED_BY            NUMBER (19),
  LAST_UPDATED_BY       NUMBER (19),
  OWNER_USER_ID         NUMBER (19),
  CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE,
  LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE
);

COMMENT ON COLUMN MODULE.CREATED_BY
IS
  'A foreign key referring to the user who creates the entity.

This column never change between the history and the current record. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN MODULE.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who updated the record. 

In the history record, this should always be the user who is editing the entity.' ;
  COMMENT ON COLUMN MODULE.OWNER_USER_ID
IS
  'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership.' ;
  COMMENT ON COLUMN MODULE.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the MODULE was created. 

This never change for a revision.' ;
  COMMENT ON COLUMN MODULE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.' ;

CREATE INDEX MODULE_CREATED_BY_IDX ON MODULE (CREATED_BY ASC);
CREATE INDEX MODULE_LAST_UPDATED_BY_IDX ON MODULE (LAST_UPDATED_BY ASC);
CREATE INDEX MODULE_OWNER_USER_ID_IDX ON MODULE (OWNER_USER_ID ASC);

ALTER TABLE MODULE ADD CONSTRAINT MODULE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;
ALTER TABLE MODULE ADD CONSTRAINT MODULE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;
ALTER TABLE MODULE ADD CONSTRAINT MODULE_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

-- Update data on MODULE table
UPDATE MODULE SET CREATED_BY = (SELECT APP_USER_ID FROM APP_USER WHERE LOGIN_ID = 'oagis'), LAST_UPDATED_BY = (SELECT APP_USER_ID FROM APP_USER WHERE LOGIN_ID = 'oagis'), OWNER_USER_ID = (SELECT APP_USER_ID FROM APP_USER WHERE LOGIN_ID = 'oagis'), CREATION_TIMESTAMP = CURRENT_TIMESTAMP(6), LAST_UPDATE_TIMESTAMP = CURRENT_TIMESTAMP(6);

ALTER TABLE MODULE MODIFY (
  CREATED_BY            NUMBER (19) NOT NULL,
  LAST_UPDATED_BY       NUMBER (19) NOT NULL,
  OWNER_USER_ID         NUMBER (19) NOT NULL,
  CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL,
  LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE OR REPLACE TRIGGER FKNTM_MODULE BEFORE
  UPDATE OF CREATED_BY ON MODULE BEGIN raise_application_error(-20225,'Non Transferable FK constraint  on table MODULE is violated');
END;
/

-- Create indices for Core Component GUIDs
CREATE INDEX ACC_GUID_IDX ON ACC (GUID ASC);
CREATE INDEX ASCC_GUID_IDX ON ASCC (GUID ASC);
CREATE INDEX ASCCP_GUID_IDX ON ASCCP (GUID ASC);
CREATE INDEX BCC_GUID_IDX ON BCC (GUID ASC);
CREATE INDEX BCCP_GUID_IDX ON BCCP (GUID ASC);

COMMIT;
