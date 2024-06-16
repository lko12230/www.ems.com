package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.Team;

public interface Teamdao extends JpaRepository<Team, Integer> {
//	@Query("select u.team_id from Team u")
//	public List<String> getAllDataFromTeam();

	@Query("select u.team_id, u.team_description from Team u where u.team_id = :team_id")
	public String getAllDataFromTeamDescription(String team_id);
}
