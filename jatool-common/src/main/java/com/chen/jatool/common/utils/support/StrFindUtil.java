package com.chen.jatool.common.utils.support;


import com.chen.jatool.common.utils.support.string.strfinder.SkipStrPair;
import com.chen.jatool.common.utils.support.string.strfinder.StrHolder;

import java.util.NavigableSet;
import java.util.Stack;


public class StrFindUtil {

    public static boolean startWithWord(String content, String toFindStr, int startIndex, boolean ignoreCase, boolean wholeWord) {
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

    public static StrHolder findWithSkip(String content, String toFindStr, int startIndex, NavigableSet<SkipStrPair> skipPairs, boolean ignoreCase, boolean wholdWord) {
        int l = content.length();
        Stack<SkipStrPair> skipStack = new Stack<>();
        for (int i = startIndex; i < l; i++) {
            // before find , try pop
            if (!skipStack.isEmpty()) {
                if (startWithWord(content, skipStack.peek().right, i, ignoreCase, wholdWord)) {
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
                if (startWithWord(content, skipPair.left, i, ignoreCase, wholdWord)) {
                    skipStack.push(skipPair);
                    i = i + skipPair.left.length() - 1;
                    break;
                }
            }

            // find right
            if (skipStack.isEmpty()) {
                if (startWithWord(content, toFindStr, i, ignoreCase, wholdWord)) {
                    return new StrHolder(i, i + toFindStr.length());
                }
            }
        }
        return null;
    }
}
