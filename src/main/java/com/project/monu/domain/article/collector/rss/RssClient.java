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

    public SyndFeed fetch(String url) {
        try{
            URLConnection connection = new URL(url).openConnection();
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
