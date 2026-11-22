package com.dhbw.tracker.service;

import com.dhbw.tracker.dto.ApplicationDtos.*;
import com.dhbw.tracker.exception.ResourceNotFoundException;
import com.dhbw.tracker.model.*;
import com.dhbw.tracker.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository apps;
    private final UserRepository users;
    private final StatusHistoryRepository history;

    public ApplicationService(ApplicationRepository apps, UserRepository users, StatusHistoryRepository history) {
        this.apps = apps;
        this.users = users;
        this.history = history;
    }

    private User owner(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
    }

    private ApplicationResponse dto(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getCompanyName(),
                application.getPositionTitle(),
                application.getStatus(),
                application.getApplicationDate(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                application.getVersion()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> list(
            String email, ApplicationStatus status, String q, int page, int size
    ) {
        User user = owner(email);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "applicationDate").and(Sort.by("id"))
        );

        // Always bind a non-null String. PostgreSQL can otherwise infer a null
        // optional search parameter as bytea when the query checks ":term is null".
        String term = (q == null || q.isBlank()) ? "" : q.trim();

        Page<Application> result = apps.searchForUser(user.getId(), status, term, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::dto).toList(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public ApplicationResponse create(String email, ApplicationRequest request) {
        User user = owner(email);
        Application application = new Application();
        application.setUser(user);
        apply(application, request);
        Application saved = apps.save(application);
        history.save(new StatusHistory(saved, null, saved.getStatus(), user));
        return dto(saved);
    }

    @Transactional
    public ApplicationResponse update(String email, Long id, ApplicationRequest request) {
        User user = owner(email);
        Application application = apps.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        ApplicationStatus oldStatus = application.getStatus();
        apply(application, request);

        if (oldStatus != application.getStatus()) {
            history.save(new StatusHistory(application, oldStatus, application.getStatus(), user));
        }

        return dto(application);
    }

    private void apply(Application application, ApplicationRequest request) {
        application.setCompanyName(request.companyName().trim());
        application.setPositionTitle(request.positionTitle().trim());
        application.setStatus(request.status());
        application.setApplicationDate(request.applicationDate());
        application.setNotes(request.notes() == null ? null : request.notes().trim());
    }

    @Transactional
    public void delete(String email, Long id) {
        User user = owner(email);
        Application application = apps.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        apps.delete(application);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(String email) {
        User user = owner(email);
        return new StatsResponse(
                apps.countByUserId(user.getId()),
                apps.countByUserIdAndStatus(user.getId(), ApplicationStatus.APPLIED),
                apps.countByUserIdAndStatus(user.getId(), ApplicationStatus.INTERVIEWING),
                apps.countByUserIdAndStatus(user.getId(), ApplicationStatus.REJECTED),
                apps.countByUserIdAndStatus(user.getId(), ApplicationStatus.OFFERED)
        );
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(String email, Long id) {
        User user = owner(email);
        apps.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        return history.findAllByApplicationIdOrderByChangedAtDesc(id).stream()
                .map(entry -> new HistoryResponse(
                        entry.getId(),
                        entry.getOldStatus(),
                        entry.getNewStatus(),
                        entry.getChangedAt()
                ))
                .toList();
    }
}
