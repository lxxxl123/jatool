//package com.haday.qms.core.tool.support;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.exceptions.ExceptionUtil;
//import cn.hutool.core.lang.func.Func1;
//import cn.hutool.core.util.StrUtil;
//import com.baomidou.mybatisplus.core.metadata.OrderItem;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.haday.qms.core.log.exception.ServiceException;
//import com.haday.qms.core.tool.support.lambda.Func2;
//import com.haday.qms.core.tool.utils.JdbcTemplateUtils;
//import com.haday.qms.core.tool.utils.LambdaUtils;
//import com.haday.qms.core.tool.utils.SqlUtil;
//import com.haday.qms.vo.PageVo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class JdbcTool {
//
//    private String countMethodName;
//    private String methodName;
//
//    private Object[] args;
//
//    private Object mapper;
//
//    private PageVo<?> page;
//
//    private JdbcTool(Object mapper) {
//        this.mapper = mapper;
//    }
//
//    public static JdbcTool of(Object mapper) {
//        return new JdbcTool(mapper);
//    }
//
//    public static <T, R> JdbcTool ofFunc(Func1<T, R> func1) {
//        Object mapper = LambdaUtils.getSpringBean(func1);
//        String methodName = LambdaUtils.getMethodName(func1);
//        JdbcTool jdbcTool = new JdbcTool(mapper);
//        jdbcTool.methodName(methodName);
//        return jdbcTool;
//    }
//
//    public static <T, F, R> JdbcTool ofFunc2(Func2<T, F, R> func1) {
//        Object mapper = LambdaUtils.getSpringBean(func1);
//        String methodName = LambdaUtils.getMethodName(func1);
//        JdbcTool jdbcTool = new JdbcTool(mapper);
//        jdbcTool.methodName(methodName);
//        return jdbcTool;
//    }
//
//    public <T, R> JdbcTool countMethod(Func1<T, R> func1) {
//        this.countMethodName = LambdaUtils.getMethodName(func1);
//        return this;
//    }
//
//    public <T, F, R> JdbcTool countMethod2(Func2<T, F, R> func1) {
//        this.countMethodName = LambdaUtils.getMethodName(func1);
//        return this;
//    }
//
//    public JdbcTool countMethodName(String countMethodName) {
//        this.countMethodName = countMethodName;
//        return this;
//    }
//
//    public JdbcTool methodName(String methodName) {
//        this.methodName = methodName;
//        return this;
//    }
//
//    public JdbcTool args(Object... args) {
//        if (args.length >= 1 && args[0] instanceof Page) {
//            this.page = PageVo.of((Page) args[0]);
//        }
//        this.args = args;
//        return this;
//    }
//
//    private String sql = null;
//
//    private String getSql(String methodName) {
//        if (StrUtil.isBlank(methodName)) {
//            methodName = this.methodName;
//        }
//        if (StrUtil.isBlank(methodName)) {
//            throw new ServiceException("未指定方法");
//        }
//        if (StrUtil.isNotBlank(sql) && StrUtil.equals(this.methodName, this.countMethodName)) {
//            return sql;
//        }
//        sql = SqlUtil.getMapperPageSql(mapper, methodName, page, args);
//        return sql;
//    }
//
//
//
//    public Long getCount() {
//        String sql = getSql(countMethodName);
//        String countSql = SqlServerFinder.of(sql)
//                .left("select")
//                .right("from")
//                .replaceWholeGroup("select count(*) as __num__ from ");
//
//        try {
//            return executeSql(countSql, Long.class);
//        } catch (Exception e) {
//            if (e.getMessage().contains("该语句没有返回结果集")) {
//                log.warn("尝试重新查询", e);
//                return executeSql(countSql, Long.class);
//            } else {
//                throw e;
//            }
//        }
//    }
//
//    public static final String PAGE_WRAPPER = " * from (select ROW_NUMBER() OVER (%s) as __rw__, %s) __table__ where __rw__ between %s and %s ";
//
//    public PageVo<Map<String, Object>> getPage() {
//        return (PageVo) getPage(Map.class);
//    }
//
//    public List<Map<String,Object>> getList(){
//        return (List)getList(Map.class);
//    }
//
//    public <T> List<T> getList(Class<T> clazz){
//        String sql = getSql(methodName);
//        return executeSqlForList(sql, clazz);
//    }
//
//    public <T> PageVo<T> getPage(Class<T> clazz) {
//        String sql = getSql(methodName);
//        String orderBy = getOrderBy();
//        long betweenLeft = page.getSize() * (page.getCurrent() - 1) + 1;
//        long betweenRight = page.getSize() * page.getCurrent();
//        String selectSql = SqlServerFinder.of(sql)
//                .left("select")
//                .right("option", sql.length())
//                .replaceGroup(group -> String.format(PAGE_WRAPPER, orderBy, group, betweenLeft, betweenRight));
//
//        try {
//            List res = (List) executeSqlForList(selectSql, clazz);
//            page.setRecords(res);
//            if (page.isSearchCount()) {
////                if (res.size() < page.getSize() && page.getCurrent() == 1) {
////                    page.setTotal(res.size());
////                } else {
////                    page.setTotal(getCount());
////                }
//                page.setTotal(getCount());
//            }
//
//            return (PageVo) page;
//        } catch (Exception e) {
//            log.error("JdbcTemplate 查询出错 , sql = {}", selectSql, e);
//            if (StrUtil.contains(ExceptionUtil.getRootCauseMessage(e), "has timed out")) {
//                throw new ServiceException("查询超时,请联系管理员");
//            }
//            throw new ServiceException(e.getMessage());
//        }
//
//    }
//
//    private <T> T executeSql(String sql, Class<T> clazz) {
//        log.debug(sql);
//        return JdbcTemplateUtils.getJdbtTemplate().queryForObject(sql, clazz);
//    }
//
//
//    private <T> List<T> executeSqlForList(String sql, Class<T> clazz) {
//        log.debug(sql);
//        if (Map.class.isAssignableFrom(clazz)) {
//            return (List) JdbcTemplateUtils.getJdbtTemplate().queryForList(sql);
//        }
//        return JdbcTemplateUtils.getJdbtTemplate().query(sql, new BeanPropertyRowMapper<>(clazz));
//    }
//
//
//    private String getOrderBy() {
//        if (page == null) {
//            return "";
//        }
//        List<OrderItem> orders = page.getOrders();
//        //获取order by
//        String orderBy;
//        if (CollUtil.isNotEmpty(orders)) {
//            orderBy = "order by " + orders.stream().map(e -> e.getColumn() + (e.isAsc() ? " asc" : " desc"))
//                    .collect(Collectors.joining(","));
//        } else {
//            orderBy = "ORDER BY CURRENT_TIMESTAMP";
//        }
//        return orderBy;
//    }
//
//
//}
