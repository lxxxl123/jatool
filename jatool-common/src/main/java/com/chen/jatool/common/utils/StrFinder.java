package com.chen.jatool.common.utils;


import com.chen.jatool.common.exception.ServiceException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于查找字符串 以left开始 以right结束的值
 * 特点： 可以自定义多个跳过配对字符串，常见的如括号，单引号，双引号，[]等 或者 (begin,end) , ('if',';') ('<div>','<div/>')等...
 * 例子
 *  1.content = ”'9999 abc 123' abc“ | find = 'abc' | 会跳过第一个abc
 *  2.content = "( '9999 abc)abc' ) abc" | find 'abc' | 会跳过括号内的和单引号内的abc
 * 注意，默认开启wholeWord
 *  1.content = ”abcd“ | find = 'abc' | 找不到任何单词
 *
 * 用法
 *  1. String str = StrFinder.of(content).left(left).right(right).group()
 *
 *  // todo 忽略字符间的空格
 * @author chenwh3
 */
public class StrFinder {

    private final String content;

    private StrHolder left;

    private StrHolder right;

    /**
     * 跳过队列 ， 在优先级低的跳过状态下 ，会进入优先级高的跳过状态
     * 在跳过状态下 ， right方法的查找字符串会被忽略
     * 例子
     * 我要找一个From
     * 但是我不想要子查询中的From
     * select 1 , (select 'from (' from dual) as [from]  from tableA
     * 上面只有1个from是我要找的 ， 我可以设置优先级
     * '' 2
     * ( ) 1
     * [ ] 0
     * 注意
     * 1.暂时不支持转义字符
     * 2.相同优先级无法互相进入
     */
    private final TreeSet<SkipStrPair> skipPairs;


    @EqualsAndHashCode
    private static class SkipStrPair {
        private String left;
        private String right;

        private Pattern leftPattern;

        private Pattern rightPattern;

        // 优先级
        private int priority;

        private boolean leftRegex = false;

        private boolean rightRegex = false;

        private SkipStrPair(String left, String right, int priority) {
            this.left = left;
            this.right = right;
            this.priority = priority;
        }

        //todo 未实现，暂时不支持
        private SkipStrPair(String left, String right, int priority, boolean leftRegex, boolean rightRegex) {
            this.left = left;
            this.right = right;
            this.priority = priority;
            this.leftRegex = leftRegex;
            this.rightRegex = rightRegex;
            this.leftPattern = Pattern.compile(left);
            this.rightPattern = Pattern.compile(right);

        }
    }

    {
        Comparator<SkipStrPair> comparator = Comparator.comparing(e -> e.priority);
        comparator = comparator.thenComparing(e -> e.left);
        skipPairs = new TreeSet<>(comparator);
    }

    @ToString
    private static class StrHolder {
        private int left;
        private int right;

        private StrHolder(int left, int right) {
            this.left = left;
            this.right = right;
        }

        private StrHolder(int i) {
            this.left = i;
            this.right = i;
        }
    }


    public StrFinder skipPair(String left, String right, int priority) {
        skipPairs.add(new SkipStrPair(left, right, priority));
        return this;
    }


    public StrFinder(String s) {
        this.content = s;
    }


    public StrFinder left(int left) {
        this.left = new StrHolder(left);
        return this;
    }

    public StrFinder left(String left, int startIndex) {
        this.left = findWithSkip(left, startIndex);
        return this;
    }

    public StrFinder left(String left, int startIndex, int orElse) {
        try {
            this.left = findWithSkip(left, startIndex);
        } catch (Exception e) {
            this.left = new StrHolder(Math.min(content.length(), orElse));
        }
        return this;
    }

    public StrFinder left(String left) {
        return left(left, 0);
    }


    public StrFinder leftRegex(String left, int orElse) {
        Matcher matcher = Pattern.compile(left).matcher(content);
        if (matcher.find()) {
            this.left = new StrHolder(matcher.start(), matcher.end());
        } else {
            this.left = new StrHolder(orElse);
        }
        return this;
    }

    public StrFinder right(int right) {
        this.right = new StrHolder(right);
        return this;
    }


    private boolean ignoreCase = false;

    private boolean wholeWord = true;

    protected StrFinder ignoreCase() {
        ignoreCase = true;
        return this;
    }

    /**
     * 默认开启
     */
    protected StrFinder wholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
        return this;
    }


    private boolean startWith(String content, int startIndex, String toFindStr) {
        return startWithWord(content, startIndex, toFindStr, ignoreCase , wholeWord);
    }

    private StrHolder findWithSkip(String toFindStr, int startIndex) {
        int l = content.length();
        Stack<SkipStrPair> skipStack = new Stack<>();
        for (int i = startIndex; i < l; i++) {
            // before find , try pop
            if (!skipStack.isEmpty()) {
                if (startWith(content, i, skipStack.peek().right)) {
                    i = i + skipStack.peek().right.length() - 1;
                    skipStack.pop();
                    continue;
                }

            }
            // try push pair
            SkipStrPair currentPair = skipStack.isEmpty() ? null : skipStack.peek();
            NavigableSet<SkipStrPair> nextPairs = skipPairs;
            if (currentPair != null) {
                nextPairs = skipPairs.tailSet(currentPair, true);
            }
            for (SkipStrPair skipPair : nextPairs) {
                if (skipPair != currentPair && currentPair != null && currentPair.priority == skipPair.priority) {
                    continue;
                }
                if(startWith(content,i,skipPair.left)){
                    skipStack.push(skipPair);
                    i = i + skipPair.left.length() - 1;
                    break;
                }
            }

            // find right
            if (skipStack.isEmpty()) {
                if (startWith(content, i, toFindStr)) {
                    return new StrHolder(i, i + toFindStr.length());
                }
            }
        }
        throw new ServiceException("can not find [" + toFindStr + "]");

    }

    public StrFinder right(String right) {
        this.right = findWithSkip(right, left.right);
        return this;
    }


    public StrFinder right(String right, int orElse) {
        try {
            this.right = findWithSkip(right, left.right);
        } catch (Exception e) {
            this.right = new StrHolder(Math.min(content.length(), orElse));
        }
        return this;
    }

    public String group() {
        return content.substring(left.right, right.left);
    }

    public String wholeGroup() {
        return content.substring(left.left, right.right);
    }

    public String removeGroup() {
        return content.substring(0, left.left) + content.substring(right.right);
    }

    public String removeWholeGroup() {
        return content.substring(0, left.left) + content.substring(right.left);
    }

    public String replaceGroup(String replacement) {
        return content.substring(0, left.left) + replacement + content.substring(right.right);
    }

    public String replaceWholeGroup(String replacement) {
        return content.substring(0, left.left) + replacement + content.substring(right.right);
    }

    public String replaceGroup(Function<String, String> replace) {
        return content.substring(0, left.right) + replace.apply(group()) + content.substring(right.left);
    }

    public String replaceWholeGroup(Function<String, String> replace) {
        return content.substring(0, left.left) + replace.apply(wholeGroup()) + content.substring(right.right);
    }

    public static boolean startWithWord(String content, int startIndex, String toFindStr, boolean ignoreCase , boolean wholeWord) {
        int toFindLength = toFindStr.length();
        int contentLength = content.length();

        // Adjust the starting index if it's negative or exceeds the content length
        startIndex = Math.max(0, Math.min(startIndex, contentLength - 1));

        // Get the end index of the substring to compare
        int endIndex = startIndex + toFindLength;

        if (endIndex <= contentLength) {
            boolean matches = content.regionMatches(ignoreCase, startIndex, toFindStr, 0, toFindLength);
            if (matches && wholeWord) {
                // Check if word boundaries exist before and after the substring
                boolean hasWordBoundaryBefore = startIndex == 0
                        || !Character.isLetterOrDigit(content.charAt(startIndex - 1))
                        || !Character.isLetterOrDigit(toFindStr.charAt(0));

                boolean hasWordBoundaryAfter = endIndex == contentLength
                        || !Character.isLetterOrDigit(content.charAt(endIndex))
                        || !Character.isLetterOrDigit(toFindStr.charAt(toFindLength - 1));
                return hasWordBoundaryAfter && hasWordBoundaryBefore;
            }
            if (matches && !wholeWord) {
                return true;
            }
        }
        return false;
    }



}
