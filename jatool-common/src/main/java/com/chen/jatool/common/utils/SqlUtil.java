package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.chen.jatool.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SqlUtil {

    private static final Pattern DETECT_SQL_INJECTION_REGEX = Pattern.compile("(?i);|--|/\\*|\\*/|(\\b(TRUNCATE|ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT( +INTO)?|MERGE|SELECT|UPDATE|UNION( +ALL)?)\\b)");

    public static void checkSql(String val){
        if (DETECT_SQL_INJECTION_REGEX.matcher(val).find()) {
            log.error("illegal input , sql = {}", val);
            throw new ServiceException("illegal input");
        }
    }

    public static void tryBetween(QueryWrapper<?> queryWrapper, String colName, Object v1, Object v2) {
        if (ObjectUtil.isBlank(v1)) {
            v1 = v2;
            v2 = null;
        }

        if (ObjectUtil.isNotBlank(v1)) {
            if (ObjectUtil.isNotBlank(v2)) {
                queryWrapper.between(colName, v1, v2);
            } else {
                queryWrapper.eq(colName, v1);
            }
        }
    }


    public static void tryBetween2Col(QueryWrapper<?> queryWrapper, String leftCol, String rightCol, Object leftVal, Object rightVal) {
        if (leftVal == null || rightVal == null || "".equals(leftVal) || "".equals(rightVal)) {
            return;
        }
        queryWrapper.le(leftCol, rightVal);
        queryWrapper.ge(rightCol, leftVal);
    }

    public static void tryEq(QueryWrapper<?> queryWrapper,String colName, Object val) {
        if(ObjectUtil.isNotBlank(val)){
            queryWrapper.eq(colName, val);
        }
    }

    public static void tryLike(QueryWrapper<?> queryWrapper,String colName, Object val) {
        if(ObjectUtil.isNotBlank(val)){
            queryWrapper.like(colName, val);
        }
    }

    public static void tryBetweenOrEq(QueryWrapper<?> queryWrapper, String colName, String v1, String v2) {
        if (StrUtil.isBlank(v2)) {
            v2 = v1;
        }
        tryBetween(queryWrapper, colName, v1, v2);
    }


    public static void tryAllFuzzyLike(QueryWrapper<?> queryWrapper, String colName, String val) {
        if (!StringUtils.isBlank(val)) {
            queryWrapper.like(colName, "%" + val + "%");
        }
    }

    public static <T> void tryEq(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, String val) {
        tryTo(queryWrapper, e -> e.eq(sFunction, val), val);
    }

    public static <T> void tryGe(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, String val) {
        tryTo(queryWrapper, e -> e.ge(sFunction, val), val);
    }

    public static <T> void tryLe(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, String val) {
        tryTo(queryWrapper, e -> e.le(sFunction, val), val);
    }

    public static <T> void tryLt(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, Object object) {
        tryTo(queryWrapper, e -> e.lt(sFunction, object), object);
    }


    public static <T> void tryTo(LambdaQueryWrapper<T> queryWrapper, Consumer<LambdaQueryWrapper<T>> consumer, Object val) {
        if (!StrUtil.isBlankIfStr(val)) {
            consumer.accept(queryWrapper);
        }
    }

    public static <T> void tryIn(AbstractWrapper queryWrapper, String column, Object... obj) {
        Collection<String> vs = CollUtils.toStrColl(obj);
        if (CollUtil.isNotEmpty(vs)) {
            queryWrapper.in(column, vs);
        }
    }


    public static <T> void tryIn(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, Object... obj) {
        Collection<String> vs = CollUtils.toStrColl(obj);
        if (CollUtil.isNotEmpty(vs)) {
            queryWrapper.in(sFunction, vs);
        }
    }

    public static <T> void tryAllFuzzyLike(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, String val) {
        if (!StringUtils.isBlank(val)) {
            queryWrapper.like(sFunction, "%" + val + "%");
        }
    }

    public static <T> void tryBetween(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, Object v1, Object v2) {
        if (StrUtil.isBlankIfStr(v1)) {
            v1 = v2;
            v2 = null;
        }

        if (!StrUtil.isBlankIfStr(v1)) {
            if (!StrUtil.isBlankIfStr(v2)) {
                queryWrapper.between(sFunction, v1, v2);
            } else {
                queryWrapper.eq(sFunction, v1);
            }
        }
    }


    public static String buildInSql(String field , Object... vals){
        Collection<?> colls = CollUtils.toColl(vals);
        if (CollUtil.isEmpty(colls)) {
            return "";
        }
        return StrUtil.format("{} in ('{}')", field, StringUtils.join(colls, "','"));
    }
}
