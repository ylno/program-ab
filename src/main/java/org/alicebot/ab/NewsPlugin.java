package org.alicebot.ab;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class NewsPlugin {

  private static final Logger logger = LoggerFactory.getLogger(NewsPlugin.class);

  public NewsPlugin() {
  }

  public String getFeed() {

    try {
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed feed = input.build(new XmlReader(new URL("http://www.taz.de/Themen-des-Tages/!p15;rss/")));

      final StringBuilder result = new StringBuilder();
      for (Object syndEntryO : feed.getEntries()) {
        SyndEntry syndEntry = (SyndEntry) syndEntryO;
        result.append(syndEntry.getDescription().getValue()).append("\n");
      }

      return result.toString();
    } catch (FeedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
