package org.oagi.score.service.bie;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.bie.model.BieDocumentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@Transactional(readOnly = true)
public class BieReadService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public BieDocument getBieDocument(ScoreUser requester, BigInteger topLevelAsbiepId) {
        BieDocument bieDocument = BieDocumentBuilder.buildFrom(
                scoreRepositoryFactory.createBieReadRepository(),
                scoreRepositoryFactory.createCcReadRepository())
                .withTopLevelAsbiepId(topLevelAsbiepId)
                .onlyUsed(true)
                .by(requester)
                .build();

        return bieDocument;
    }

}
