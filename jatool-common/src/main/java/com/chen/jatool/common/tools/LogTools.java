package com.chen.jatool.common.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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




    }

    private static final int BATCH_SIZE = 5_000_000;

    public static void readLargeFileInBatches(String filePath,
                                              Consumer<StringBuffer> batchProcessor)
            throws IOException {

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            StringBuffer batch = new StringBuffer(BATCH_SIZE);
            String line;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                batch.append(line).append("\n");

                // 达到50万行时处理批次
                if (batch.length() >= BATCH_SIZE) {
                    batchCount++;
                    processBatch(batch, batchProcessor, batchCount);
                    batch.setLength(0); // 清空列表重用

                    // 可选：强制GC避免内存溢出
                    if (batchCount % 10 == 0) {
                        System.gc();
                    }
                }
            }

            // 处理最后一批（不足50万行）
            if (batch.length() > 0) {
                batchCount += batch.length();
                processBatch(batch, batchProcessor, batchCount);
            }

            System.out.printf("处理完成！字数: %,d, 批次: %d%n", batchCount, batchCount);
        }
    }

    private static void processBatch(StringBuffer batch,
                                     Consumer<StringBuffer> processor,
                                     int batchCount) {
        long startTime = System.currentTimeMillis();

        System.out.printf("开始处理批次 %d, 字数: %,d%n",
                batchCount, batch.length());

        processor.accept(batch); // 创建副本避免修改原数据

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("批次 %d 处理完成，耗时: %d ms%n", batchCount, duration);
    }

    public static void main(String[] args) {
        try {
            readLargeFileInBatches("C:\\Users\\chenwh3\\Desktop\\Temp\\catalina.out", e -> processBytes(e.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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


}
