package com.chen.jatool.common.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author chenwh3
 */
public class FormularsUtil {

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

    public static BigDecimal eval(final String str, Map<String, BigDecimal> map) {
        return new Object() {
            int pos = -1;
            int ch;

            MathContext mc = new MathContext(6, RoundingMode.HALF_UP);

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
                BigDecimal x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

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

            /**
             * 获取任意连续字符或数字
             */
            BigDecimal parseFactor() {
                if (eat(PLUS)) return parseFactor(); // unary plus
                if (eat(SUB)) return parseFactor().negate(); // unary minus

                BigDecimal x;
                int startPos = this.pos;
                if (eat(L_BRACKET)) { // parentheses
                    x = parseExpression();
                    if (!eat(R_BRACKET)) throw new RuntimeException("Missing ')'");
                } else if ((ch >= CHAR_0 && ch <= CHAR_9) || ch == DOT) { // numbers
                    while ((ch >= CHAR_0 && ch <= CHAR_9) || ch == DOT) nextChar();
                    x = new BigDecimal(str.substring(startPos, this.pos));
                } else if (ch >= A && ch <= Z) { // functions
                    while (ch >= A && ch <= Z) nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat(L_BRACKET)) {
                        x = parseExpression();
                        if (!eat(R_BRACKET)) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else if ((x = map.get(func)) == null) {
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

                if (eat('^')){
                    x = BigDecimal.valueOf(Math.pow(x.doubleValue(), parseFactor().doubleValue())); // exponentiation
                }

                for (;;) {
                    if (eat('%')) x = x.divide(new BigDecimal(100), mc);
                    else break;
                }

                return x;
            }
        }.parse();
    }
}
