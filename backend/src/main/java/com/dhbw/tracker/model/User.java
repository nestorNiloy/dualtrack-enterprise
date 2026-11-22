package com.dhbw.tracker.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="app_users", indexes=@Index(name="idx_app_users_email",columnList="email",unique=true))
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=254) private String email;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @PrePersist void prePersist(){createdAt=Instant.now();}
 public Long getId(){return id;} public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;} public Instant getCreatedAt(){return createdAt;}
}
