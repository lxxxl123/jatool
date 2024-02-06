package com.chen.jatool.common.utils.support;

import java.util.Calendar;

/**
 * @author chenwh3
 */
public class DateBetween {
    private Calendar from;
    private Calendar to;

    public DateBetween(Calendar from, Calendar to) {
        Calendar temp;
        if(from.after(to)){
            temp = from;
            from = to;
            to = temp;
        }
        this.from = from;
        this.to = to;
    }


    public int year() {
        int year = to.get(Calendar.YEAR) - from.get(Calendar.YEAR);
        return year;
    }

    public int month() {
        int year = year();
        int month = to.get(Calendar.MONTH) - from.get(Calendar.MONTH);
        return year * 12 + month;
    }

    public int day() {
        long ft = from.getTimeInMillis();
        long tt = to.getTimeInMillis();
        long day = (tt - ft) / (1000 * 60 * 60 * 24);
        int h1 = from.get(Calendar.HOUR_OF_DAY);
        int h2 = to.get(Calendar.HOUR_OF_DAY);
        if (h1 > h2) {
            day++;
        }
        return (int) day;

    }

}
