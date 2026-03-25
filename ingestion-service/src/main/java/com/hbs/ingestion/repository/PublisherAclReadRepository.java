package com.hbs.ingestion.repository;

import com.hbs.ingestion.entity.PublisherAclReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublisherAclReadRepository extends JpaRepository<PublisherAclReadModel, Long> {

    /**
     * 특정 subject의 활성 발행 권한 목록 조회.
     * channelId가 NULL이면 해당 program 전체 채널에 대한 권한을 의미한다.
     */
    @Query("""
            SELECT p FROM PublisherAclReadModel p
            WHERE p.orgId = :orgId
              AND p.subjectType = :subjectType
              AND p.subjectId = :subjectId
              AND p.status = 'ACTIVE'
            """)
    List<PublisherAclReadModel> findActiveAcls(@Param("orgId") String orgId,
                                                @Param("subjectType") String subjectType,
                                                @Param("subjectId") String subjectId);
}
