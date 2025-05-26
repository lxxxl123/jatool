package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.sql.ColumnMapVo;
import com.chen.jatool.common.utils.support.sql.ConditionVo;
import com.chen.jatool.common.utils.support.sql.PlainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static void tryBetween2Col(QueryWrapper<?> queryWrapper, String leftCol, String rightCol, Object val){
        if (val == null || "".equals(val)) {
            return;
        }
        queryWrapper.le(leftCol, val);
        queryWrapper.ge(rightCol, val);
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
        List<String> vs = CollUtils.toStrList(obj);
        if (CollUtil.isNotEmpty(vs)) {
            queryWrapper.in(column, vs);
        }
    }


    public static <T> void tryIn(LambdaQueryWrapper<T> queryWrapper, SFunction<T, ?> sFunction, Object... obj) {
        List<String> vs = CollUtils.toStrList(obj);
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

    private static String getPrefix(String prefix) {
        return StrUtil.isBlank(prefix) ? "" : (prefix + ".");
    }

    private static List<String> splitString(String str) {
        return Arrays.stream(str.split("\n|\r\n|\r")).filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }

    /*quote：com.haday.tp.query.service.impl.BaseQueryServiceImpl*/
    public static void buildConsumer(List<ConditionVo> conditions, QueryWrapper<?> consumer, boolean orFlag, List<ColumnMapVo> columnMap) {
        for (int i = 0; i < conditions.size(); ++i) {
            ConditionVo conditionVo = conditions.get(i);
            if (i > 0 && orFlag) {
                consumer = consumer.or();
            }

            String key = conditionVo.getKey();
            if (CollUtil.isNotEmpty(columnMap)) {
                for (ColumnMapVo map : columnMap) {
                    if (StrUtil.equals(map.getProperty(), key)) {
                        key = map.getColumn();
                        break;
                    }
                }
            }

            String[] range;
            List<String> values;
            String left = getPrefix(conditionVo.getPrefix()) + key;
            String right = conditionVo.getValue();
            switch (conditionVo.getLogic()) {
                case "is":
                    consumer = consumer.eq(left, right);
                    break;
                case "not":
                    consumer = consumer.ne(left, right);
                    break;
                case "should":
                    if (consumer instanceof PlainWrapper) {
                        consumer = ((PlainWrapper) consumer).fuzzyLike(left, right);
                    } else {
                        consumer = consumer.like(left, right);
                    }
                    break;
                case "likeRight":
                    consumer = consumer.likeRight(left, right);
                    break;
                case "likeLeft":
                    consumer = consumer.likeLeft(left, right);
                    break;
                case "not_should":
                    consumer = consumer.notLike(left, right);
                    break;
                case "range":
                    values = splitString(right);
                    consumer = consumer.in(left, values);
                    break;
                case "not_range":
                    values = splitString(right);
                    consumer = consumer.notIn(left, values);
                    break;
                case "blank":
                    consumer = consumer.and(e->{
                        e.isNull(left).or().eq(left, "");
                    });
                    break;
                case "present":
                    consumer = consumer.isNotNull(left);
                    consumer = consumer.gt(left, "");
                    break;
                case "lte":
                    consumer = consumer.le(left, right);
                    break;
                case "gte":
                    consumer = consumer.ge(left, right);
                    break;
                case "brange":
                    range = right.split(",");
                    consumer = consumer.between(left, range[0], range[1]);
                    break;
                case "not_brange":
                    range = right.split(",");
                    consumer = consumer.notBetween(left, range[0], range[1]);
                    break;
                case "lt":
                    consumer = consumer.lt(left, right);
                    break;
                case "gt":
                    consumer = consumer.gt(left, right);
            }

        }
    }

    /**
     * 通过接口获取sql
     *
     * @param mapper
     * @param methodName
     * @param args
     * @return
     */
    public static String getMapperSql(Object mapper, String methodName, Object... args) {
        MetaObject metaObject = SystemMetaObject.forObject(mapper);
        SqlSession session = (SqlSession) metaObject.getValue("h.sqlSession");
        Class mapperInterface = (Class) metaObject.getValue("h.mapperInterface");
        String fullMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMethodName, null);
        } else {
            return getMapperSql(session, mapperInterface, methodName, args);
        }
    }

    /**
     * 通过Mapper接口和方法名
     *
     * @param session
     * @param mapperInterface
     * @param methodName
     * @param args
     * @return
     */
    public static String getMapperSql(SqlSession session, Class mapperInterface, String methodName, Object... args) {
        String fullMapperMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMapperMethodName, null);
        }
        Method method = ReflectUtil.getMethodByName(mapperInterface, methodName);
        Map params = new HashMap();
        final Class<?>[] argTypes = method.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (!RowBounds.class.isAssignableFrom(argTypes[i]) && !ResultHandler.class.isAssignableFrom(argTypes[i])) {
                String paramName = "param" + String.valueOf(params.size() + 1);
                paramName = getParamNameFromAnnotation(method, i, paramName);
                params.put(paramName, i >= args.length ? null : args[i]);
            }
        }
        if (args != null && args.length == 1) {
            Object _params = wrapCollection(args[0]);
            if (_params instanceof Map) {
                params.putAll((Map) _params);
            }
        }
        return getNamespaceSql(session, fullMapperMethodName, params);
    }

    public static String getMapperPageSql(Object mapper, String methodName, Page page, Object... args) {
        MetaObject metaObject = SystemMetaObject.forObject(mapper);
        Object proxy = metaObject.getValue("h");
        SqlSession session;
        Class mapperInterface;
        if (proxy instanceof MybatisMapperProxy) {
            session = (SqlSession) metaObject.getValue("h.sqlSession");
            mapperInterface = (Class) metaObject.getValue("h.mapperInterface");
        } else {
            // 部分mapper的代理类为JdkDynamicProxy，需要从该位置获取sql。如FactoryReportMapper，原理未知
            session = (SqlSession) metaObject .getValue("h.advised.targetSource.target.h.sqlSession");
            mapperInterface = (Class) metaObject.getValue("h.advised.targetSource.target.h.mapperInterface");
        }
        String fullMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        Method method = ReflectUtil.getMethodByName(mapperInterface, methodName);
        Parameter[] parameters = method.getParameters();
        if (parameters == null || parameters.length == 0 || args == null || args.length == 0) {
            return getNamespaceSql(session, fullMethodName, null);
        } else {
            if (Page.class.isAssignableFrom(parameters[0].getType()) && !(args[0] instanceof Page)) {
                args = ArrayUtil.insert(args, 0, page);
                return getMapperSql(session, mapperInterface, methodName, args);
            } else if (!Page.class.isAssignableFrom(parameters[0].getType()) && args[0] instanceof Page) {
                args = ArrayUtil.remove(args,0);
                return getMapperSql(session, mapperInterface, methodName, args);
            }
            return getMapperSql(session, mapperInterface, methodName, args);
        }
    }

    /**
     * 通过命名空间方式获取sql
     *
     * @param session
     * @param namespace
     * @param params
     * @return
     */
    public static String getNamespaceSql(SqlSession session, String namespace, Object params) {
        params = wrapCollection(params);
        Configuration configuration = session.getConfiguration();
        MappedStatement mappedStatement = configuration.getMappedStatement(namespace);
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        BoundSql boundSql = mappedStatement.getBoundSql(params);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (params == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(params.getClass())) {
                        value = params;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(params);
                        value = metaObject.getValue(propertyName);
                    }
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) jdbcType = configuration.getJdbcTypeForNull();
                    sql = replaceParameter(sql, value, jdbcType, parameterMapping.getJavaType());
                }
            }
        }
        return sql;
    }

    /**
     * 根据类型替换参数
     * 仅作为数字和字符串两种类型进行处理，需要特殊处理的可以继续完善这里
     *
     * @param sql
     * @param value
     * @param jdbcType
     * @param javaType
     * @return
     */
    private static String replaceParameter(String sql, Object value, JdbcType jdbcType, Class javaType) {
        String strValue = String.valueOf(value);
        if (jdbcType != null) {
            switch (jdbcType) {
                //数字
                case BIT:
                case TINYINT:
                case SMALLINT:
                case INTEGER:
                case BIGINT:
                case FLOAT:
                case REAL:
                case DOUBLE:
                case NUMERIC:
                case DECIMAL:
                    break;
                //日期
                case DATE:
                case TIME:
                case TIMESTAMP:
                    //其他，包含字符串和其他特殊类型
                default:
                    strValue = "'" + strValue + "'";


            }
        } else if (Number.class.isAssignableFrom(javaType)) {
            //不加单引号
        } else {
            strValue = "'" + strValue + "'";
        }
        return sql.replaceFirst("\\?", strValue);
    }

    /**
     * 简单包装参数
     *
     * @param object
     * @return
     */
    private static Object wrapCollection(final Object object) {
        if (object instanceof List) {
            Map<String, Object> map = new HashMap<>();
            map.put("list", object);
            return map;
        } else if (object != null && object.getClass().isArray()) {
            Map<String, Object> map = new HashMap<>();
            map.put("array", object);
            return map;
        }
        return object;
    }

    /**
     * 获取参数注解名
     *
     * @param method
     * @param i
     * @param paramName
     * @return
     */
    private static String getParamNameFromAnnotation(Method method, int i, String paramName) {
        final Object[] paramAnnos = method.getParameterAnnotations()[i];
        for (Object paramAnno : paramAnnos) {
            if (paramAnno instanceof Param) {
                paramName = ((Param) paramAnno).value();
            }
        }
        return paramName;
    }
}
