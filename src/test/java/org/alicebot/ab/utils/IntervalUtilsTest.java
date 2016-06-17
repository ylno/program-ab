package org.alicebot.ab.utils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(IntervalUtilsTest.class);
    @Test
    public void testGetHoursBetween() throws Exception {

    }

    @Test
    public void testGetYearsBetween() throws Exception {

    }

    @Test
    public void testGetMonthsBetween() throws Exception {

    }

    @Test
    public void testGetDaysBetween() throws Exception {
        Locale.setDefault(new Locale("de", "DE"));
        int daysBetween = IntervalUtils.getDaysBetween("Juli 1", "Juli 10", "MMMMMMMM dd");
        assertThat(daysBetween, is(9));


        int daysBetween2 = IntervalUtils.getDaysBetween("Januar 10", "Januar 1", "MMMMMMMM dd");
        assertThat(daysBetween2>300, is(true));
    }

    @Test
    public void testGetDaysBetweenError() throws Exception {
        Locale.setDefault(new Locale("de", "DE"));
        int daysBetween = IntervalUtils.getDaysBetween("1 August", "3 August", "dd MMMMMMMM");
        assertThat(daysBetween, is(2));
    }
}
