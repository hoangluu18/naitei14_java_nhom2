package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.sun.membermanagementsystem.entities.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT a FROM ActivityLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:fromDate IS NULL OR a.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR a.createdAt <= :toDate) " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> searchLogs(@Param("entityType") String entityType,
                                  @Param("fromDate") LocalDateTime fromDate,
                                  @Param("toDate") LocalDateTime toDate,
                                  Pageable pageable);
    
    @Query("SELECT DISTINCT a.entityType FROM ActivityLog a WHERE a.entityType IS NOT NULL ORDER BY a.entityType")
    List<String> findDistinctEntityTypes();
}
