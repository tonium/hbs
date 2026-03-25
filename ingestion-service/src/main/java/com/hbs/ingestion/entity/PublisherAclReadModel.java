package com.hbs.ingestion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ingestion-service에서 publisher_acl 테이블을 읽기 전용으로 조회하기 위한 엔티티.
 * 쓰기 작업은 subscription-service를 통해서만 수행한다.
 */
@Entity
@Table(name = "publisher_acl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublisherAclReadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String orgId;

    @Column(length = 16)
    private String subjectType;

    @Column(length = 128)
    private String subjectId;

    @Column(length = 64)
    private String programId;

    @Column(length = 64)
    private String channelId;

    @Column(length = 16)
    private String status;
}
