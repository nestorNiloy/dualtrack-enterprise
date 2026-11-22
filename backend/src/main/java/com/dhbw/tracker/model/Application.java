package com.dhbw.tracker.model;
import jakarta.persistence.*;
import java.time.LocalDate; import java.time.Instant;
@Entity @Table(name="applications", indexes={@Index(name="idx_app_owner_date",columnList="user_id,application_date"),@Index(name="idx_app_owner_status",columnList="user_id,status")})
public class Application {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="company_name",nullable=false,length=120) private String companyName;
 @Column(name="position_title",nullable=false,length=160) private String positionTitle;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private ApplicationStatus status;
 @Column(name="application_date",nullable=false) private LocalDate applicationDate;
 @Column(columnDefinition="text") private String notes;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Version private Long version;
 @PrePersist void onCreate(){Instant now=Instant.now();createdAt=now;updatedAt=now;if(applicationDate==null)applicationDate=LocalDate.now();}
 @PreUpdate void onUpdate(){updatedAt=Instant.now();}
 public Long getId(){return id;} public String getCompanyName(){return companyName;} public void setCompanyName(String v){companyName=v;}
 public String getPositionTitle(){return positionTitle;} public void setPositionTitle(String v){positionTitle=v;}
 public ApplicationStatus getStatus(){return status;} public void setStatus(ApplicationStatus v){status=v;}
 public LocalDate getApplicationDate(){return applicationDate;} public void setApplicationDate(LocalDate v){applicationDate=v;}
 public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
 public User getUser(){return user;} public void setUser(User v){user=v;}
 public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public Long getVersion(){return version;}
}
