package com.org.ecommerce.common.security;

import com.org.ecommerce.user.entity.User;
import com.org.ecommerce.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // we return the username - here is where we tell spring that the username is the email id.
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_"  + user.getRole().name()))
                // spring security convention while looking for role is - it searches whether something like this exists - ROLE_ADMIN, ROLE_USER
                // that too when we use this - @PreAuthorize("hasRole('ADMIN')")
        );
    }
}
