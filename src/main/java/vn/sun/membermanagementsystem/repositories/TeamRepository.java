package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.sun.membermanagementsystem.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.Team;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE t.name = :name AND t.deletedAt IS NULL")
    boolean existsByNameAndNotDeleted(@Param("name") String name);

    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE t.name = :name AND t.id <> :id AND t.deletedAt IS NULL")
    boolean existsByNameAndNotDeletedAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT t FROM Team t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Team> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT t FROM Team t WHERE t.name = :name AND t.deletedAt IS NULL")
    Optional<Team> findByNameAndNotDeleted(@Param("name") String name);

    @Query("SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.leadershipHistory lh " +
            "LEFT JOIN FETCH lh.leader " +
            "WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Team> findByIdWithLeadershipHistory(@Param("id") Long id);

    @Query("SELECT t FROM Team t WHERE t.deletedAt IS NULL ORDER BY t.name")
    List<Team> findAllNotDeleted();

    @Query("SELECT t FROM Team t WHERE t.deletedAt IS NULL")
    Page<Team> findAllNotDeleted(Pageable pageable);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId " +
            "AND tm.status = 'ACTIVE' AND tm.leftAt IS NULL")
    long countActiveMembers(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.team.id = :teamId " +
            "AND p.status NOT IN ('COMPLETED', 'CANCELLED') AND p.deletedAt IS NULL")
    boolean hasActiveProjects(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.team.id = :teamId AND p.deletedAt IS NULL")
    long countAllProjects(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.team.id = :teamId " +
            "AND p.status NOT IN ('COMPLETED', 'CANCELLED') AND p.deletedAt IS NULL")
    long countActiveProjects(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.team.id = :teamId " +
            "AND p.status IN ('COMPLETED', 'CANCELLED') AND p.deletedAt IS NULL")
    long countCompletedProjects(@Param("teamId") Long teamId);
}
