package com.project.monu.domain.article.collector.rss;

import java.io.IOException;
import java.net.URLConnection;

@FunctionalInterface
public interface RssConnectionFactory {

    URLConnection open(String url) throws IOException;
}
