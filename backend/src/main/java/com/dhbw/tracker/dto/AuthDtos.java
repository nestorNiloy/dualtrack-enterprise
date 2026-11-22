package com.dhbw.tracker.dto;
import jakarta.validation.constraints.*;
public final class AuthDtos { private AuthDtos(){}
 public record RegisterRequest(@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(min=10,max=72) String password){}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 public record UserResponse(Long id,String email){}
}
