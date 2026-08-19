package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class RssCollectorRealTest {

    private static final Logger log = LoggerFactory.getLogger(RssCollectorRealTest.class);

    @Test
    @Disabled("연합뉴스 실제 RSS 호출 - 필요할 때만 수동 실행")
    void 실제_연합뉴스_RSS를_수집한다() {
        // given - 실제 객체들 조립
        RssClient rssClient = new RssClient();
        RssArticleMapper mapper = new RssArticleMapper();
        RssCollector collector = new RssCollector(rssClient, mapper);

        // when - 실제 연합뉴스 RSS 수집
        List<CollectedArticle> articles = collector.collect(
                "https://www.yonhapnewstv.co.kr/browse/feed/");

        // then 콘솔로 확인
        log.info("연합 수집: {}건", articles.size());
        articles.forEach(article -> {
            log.info("제목: {}", article.title());
            log.info("링크: {}", article.originalLink());
            log.info("요약: {}", article.summary());
            log.info("발행일: {}", article.publishedAt());
            log.info("---");
        });
    }

    @Test
    @Disabled("조선일보 실제 RSS 호출 - 수동 실행")
    void 조선일보_RSS를_수집한다 () {
        // given
        RssClient rssClient = new RssClient();
        RssArticleMapper mapper = new RssArticleMapper();
        RssCollector collector = new RssCollector(rssClient, mapper);

        // when
        List<CollectedArticle> articles = collector.collect(
                "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml");

        // then
        log.info("조선 수집: {}건", articles.size());
        articles.forEach(a -> {
            log.info("제목: {}", a.title());
            log.info("링크: {}", a.originalLink());
            log.info("발행일: {}", a.publishedAt());
            log.info("---");
        });
    }

    @Test
    @Disabled("한국경제 실제 RSS 호출 - 수동 실행")
    void 한국경제_RSS를_수집한다 () {
        // given
        RssClient rssClient = new RssClient();
        RssArticleMapper mapper = new RssArticleMapper();
        RssCollector collector = new RssCollector(rssClient, mapper);

        // when
        List<CollectedArticle> articles = collector.collect(
                "https://www.hankyung.com/feed/all-news");

        // then
        log.info("한경 수집: {}건", articles.size());
        articles.forEach(a -> {
            log.info("제목: {}", a.title());
            log.info("링크: {}", a.originalLink());
            log.info("발행일: {}", a.publishedAt());
            log.info("---");
        });
    }

}
