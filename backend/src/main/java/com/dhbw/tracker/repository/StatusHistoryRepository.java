package com.dhbw.tracker.repository;
import com.dhbw.tracker.model.StatusHistory; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface StatusHistoryRepository extends JpaRepository<StatusHistory,Long>{List<StatusHistory> findAllByApplicationIdOrderByChangedAtDesc(Long applicationId);}
