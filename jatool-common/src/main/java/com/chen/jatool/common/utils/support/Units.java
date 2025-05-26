package com.chen.jatool.common.utils.support;

import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.interceptor.holder.StopWatchHolder;
import com.chen.jatool.common.utils.ObjectUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenwh3
 */
public class Units implements Cloneable {
    @Getter
    private String unit;
    @Getter
    private Numbers val;

    @Setter
    private int scale = 16;

    public static Units of(Object val, String unit) {
        if (!unitMap.containsKey(unit)) {
            throw new ServiceException("not support unit = {}", unit);
        }

        Units u = new Units();
        u.val = Numbers.ofNull(val, 1);
        u.unit = unit;
        return u;
    }
    public static Units of(Object obj) {
        if (ObjectUtil.isBlank(obj)) {
            throw new ServiceException("units can not be blank");
        }
        if (obj instanceof Units) {
            return ((Units) obj);
        }
        String fullVal = obj.toString().trim();
        // 避免使用正则，性能不高
        int i = 0;
        for (; i < fullVal.length(); ) {
            char c = fullVal.charAt(i);
            if (c <= '9' && c >= '0' || c == '.') {
                // do nothing
                i++;
            } else {
                break;
            }
        }
        if (i == fullVal.length()) {
            throw new ServiceException("unit can not be blank , val = {}", fullVal);
        }
        String unit = fullVal.substring(i);
        String val = fullVal.substring(0, i);

        return of(val, unit);
    }

    public boolean haveSameUnit(Object fullval) {
        return this.unit.equals(of(fullval).unit);
    }

    public Units getBaseUnit() {
        Units res = getBaseUnit(unit);
        res.val.multiply(val);
        return res;
    }

    public Units toUnit(String unit) {
        Units u1 = getBaseUnit(unit);
        Units u2 = getBaseUnit();
        if (!u1.unit.equalsIgnoreCase(u2.unit)) {
            throw new ServiceException("unit conversion failed!");
        }
        this.val = u2.getVal().divide(u1.getVal(), scale);
        this.unit = unit;
        return this;
    }


    private static final Map<String, Units> cache = new ConcurrentHashMap<>();
    private static final Map<String, String> unitMap = new CaseInsensitiveMap<>();

    public static void setGlobalUnitMap(Map<String, String> map) {
        if (map == null) {
            return;
        }
        cache.clear();
        unitMap.putAll(map);
    }

    public static Map<String, String> getGlobalUnitMap() {
        return unitMap;
    }


    public static Units getBaseUnit(String unit) {
        return cache.computeIfAbsent(unit, k -> {
            if (StrUtil.isBlank(k)) {
                throw new ServiceException("unit can not be blank");
            }
            String nextUnit = unitMap.get(k);
            Units next = Units.of(nextUnit);
            if (StrUtil.endWithAnyIgnoreCase(k, next.unit)) {
                return next;
            }
            Units res = getBaseUnit(next.unit);
            res.val.multiply(next.val);
            return res;
        }).clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Units other = (Units) obj;
        return unit.equals(other.unit) && val.equals(other.val);

    }

    @Override
    public Units clone() {
        Units units = new Units();
        units.unit = unit;
        units.val = val.clone();
        return units;
    }

    @Override
    public String toString() {
        return val.toPlainStr() + unit;
    }

    // 测试方法
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("kg", "1000g");
        map.put("G", "1G");
        map.put("mg", "0.001g");
        map.put("t", "1000kg");
        setGlobalUnitMap(map);

        StopWatchHolder.start("start");

//        for (int i = 0; i < 1_000_000_0; i++) {
//            Units t = of("0.1mg").toUnit("t");
//            System.out.println(t);
//        }
        System.out.println(of("100g").toUnit("kg"));
        System.out.println(StopWatchHolder.clearAndSimplePrint());
    }


}
