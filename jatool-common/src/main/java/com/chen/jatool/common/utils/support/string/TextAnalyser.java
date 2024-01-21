package com.chen.jatool.common.utils.support.string;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.chen.jatool.common.exception.ServiceException;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TextAnalyser {

    private List<Map<String, Object>> list = new ArrayList<>();

    private String text;

    private int index = 0;

    TextAnalyser(String text) {
        this.text = text;
    }

    public static TextAnalyser of(String text) {
        return new TextAnalyser(text);
    }

    public class ResWrapper {

        private StrRes res;

        ResWrapper(StrRes res) {
            this.res = res;
        }

        public boolean isExist() {
            return res != null && res.left != -1 && res.right != -1;
        }

        public boolean contains(String str) {
            return text.substring(res.left, res.right).contains(str);
        }

        public void append(String str) {
            replaceList.add(new ReplaceResult(res.right, res.right, str));
        }

        public void replace(String str) {
            replaceList.add(new ReplaceResult(res.left, res.right, str));
        }

        @Override
        public String toString() {
            return res.toString();
        }
    }

    public class ListResWrapper {

        @Getter
        private List<ResWrapper> resList;

        ListResWrapper(List<StrRes> res) {
            if (res == null) {
                resList = new ArrayList<>();
            } else {
                resList = res.stream().map(ResWrapper::new).collect(Collectors.toList());
            }
        }

        public boolean anyMatch(String str) {
            return isExist() && resList.stream().anyMatch(e -> e.contains(str));
        }

        public boolean isExist() {
            return !resList.isEmpty();
        }

        public ResWrapper getLast() {
            return resList.get(resList.size() - 1);
        }
    }

    public List<ResultWrapper> getResultList() {
        return list.stream().map(ResultWrapper::new).collect(Collectors.toList());
    }

    public class ResultWrapper {

        private Map<String, Object> result;

        public ResultWrapper(Map<String, Object> result) {
            this.result = result;
        }

        public ResWrapper getRes(String key) {
            return new ResWrapper((StrRes) result.get(key));
        }

        public ListResWrapper getResList(String key) {
            return new ListResWrapper((List<StrRes>) result.get(key));
        }
    }

    @Data
    public class ReplaceResult {

        private final int left;

        private final int right;

        private final String replaceStr;

        public ReplaceResult(int left, int right, String replaceStr) {
            this.left = left;
            this.right = right;
            this.replaceStr = replaceStr;
        }
    }

    public class StrRes {

        private final int left;

        private final int right;

        public StrRes(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return text.substring(this.left, this.right);
        }
    }

    public class TextResult {

        private Map<String, Object> obj = new HashMap<>();

        private int left;

        private int right;

        public TextResult(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public TextAnalyser finish() {
            list.add(obj);
            obj = null;
            index = Math.max(left, right);
            return TextAnalyser.this;
        }

        public TextResult putAsKey(String key) {
            if (right == -1) {
                return TextResult.this;
            }
            obj.put(key, new StrRes(left, right));
            left = right;
            right = -1;
            return TextResult.this;
        }

        public String getStr(String key) {
            StrRes tr = (StrRes) obj.get(key);
            if (tr == null) {
                return null;
            }
            return text.substring(tr.left, tr.right);
        }

        public TextResult putInKey(String key) {
            if (right == -1) {
                return TextResult.this;
            }
            obj.putIfAbsent(key, new ArrayList<>());
            obj.computeIfPresent(key, (k, v) -> {
                ((ArrayList) v).add(new StrRes(left, right));
                left = right;
                right = -1;
                return v;
            });
            return TextResult.this;
        }

        public TextResult rightExpend(String str) {
            int idx = text.indexOf(str, Integer.max(right, left));
            if (idx > 0) {
                this.right = idx + str.length();
            }
            return this;
        }

        public TextResult findRegex(String regex, int group) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find(Integer.max(right, left))) {
                this.left = matcher.start(group);
                this.right = matcher.end(group);
            }
            return this;
        }

        public TextResult peek(Consumer<TextResult> consumer) {
            consumer.accept(this);
            return this;
        }
    }

    public TextResult find(String str) {
        int idx = text.indexOf(str, index);
        if (idx > 0) {
            index = idx + str.length();
            return new TextResult(idx, index);
        }
        return new TextResult(-1, -1);
    }

    public TextResult findRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find(index)) {
            index = matcher.end();
            return new TextResult(matcher.start(), index);
        }
        return new TextResult(-1, -1);
    }

    private PriorityQueue<ReplaceResult> replaceList = new PriorityQueue<>(Comparator.comparingInt(ReplaceResult::getRight));

    public String finishWrite() {
        StringBuilder sb = new StringBuilder(text);
        int idx = 0;
        while (!replaceList.isEmpty()) {
            ReplaceResult poll = replaceList.poll();
            int left = poll.getLeft();
            int right = poll.getRight();
            if (left < idx) {
                throw new ServiceException("replace error , 不能重叠");
            }
            if (left != right) {
                sb.replace(left, right, poll.getReplaceStr());
            } else {
                sb.insert(right, poll.getReplaceStr());
            }
            idx = right;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private static final String REPLACEMENT = "-51.000000  77 2071 1 1224979098644774912 4 0 2133 2 1224979098644774913 0 2133 2 1224979098644774915 0 1718 2 1224979098644774914 1224979098644774912 4 0 31 2 1224979098644774914 360287970189639680 2170 3 1224979098644774915 1369094286720630817 1224979098644774914 2123 3 1224979098644774913 1224979098644774915 3 2120 3 1224979098644774913 1224979098644774913 1 5 0 110 1 1224979098644774914 2170 3 1224979098644774915 1369094286720630817 1224979098644774914 2123 3 1224979098644774913 1224979098644774915 3 3 0 30 2 1224979098644774913 1 1726 2 1224979098644774914 1224979098644774912 1700 1 1224979098644774916 2133 2 1224979098644774919 288230376151711744 7 3 1224979098644774920 0 4 1804 3 1224979098644774921 1224979098644774912 1224979098644774920 4 0 31 2 1224979098644774921 1224979098644774914 1542 3 1224979098644774922 1224979098644774914 1224979098644774920 5 0 1073741857 3 1224979098644774921 288230376151711748 288230376151711749 1073741857 3 1224979098644774921 288230376151711779 288230376151711783 31 2 1224979098644774921 288230376151711944 4 0 2133 2 1224979098644774919 1224979098644774921 4 0 30 2 1224979098644774915 1 1825 3 1224979098644774928 1224979098644774912 1224979098644774920 2105 2 1224979098644774928 1 1776 3 1224979098644774912 1224979098644774921 1224979098644774928 3 0 3 0 3 0 31 2 1224979098644774921 1224979098644774914 3 0 4 0 31 2 1224979098644774919 288230376151711744 2133 2 1224979098644774919 288230376151711944 3 0 30 2 1224979098644774913 1 700 2 20 1 1506 2 1224979098644774929 1224979098644774914 2124 1 100 2133 2 1224979098644774931 4900 527 3 1224979098644774932 1224979098644774914 47 2107 2 1224979098644774932 38 2105 2 1224979098644774931 1224979098644774932 2170 3 1224979098644774915 1369094286720630817 1224979098644774914 2107 2 1224979098644774915 100 2105 2 1224979098644774931 1224979098644774915 2176 3 1224979098644774934 1224979098644774914 3 2107 2 1224979098644774934 10 2105 2 1224979098644774931 1224979098644774934 6 3 1224979098644774920 0 1224979098644774913 1709 2 3 1224979098644774912 700 2 2 1 718 2 2 3 2136 3 1224979098644774918 0 21 2121 3 1224979098644774918 10 1224979098644774918 720 2 2 1224979098644774918 2136 3 1224979098644774918 0 21 2121 3 1224979098644774918 10 1224979098644774918 722 2 2 1224979098644774918 2136 3 1224979098644774918 0 2 2121 3 1224979098644774918 1 1224979098644774918 725 2 2 1224979098644774918 2136 3 1224979098644774918 0 2 2121 3 1224979098644774918 1 1224979098644774918 723 2 2 1224979098644774918 1829 7 1224979098644774912 2 1224979098644774931 1224979098644774914 1224979098644774922 1224979098644774919 0 3 0 3 0 ";

    public static void main(String[] args) {
        String find = "itm_tutorial_short_bow\n" +
                "itm_tutorial_crossbow\n" +
                "itm_practice_bow\n" +
                "itm_practice_crossbow\n" +
                "itm_tutorial_short_bow\n" +
                "itm_tutorial_crossbow\n" +
                "itm_hunting_bow\n" +
                "itm_short_bow\n" +
                "itm_nomad_bow\n" +
                "itm_long_bow\n" +
                "itm_khergit_bow\n" +
                "itm_strong_bow\n" +
                "itm_war_bow\n" +
                "itm_hunting_crossbow\n" +
                "itm_light_crossbow\n" +
                "itm_crossbow\n" +
                "itm_heavy_crossbow\n" +
                "itm_sniper_crossbow\n" +
                "itm_dunu\n" +
                "itm_crusaders_crossbows_c\n" +
                "itm_practice_bow_2\n" +
                "itm_shenbi_crossbow\n" +
                "itm_tiaodeng_crossbow\n" +
                "itm_song_bow\n" +
                "itm_longweigong\n" +
                "itm_longshougong\n" +
                "itm_zilangong\n" +
                "itm_linglong_qingyungong\n" +
                "itm_kalmyk_naluch\n" +
                "itm_steelbow\n" +
                "itm_elfbow\n" +
                "itm_linglong_qibingnu\n" +
                "itm_bogmir_yumi\n" +
                "itm_sniper_crossbow_jiawen\n" +
                "itm_saracin_bow_a\n" +
                "itm_saracin_bow_b\n" +
                "itm_saracin_bow_c\n" +
                "itm_wooden_mortis_light_crossbow2\n" +
                "itm_elven_bow\n" +
                "itm_triangle_bow\n" +
                "itm_javelin_bow\n";
        String s = FileUtil.readUtf8String("C:\\Users\\84999\\Desktop\\test\\item_kinds1.txt");
        String[] split = find.split("[\r\n]+");

        TextAnalyser analyser = TextAnalyser.of(s);
        for (String findStr : split) {
            analyser.find(findStr).rightExpend("\r\n").putAsKey("weaponMsg")
                    .findRegex("\\v+(\\d+)\\v+", 1).putAsKey("triggerNum")
                    .peek(e -> {
                        Integer triggerNum = Convert.toInt(e.getStr("triggerNum"), 0);
                        if (triggerNum >= 0) {
                            for (Integer i = 0; i < triggerNum; i++) {
                                e.findRegex("\\v+(.*\\v+)", 1).putInKey("triggers");
                            }
                        }
                    }).finish();
        }
        for (ResultWrapper resultWrapper : analyser.getResultList()) {
            ResWrapper triggerNumStr = resultWrapper.getRes("triggerNum");
            ListResWrapper triggers = resultWrapper.getResList("triggers");
            int triggerNum = Convert.toInt(triggerNumStr.toString(), -1);

            if (triggers.anyMatch(REPLACEMENT)) {
                triggerNumStr.replace(triggerNum + 1 + "");
                triggers.getLast().append("\r\n" + REPLACEMENT);
            } else {
                triggerNumStr.replace("1\r\n" + REPLACEMENT);
            }
        }
        System.out.println(analyser.finishWrite());
    }
}
