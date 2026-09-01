-- ========== 출처 등록 ==========
INSERT INTO article_sources (id, type, name, display_name, source_url, enabled, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'API', 'NAVER', '네이버', 'https://openapi.naver.com/v1/search/news.json', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'YEONHAP', '연합뉴스TV', 'https://www.yonhapnewstv.co.kr/browse/feed/', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'HANKYUNG', '한국경제', 'https://www.hankyung.com/feed/all-news', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'CHOSUN', '조선일보', 'https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml', true, now(), now())
    ON CONFLICT (name) DO NOTHING;