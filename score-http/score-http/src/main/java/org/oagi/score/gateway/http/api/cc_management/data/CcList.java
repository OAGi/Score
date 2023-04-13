package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
public class CcList {

    private CcType type;
    private BigInteger manifestId;
    private String guid;
    private String den;
    private String definition;
    private String module;
    private String name;

    public String getModule() {
        return !StringUtils.hasLength(module) ? "" : module;
    }

    private String definitionSource;
    private OagisComponentType oagisComponentType;
    private String dtType;
    private String owner;
    private CcState state;
    private String revision;
    private boolean deprecated;
    private String lastUpdateUser;
    private Date lastUpdateTimestamp;
    private String releaseNum;
    private BigInteger id;

    private boolean ownedByDeveloper;

    private BigInteger basedManifestId;
    private String sixDigitId;
    private String defaultValueDomain;

    private boolean newComponent;
    private List<ShortTag> tagList = Collections.emptyList();

    public CcList() {
    }

    public CcList(CcType type, BigInteger manifestId, String guid, String den, String definition, String module,
                  String name, String definitionSource, Optional<String> oagisComponentType, String dtType,
                  String owner, CcState state, String revision, boolean deprecated,
                  String lastUpdateUser, Date lastUpdateTimestamp, String releaseNum, BigInteger id,
                  boolean ownedByDeveloper, String sixDigitId, String defaultValueDomain) {
        this.type = type;
        this.manifestId = manifestId;
        this.guid = guid;
        this.den = den;
        this.definition = definition;
        this.module = module;
        this.name = name;
        this.definitionSource = definitionSource;
        if (oagisComponentType.isPresent()) {
            this.oagisComponentType = OagisComponentType.valueOf(Integer.parseInt(oagisComponentType.get()));
        }
        this.dtType = dtType;
        this.owner = owner;
        this.state = state;
        this.revision = revision;
        this.deprecated = deprecated;
        this.lastUpdateUser = lastUpdateUser;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.releaseNum = releaseNum;
        this.id = id;
        this.ownedByDeveloper = ownedByDeveloper;
        this.sixDigitId = sixDigitId;
        this.defaultValueDomain = defaultValueDomain;
    }

}
