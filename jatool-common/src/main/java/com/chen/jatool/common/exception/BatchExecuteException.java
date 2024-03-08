package com.chen.jatool.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 业务异常
 *
 * @author chenwh3
 */
@AllArgsConstructor
public class BatchExecuteException extends RuntimeException {

    @Getter
    private List<Object> errorList ;

    @Getter
    private List<String> errorMsg ;

}

