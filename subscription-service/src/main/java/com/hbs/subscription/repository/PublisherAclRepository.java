package com.hbs.subscription.repository;

import com.hbs.subscription.entity.PublisherAcl;
import com.hbs.subscription.enums.SubjectType;
import com.hbs.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublisherAclRepository extends JpaRepository<PublisherAcl, Long> {

    Optional<PublisherAcl> findByOrgIdAndSubjectTypeAndSubjectIdAndProgramIdAndChannelIdAndStatus(
            String orgId, SubjectType subjectType, String subjectId,
            String programId, String channelId, SubscriptionStatus status);

    List<PublisherAcl> findByOrgIdAndSubjectTypeAndSubjectIdAndStatus(
            String orgId, SubjectType subjectType, String subjectId, SubscriptionStatus status);
}
