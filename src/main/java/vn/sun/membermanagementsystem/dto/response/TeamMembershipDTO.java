package vn.sun.membermanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.enums.MembershipStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMembershipDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long teamId;
    private String teamName;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
