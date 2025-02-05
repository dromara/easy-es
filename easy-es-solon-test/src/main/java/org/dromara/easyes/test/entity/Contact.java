package org.dromara.easyes.test.entity;


import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.rely.FieldType;


/**
 * 联系方式 数据模型 Author的子文档,Author是其父文档,Document是其爷文档
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
@Data
public class Contact {
    /**
     * 联系人id
     */
    @IndexId
    private String contactId;
    /**
     * 地址
     */
    @IndexField(fieldType = FieldType.TEXT)
    private String address;
}
