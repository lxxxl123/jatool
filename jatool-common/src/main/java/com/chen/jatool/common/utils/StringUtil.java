package com.chen.jatool.common.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.finder.StrFinder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

public class StringUtil {

    public static boolean isChinese(char ch) {
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


    public static int indexOf(String str, String findStr, int start, int end, boolean ignoreCase) {
        return new StrFinder(findStr, ignoreCase).setEndIndex(end).setText(str).start(start);
    }
    public static int indexOf(String str, String findStr, int start, int end) {
        return indexOf(str, findStr, start, end, false);
    }
    public static int indexOf(String str, String findStr, int start) {
        return indexOf(str, findStr, start, -1, false);
    }

    public static int endIndexOf(String str, String findStr, int start, int end, boolean ignoreCase) {
        return new StrFinder(findStr, ignoreCase).setEndIndex(end).setText(str).start(start);
    }

    public static int endIndexOf(String str, String findStr, int start, int end) {
        return endIndexOf(str, findStr, start, end, false);
    }

    public static int endIndexOf(String str, String findStr, int start) {
        return endIndexOf(str, findStr, start, -1, false);
    }



    public static int endIndexOfBatch(String cs, List<? extends CharSequence> list, int start) {
        return endIndexOfBatch(cs, list, start, -1);
    }

    public static int endIndexOfBatch(String cs, List<? extends CharSequence> list, int start, int orElseIdx) {
        int i = indexOfBatch(cs, list, start, orElseIdx);
        if (i == orElseIdx || i == -1) {
            return i;
        }
        return i + CollUtil.getLast(list).length();
    }


    public static int indexOfBatch(String cs, List<? extends CharSequence> list, int start) {
        return indexOfBatch(cs, list, start, -1);
    }

    public static int indexOfBatch(String cs, List<? extends CharSequence> list, int start , int orElseIdx) {
        if (cs == null) {
            return orElseIdx;
        }
        if (CollUtil.isEmpty(list)) {
            return orElseIdx;
        }
        for (CharSequence c : list) {
            start = cs.indexOf(c.toString(), start);
            if (start == -1) {
                return orElseIdx;
            }
        }
        return start;
    }


    public static String replace(CharSequence cs, Map<String, ?> replaceMap) {
        int length = cs.length();
        StringBuilder sb = new StringBuilder(cs);

        int i = -1;
        for (int j = 0 ; j < length; j++) {
            char c = sb.charAt(j);
            if (c == '{') {
                i = j;
            } else if (c == '}' && i > -1) {
                String key = sb.substring(i + 1, j);
                String value = StrUtil.toStringOrNull(replaceMap.get(key));
                if (value != null) {
                    sb.replace(i, j + 1, value);
                    length = sb.length();
                    j = i + value.length() - 1;
                }
                i = -1;
            }
        }
        return sb.toString();
    }



    /**
     * 该方法较慢，原理未知
     */
    @Deprecated
    private static String replace1(CharSequence cs, Map<String, ?> replaceMap) {
        int length = cs.length();
        StringBuilder sb = new StringBuilder(length + (length >> 2)); // *1.25

        int i = 0;
        for (int j = 0; j < length; j++) {
            char c = cs.charAt(j);
            sb.append(c);
            if (c == '{') {
                i = j;
            } else if (c == '}') {
                String key = cs.subSequence(i + 1, j).toString();
                String value = StrUtil.toStringOrNull(replaceMap.get(key));
                if (value != null) {
                    sb.replace(sb.length() - key.length() - 2, sb.length(), value);
                }
            }
        }
        return sb.toString();
    }



    private static void testReplace() {
        long current = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        obj.put("a", "aValue");
        obj.put("b", "bValue");
        String s = StrUtil.padPre("0", 100000, "0");
        String str = "{{ {c} {a} {b} {a}" + s;
        for (int i = 0; i < 10000; i++) {
            replace(str, obj);
//            replace1(str, obj);
        }
        System.out.println(replace(str, obj));
        System.out.println(System.currentTimeMillis() - current);
    }

}
