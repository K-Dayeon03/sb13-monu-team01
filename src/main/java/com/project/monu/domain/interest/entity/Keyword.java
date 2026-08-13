package com.project.monu.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, length = 255)
    private String keyword;

    private Keyword(String keyword) {
        this.keyword = keyword;
    }

    public static Keyword of(String keyword) {
        return new Keyword(keyword);
    }

    void assignInterest(Interest interest) {
        this.interest = interest;
    }
}