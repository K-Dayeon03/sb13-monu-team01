package com.project.monu.domain.interest.entity;

import com.project.monu.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "subscription",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_subscription_user_interest", columnNames = {"user_id", "interest_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    private Subscription(UUID userId, Interest interest) {
        this.userId = userId;
        this.interest = interest;
    }

    public static Subscription create(UUID userId, Interest interest) {
        return new Subscription(userId, interest);
    }
}