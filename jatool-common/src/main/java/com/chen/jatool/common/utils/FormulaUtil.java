package com.chen.jatool.common.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenwh3
 */
public class FormulaUtil {

    public static final char PLUS = '+';
    public static final char SUB = '-';
    public static final char SPACE = ' ';
    public static final char MULIT = '*';
    public static final char DIV = '/';
    public static final char A = 'a';
    public static final char Z = 'z';
    public static final char L_BRACKET = '(';
    public static final String SQRT = "sqrt";
    public static final char R_BRACKET = ')';
    public static final char CHAR_0 = '0';
    public static final char CHAR_9 = '9';
    public static final String SIN = "sin";
    public static final String COS = "cos";
    public static final String TAN = "tan";
    public static final char DOT = '.';
    public static final String MIN = "min";
    public static final String MAX = "max";


    private static boolean isNum(char ch) {
        return (ch >= CHAR_0 && ch <= CHAR_9) || ch == DOT;
    }

    private static boolean isChar(char ch) {
        return ch >= A && ch <= Z || StringUtil.isChinese(ch);
    }


    public static BigDecimal eval(final String str, Map<String, BigDecimal> map) {
        return new Object() {
            int pos = -1;
            int ch;

            final MathContext mc = new MathContext(6, RoundingMode.HALF_UP);

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == SPACE) nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            BigDecimal parse() {
                nextChar();
                BigDecimal x = parseFirst();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }


            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor
            // 后续考虑用责任链重构 ， 每个parse做成1个类
            private BigDecimal parseFirst() {
                return parseTernary();
            }

            /**
             * 三目运算符
             */
            BigDecimal parseTernary() {
                BigDecimal x = parseLogic();
                for (; ; ) {
                    if (eat('?')) {
                        BigDecimal y = parseLogic();
                        if (!eat(':')) throw new RuntimeException("Missing ':'");
                        BigDecimal z = parseLogic();
                        return x.compareTo(BigDecimal.ZERO) > 0 ? y : z;
                    } else {
                        return x;
                    }
                }

            }

            /**
             * 逻辑运算符号： && ||
             */
            BigDecimal parseLogic() {
                BigDecimal x = parseRelational();
                boolean res;
                for (; ; ) {
                    if (eat('&') && eat('&')) {
                        BigDecimal y = parseRelational();
                        res = x.compareTo(BigDecimal.ZERO) > 0 && y.compareTo(BigDecimal.ZERO) > 0;
                        break;
                    } else if (eat('|') && eat('|')) {
                        BigDecimal y = parseRelational();
                        res = x.compareTo(BigDecimal.ZERO) > 0 || y.compareTo(BigDecimal.ZERO) > 0;
                        break;
                    } else {
                        return x;
                    }
                }
                return res ? BigDecimal.ONE : BigDecimal.ZERO;

            }


            /**
             * 解析关系运算符
             * 因关系运算符连续运算 ，因此无需使用循环 如： 1<=3<=5 不合法
             */
            BigDecimal parseRelational() {
                BigDecimal x = parseExpression();
                boolean res;
                if (eat('>')) {
                    if (eat('=')) {
                        res = x.compareTo(parseExpression()) >= 0;
                    } else {
                        res = x.compareTo(parseExpression()) > 0;
                    }
                } else if (eat('<')) {
                    if (eat('=')) {
                        res = x.compareTo(parseExpression()) <= 0;
                    } else {
                        res = x.compareTo(parseExpression()) < 0;
                    }
                } else if (eat('=') && eat('=')) {
                    res = x.compareTo(parseExpression()) == 0;
                } else {
                    return x;
                }
                return res ? BigDecimal.ONE : BigDecimal.ZERO;
            }

            BigDecimal parseExpression() {
                BigDecimal x = parseTerm();
                for (; ; ) {
                    if (eat(PLUS)) x = x.add(parseTerm()); // addition
                    else if (eat(SUB)) x = x.subtract(parseTerm()); // subtraction
                    else return x;
                }
            }

            BigDecimal parseTerm() {
                BigDecimal x = parseFactor();
                for (; ; ) {
                    if (eat(MULIT)) x = x.multiply(parseFactor()); // multiplication
                    else if (eat(DIV)) x = x.divide(parseFactor(), mc); // division
                    else return x;
                }
            }

            List<BigDecimal> parseArray() {
                List<BigDecimal> res = new ArrayList<>();
                BigDecimal x = parseFirst();
                res.add(x);
                while (eat(',')) {
                    res.add(parseFirst());
                }
                return res;
            }

            BigDecimal parseVar(String str) {
                return map != null ? map.get(str) : null;
            }


            /**
             * 获取任意连续字符或数字
             */
            BigDecimal parseFactor() {
                if (eat(PLUS)) return parseFactor(); // unary plus
                if (eat(SUB)) return parseFactor().negate(); // unary minus
                if (eat('!')) return parseFactor().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ZERO : BigDecimal.ONE;

                BigDecimal x;
                int startPos = this.pos;
                if (eat(L_BRACKET)) { // parentheses
                    x = parseFirst();
                    if (!eat(R_BRACKET)) throw new RuntimeException("Missing ')'");
                } else if (isNum((char) ch)) { // numbers
                    while (isNum((char) ch)) nextChar();
                    x = new BigDecimal(str.substring(startPos, this.pos));
                } else if (isChar((char) ch)) { // functions
                    while (isChar((char) ch)) nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat(L_BRACKET)) {
                        if (MAX.equals(func)) {
                            x = parseArray().stream().max(BigDecimal::compareTo).get();
                        } else if (MIN.equals(func)) {
                            x = parseArray().stream().min(BigDecimal::compareTo).get();
                        } else {
                            x = parseFirst();
                        }
                        if (!eat(R_BRACKET)) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else if ((x = parseVar(func)) == null) {
                        x = parseFactor();
                        if (func.equals(SQRT)) x = BigDecimal.valueOf(Math.sqrt(x.doubleValue()));
                        else if (func.equals(SIN)) x = BigDecimal.valueOf(Math.sin(Math.toRadians(x.doubleValue())));
                        else if (func.equals(COS)) x = BigDecimal.valueOf(Math.cos(Math.toRadians(x.doubleValue())));
                        else if (func.equals(TAN)) x = BigDecimal.valueOf(Math.tan(Math.toRadians(x.doubleValue())));
                        else {
                            throw new RuntimeException("Unknown function: " + func);
                        }
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) {
                    x = BigDecimal.valueOf(Math.pow(x.doubleValue(), parseFactor().doubleValue())); // exponentiation
                }

                for (; ; ) {
                    if (eat('%')) x = x.divide(new BigDecimal(100), mc);
                    else break;
                }

                return x;
            }

        }.parse();
    }
}
