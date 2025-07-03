package com.chen.jatool.common.utils;

import cn.hutool.core.util.StrUtil;

import java.util.Map;

public class StringUtil {

    public static boolean isChinese(char ch){
        //获取此字符的UniCodeBlock
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        //  GENERAL_PUNCTUATION 判断中文的“号
        //  CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号
        //  HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号
        return (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION);
    }

    public static String toStringOrElse(Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    public static String toNotNullStr(Object obj) {
        return toStringOrElse(obj, "");
    }

    public static String replace(CharSequence cs, Map<String, ?> replaceMap) {
        int length = cs.length();
        int i = 0;
        StringBuilder sb = new StringBuilder(cs);

        for (; i < length; i++) {
            if (sb.charAt(i) == '{') {
                int j = i + 1;
                for (; j < length; j++) {
                    if (sb.charAt(j) == '{') {
                        i = j;
                    } else if (sb.charAt(j) == '}') {
                        String key = sb.substring(i + 1, j);
                        String value = StrUtil.toStringOrNull(replaceMap.get(key));
                        if (value != null) {
                            sb.replace(i, j + 1, value);
                            length = sb.length();
                            i = i + value.length();
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}
