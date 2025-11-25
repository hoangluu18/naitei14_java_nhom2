package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.TeamMapper;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.services.TeamService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
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

        return teamMapper.toDTO(savedTeam);
    }

    @Override
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

        team.setUpdatedAt(LocalDateTime.now());

        Team updatedTeam = teamRepository.save(team);
        log.info("Team updated successfully with ID: {}", updatedTeam.getId());

        return teamMapper.toDTO(updatedTeam);
    }

    @Override
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
}
