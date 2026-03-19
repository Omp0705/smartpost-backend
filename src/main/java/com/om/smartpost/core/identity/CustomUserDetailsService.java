package com.om.smartpost.core.identity;

import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.repository.UserRepository;
import com.om.smartpost.core.identity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(identifier);

        if(user.isEmpty()) {
            user = userRepository.findByUsername(identifier);
        }

        User foundUser = user.orElseThrow(() ->
                new UsernameNotFoundException("User does not exist"));

        // Convert the user entity to spring security user details
        return new CustomUserDetails(
                foundUser.getUserId(),
                foundUser.getUsername(),
                foundUser.getPasswordHash(),
                foundUser.getIsActive(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + foundUser.getRole().name()))
        );

    }
}

