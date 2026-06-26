package com.ayush.ems.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Laptop;
// Class names should be capitalized
@Repository
public interface LaptopDao extends JpaRepository<Laptop, Integer> {

	@Query(value = "SELECT COALESCE((SELECT 1 FROM laptop WHERE laptop_model=:laptop_model AND product_id=:productId AND serial_number=:laptopSerialNumber AND laptop_brand=:laptopBrand  AND company_id=:companyId AND product_id <> 'No Record Found' AND serial_number<> 'No Record Found' AND laptop_brand<> 'No Record Found' order by sno desc limit 1), 0)", nativeQuery = true)
	Integer existsByProductIdAndSerialNumberAndBrand(@Param("laptop_model") String laptop_model ,@Param("productId") String productId,
			@Param("laptopSerialNumber") String laptopSerialNumber, @Param("laptopBrand") String laptopBrand
			,@Param("companyId") String companyId);

	// Fetch distinct brands
	@Query(value = "SELECT DISTINCT l.laptop_brand FROM laptop l where l.company_id=:company_id", nativeQuery = true)
	List<String> findAllDistinctBrands(@Param("company_id") String company_id);

	// Fetch laptops by brand
	@Query(value = "SELECT * FROM laptop l where l.laptop_brand=:brand and l.company_id=:company_id", nativeQuery = true)
	List<Laptop> findByBrand(@Param("brand") String brand, @Param("company_id") String company_id);
	
	// Fetch laptops by brand
	@Query(value = "SELECT * FROM laptop l where l.laptop_brand=:brand and l.laptop_model=:laptop_model and l.company_id=:company_id", nativeQuery = true)
	List<Laptop> findByBrandAndModel(@Param("brand") String brand,@Param("laptop_model") String laptop_model, @Param("company_id") String company_id);
	
	@Query(value = "SELECT * FROM laptop  WHERE laptop_brand=:brand AND laptop_model=:laptop_model AND serial_number=:laptop_serial_number AND company_id=:company_id", nativeQuery = true)
	List<Laptop> findByBrandAndModelAndSerialNumber(@Param("brand") String brand, 
	                                                @Param("laptop_model") String laptop_model, 
	                                                @Param("laptop_serial_number") String laptop_serial_number, 
	                                                @Param("company_id") String company_id);

	// Fetch laptops by brand
	@Query(value = "SELECT laptop_model FROM laptop where laptop_brand=:brand and laptop_model=:model and serial_number=:serialnumber and company_id=:company_id order by sno desc limit 1", nativeQuery = true)
	String getfindByBrandORModelORSerialNumberORProductId(@Param("brand") String brand,
			@Param("model") String model, @Param("serialnumber") String serialnumber,
			@Param("company_id") String company_id);

//	@Query(value = "SELECT COUNT(1) FROM laptop WHERE laptop_brand = :brand AND laptop_model = :model AND serial_number = :serialnumber AND company_id = :company_id", nativeQuery = true)
//	int countByBrandORModelORSerialNumberORProductId(@Param("brand") String brand, @Param("model") String model,
//	      @Param("serialnumber") String serialnumber, @Param("company_id") String string); 

}
