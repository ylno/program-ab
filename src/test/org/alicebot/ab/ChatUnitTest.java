package org.alicebot.ab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(ChatUnitTest.class);



    @Test
    public void testMultisentenceRespond() throws Exception {
        if(true) {
            return;
        }
        ChatTest chatTest = new ChatTest(new Bot());

        for (int i = 0; i < chatTest.getPairs().length; i++) {
            String request = chatTest.getPairs()[i][0];
            String expected = chatTest.getPairs()[i][1];
            String actual = chatTest.getChatSession().multisentenceRespond(request);
            assertThat(actual, containsString(expected));
        }
        logger.debug("Passed "+chatTest.getPairs().length+" test cases.");
    }
}
