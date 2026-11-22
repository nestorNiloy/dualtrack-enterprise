package com.dhbw.tracker.repository;

import com.dhbw.tracker.model.Application;
import com.dhbw.tracker.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
            select a from Application a
            where a.user.id = :userId
              and (:status is null or a.status = :status)
              and (
                  :term = ''
                  or lower(a.companyName) like lower(concat('%', :term, '%'))
                  or lower(a.positionTitle) like lower(concat('%', :term, '%'))
              )
            """)
    Page<Application> searchForUser(
            @Param("userId") Long userId,
            @Param("status") ApplicationStatus status,
            @Param("term") String term,
            Pageable pageable
    );

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ApplicationStatus status);
}
