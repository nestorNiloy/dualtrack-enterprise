package com.dhbw.tracker.service;

import com.dhbw.tracker.dto.ApplicationDtos.ApplicationRequest;
import com.dhbw.tracker.exception.ResourceNotFoundException;
import com.dhbw.tracker.model.*;
import com.dhbw.tracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock private ApplicationRepository apps;
    @Mock private UserRepository users;
    @Mock private StatusHistoryRepository history;
    @InjectMocks private ApplicationService service;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = mock(User.class);
        when(owner.getId()).thenReturn(42L);
        when(users.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
    }

    @Test
    void listReturnsPagedApplicationsForAuthenticatedOwner() {
        Application application = application(ApplicationStatus.APPLIED);
        when(apps.searchForUser(eq(42L), eq(ApplicationStatus.APPLIED), eq("sap"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(application)));
        var result = service.list("owner@example.com", ApplicationStatus.APPLIED, " sap ", 0, 10);
        assertEquals(1, result.totalElements());
        assertEquals("SAP", result.content().get(0).companyName());
        verify(apps).searchForUser(eq(42L), eq(ApplicationStatus.APPLIED), eq("sap"), any(Pageable.class));
    }

    @Test
    void listNormalizesMissingSearchTermToEmptyString() {
        when(apps.searchForUser(eq(42L), isNull(), eq(""), any(Pageable.class))).thenReturn(Page.empty());
        assertEquals(0, service.list("owner@example.com", null, "  ", 0, 20).totalElements());
    }

    @Test
    void createAssignsOwnerAndRecordsInitialStatus() {
        when(apps.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create("owner@example.com", request("SAP", "Software Engineer", ApplicationStatus.APPLIED));
        assertEquals("SAP", result.companyName());
        verify(apps).save(argThat(a -> a.getUser() == owner && a.getCompanyName().equals("SAP")
                && a.getPositionTitle().equals("Software Engineer")));
        verify(history).save(argThat(h -> h.getNewStatus() == ApplicationStatus.APPLIED));
    }

    @Test
    void updateRecordsHistoryWhenStatusChanges() {
        Application application = application(ApplicationStatus.APPLIED);
        when(apps.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(application));
        var result = service.update("owner@example.com", 7L,
                request("SAP", "Backend Developer", ApplicationStatus.INTERVIEWING));
        assertEquals(ApplicationStatus.INTERVIEWING, result.status());
        assertEquals("Backend Developer", result.positionTitle());
        verify(history).save(argThat(h -> h.getOldStatus() == ApplicationStatus.APPLIED
                && h.getNewStatus() == ApplicationStatus.INTERVIEWING));
    }

    @Test
    void updateDoesNotCreateHistoryWhenStatusIsUnchanged() {
        when(apps.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(application(ApplicationStatus.APPLIED)));
        service.update("owner@example.com", 7L,
                request("SAP", "Backend Developer", ApplicationStatus.APPLIED));
        verify(history, never()).save(any());
    }

    @Test
    void updateRejectsApplicationOwnedByAnotherUser() {
        when(apps.findByIdAndUserId(7L, 42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update("owner@example.com", 7L,
                request("Other", "Developer", ApplicationStatus.APPLIED)));
        verify(history, never()).save(any());
    }

    @Test
    void deleteOnlyDeletesApplicationBelongingToOwner() {
        Application application = application(ApplicationStatus.APPLIED);
        when(apps.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(application));
        service.delete("owner@example.com", 7L);
        verify(apps).delete(application);
    }

    @Test
    void statsReturnsCountsForAuthenticatedOwner() {
        when(apps.countByUserId(42L)).thenReturn(9L);
        when(apps.countByUserIdAndStatus(42L, ApplicationStatus.APPLIED)).thenReturn(4L);
        when(apps.countByUserIdAndStatus(42L, ApplicationStatus.INTERVIEWING)).thenReturn(2L);
        when(apps.countByUserIdAndStatus(42L, ApplicationStatus.REJECTED)).thenReturn(2L);
        when(apps.countByUserIdAndStatus(42L, ApplicationStatus.OFFERED)).thenReturn(1L);
        var result = service.stats("owner@example.com");
        assertEquals(9L, result.total());
        assertEquals(4L, result.applied());
        assertEquals(2L, result.interviewing());
        assertEquals(2L, result.rejected());
        assertEquals(1L, result.offered());
    }

    @Test
    void historyChecksOwnershipBeforeReturningEntries() {
        StatusHistory entry = mock(StatusHistory.class);
        when(apps.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(application(ApplicationStatus.APPLIED)));
        when(history.findAllByApplicationIdOrderByChangedAtDesc(7L)).thenReturn(List.of(entry));
        when(entry.getId()).thenReturn(3L);
        when(entry.getNewStatus()).thenReturn(ApplicationStatus.APPLIED);
        when(entry.getChangedAt()).thenReturn(Instant.parse("2026-11-22T12:00:00Z"));
        var result = service.history("owner@example.com", 7L);
        assertEquals(1, result.size());
        assertEquals(ApplicationStatus.APPLIED, result.get(0).newStatus());
    }

    private Application application(ApplicationStatus status) {
        Application a = new Application();
        a.setCompanyName("SAP");
        a.setPositionTitle("Software Engineer");
        a.setStatus(status);
        a.setApplicationDate(LocalDate.of(2026, 11, 22));
        a.setUser(owner);
        return a;
    }

    private ApplicationRequest request(String company, String position, ApplicationStatus status) {
        return new ApplicationRequest(company, position, status, LocalDate.of(2026, 11, 22), "Notes");
    }
}
