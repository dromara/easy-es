package cn.easyes.core.biz;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

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
     * 索引字段信息
     */
    private Map<String, Object> mapping;
}
