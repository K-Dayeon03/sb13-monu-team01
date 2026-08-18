package com.project.monu.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    /**
     * QueryDSL에서 JPA 쿼리를 만들 때 사용하는 핵심 객체입니다.
     * Repository 구현체에서 생성자 주입으로 받아 동적 쿼리를 작성합니다.
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
