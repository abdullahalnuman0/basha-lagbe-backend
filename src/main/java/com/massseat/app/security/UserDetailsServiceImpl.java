package com.massseat.app.security;

import com.massseat.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return UserPrincipal.of(
                repository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username))
        );
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long id) throws UsernameNotFoundException {
        return UserPrincipal.of(
                repository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id))
        );
    }
}
