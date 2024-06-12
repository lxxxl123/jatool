package com.chen.jatool.common.utils.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLOutput;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中缀表达式转后缀表达式
 * 然后使用后缀表达式计算
 * @author chenwh3
 */
@Slf4j
public class Formulas {

    @Getter
    private List<String> suffixExpression;

    private Formulas() {
    }

    public static Formulas of(String infixExpression) {
        Formulas cal = new Formulas();
        cal.suffixExpression = parseSuffix(infixExpression);
        return cal;
    }

    /**
     * 输入参数进行计算
     */
    public Numbers cals(Map<String, Object> argsMap) {
        return doCalc(this.suffixExpression, (Map)argsMap);
    }

    /**
     * 匹配 + - * / ( ) 运算符 ，跳过负数
     */
    public static final Pattern PTN_SYMBOL = Pattern.compile("(?<!^|[-+*/(（])-|[+*/()（）]");
    public static final Pattern PTN_CALCULATE_SYMBOL = Pattern.compile("[-+*/]");

    public static final Pattern PTN_NEGATIVE = Pattern.compile("-(?=[(（])");

    static final String RIGHT = ")";

    static final String RIGHT_CH = "）";
    public static final String LEFT = "(";
    public static final String LEFT_CH = "（";


    private Integer divScale = 4;

    public Formulas setDivScale(Integer divScale) {
        this.divScale = divScale;
        return this;
    }
    /**
     * 加減 + -
     */
    static final int LEVEL_01 = 1;
    /**
     * 乘除 * /
     */
    static final int LEVEL_02 = 2;

    /**
     * 括号
     */
    static final int LEVEL_HIGH = Integer.MAX_VALUE;
    public static final String ADD = "+";
    public static final String MINUS = "-";
    public static final String MUL = "*";
    public static final String DIV = "/";

    public static String replaceAllBlank(String s) {
        return s.replaceAll("\\s+", "");
    }


    /**
     * 判断是不是运算符
     */
    public static boolean isSymbol(String s) {
        return PTN_SYMBOL.matcher(s).matches();
    }

    /**
     * 匹配运算等级
     */
    public static int calcLevel(String s) {
        if (ADD.equals(s) || MINUS.equals(s)) {
            return LEVEL_01;
        } else if (MUL.equals(s) || DIV.equals(s)) {
            return LEVEL_02;
        }
        return LEVEL_HIGH;
    }

    public static boolean checkIsRight(String obj) {
        return obj != null && (obj.endsWith(RIGHT) || obj.endsWith(RIGHT_CH));
    }

    public static boolean checkIsLeft(String obj) {
        return obj != null && (obj.startsWith(LEFT) || obj.startsWith(LEFT_CH));
    }

    /**
     * 中缀表达式转后缀表达式
     */
    public static List<String> parseSuffix(String s) {
        if (ObjectUtil.isBlank(s)) {
            throw new IllegalArgumentException(s);
        }

        Deque<String> symbolStack = new ArrayDeque<>();
        List<String> numStack = new ArrayList<>();

        s = replaceAllBlank(s);
        s = PTN_NEGATIVE.matcher(s).replaceAll("-1*");

        int pointer = 0;
        Matcher matcher = PTN_SYMBOL.matcher(s);
        while (matcher.find()) {
            String symbol = matcher.group();
            if (checkIsLeft(symbol)) {
                symbolStack.add(symbol);
            } else if (checkIsRight(symbol)) {
                numStack.add(s.substring(pointer, matcher.start()));
                String pop;
                while (!checkIsLeft(pop = symbolStack.pollLast())) {
                    if (pop == null) {
                        throw new ServiceException("bracket not match");
                    }
                    numStack.add(pop);
                }
            } else {
                int numEnd = matcher.start();
                if (numEnd == 0 && !symbol.equals(MINUS)) {
                    throw new ServiceException("symbol err , first symbol wrong");
                }
                if (pointer != numEnd) {
                    numStack.add(s.substring(pointer, numEnd));
                }
                int currentLevel = calcLevel(symbol);
                String last;
                if (symbolStack.isEmpty() || calcLevel(last = symbolStack.peekLast()) < currentLevel || checkIsLeft(last)) {
                    symbolStack.add(symbol);
                } else {
                    while (!symbolStack.isEmpty() && !checkIsLeft(last = symbolStack.peekLast()) && calcLevel(last) >= currentLevel) {
                        numStack.add(symbolStack.pollLast());
                    }
                    symbolStack.add(symbol);
                }
            }
            pointer = matcher.end();
        }
        if (pointer != s.length()) {
            numStack.add(s.substring(pointer));
        }
        while (!symbolStack.isEmpty()) {
            numStack.add(symbolStack.pollLast());
        }
        return numStack;
    }

    /**
     * 计算后缀表达式
     */
    public Numbers doCalc(List<String> list, Map<String, BigDecimal> symbolMap) {
        ArrayDeque<BigDecimal> numDeque = new ArrayDeque<>();
        for (String ele : list) {
            if (StrUtil.equalsAny(ele, ADD, MINUS, MUL, DIV)) {
                BigDecimal num1 = numDeque.pollLast();
                BigDecimal num2 = numDeque.pollLast();
                BigDecimal res = doTheMath(num2, num1, ele);
                numDeque.addLast(res);
            } else {
                boolean negative = ele.startsWith(MINUS);
                if (negative) {
                    ele = ele.substring(1);
                }
                char ch = ele.charAt(0);
                BigDecimal x;
                if (ch <= '9' && ch >= '0') {
                    x = new BigDecimal(ele);
                } else {
                    x = symbolMap.get(ele);
                }
                if (negative) {
                    x = x.negate();
                }
                numDeque.addLast(x);
            }
        }
        return Numbers.of(numDeque.pop());
    }

    /**
     * 运算
     */
    public BigDecimal doTheMath(BigDecimal s1, BigDecimal s2, String symbol) {
        try {
            switch (symbol) {
                case ADD:
                    return s1.add(s2);
                case MINUS:
                    return s1.subtract(s2);
                case MUL:
                    return s1.multiply(s2);
                case DIV:
                    return s1.divide(s2, divScale, RoundingMode.HALF_UP);
                default:
                    throw new IllegalArgumentException(symbol);
            }
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            throw new ServiceException("发现无法解析参数 [{} {} {}]", s1, symbol, s2);
        }
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        //String math = "9+(3-1)*3+10/2";
//        String math = "12.8 + (2 - 3)*4+10/5.0"; // 后缀表达式为[12.8, 2, 3, -, 4, *, +, 10, 5.0, /, +]
//        String math = "（氯化钠+无盐固形物-灰分*密度）*计量量/密度/100 ";
        String math = "1*-1";
        Formulas formulas = Formulas.of(math).setDivScale(6);
        System.out.println(formulas.getSuffixExpression());

        System.out.println(formulas.cals(new JSONObject()
                .set("氯化钠", 1)
                .set("无盐固形物", 2)
                .set("灰分", 3)
                .set("密度", 4)
                .set("计量量", 10)
        ));

    }
}

