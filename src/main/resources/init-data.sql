-- 출처 등록 (출처가 등록되어 있지 않으면 기사가 수집되지 않는다)
INSERT INTO article_sources (id, type, name, source_url, enabled, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'API', 'NAVER',    'https://openapi.naver.com/v1/search/news.json', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'YEONHAP',  'https://www.yonhapnewstv.co.kr/browse/feed/', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'HANKYUNG', 'https://www.hankyung.com/feed/all-news', true, now(), now()),
    (gen_random_uuid(), 'RSS', 'CHOSUN',   'https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml', true, now(), now());