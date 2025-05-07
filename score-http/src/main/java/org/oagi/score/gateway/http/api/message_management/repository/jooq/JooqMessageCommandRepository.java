package org.oagi.score.gateway.http.api.message_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.repository.MessageCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.MessageRecord;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MESSAGE;

public class JooqMessageCommandRepository extends JooqBaseRepository implements MessageCommandRepository {

    public JooqMessageCommandRepository(DSLContext dslContext, ScoreUser requester,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public Map<UserId, MessageId> createMessages(Collection<UserId> recipientIdList,
                                                 String subject, String body, String bodyContentType) {

        Map<UserId, MessageId> messageIds = new HashMap();
        for (UserId recipientId : recipientIdList) {
            MessageRecord message = new MessageRecord();
            message.setSenderId(valueOf(requester().userId()));
            message.setRecipientId(valueOf(recipientId));
            message.setSubject(subject);
            message.setBody(body);
            message.setBodyContentType(bodyContentType);
            message.setIsRead((byte) 0);
            message.setCreationTimestamp(LocalDateTime.now());
            MessageId messageId = new MessageId(
                    dslContext().insertInto(MESSAGE)
                            .set(message)
                            .returning(MESSAGE.MESSAGE_ID)
                            .fetchOne().getMessageId().toBigInteger()
            );
            messageIds.put(recipientId, messageId);
        }

        return messageIds;
    }

    @Override
    public int deleteMessages(Collection<MessageId> messageIdList) {
        if (messageIdList == null || messageIdList.isEmpty()) {
            return 0;
        }

        Result<Record2<ULong, ULong>> result =
                dslContext().select(MESSAGE.MESSAGE_ID, MESSAGE.RECIPIENT_ID)
                        .from(MESSAGE)
                        .where(MESSAGE.MESSAGE_ID.in(valueOf(messageIdList)))
                        .fetch();

        if (result.stream().map(e -> e.get(MESSAGE.RECIPIENT_ID).longValue())
                .filter(e -> e != requester().userId().value().longValue()).count() > 0) {
            throw new ScoreDataAccessException("You do not have a permission to access this message.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(MESSAGE)
                .where(MESSAGE.MESSAGE_ID.in(valueOf(messageIdList)))
                .execute();
        return numOfDeletedRecords;
    }
}
