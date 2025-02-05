package org.dromara.easyes.test.entity;


import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.JoinField;


/**
 * es 评论 数据模型 Document的子文档,Document是其父文档
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class Comment {
    /**
     * 评论id
     */
    private String id;
    /**
     * 评论内容
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String commentContent;
}
