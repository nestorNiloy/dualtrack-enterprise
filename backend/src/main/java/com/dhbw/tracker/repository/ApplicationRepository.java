package com.dhbw.tracker.repository;
import com.dhbw.tracker.model.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicationRepository extends JpaRepository<Application,Long>{
 Page<Application> findAllByUserId(Long userId,Pageable pageable);
 Page<Application> findAllByUserIdAndStatus(Long userId,ApplicationStatus status,Pageable pageable);
 Page<Application> findAllByUserIdAndCompanyNameContainingIgnoreCaseOrUserIdAndPositionTitleContainingIgnoreCase(Long userId,String company,Long userId2,String position,Pageable pageable);
 java.util.Optional<Application> findByIdAndUserId(Long id,Long userId);
 long countByUserId(Long userId); long countByUserIdAndStatus(Long userId,ApplicationStatus status);
}
