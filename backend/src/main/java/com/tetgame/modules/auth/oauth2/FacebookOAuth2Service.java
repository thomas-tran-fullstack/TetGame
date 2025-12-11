package com.tetgame.modules.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.service.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FacebookOAuth2Service {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public FacebookOAuth2Service(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public User processFacebookUser(OAuth2User oAuth2User) {
        String facebookId = oAuth2User.getName();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        // Facebook picture is nested in a map
        String picture = null;
        Object pictureObj = oAuth2User.getAttribute("picture");
        if (pictureObj != null) {
            try {
                if (pictureObj instanceof java.util.Map) {
                    java.util.Map<String, Object> picMap = (java.util.Map) pictureObj;
                    Object dataObj = picMap.get("data");
                    if (dataObj instanceof java.util.Map) {
                        java.util.Map<String, Object> dataMap = (java.util.Map) dataObj;
                        picture = (String) dataMap.get("url");
                    }
                }
            } catch (Exception ex) {
                // fallback to null
            }
        }

        Optional<User> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            User u = existing.get();
            if (u.getOauthId() == null) {
                u.setOauthProvider("facebook");
                u.setOauthId(facebookId);
                u.setAvatarUrl(picture);
                return userService.updateUser(u);
            }
            return u;
        }

        // Create new user from Facebook
        User newUser = User.builder()
                .email(email)
                .fullName(name)
                .username(generateUsernameFromEmail(email))
                .oauthProvider("facebook")
                .oauthId(facebookId)
                .avatarUrl(picture)
                .enabled(true)
                .build();

        return userService.createUser(newUser);
    }

    private String generateUsernameFromEmail(String email) {
        return email.split("@")[0] + "_fb_" + System.currentTimeMillis();
    }
}
