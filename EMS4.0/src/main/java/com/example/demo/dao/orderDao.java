package com.example.demo.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.Payment_Order_Info;

public interface orderDao extends JpaRepository<Payment_Order_Info, Integer> {
	
public Payment_Order_Info findByOrderId(String orderId);

@Query(value = "select count(1) from orders",nativeQuery = true)
public int countt();

@Query(value = "select u.sno from orders u order by u.sno desc limit 1", nativeQuery = true)
public int getLastId();

@Query(value = "select * from orders u  where company_id=?1 and status='PAID' order by u.sno desc limit 1", nativeQuery = true)
public Optional<Payment_Order_Info> findbycompany(String company_id);

@Query(value = "select count(1) from (select * from orders u  where company_id=?1 and status='paid' and license_status='ACTIVE' and u.subscription_start_date <= (NOW() - INTERVAL 1 DAY) ORDER BY u.system_date_and_time DESC LIMIT 1) as subquery", nativeQuery = true)
public int check_users_subscription_plan(String company_id);

@Query(value = "select u.receipt from orders u order by u.receipt desc limit 1", nativeQuery = true)
public String getLastReceiptNumber();

@Query(value = "select * from orders u where u.order_id=?1 and status='PAID' order by u.system_date_and_time desc limit 1", nativeQuery = true)
public Optional<Payment_Order_Info> findOrderByTransactionId(String transaction_id);

@Query(value = "select *  from orders u where u.company_id=?1 order by u.system_date_and_time desc",nativeQuery = true)
public List<Payment_Order_Info> transactionHistoryFindByCompanyId(String company_id);

@Query(value = "update orders u set u.license_status='INACTIVE' where u.company_id=?1 and status='paid' and u.subscription_start_date <= (NOW() - INTERVAL 1 DAY) ORDER BY u.system_date_and_time DESC", nativeQuery = true)
@Modifying
public void expired_license_status(String company_id);

}
