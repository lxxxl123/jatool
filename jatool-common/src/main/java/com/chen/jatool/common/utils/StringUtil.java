package com.chen.jatool.common.utils;

public class StringUtil {

    public static boolean isChinese(char ch){
        return ch >= 0x4E00 && ch <= 0x9FA5;
    }

    public static String toStringOrElse(Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    public static String toNotNullStr(Object obj) {
        return toStringOrElse(obj, "");
    }
}
