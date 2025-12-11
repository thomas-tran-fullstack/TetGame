package com.tetgame.modules.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.service.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoogleOAuth2Service {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public GoogleOAuth2Service(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public User processGoogleUser(OAuth2User oAuth2User) {
        String googleId = oAuth2User.getName();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Optional<User> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            User u = existing.get();
            if (u.getOauthId() == null) {
                u.setOauthProvider("google");
                u.setOauthId(googleId);
                u.setAvatarUrl(picture);
                return userService.updateUser(u);
            }
            return u;
        }

        // Create new user from Google
        User newUser = User.builder()
                .email(email)
                .fullName(name)
                .username(generateUsernameFromEmail(email))
                .oauthProvider("google")
                .oauthId(googleId)
                .avatarUrl(picture)
                .enabled(true)
                .build();

        return userService.createUser(newUser);
    }

    private String generateUsernameFromEmail(String email) {
        return email.split("@")[0] + "_google_" + System.currentTimeMillis();
    }
}
