package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.UserSkill;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    
    @Query("SELECT us FROM UserSkill us WHERE us.user.id = :userId")
    List<UserSkill> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT us FROM UserSkill us WHERE us.user.id = :userId AND us.skill.id = :skillId")
    Optional<UserSkill> findByUserIdAndSkillId(@Param("userId") Long userId, @Param("skillId") Long skillId);
    
    @Modifying
    @Query("DELETE FROM UserSkill us WHERE us.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
