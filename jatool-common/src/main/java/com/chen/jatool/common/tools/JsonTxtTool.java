package com.chen.jatool.common.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chen.jatool.common.utils.CollUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonTxtTool {


    // [2025-09-19 12:46:06.265] [INFO]
    private static Pattern pattern = Pattern.compile("params = (.*)");


    public static void process(String str) {
        Matcher matcher = pattern.matcher(str);
        List<JSONObject> jsonObjects = new ArrayList<>();
        while (matcher.find()) {
            JSONArray objects = JSONUtil.parseArray(matcher.group(1));
            jsonObjects.addAll(objects.toList(JSONObject.class));
        }

        List<JSONObject> collect = jsonObjects.stream().filter(CollUtils.distinctByKeys(e -> e.getStr("matCode"), e -> e.getStr("pkgMatCode"), e -> e.getStr("werk")))
                .collect(Collectors.toList());


        JSONArray objects = JSONUtil.parseArray(collect);

    }

    public static void main(String[] args) {
        String s = FileUtil.readString(new File("C:\\Users\\chenwh3\\Desktop\\Temp\\json.txt"), StandardCharsets.UTF_8);
        process(s);
    }


}
