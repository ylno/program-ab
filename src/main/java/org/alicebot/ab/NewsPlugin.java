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

  private static int MAXLENGTH = 4096;

  public NewsPlugin() {
  }

  public String getFeed() {

    try {
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed feed = input.build(new XmlReader(new URL("http://www.taz.de/Themen-des-Tages/!p15;rss/")));

      final StringBuilder result = new StringBuilder();
      for (Object syndEntryO : feed.getEntries()) {
        SyndEntry syndEntry = (SyndEntry) syndEntryO;
        StringBuilder next = new StringBuilder();
        next.append(syndEntry.getTitle()).append("\n").append(syndEntry.getLink()).append("\n\n");

        if (result.length() + next.length() > MAXLENGTH) {
          break;
        }
        result.append(next);
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
