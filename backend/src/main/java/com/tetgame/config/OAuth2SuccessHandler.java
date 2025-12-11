package com.tetgame.config;

import com.tetgame.config.JwtProvider;
import com.tetgame.modules.auth.oauth2.GoogleOAuth2Service;
import com.tetgame.modules.auth.oauth2.FacebookOAuth2Service;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.service.WalletService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final FacebookOAuth2Service facebookOAuth2Service;
    private final WalletService walletService;

    public OAuth2SuccessHandler(JwtProvider jwtProvider, GoogleOAuth2Service googleOAuth2Service, 
                                FacebookOAuth2Service facebookOAuth2Service, WalletService walletService) {
        this.jwtProvider = jwtProvider;
        this.googleOAuth2Service = googleOAuth2Service;
        this.facebookOAuth2Service = facebookOAuth2Service;
        this.walletService = walletService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Determine provider
        String registrationId = (String) request.getSession().getAttribute("oauth2AuthorizationRequest.registrationId");
        if (registrationId == null || registrationId.isEmpty()) {
            registrationId = extractRegistrationIdFromRequest(request);
        }

        User user = null;
        if ("google".equalsIgnoreCase(registrationId)) {
            user = googleOAuth2Service.processGoogleUser(oAuth2User);
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            user = facebookOAuth2Service.processFacebookUser(oAuth2User);
        }

        if (user != null) {
            String token = jwtProvider.generateToken(user.getUsername());
            long balance = walletService.getBalance(user.getId());
            String redirectUrl = String.format("http://localhost:5173/oauth/callback?token=%s&username=%s&avatar=%s&balance=%d",
                    URLEncoder.encode(token, StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getAvatarUrl() != null ? user.getAvatarUrl() : "", StandardCharsets.UTF_8),
                    balance);
            response.sendRedirect(redirectUrl);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process OAuth2 login");
        }
    }

    private String extractRegistrationIdFromRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("google")) return "google";
        if (requestUri.contains("facebook")) return "facebook";
        return "unknown";
    }
}
