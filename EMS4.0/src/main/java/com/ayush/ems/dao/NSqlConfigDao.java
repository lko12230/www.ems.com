package com.ayush.ems.dao;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.NSqlConfig;
@Repository
public interface NSqlConfigDao  extends JpaRepository<NSqlConfig, Integer > {

	   // Method to fetch user by email
		@Query("select u from NSqlConfig u where u.Configkey = :Configkey")
		public Optional<NSqlConfig> findByConfigkey(@Param("Configkey") String configkey);
		
		@Query(value = "SELECT c.nsql_value FROM nsql_config c WHERE c.company_id = :companyId and c.configkey = :Configkey ",nativeQuery = true)
		String findbyConfigKeyAndCompanyId(@Param("companyId") String companyId, @Param("Configkey") String configkey);
		
		@Query(value = "SELECT SUBSTRING_INDEX(c.nsql_value, ',', 1) AS folderId, " +
	               "COALESCE(c.configurable, '') AS configurable " +
	               "FROM nsql_config c " +
	               "WHERE c.company_id = :companyId " +
	               "AND c.configkey = :configKey " +
	               "LIMIT 1", nativeQuery = true)
	List<Object[]> findNsqlValueAndIsNotifybyConfigKeyAndCompanyId(
	    @Param("companyId") String companyId,
	    @Param("configKey") String configKey
	);
	
	
	@Query(value = "SELECT SUBSTRING_INDEX(c.nsql_value, ',', 1) AS isSalarySlipGenerate, " +
            "COALESCE(c.configurable, '') AS configurable " +
            "FROM nsql_config c " +
            "WHERE c.company_id = :companyId " +
            "AND c.configkey = :configKey " +
            "LIMIT 1", nativeQuery = true)
List<Object[]> findNsqlValueAndIsSalaryDatebyConfigKeyAndCompanyId(
 @Param("companyId") String companyId,
 @Param("configKey") String configKey);

@Modifying
@Transactional
@Query(value = "UPDATE nsql_config SET nsql_value = :value, configurable = :configDate " +
               "WHERE company_id = :companyId AND configkey = :configKey", nativeQuery = true)
int updateSalarySlipConfig(@Param("companyId") String companyId,
                           @Param("configKey") String configKey,
                           @Param("value") String value,
                           @Param("configDate") String configDate);


@Modifying
@Transactional
@Query(value = "INSERT INTO nsql_config (company_id, configkey, nsql_value, configurable) " +
               "VALUES (:companyId, :configKey, :value, :configDate)", nativeQuery = true)
void insertSalarySlipConfig(@Param("companyId") String companyId,
                            @Param("configKey") String configKey,
                            @Param("value") String value,
                            @Param("configDate") String configDate);


// ✅ Get Max Image Size by companyId
@Query(value = "SELECT c.nsql_value FROM nsql_config c " +
        "WHERE c.company_id = :companyId " +
        "AND c.configkey = 'MAX_IMAGE_SIZE' LIMIT 1", nativeQuery = true)
String findMaxImageSizeByCompanyId(@Param("companyId") String companyId);

// ✅ Get Max File Size by companyId
@Query(value = "SELECT c.nsql_value FROM nsql_config c " +
        "WHERE c.company_id = :companyId " +
        "AND c.configkey = 'MAX_FILE_SIZE' LIMIT 1", nativeQuery = true)
String findMaxFileSizeByCompanyId(@Param("companyId") String companyId);

}
