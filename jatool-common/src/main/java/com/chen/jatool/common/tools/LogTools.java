package com.chen.jatool.common.tools;

import cn.hutool.core.io.FileUtil;

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

public class LogTools {


    private static Map<String, AtomicInteger> map = new HashMap<>();
    private static Map<String, Map<String, AtomicInteger>> subMap = new HashMap<>();

    // [2025-09-19 12:46:06.265] [INFO]
    private static Pattern pattern = Pattern.compile("(?m)^\\[[\\d\\-. :]+] \\[\\w{4}] \\[T:.*?] \\[C:(.*?)]:(\\D{5,20})");
    private static Pattern removePth = Pattern.compile("[\\[-][a-z]{1,}$");


    public static void processBytes(String str) {
        Matcher matcher = pattern.matcher(str);
        String lastClass = null;
        String lastSubName = null;
        int lastStart = 0;
        while (matcher.find()) {
            String className = matcher.group(1);
            String subName = matcher.group(2);
            subName = removePth.matcher(subName).replaceAll("");
            int start = matcher.start();
            if (lastStart > 0) {
                map.computeIfAbsent(lastClass, k -> new AtomicInteger(0))
                        .addAndGet(start - lastStart);
                subMap.computeIfAbsent(lastClass, k -> new HashMap<>())
                        .computeIfAbsent(lastSubName, k -> new AtomicInteger(0))
                        .addAndGet(start - lastStart);
            }

            lastClass = className;
            lastSubName = subName;
            lastStart = start;
        }

        map.computeIfAbsent(lastClass, k -> new AtomicInteger(0))
                .addAndGet(str.length() - lastStart);

        subMap.computeIfAbsent(lastClass, k -> new HashMap<>())
                .computeIfAbsent(lastSubName, k -> new AtomicInteger(0))
                .addAndGet(str.length() - lastStart);

        List<Map.Entry<String, AtomicInteger>> res = new ArrayList<>(map.entrySet()).stream()
                .sorted(Map.Entry.comparingByValue((e1, e2) -> e2.get() - e1.get()))
                .collect(Collectors.toList());


        for (Map.Entry<String, AtomicInteger> re : res) {
            System.out.println(re);
            Map<String, AtomicInteger> subRes = subMap.get(re.getKey());
            if (subRes != null) {
                List<Map.Entry<String, AtomicInteger>> subResList = new ArrayList<>(subRes.entrySet()).stream()
                        .sorted(Map.Entry.comparingByValue((e1, e2) -> e2.get() - e1.get()))
                        .collect(Collectors.toList());
                for (Map.Entry<String, AtomicInteger> subRe : subResList) {
                    System.out.println("\t" + subRe);
                }
            }

        }
    }

    public static void main(String[] args) {
        String s = FileUtil.readString(new File("C:\\Users\\chenwh3\\Desktop\\Temp\\info.2025-09-22.0.log"), StandardCharsets.UTF_8);
        processBytes(s);
    }


}
