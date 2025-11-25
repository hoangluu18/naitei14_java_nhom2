package vn.sun.membermanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailDTO {

    private Long id;
    private String name;
    private String description;
    private TeamLeaderDTO currentLeader;
    private List<TeamMemberDTO> members;
    private Integer memberCount;
    private List<ProjectSummaryDTO> projects;
    private List<TeamLeadershipHistoryDTO> leadershipHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberDTO {
        private Long userId;
        private String name;
        private String email;
        private String position;
        private LocalDateTime joinedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSummaryDTO {
        private Long projectId;
        private String name;
        private String abbreviation;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamLeadershipHistoryDTO {
        private Long leaderId;
        private String leaderName;
        private String leaderEmail;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Boolean isCurrent;
    }
}
