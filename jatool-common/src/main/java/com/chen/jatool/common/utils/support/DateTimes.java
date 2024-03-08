package com.chen.jatool.common.utils.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.*;
import cn.hutool.core.date.format.FastDateFormat;
import com.chen.jatool.common.exception.ServiceException;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;

import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author chenwh3
 */
public class DateTimes implements Comparable<DateTimes>, Cloneable {

    public static final FastDateFormat DOT_DATE_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd");
    public static final FastDateFormat DOT_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd");

    /**
     * note! calendar is a mutable obj
     */
    @Getter
    private Calendar calendar;

    public static DateTimes now() {
        return of(Calendar.getInstance());
    }

    public static DateTimes of(Object o) {
        DateTimes dateTimes = new DateTimes();
        if (o instanceof Calendar) {
            dateTimes.calendar = (Calendar) ((Calendar) o).clone();
            return dateTimes;
        } else if (o instanceof DateTimes) {
            dateTimes.calendar = (Calendar) ((DateTimes) o).getCalendar().clone();
            return dateTimes;
        } else if (o == null || "".equals(o)) {
            throw new ServiceException("DateTimes can not be blank");
        } else {
            dateTimes.calendar = CalendarUtil.calendar(parse(o));
            return dateTimes;
        }
    }

    private DateTimes() {
    }

    private DateTimes(Calendar calendar) {
        this.calendar = calendar;
    }

    public static DateTimes ofNull(Object o) {
        if (o == null || "".equals(o)) {
            return new DateTimes(Calendar.getInstance());
        }
        return of(o);
    }

    public static DateTimes ofNull(Object o, Object orElse) {
        if (o == null || "".equals(o)) {
            return ofNull(orElse);
        }
        return of(o);
    }

    public static DateTimes ofNull(Object o, Supplier<Object> orElse) {
        if (o == null || "".equals(o)) {
            return ofNull(orElse == null ? null : orElse.get());
        }
        return of(o);
    }

    public static Date parse(Object obj) {
        try {
            if (obj instanceof Date) {
                return (Date) obj;
            } else if (obj instanceof String) {
                String dateStr = (String) obj;
                if (dateStr.length() == 6) {
                    return DatePattern.SIMPLE_MONTH_FORMAT.parse(dateStr);
                } else if (dateStr.length() == 7) {
                    return DatePattern.NORM_MONTH_FORMAT.parse(dateStr);
                }
                return DateUtil.parse(dateStr);
            } else if (obj instanceof TemporalAccessor) {
                return Date.from(TemporalAccessorUtil.toInstant((TemporalAccessor) obj));
            } else if (obj instanceof Number) {
                return new Date(((Number) obj).longValue());
            }
            return Optional.of(Convert.toDate(obj)).orElseThrow(IllegalArgumentException::new);
        } catch (Exception e) {
            throw new ServiceException("incorrect date format. input parameter = [{}]", obj);
        }
    }


    public DateTimes truncate(int field) {
        calendar = DateUtils.truncate(getCalendar(), field);
        return this;
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-04-05 00:00:00
     */
    public DateTimes truncateDate() {
        return truncate(Calendar.DATE);
    }

    public DateTimes truncateHour() {
        return truncate(Calendar.HOUR);
    }

    public DateTimes truncateMinute() {
        return truncate(Calendar.MINUTE);
    }

    public DateTimes truncateSecond() {
        return truncate(Calendar.SECOND);
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-04-01 00:00:00
     */
    public DateTimes truncateMonth() {
        return truncate(Calendar.MONTH);
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-01-01 00:00:00
     */
    public DateTimes truncateYear() {
        return truncate(Calendar.YEAR);
    }

    public String format(String pattern) {
        return FastDateFormat.getInstance(pattern).format(getCalendar());
    }

    public String formatDate() {
        return DatePattern.NORM_DATE_FORMAT.format(getCalendar());
    }

    /**
     * yyyyMMdd
     */
    public String formatSimpleDate() {
        return DatePattern.PURE_DATE_FORMAT.format(getCalendar());
    }

    public String formatMonth(){
        return DatePattern.NORM_MONTH_FORMAT.format(getCalendar());
    }


    /**
     * yyyyMM
     */
    public String formatSimpleMonth(){
        return DatePattern.SIMPLE_MONTH_FORMAT.format(getCalendar());
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public String formatDateTime() {
        return DatePattern.NORM_DATETIME_FORMAT.format(getCalendar());
    }

    /**
     * yyyy.MM.dd
     */
    public String formatDotDate(){
        return DOT_DATE_FORMAT.format(getCalendar());
    }

    /**
     * yyyy.MM.dd HH:mm:ss
     */
    public String formatDotDateTime(){
        return DOT_DATETIME_FORMAT.format(getCalendar());
    }

    /**
     * yyyyMMddHHmmss
     */
    public String formatSimpleDateTime() {
        return DatePattern.PURE_DATETIME_FORMAT.format(getCalendar());
    }

    public String formatDateTimeMs() {
        return DatePattern.NORM_DATETIME_MS_FORMAT.format(getDate());
    }

    public DateTimes endOf(int field) {
        calendar = CalendarUtil.ceiling(getCalendar(), DateField.of(field));
        return this;
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-04-30 23:59:59
     */
    public DateTimes endOfMonth() {
        return endOf(Calendar.MONTH);
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-04-05 23:59:59
     */
    public DateTimes endOfDate() {
        return endOf(Calendar.DATE);
    }

    public Date getDate() {
        return calendar.getTime();
    }


    public DateTimes add(int field, int amount) {
        getCalendar().add(field, amount);
        return this;
    }

    public DateTimes add(DateField dateField, int amount) {
        return add(dateField.getValue(), amount);
    }

    public DateTimes addYears(int amount) {
        return add(Calendar.YEAR, amount);
    }

    public DateTimes addHour(int amount) {
        return add(Calendar.HOUR, amount);
    }

    public DateTimes addMinutes(int amount) {
        return add(Calendar.MINUTE, amount);
    }

    public DateTimes addSeconds(int amount) {
        return add(Calendar.SECOND, amount);
    }

    public DateTimes addMillis(int amount) {
        return add(Calendar.MILLISECOND, amount);
    }


    public DateTimes addDays(int amount) {
        return add(Calendar.DATE, amount);
    }

    public DateTimes addMonths(int amount) {
        return add(Calendar.MONTH, amount);
    }

    public int getField(DateField field) {
        return getCalendar().get(field.getValue());
    }

    public DateTimes setField(DateField field, int value) {
        getCalendar().set(field.getValue(), value);
        return this;
    }

    public int getDayOfMonth() {
        return getField(DateField.DAY_OF_MONTH);
    }

    public DateTimes setDayOfMonth(int value) {
        return setField(DateField.DAY_OF_MONTH, value);
    }

    public DateTimes setMonth(int value){
        return setField(DateField.MONTH, value);
    }

    public DateTimes clone() {
        return of(this);
    }

    public boolean before(DateTimes dateTimes) {
        return compareTo(dateTimes) < 0;
    }

    public boolean beforeOrEquals(DateTimes dateTimes) {
        return compareTo(dateTimes) <= 0;
    }

    @Override
    public boolean equals(Object dateTimes) {
        return dateTimes instanceof DateTimes && (((DateTimes) dateTimes).getCalendar()).equals(getCalendar());
    }

    public boolean after(DateTimes dateTimes) {
        return compareTo(dateTimes) > 0;
    }

    public boolean afterOrEquals(DateTimes dateTimes) {
        return compareTo(dateTimes) >= 0;
    }

    public long getTimeInMillis() {
        return getCalendar().getTimeInMillis();
    }

    public void loop(Object toObj, DateField dateField, int step, Consumer<DateTimes> consumer) {
        DateTimes from = clone();
        DateTimes to = DateTimes.of(toObj);
        if (step == 0) {
            throw new ServiceException("step can not be zero");
        }
        for (DateTimes left = from; left.compareTo(to) * step <= 0; ) {
            consumer.accept(left);
            left.add(dateField.getValue(), step);
        }

    }

    public <U> List<U> loopFor(Object toObj, DateField dateField, int step, Function<DateTimes, U> function) {
        List<U> list = new ArrayList<>();
        loop(toObj, dateField, step, e -> list.add(function.apply(e)));
        return list;
    }

    public void loopRange(Object toObj, DateField dateField, int step, BiConsumer<DateTimes, DateTimes> consumer) {
        DateTimes from = clone();
        DateTimes to = DateTimes.of(toObj);
        if (step == 0) {
            throw new ServiceException("step can not be zero");
        }

        DateTimes left = from;
        DateTimes right = from.clone().add(dateField.getValue(), step);
        while (left.compareTo(to) * step < 0) {
            if (right.compareTo(to) * step > 0) {
                consumer.accept(left, to);
                break;
            } else {
                consumer.accept(left, right.clone().addMillis(-1));
            }
            left.add(dateField.getValue(), step);
            right.add(dateField.getValue(), step);
        }
    }

    public DateBetween between(Object to) {
        return new DateBetween(this.getCalendar(), DateTimes.of(to).getCalendar());
    }

    public String toString() {
        return formatDateTime();
    }


    @Override
    public int hashCode() {
        return getCalendar().hashCode();
    }

    @Override
    public int compareTo(DateTimes o) {
        if (o == null) {
            throw new ServiceException("dateTimes can not be null in compareTo");
        }
        return getCalendar().compareTo(o.getCalendar());
    }

}
