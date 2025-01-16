package com.chen.jatool.common.utils.support;

import cn.hutool.core.date.*;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import org.apache.commons.lang3.time.DateUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenwh3
 */
public class DateTimes implements Comparable<DateTimes>, Cloneable {

    public static final FastDateFormat DOT_DATE_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd");

    public static final FastDateFormat SLASH_DATE_FORMAT = FastDateFormat.getInstance("yyyy/MM/dd");
    public static final FastDateFormat DOT_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd HH:mm:ss");

    /**
     * note! calendar is a mutable obj
     */
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
        } else if (o instanceof Number) {
            // build with millis from epoch
            dateTimes.calendar = CalendarUtil.calendar(((Number) o).longValue());
            return dateTimes;
        } else if (o instanceof TemporalAccessor) {
            dateTimes.calendar = CalendarUtil.calendar(TemporalAccessorUtil.toInstant((TemporalAccessor) o).toEpochMilli());
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

    public static DateTimes ofEx(Object o) {
        if (o == null || ObjectUtil.isBlank(o)) {
            return null;
        }
        try {
            return of(o);
        } catch (Exception e) {
            return null;
        }
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

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[.-/](\\d{1,2})[.-/](\\d{1,2})");

    public static Date parse(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("DateTimes can not be blank");
        }
        try {
            Matcher mat;
            if (obj instanceof Date) {
                return (Date) obj;
            } else if (obj instanceof String) {
                String dateStr = (String) obj;
                if (dateStr.length() == 6) {
                    return DatePattern.SIMPLE_MONTH_FORMAT.parse(dateStr);
                } else if (dateStr.length() == 7) {
                    return DatePattern.NORM_MONTH_FORMAT.parse(dateStr);
                } else if (dateStr.length() < 10 && dateStr.length() > 7 && (mat = DATE_PATTERN.matcher(dateStr)).matches()) {
                    dateStr = mat.group(1) + StrUtil.padPre(mat.group(2), 2, "0") + StrUtil.padPre(mat.group(3), 2, "0");
                    return DatePattern.PURE_DATE_FORMAT.parse(dateStr);
                }
                return DateUtil.parse(dateStr);
            } else if (obj instanceof TemporalAccessor) {
                return Date.from(TemporalAccessorUtil.toInstant((TemporalAccessor) obj));
            } else if (obj instanceof Number) {
                return new Date(((Number) obj).longValue());
            }
            throw new IllegalArgumentException("unknown type class = " + obj.getClass().getName());
        } catch (Exception e) {
            throw new ServiceException("incorrect date format. input parameter = [{}]", obj).causeBy(e);
        }
    }


    public DateTimes truncate(int field) {
        calendar = DateUtils.truncate(getCalendar(), field);
        return this;
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-01-01 00:00:00
     */
    public DateTimes truncateYear() {
        return truncate(Calendar.YEAR);
    }
    /**
     * 2023-04-05 12:34:56 -> 2023-04-01 00:00:00
     */
    public DateTimes truncateMonth() {
        return truncate(Calendar.MONTH);
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



    public String format(String pattern) {
        return FastDateFormat.getInstance(pattern).format(getCalendar());
    }

    /**
     * yyyy-MM-dd
     */
    public String formatDate() {
        return DatePattern.NORM_DATE_FORMAT.format(getCalendar());
    }
    public String formatChDate(){
        return DatePattern.CHINESE_DATE_FORMAT.format(getCalendar());
    }

    public String formatSlashDate() {
        return SLASH_DATE_FORMAT.format(getCalendar());
    }

    /**
     * yyyyMMdd
     */
    public String formatSimpleDate() {
        return DatePattern.PURE_DATE_FORMAT.format(getCalendar());
    }

    public String formatMonth() {
        return DatePattern.NORM_MONTH_FORMAT.format(getCalendar());
    }


    /**
     * yyyyMM
     */
    public String formatSimpleMonth() {
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
    public String formatDotDate() {
        return DOT_DATE_FORMAT.format(getCalendar());
    }

    /**
     * yyyy.MM.dd HH:mm:ss
     */
    public String formatDotDateTime() {
        return DOT_DATETIME_FORMAT.format(getCalendar());
    }

    /**
     * HH:mm:ss
     */
    public String formatTime() {
        return DatePattern.NORM_TIME_FORMAT.format(getCalendar());
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

    /**
     * 对于datetime类型数据库会把998ms以上数据进位
     */
    public DateTimes fixDatabaseMs(){
        if (getMillis() > 997) {
            return setMillis(997);
        }
        return this;
    }

    public DateTimes endOf(int field) {
        calendar = CalendarUtil.ceiling(getCalendar(), DateField.of(field));
        return this;
    }

    /**
     * 2023-04-05 12:34:56 -> 2023-12-31 23:59:59
     */
    public DateTimes endOfYear() {
        return endOf(Calendar.YEAR);
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

    public DateTimes endOfHour() {
        return endOf(Calendar.HOUR);
    }
    public DateTimes endOfMinute() {
        return endOf(Calendar.MINUTE);
    }
    public DateTimes endOfSecond() {
        return endOf(Calendar.SECOND);
    }

    public Date getDate() {
        return calendar.getTime();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public LocalDate getLocalDate() {
        return LocalDate.of(getYear(), getMonthLiteral(), getDayOfMonth());
    }

    public LocalTime getLocalTime() {
        return LocalTime.of(getHour(), getMinute(), getSecond(), getMillis() * 1_000_000);
    }

    public Instant getInstant() {
        return getCalendar().toInstant();
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(getLocalDate(), getLocalTime());
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

    public DateTimes addMonths(int amount) {
        return add(Calendar.MONTH, amount);
    }

    public DateTimes addDays(int amount) {
        return add(Calendar.DATE, amount);
    }
    public DateTimes addHours(int amount) {
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



    public int getField(DateField field) {
        return getCalendar().get(field.getValue());
    }

    public DateTimes setField(DateField field, int value) {
        getCalendar().set(field.getValue(), value);
        return this;
    }


    public DateTimes setYear(int value) {
        return setField(DateField.YEAR, value);
    }
    /**
     * 0 表示1月，11表示12月
     */
    public DateTimes setMonth(int value) {
        return setField(DateField.MONTH, value);
    }
    /**
     * 1 表示1月，12表示12月
     */
    public DateTimes setMonthLiteral(int value) {
        return setField(DateField.MONTH, value - 1);
    }
    public DateTimes setDayOfMonth(int value) {
        return setField(DateField.DAY_OF_MONTH, value);
    }
    public DateTimes setHour(int value) {
        return setField(DateField.HOUR_OF_DAY, value);
    }
    public DateTimes setMinute(int value) {
        return setField(DateField.MINUTE, value);
    }
    public DateTimes setSecond(int value) {
        return setField(DateField.SECOND, value);
    }
    public DateTimes setMillis(int value) {
        return setField(DateField.MILLISECOND, value);
    }

    public DateTimes setSecondOfDay(int value) {
        return setHour(0).setMinute(0).setSecond(value);
    }

    public DateTimes setLocalTime(LocalTime date) {
        return setHour(date.getHour())
                .setMinute(date.getMinute())
                .setSecond(date.getSecond())
                .setMillis(date.getNano() / 1_000_000);
    }

    public int getYear() {
        return getField(DateField.YEAR);
    }
    public int getMonth() {
        return getField(DateField.MONTH);
    }
    public int getMonthLiteral() {
        return getField(DateField.MONTH) + 1;
    }
    /**
     * 1 表示周一，7表示周日； Calendar中 1代表周日，2表示周一
     *
     */
    public int getDayOfWeek() {
        return (getField(DateField.DAY_OF_WEEK) + 5) % 7 + 1;
    }
    public int getDayOfMonth() {
        return getField(DateField.DAY_OF_MONTH);
    }

    public int getDayOfYear() {
        return getField(DateField.DAY_OF_YEAR);
    }
    public int getHour() {
        return getField(DateField.HOUR_OF_DAY);
    }
    public int getMinute(){
        return getField(DateField.MINUTE);
    }
    public int getSecond(){
        return getField(DateField.SECOND);
    }
    public int getMillis(){
        return getField(DateField.MILLISECOND);
    }
    public int getSecondOfDay(){
        return getHour() * 3600 + getMinute() * 60 + getSecond();
    }

    /**
     * epoch, get the milliseconds since 1970-01-01 00:00:00 UTC
     */
    public long getEpochMillis() {
        return getCalendar().getTimeInMillis();
    }

    public DateTimes clone() {
        return of(this);
    }

    @Override
    public boolean equals(Object dateTimes) {
        return dateTimes instanceof DateTimes && (((DateTimes) dateTimes).getCalendar()).equals(getCalendar());
    }

    public boolean eq(Object obj) {
        return ObjectUtil.equal(DateTimes.of(obj).getEpochMillis(), getEpochMillis());
    }

    public boolean after(DateTimes dateTimes) {
        return compareTo(dateTimes) > 0;
    }
    public boolean after(LocalTime time) {
        return getSecondOfDay() > time.toSecondOfDay();
    }

    public boolean afterOrEquals(DateTimes dateTimes) {
        return compareTo(dateTimes) >= 0;
    }

    public boolean before(DateTimes dateTimes) {
        return compareTo(dateTimes) < 0;
    }
    public boolean before(LocalTime time) {
        return getSecondOfDay() < time.toSecondOfDay();
    }
    public boolean beforeOrEquals(DateTimes dateTimes) {
        return compareTo(dateTimes) <= 0;
    }



    public void loop(Object toObj, DateField dateField, int step, Consumer<DateTimes> consumer) {
        DateTimes from = clone();
        DateTimes to = DateTimes.of(toObj);
        if (step == 0) {
            throw new ServiceException("step can not be zero");
        }
        for (DateTimes left = from; left.compareTo(to) * step <= 0; ) {
            consumer.accept(left.clone());
            left.add(dateField.getValue(), step);
        }

    }

    public <U> List<U> loopFor(Object toObj, DateField dateField, int step, Function<DateTimes, U> function) {
        List<U> list = new ArrayList<>();
        loop(toObj, dateField, step, e -> list.add(function.apply(e)));
        return list;
    }

    public List<Tuple2<DateTimes,DateTimes>> loopRangeList(Object toObj, DateField dateField, int step){
        List<Tuple2<DateTimes,DateTimes>> list = new ArrayList<>();
        loopRange(toObj, dateField, step, (left, right) -> list.add(Tuples.of(left, right)), true);
        return list;
    }

    /**
     * 闭区间：[from , to1) , [to1, to2) , [to2, to]
     */
    public void loopRange(Object toObj, DateField dateField, int step, BiConsumer<DateTimes, DateTimes> consumer) {
        loopRange(toObj, dateField, step, consumer, true);
    }

    /**
     * @param closeRange 是否闭区间
     *                   true: [from , to1) , [to1, to2) , [to2, to]
     *                   false: [from, to1] , [to1, to2] , [to2, to]
     */
    public void loopRange(Object toObj, DateField dateField, int step, BiConsumer<DateTimes, DateTimes> consumer, boolean closeRange) {
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
                if (closeRange) {
                    consumer.accept(left.clone(), right.clone().addMillis(-1));
                } else {
                    consumer.accept(left.clone(), right.clone());
                }
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

    /*
     * 假期记录
     */
    private static Set<LocalDate> globalHolidaySet;

    private static ThreadLocal<Set<LocalDate>> localHolidaySet = ThreadLocal.withInitial(() -> null);
    private Set<LocalDate> holidaySet;

    public static void setGlobalHoliday(Set<LocalDate> set) {
        globalHolidaySet = set;
    }
    public static void setLocalHoliday(Set<LocalDate> set) {
        localHolidaySet.set(set);
    }

    public static void clearLocalVars() {
        localHolidaySet.remove();
    }

    public DateTimes setHoliday(Set<LocalDate> set) {
        holidaySet = set;
        return this;
    }

    private Set<LocalDate> getHolidaySet(){
        if (holidaySet != null) {
            return holidaySet;
        }
        Set<LocalDate> localDates = localHolidaySet.get();
        if (localDates != null) {
            // 避免重复查询线程hash表
            holidaySet = localDates;
            return holidaySet;
        }
        if (globalHolidaySet != null) {
            holidaySet = globalHolidaySet;
            return holidaySet;
        }
        return Collections.emptySet();
    }

    /**
     * 空则只算周末
     */
    public boolean isHoliday() {
        Set<LocalDate> set = getHolidaySet();
        return set.isEmpty() ? getDayOfWeek() >= 6 : set.contains(getLocalDate());
    }

    public boolean isWorkDay() {
        return !isHoliday();
    }

    public DateTimes addWorkDay(int amount) {
        int one = amount > 0 ? 1 : -1;
        while (amount != 0) {
            addDays(one);
            if (isWorkDay()) {
                amount -= one;
            }
        }
        return this;
    }

    public DateTimes addWorkDay(double amount) {
        addWorkDay((int) amount);
        int one = amount > 0 ? 1 : -1;
        amount %= 1;
        if (amount != 0) {
            addSeconds((int) (amount * 24 * 60 * 60));
            while (isHoliday()) {
                addDays(one);
            }
        }
        return this;
    }



}
