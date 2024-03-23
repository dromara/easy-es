package org.dromara.easyes.test.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.InnerIndexField;
import org.dromara.easyes.annotation.MultiIndexField;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;

/**
 * 问答
 *
 * @ProductName: Hundsun HEP
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.sample.entity
 * @Description: note
 * @Author: xingpc37977
 * @Date: 2022/5/12 17:18
 * @UpdateUser: xingpc37977
 * @UpdateDate: 2022/5/12 17:18
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2022 Hundsun Technologies Inc. All Rights Reserved
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faq {
    /**
     * 问题 高亮内容直接覆盖在原字段值上进行返回,故不需要指定高亮注解中的mappingField
     */
    @HighLight
    @MultiIndexField(mainIndexField = @IndexField,
            otherIndexFields = {
                    @InnerIndexField(suffix = "ik", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_MAX_WORD),
                    @InnerIndexField(suffix = "py", fieldType = FieldType.TEXT, analyzer = Analyzer.PINYIN)
            })
    private String faqName;

    /**
     * 答案
     */
    @IndexField(value = "answer")
    private String faqAnswer;
}
