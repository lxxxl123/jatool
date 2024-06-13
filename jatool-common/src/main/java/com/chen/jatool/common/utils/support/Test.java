package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.utils.FormulaUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The trainee across from me always look at me sharply when I walk pass ，
 */
public class Test {


    public static void main(String[] args) {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("a", new BigDecimal(100));
        long current = System.currentTimeMillis();
        BigDecimal res = null;
        for (int i = 0; i < 1000000; i++) {
            res = FormulaUtil.eval("a+2*(3-4)/5", map);
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
