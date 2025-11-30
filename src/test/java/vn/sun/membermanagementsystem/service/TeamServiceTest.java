package vn.sun.membermanagementsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.TeamMapper;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.services.impls.TeamServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private vn.sun.membermanagementsystem.services.TeamLeadershipService teamLeadershipService;

    @InjectMocks
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTeam_Success() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");
        request.setDescription("Description A");

        Team savedTeam = new Team();
        savedTeam.setId(1L);
        savedTeam.setName("Team A");
        savedTeam.setDescription("Description A");
        savedTeam.setCreatedAt(LocalDateTime.now());
        savedTeam.setUpdatedAt(LocalDateTime.now());

        TeamDTO response = new TeamDTO();
        response.setId(1L);
        response.setName("Team A");
        response.setDescription("Description A");

        when(teamRepository.existsByNameAndNotDeleted("Team A")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(teamMapper.toDTO(savedTeam)).thenReturn(response);

        TeamDTO result = teamService.createTeam(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Team A", result.getName());
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamLeadershipService, never()).assignLeader(any(), any());
    }

    @Test
    void createTeam_DuplicateName_ThrowsException() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");

        when(teamRepository.existsByNameAndNotDeleted("Team A")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamService.createTeam(request));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void updateTeam_Success() {
        Long teamId = 1L;
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Team B");
        request.setDescription("Description B");

        Team existingTeam = new Team();
        existingTeam.setId(teamId);
        existingTeam.setName("Team A");
        existingTeam.setDescription("Description A");
        existingTeam.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingTeam.setUpdatedAt(LocalDateTime.now().minusDays(1));

        Team updatedTeam = new Team();
        updatedTeam.setId(teamId);
        updatedTeam.setName("Team B");
        updatedTeam.setDescription("Description B");
        updatedTeam.setCreatedAt(existingTeam.getCreatedAt());
        updatedTeam.setUpdatedAt(LocalDateTime.now());

        TeamDTO response = new TeamDTO();
        response.setId(teamId);
        response.setName("Team B");
        response.setDescription("Description B");

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByNameAndNotDeletedAndIdNot("Team B", teamId)).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(updatedTeam);
        when(teamMapper.toDTO(updatedTeam)).thenReturn(response);

        TeamDTO result = teamService.updateTeam(teamId, request);

        assertNotNull(result);
        assertEquals("Team B", result.getName());
        assertEquals("Description B", result.getDescription());
        verify(teamRepository, times(1)).save(existingTeam);
        verify(teamLeadershipService, never()).changeLeader(any(), any());
    }

    @Test
    void updateTeam_NotFound_ThrowsException() {
        Long teamId = 1L;
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Team B");

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.updateTeam(teamId, request));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void updateTeam_DuplicateName_ThrowsException() {
        Long teamId = 1L;
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Team B");

        Team existingTeam = new Team();
        existingTeam.setId(teamId);
        existingTeam.setName("Team A");

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByNameAndNotDeletedAndIdNot("Team B", teamId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamService.updateTeam(teamId, request));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void deleteTeam_Success() {
        Long teamId = 1L;

        Team existingTeam = new Team();
        existingTeam.setId(teamId);
        existingTeam.setName("Team A");
        existingTeam.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingTeam.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        boolean result = teamService.deleteTeam(teamId);

        assertTrue(result);
        assertNotNull(existingTeam.getDeletedAt());
        verify(teamRepository, times(1)).save(existingTeam);
    }

    @Test
    void deleteTeam_NotFound_ThrowsException() {
        Long teamId = 1L;

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.deleteTeam(teamId));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void getTeamDetail_Success() {
        Long teamId = 1L;

        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setDescription("Description A");
        team.setCreatedAt(LocalDateTime.now().minusDays(1));
        team.setUpdatedAt(LocalDateTime.now().minusDays(1));

        TeamDetailDTO detailDTO = new TeamDetailDTO();
        detailDTO.setId(teamId);
        detailDTO.setName("Team A");
        detailDTO.setDescription("Description A");

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.of(team));
        when(teamMapper.toDetailDTO(team)).thenReturn(detailDTO);

        TeamDetailDTO result = teamService.getTeamDetail(teamId);

        assertNotNull(result);
        assertEquals(teamId, result.getId());
        assertEquals("Team A", result.getName());
        verify(teamRepository, times(1)).findByIdAndNotDeleted(teamId);
    }

    @Test
    void getTeamDetail_NotFound_ThrowsException() {
        Long teamId = 1L;

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamDetail(teamId));
    }

    @Test
    void createTeam_WithLeader_Success() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");
        request.setDescription("Description A");
        request.setLeaderId(1L);

        Team savedTeam = new Team();
        savedTeam.setId(1L);
        savedTeam.setName("Team A");
        savedTeam.setDescription("Description A");
        savedTeam.setCreatedAt(LocalDateTime.now());
        savedTeam.setUpdatedAt(LocalDateTime.now());

        TeamDTO response = new TeamDTO();
        response.setId(1L);
        response.setName("Team A");
        response.setDescription("Description A");

        when(teamRepository.existsByNameAndNotDeleted("Team A")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(teamMapper.toDTO(savedTeam)).thenReturn(response);

        TeamDTO result = teamService.createTeam(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamLeadershipService, times(1)).assignLeader(1L, 1L);
    }

    @Test
    void updateTeam_WithLeaderChange_Success() {
        Long teamId = 1L;
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Team B");
        request.setLeaderId(2L);

        Team existingTeam = new Team();
        existingTeam.setId(teamId);
        existingTeam.setName("Team A");
        existingTeam.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingTeam.setUpdatedAt(LocalDateTime.now().minusDays(1));

        TeamDTO response = new TeamDTO();
        response.setId(teamId);
        response.setName("Team B");

        when(teamRepository.findByIdAndNotDeleted(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByNameAndNotDeletedAndIdNot("Team B", teamId)).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);
        when(teamMapper.toDTO(existingTeam)).thenReturn(response);

        TeamDTO result = teamService.updateTeam(teamId, request);

        assertNotNull(result);
        verify(teamLeadershipService, times(1)).changeLeader(teamId, 2L);
    }
}
