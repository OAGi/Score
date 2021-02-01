package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAccNode extends CcNode {

    private CcType type = CcType.ACC;
    private BigInteger accId = BigInteger.ZERO;
    private String den;
    private String guid;
    private String objectClassTerm;
    private BigInteger basedAccManifestId = BigInteger.ZERO;
    private int oagisComponentType;
    private String definition;
    private boolean group;
    private boolean isDeprecated;
    private boolean isAbstract;
    private BigInteger manifestId = BigInteger.ZERO;
    private boolean hasExtension;
    private String accType;

    @Override
    public BigInteger getId() {
        return accId;
    }
}
