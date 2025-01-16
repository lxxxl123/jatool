package com.chen.jatool.common.interceptor.holder;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.utils.support.Numbers;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author chenwh3
 */
@Slf4j
public class StopWatchHolder {

    private static final ThreadLocal<StopWatch> LOCAL = ThreadLocal.withInitial(StopWatch::new);

    private StopWatchHolder() {}


    public static void start(String taskName, Object... obj) {
        StopWatch stopWatch = LOCAL.get();
        tryStop();
        int taskCount = stopWatch.getTaskCount();
        String msg = StrUtil.format(taskName, obj);
        if (taskCount == 0) {
            log.info(msg);
        }
        stopWatch.start(msg);
    }

    public static void startNoLog(String taskName, Object... obj) {
        StopWatch stopWatch = LOCAL.get();
        tryStop();
        String msg = StrUtil.format(taskName, obj);
        stopWatch.start(msg);

    }

    public static void tryStop(){
        StopWatch stopWatch = LOCAL.get();
        if(stopWatch.isRunning()){
            stopWatch.stop();
        }
    }

    public static void clear() {
        tryStop();
        LOCAL.set(new StopWatch());
    }

    public static String prettyPrint() {
        tryStop();
        return LOCAL.get().prettyPrint(TimeUnit.MILLISECONDS);
    }



    private static String simplePrint(StopWatch sw, TimeUnit timeUnit) {
        long total = sw.getTotal(timeUnit);
        StopWatch.TaskInfo[] taskInfo = sw.getTaskInfo();
        StringBuilder sb = new StringBuilder();
        sb.append(StrUtil.format("total cost = {}{} . ", total, DateUtil.getShotName(timeUnit)));
        for (StopWatch.TaskInfo info : taskInfo) {
            long part = info.getTime(timeUnit);
            String taskName = info.getTaskName();
            sb.append(StrUtil.format("[{} ; {}{} ; {}]", Numbers.of(info.getTimeNanos()).divide(sw.getTotalTimeNanos(),4).format("00.00%"), Numbers.of(part).format("00000"), DateUtil.getShotName(timeUnit), taskName));
            sb.append(" | ");
        }
        sb.replace(sb.length() - " | ".length(), sb.length(), "");
        return sb.toString();
    }

    public static String clearAndSimplePrint() {
        tryStop();
        String s = simplePrint(LOCAL.get(), TimeUnit.MILLISECONDS);
        clear();
        return s;
    }

    public static void flushAndLog() {
        tryStop();
        StopWatch sw = LOCAL.get();
        if (sw.getTaskInfo().length == 0) {
            return;
        }
        log.info(simplePrint(sw, TimeUnit.MILLISECONDS));
        clear();
    }

    public static String clearAndPrettyPrint() {
        String prettyPrint = prettyPrint();
        clear();
        return prettyPrint;
    }

    public static boolean isRunning(){
        StopWatch stopWatch = LOCAL.get();
        return stopWatch.isRunning();
    }

}
