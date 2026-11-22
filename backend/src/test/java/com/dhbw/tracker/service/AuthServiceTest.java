package com.dhbw.tracker.service;

import com.dhbw.tracker.dto.AuthDtos.LoginRequest;
import com.dhbw.tracker.dto.AuthDtos.RegisterRequest;
import com.dhbw.tracker.model.User;
import com.dhbw.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService service;

    @Test
    void registerNormalizesEmailAndStoresEncodedPassword() {
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(encoder.encode("a-strong-password")).thenReturn("encoded-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.register(new RegisterRequest(" Person@Example.com ", "a-strong-password"));

        assertEquals("person@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPasswordHash());
        verify(users).save(argThat(user -> user.getEmail().equals("person@example.com")));
        verify(encoder).encode("a-strong-password");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.register(
                new RegisterRequest("Person@Example.com", "a-strong-password")));

        verify(users, never()).save(any());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void authenticateNormalizesEmailAndDelegatesToSpringSecurity() {
        service.authenticate(new LoginRequest(" Person@Example.com ", "secret-password"));

        verify(authenticationManager).authenticate(argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken
                        && token.getName().equals("person@example.com")
                        && token.getCredentials().equals("secret-password")));
    }
}
