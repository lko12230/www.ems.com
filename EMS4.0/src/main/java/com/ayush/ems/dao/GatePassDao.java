package com.ayush.ems.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.GatePassGenerator;
@Repository
public interface GatePassDao extends JpaRepository<GatePassGenerator, String> {
	@Query("SELECT g.gatePassId FROM GatePassGenerator g ORDER BY g.gatePassId DESC")
	List<String> findLatestGatePassId(Pageable pageable);

}
