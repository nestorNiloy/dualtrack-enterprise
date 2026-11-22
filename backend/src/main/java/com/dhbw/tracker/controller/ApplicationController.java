package com.dhbw.tracker.controller;
import com.dhbw.tracker.dto.ApplicationDtos.*;import com.dhbw.tracker.model.ApplicationStatus;import com.dhbw.tracker.service.ApplicationService;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/applications") public class ApplicationController {
 private final ApplicationService service;public ApplicationController(ApplicationService s){service=s;}
 @GetMapping public PageResponse<ApplicationResponse> list(Authentication a,@RequestParam(required=false) ApplicationStatus status,@RequestParam(required=false) String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){if(page<0||size<1||size>100)throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 100");return service.list(a.getName(),status,q,page,size);}
 @PostMapping public ResponseEntity<ApplicationResponse> create(Authentication a,@Valid @RequestBody ApplicationRequest r){return ResponseEntity.status(201).body(service.create(a.getName(),r));}
 @PutMapping("/{id}") public ApplicationResponse update(Authentication a,@PathVariable Long id,@Valid @RequestBody ApplicationRequest r){return service.update(a.getName(),id,r);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(Authentication a,@PathVariable Long id){service.delete(a.getName(),id);return ResponseEntity.noContent().build();}
 @GetMapping("/stats") public StatsResponse stats(Authentication a){return service.stats(a.getName());}
 @GetMapping("/{id}/history") public List<HistoryResponse> history(Authentication a,@PathVariable Long id){return service.history(a.getName(),id);}
}
