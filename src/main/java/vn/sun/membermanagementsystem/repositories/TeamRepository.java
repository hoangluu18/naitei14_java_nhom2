package vn.sun.membermanagementsystem.repositories;

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

    @Query("SELECT t FROM Team t WHERE t.deletedAt IS NULL ORDER BY t.name")
    List<Team> findAllNotDeleted();
}
