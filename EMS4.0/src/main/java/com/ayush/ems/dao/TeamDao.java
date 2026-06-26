package com.ayush.ems.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Team;
@Repository
public interface TeamDao extends JpaRepository<Team, Integer> {
//	@Query("select u.team_id from Team u")
//	public List<String> getAllDataFromTeam();

	@Query("select u.team_id, u.team_description from Team u where u.team_id = :team_id and company_id = :company_id")
	public String getAllDataFromTeamDescription(String team_id, String company_id);

	@Query(value = "select count(1) from team", nativeQuery = true)
	public int getTeamCount();

	@Query(value = "select id from team order by id desc limit 1", nativeQuery = true)
	public int getLastSno();

	@Query(value = "select team_id from team order by id desc limit 1", nativeQuery = true)
	public String getLastTeamId();
	
	@Query(value = "SELECT * FROM team WHERE company_id = :company_id", nativeQuery = true)
	List<Team> findAllByCompanyId(@Param("company_id") String companyId);
	
	@Query("SELECT t FROM Team t WHERE t.team_id = :teamId AND t.company_id = :companyId")
	Team findByTeamIdAndCompanyId(@Param("teamId") String teamId, 
	                                        @Param("companyId") String companyId);


}
