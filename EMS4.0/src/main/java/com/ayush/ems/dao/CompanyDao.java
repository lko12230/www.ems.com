package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ayush.ems.entities.CompanyInfo;

public interface CompanyDao extends JpaRepository<CompanyInfo, Integer> {
	@Query("select u from CompanyInfo u where u.Company_id =?1")
	public CompanyInfo getCompanyByCompanyId(String company_id);
	
	@Query(value = "select * from company_info u limit 1",nativeQuery = true)
	public CompanyInfo getCompany();
	
	@Query(value = "select count(1) from company_info",nativeQuery = true)
	public int getCompanyCount();
	
	@Query("select u from CompanyInfo u where u.Company_id =?1")
	 Optional<CompanyInfo> findByCompanyIdOptional(String company_id);
}
