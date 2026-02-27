package org.dromara.easyes.test.entity;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;

/**
 * @author MoJie
 * @since 2025-09-19
 */
@Data
public class BaseEsEntity {

    /**
     * 文档ID，使用UUID策略自动生成
     */
    @IndexId(type = IdType.UUID)
    private String id;

    /**
     * 创建时间
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String createTime;

    /**
     * 创建人
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String createBy;

    /**
     * 更新时间
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String updateTime;

    /**
     * 更新人
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String updateBy;

    /**
     * 是否删除（逻辑删除标记）
     */
    @IndexField(fieldType = FieldType.BOOLEAN)
    private Boolean deleted = false;

    /**
     * 版本号（用于乐观锁）
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long version = 1L;
}
