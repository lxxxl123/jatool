package com.chen.jatool.common.utils;



import com.chen.jatool.common.utils.support.FormulaCalculator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 公式计算工具
 *
 * @author chenwh3
 */
public class FormulaUtil {

    public static BigDecimal eval(final String str, Map<String, BigDecimal> map) {
        return FormulaCalculator.of(str).parse(map);
    }

    public static void main(String[] args) {
        //        System.out.println(eval("1 > 3 && 1 || 0 && 1? 2 : max(3,1>5 && 5 || 3?4:3*5+1,10)", null));
//        System.out.println(eval("1 ? 0 ? 1:2 : 3 ? 4: 5", null));
//        System.out.println(true ? false ? 1 : 2 : true ? 4 : 5);
//        System.out.println(eval("1!=1 ? 10%3 : 10%4", null));
//        System.out.println(eval("1<n<=3<4?3:5", new HashMap<String, BigDecimal>() {{ put("n", new BigDecimal(2)); }}));
        HashMap<String, BigDecimal> paramMap = new HashMap<>();
        long start = System.currentTimeMillis();
        for (int i = 1; i < 1_000; i++) {
            paramMap.put("n", new BigDecimal(i));
//            BigDecimal res = FormulaCalculator.of("1<n<=3?100*n+200:n>4?200*n+10:0").parse(paramMap);
//            System.out.println("n=" + i + ", res=" + res);
        }
        System.out.println("cost=" + (System.currentTimeMillis() - start));
        paramMap.put("n", BigDecimal.valueOf(1));
        System.out.println(eval("n>1?(n+1)/(n-1):n==1?12:0", paramMap));

    }
}
