package org.oagi.score.gateway.http.configuration.security;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Gives a method of a controller the {@link ScoreUser} of the request when it has a parameter of that type, so that the controller
 * does not turn the principal into the user itself. The user is read once, before the method (and what {@link
 * org.oagi.score.gateway.http.common.model.AccessControl} checks of it) is called, and the method and the check have the same one.
 *
 * <p>A request that is not signed in has no user: the parameter is {@code null}, which {@code AccessControl} refuses.</p>
 */
@Component
public class ScoreUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectProvider<SessionService> sessionService;

    public ScoreUserArgumentResolver(ObjectProvider<SessionService> sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ScoreUser.class.equals(parameter.getParameterType());
    }

    @Override
    public ScoreUser resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                     WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return null;
        }
        return sessionService.getObject().asScoreUser(authentication);
    }
}
