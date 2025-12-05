package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.Skill;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    @Query("SELECT s FROM Skill s WHERE s.deletedAt IS NULL")
    Page<Skill> findAllActive(Pageable pageable);

    @Query("SELECT s FROM Skill s WHERE s.deletedAt IS NULL")
    List<Skill> findAllNotDeleted();
    
    @Query("SELECT s FROM Skill s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Skill> findByIdAndNotDeleted(@Param("id") Long id);
    
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) = LOWER(:name) AND s.deletedAt IS NULL")
    Optional<Skill> findByNameIgnoreCaseAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Skill s WHERE LOWER(s.name) = LOWER(:name) AND s.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Skill s WHERE LOWER(s.name) = LOWER(:name) AND s.id <> :id AND s.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndIdNotAndNotDeleted(@Param("name") String name, @Param("id") Long id);

}
