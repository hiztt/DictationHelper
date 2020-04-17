package com.dictation.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName BookVersion
 * @Description
 * @Author zlc
 * @Date 2020-04-13 14:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tbl_bookversion")
public class Version {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String versionName;

    private Integer versionType;

    private Date versionPublish;

    @com.baomidou.mybatisplus.annotation.Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
