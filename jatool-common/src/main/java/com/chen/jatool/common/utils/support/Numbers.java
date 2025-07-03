package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.function.Function;

/**
 * @author chenwh3
 */
@Slf4j
public class Numbers extends Number implements Comparable<Numbers>, Cloneable {

    private static final BigDecimal NUM_100 = BigDecimal.valueOf(100);
    private static final BigDecimal NUM_1000 = BigDecimal.valueOf(1000);

    @Getter
    private BigDecimal decimal;

    public static Numbers sum(List<?> nums) {
        if (CollUtil.isEmpty(nums)) {
            return null;
        }
        Numbers res = ofZero();
        for (Object num : nums) {
            if (ObjectUtil.isBlank(num)) {
                continue;
            }
            res.add(num);
        }
        return res;
    }

    public static Numbers avg(List<?> nums, int scale) {
        if (CollUtil.isEmpty(nums)) {
            return null;
        }
        return sum(nums).divide(nums.size(), scale);
    }

    public static Numbers stdev(List<?> nums){
        return stdev(nums, 16);
    }

    /**
     * 计算标准偏差（又称为-样品标准差）
     * 最大精度应该为15位左右 , 再大可能精度丢失 ， 由sqrt决定 ， 除非自行实现sqrt
     * @param scale 计算过程的精度，并非结果精度
     */
    public static Numbers stdev(List<?> nums, int scale) {
        if (CollUtil.isEmpty(nums)) {
            return null;
        }
        if (nums.size() == 1) {
            return Numbers.ofZero();
        }
        Numbers avg = avg(nums, scale);
        Numbers res = ofZero();
        for (Object num : nums) {
            res.add(Numbers.of(num).subtract(avg).pow(2));
        }
        return res.divide(nums.size() - 1, scale).sqrt();
    }

    public static BigDecimal parseDecimal(Object obj){
        if (obj instanceof Numbers) {
            return ((Numbers) obj).getDecimal();
        } else if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        return Numbers.of(obj).getDecimal();
    }

    private Numbers(BigDecimal bigDecimal) {
        this.decimal = bigDecimal;
    }

    private Numbers() {
    }


    public static Numbers ofZero(){
        return of(BigDecimal.ZERO);
    }

    public static Numbers ofOne() {
        return of(BigDecimal.ONE);
    }

    public static Numbers ofNull(Object o) {
        return ofNull(o, BigDecimal.ZERO);
    }

    /**
     * 使用ofEx代替
     */
    @Deprecated
    public static Numbers ofNull(Object o, Object orElse) {
        if (ObjectUtil.isBlank(o)) {
            if (ObjectUtil.isBlank(orElse)) {
                return null;
            }
            return of(orElse);
        }
        return of(o);
    }

    public static Numbers ofEx(Object o) {
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

    public static Numbers ofEx(Object o, Object orElse) {
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
    public static <T> T ofThen(Object o , Function<Numbers, T> then) {
        return ofThen(o, then, null);
    }

    /**
     * 异常会返回orElse
     */
    public static <T> T ofThen(Object o , Function<Numbers, T> then, T orElse) {
        Numbers obj = Numbers.ofEx(o);
        if (obj != null) {
            return then.apply(obj);
        }
        return orElse;
    }



    /**
     * 支持科学计数法， 如：1e32
     * 支持二进制数字， 如：0b1010
     * 支持八进制数字， 如：0o123
     * 支持十六进制数字， 如：0x123
     * 支持百分号， 如：1.2%
     * 支持千分号， 如：1.2‰
     */
    public static Numbers of(Object o) {
        if (ObjectUtil.isBlank(o)) {
            throw new ServiceException("Decimals can not be blank , o = {}", o);
        }
        if (o instanceof Numbers) {
            return Numbers.of(((Numbers) o).getDecimal());
        }
        BigDecimal decimal;
        Numbers numbers = new Numbers();
        BigDecimal div = BigDecimal.ONE;
        try {
            if (o instanceof BigDecimal) {
                decimal = (BigDecimal) o;
            } else if (o instanceof Number) {
                decimal = NumberUtil.toBigDecimal((Number) o);
            } else if (o instanceof String) {
                String str = ((String) o).trim();
                int lastIdx = str.length() - 1;
                while (lastIdx >= 1) {
                    char last = str.charAt(lastIdx);
                    if (last == '%') {
                        div = div.multiply(NUM_100);
                    } else if (last == '‰') {
                        div = div.multiply(NUM_1000);
                    } else {
                        break;
                    }
                    lastIdx--;
                }
                str = str.substring(0, lastIdx + 1);
                if (StrUtil.startWithAnyIgnoreCase(str, "0b")) {
                    decimal = new BigDecimal(Integer.parseInt(str.substring(2), 2));
                } else if (StrUtil.startWithAnyIgnoreCase(str, "0o")) {
                    decimal = new BigDecimal(Integer.parseInt(str.substring(2), 8));
                } else if (StrUtil.startWithAnyIgnoreCase(str, "0x")) {
                    decimal = new BigDecimal(Long.parseLong(str.substring(2), 16));
                } else {
                    decimal = new BigDecimal(str);
                }
            } else {
                throw new IllegalArgumentException("unknown type");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(StrUtil.format("parse decimal error , type = {} , value = {}", o.getClass(), o));
        }
        if (!div.equals(BigDecimal.ONE)) {
            decimal = decimal.divide(div);
        }
        numbers.decimal = decimal;
        return numbers;
    }



    @Override
    public Numbers clone() {
        return of(getDecimal());
    }

    public Numbers add(Object o) {
        decimal = getDecimal().add(parseDecimal(o));
        return this;
    }

    public Numbers subtract(Object o) {
        decimal = getDecimal().subtract(parseDecimal(o));
        return this;
    }

    public Numbers multiply(Object o) {
        decimal = getDecimal().multiply(parseDecimal(o));
        return this;
    }

    /**
     * 四舍五入 , 保留scale位小数
     */
    public Numbers multiply(Object o, int scale) {
        decimal = getDecimal().multiply(parseDecimal(o)).setScale(scale, RoundingMode.HALF_UP);
        return this;
    }

    public Numbers divide(Object o) {
        decimal = getDecimal().divide(parseDecimal(o));
        return this;
    }
    /**
     * @param scale 四舍五入 , 保留scale位小数
     */
    public Numbers divide(Object o, int scale) {
        decimal = getDecimal().divide(parseDecimal(o), scale, RoundingMode.HALF_UP);
        return this;
    }

    /**
     * 指数运算
     */
    public Numbers pow(int pow) {
        decimal = getDecimal().pow(pow);
        return this;
    }

    /**
     * 开根号
     */
    public Numbers sqrt() {
        decimal = BigDecimal.valueOf(Math.sqrt(dbVal()));
        return this;
    }


    @Override
    public String toString() {
        return decimal.toString();
    }

    /**
     * 去除所有无用的0和.
     */
    public String toPlainStr(){
        return decimal.stripTrailingZeros().toPlainString();
    }

    @Override
    public int compareTo(Numbers o) {
        return getDecimal().compareTo(o.getDecimal());
    }

    public boolean gt(Object o) {
        return compareTo(of(o)) > 0;
    }

    public boolean ge(Object o) {
        return compareTo(of(o)) >= 0;
    }

    public boolean lt(Object o) {
        return compareTo(of(o)) < 0;
    }

    public boolean le(Object o) {
        return compareTo(of(o)) <= 0;
    }

    public boolean ne(Object o) {
        return compareTo(of(o)) != 0;
    }

    public boolean eq(Object o) {
        return compareTo(of(o)) == 0;
    }

    /**
     * @param maxScale 保留最多maxScale位小数（不包括0，所有多余的0不显示） ,
     */
    public String formatPercent(int maxScale) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(maxScale);
        return percentFormat.format(decimal);
    }

    /**
     * 四舍五入 , 保留scale位小数
     */
    public Numbers round(int scale) {
        decimal = decimal.setScale(scale, RoundingMode.HALF_UP);
        return this;
    }

    public Numbers floor(int scale) {
        decimal = decimal.setScale(scale, RoundingMode.FLOOR);
        return this;
    }

    public Numbers ceiling(int scale) {
        decimal = decimal.setScale(scale, RoundingMode.CEILING);
        return this;
    }

    public int scale() {
        return decimal.scale();
    }

    public Numbers stripTrail0() {
        decimal = decimal.stripTrailingZeros();
        return this;
    }


    @Override
    public int intValue() {
        return getDecimal().intValue();
    }

    @Override
    public long longValue() {
        return getDecimal().longValue();
    }

    @Override
    public float floatValue() {
        return getDecimal().floatValue();
    }

    @Override
    public double doubleValue() {
        return getDecimal().doubleValue();
    }

    public Double dbVal() {
        return doubleValue();
    }

    public Integer intVal() {
        return intValue();
    }

    public Long longVal() {
        return longValue();
    }

    /**
     * 闭区间 [min,max]
     */
    public boolean between(Object min, Object max, boolean leftClose, boolean rightClose) {
        Numbers minNum = of(min);
        Numbers maxNum = of(max);
        return (leftClose ? compareTo(minNum) >= 0 : compareTo(minNum) > 0)
                && (rightClose ? compareTo(maxNum) <= 0 : compareTo(maxNum) < 0);
    }

    /**
     * 闭区间 [min,max]
     */
    public boolean between(Object min, Object max) {
        return between(min, max, true, true);
    }

    /**
     * 开区间 (min,max)
     */
    public boolean betweenOpen(Object min, Object max) {
        return between(min, max, false, false);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Numbers && ((Numbers) obj).getDecimal().equals(getDecimal());
    }

    @Override
    public int hashCode() {
        return getDecimal().hashCode();
    }

    /**
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 不会取整数开头的0和小数末尾的0 <br>
     *                <ul>
     *                <li>0 =》 取全部整数</li>
     *                <li>0.00 =》 取全部整数和两位小数，空则补0</li>
     *                <li>0.0 => 3.0000 -> 3.0 ; 3 -> 3.0</li>
     *                <li>00.000 =》 取两位整数和三位小数，空则补0</li>
     *                <li># =》 取所有整数部分</li>
     *                <li>#.# => 3.0000 -> 3</li>
     *                <li>#.##% =》 以百分比方式计数，并取两位小数</li>
     *                <li>#.#####E0 =》 显示为科学计数法，并取五位小数</li>
     *                <li>,### =》 每三位以逗号进行分隔，例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =》 将格式嵌入文本</li>
     *                <li>#‰ =》 乘以1000</li>
     *                <li>#% =》 乘以100</li>
     *                </ul>
     */
    public String format(String pattern) {
        return new DecimalFormat(pattern).format(decimal);
    }



}
