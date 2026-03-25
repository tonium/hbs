package com.hbs.subscription.entity;

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
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orgId;

    @Column(nullable = false, length = 64)
    private String userId;

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
    public Subscription(String orgId, String userId, String programId, String channelId) {
        this.orgId = orgId;
        this.userId = userId;
        this.programId = programId;
        this.channelId = channelId;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
    }

    public void resume() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }
}
