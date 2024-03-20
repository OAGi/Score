package org.oagi.score.repo.api.impl.jooq.message;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.message.MessageReadRepository;
import org.oagi.score.repo.api.message.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.MESSAGE;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqMessageReadRepository
        extends JooqScoreRepository
        implements MessageReadRepository {

    public JooqMessageReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public GetMessageResponse getMessage(GetMessageRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        Record message = dslContext().select(MESSAGE.RECIPIENT_ID,
                MESSAGE.SUBJECT, MESSAGE.BODY, MESSAGE.BODY_CONTENT_TYPE,
                MESSAGE.CREATION_TIMESTAMP, MESSAGE.IS_READ,
                APP_USER.APP_USER_ID, APP_USER.LOGIN_ID, APP_USER.NAME, APP_USER.IS_DEVELOPER)
                .from(MESSAGE)
                .join(APP_USER).on(MESSAGE.SENDER_ID.eq(APP_USER.APP_USER_ID))
                .where(MESSAGE.MESSAGE_ID.eq(ULong.valueOf(request.getMessageId())))
                .fetchOptional().orElse(null);
        if (message == null) {
            throw new ScoreDataAccessException("Message with ID: '" + request.getMessageId() + "' does not exist.");
        }
        if (!message.get(MESSAGE.RECIPIENT_ID).equals(ULong.valueOf(requester.getUserId()))) {
            throw new ScoreDataAccessException("You do not have a permission to access this message.");
        }

        // Mark as read
        if (message.get(MESSAGE.IS_READ) == (byte) 0) {
            dslContext().update(MESSAGE)
                    .set(MESSAGE.IS_READ, (byte) 1)
                    .where(MESSAGE.MESSAGE_ID.eq(ULong.valueOf(request.getMessageId())))
                    .execute();
        }

        return new GetMessageResponse(request.getMessageId(),
                new ScoreUser(message.get(APP_USER.APP_USER_ID).toBigInteger(),
                        message.get(APP_USER.LOGIN_ID),
                        message.get(APP_USER.NAME),
                        (byte) 1 == message.get(APP_USER.IS_DEVELOPER) ? DEVELOPER : END_USER),
                message.get(MESSAGE.SUBJECT),
                message.get(MESSAGE.BODY), message.get(MESSAGE.BODY_CONTENT_TYPE),
                message.get(MESSAGE.CREATION_TIMESTAMP));
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                MESSAGE.MESSAGE_ID,
                MESSAGE.SUBJECT,
                MESSAGE.CREATION_TIMESTAMP,
                MESSAGE.IS_READ,
                APP_USER.as("sender").APP_USER_ID.as("sender_user_id"),
                APP_USER.as("sender").LOGIN_ID.as("sender_login_id"),
                APP_USER.as("sender").NAME.as("sender_name"),
                APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer"))
                .from(MESSAGE)
                .join(APP_USER.as("sender")).on(MESSAGE.SENDER_ID.eq(APP_USER.as("sender").APP_USER_ID))
                .join(APP_USER.as("recipient")).on(MESSAGE.RECIPIENT_ID.eq(APP_USER.as("recipient").APP_USER_ID));
    }

    private RecordMapper<Record, MessageList> mapper() {
        return record -> {
            MessageList messageList = new MessageList();
            messageList.setMessageId(record.get(MESSAGE.MESSAGE_ID).toBigInteger());
            messageList.setSubject(record.get(MESSAGE.SUBJECT));
            messageList.setRead(record.get(MESSAGE.IS_READ) == (byte) 1);
            messageList.setSender(new ScoreUser(
                    record.get(APP_USER.as("sender").APP_USER_ID.as("sender_user_id")).toBigInteger(),
                    record.get(APP_USER.as("sender").LOGIN_ID.as("sender_login_id")),
                    record.get(APP_USER.as("sender").NAME.as("sender_name")),
                    (byte) 1 == record.get(APP_USER.as("sender").IS_DEVELOPER.as("sender_is_developer")) ? DEVELOPER : END_USER
            ));
            messageList.setTimestamp(
                    Date.from(record.get(MESSAGE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return messageList;
        };
    }

    private Collection<Condition> getConditions(GetMessageListRequest request) {
        List<Condition> conditions = new ArrayList();

        conditions.add(APP_USER.as("recipient").APP_USER_ID.eq(ULong.valueOf(request.getRequester().getUserId())));

        if (!request.getSenderUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("sender").LOGIN_ID.in(
                    new HashSet<>(request.getSenderUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getCreateStartDate() != null) {
            conditions.add(MESSAGE.CREATION_TIMESTAMP.greaterOrEqual(request.getCreateStartDate()));
        }
        if (request.getCreateEndDate() != null) {
            conditions.add(MESSAGE.CREATION_TIMESTAMP.lessThan(request.getCreateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetMessageListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "sender":
                field = APP_USER.as("sender").LOGIN_ID;
                break;

            case "timestamp":
                field = MESSAGE.CREATION_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetMessageListResponse getMessageList(GetMessageListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);

        SelectConditionStep conditionStep = select().where(conditions);
        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetMessageListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    @Override
    public GetCountOfUnreadMessagesResponse getCountOfUnreadMessages(
            GetCountOfUnreadMessagesRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        List<Condition> conds = new ArrayList();
        conds.add(MESSAGE.RECIPIENT_ID.eq(ULong.valueOf(requester.getUserId())));
        conds.add(MESSAGE.IS_READ.eq((byte) 0));

        List<ULong> senderUserIds =
                request.getSenders().stream().map(e -> ULong.valueOf(e.getUserId())).collect(Collectors.toList());
        if (!senderUserIds.isEmpty()) {
            conds.add(senderUserIds.size() == 1 ?
                    MESSAGE.SENDER_ID.eq(senderUserIds.get(0)) :
                    MESSAGE.SENDER_ID.in(senderUserIds));
        }

        int countOfUnreadMessages = dslContext().selectCount()
                .from(MESSAGE)
                .where(conds)
                .fetchOptionalInto(Integer.class).orElse(0);
        return new GetCountOfUnreadMessagesResponse(countOfUnreadMessages);
    }

}
