package com.dhbw.tracker.dto;
import com.dhbw.tracker.model.ApplicationStatus; import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
public final class ApplicationDtos {private ApplicationDtos(){}
 public record ApplicationRequest(@NotBlank @Size(max=120) String companyName,@NotBlank @Size(max=160) String positionTitle,@NotNull ApplicationStatus status,@NotNull LocalDate applicationDate,@Size(max=5000) String notes){}
 public record ApplicationResponse(Long id,String companyName,String positionTitle,ApplicationStatus status,LocalDate applicationDate,String notes,Instant createdAt,Instant updatedAt,Long version){}
 public record PageResponse<T>(List<T> content,int page,int size,long totalElements,int totalPages,boolean first,boolean last){}
 public record StatsResponse(long total,long applied,long interviewing,long rejected,long offered){}
 public record HistoryResponse(Long id,ApplicationStatus oldStatus,ApplicationStatus newStatus,Instant changedAt){}
}
