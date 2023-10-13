package com.chen.jatool.common.utils;

/**
 * @author chenwh3
 */
public class SqlServerFinder extends StrFinder {


    public SqlServerFinder(String s) {
        super(s);
    }

    public static StrFinder of(String s) {
        return new SqlServerFinder(s);
    }

    {
        skipPair("begin", "end", -1);
        skipPair("(", ")", 3);
        skipPair("[", "]", 4);
        skipPair("'", "'", 4);
        ignoreCase();
    }


//    public static void main(String[] args) {
//        System.out.println(of("seleCt 1 , (select 'from (' from dual) as [from]  from tableA")
//                .left("select")
//                .right("from")
//                .replaceWholeGroup("select count(*) from"));
//
//
//    }
}
