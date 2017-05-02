package org.alicebot.ab;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsPluginTest {

  private static final Logger logger = LoggerFactory.getLogger(NewsPluginTest.class);

  @Test
  public void getFirstEntry() throws Exception {
    NewsPlugin newsPlugin = new NewsPlugin();
    String firstEntry = newsPlugin.getFirstEntry();
    logger.debug(firstEntry);
  }

}
