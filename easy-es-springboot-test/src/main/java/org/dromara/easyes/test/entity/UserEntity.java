package org.dromara.easyes.test.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;

/**
 * @author MoJie
 * @since 2025-09-19
 */
@Data
@IndexName(value = "user")
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEsEntity {

    /**
     * 用户名（精确匹配）
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String username;

    /**
     * 姓名（支持分词搜索）
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String name;

    /**
     * 年龄
     */
    @IndexField(fieldType = FieldType.INTEGER)
    private Integer age;

    /**
     * 邮箱（精确匹配）
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String email;

    /**
     * 地址（支持分词搜索）
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String address;

    /**
     * 用户状态（0-禁用，1-启用）
     */
    @IndexField(fieldType = FieldType.INTEGER)
    private Integer status = 1;

    /**
     * 用户标签（支持多值）
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String[] tags;

    /**
     * 用户描述（支持分词搜索）
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String description;
}