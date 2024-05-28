package com.chen.jatool.common.utils.support.string.strfinder;


import lombok.Getter;
import lombok.ToString;


@ToString
public class StrHolder {

    final int left;

    final int right;

    public StrHolder(int left, int right) {
        this.left = left;
        this.right = right;
    }

    StrHolder(int i) {
        this.left = i;
        this.right = i;
    }
}