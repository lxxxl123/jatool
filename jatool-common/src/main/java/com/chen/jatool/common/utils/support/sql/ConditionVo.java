package com.chen.jatool.common.utils.support.sql;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class ConditionVo implements Serializable {
    private String prefix;
    private String key;
    private String logic;
    private String value;
}