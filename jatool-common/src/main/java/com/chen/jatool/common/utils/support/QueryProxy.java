package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.jatool.common.modal.vo.PageVo;
import com.chen.jatool.common.utils.support.lambda.Func1;
import com.chen.jatool.common.utils.support.lambda.Func2;
import com.chen.jatool.common.utils.support.sql.JdbcTool;
import com.chen.jatool.common.utils.support.sql.PlainWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author chenwh3
 */
public class QueryProxy {

    private JdbcTool jdbcTool;

    private QueryWrapper wrapper;

    private PageVo<?> inPage;

    private Consumer<List<?>> afterGet;

    private QueryProxy(){
    }

    public static <F> QueryProxy ofFun(Func1<F,Object> func1) {
        QueryProxy QueryProxy = new QueryProxy();
        QueryProxy.jdbcTool = JdbcTool.ofFunc(func1);
        return QueryProxy;
    }

    public static <F, T> QueryProxy ofFun2(Func2<T, F, ?> mapper) {
        QueryProxy QueryProxy = new QueryProxy();
        QueryProxy.jdbcTool = JdbcTool.ofFunc2(mapper);
        return QueryProxy;
    }



    public <T> PageVo<T> getPage(Class<T> clazz) {
        beforeGetPage();
        if (checkIsBlock()) {
            return new PageVo<>(0, 0);
        }
        PageVo<T> page = jdbcTool.getPage(clazz);
        tryAfterGet(page.getRecords());
        return page;
    }

    public PageVo<Map<String, Object>> getPage() {
        beforeGetPage();
        if (checkIsBlock()) {
            return new PageVo<>(0, 0);
        }
        PageVo<Map<String, Object>> page = jdbcTool.getPage();
        tryAfterGet(page.getRecords());
        return page;
    }

    public <T> List<T> getList(Class<T> clazz) {
        beforeGetList();
        if (checkIsBlock()) {
            return Collections.emptyList();
        }
        return tryAfterGet(jdbcTool.getList(clazz));
    }

    public List<Map<String, Object>> getList() {
        beforeGetList();
        if (checkIsBlock()) {
            return Collections.emptyList();
        }
        return tryAfterGet(jdbcTool.getList());

    }

    private <T> List<T> tryAfterGet(List<T> list) {
        if (afterGet != null) {
            afterGet.accept(list);
        }
        return list;

    }

    public <T> T getOne(Class<T> clazz) {
        return CollUtil.getFirst(getList(clazz));
    }

    public Map<String,Object> getOne(){
        return CollUtil.getFirst(getList());
    }

    public boolean checkIsBlock(){
        return wrapper instanceof PlainWrapper && Boolean.TRUE.equals(((PlainWrapper) wrapper).getBlock());
    }

    public Long getCount() {
        beforeGet();
        if (checkIsBlock()) {
            return 0L;
        }
        return jdbcTool.getCount();
    }


    private void beforeGet(){
        if (wrapper == null) {
            wrapper = PlainWrapper.of();
        }
        if (wrapper instanceof PlainWrapper) {
            plainWrapperConsumer.accept((PlainWrapper) wrapper);
        }
    }
    private void beforeGetPage(){
        beforeGet();
        if (inPage == null && wrapper != null && wrapper instanceof PlainWrapper) {
            inPage = ((PlainWrapper) wrapper).buildPage();
        }
        jdbcTool.args(inPage, wrapper);
    }


    private void beforeGetList(){
        beforeGet();
        jdbcTool.args(wrapper);
    }

    public QueryProxy setWrapper(QueryWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public QueryProxy afterGet(Consumer<List<?>> afterGet) {
        this.afterGet = afterGet;
        return this;
    }


    private Consumer<PlainWrapper> plainWrapperConsumer = (wrapper)-> {};

    public QueryProxy computePlainWrapper(Consumer<PlainWrapper> consumer) {
        plainWrapperConsumer = consumer;
        return this;
    }

    public QueryProxy setPage(Page inPage) {
        this.inPage = PageVo.of(inPage);
        return this;
    }

}
