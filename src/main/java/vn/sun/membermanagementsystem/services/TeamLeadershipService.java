package vn.sun.membermanagementsystem.services;

import vn.sun.membermanagementsystem.dto.response.TeamLeaderDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;

import java.util.List;

public interface TeamLeadershipService {

    TeamLeaderDTO assignLeader(Long teamId, Long leaderId);

    TeamLeaderDTO changeLeader(Long teamId, Long newLeaderId);

    void removeLeader(Long teamId);

    TeamLeaderDTO getCurrentLeader(Long teamId);

    List<TeamDetailDTO.TeamLeadershipHistoryDTO> getLeadershipHistory(Long teamId);
}
