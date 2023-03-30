package org.oagi.score.repo.api.impl.jooq.message;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.MessageRecord;
import org.oagi.score.repo.api.message.MessageWriteRepository;
import org.oagi.score.repo.api.message.model.DiscardMessageRequest;
import org.oagi.score.repo.api.message.model.SendMessageRequest;
import org.oagi.score.repo.api.message.model.SendMessageResponse;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.MESSAGE;

public class JooqMessageWriteRepository
        extends JooqScoreRepository
        implements MessageWriteRepository {

    public JooqMessageWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws ScoreDataAccessException {
        ScoreUser sender = request.getRequester();
        Map<ScoreUser, BigInteger> messageIds = new HashMap();
        for (ScoreUser recipient : request.getRecipients()) {
            MessageRecord message = new MessageRecord();
            message.setSenderId(ULong.valueOf(sender.getUserId()));
            message.setRecipientId(ULong.valueOf(recipient.getUserId()));
            message.setSubject(request.getSubject());
            message.setBody(request.getBody());
            message.setBodyContentType(request.getBodyContentType());
            message.setIsRead((byte) 0);
            message.setCreationTimestamp(LocalDateTime.now());
            ULong messageId = dslContext().insertInto(MESSAGE)
                    .set(message)
                    .returning(MESSAGE.MESSAGE_ID)
                    .fetchOne().getMessageId();
            messageIds.put(recipient, messageId.toBigInteger());
        }

        return new SendMessageResponse(messageIds);
    }

    @Override
    public void discardMessage(DiscardMessageRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        if (requester == null) {
            throw new IllegalArgumentException();
        }

        ULong recipientId = dslContext().select(MESSAGE.RECIPIENT_ID)
                .from(MESSAGE)
                .where(MESSAGE.MESSAGE_ID.eq(ULong.valueOf(request.getMessageId())))
                .fetchOptionalInto(ULong.class).orElse(null);

        if (!recipientId.equals(ULong.valueOf(requester.getUserId()))) {
            throw new ScoreDataAccessException("You do not have a permission to access this message.");
        }

        dslContext().deleteFrom(MESSAGE)
                .where(MESSAGE.MESSAGE_ID.eq(ULong.valueOf(request.getMessageId())))
                .execute();
    }

}
