package com.astavet.controller.admin;

import com.astavet.dto.request.auth.LoginRequest;
import com.astavet.dto.response.auth.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/session")
    public SessionResponse session(Authentication authentication, CsrfToken csrfToken) {
        csrfToken.getToken();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return new SessionResponse(false, null);
        }
        return new SessionResponse(true, authentication.getName());
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim(), request.password()));
        httpRequest.getSession(true);
        httpRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return new SessionResponse(true, authentication.getName());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
    }

}
