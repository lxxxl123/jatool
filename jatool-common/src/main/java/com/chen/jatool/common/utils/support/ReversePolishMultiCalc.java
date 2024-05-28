package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.utils.ObjectUtil;

import java.util.*;
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
    public static List<String> doMatch(String s) {
        if (ObjectUtil.isBlank(s)) {
            throw new IllegalArgumentException(s);
        }

        Stack<String> stack = new Stack<>();
        List<String> data = new ArrayList<>();

        s = replaceAllBlank(s);

        String each;
        int start = 0;

        for (int i = 0; i < s.length(); i++) {
            String ele = s.charAt(i) + "";
            if (isSymbol(ele)) {
                each = s.charAt(i) + "";
                //栈为空，(操作符，或者 操作符优先级大于栈顶优先级 && 操作符优先级不是( )的优先级 及是 ) 不能直接入栈
                if (stack.isEmpty() || LEFT.equals(each)
                        || ((calcLevel(each) > calcLevel(stack.peek())) && calcLevel(each) < LEVEL_HIGH)) {
                    stack.push(each);
                } else if (!stack.isEmpty() && calcLevel(each) <= calcLevel(stack.peek())) {
                    //栈非空，操作符优先级小于等于栈顶优先级时出栈入列，直到栈为空，或者遇到了(，最后操作符入栈
                    while (!stack.isEmpty() && calcLevel(each) <= calcLevel(stack.peek())) {
                        if (calcLevel(stack.peek()) == LEVEL_HIGH) {
                            break;
                        }
                        data.add(stack.pop());
                    }
                    stack.push(each);
                } else if (RIGHT.equals(each)) {
                    // ) 操作符，依次出栈入列直到空栈或者遇到了第一个)操作符，此时)出栈
                    while (!stack.isEmpty()) {
                        String pop = stack.pop();
                        if (Objects.equals(pop, LEFT)) {
                            break;
                        }
                        data.add(pop);
                    }
                }
                start = i;    //前一个运算符的位置
            } else if (i == s.length() - 1 || isSymbol(s.charAt(i + 1) + "")) {
                each = start == 0 ? s.substring(start, i + 1) : s.substring(start + 1, i + 1);
                if (isNumber(each)) {
                    data.add(each);
                    continue;
                }
                throw new RuntimeException("data not match number");
            }
        }
        //如果栈里还有元素，此时元素需要依次出栈入列，可以想象栈里剩下栈顶为/，栈底为+，应该依次出栈入列，可以直接翻转整个stack 添加到队列
        Collections.reverse(stack);
        data.addAll(new ArrayList<>(stack));

        System.out.println(data);
        return data;
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

    public static void main(String[] args) {
        //String math = "9+(3-1)*3+10/2";
        String math = "12.8 + (2 - 3)*4+10/5.0";
        ReversePolishMultiCalc calc = new ReversePolishMultiCalc();
        System.out.println(new ReversePolishMultiCalc().doCalc(doMatch(math)));
    }
}
