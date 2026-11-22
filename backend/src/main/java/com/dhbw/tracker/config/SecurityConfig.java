package com.dhbw.tracker.config;
import com.dhbw.tracker.repository.UserRepository; import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod; import org.springframework.security.authentication.*; import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.core.userdetails.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.*; import org.springframework.security.web.csrf.CookieCsrfTokenRepository; import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean UserDetailsService userDetailsService(UserRepository users){return email->users.findByEmail(email.toLowerCase()).map(u->User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles("USER").build()).orElseThrow(()->new UsernameNotFoundException("User not found"));}
 @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
  CookieCsrfTokenRepository csrf=CookieCsrfTokenRepository.withHttpOnlyFalse(); csrf.setCookiePath("/");
  CsrfTokenRequestAttributeHandler handler=new CsrfTokenRequestAttributeHandler();
  http.csrf(c->c.csrfTokenRepository(csrf).csrfTokenRequestHandler(handler))
   .authorizeHttpRequests(a->a.requestMatchers("/","/index.html","/css/**","/js/**","/favicon.ico","/actuator/health","/api/auth/csrf", "/api/auth/register","/api/auth/login","/error").permitAll().requestMatchers(HttpMethod.OPTIONS,"/**").permitAll().anyRequest().authenticated())
   .formLogin(f->f.disable()).httpBasic(b->b.disable())
   .logout(l->l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req,res,auth)->res.setStatus(204)).invalidateHttpSession(true).deleteCookies("JSESSIONID","XSRF-TOKEN"))
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401)))
   .sessionManagement(s->s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED));
  return http.build();
 }
}