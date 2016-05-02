package org.alicebot.ab;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BotTest {

  @Test
  public void testDefaultConstructor() {
    Bot bot = new Bot();
    assertThat(bot.root_path, is("c:/ab"));
  }

  @Test
  public void testFullConstructor() {
    Bot bot = new Bot("botname", "mypath", "action");
    assertThat(bot.bot_name_path, is("mypath/bots/botname"));
  }

}
