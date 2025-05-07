package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.common.util.Utility;

public class ModelUtils {

    private ModelUtils() {}

    public static String getTypeName(DtSummaryRecord dt) {
        String den = dt.den();
        String name = Utility.denToName(den);
        String sixDigitId = dt.sixDigitId();
        if (StringUtils.hasLength(sixDigitId)) {
            return name + "_" + sixDigitId;
        } else {
            return name;
        }
    }
}
