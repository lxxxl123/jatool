package com.chen.jatool.common.utils.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author chenwh3
 */
public class Numbers implements Comparable<Numbers>, Cloneable {

    @Getter
    private BigDecimal decimal;

    private Numbers(BigDecimal bigDecimal) {
        this.decimal = bigDecimal;
    }

    private Numbers() {
    }

    public static Numbers of(Object o) {
        if (o == null || o instanceof String && StrUtil.isBlank((String) o)) {
            throw new ServiceException("Decimals can not be blank , o = {}", o);
        }
        if (o instanceof Numbers) {
            return (Numbers) o;
        }
        BigDecimal decimal = null;
        Numbers numbers = new Numbers();
        BigDecimal div = BigDecimal.ONE;
        if (o instanceof Number) {
            decimal = NumberUtil.toBigDecimal((Number) o);
        } else if (o instanceof String) {
            String str = ((String) o).trim();
            int lastIdx = str.length() - 1;
            while (lastIdx >= 0){
                char last = str.charAt(lastIdx);
                if (last == '%') {
                    div = div.multiply(BigDecimal.valueOf(100));
                } else if (last == '‰') {
                    div = div.multiply(BigDecimal.valueOf(1000));
                } else {
                    break;
                }
                lastIdx--;
            }
            decimal = NumberUtil.toBigDecimal(str);
        } else {
            decimal = Convert.toBigDecimal(o);
        }
        if (!div.equals(BigDecimal.ONE)) {
            decimal = decimal.divide(div);
        }
        numbers.decimal = decimal;
        return numbers;
    }

    public static Numbers ofNull(Object o) {
        if (o == null || o instanceof String && StrUtil.isBlank((String) o)) {
            return new Numbers(BigDecimal.ZERO);
        }
        return of(o);
    }

    @Override
    protected Numbers clone() {
        return of(getDecimal());
    }

    public Numbers add(Object o) {
        decimal = getDecimal().add(of(o).getDecimal());
        return this;
    }

    public Numbers subtract(Object o) {
        decimal = getDecimal().subtract(of(o).getDecimal());
        return this;
    }

    public Numbers multiply(Object o) {
        decimal = getDecimal().multiply(of(o).getDecimal());
        return this;
    }

    /**
     * 四舍五入 , 保留scale位小数
     */
    public Numbers multiply(Object o, int scale) {
        decimal = getDecimal().multiply(of(o).getDecimal()).setScale(scale, RoundingMode.HALF_UP);
        return this;
    }

    public Numbers divide(Object o) {
        decimal = getDecimal().divide(of(o).getDecimal());
        return this;
    }

    /**
     * @param scale 四舍五入 , 保留scale位小数
     */
    public Numbers divide(Object o, int scale) {
        decimal = getDecimal().divide(of(o).getDecimal(), scale, RoundingMode.HALF_UP);
        return this;
    }

    @Override
    public String toString() {
        return decimal.toString();
    }

    @Override
    public int compareTo(Numbers o) {
        return getDecimal().compareTo(o.getDecimal());
    }

    public boolean gt(Object o) {
        return compareTo(of(o)) > 0;
    }

    public boolean lt(Object o) {
        return compareTo(of(o)) < 0;
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

    public Double dbVal() {
        return decimal.doubleValue();
    }

    public Integer intVal() {
        return decimal.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        return getDecimal().equals(of(obj));
    }

    @Override
    public int hashCode() {
        return getDecimal().hashCode();
    }

    /**
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。<br>
     *                <ul>
     *                <li>0 =》 取全部整数</li>
     *                <li>0.00 =》 取全部整数和两位小数，空则补0</li>
     *                <li>00.000 =》 取两位整数和三位小数，空则补0</li>
     *                <li># =》 取所有整数部分</li>
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

    public static void main(String[] args) {
    }
}
