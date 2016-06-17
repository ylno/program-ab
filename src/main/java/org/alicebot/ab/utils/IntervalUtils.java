package org.alicebot.ab.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.LenientChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seibertmedia.chatbot.CommandLineInteraction;
import net.seibertmedia.chatbot.UserInteraction;

public class IntervalUtils {

    private static final Logger logger = LoggerFactory.getLogger(IntervalUtils.class);

    public static void test () {
    UserInteraction userinteraction = new CommandLineInteraction();
        String date1 = "23:59:59.00";
        String date2 = "12:00:00.00";
        String format = "HH:mm:ss.SS";
        int hours = getHoursBetween(date2, date1, format);
        userinteraction.outputForUserWithNewline("Hours = "+hours);
        date1 = "January 30, 2013";
        date2 = "August 2, 1960";
        format = "MMMMMMMMM dd, yyyy";
        int years = getYearsBetween(date2, date1, format);
        userinteraction.outputForUserWithNewline("Years = "+years);
    }
    // http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
    public static int getHoursBetween(final String date1, final String date2, String format){
        try {
        final DateTimeFormatter fmt =
                DateTimeFormat
                        .forPattern(format)
                        .withChronology(
                                LenientChronology.getInstance(
                                        GregorianChronology.getInstance()));
        return Hours.hoursBetween(
                fmt.parseDateTime(date1),
                fmt.parseDateTime(date2)
        ).getHours();
        } catch (Exception ex) {
            logger.error("{}", ex);
            return 0;
        }
    }
    public static int getYearsBetween(final String date1, final String date2, String format){
        try {
        final DateTimeFormatter fmt =
                DateTimeFormat
                        .forPattern(format)
                        .withChronology(
                                LenientChronology.getInstance(
                                        GregorianChronology.getInstance()));
        return Years.yearsBetween(
                fmt.parseDateTime(date1),
                fmt.parseDateTime(date2)
        ).getYears();
        } catch (Exception ex) {
            logger.error("{}", ex);
            return 0;
        }
    }
    public static int getMonthsBetween(final String date1, final String date2, String format){
        try {
        final DateTimeFormatter fmt =
                DateTimeFormat
                        .forPattern(format)
                        .withChronology(
                                LenientChronology.getInstance(
                                        GregorianChronology.getInstance()));
            DateTime startDate = fmt.parseDateTime(date1);
            DateTime endDate = fmt.parseDateTime(date2);
            return Months.monthsBetween(startDate, endDate
        ).getMonths();
        } catch (Exception ex) {
            logger.error("{}", ex);
            return 0;
        }
    }
    public static int getDaysBetween(final String date1, final String date2, String format){

        try {
            final DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));

            DateTime startDate = fmt.parseDateTime(date1);
            DateTime endDate = fmt.parseDateTime(date2);

            // if startDate id after enddate add a year to the enddate
            if(endDate.isBefore(startDate)) {
                endDate = endDate.plusYears(1);
            }

            return Days.daysBetween(

                    startDate,
                    endDate
            ).getDays();
        } catch (Exception ex) {
            logger.error("{}", ex);
            return 0;
        }
    }
}
