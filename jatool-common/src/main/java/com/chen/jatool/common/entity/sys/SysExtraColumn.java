package com.chen.jatool.common.entity.sys;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录表额外字段信息实体
 *
 * @author chenwh3
 */
@Data
@TableName("sys_extra_column")
public class SysExtraColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 业务id
     */
    @TableField(value = "busId")
    private Integer busId;

    /**
     * 表名
     */
    @TableField(value = "tableName")
    private String tableName;

    /**
     * 列名
     */
    @TableField(value = "col")
    private String col;

    /**
     * 值
     */
    @TableField(value = "value")
    private String value;

    /**
     * 更新人
     */
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "updateAcc", fill = FieldFill.INSERT_UPDATE)
    private String updateAcc;

    /**
     * 历史标记 ; 0-否,1-是
     */
    @TableField(value = "hisTag")
    private Boolean hisTag;

}