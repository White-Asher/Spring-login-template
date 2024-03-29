package com.springboot.template.auth.service;

import com.springboot.template.auth.entity.UserPrincipal;
import com.springboot.template.user.entity.User;
import com.springboot.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 위 코드는 Spring Security를 사용하여 사용자 인증을 구현하는 과정에서 사용자 정보를 조회하는 CustomUserDetailsService 클래스의 구현 코드입니다. <br>
 * CustomUserDetailsService 클래스는 UserDetailsService 인터페이스를 구현하고 있습니다. <br>
 * UserDetailsService 인터페이스는 Spring Security에서 제공하는 인터페이스 중 하나로, 사용자 정보를 조회하는 메소드인 loadUserByUsername()을 정의하고 있습니다. 이 메소드는 사용자 이름을 파라미터로 받아서 해당 사용자 정보를 조회하고, UserDetails 객체를 반환합니다. <br>
 * CustomUserDetailsService 클래스의 loadUserByUsername() 메소드는 UserRepository를 사용하여 사용자 정보를 조회합니다. <br>
 * UserRepository는 사용자 정보를 데이터베이스에서 조회하는 역할을 합니다.
 * 만약 해당 사용자 정보가 존재하지 않을 경우 UsernameNotFoundException 예외가 발생합니다. <br>
 * 마지막으로, UserPrincipal.create() 메소드를 사용하여 조회한 사용자 정보를 UserPrincipal 객체로 변환하여 반환합니다.
 * UserPrincipal 객체는 Spring Security에서 제공하는 UserDetails 인터페이스를 구현하는 객체로, 인증된 사용자의 정보를 담고 있습니다. 반환된 UserPrincipal 객체는 인증 과정에서 인증된 사용자 정보를 저장하는 Principal 객체로 사용됩니다.
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("Can not find username.");
        }
        return UserPrincipal.create(user);
    }
}