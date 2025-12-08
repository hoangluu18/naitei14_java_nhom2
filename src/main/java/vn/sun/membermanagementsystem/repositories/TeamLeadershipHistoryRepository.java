package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.TeamLeadershipHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamLeadershipHistoryRepository extends JpaRepository<TeamLeadershipHistory, Long> {

        @Query("SELECT tlh FROM TeamLeadershipHistory tlh " +
                        "JOIN FETCH tlh.leader " +
                        "WHERE tlh.team.id = :teamId " +
                        "AND tlh.endedAt IS NULL")
        Optional<TeamLeadershipHistory> findActiveLeaderByTeamId(@Param("teamId") Long teamId);

        @Query("SELECT tlh FROM TeamLeadershipHistory tlh " +
                        "JOIN FETCH tlh.leader " +
                        "WHERE tlh.team.id = :teamId " +
                        "ORDER BY tlh.startedAt DESC")
        List<TeamLeadershipHistory> findByTeamIdOrderByStartedAtDesc(@Param("teamId") Long teamId);

        @Query("SELECT tlh FROM TeamLeadershipHistory tlh " +
                        "WHERE tlh.leader.id = :leaderId " +
                        "AND tlh.team.id = :teamId " +
                        "AND tlh.endedAt IS NULL")
        Optional<TeamLeadershipHistory> findActiveByLeaderIdAndTeamId(@Param("leaderId") Long leaderId,
                        @Param("teamId") Long teamId);
}
