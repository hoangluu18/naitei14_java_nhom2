package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.UserSelectionDTO;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.exception.BadRequestException;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.TeamMapper;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.TeamMapper;
import vn.sun.membermanagementsystem.repositories.TeamMemberRepository;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.services.TeamLeadershipService;
import vn.sun.membermanagementsystem.services.TeamService;

import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final TeamLeadershipService teamLeadershipService;
    private final TeamMemberRepository teamMemberRepository;


    @Override
    public Optional<TeamDTO> getTeamById(Long id) {
        return teamRepository.findById(id)
                .map(teamMapper::toDTO);
    }

    @Override
    public Team getRequiredTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSelectionDTO> getActiveUsersByTeam(Long teamId) {
        log.debug("Loading active users for teamId={}", teamId);
        List<User> users = teamMemberRepository.findActiveUsersByTeamId(teamId);
        return users.stream()
                .map(u -> new UserSelectionDTO(u.getId(), u.getName(), u.getEmail()))
                .collect(Collectors.toList());
    }


    @Transactional
    public TeamDTO createTeam(CreateTeamRequest request) {
        log.info("Creating team with name: {}", request.getName());

        if (teamRepository.existsByNameAndNotDeleted(request.getName())) {
            log.error("Team name already exists: {}", request.getName());
            throw new DuplicateResourceException("Team name already exists: " + request.getName());
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());

        Team savedTeam = teamRepository.save(team);
        log.info("Team created successfully with ID: {}", savedTeam.getId());

        if (request.getLeaderId() != null) {
            log.info("Assigning leader {} to team {}", request.getLeaderId(), savedTeam.getId());
            teamLeadershipService.assignLeader(savedTeam.getId(), request.getLeaderId());
        }

        return teamMapper.toDTO(savedTeam);
    }

    @Transactional
    public TeamDTO updateTeam(Long id, UpdateTeamRequest request) {
        log.info("Updating team with ID: {}", id);

        Team team = teamRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", id);
                    return new ResourceNotFoundException("Team not found with ID: " + id);
                });

        if (request.getName() != null && !request.getName().equals(team.getName())) {
            if (teamRepository.existsByNameAndNotDeletedAndIdNot(request.getName(), id)) {
                log.error("Team name already exists: {}", request.getName());
                throw new DuplicateResourceException("Team name already exists: " + request.getName());
            }
            team.setName(request.getName());
        }

        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        if (request.getLeaderId() != null) {
            handleLeaderChange(id, request.getLeaderId());
        }

        team.setUpdatedAt(LocalDateTime.now());

        Team updatedTeam = teamRepository.save(team);
        log.info("Team updated successfully with ID: {}", updatedTeam.getId());

        return teamMapper.toDTO(updatedTeam);
    }

    @Transactional
    public boolean deleteTeam(Long id) {
        log.info("Soft deleting team with ID: {}", id);

        Team team = teamRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", id);
                    return new ResourceNotFoundException("Team not found with ID: " + id);
                });

        team.setDeletedAt(LocalDateTime.now());
        teamRepository.save(team);

        log.info("Team soft deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDetailDTO getTeamDetail(Long id) {
        log.info("Getting team detail with ID: {}", id);

        Team team = teamRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Team not found with ID: {}", id);
                    return new ResourceNotFoundException("Team not found with ID: " + id);
                });

        TeamDetailDTO detailDTO = teamMapper.toDetailDTO(team);

        log.info("Team detail retrieved successfully for ID: {}", id);
        return detailDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDTO> getAllTeams() {
        log.info("Getting all teams");

        List<Team> teams = teamRepository.findAllNotDeleted();
        return teamMapper.toDTOList(teams);
    }

    private void handleLeaderChange(Long teamId, Long newLeaderId) {
        log.info("Handling leader change for team {}", teamId);

        try {
            teamLeadershipService.changeLeader(teamId, newLeaderId);
        } catch (BadRequestException e) {
            if (e.getMessage().contains("has no active leader")) {
                log.info("Team has no active leader, assigning new leader");
                teamLeadershipService.assignLeader(teamId, newLeaderId);
            } else {
                throw e;
            }
        }
    }
}
