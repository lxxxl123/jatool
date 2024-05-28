package com.chen.jatool.common.utils.support.string.strfinder;


import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;


@EqualsAndHashCode
public class SkipStrPair {

    public String left;

    public String right;

    public Pattern leftPattern;

    public Pattern rightPattern;

    // 优先级
    public int priority;

    boolean leftRegex = false;

    boolean rightRegex = false;

    SkipStrPair(String left, String right, int priority) {
        this.left = left;
        this.right = right;
        this.priority = priority;
    }

}