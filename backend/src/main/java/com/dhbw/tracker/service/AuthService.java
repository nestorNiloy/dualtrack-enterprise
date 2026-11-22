package com.dhbw.tracker.service;
import com.dhbw.tracker.dto.AuthDtos.*; import com.dhbw.tracker.model.User; import com.dhbw.tracker.repository.UserRepository; import org.springframework.security.authentication.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class AuthService {
 private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager auth;
 public AuthService(UserRepository u,PasswordEncoder e,AuthenticationManager a){users=u;encoder=e;auth=a;}
 @Transactional public User register(RegisterRequest r){String email=r.email().trim().toLowerCase();if(users.existsByEmail(email))throw new IllegalArgumentException("An account with this email already exists.");User u=new User();u.setEmail(email);u.setPasswordHash(encoder.encode(r.password()));return users.save(u);}
 public void authenticate(LoginRequest r){auth.authenticate(new UsernamePasswordAuthenticationToken(r.email().trim().toLowerCase(),r.password()));}
}
