package vn.sun.membermanagementsystem.services.impls;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.response.TeamMembershipDTO;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.entities.TeamLeadershipHistory;
import vn.sun.membermanagementsystem.entities.TeamMember;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.enums.MembershipStatus;
import vn.sun.membermanagementsystem.exception.BadRequestException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.repositories.TeamLeadershipHistoryRepository;
import vn.sun.membermanagementsystem.repositories.TeamMemberRepository;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.services.TeamMemberService;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamLeadershipHistoryRepository teamLeadershipHistoryRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamMembershipDTO addMember(Long userId, Long teamId) {
        log.info("Adding user {} to team {}", userId, teamId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        TeamMember existingMembership = teamMemberRepository.findActiveTeamByUserId(userId);
        if (existingMembership != null) {
            log.error("User {} is already in team {}", userId, existingMembership.getTeam().getId());
            throw new BadRequestException(
                    String.format("User is already in team '%s'. Please transfer instead.",
                            existingMembership.getTeam().getName()));
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setUser(user);
        teamMember.setTeam(team);
        teamMember.setStatus(MembershipStatus.ACTIVE);
        teamMember.setJoinedAt(LocalDateTime.now());

        TeamMember savedMember = teamMemberRepository.save(teamMember);
        log.info("User {} added to team {} successfully", userId, teamId);

        return mapToDTO(savedMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamMembershipDTO transferMember(Long userId, Long newTeamId) {
        log.info("Transferring user {} to team {}", userId, newTeamId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Team newTeam = teamRepository.findByIdAndNotDeleted(newTeamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", newTeamId);
                    return new ResourceNotFoundException("Team not found with ID: " + newTeamId);
                });

        TeamMember currentMembership = teamMemberRepository.findActiveTeamByUserId(userId);
        if (currentMembership == null) {
            log.error("User {} is not in any active team", userId);
            throw new BadRequestException("User is not currently in any team. Use addMember instead.");
        }

        if (currentMembership.getTeam().getId().equals(newTeamId)) {
            log.error("User {} is already in team {}", userId, newTeamId);
            throw new BadRequestException("User is already in this team.");
        }

        // Check if user is current leader and end leadership before transfer
        Optional<TeamLeadershipHistory> currentLeadership = teamLeadershipHistoryRepository
                .findActiveByLeaderIdAndTeamId(userId, currentMembership.getTeam().getId());

        if (currentLeadership.isPresent()) {
            TeamLeadershipHistory leadership = currentLeadership.get();
            leadership.setEndedAt(LocalDateTime.now());
            teamLeadershipHistoryRepository.save(leadership);
            log.info("Ended leadership for user {} in team {} before transfer",
                    userId, currentMembership.getTeam().getId());
        }

        currentMembership.setStatus(MembershipStatus.INACTIVE);
        currentMembership.setLeftAt(LocalDateTime.now());
        teamMemberRepository.save(currentMembership);
        entityManager.flush(); // Force database update before creating new membership
        log.info("Set old membership {} to inactive", currentMembership.getId());

        TeamMember newMembership = new TeamMember();
        newMembership.setUser(user);
        newMembership.setTeam(newTeam);
        newMembership.setStatus(MembershipStatus.ACTIVE);
        newMembership.setJoinedAt(LocalDateTime.now());

        TeamMember savedMember = teamMemberRepository.save(newMembership);
        log.info("User {} transferred from team {} to team {} successfully",
                userId, currentMembership.getTeam().getId(), newTeamId);

        return mapToDTO(savedMember);
    }

    private TeamMembershipDTO mapToDTO(TeamMember teamMember) {
        return TeamMembershipDTO.builder()
                .id(teamMember.getId())
                .userId(teamMember.getUser().getId())
                .userName(teamMember.getUser().getName())
                .teamId(teamMember.getTeam().getId())
                .teamName(teamMember.getTeam().getName())
                .status(teamMember.getStatus())
                .joinedAt(teamMember.getJoinedAt())
                .leftAt(teamMember.getLeftAt())
                .build();
    }
}
