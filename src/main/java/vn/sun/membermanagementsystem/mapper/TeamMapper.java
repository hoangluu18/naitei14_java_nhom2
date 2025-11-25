package vn.sun.membermanagementsystem.mapper;

import org.mapstruct.Mapper;
import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.entities.Team;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    TeamDTO toDTO(Team team);

    List<TeamDTO> toDTOList(List<Team> teams);

    TeamDetailDTO toDetailDTO(Team team);

    Team toEntity(CreateTeamRequest request);
}
