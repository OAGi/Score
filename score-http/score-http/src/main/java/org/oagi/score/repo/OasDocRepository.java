package org.oagi.score.repo;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.OasDoc;
import org.oagi.score.repo.api.security.AccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.sha256;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

@Repository
public class OasDocRepository {
    @Autowired
    private DSLContext dslContext;

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkOasDocUniqueness(OasDoc oasDoc)
            throws ScoreDataAccessException {

        if (oasDoc.getTitle() != null && oasDoc.getOpenAPIVersion() != null) {
            List<Condition> conditions = new ArrayList<>();

            conditions.add(and(OAS_DOC.TITLE.eq(oasDoc.getTitle()),
                    OAS_DOC.OPEN_API_VERSION.eq(oasDoc.getOpenAPIVersion()),
                    OAS_DOC.VERSION.eq(oasDoc.getVersion()),
                    OAS_DOC.LICENSE_NAME.eq(oasDoc.getLicenseName())));

            if (oasDoc.getOasDocId() != null) {
                conditions.add(OAS_DOC.OAS_DOC_ID.ne(ULong.valueOf(oasDoc.getOasDocId())));
            }
            return dslContext.selectCount()
                    .from(OAS_DOC)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkOasDocTitleUniqueness(OasDoc oasDoc)
            throws ScoreDataAccessException {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(OAS_DOC.TITLE.eq(oasDoc.getTitle()));
        if (oasDoc.getOasDocId() != null) {
            conditions.add(OAS_DOC.OAS_DOC_ID.ne(ULong.valueOf(oasDoc.getOasDocId())));
        }

        if (oasDoc != null) {
            return dslContext.selectCount()
                    .from(OAS_DOC)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

    public class InsertOasMessageBodyArguments {
        private ULong userId;
        private ULong topLevelAsbiepId;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasMessageBodyArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasMessageBodyArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }
        public InsertOasMessageBodyArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }
        public InsertOasMessageBodyArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }
        public InsertOasMessageBodyArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasMessageBodyArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasMessageBodyArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getUserId() {
            return userId;
        }
        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasMessageBody(this);
        }
    }
    public InsertOasMessageBodyArguments insertOasMessageBody() {
        return new InsertOasMessageBodyArguments();
    }
    private ULong insertOasMessageBody(InsertOasMessageBodyArguments arguments) {
        return dslContext.insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .fetchOne().value1();
    }


    public class InsertOasResourceArguments {
        private ULong userId;
        private ULong oasDocId;
        private String path;
        private String ref;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasResourceArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasResourceArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasDocId() {
            return oasDocId;
        }

        public InsertOasResourceArguments setOasDocId(ULong oasDocId) {
            this.oasDocId = oasDocId;
            return this;
        }
        public String getPath() {
            return path;
        }
        public InsertOasResourceArguments setOasDocId(BigInteger oasDocId) {
            return setOasDocId(ULong.valueOf(oasDocId));
        }

        public InsertOasResourceArguments setPath(String path) {
            this.path = path;
            return this;
        }

        public String getRef() {
            return ref;
        }

        public InsertOasResourceArguments setRef(String ref) {
            this.ref = ref;
            return this;
        }

        public InsertOasResourceArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasResourceArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasResourceArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasResource(this);
        }
    }
    public InsertOasResourceArguments insertOasResource() {
        return new InsertOasResourceArguments();
    }
    private ULong insertOasResource(InsertOasResourceArguments arguments) {
        return dslContext.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.CREATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.OAS_DOC_ID, arguments.getOasDocId())
                .set(OAS_RESOURCE.PATH, arguments.getPath())
                .set(OAS_RESOURCE.REF, arguments.getRef())
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID)
                .fetchOne().value1();
    }

    public class InsertOasOperationArguments {
        private ULong userId;
        private ULong oasResourceId;
        private String verb;
        private String operationId;
        private String summary;
        private String description;
        private boolean deprecated;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasOperationArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasOperationArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasResourceId() {
            return oasResourceId;
        }

        public InsertOasOperationArguments setOasResourceId(BigInteger oasResourceId) {
            return setOasResourceId(ULong.valueOf(oasResourceId));
        }

        public InsertOasOperationArguments setOasResourceId(ULong oasResourceId) {
            this.oasResourceId = oasResourceId;
            return this;
        }

        public String getVerb() {
            return verb;
        }

        public InsertOasOperationArguments setVerb(String verb) {
            this.verb = verb;
            return this;
        }

        public String getOperationId() {
            return operationId;
        }

        public InsertOasOperationArguments setOperationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public String getSummary() {
            return summary;
        }

        public InsertOasOperationArguments setSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public InsertOasOperationArguments setDescription(String description) {
            this.description = description;
            return this;
        }
        public boolean isDeprecated() {
            return deprecated;
        }

        public InsertOasOperationArguments setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public InsertOasOperationArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasOperationArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasOperationArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasOperation(this);
        }
    }
    public InsertOasOperationArguments insertOasOperation() {
        return new InsertOasOperationArguments();
    }
    private ULong insertOasOperation(InsertOasOperationArguments arguments) {
        return dslContext.insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.CREATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.OAS_RESOURCE_ID, arguments.getOasResourceId())
                .set(OAS_OPERATION.VERB, arguments.getVerb())
                .set(OAS_OPERATION.OPERATION_ID, arguments.getOperationId())
                .set(OAS_OPERATION.SUMMARY, arguments.getSummary())
                .set(OAS_OPERATION.DESCRIPTION, arguments.getDescription())
                .set(OAS_OPERATION.DEPRECATED, (byte) (arguments.isDeprecated() ? 1 : 0))
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID)
                .fetchOne().value1();
    }




}
