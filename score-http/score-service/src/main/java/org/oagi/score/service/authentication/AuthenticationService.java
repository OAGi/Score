package org.oagi.score.service.authentication;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.user.model.GetScoreUserRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public ScoreUser asScoreUser(AuthenticatedPrincipal user) {
        GetScoreUserRequest request;
        if (user instanceof User) {
            request = new GetScoreUserRequest().withUserName(user.getName());
        } else if (user instanceof OAuth2User) {
            request = new GetScoreUserRequest().withOidcSub(user.getName());
        } else {
            throw new IllegalStateException();
        }

        return scoreRepositoryFactory.createScoreUserReadRepository()
                .getScoreUser(request)
                .getUser();
    }

}
