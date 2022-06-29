package cn.easyes.test.entity;

import cn.easyes.annotation.IndexField;
import cn.easyes.annotation.IndexName;
import cn.easyes.common.constants.Analyzer;
import cn.easyes.common.enums.FieldType;
import cn.easyes.common.params.JoinField;
import lombok.Data;

/**
 * es 评论 数据模型 Document的子文档,Document是其父文档
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@IndexName(child = true)
public class Comment {
    /**
     * 评论id
     */
    private String id;
    /**
     * 评论内容
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String commentContent;
    /**
     * 父子关系字段
     */
    @IndexField(fieldType = FieldType.JOIN)
    private JoinField joinField;
}
