package org.oagi.srt.persistence.populate;

import org.oagi.srt.repository.NamespaceRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;

@Component
public class ImportUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    private long userId;
    private long releaseId;
    private long namespaceId;

    public long getUserId() {
        if (userId == 0L) {
            userId = userRepository.findAppUserIdByLoginId("oagis");
        }
        return userId;
    }

    public long getReleaseId() {
        if (releaseId == 0L) {
            releaseId = releaseRepository.findReleaseIdByReleaseNum(Double.toString(OAGIS_VERSION));
        }
        return releaseId;
    }

    public long getNamespaceId() {
        if (namespaceId == 0L) {
            namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");
        }
        return namespaceId;
    }

}
