package com.saxion.proj.tfms.commons.security.resolvers;

import com.saxion.proj.tfms.commons.security.JwtUtil;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(UserContext.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new UserContext(null, null, null, false);
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.getEmailFromToken(token);
            boolean valid = jwtUtil.validateToken(token, email);

            if (!valid) return new UserContext(null, null, email, false);

            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getUserTypeFromToken(token);

            return new UserContext(userId, role, email, true);

        } catch (Exception e) {
            return new UserContext(null, null, null, false);
        }
    }
}
