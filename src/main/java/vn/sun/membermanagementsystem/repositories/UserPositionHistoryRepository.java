package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.UserPositionHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPositionHistoryRepository extends JpaRepository<UserPositionHistory, Long> {
    
    @Query("SELECT uph FROM UserPositionHistory uph WHERE uph.user.id = :userId AND uph.endedAt IS NULL")
    Optional<UserPositionHistory> findActiveByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uph FROM UserPositionHistory uph WHERE uph.user.id = :userId ORDER BY uph.startedAt DESC")
    List<UserPositionHistory> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);
}
