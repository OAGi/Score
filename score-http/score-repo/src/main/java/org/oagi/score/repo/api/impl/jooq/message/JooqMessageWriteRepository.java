package org.oagi.score.repo.api.impl.jooq.message;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (request.getMessageIdList() == null || request.getMessageIdList().isEmpty()) {
            return;
        }

        List<ULong> messageIdList = request.getMessageIdList().stream()
                .map(e -> ULong.valueOf(e)).collect(Collectors.toList());

        Result<Record2<ULong, ULong>> result = dslContext().select(MESSAGE.MESSAGE_ID, MESSAGE.RECIPIENT_ID)
                .from(MESSAGE)
                .where(MESSAGE.MESSAGE_ID.in(messageIdList))
                .fetch();

        if (result.stream().map(e -> e.get(MESSAGE.RECIPIENT_ID).longValue())
                .filter(e -> e != requester.getUserId().longValue()).count() > 0) {
            throw new ScoreDataAccessException("You do not have a permission to access this message.");
        }

        dslContext().deleteFrom(MESSAGE)
                .where(MESSAGE.MESSAGE_ID.in(messageIdList))
                .execute();
    }

}
