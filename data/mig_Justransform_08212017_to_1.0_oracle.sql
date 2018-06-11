-- Add Columns on RELEASE table
ALTER TABLE RELEASE ADD (
  CREATED_BY            NUMBER (19),
  LAST_UPDATED_BY       NUMBER (19),
  CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE,
  LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE,
  STATE                 NUMBER (10)
);
  COMMENT ON COLUMN RELEASE.CREATED_BY
IS
  'A foreign key referring to the user who creates the entity.' ;
  COMMENT ON COLUMN RELEASE.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who updated the record.' ;
  COMMENT ON COLUMN RELEASE.CREATION_TIMESTAMP
IS
  'Timestamp when the record was created.' ;
  COMMENT ON COLUMN RELEASE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.' ;
  COMMENT ON COLUMN RELEASE.STATE
IS
  '1 = DRAFT, 2 = FINAL. This the revision life cycle state of the Release.' ;

CREATE INDEX RELEASE_CREATED_BY_IDX ON RELEASE (CREATED_BY ASC);
CREATE INDEX RELEASE_LAST_UPDATED_BY_IDX ON RELEASE (LAST_UPDATED_BY ASC);

ALTER TABLE RELEASE ADD CONSTRAINT RELEASE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;
ALTER TABLE RELEASE ADD CONSTRAINT RELEASE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

-- Update data on RELEASE table
UPDATE RELEASE SET CREATED_BY = (SELECT APP_USER_ID FROM APP_USER WHERE LOGIN_ID = 'oagis'), LAST_UPDATED_BY = (SELECT APP_USER_ID FROM APP_USER WHERE LOGIN_ID = 'oagis'), CREATION_TIMESTAMP = CURRENT_TIMESTAMP(6), LAST_UPDATE_TIMESTAMP = CURRENT_TIMESTAMP(6), STATE = 2;

ALTER TABLE RELEASE MODIFY (
  CREATED_BY            NUMBER (19) NOT NULL ,
  LAST_UPDATED_BY       NUMBER (19) NOT NULL ,
  CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
  LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
  STATE                 NUMBER (10) NOT NULL
);

CREATE OR REPLACE TRIGGER FKNTM_RELEASE BEFORE
  UPDATE OF CREATED_BY ON RELEASE BEGIN raise_application_error(-20225,'Non Transferable FK constraint  on table RELEASE is violated');
END;
/

-- Add Column on TOP_LEVEL_ABIE table
ALTER TABLE TOP_LEVEL_ABIE ADD (
  RELEASE_ID        NUMBER (19)
);
  COMMENT ON COLUMN TOP_LEVEL_ABIE.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.

Unpublished components cannot be released.' ;

CREATE INDEX TOP_LEVEL_ABIE_RELEASE_ID_IDX ON TOP_LEVEL_ABIE (RELEASE_ID ASC);

ALTER TABLE TOP_LEVEL_ABIE ADD CONSTRAINT TOP_LEVEL_ABIE_RELEASE_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

-- Update data on TOP_LEVEL_ABIE table
UPDATE TOP_LEVEL_ABIE SET RELEASE_ID = 1;

COMMIT;
