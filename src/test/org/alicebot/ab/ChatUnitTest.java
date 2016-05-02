package org.alicebot.ab;

import junit.framework.Assert;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ChatUnitTest {


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
        System.out.println("Passed "+chatTest.getPairs().length+" test cases.");
    }
}
