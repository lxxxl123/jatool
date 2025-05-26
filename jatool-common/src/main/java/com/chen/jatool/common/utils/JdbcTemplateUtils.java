package com.chen.jatool.common.utils;


import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 动态数据库 , 手动获切jdbcTemplate
 */
public class JdbcTemplateUtils {

//    private static Map<String, DataSource> dataSourceMap;
//    private static final Map<String, JdbcTemplate> JDBC_TEMPLATE_MAP = new HashMap<>(16);
//
//    private static DataSource getDatasource(String ds) {
//        if (dataSourceMap == null) {
//            DynamicRoutingDataSource dataSource = (DynamicRoutingDataSource) SpringUtil.getBean(JdbcTemplate.class).getDataSource();
//            dataSourceMap = dataSource.getCurrentDataSources();
//        }
//        return dataSourceMap.get(ds);
//    }
//
//    public static JdbcTemplate getJdbtTemplate(String ds){
//        return JDBC_TEMPLATE_MAP.computeIfAbsent(ds, (k) -> new JdbcTemplate(getDatasource(ds)));
//    }

    public static final int QUERY_TIMEOUT = 60;

    public static JdbcTemplate getJdbcTemplate() {
        // 必须设置为 DynamicRoutingDataSource
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
//            设置超时时间 (s)
        jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT);
        return jdbcTemplate;
//        return JDBC_TEMPLATE_MAP.computeIfAbsent("common", key -> {
//            JdbcTemplate jdbcTemplate = new JdbcTemplateEx(getDds());
//            设置超时时间 (s)
//            jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT);
//            return jdbcTemplate;
//        });
    }
}