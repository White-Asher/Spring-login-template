package com.springboot.template.auth.service;

import com.springboot.template.auth.entity.ProviderType;
import com.springboot.template.auth.entity.RoleType;
import com.springboot.template.auth.entity.UserPrincipal;
import com.springboot.template.auth.exception.OAuthProviderMissMatchException;
import com.springboot.template.auth.info.OAuth2UserInfo;
import com.springboot.template.auth.info.OAuth2UserInfoFactory;
import com.springboot.template.user.entity.User;
import com.springboot.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 위 코드는 Spring Security와 OAuth2.0을 이용한 소셜 로그인에 사용되는 CustomOAuth2UserService 클래스입니다. <br>
 * `DefaultOAuth2UserService`를 상속받아서 OAuth2User 인터페이스를 구현하는 `loadUser()` 메소드를 오버라이드합니다. <br>
 * `loadUser()` 메소드에서는 `super.loadUser(userRequest)`를 호출하여 OAuth2User 객체를 가져오고, `process()` 메소드를 호출하여 해당 객체를 처리합니다. <br>
 * `process()` 메소드에서는 OAuth2UserRequest 객체와 OAuth2User 객체를 인자로 받아서, 해당 소셜 미디어 타입에 대한 정보를 바탕으로 OAuth2UserInfo 객체를 생성하고, 해당 사용자 정보가 데이터베이스에 있는지 확인한 후, 없으면 새로운 User 객체를 생성하고, 있으면 해당 User 객체를 업데이트합니다. <br>
 * `createUser()` 메소드에서는 OAuth2UserInfo 객체와 소셜 미디어 타입을 인자로 받아서 새로운 User 객체를 생성하고, 해당 객체를 저장합니다. <br>
 * `updateUser()` 메소드에서는 User 객체와 OAuth2UserInfo 객체를 인자로 받아서 User 객체를 업데이트합니다. <br>
 * 마지막으로 **`UserPrincipal.create()`**를 호출하여 UserPrincipal 객체를 생성하고, 이를 반환합니다. <br>
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        User savedUser = userRepository.findByUserId(userInfo.getId());

        if (savedUser != null) {
            if (providerType != savedUser.getProviderType()) {
                throw new OAuthProviderMissMatchException(
                        "Looks like you're signed up with " + providerType +
                                " account. Please use your " + savedUser.getProviderType() + " account to login."
                );
            }
            updateUser(savedUser, userInfo);
        } else {
            savedUser = createUser(userInfo, providerType);
        }

        return UserPrincipal.create(savedUser, user.getAttributes());
    }

    // 소셜 로그인 회원가입 (소셜 로그인 시 DB에 없으면 회원가입 로직을 수행함)
    private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .userId(userInfo.getId())
                .userPassword("NO_PASS")
                .userName(userInfo.getName())
                .userEmail(userInfo.getEmail() != null ? userInfo.getEmail() : "NO_EMAIL")
                .userPhone("NO_PHONE")
                .userBirthDate("BD")
                .userGender("N")
                .userTerms(true)
                .providerType(providerType)
                .roleType(RoleType.USER)
                .createdAt(now)
                .userActive(true)
                .build();

//        User user = new User(
//                userInfo.getId(),
//                userInfo.getName(),
//                userInfo.getEmail(),
//
//                providerType,
//                RoleType.USER,
//                now,
//        );

        return userRepository.saveAndFlush(user);
    }

    // 회원수정
    private User updateUser(User user, OAuth2UserInfo userInfo) {
        if (userInfo.getName() != null && !user.getUserName().equals(userInfo.getName())) {
            user.setUserName(userInfo.getName());
        }

//        if (userInfo.getImageUrl() != null && !user.getProfileImageUrl().equals(userInfo.getImageUrl())) {
//            user.setProfileImageUrl(userInfo.getImageUrl());
//        }

        return user;
    }
}