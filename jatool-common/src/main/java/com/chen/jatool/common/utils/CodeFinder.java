package com.chen.jatool.common.utils;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.finder.Finder;
import cn.hutool.core.text.finder.TextFinder;


public class CodeFinder extends TextFinder {

    private final CharSequence strToFind;

    /**
     * 构造
     * @param strToFind       被查找的字符串
     */
    public CodeFinder(CharSequence strToFind) {
        Assert.notEmpty(strToFind);
        this.strToFind = strToFind;
    }


    @Override
    public int start(int from) {
        return 0;
    }

    @Override
    public int end(int start) {
        return 0;
    }

    @Override
    public Finder reset() {
        "".hashCode();
        return super.reset();
    }
}
