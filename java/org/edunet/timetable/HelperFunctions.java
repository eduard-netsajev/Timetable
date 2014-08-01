package org.edunet.timetable;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Netšajev on 29/07/2014.
 * This class contains different functions
 */
public class HelperFunctions {

    public static int getTodayPageNumber() {

        Calendar today = new GregorianCalendar(Locale.UK);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;//returns int Day of Week

        //Find is it even or odd week, and current day
        boolean evenWeek;
        //Doesn't mean truly even week of the year
        //Must be even/odd according to TTU schedule
        //In future, can be reworked to work so that
        //1st of September is first odd week and counting from there
        evenWeek = today.get(Calendar.WEEK_OF_YEAR) % 2 != 0;

        //Case for Sundays, when we need to see next week Monday timetable
        if (dayOfWeek == 0) {
            dayOfWeek++;
            evenWeek = !evenWeek;
        }

        int initialPage = 5003 + dayOfWeek;
        if(evenWeek){
            initialPage += 6;
        }
        return initialPage;
    }
}
