package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CcListTypes {

    private boolean acc;
    private boolean asccp;
    private boolean bccp;
    private boolean ascc;
    private boolean bcc;
    private boolean bdt;

    public static CcListTypes fromString(String str) {
        if (str != null) {
            str = str.trim();
        }

        CcListTypes types = new CcListTypes();
        if (!StringUtils.hasLength(str)) {
            types.setAcc(true);
            types.setAsccp(true);
            types.setBccp(true);
            types.setAscc(true);
            types.setBcc(true);
            types.setBdt(true);
        } else {
            for (String type : str.split(",")) {
                switch (CcType.valueOf(type.toUpperCase())) {
                    case ACC:
                        types.setAcc(true);
                        break;

                    case ASCCP:
                        types.setAsccp(true);
                        break;

                    case BCCP:
                        types.setBccp(true);
                        break;

                    case ASCC:
                        types.setAscc(true);
                        break;

                    case BCC:
                        types.setBcc(true);
                        break;

                    case BDT:
                        types.setBdt(true);
                        break;
                }
            }
        }

        return types;
    }

}
