package org.oagi.score.service.corecomponent.seqkey;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.corecomponent.model.EntityType;
import org.oagi.score.repo.api.corecomponent.seqkey.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.service.corecomponent.seqkey.MoveTo.FIRST;
import static org.oagi.score.service.corecomponent.seqkey.MoveTo.LAST;

public class SeqKeyHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ScoreRepositoryFactory scoreRepositoryFactory;
    private ScoreUser requester;

    private SeqKey head;
    private SeqKey tail;
    private SeqKey current;

    public SeqKeyHandler(ScoreRepositoryFactory scoreRepositoryFactory, ScoreUser requester) {
        this.scoreRepositoryFactory = scoreRepositoryFactory;
        this.requester = requester;
    }

    public void initAscc(BigInteger fromAccManifestId, BigInteger seqKeyId, BigInteger associationId) {
        init(fromAccManifestId, seqKeyId, SeqKeyType.ASCC, associationId);
    }

    public void initBcc(BigInteger fromAccManifestId, BigInteger seqKeyId, BigInteger associationId) {
        init(fromAccManifestId, seqKeyId, SeqKeyType.BCC, associationId);
    }

    private void init(BigInteger fromAccManifestId, BigInteger seqKeyId, SeqKeyType type, BigInteger associationManifestId) {
        GetSeqKeyRequest getSeqKeyRequest = new GetSeqKeyRequest(this.requester)
                .withFromAccManifestId(fromAccManifestId);
        GetSeqKeyResponse response = scoreRepositoryFactory.createSeqKeyReadRepository()
                .getSeqKey(getSeqKeyRequest);

        if (response.getSeqKey() != null) {
            for (SeqKey seqKey : response.getSeqKey()) {
                if (seqKey.getPrevSeqKey() == null) {
                    this.head = seqKey;
                }
                if (seqKey.getNextSeqKey() == null) {
                    this.tail = seqKey;
                }
                if (seqKey.getSeqKeyId().equals(seqKeyId)) {
                    this.current = seqKey;
                }
            }
        }

        if (this.current == null) {
            this.current = scoreRepositoryFactory.createSeqKeyWriteRepository()
                    .createSeqKey(new CreateSeqKeyRequest(this.requester)
                            .withFromAccManifestId(fromAccManifestId)
                            .withType(type)
                            .withManifestId(associationManifestId))
                    .getSeqKey();
        }
    }

    public SeqKey getHead() {
        return head;
    }

    public SeqKey getTail() {
        return tail;
    }

    public SeqKey getCurrent() {
        return current;
    }

    public void moveTo(int pos) {
        if (pos < -1) {
            throw new IllegalArgumentException();
        }

        if (pos == 0) {
            this.moveTo(FIRST);
        } else if (pos == -1) {
            this.moveTo(LAST);
        } else {
            SeqKey target = this.head;
            pos--;
            while (pos > 0) {
                target = target.getNextSeqKey();
                pos--;
            }
            moveAfter(target);
        }
    }

    public void moveTo(MoveTo to) {
        switch (to) {
            case FIRST:
                if (this.head != null) {
                    if (this.head.equals(this.current)) {
                        break;
                    }
                    brokeLinks();

                    this.current.setPrevSeqKey(null);
                    this.current.setNextSeqKey(this.head);
                    update(this.current);
                    this.head.setPrevSeqKey(this.current);
                    update(this.head);

                    this.head = this.current;
                }

                break;

            case LAST_OF_ATTR:
                SeqKey target = this.head;

                while (target != null &&
                        target.getBccManifestId() != null &&
                        target.getEntityType() == EntityType.Attribute) {
                    target = target.getNextSeqKey();
                }

                if (target != null && target.getPrevSeqKey() != null) {
                    moveAfter(target.getPrevSeqKey());
                } else {
                    moveTo(FIRST);
                }

                break;

            case LAST:
                if (this.tail != null) {
                    if (this.tail.equals(this.current)) {
                        break;
                    }
                    brokeLinks();

                    this.current.setPrevSeqKey(this.tail);
                    this.current.setNextSeqKey(null);
                    update(this.current);
                    this.tail.setNextSeqKey(this.current);
                    update(this.tail);

                    this.tail = this.current;
                }
                break;
        }
    }

    public void deleteCurrent() {
        brokeLinks();

        scoreRepositoryFactory.createSeqKeyWriteRepository()
                .deleteSeqKey(new DeleteSeqKeyRequest(this.requester)
                        .withSeqKeyId(this.current.getSeqKeyId()));

        this.current = null;
    }

    private void brokeLinks() {
        SeqKey prev = this.current.getPrevSeqKey();
        SeqKey next = this.current.getNextSeqKey();

        if (prev != null) {
            prev.setNextSeqKey(next);
        }

        if (next != null) {
            next.setPrevSeqKey(prev);
        }

        update(prev);
        update(next);

        this.current.setPrevSeqKey(null);
        this.current.setNextSeqKey(null);
        update(this.current);
    }

    public void moveAfter(SeqKey after) {
        scoreRepositoryFactory.createSeqKeyWriteRepository()
                .moveAfter(new MoveAfterRequest(this.requester)
                        .withItem(this.current)
                        .withAfter(after)
                );
    }

    private void update(SeqKey seqKey) {
        if (seqKey == null) {
            return;
        }

        UpdateSeqKeyResponse response = scoreRepositoryFactory.createSeqKeyWriteRepository()
                .updateSeqKey(new UpdateSeqKeyRequest(requester).withSeqKey(seqKey));
        if (response != null && seqKey.getSeqKeyId().equals(response.getSeqKeyId())) {
            logger.debug(seqKey + " changed.");
        }
    }

    public static List<SeqKeySupportable> sort(List<SeqKeySupportable> seqKeyList) {
        if (seqKeyList == null || seqKeyList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<BigInteger, SeqKeySupportable> seqKeyMap = seqKeyList.stream()
                .collect(Collectors.toMap(
                        SeqKeySupportable::getSeqKeyId, Function.identity()));

        /*
         * To ensure that every CC has different next seq_key id.
         * If not, it would cause a circular reference.
         */
        if (seqKeyMap.size() - 1 != seqKeyMap.values().stream()
                .filter(e -> e.getNextSeqKeyId() != null)
                .map(e -> e.getNextSeqKeyId())
                .collect(Collectors.toSet()).size()) {
            throw new IllegalStateException();
        }

        SeqKeySupportable head = seqKeyMap.values().stream()
                .filter(e -> e.getPrevSeqKeyId() == null)
                .findFirst().orElse(null);

        List<SeqKeySupportable> sorted = new ArrayList();
        SeqKeySupportable current = head;
        while (current != null) {
            sorted.add(current);
            current = seqKeyMap.get(current.getNextSeqKeyId());
        }
        return sorted;
    }

}
