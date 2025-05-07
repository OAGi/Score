package org.oagi.score.gateway.http.api.message_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.message_management.model.MessageDetailsRecord;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.model.MessageListEntryRecord;
import org.oagi.score.gateway.http.api.message_management.repository.MessageQueryRepository;
import org.oagi.score.gateway.http.api.message_management.repository.criteria.MessageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MESSAGE;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class JooqMessageQueryRepository extends JooqBaseRepository implements MessageQueryRepository {

    public JooqMessageQueryRepository(DSLContext dslContext, ScoreUser requester,
                                      RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public int getCountOfUnreadMessages(UserId requesterId, Collection<UserId> senderIdList) {

        List<Condition> conds = new ArrayList();
        conds.add(MESSAGE.RECIPIENT_ID.eq(valueOf(requesterId)));
        conds.add(MESSAGE.IS_READ.eq((byte) 0));

        if (senderIdList != null && !senderIdList.isEmpty()) {
            conds.add(senderIdList.size() == 1 ?
                    MESSAGE.SENDER_ID.eq(valueOf(senderIdList.iterator().next())) :
                    MESSAGE.SENDER_ID.in(valueOf(senderIdList)));
        }

        int countOfUnreadMessages = dslContext().selectCount()
                .from(MESSAGE)
                .where(conds)
                .fetchOptionalInto(Integer.class).orElse(0);
        return countOfUnreadMessages;
    }

    @Override
    public MessageDetailsRecord getMessageDetails(UserId requesterId, MessageId messageId) {

        var queryBuilder = new GetMessageDetailsQueryBuilder();
        MessageDetailsRecord messageDetails = queryBuilder.select()
                .where(MESSAGE.MESSAGE_ID.eq(valueOf(messageId)))
                .fetchOne(queryBuilder.mapper());

        if (messageDetails == null) {
            throw new ScoreDataAccessException("Message with ID: '" + messageId + "' does not exist.");
        }
        if (!messageDetails.recipient().userId().equals(requesterId)) {
            throw new ScoreDataAccessException("You do not have a permission to access this message.");
        }

        markAsRead(messageId);
        return messageDetails;
    }

    private boolean markAsRead(MessageId messageId) {
        int numOfUpdatedRecords = dslContext().update(MESSAGE)
                .set(MESSAGE.IS_READ, (byte) 1)
                .where(MESSAGE.MESSAGE_ID.eq(valueOf(messageId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    private class GetMessageDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {

            return dslContext().select(
                            MESSAGE.MESSAGE_ID,
                            MESSAGE.SUBJECT, MESSAGE.BODY, MESSAGE.BODY_CONTENT_TYPE,
                            MESSAGE.IS_READ,
                            MESSAGE.CREATION_TIMESTAMP,

                            APP_USER.as("sender").APP_USER_ID.as("sender_user_id"),
                            APP_USER.as("sender").LOGIN_ID.as("sender_login_id"),
                            APP_USER.as("sender").NAME.as("sender_name"),
                            APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer"),
                            APP_USER.as("sender").IS_ADMIN.as("sender_is_admin"),

                            APP_USER.as("recipient").APP_USER_ID.as("recipient_user_id"),
                            APP_USER.as("recipient").LOGIN_ID.as("recipient_login_id"),
                            APP_USER.as("recipient").NAME.as("recipient_name"),
                            APP_USER.as("recipient").IS_DEVELOPER.as("recipient_is_developer"),
                            APP_USER.as("recipient").IS_ADMIN.as("recipient_is_admin")
                    )
                    .from(MESSAGE)
                    .join(APP_USER.as("sender")).on(MESSAGE.SENDER_ID.eq(APP_USER.as("sender").APP_USER_ID))
                    .join(APP_USER.as("recipient")).on(MESSAGE.RECIPIENT_ID.eq(APP_USER.as("recipient").APP_USER_ID));
        }

        private RecordMapper<org.jooq.Record, MessageDetailsRecord> mapper() {
            return record -> new MessageDetailsRecord(
                    new MessageId(record.get(MESSAGE.MESSAGE_ID).toBigInteger()),
                    record.get(MESSAGE.SUBJECT),
                    record.get(MESSAGE.BODY),
                    record.get(MESSAGE.BODY_CONTENT_TYPE),
                    record.get(MESSAGE.IS_READ) == (byte) 1,
                    new UserSummaryRecord(
                            new UserId(record.get(APP_USER.as("recipient").APP_USER_ID.as("recipient_user_id")).toBigInteger()),
                            record.get(APP_USER.as("recipient").LOGIN_ID.as("recipient_login_id")),
                            record.get(APP_USER.as("recipient").LOGIN_ID.as("recipient_name")),
                            ((byte) 1 == record.get(APP_USER.as("recipient").IS_ADMIN.as("recipient_is_admin"))) ?
                                    Arrays.asList(
                                            ((byte) 1 == record.get(APP_USER.as("recipient").IS_DEVELOPER.as("recipient_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                            ScoreRole.ADMINISTRATOR
                                    ) :
                                    Arrays.asList(
                                            ((byte) 1 == record.get(APP_USER.as("recipient").IS_DEVELOPER.as("recipient_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                                    )
                    ),
                    new WhoAndWhen(
                            new UserSummaryRecord(
                                    new UserId(record.get(APP_USER.as("sender").APP_USER_ID.as("sender_user_id")).toBigInteger()),
                                    record.get(APP_USER.as("sender").LOGIN_ID.as("sender_login_id")),
                                    record.get(APP_USER.as("sender").LOGIN_ID.as("sender_name")),
                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_ADMIN.as("sender_is_admin"))) ?
                                            Arrays.asList(
                                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                                    ScoreRole.ADMINISTRATOR
                                            ) :
                                            Arrays.asList(
                                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                                            )
                            ),
                            toDate(record.get(MESSAGE.CREATION_TIMESTAMP))
                    ));
        }
    }

    @Override
    public ResultAndCount<MessageListEntryRecord> getMessageList(
            UserId requesterId, MessageListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetMessageListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(requesterId, filterCriteria));
        int count = dslContext().fetchCount(where);
        List<MessageListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetMessageListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {

            return dslContext().select(
                            MESSAGE.MESSAGE_ID,
                            MESSAGE.SUBJECT, MESSAGE.BODY,
                            MESSAGE.IS_READ,
                            MESSAGE.CREATION_TIMESTAMP,

                            APP_USER.as("sender").APP_USER_ID.as("sender_user_id"),
                            APP_USER.as("sender").LOGIN_ID.as("sender_login_id"),
                            APP_USER.as("sender").NAME.as("sender_name"),
                            APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer"),
                            APP_USER.as("sender").IS_ADMIN.as("sender_is_admin")
                    )
                    .from(MESSAGE)
                    .join(APP_USER.as("sender")).on(MESSAGE.SENDER_ID.eq(APP_USER.as("sender").APP_USER_ID))
                    .join(APP_USER.as("recipient")).on(MESSAGE.RECIPIENT_ID.eq(APP_USER.as("recipient").APP_USER_ID));
        }

        Collection<? extends Condition> conditions(UserId requesterId, MessageListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            conditions.add(APP_USER.as("recipient").APP_USER_ID.eq(valueOf(requesterId)));

            if (hasLength(filterCriteria.subject())) {
                conditions.addAll(contains(filterCriteria.subject(), MESSAGE.SUBJECT));
            }
            if (filterCriteria.senderLoginIdSet() != null && !filterCriteria.senderLoginIdSet().isEmpty()) {
                conditions.add(APP_USER.as("sender").LOGIN_ID.in(filterCriteria.senderLoginIdSet()));
            }
            if (filterCriteria.createdTimestampRange() != null) {
                if (filterCriteria.createdTimestampRange().after() != null) {
                    conditions.add(MESSAGE.CREATION_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.createdTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.createdTimestampRange().before() != null) {
                    conditions.add(MESSAGE.CREATION_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.createdTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "sender":
                        field = APP_USER.as("sender").LOGIN_ID;
                        break;

                    case "timestamp":
                        field = MESSAGE.CREATION_TIMESTAMP;
                        break;

                    default:
                        continue;
                }

                if (sort.direction() == DESC) {
                    sortFields.add(field.desc());
                } else {
                    sortFields.add(field.asc());
                }
            }

            return sortFields;
        }

        List<MessageListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends Record> finalStep;
            if (sortFields == null || sortFields.isEmpty()) {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep;
                }
            } else {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.orderBy(sortFields)
                            .limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep.orderBy(sortFields);
                }
            }
            return finalStep.fetch(this.mapper());
        }

        private RecordMapper<org.jooq.Record, MessageListEntryRecord> mapper() {
            return record -> new MessageListEntryRecord(
                    new MessageId(record.get(MESSAGE.MESSAGE_ID).toBigInteger()),
                    record.get(MESSAGE.SUBJECT),
                    record.get(MESSAGE.BODY),
                    record.get(MESSAGE.IS_READ) == (byte) 1,
                    new WhoAndWhen(
                            new UserSummaryRecord(
                                    new UserId(record.get(APP_USER.as("sender").APP_USER_ID.as("sender_user_id")).toBigInteger()),
                                    record.get(APP_USER.as("sender").LOGIN_ID.as("sender_login_id")),
                                    record.get(APP_USER.as("sender").LOGIN_ID.as("sender_name")),
                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_ADMIN.as("sender_is_admin"))) ?
                                            Arrays.asList(
                                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer"))) ? ScoreRole.DEVELOPER : ScoreRole.END_USER,
                                                    ScoreRole.ADMINISTRATOR
                                            ) :
                                            Arrays.asList(
                                                    ((byte) 1 == record.get(APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer")) ? ScoreRole.DEVELOPER : ScoreRole.END_USER)
                                            )
                            ),
                            toDate(record.get(MESSAGE.CREATION_TIMESTAMP))
                    ));
        }

    }
}
