package com.chen.jatool.common.utils.support.string;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author chenwh3
 */
public class MessageBuilder {
    private String title;

    private Collection<String> list = new ArrayList<>();

    private String separator = "";
    private String tail;

    private boolean distinct = false;

    public MessageBuilder distinct(){
        list = new LinkedHashSet<>();
        return this;
    }

    public boolean containMsg(){
        return !list.isEmpty();
    }

    public MessageBuilder() {
    }

    public void setTitle(String title,String ...val) {
        this.title = StrUtil.format(title, val);
    }

    public void setTail(String tail,String ...val) {
        this.tail = StrUtil.format(tail, val);
    }

    public MessageBuilder(String title, String tail) {
        this.title = title;
        this.tail = tail;
    }

    public MessageBuilder(String title, String separator , String tail) {
        this.title = title;
        this.tail = tail;
        this.separator = separator;
    }

    public void append(String message, Object... val) {
        if (StrUtil.isBlank(message)) {
            return;
        }
        addMsg(message, val);
    }

    private void addMsg(String message , Object...val){
        String msg = StrUtil.format(message, val);
        list.add(msg);
    }

    private String getContent(){
        StringBuilder sb = new StringBuilder();
        for (String s : list) { sb.append(s); }
        return sb.toString();
    }


    public void appendln(String message, Object... val) {
        if (StrUtil.isBlank(message)) {
            return;
        }
        append(message + separator, val);
    }

    public String toStringWithoutHtml(){
        String s = toString();
        s = s.replaceAll("<br/>|<pre>", "\r\n");
        s = s.replaceAll("</pre>", "");
        return s;
    }

    @Override
    public String toString() {
        String fullMessage = getContent();

        if (!fullMessage.isEmpty()) {
            if (!title.isEmpty()) {
                fullMessage = title + fullMessage;
            }

            if (!tail.isEmpty()) {
                fullMessage = StrUtil.removeSuffix(fullMessage,separator);
                fullMessage += tail;
            }
        }

        return fullMessage;
    }
}