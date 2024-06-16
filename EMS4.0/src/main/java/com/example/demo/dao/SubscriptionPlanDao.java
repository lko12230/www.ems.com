package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.SubscriptionPlans;

public interface SubscriptionPlanDao extends JpaRepository<SubscriptionPlans, Integer> {
	@Query(value = "select * from subscription_plans u where u.sno='1' ",nativeQuery = true)
public Optional<SubscriptionPlans> getAllPlans();
}
