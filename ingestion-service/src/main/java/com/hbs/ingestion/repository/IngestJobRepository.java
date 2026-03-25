package com.hbs.ingestion.repository;

import com.hbs.ingestion.entity.IngestJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IngestJobRepository extends JpaRepository<IngestJob, UUID> {

    /**
     * PENDING 상태의 job을 최대 limit개 잠금 취득.
     * FOR UPDATE SKIP LOCKED: 다른 트랜잭션이 처리 중인 행은 건너뛴다.
     * 다수의 worker 인스턴스가 동시에 실행될 때 중복 처리를 방지한다.
     */
    @Query(value = """
            SELECT * FROM ingest_jobs
            WHERE status = 'PENDING'
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<IngestJob> findPendingJobsForUpdate(@Param("limit") int limit);
}
