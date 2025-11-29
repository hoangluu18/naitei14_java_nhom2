package vn.sun.membermanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.sun.membermanagementsystem.dto.response.*;
import vn.sun.membermanagementsystem.entities.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "activeTeam", expression = "java(getActiveTeamName(user))")
    @Mapping(target = "activeProjects", expression = "java(mapActiveProjects(user))")
    @Mapping(target = "currentPosition", expression = "java(mapCurrentPosition(user))")
    @Mapping(target = "skills", expression = "java(mapUserSkills(user))")
    UserSummaryDTO toSummaryDTO(User user);

    List<UserSummaryDTO> toSummaryDTOList(List<User> users);
    
    default String getActiveTeamName(User user) {
        if (user.getTeamMemberships() == null || user.getTeamMemberships().isEmpty()) {
            return null;
        }
        return user.getTeamMemberships().stream()
                .filter(tm -> tm.getStatus() != null && "ACTIVE".equals(tm.getStatus().name()) 
                        && tm.getLeftAt() == null 
                        && tm.getTeam() != null 
                        && tm.getTeam().getDeletedAt() == null)
                .map(tm -> tm.getTeam().getName())
                .findFirst()
                .orElse(null);
    }
    
    default List<UserSummaryDTO.SimpleProjectDTO> mapActiveProjects(User user) {
        if (user.getProjectMemberships() == null || user.getProjectMemberships().isEmpty()) {
            return List.of();
        }
        return user.getProjectMemberships().stream()
                .filter(pm -> pm.getStatus() != null && "ACTIVE".equals(pm.getStatus().name())
                        && pm.getLeftAt() == null
                        && pm.getProject() != null
                        && pm.getProject().getDeletedAt() == null)
                .map(pm -> UserSummaryDTO.SimpleProjectDTO.builder()
                        .id(pm.getProject().getId())
                        .name(pm.getProject().getName())
                        .abbreviation(pm.getProject().getAbbreviation())
                        .status(pm.getProject().getStatus() != null ? pm.getProject().getStatus().name() : null)
                        .build())
                .toList();
    }
    
    default PositionDTO mapCurrentPosition(User user) {
        if (user.getPositionHistories() == null || user.getPositionHistories().isEmpty()) {
            return null;
        }
        return user.getPositionHistories().stream()
                .filter(ph -> ph.getEndedAt() == null && ph.getPosition() != null)
                .findFirst()
                .map(ph -> PositionDTO.builder()
                        .id(ph.getPosition().getId())
                        .name(ph.getPosition().getName())
                        .abbreviation(ph.getPosition().getAbbreviation())
                        .build())
                .orElse(null);
    }
    
    default List<UserSummaryDTO.UserSkillDTO> mapUserSkills(User user) {
        if (user.getUserSkills() == null || user.getUserSkills().isEmpty()) {
            return List.of();
        }
        return user.getUserSkills().stream()
                .filter(us -> us.getSkill() != null && us.getSkill().getDeletedAt() == null)
                .map(us -> UserSummaryDTO.UserSkillDTO.builder()
                        .skillId(us.getSkill().getId())
                        .skill(SkillDTO.builder()
                                .id(us.getSkill().getId())
                                .name(us.getSkill().getName())
                                .description(us.getSkill().getDescription())
                                .build())
                        .level(us.getLevel() != null ? us.getLevel().name() : null)
                        .usedYearNumber(us.getUsedYearNumber())
                        .build())
                .toList();
    }
}
