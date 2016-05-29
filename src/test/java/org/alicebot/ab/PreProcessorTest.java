package org.alicebot.ab;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PreProcessorTest {

  @Test
  public void testSentenceSplit() throws Exception {
    PreProcessor preProcessor = new PreProcessor(new Bot());
    String[] strings = preProcessor.sentenceSplit("This is the fist sentence.");
    assertThat(strings.length, is(1));

    strings = preProcessor.sentenceSplit("This is the fist sentence. This is the second sentence.");
    assertThat(strings.length, is(2));

  }

  @Test
  public void testSentenceSplitMultiplePoints() throws Exception {
    PreProcessor preProcessor = new PreProcessor(new Bot());
    String[] strings = preProcessor.sentenceSplit("This is the fist sentence...");
    assertThat(strings.length, is(1));

    strings = preProcessor.sentenceSplit("This is the fist sentence.... This is the second sentence.");
    assertThat(strings.length, is(2));

  }

  @Test
  public void testSentenceSplitMultiplePointsEmpty() throws Exception {
    PreProcessor preProcessor = new PreProcessor(new Bot());
    String[] strings = preProcessor.sentenceSplit("This is the fist sentence....");
    assertThat(strings.length, is(1));

  }
}
