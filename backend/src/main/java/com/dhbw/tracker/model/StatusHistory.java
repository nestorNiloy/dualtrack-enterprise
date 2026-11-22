package com.dhbw.tracker.model;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="application_status_history",indexes=@Index(name="idx_history_application",columnList="application_id,changed_at"))
public class StatusHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",nullable=false) private Application application;
 @Enumerated(EnumType.STRING) @Column(name="old_status",length=24) private ApplicationStatus oldStatus;
 @Enumerated(EnumType.STRING) @Column(name="new_status",nullable=false,length=24) private ApplicationStatus newStatus;
 @Column(name="changed_at",nullable=false) private Instant changedAt;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="changed_by",nullable=false) private User changedBy;
 protected StatusHistory(){}
 public StatusHistory(Application a,ApplicationStatus oldS,ApplicationStatus newS,User u){application=a;oldStatus=oldS;newStatus=newS;changedBy=u;changedAt=Instant.now();}
 public Long getId(){return id;} public ApplicationStatus getOldStatus(){return oldStatus;} public ApplicationStatus getNewStatus(){return newStatus;} public Instant getChangedAt(){return changedAt;}
}
