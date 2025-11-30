package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.dto.response.TeamLeaderDTO;
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
import vn.sun.membermanagementsystem.services.TeamLeadershipService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamLeadershipServiceImpl implements TeamLeadershipService {

    private final TeamLeadershipHistoryRepository leadershipRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamLeaderDTO assignLeader(Long teamId, Long leaderId) {
        log.info("Assigning leader {} to team {}", leaderId, teamId);

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        User leader = userRepository.findByIdAndNotDeleted(leaderId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", leaderId);
                    return new ResourceNotFoundException("User not found with ID: " + leaderId);
                });

        Optional<TeamLeadershipHistory> existingLeader = leadershipRepository.findActiveLeaderByTeamId(teamId);
        if (existingLeader.isPresent()) {
            log.error("Team {} already has an active leader", teamId);
            throw new BadRequestException("Team already has an active leader. Use changeLeader instead.");
        }

        ensureUserIsTeamMember(team, leader);

        TeamLeadershipHistory leadershipHistory = new TeamLeadershipHistory();
        leadershipHistory.setTeam(team);
        leadershipHistory.setLeader(leader);
        leadershipHistory.setStartedAt(LocalDateTime.now());

        TeamLeadershipHistory savedHistory = leadershipRepository.save(leadershipHistory);
        log.info("Leader assigned successfully: {} to team {}", leaderId, teamId);

        return mapToLeaderDTO(savedHistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamLeaderDTO changeLeader(Long teamId, Long newLeaderId) {
        log.info("Changing leader of team {} to user {}", teamId, newLeaderId);

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        User newLeader = userRepository.findByIdAndNotDeleted(newLeaderId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", newLeaderId);
                    return new ResourceNotFoundException("User not found with ID: " + newLeaderId);
                });

        Optional<TeamLeadershipHistory> currentLeadershipOpt = leadershipRepository.findActiveLeaderByTeamId(teamId);

        if (currentLeadershipOpt.isPresent()) {
            TeamLeadershipHistory currentLeadership = currentLeadershipOpt.get();

            if (currentLeadership.getLeader().getId().equals(newLeaderId)) {
                log.warn("User {} is already the current leader of team {}", newLeaderId, teamId);
                throw new BadRequestException("User is already the current leader of this team");
            }

            currentLeadership.setEndedAt(LocalDateTime.now());
            leadershipRepository.save(currentLeadership);
            log.info("Closed previous leadership for team {}", teamId);
        }

        ensureUserIsActiveMember(team, newLeader);

        TeamLeadershipHistory newLeadership = new TeamLeadershipHistory();
        newLeadership.setTeam(team);
        newLeadership.setLeader(newLeader);
        newLeadership.setStartedAt(LocalDateTime.now());

        TeamLeadershipHistory savedHistory = leadershipRepository.save(newLeadership);
        log.info("New leader assigned successfully: {} to team {}", newLeaderId, teamId);

        return mapToLeaderDTO(savedHistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLeader(Long teamId) {
        log.info("Removing leader from team {}", teamId);

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        Optional<TeamLeadershipHistory> currentLeadershipOpt = leadershipRepository.findActiveLeaderByTeamId(teamId);

        if (currentLeadershipOpt.isEmpty()) {
            log.warn("Team {} has no active leader to remove", teamId);
            throw new BadRequestException("Team has no active leader");
        }

        TeamLeadershipHistory currentLeadership = currentLeadershipOpt.get();
        currentLeadership.setEndedAt(LocalDateTime.now());
        leadershipRepository.save(currentLeadership);

        log.info("Leader removed successfully from team {}", teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamLeaderDTO getCurrentLeader(Long teamId) {
        log.info("Getting current leader of team {}", teamId);

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        return leadershipRepository.findActiveLeaderByTeamId(teamId)
                .map(this::mapToLeaderDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDetailDTO.TeamLeadershipHistoryDTO> getLeadershipHistory(Long teamId) {
        log.info("Getting leadership history of team {}", teamId);

        Team team = teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", teamId);
                    return new ResourceNotFoundException("Team not found with ID: " + teamId);
                });

        List<TeamLeadershipHistory> history = leadershipRepository.findByTeamIdOrderByStartedAtDesc(teamId);

        return history.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    private void ensureUserIsTeamMember(Team team, User user) {
        TeamMember existingMembership = teamMemberRepository.findActiveTeamByUserId(user.getId());

        if (existingMembership != null) {
            if (!existingMembership.getTeam().getId().equals(team.getId())) {
                log.error("User {} is already a member of another team", user.getId());
                throw new BadRequestException("User is already a member of another team");
            }
            log.info("User {} is already a member of team {}", user.getId(), team.getId());
            return;
        }

        TeamMember newMembership = new TeamMember();
        newMembership.setUser(user);
        newMembership.setTeam(team);
        newMembership.setStatus(MembershipStatus.ACTIVE);
        newMembership.setJoinedAt(LocalDateTime.now());

        teamMemberRepository.save(newMembership);
        log.info("User {} added as member of team {}", user.getId(), team.getId());
    }

    private void ensureUserIsActiveMember(Team team, User user) {
        TeamMember membership = teamMemberRepository.findActiveTeamByUserId(user.getId());

        if (membership == null) {
            log.error("User {} is not a member of any team", user.getId());
            throw new BadRequestException("User must be an active member of the team to become a leader");
        }

        if (!membership.getTeam().getId().equals(team.getId())) {
            log.error("User {} is a member of a different team", user.getId());
            throw new BadRequestException("User is a member of a different team");
        }

        if (membership.getStatus() != MembershipStatus.ACTIVE || membership.getLeftAt() != null) {
            log.error("User {} is not an active member of team {}", user.getId(), team.getId());
            throw new BadRequestException("User must be an active member of the team to become a leader");
        }
    }

    private TeamLeaderDTO mapToLeaderDTO(TeamLeadershipHistory history) {
        return TeamLeaderDTO.builder()
                .userId(history.getLeader().getId())
                .name(history.getLeader().getName())
                .email(history.getLeader().getEmail())
                .startedAt(history.getStartedAt())
                .build();
    }

    private TeamDetailDTO.TeamLeadershipHistoryDTO mapToHistoryDTO(TeamLeadershipHistory history) {
        return TeamDetailDTO.TeamLeadershipHistoryDTO.builder()
                .leaderId(history.getLeader().getId())
                .leaderName(history.getLeader().getName())
                .leaderEmail(history.getLeader().getEmail())
                .startedAt(history.getStartedAt())
                .endedAt(history.getEndedAt())
                .isCurrent(history.getEndedAt() == null)
                .build();
    }
}
