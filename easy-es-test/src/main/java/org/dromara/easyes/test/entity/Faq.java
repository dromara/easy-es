package org.dromara.easyes.test.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.IndexField;

/**
 * 文件描述
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
    private String faqName;
    @IndexField(value = "answer")
    private String faqAnswer;
}
