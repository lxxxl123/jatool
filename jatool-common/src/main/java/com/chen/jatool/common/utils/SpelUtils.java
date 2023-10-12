package com.chen.jatool.common.utils;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;

import java.util.HashMap;

/**
 * @author chenwh3
 */
public class SpelUtils {

    private static ExpressionParser parser = new SpelExpressionParser();

    public static void parse(String expression, Object context) {
        parse(expression, context, Object.class);
    }

    public static <T> T parse(String expression, Object context, Class<T> clazz) {
        TemplateParserContext templateParserContext = new TemplateParserContext("#{", "}");

        Expression exp = parser.parseExpression(expression,templateParserContext);


        StandardEvaluationContext ctx = new StandardEvaluationContext();

        // 设置表达式支持 #{key} 读取map , 否则需要 #{['key']}\
        ctx.addPropertyAccessor(new MapAccessor());

        ctx.setRootObject(context);
        StandardTypeLocator standardTypeLocator = new StandardTypeLocator();
        standardTypeLocator.registerImport("cn.hutool.core.util");
        standardTypeLocator.registerImport("com.chen.utils");
        ctx.setTypeLocator(standardTypeLocator);

        return exp.getValue(ctx, clazz);
    }

    public static Boolean parseBool(String expression, Object context) {
        return parse(expression, context, Boolean.class);
    }

    public static String parseStr(String expression, Object context) {
        return parse(expression, context, String.class);
    }


    public static void main(String[] args) {
        HashMap<String, Object> map = new HashMap<>(2);
        HashMap<Object, Object> map1 = new HashMap<>(2);
        map1.put("b", null);
        map.put("a", "abc");
        map.put("map", map1);
        System.out.println(SpelUtils.parseStr("#{['a']}", map));
        System.out.println(SpelUtils.parseStr("#{'a'} 123", map));
        System.out.println(SpelUtils.parseStr("#{T(StrUtil).upperFirst(a)}", map));
        System.out.println(SpelUtils.parseStr("#{map?.get('b')?.get('c')}", map));

//        SapUdRes sapUdRes = new SapUdRes();
//        sapUdRes.setDate("2022-09-01");
//        sapUdRes.setTime("19:22:35");
//        System.out.println(SpelUtils.parse("#{udDate = T(DateUtil).combineSapDatetime(date,time)}",sapUdRes, Object.class));
//        System.out.println(sapUdRes);
    }
}

