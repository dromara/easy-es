package com.xpc.easyes.sample.entity;

import com.xpc.easyes.core.anno.HighLightMappingField;
import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.enums.FieldStrategy;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * es 数据模型
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
public class Document {
    /**
     * es中的唯一id
     */
    private String id;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 文档内容
     */
    private String content;
    /**
     * 作者 加@TableField注解,并指明strategy = FieldStrategy.NOT_EMPTY 表示更新的时候的策略为 创建者不为空字符串时才更新
     */
    @TableField(strategy = FieldStrategy.NOT_EMPTY)
    private String creator;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
    /**
     * es中实际不存在的字段,但模型中加了,为了不和es映射,可以在此类型字段上加上 注解@TableField,并指明exist=false
     */
    @TableField(exist = false)
    private String notExistsField;
    /**
     * 地理位置经纬度坐标 例如: "40.13933715136454,116.63441990026217"
     */
    private String location;
    /**
     * 图形
     */
    private String geoLocation;
    /**
     * 自定义字段
     */
    @TableField(value = "wu-la")
    private String customField;

    /**
     * 高亮返回值被映射的字段
     */
    @HighLightMappingField("customField")
    private String highlightContent;
}
