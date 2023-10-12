package com.chen.jatool.common.exception;


import com.chen.jatool.common.modal.api.IResultCode;
import com.chen.jatool.common.modal.api.ResultCode;
import lombok.Getter;


/**
 * 业务异常
 *
 * @author Chill
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 2359767895161832954L;

    @Getter
    private final IResultCode resultCode;

    public ServiceException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILURE;
    }

    public ServiceException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ServiceException(IResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    /**
     * 提高性能
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public Throwable doFillInStackTrace() {
        return super.fillInStackTrace();
    }

}

