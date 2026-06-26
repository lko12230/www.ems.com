package com.ayush.ems.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Payment_Order_Info;
@Repository
public interface OrderDao extends JpaRepository<Payment_Order_Info, Integer> {

    Payment_Order_Info findByOrderId(String orderId);

    @Query(value = "SELECT COUNT(1) FROM orders", nativeQuery = true)
    int countt();

    @Query(value = "SELECT * FROM orders u WHERE u.license_status='ACTIVE'", nativeQuery = true)
    List<Payment_Order_Info> findAllByActive();

    @Query(value = "SELECT u.sno FROM orders u ORDER BY u.sno DESC LIMIT 1", nativeQuery = true)
    int getLastId();

    @Query(value = "SELECT * FROM orders u WHERE company_id=?1 AND status='PAID' AND (license_status='ACTIVE' OR license_status='INACTIVE')  ORDER BY u.subscription_expiry_date DESC LIMIT 1", nativeQuery = true)
    Optional<Payment_Order_Info> findbycompany(String company_id);

    @Query(value = "SELECT * FROM orders WHERE company_id = ?1 AND status='PAID' AND license_status='WAITING' ORDER BY subscription_expiry_date ASC", nativeQuery = true)
    List<Payment_Order_Info> findbycompanyUpcomingRecharges(String company_id);

    @Query(value = "SELECT * FROM orders WHERE license_status = 'ACTIVE' AND status = 'PAID' AND DATE(subscription_expiry_date) BETWEEN DATE_SUB(CURDATE(), INTERVAL (SELECT nsql_value FROM nsql_config where configkey='INTERVALDAYS') DAY) AND CURDATE() AND NOT EXISTS (SELECT 1 FROM orders u WHERE u.company_id = orders.company_id AND u.status = 'PAID' AND u.license_status = 'WAITING' AND DATE(u.subscription_expiry_date) > CURDATE())", nativeQuery = true)
    List<Payment_Order_Info> findExpiredPlansWithoutUpcomingRecharges();

    @Query(value = "SELECT u.receipt FROM orders u ORDER BY u.receipt DESC LIMIT 1", nativeQuery = true)
    String getLastReceiptNumber();

    @Query(value = "SELECT * FROM orders u WHERE u.order_id=?1 AND status='PAID' ORDER BY u.system_date_and_time DESC LIMIT 1", nativeQuery = true)
    Optional<Payment_Order_Info> findOrderByTransactionId(String transaction_id);

    @Query(value = "SELECT * FROM orders u WHERE u.company_id=?1 ORDER BY u.system_date_and_time DESC", nativeQuery = true)
    List<Payment_Order_Info> transactionHistoryFindByCompanyId(String company_id);

    @Modifying
    @Query(value = "UPDATE orders u SET u.license_status='INACTIVE' WHERE u.company_id=?1 AND status='PAID' AND u.license_status='ACTIVE' ORDER BY u.system_date_and_time DESC", nativeQuery = true)
    void expired_license_status(String company_id);

    @Query(value = "SELECT * FROM orders WHERE company_id = :companyId AND license_status = 'WAITING' AND status='PAID' ORDER BY subscription_expiry_date ASC LIMIT 1", nativeQuery = true)
    Payment_Order_Info findUpcomingPlanByCompanyId(@Param("companyId") String companyId);

    @Modifying
    @Query(value = "UPDATE orders u SET u.license_status='ACTIVE' WHERE u.company_id=?1 AND status='PAID' AND u.license_status='WAITING' ORDER BY u.subscription_expiry_date ASC LIMIT 1", nativeQuery = true)
    void updatePlanToActive(String company_id);

    @Query(value = "SELECT * FROM orders WHERE company_id = :companyId AND license_status = 'ACTIVE' ORDER BY subscription_expiry_date DESC LIMIT 1", nativeQuery = true)
    Payment_Order_Info findActivePlanByCompanyId(@Param("companyId") String companyId);

    @Query(value = "SELECT * FROM orders WHERE company_id = :companyId AND (license_status = 'WAITING' OR license_status = 'ACTIVE') ORDER BY subscription_expiry_date DESC LIMIT 1", nativeQuery = true)
    Payment_Order_Info findLastPlanByCompanyId(@Param("companyId") String companyId);
    
    @Query(value = "SELECT * FROM orders  where status ='pending'", nativeQuery = true)
    List<Payment_Order_Info> findPendingPayments();
}

