package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.repository.SeqKeyCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.SeqKeyQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo.FIRST;
import static org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo.LAST;

public class SeqKeyHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SeqKeyQueryRepository query;
    private SeqKeyCommandRepository command;

    private SeqKeySummaryRecord head;
    private SeqKeySummaryRecord tail;
    private SeqKeySummaryRecord current;

    public SeqKeyHandler(SeqKeyQueryRepository query,
                         SeqKeyCommandRepository command) {
        this.query = query;
        this.command = command;
    }

    public void init(AccManifestId fromAccManifestId, SeqKeyId seqKeyId, ManifestId associationManifestId) {
        List<SeqKeySummaryRecord> seqKeySummaryList = sort(query.getSeqKeySummaryList(fromAccManifestId));
        for (SeqKeySummaryRecord seqKey : seqKeySummaryList) {
            if (seqKey.prevSeqKeyId() == null) {
                this.head = seqKey;
            }
            if (seqKey.nextSeqKeyId() == null) {
                this.tail = seqKey;
            }
            if (seqKey.seqKeyId().equals(seqKeyId)) {
                this.current = seqKey;
            }
        }

        if (this.current == null) {
            SeqKeyId newSeqKeyId;
            if (associationManifestId instanceof AsccManifestId) {
                newSeqKeyId = command.create(fromAccManifestId, (AsccManifestId) associationManifestId);
            } else {
                newSeqKeyId = command.create(fromAccManifestId, (BccManifestId) associationManifestId);
            }
            this.current = query.getSeqKeySummary(newSeqKeyId);
        }
    }

    public SeqKeySummaryRecord getHead() {
        return head;
    }

    public SeqKeySummaryRecord getTail() {
        return tail;
    }

    public SeqKeySummaryRecord getCurrent() {
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
            SeqKeySummaryRecord target = this.head;
            pos--;
            while (pos > 0) {
                target = query.getSeqKeySummary(target.nextSeqKeyId());
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

                    command.updatePrev(this.current.seqKeyId(), null);
                    command.updateNext(this.current.seqKeyId(), this.head.seqKeyId());

                    command.updatePrev(this.head.seqKeyId(), this.current.seqKeyId());

                    this.head = this.current;
                }

                break;

            case LAST_OF_ATTR:
                SeqKeySummaryRecord target = this.head;

                while (target != null &&
                        target.bccManifestId() != null &&
                        target.entityType() == EntityType.Attribute) {
                    target = query.getSeqKeySummary(target.nextSeqKeyId());
                }

                if (target != null && target.prevSeqKeyId() != null) {
                    moveAfter(query.getSeqKeySummary(target.prevSeqKeyId()));
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

                    command.updatePrev(this.current.seqKeyId(), this.tail.seqKeyId());
                    command.updateNext(this.current.seqKeyId(), null);

                    command.updateNext(this.tail.seqKeyId(), this.current.seqKeyId());

                    this.tail = this.current;
                }
                break;
        }
    }

    public void deleteCurrent() {
        brokeLinks();

        command.delete(this.current.seqKeyId());

        this.current = null;
    }

    private void brokeLinks() {
        SeqKeyId prev = this.current.prevSeqKeyId();
        SeqKeyId next = this.current.nextSeqKeyId();

        if (prev != null) {
            this.command.updateNext(prev, next);
        }

        if (next != null) {
            this.command.updatePrev(next, prev);
        }

        this.command.updatePrev(this.current.seqKeyId(), null);
        this.command.updateNext(this.current.seqKeyId(), null);
    }

    public void moveAfter(SeqKeySummaryRecord after) {
        command.moveAfter(this.current, after);
    }

    public static <T extends SeqKeySupportable> List<T> sort(List<T> seqKeyList) {
        if (seqKeyList == null || seqKeyList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<SeqKeyId, T> seqKeyMap = seqKeyList.stream()
                .collect(Collectors.toMap(
                        SeqKeySupportable::seqKeyId, Function.identity()));

        /*
         * To ensure that every CC has different next seq_key id.
         * If not, it would cause a circular reference.
         */
        if (seqKeyMap.size() - 1 != seqKeyMap.values().stream()
                .filter(e -> e.nextSeqKeyId() != null)
                .map(e -> e.nextSeqKeyId())
                .collect(Collectors.toSet()).size()) {
            throw new IllegalStateException();
        }

        T head = seqKeyMap.values().stream()
                .filter(e -> e.prevSeqKeyId() == null)
                .findFirst().orElse(null);

        List<T> sorted = new ArrayList();
        T current = head;
        while (current != null) {
            sorted.add(current);
            current = seqKeyMap.get(current.nextSeqKeyId());
        }
        return sorted;
    }

}
