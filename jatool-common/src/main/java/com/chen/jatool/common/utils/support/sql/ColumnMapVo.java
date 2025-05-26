package com.chen.jatool.common.utils.support.sql;

import lombok.Data;

@Data
public class ColumnMapVo {
    private String property;

    /*用于执行sql*/
    private String column;
}