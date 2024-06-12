package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.utils.FormularUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Test {


    public static void main(String[] args) {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("a", new BigDecimal(10));
        long current = System.currentTimeMillis();
        BigDecimal res = null;
        for (int i = 0; i < 1000000; i++) {
            res = FormularUtil.eval("a+2*(3-4)/5", map);
        }
        System.out.println(res);
        System.out.println("cost " + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        Formulas formulas = Formulas.of("a+2*(3-4)/5");
        for (int i = 0; i < 1000000; i++) {
            res = formulas.cals((Map) map).getDecimal();
        }
        System.out.println(res);
        System.out.println("cost " + (System.currentTimeMillis() - current));

    }


}
