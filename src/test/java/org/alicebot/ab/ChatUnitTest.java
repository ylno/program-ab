package org.alicebot.ab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatUnitTest {
  private static final Logger logger = LoggerFactory.getLogger(ChatUnitTest.class);

  @Before
  public void setUp() throws Exception {
    Locale.setDefault(Locale.ENGLISH);

  }

  @Test
  public void testMultisentenceRespond() throws Exception {
    if (false) {
      return;
    }
    Bot bot = new Bot("testbot", "src/test/resources", "auto");
    ChatTest chatTest = new ChatTest(bot);

    for (int i = 0; i < chatTest.getPairs().length; i++) {
      String request = chatTest.getPairs()[i][0];
      String expected = chatTest.getPairs()[i][1];
      String actual = chatTest.getChatSession().multisentenceRespond(request);
      logger.debug("request: " + request);
      assertThat(actual, containsString(expected));
    }
    logger.debug("Passed " + chatTest.getPairs().length + " test cases.");
  }

  @Test
  public void testParallelChats() {
    Bot bot = new Bot("testbot", "src/test/resources", "auto");
    Chat test1 = new Chat(bot);
    Chat test2 = new Chat(bot);
    String answer1 = test1.multisentenceRespond("My name is gerulf.");
    assertThat(answer1, containsString("Gerulf"));
    assertThat(test1.multisentenceRespond("What is my name?"), containsString("Gerulf"));

    String answer2 = test2.multisentenceRespond("My name is Uwe");
    assertThat(answer2, containsString("Uwe"));
    assertThat(test1.multisentenceRespond("What is my name?"), containsString("Gerulf"));
  }

  @Test
  @Ignore
  public void testBotVsBot() {
    Bot bot = new Bot("german-bot", "../", "auto");
    Chat chat1 = new Chat(bot, false, "1");
    Chat chat2 = new Chat(bot, false, "2");

    String abot1 = chat1.multisentenceRespond("Mein Name ist Michael");
    String abot2;
    for (int i = 0; i < 100; i++) {
      logger.debug("bot1: {}", abot1);
      abot2 = chat2.multisentenceRespond(abot1);
      logger.debug("bot2: {}", abot2);
      abot1 = chat1.firstsentenceRespond(abot2);
    }

  }

  @Test
  public void testThat() {
    Bot bot = new Bot("testbot", "src/test/resources", "auto");
    Chat test1 = new Chat(bot);
    String answer1 = test1.multisentenceRespond("GIVE ME DIRECTIONS");
    assertThat(answer1, containsString("Where do you want to go?"));

    String answer2 = test1.multisentenceRespond("Pain");
    assertThat(answer2, containsString("Here are the driving directions."));

  }

}
