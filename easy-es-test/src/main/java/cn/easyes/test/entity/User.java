package cn.easyes.test.entity;

import cn.easyes.annotation.TableField;
import cn.easyes.common.constants.Analyzer;
import cn.easyes.common.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * es 嵌套类型
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @TableField(value = "user_name", analyzer = Analyzer.IK_SMART)
    private String username;
    @TableField(exist = false)
    private Integer age;
    /**
     * 多级嵌套
     */
    @TableField(fieldType = FieldType.NESTED, nestedClass = Faq.class)
    private Set<Faq> faqs;
}
