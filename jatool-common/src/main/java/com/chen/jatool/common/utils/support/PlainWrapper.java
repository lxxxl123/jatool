package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.CollUtils;
import com.chen.jatool.common.utils.SqlUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author chenwh3
 */
public class PlainWrapper extends QueryWrapper<Object> {

    public static final String CONDITION = "condition";
    private final StringBuilder sqlSegment = new StringBuilder();

    @Getter
    private JSONObject entity;

    public static PlainWrapper ofObj(Object o){
        if(o instanceof PlainWrapper){
            return (PlainWrapper) o;
        }
        if(o instanceof Map){
            return of((Map<String, Object>) o);
        }

        throw new ServiceException("PlainWrapper.of() 参数类型不支持 {}", o);
    }



    @Override
    public PlainWrapper setEntity(Object entity) {
        if(entity instanceof Map){
            this.entity = new JSONObject(entity);
        } else {
            throw new ServiceException("PlainWrapper.setEntity() 参数类型不支持 {}", entity);
        }
        return this;
    }

    public PlainWrapper put(String key, Object val){
        if (entity == null) {
            entity = new JSONObject();
        }
        entity.put(key, val);
        return this;
    }

    public static PlainWrapper of(Map<String,Object> map){
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
        plainWrapper.sqlSegment.append(condition);
        return plainWrapper;
    }


    public void addSql(String str, Object... vals) {
        if (sqlSegment.length() != 0) {
            sqlSegment.append(" and ");
        }
        for (int i = 0; i < vals.length; i++) {
            vals[i] = tryFormatVal(vals[i]);
        }
        sqlSegment.append(StrUtil.format(str, vals));
    }

    @Override
    public PlainWrapper eq(String column, Object val) {
        addSql("{} = '{}'", column, val);
        return this;
    }

    @Override
    public PlainWrapper le(String column, Object val) {
        addSql("{} <= '{}'", column, val);
        return this;
    }

    @Override
    public PlainWrapper ge(String column, Object val) {
        addSql("{} >= '{}'", column, val);
        return this;
    }




    public PlainWrapper tryEq(String column, Object val){
        SqlUtil.tryEq(this, column, Convert.toStr(val));
        return this;
    }

    public PlainWrapper tryIn(String column, Object... vals){
        SqlUtil.tryIn(this, column, vals);
        return this;
    }



    @Override
    public QueryWrapper<Object> apply(String applySql, Object... value) {
        addSql(applySql, value);
        return this;
    }

    @Override
    public PlainWrapper in(String column, Object...vals) {
        Collection<?> coll = CollUtils.toColl(vals);
        addSql("{} in ('{}')", column, CollUtil.join(coll, "','"));
        return this;
    }

    @Override
    public PlainWrapper in(String column, Collection<?> coll) {
        return this.in(column, (Object) coll);
    }

    public Object tryFormatVal(Object o){
        if (o instanceof Date || o instanceof Calendar || o instanceof DateTimes) {
            o = DateTimes.of(o).formatDateTime();
        }
        return o;
    }
    public PlainWrapper between(String column, Object left, Object right) {
        addSql("{} between '{}' and '{}'", column, left, right);
        return this;
    }

    public PlainWrapper tryBetween2Col(String leftCol, String rightCol, Collection<?> coll) {
        SqlUtil.tryBetween2Col(this, leftCol, rightCol, CollUtil.get(coll, 0), CollUtil.get(coll, 1));
        return this;
    }

    public void tryRange(String column, Object from, Object to, DateField dateField, int step, Consumer<PlainWrapper> consumer) {
        if(StringUtils.isAnyBlank(Convert.toStr(from), Convert.toStr(to))){
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
    
    public Map<String,Object> toMap(){
        Map<String, Object> map = new HashMap<>(4);
        if (entity != null) {
            map.putAll(entity);
        }
        map.put(CONDITION, sqlSegment.toString());
        return map;
    }

    @Override
    public PlainWrapper clone() {
        PlainWrapper plainWrapper = new PlainWrapper();
        plainWrapper.sqlSegment.append(this.sqlSegment.toString());
        plainWrapper.entity = new JSONObject(entity);
        return plainWrapper;
    }


}
