package com.chen.jatool.common.utils;

import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;

/**
 * 二进制工具
 * @author chenwh3
 */
public class BinaryUtils {

    public static String to0xb(Object obj, Integer length) {
        String str ;
        if (obj instanceof Integer) {
            str = Integer.toBinaryString((Integer) obj);
            length = length == null ? 32 : length;
        } else if (obj instanceof Long) {
            str = Long.toBinaryString((Long) obj);
            length = length == null ? 64 : length;
        } else {
            throw new ServiceException("不支持的类型");
        }
        return StrUtil.padPre(str, length, "0");
    }

    public static String to0xb(Object obj) {
        return to0xb(obj, null);
    }

    public static String toPretty0xb(Object obj) {
        return appendSpace(to0xb(obj));
    }


    /**
     * 从右到左添加空格
     */
    private static String appendSpace(String in){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            result.append(in.charAt(i));
            if ((in.length() - i - 1) % 4 == 0) {
                result.append(" ");
            }
        }

        return result.toString();
    }


}
