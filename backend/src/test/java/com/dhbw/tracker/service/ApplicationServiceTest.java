package com.dhbw.tracker.service;
import com.dhbw.tracker.dto.ApplicationDtos.ApplicationRequest;import com.dhbw.tracker.model.*;import com.dhbw.tracker.repository.*;import org.junit.jupiter.api.*;import org.mockito.*;import java.time.LocalDate;import java.util.Optional;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class ApplicationServiceTest {
 @Mock ApplicationRepository apps;@Mock UserRepository users;@Mock StatusHistoryRepository history;@InjectMocks ApplicationService service;
 @BeforeEach void setup(){MockitoAnnotations.openMocks(this);}
 @Test void createAssignsAuthenticatedOwnerAndRecordsInitialStatus(){User u=new User();u.setEmail("a@example.com");when(users.findByEmail("a@example.com")).thenReturn(Optional.of(u));when(apps.save(any())).thenAnswer(inv->{Application a=inv.getArgument(0);return a;});ApplicationRequest r=new ApplicationRequest("SAP","Software Engineer",ApplicationStatus.APPLIED,LocalDate.of(2026,11,22),"note");assertDoesNotThrow(()->service.create("a@example.com",r));verify(apps).save(argThat(a->a.getUser()==u&&a.getCompanyName().equals("SAP")));verify(history).save(any());}
 @Test void updateCannotAccessAnotherUsersApplication(){User u=new User();u.setEmail("a@example.com");when(users.findByEmail("a@example.com")).thenReturn(Optional.of(u));when(apps.findByIdAndUserId(7L,null)).thenReturn(Optional.empty());assertThrows(RuntimeException.class,()->service.update("a@example.com",7L,new ApplicationRequest("X","Y",ApplicationStatus.APPLIED,LocalDate.now(),null)));}
}
