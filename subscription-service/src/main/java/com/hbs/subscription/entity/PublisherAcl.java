package com.hbs.subscription.entity;

import com.hbs.subscription.enums.SubjectType;
import com.hbs.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "publisher_acl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublisherAcl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubjectType subjectType;

    @Column(nullable = false, length = 128)
    private String subjectId;

    @Column(nullable = false, length = 64)
    private String programId;

    @Column(length = 64)
    private String channelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubscriptionStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    public PublisherAcl(String orgId, SubjectType subjectType, String subjectId,
                         String programId, String channelId) {
        this.orgId = orgId;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.programId = programId;
        this.channelId = channelId;
        this.status = SubscriptionStatus.ACTIVE;
    }
}
