package org.oagi.score.gateway.http.api.cc_management.model.seq_key;

public interface SeqKeySupportable {

    SeqKeyId seqKeyId();

    SeqKeyId prevSeqKeyId();

    SeqKeyId nextSeqKeyId();

}
