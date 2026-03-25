package com.hbs.subscription.repository;

import com.hbs.subscription.entity.Subscription;
import com.hbs.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByOrgIdAndUserIdAndStatus(String orgId, String userId,
                                                      SubscriptionStatus status);

    List<Subscription> findByOrgIdAndUserId(String orgId, String userId);

    Optional<Subscription> findByIdAndUserId(Long id, String userId);
}
