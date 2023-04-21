package org.dromara.easyes.test.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;

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
    @IndexField(value = "user_name", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART)
    private String username;
    @IndexField(fieldType = FieldType.INTEGER)
    private Integer age;

    @IndexField(fieldType = FieldType.KEYWORD)
    private String password;
    /**
     * 多级嵌套
     */
    @IndexField(fieldType = FieldType.NESTED, nestedClass = Faq.class)
    private Set<Faq> faqs;
}
