package com.project.monu.domain.interest.entity;

import com.project.monu.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Long subscriberCount = 0L;

    @Version
    private Long version;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

    private Interest(String name) {
        this.name = name;
    }

    public static Interest create(String name) {
        return new Interest(name);
    }

    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        keyword.assignInterest(this);
    }

    public void updateKeywords(List<String> newKeywords) {
        this.keywords.clear();
        newKeywords.forEach(keyword -> addKeyword(Keyword.of(keyword)));
    }

    public void increaseSubscriberCount() {
        this.subscriberCount++;
    }

    public void decreaseSubscriberCount() {
        this.subscriberCount = Math.max(0, this.subscriberCount - 1);
    }
}
