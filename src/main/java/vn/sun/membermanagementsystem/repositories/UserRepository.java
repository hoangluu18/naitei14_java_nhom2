package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(Long id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.teamMemberships tm " +
            "LEFT JOIN FETCH tm.team " +
            "WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithTeams(@Param("id") Long id);
    
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.projectMemberships pm " +
            "LEFT JOIN FETCH pm.project " +
            "WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithProjects(@Param("id") Long id);
    
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.positionHistories ph " +
            "LEFT JOIN FETCH ph.position " +
            "WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithPositionHistories(@Param("id") Long id);
    
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userSkills us " +
            "LEFT JOIN FETCH us.skill " +
            "WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithSkills(@Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllNotDeleted();

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    List<User> findByStatusAndNotDeleted(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findByRoleAndNotDeleted(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.teamMemberships tm " +
            "LEFT JOIN FETCH tm.team t " +
            "WHERE u.deletedAt IS NULL " +
            "AND (tm IS NULL OR (tm.status = 'ACTIVE' AND tm.leftAt IS NULL AND t.deletedAt IS NULL))")
    Page<User> findAllNotDeleted(Pageable pageable);
    
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.teamMemberships tm " +
            "LEFT JOIN FETCH tm.team t " +
            "WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "u.deletedAt IS NULL AND " +
            "(tm IS NULL OR (tm.status = 'ACTIVE' AND tm.leftAt IS NULL AND t.deletedAt IS NULL))")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") UserStatus status,
                           @Param("role") UserRole role,
                           Pageable pageable);
    
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.teamMemberships tm " +
            "LEFT JOIN FETCH tm.team t " +
            "WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:teamId IS NULL OR (tm.team.id = :teamId AND tm.status = 'ACTIVE' AND tm.leftAt IS NULL)) AND " +
            "u.deletedAt IS NULL AND " +
            "(tm IS NULL OR (tm.status = 'ACTIVE' AND tm.leftAt IS NULL AND t.deletedAt IS NULL))")
    Page<User> searchUsersWithTeam(@Param("keyword") String keyword,
                                   @Param("status") UserStatus status,
                                   @Param("role") UserRole role,
                                   @Param("teamId") Long teamId,
                                   Pageable pageable);
}
