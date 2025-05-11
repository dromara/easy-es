package org.dromara.easyes.test.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.InnerIndexField;
import org.dromara.easyes.annotation.MultiIndexField;
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
public class User {
    /**
     * 用户名
     */
    @HighLight(mappingField = "highlightUsername")
    @MultiIndexField(mainIndexField = @IndexField("user_name"),
    otherIndexFields = {
            @InnerIndexField(suffix = "ik", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_MAX_WORD),
            @InnerIndexField(suffix = "py", fieldType = FieldType.TEXT, analyzer = Analyzer.PINYIN)
    })
    private String username;
    /**
     * 年龄
     */
    @IndexField(fieldType = FieldType.INTEGER)
    private Integer age;
    /**
     * 密码
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String password;
    /**
     * 多级嵌套
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = Faq.class)
    private Set<Faq> faqs;
    /**
     * 高亮显示的内容
     */
    private String highlightUsername;

    public User(String username, Integer age, String password, Set<Faq> faqs) {
        this.username = username;
        this.age = age;
        this.password = password;
        this.faqs = faqs;
    }
}
