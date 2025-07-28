package com.chen.jatool.common.exception;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务异常
 *
 * @author chenwh3
 */
public class BatchExecuteException extends RuntimeException {

    @Getter
    private List<List<Object>> errorList ;

    @Getter
    private List<Exception> exList;

    public BatchExecuteException(List<List<Object>> errorList, List<Exception> exList) {
        super(buildErrorMessage(errorList, exList));
        this.errorList = errorList;
        this.exList = exList;
    }

    private static String buildErrorMessage(List<List<Object>> errorList, List<Exception> exList){
        if (exList.isEmpty()) {
            return StrUtil.format("errorList = {} ", JSONUtil.toJsonStr(errorList));
        }
        return StrUtil.format("errorList = {} , exMessage = [{}]", JSONUtil.toJsonStr(errorList), getErrorMessage(exList));
    }

    public String getErrorMessage(){
        return getErrorMessage(exList);
    }

    private static String getErrorMessage(List<Exception> exList) {
        return exList.stream().map(Exception::getLocalizedMessage).collect(Collectors.joining(","));
    }

    public List<Object> getFlatList(){
        return errorList.stream().flatMap(List::stream).collect(Collectors.toList());
    }



}

