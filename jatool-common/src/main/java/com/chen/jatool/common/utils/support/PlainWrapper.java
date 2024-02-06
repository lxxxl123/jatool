package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.CollUtils;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.SqlUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author chenwh3
 */
public class PlainWrapper extends QueryWrapper<Object> {

    public static final String CONDITION = "condition";
    public static final String OR = " or ";
    public static final String AND = " and ";
    private final StringBuilder sqlSegment = new StringBuilder();

    @Getter
    private JSONObject entity;

    private boolean strict = true;

    public PlainWrapper strictMode(boolean strict) {
        this.strict = strict;
        return this;
    }

    private void appendSqlSegment(String str) {
        this.sqlSegment.append(str);
    }

    private void appendSqlSetmentAndCheck(String str) {
        SqlUtil.checkSql(str);
        this.sqlSegment.append(str);

    }

    @Override
    public PlainWrapper setEntity(Object entity) {
        if (entity instanceof Map) {
            this.entity = new JSONObject(entity);
        } else {
            throw new ServiceException("PlainWrapper.setEntity() 参数类型不支持 {}", entity);
        }
        return this;
    }

    public PlainWrapper put(String key, Object val) {
        if (entity == null) {
            entity = new JSONObject();
        }
        entity.put(key, val);
        return this;
    }

    public static PlainWrapper ofObj(Object o) {
        if (o instanceof PlainWrapper) {
            return (PlainWrapper) o;
        }
        if (o instanceof Map) {
            return of((Map<String, Object>) o);
        }
        throw new ServiceException("PlainWrapper.of() 参数类型不支持 {}", o);
    }


    public static PlainWrapper of(Map<String, Object> map) {
        Object o = map.get(CONDITION);
        PlainWrapper wrapper = of(Convert.toStr(o, ""));
        wrapper.entity = new JSONObject(map);
        return wrapper;
    }

    public static PlainWrapper of() {
        return new PlainWrapper();
    }

    public static PlainWrapper of(String condition) {
        PlainWrapper plainWrapper = new PlainWrapper();
        SqlUtil.checkSql(condition);
        plainWrapper.appendSqlSegment(condition);
        return plainWrapper;
    }

    public PlainWrapper and(Consumer<QueryWrapper<Object>> consumer) {
        PlainWrapper wrapper = PlainWrapper.of();
        consumer.accept(wrapper);
        String sql = wrapper.getSqlSegment();
        if (StrUtil.isNotBlank(sql)) {
            addSql("(" + sql + ")");
        }
        return this;
    }

    public PlainWrapper or() {
        appendSqlSegment(OR);
        return this;
    }


    private interface ValWrapper {
        default void validateVal(String val) {
            // if val contains  continuous '' , it ok , but one ' then throw ex
            int l = val.length();
            for (int i = 0; i < l; i++) {
                char c = val.charAt(i);
                if (c == '\'') {
                    if (i + 1 < l && val.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        throw new ServiceException("illegal input, val = {}", val);
                    }
                }
            }

        }
        String toString();
    }

    private class CollValWrapper implements ValWrapper {
        private final Collection<?> coll;
        public CollValWrapper(Object obj) { this.coll = CollUtils.toColl(obj); }

        @Override
        public String toString() {
            String res = coll.stream()
                    .map(e -> Convert.toStr(e, ""))
                    .peek(e -> {
                        if (strict) {
                            validateVal(e);
                        }
                    })
                    .collect(Collectors.joining("','"));
            return "('" + res + "')";
        }
    }

    private class StrValWrapper implements ValWrapper {
        private final Object str;
        private String prefix = "";
        private String suffix = "";
        private StrValWrapper setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        private StrValWrapper setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public StrValWrapper(Object o) {
            if (o instanceof Date || o instanceof Calendar || o instanceof DateTimes) {
                o = DateTimes.of(o).formatDateTime();
            }
            this.str = o;
        }



        @Override
        public String toString() {

            String val = Convert.toStr(str, "");
            if (strict) {
                validateVal(val);
            }
            return "'" + prefix + val + suffix + "'";
        }

    }




    public PlainWrapper addSql(String str, Object... vals) {
        String sql = getSqlSegment();
        if (StrUtil.isNotBlank(sql) && !sql.endsWith(OR) && !sql.endsWith(AND)) {
            appendSqlSegment(AND);
        }
        appendSqlSegment(StrUtil.format(str, vals));
        return this;
    }

    @Override
    public PlainWrapper between(String column, Object left, Object right) {
        addSql("{} between {} and {}", column, new StrValWrapper(left), new StrValWrapper(right));
        return this;
    }
    @Override
    public PlainWrapper eq(String column, Object val) {
        addSql("{} = {}", column, new StrValWrapper(val));
        return this;
    }

    public PlainWrapper notEq(String column, Object val) {
        addSql("{} != {}", column, new StrValWrapper(val));
        return this;
    }

    @Override
    public PlainWrapper le(String column, Object val) {
        addSql("{} <= {}", column, new StrValWrapper(val));
        return this;
    }

    @Override
    public PlainWrapper ge(String column, Object val) {
        addSql("{} >= {}", column, new StrValWrapper(val));
        return this;
    }

    @Override
    public PlainWrapper like(String column, Object val) {
        addSql("{} like {}", column, new StrValWrapper(val));
        return this;
    }

    public PlainWrapper fuzzyLike(String column, Object val) {
        addSql("{} like {}", column, new StrValWrapper(val).setPrefix("%").setSuffix("%"));
        return this;
    }

    @Override
    public PlainWrapper likeRight(String column, Object val) {
        addSql("{} like {}", column, new StrValWrapper(val).setPrefix("%"));
        return this;
    }

    @Override
    public PlainWrapper in(String column, Object... vals) {
        addSql("{} in {}", column, new CollValWrapper(vals));
        return this;
    }

    @Override
    public PlainWrapper in(String column, Collection<?> coll) {
        return this.in(column, (Object) coll);
    }


    public PlainWrapper tryEq(String column, Object val) {
        SqlUtil.tryEq(this, column, Convert.toStr(val));
        return this;
    }

    public PlainWrapper tryIn(String column, Object... vals) {
        SqlUtil.tryIn(this, column, vals);
        return this;
    }

    public PlainWrapper tryLike(String column, Object val) {
        SqlUtil.tryLike(this, column, val);
        return this;
    }

    public PlainWrapper tryFuzzyLike(String column, Object val) {
        if(ObjectUtil.isNotBlank(val)){
            fuzzyLike(column, val);
        }
        return this;
    }


    @Override
    public QueryWrapper<Object> apply(String applySql, Object... value) {
        appendSqlSetmentAndCheck(StrUtil.format(applySql, value));
        return this;
    }



    public PlainWrapper tryBetween(String column, Object left, Object right) {
        SqlUtil.tryBetween(this, column, left, right);
        return this;
    }

    public PlainWrapper tryBetween2Col(String leftCol, String rightCol, Collection<?> coll) {
        SqlUtil.tryBetween2Col(this, leftCol, rightCol, CollUtil.get(coll, 0), CollUtil.get(coll, 1));
        return this;
    }

    public void tryRange(String column, Object from, Object to, DateField dateField, int step, Consumer<PlainWrapper> consumer) {
        if (StringUtils.isAnyBlank(Convert.toStr(from), Convert.toStr(to))) {
            consumer.accept(this);
        } else {
            DateTimes.of(from).loopRange(to, dateField, step, (f, t) -> {
                final PlainWrapper copy = this.clone();
                copy.between(column, f, t);
                consumer.accept(copy);
            });
        }
    }

    public PlainWrapper tryBetween2Col(String leftCol, String rightCol, Object leftVal, Object rigthVal) {
        SqlUtil.tryBetween2Col(this, leftCol, rightCol, leftVal, rigthVal);
        return this;
    }

    public String getSqlSegment() {
        return sqlSegment.toString();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(4);
        if (entity != null) {
            map.putAll(entity);
        }
        map.put(CONDITION, getSqlSegment());
        return map;
    }

    private static final Pattern PTN_ORDER_SPLIT = Pattern.compile("\\s*,\\s*(?=\\w\\.|\\w{2}|$)");

    public Page<?> buildPage() {
        if (this.entity == null) {
            throw new ServiceException("please fulfill the page args");
        }
        String order = Convert.toStr(this.entity.getOrDefault("order", "")).trim();
        Long pageSize = Convert.toLong(this.entity.getOrDefault("pageSize", 200L));
        Long pageIndex = Convert.toLong(this.entity.getOrDefault("pageIndex", 1L));
        Boolean searchCount = Convert.toBool(this.entity.getOrDefault("searchCount", false));

        //分页参数设置
        Page<?> page = new Page<>(pageIndex, pageSize, searchCount);
        //处理排序
        if (StringUtils.isNotBlank(order)) {
            String[] orders = PTN_ORDER_SPLIT.split(order);
            List<OrderItem> list = new ArrayList<>();
            for (String column : orders) {
                //不填或者带有asc结尾代表该列升序
                int isDesc = StrUtil.indexOfIgnoreCase(column, " desc");
                String param;
                if (isDesc > 0) {
                    param = column.substring(0, isDesc);
                } else {
                    int asc = StrUtil.indexOfIgnoreCase(column, " asc");
                    if (asc != -1) {
                        param = StrUtil.sub(column, 0, asc);
                    } else {
                        param = column;
                    }
                }
                OrderItem orderItem = new OrderItem(param, isDesc < 0);
                list.add(orderItem);
            }
            page.setOrders(list);
        }
        return page;
    }

    @Override
    public PlainWrapper clone() {
        PlainWrapper plainWrapper = new PlainWrapper();
        appendSqlSetmentAndCheck(getSqlSegment());
        plainWrapper.entity = new JSONObject(entity);
        return plainWrapper;
    }
}
