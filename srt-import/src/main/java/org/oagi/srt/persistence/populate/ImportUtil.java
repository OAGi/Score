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

    private int userId;
    private int releaseId;
    private int namespaceId;

    public int getUserId() {
        if (userId == 0) {
            userId = userRepository.findAppUserIdByLoginId("oagis");
        }
        return userId;
    }

    public int getReleaseId() {
        if (releaseId == 0) {
            releaseId = releaseRepository.findReleaseIdByReleaseNum(OAGIS_VERSION);
        }
        return releaseId;
    }

    public int getNamespaceId() {
        if (namespaceId == 0) {
            namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");
        }
        return namespaceId;
    }

}
