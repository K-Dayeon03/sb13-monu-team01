package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.exception.RssCollectException;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;

@Component
public class RssClient {

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 10_000;

    private final RssConnectionFactory connectionFactory;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public RssClient() {
        this(
                url -> new URL(url).openConnection(),
                DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DEFAULT_READ_TIMEOUT_MILLIS
        );
    }

    RssClient(
            RssConnectionFactory connectionFactory,
            int connectTimeoutMillis,
            int readTimeoutMillis
    ){
        this.connectionFactory = connectionFactory;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public SyndFeed fetch(String url) {
        try{
            URLConnection connection = connectionFactory.open(url);

            connection.setConnectTimeout(connectTimeoutMillis);
            connection.setReadTimeout(readTimeoutMillis);
            connection.setRequestProperty(
                    "User-Agent", "Mozilla/5.0");
            return new SyndFeedInput()
                    .build(new XmlReader(connection.getInputStream()));
        } catch (Exception e) {
            throw new RssCollectException(
                    "RSS 피드를 가져오지 못했습니다: " + url, e);
        }
    }
}
