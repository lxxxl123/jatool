package com.chen.jatool.common.utils.support.string;


import com.chen.jatool.common.utils.support.string.strfinder.StrPairFinder;


/**
 * @author chenwh3
 */
public class SqlServerPairFinder extends StrPairFinder {


    public SqlServerPairFinder(String s) {
        super(s);
    }

    public static StrPairFinder of(String s) {
        return new SqlServerPairFinder(s);
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
