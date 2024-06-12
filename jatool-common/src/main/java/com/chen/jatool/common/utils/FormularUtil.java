package com.chen.jatool.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author chenwh3
 */
public class FormularUtil {

    public static BigDecimal eval(final String str, Map<String, BigDecimal> map) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
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
                    if (eat('+')) x = x.add(parseTerm()); // addition
                    else if (eat('-')) x = x.subtract(parseTerm()); // subtraction
                    else return x;
                }
            }

            BigDecimal parseTerm() {
                BigDecimal x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x = x.multiply(parseFactor()); // multiplication
                    else if (eat('/')) x = x.divide(parseFactor(), 4, RoundingMode.HALF_UP); // division
                    else return x;
                }
            }

            /**
             * 获取任意连续字符或数字
             */
            BigDecimal parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return parseFactor().negate(); // unary minus

                BigDecimal x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = new BigDecimal(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else if ((x = map.get(func)) == null) {
                        x = parseFactor();
                        if (func.equals("sqrt")) x = BigDecimal.valueOf(Math.sqrt(x.doubleValue()));
                        else if (func.equals("sin")) x = BigDecimal.valueOf(Math.sin(Math.toRadians(x.doubleValue())));
                        else if (func.equals("cos")) x = BigDecimal.valueOf(Math.cos(Math.toRadians(x.doubleValue())));
                        else if (func.equals("tan")) x = BigDecimal.valueOf(Math.tan(Math.toRadians(x.doubleValue())));
                        else {
                            throw new RuntimeException("Unknown function: " + func);
                        }
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                    x = BigDecimal.valueOf(Math.pow(x.doubleValue(), parseFactor().doubleValue())); // exponentiation

                return x;
            }
        }.parse();
    }
}
