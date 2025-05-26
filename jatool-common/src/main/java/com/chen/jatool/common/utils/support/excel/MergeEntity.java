package com.chen.jatool.common.utils.support.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergeEntity {
    //起始行
    Integer row;
    //起始列
    Integer col;
    //合并行数量
    Integer rowspan;
    //合并列数量
    Integer colspan;
}
