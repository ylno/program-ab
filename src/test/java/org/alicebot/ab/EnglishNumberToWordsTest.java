package org.alicebot.ab;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EnglishNumberToWordsTest {

  @Test
  public void testConvert() throws Exception {
    assertThat(EnglishNumberToWords.convert(1), is("one"));
    assertThat(EnglishNumberToWords.convert(9), is("nine"));
    assertThat(EnglishNumberToWords.convert(90), is("ninety"));
    assertThat(EnglishNumberToWords.convert(99), is("ninety nine"));
    assertThat(EnglishNumberToWords.convert(100), is("one hundred"));
    assertThat(EnglishNumberToWords.convert(999), is("nine hundred ninety nine"));
    assertThat(EnglishNumberToWords.convert(1000), is("one thousand "));
  }

  @Test
  public void testMakeSetMap() throws Exception {

  }
}
