package com.chen.jatool.common.exception;

import lombok.Getter;

import java.util.List;

/**
 * @author chenwh3
 */
public class WarnException extends ServiceException {

    /**
     * 警告等级，无特别意义，默认0
     * 在个别业务用用户可自行定义等级高低的意义
     */
    @Getter
    private Integer level;

    @Getter
    private List<String> errorList;


    public WarnException(String message) {
        super(message);
    }

    public WarnException(List<String> errorList) {
        super(errorList.toString());
        this.errorList = errorList;
    }

    public WarnException(Integer level, String message, Object... objects) {
        super(message, objects);
        this.level = level;
    }
    public WarnException(String message, Object... objects) {
        this(0, message, objects);
    }


}
