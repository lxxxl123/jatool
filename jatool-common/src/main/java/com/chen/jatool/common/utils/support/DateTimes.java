package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.*;
import cn.hutool.core.date.format.FastDateFormat;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.text.ParseException;
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

/**
 * @author chenwh3
 */
@Slf4j
public class DateTimes implements Comparable<DateTimes>, Cloneable, Serializable {

    public static final FastDateFormat DOT_DATE_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd");

    public static final FastDateFormat SLASH_DATE_FORMAT = FastDateFormat.getInstance("yyyy/MM/dd");
    public static final FastDateFormat DOT_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd HH:mm:ss");

    public static final FastDateFormat CHINESE_WEEK_FORMAT = FastDateFormat.getInstance("EEE");

    /**
     * note! calendar is a mutable obj
     */
    private Calendar calendar;

    public static DateTimes now() {
        return of(Calendar.getInstance());
    }

    public static DateTimes ofFormat(String o, String format) {
        DateTime parse = DateUtil.parse(o, format);
        return ofDate(parse);
    }

    private static Calendar mom;

    /**
     * 速度更快，但calendar值不可控
     */
    public static Calendar buildRandomCalendar() {
        if (mom != null) {
            return (Calendar) mom.clone();
        } else {
            mom = Calendar.getInstance();
            return mom;
        }
    }

    public static boolean isSupportVal(Object obj) {
        if (obj == null) {
            return false;
        }
        return isSupportClass(obj.getClass());
    }


    /**
     * 可互相转换的类型
     */
    public static boolean isSupportClass(Class<?> clazz) {
        return clazz.isAssignableFrom(Date.class)
                || clazz.isAssignableFrom(TemporalAccessor.class)
                || clazz.isAssignableFrom(Calendar.class)
                || clazz.isAssignableFrom(DateTimes.class);
    }

    public static DateTimes ofCalendar(Calendar o) {
        DateTimes times = new DateTimes();
        times.calendar = (Calendar) o.clone();
        return times;
    }

    public static DateTimes ofDate(Date date) {
        return ofCalendar(buildCalendar(date));
    }

    /**
     * epochMillis
     */
    public static DateTimes ofMillis(long millis){
        return ofCalendar(buildCalendar(millis));
    }

    public static Calendar buildCalendar(Date date) {
        Calendar res = buildRandomCalendar();
        res.setTime(date);
        return res;
    }

    public static Calendar buildCalendar(long mills) {
        Calendar res = buildRandomCalendar();
        res.setTimeInMillis(mills);
        return res;
    }

    public static DateTimes of(Object o) {
        if (o instanceof Calendar) {
            return ofCalendar((Calendar) ((Calendar) o).clone());
        } else if (o instanceof DateTimes) {
            return ofCalendar((Calendar) ((DateTimes) o).getCalendar().clone());
        } else if (o instanceof Number) {
            return ofMillis(((Number) o).longValue());
        } else if (o instanceof TemporalAccessor) {
            return ofMillis(TemporalAccessorUtil.toInstant((TemporalAccessor) o).toEpochMilli());
        } else if (o == null || "".equals(o)) {
            throw new ServiceException("DateTimes can not be blank");
        } else {
            return ofDate(parse(o));
        }
    }





    private DateTimes() {
    }

    private DateTimes(Calendar calendar) {
        this.calendar = calendar;
    }

    /**
     * 空则返回当前时间
     */
    public static DateTimes ofNull(Object o) {
        if (ObjectUtil.isBlank(o)) {
            return new DateTimes(Calendar.getInstance());
        }
        return of(o);
    }

    public static DateTimes ofNull(Object o, Object orElse) {
        if (ObjectUtil.isBlank(o)) {
            return ofNull(orElse);
        }
        return of(o);
    }

    public static DateTimes ofNull(Object o, Supplier<Object> orElse) {
        if (ObjectUtil.isBlank(o)) {
            return ofNull(orElse == null ? null : orElse.get());
        }
        return of(o);
    }

    /**
     * 错误或者空则返回null
     */
    public static DateTimes ofEx(Object o) {
        if (ObjectUtil.isBlank(o)) {
            return null;
        }
        try {
            return of(o);
        } catch (Exception e) {
            log.warn("{}", e.getLocalizedMessage());
            return null;
        }
    }
    /**
     * 错误或者空则返回orElse，orElse解析也出错则返回null
     */
    public static DateTimes ofEx(Object o, Object orElse) {
        if (ObjectUtil.isBlank(o)) {
            return ofEx(orElse);
        }
        try {
            return of(o);
        } catch (Exception e) {
            log.warn("{}", e.getLocalizedMessage());
            return ofEx(orElse);
        }
    }

    /**
     * 异常返回null
     */
    public static <T> T ofThen(Object o , Function<DateTimes, T> then) {
        return ofThen(o, then, null);
    }

    /**
     * 异常会返回orElse
     */
    public static <T> T ofThen(Object o , Function<DateTimes, T> then, T orElse) {
        DateTimes dateTimes = ofEx(o);
        if (dateTimes != null) {
            return then.apply(dateTimes);
        }
        return orElse;
    }

    /**
     * 分别对应年月日时分秒的长度
     */
    private static final int[] dateLenArray = new int[]{4, 2, 2, 2, 2, 2, 3};

    private static Calendar parseStr2Calendar(String str) {
        if (str == null) {
            throw new IllegalArgumentException("DateTimes can not be blank");
        }
        int[] list = new int[]{0, 1, 1, 0, 0, 0, 0};
        char[] charArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        int point = 0;
        if (str.length() == 8 && str.charAt(2) == ':' && str.charAt(5) == ':') {
            // 如果检测到只有时间，使用当天作为日期
            Calendar today = Calendar.getInstance();
            point = 3;
            list[0] = today.get(Calendar.YEAR);
            list[1] = today.get(Calendar.MONTH) + 1;
            list[2] = today.get(Calendar.DATE);
        }
        for (int i = 0; i < charArray.length; i++) {
            switch (charArray[i]) {
                case '年':
                case '月':
                case '日':
                case '时':
                case '分':
                case '秒':
                case '.':
                case '-':
                case '_':
                case '/':
                case ' ':
                case ':':
                case 'T':
                    if (sb.length() > 0) {
                        list[point] = Integer.parseInt(sb.toString());
                        point++;
                        sb.setLength(0);
                    }
                    break;
                default:
                    sb.append(charArray[i]);
                    if (dateLenArray[point] == sb.length() || i == charArray.length - 1) {
                        list[point] = Integer.parseInt(sb.toString());
                        point++;
                        sb.setLength(0);
                    }
            }
            if (point >= 7) {
                break;
            }
        }
        Calendar instance = buildRandomCalendar();
        instance.set(list[0], list[1] - 1, list[2], list[3], list[4], list[5]);
        instance.set(Calendar.MILLISECOND, list[6]);
        return instance;
    }



    public static Date parse(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("DateTimes can not be blank");
        }
        try {
            if (obj instanceof Date) {
                return (Date) obj;
            } else if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() < 24 && !str.isEmpty()) {
                    // 一般28位或以上是该格式EEE MMM dd HH:mm:ss zzz yyyy，不支持
                    try {
                        // 最大支持格式yyyy-MM-dd HH:mm:ss.SSS
                        return parseStr2Calendar((String) obj).getTime();
                    } catch (Exception e) {
                        log.warn("parseStr2Calendar error : {}", e.getLocalizedMessage());
                    }
                }
                return DateUtil.parse(str);
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

    /**
     * yyyy-MM
     */
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

    public String formatSimpleTime() {
        return DatePattern.PURE_TIME_FORMAT.format(getCalendar());
    }

    /**
     * yyyy-MM-dd HH:mm:ss.SSS
     */
    public String formatDateTimeMs() {
        return DatePattern.NORM_DATETIME_MS_FORMAT.format(getDate());
    }

    /**
     * sqlserver数据库会把毫秒.999四舍五入，.998则会视为997
     */
    public String formatDbTime(){
        return formatDateTimeMs().replace(".999", ".997");
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

    /**
     * 0 表示1月，11表示12月
     */
    public int getMonth() {
        return getField(DateField.MONTH);
    }

    /**
     * 1 表示1月，12表示12月
     */
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

    /**
     * 星期一、二...日
     */
    public String getChineseWeek(){
        return CHINESE_WEEK_FORMAT.format(getCalendar());
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
    public boolean lt(Object obj) {
        return before(DateTimes.of(obj));
    }
    public boolean gt(Object obj) {
        return after(DateTimes.of(obj));
    }
    public boolean le(Object obj) {
        return beforeOrEquals(DateTimes.of(obj));
    }
    public boolean ge(Object obj) {
        return afterOrEquals(DateTimes.of(obj));
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
     * 开区间：[from , to1) , [to1, to2) , [to2, to]
     */
    public void loopRange(Object toObj, DateField dateField, int step, BiConsumer<DateTimes, DateTimes> consumer) {
        loopRange(toObj, dateField, step, consumer, true);
    }

    /**
     * @param openRange 右侧是否开区间
     *                   true: [from , to1) , [to1, to2) , [to2, to]
     *                   false: [from, to1] , [to1, to2] , [to2, to]
     */
    public void loopRange(Object toObj, DateField dateField, int step, BiConsumer<DateTimes, DateTimes> consumer, boolean openRange) {
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
                if (openRange) {
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

    public boolean isBetween(Object left, Object right) {
        return DateTimes.of(left).compareTo(this) <= 0 && compareTo(DateTimes.of(right)) <= 0;
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
     * 假期记录，使用BitSet优化内存使用， bitSet(400)只占大约92byte，可表示一年所有假期
     * 一年大约有100多个假期（包含周末）
     * 若使用Set<LocalDate|long|int|String>记录日期，单个假期分别占据约24byte,8byte,4type,40byte
     */
    private static Map<Integer, BitSet> globalHolidayMap;

    private static ThreadLocal<Map<Integer, BitSet>> localHolidayMap = ThreadLocal.withInitial(() -> null);
    private Map<Integer, BitSet> holidayMap;

    private static Map<Integer, BitSet> buildHolidayMap(Collection<LocalDate> set) {
        Map<Integer, BitSet> map = new HashMap<>();
        for (LocalDate date : set) {
            int year = date.getYear();
            map.computeIfAbsent(year, k -> new BitSet(400))
                    .set(date.getDayOfYear());
        }
        return map;
    }
    public static void setGlobalHoliday(Collection<LocalDate> set) {
        globalHolidayMap = buildHolidayMap(set);
    }

    public static void setLocalHoliday(Collection<LocalDate> set) {
        localHolidayMap.set(buildHolidayMap(set));
    }

    public DateTimes setHoliday(Collection<LocalDate> set) {
        holidayMap = buildHolidayMap(set);
        return this;
    }

    public static void clearLocalVars() {
        localHolidayMap.remove();
    }


    private Map<Integer, BitSet> getHolidayMap(){
        if (holidayMap != null) {
            return holidayMap;
        }
        Map<Integer, BitSet> localDates = localHolidayMap.get();
        if (localDates != null) {
            // 避免重复查询线程hash表
            holidayMap = localDates;
            return holidayMap;
        }
        if (globalHolidayMap != null) {
            holidayMap = globalHolidayMap;
            return holidayMap;
        }
        return Collections.emptyMap();
    }

    /**
     * 空则只算周末
     */
    public boolean isHoliday() {
        Map<Integer, BitSet> map = getHolidayMap();
        BitSet bitSet;
        return map.isEmpty() ? getDayOfWeek() > 5 : ((bitSet = map.get(getYear())) != null && bitSet.get(getDayOfYear()));
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


    public static void main(String[] args) throws ParseException {
        long start = System.currentTimeMillis();
        //1000万耗时 7秒，性能尚可
        //100万耗时 1秒
        for (int i = 0; i < 10000000; i++) {
//            Calendar calendar1 = buildRandomCalendar();
//            DateUtil.parse("Thu May 16 17:57:18 GMT+08:00 2019");
//            Calendar calendar2 = buildCalendar(new Date());
//            Calendar calendar2 = CalendarUtil.calendar(new Date());
            DateTimes.of("2024年5月16日 12:23:59");
        }
        System.out.println(System.currentTimeMillis() - start);

        System.out.println( DateUtil.parse("Thu May 16 17:57:18 GMT+08:00 2019"));
        System.out.println( DateUtil.parse("Wed Aug 01 00:00:00 CST 2024"));
        System.out.println(DateTimes.of("2024年5月16日 12:23:59.123").formatDateTimeMs());
        System.out.println(DateTimes.of(" 2024-01-01"));
        System.out.println(DateTimes.of("23:22:10").formatDateTimeMs());
        System.out.println(DateTimes.of(" 2024").formatDateTimeMs());
        System.out.println(DateTimes.of(" 2024 03 05 23 5959").formatDateTimeMs());
        System.out.println(DateTimes.of("Wed Aug 01 00:00:00 CST 2012").formatDateTimeMs());
        System.out.println(DateTimes.now().addDays(2).getChineseWeek());
        DateTimes.setGlobalHoliday(ListUtil.of(DateTimes.of("2024-01-01").getLocalDate(), DateTimes.of("2024-06-01").getLocalDate()));
        System.out.println(DateTimes.of("2024-01-01").isHoliday());
        System.out.println(DateTimes.of("2024-01-02").isHoliday());
        System.out.println(DateTimes.of("2024-06-01").isHoliday());
        System.out.println(DateTimes.now().endOfDate().formatDbTime());
        System.out.println(ofThen(null, DateTimes::formatDateTimeMs, "123"));

    }


}
