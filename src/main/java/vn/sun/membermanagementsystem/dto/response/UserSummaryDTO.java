package vn.sun.membermanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDate birthday;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String activeTeam;
    private List<SimpleProjectDTO> activeProjects;
    private PositionDTO currentPosition;
    private List<UserSkillDTO> skills;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleProjectDTO {
        private Long id;
        private String name;
        private String abbreviation;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSkillDTO {
        private Long skillId;
        private SkillDTO skill;
        private String level;
        private BigDecimal usedYearNumber;
    }
}

