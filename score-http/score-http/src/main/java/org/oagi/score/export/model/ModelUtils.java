package org.oagi.score.export.model;

import org.oagi.score.common.util.Utility;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;

public class ModelUtils {

    private ModelUtils() {}

    public static String getTypeName(DtManifestRecord dtManifestRecord, DtRecord dtRecord) {
        String den = dtManifestRecord.getDen();
        String name = Utility.denToName(den);
        String sixDigitId = dtRecord.getSixDigitId();
        if (StringUtils.hasLength(sixDigitId)) {
            return name + "_" + sixDigitId;
        } else {
            return name;
        }
    }
}
