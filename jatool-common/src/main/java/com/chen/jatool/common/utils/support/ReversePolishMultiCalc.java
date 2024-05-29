package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中缀表达式转后缀表达式
 * 后缀表达式计算
 */
public class ReversePolishMultiCalc {

    public static final Pattern PTN_NUM = Pattern.compile("[-+]?[.\\d]{1,}");
    /**
     * 匹配 + - * / ( ) 运算符
     */
    public static final Pattern PTN_SYMBOL = Pattern.compile("[-+*/()]");

    static final String RIGHT = ")";
    public static final String LEFT = "(";

    private Integer divScale = 4;
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
     * 判断是不是数字 int double long float
     */
    public static boolean isNumber(String s) {
        return PTN_NUM.matcher(s).matches();
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

    /**
     * 匹配
     */
    public static List<String> parseSuffix(String s) {
        if (ObjectUtil.isBlank(s)) {
            throw new IllegalArgumentException(s);
        }

        Stack<String> symbolStack = new Stack<>();
        List<String> numStack = new ArrayList<>();

        s = replaceAllBlank(s);

        int pointer = 0;
        Matcher matcher = PTN_SYMBOL.matcher(s);
        while (matcher.find()) {
            String symbol = matcher.group();
            if (LEFT.equals(symbol) ) {
                symbolStack.push(symbol);
            } else if (RIGHT.equals(symbol)) {
                String pop = "";
                numStack.add(s.substring(pointer, matcher.start()));

                while (!symbolStack.isEmpty()) {
                    pop = symbolStack.pop();
                    if (Objects.equals(pop, LEFT)) {
                        break;
                    }
                    numStack.add(pop);
                }
                if (!pop.equals(LEFT)) {
                    throw new ServiceException("bracket not match");
                }

            } else {
                int numEnd = matcher.start();
                if (numEnd == 0) {
                    throw new ServiceException("symbol err , first symbol wrong");
                }
                if (pointer != numEnd) {
                    numStack.add(s.substring(pointer, numEnd));
                }
                int currentLevel = calcLevel(symbol);
                if (symbolStack.isEmpty() || calcLevel(symbolStack.peek()) < currentLevel || calcLevel(symbolStack.peek()) == LEVEL_HIGH) {
                    symbolStack.add(symbol);
                } else {
                    while (!symbolStack.isEmpty() && calcLevel(symbolStack.peek()) >= currentLevel) {
                        numStack.add(symbolStack.pop());
                    }
                    symbolStack.add(symbol);
                }
            }
            pointer = matcher.end();
        }
        numStack.add(s.substring(pointer));
        while (!symbolStack.isEmpty()) {
            numStack.add(symbolStack.pop());
        }

        return numStack;
    }

    /**
     * 算出结果
     */
    public Numbers doCalc(List<String> list) {
        ArrayDeque<Object> numDeque = new ArrayDeque<>();
        for (String ele : list) {
            if (isSymbol(ele)) {
                Object num1 = numDeque.pollLast();
                Object num2 = numDeque.pollLast();
                Numbers res = doTheMath(num2, num1, ele);
                numDeque.addLast(res);
            } else {
                numDeque.addLast(ele);
            }
        }
        return Numbers.of(numDeque.pop());
    }

    /**
     * 运算
     */
    public Numbers doTheMath(Object s1, Object s2, String symbol) {
        switch (symbol) {
            case ADD:
                return Numbers.of(s1).add(s2);
            case MINUS:
                return Numbers.of(s1).subtract(s2);
            case MUL:
                return Numbers.of(s1).multiply(s2);
            case DIV:
                return Numbers.of(s1).divide(s2, divScale);
            default:
                throw new IllegalArgumentException(symbol);
        }
    }

    /**
     * [12.8, 2, 3, -, 4, *, +, 10, 5.0, /, +]
     */
    public static void main(String[] args) {
        //String math = "9+(3-1)*3+10/2";
        String math = "12.8 + (2 - 3)*4+10/5.0";
        ReversePolishMultiCalc calc = new ReversePolishMultiCalc();
        System.out.println(calc.parseSuffix(math));

    }
}
