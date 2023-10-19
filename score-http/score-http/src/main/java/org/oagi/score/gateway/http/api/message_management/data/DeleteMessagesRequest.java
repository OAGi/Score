package org.oagi.score.gateway.http.api.message_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class DeleteMessagesRequest {

    private List<BigInteger> messageIdList;

}
