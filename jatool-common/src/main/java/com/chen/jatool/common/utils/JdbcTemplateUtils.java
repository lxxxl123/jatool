package com.chen.jatool.common.utils;


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
}