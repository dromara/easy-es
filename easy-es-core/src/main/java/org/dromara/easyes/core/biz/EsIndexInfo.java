package org.dromara.easyes.core.biz;


import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * es索引信息
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
public class EsIndexInfo {
    /**
     * 是否存在默认别名
     */
    private Boolean hasDefaultAlias;
    /**
     * 分片数
     */
    private Integer shardsNum;
    /**
     * 副本数
     */
    private Integer replicasNum;
    /**
     * 最大返回数
     */
    private Integer maxResultWindow;
    /**
     * 索引字段信息
     */
    private TypeMapping.Builder builder;
}
