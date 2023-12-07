package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.CollUtils;
import com.chen.jatool.common.utils.SqlUtil;
import lombok.Getter;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author chenwh3
 */
public class PlainWrapper extends QueryWrapper<Object> {

    private final StringBuilder sqlSegment = new StringBuilder();

    private boolean strict = false;

    public PlainWrapper strictMode(boolean strict) {
        this.strict = strict;
        return this;
    }

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

    public static PlainWrapper of(Map<String,Object> map){
        Object o = map.get("condition");
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

    private void validateVal(Object o) {
        Collection<String> list = CollUtils.toStrColl(o);
        for (String val : list) {
            if (val.contains("'")) {
                throw new ServiceException("dangerous sql! , params = {}", val);
            }
        }
    }

    public void addSql(String str, Object... vals) {
        if (sqlSegment.length() != 0) {
            sqlSegment.append(" and ");
        }
        for (int i = 0; i < vals.length; i++) {
            Object val = tryFormatVal(vals[i]);
            if (strict) {
                validateVal(val);
            }
            vals[i] = val;
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

    public PlainWrapper like(String column, Object val) {
        addSql("{} like '{}'", column, val);
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

    public PlainWrapper tryLike(String column, Object val){
        SqlUtil.tryLike(this, column, val);
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

    public PlainWrapper tryBetween2Col(String leftCol, String rightCol, Object leftVal, Object rigthVal) {
        SqlUtil.tryBetween2Col(this, leftCol, rightCol, leftVal, rigthVal);
        return this;
    }

    public String getSqlSegment() {
        return sqlSegment.toString();
    }

    @Override
    public PlainWrapper clone() {
        PlainWrapper plainWrapper = new PlainWrapper();
        plainWrapper.sqlSegment.append(this.sqlSegment.toString());
        plainWrapper.entity = new JSONObject(entity);
        return plainWrapper;
    }
}
