package org.dromara.easyes.test.entity;


import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.common.join.BaseJoin;


/**
 * 作者 数据模型 Document的子文档,Document是其父文档
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
@Data
public class Author extends BaseJoin {
    /**
     * 作者id
     */
    @IndexId
    private String authorId;
    /**
     * 作者姓名
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String authorName;
}
